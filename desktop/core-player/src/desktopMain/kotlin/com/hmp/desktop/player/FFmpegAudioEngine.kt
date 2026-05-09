package com.hmp.desktop.player

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine

class FFmpegAudioEngine : AudioEngine {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var ffmpegProcess: Process? = null
    private var sourceLine: SourceDataLine? = null
    private var playbackJob: Job? = null

    private var currentPath: String? = null
    private var isPaused = false
    private var isStopped = true
    private var durationMs: Long = 0L
    private var bytesWritten: Long = 0L
    private var sampleRate: Float = 44100f
    private var channels: Int = 2
    private var sampleSizeInBytes: Int = 2
    private var seekPositionMs: Long = 0L
    private var volume: Float = 1.0f

    override var onPlaybackComplete: (() -> Unit)? = null
    override var onError: ((Exception) -> Unit)? = null

    private val ffmpegPath: String
        get() {
            // Check common locations
            val candidates = listOf(
                "ffmpeg",
                "ffmpeg.exe",
                "/usr/bin/ffmpeg",
                "/usr/local/bin/ffmpeg",
                "${System.getProperty("user.home")}/ffmpeg/bin/ffmpeg"
            )
            for (candidate in candidates) {
                try {
                    val pb = ProcessBuilder(candidate, "-version")
                    pb.redirectErrorStream(true)
                    val p = pb.start()
                    p.waitFor()
                    if (p.exitValue() == 0) return candidate
                } catch (_: Exception) {
                    // Try next
                }
            }
            return "ffmpeg" // Fallback to PATH
        }

    override fun play(path: String) {
        stop()
        currentPath = path
        isStopped = false
        isPaused = false
        seekPositionMs = 0L
        bytesWritten = 0L

        // First, probe duration
        probeDuration(path)

        // Start playback
        startPlayback(path, 0L)
    }

    override fun pause() {
        if (!isPlaying()) return
        isPaused = true
        sourceLine?.stop()
    }

    override fun resume() {
        if (!isPaused) return
        isPaused = false
        sourceLine?.start()
    }

    override fun stop() {
        isStopped = true
        isPaused = false
        playbackJob?.cancel()
        playbackJob = null
        sourceLine?.let { line ->
            try {
                line.stop()
                line.close()
            } catch (_: Exception) {}
        }
        sourceLine = null
        ffmpegProcess?.let { process ->
            try {
                process.destroyForcibly()
            } catch (_: Exception) {}
        }
        ffmpegProcess = null
        currentPath = null
    }

    override fun seekTo(positionMs: Long) {
        val path = currentPath ?: return
        val wasPlaying = isPlaying() || isPaused
        if (!wasPlaying) return

        seekPositionMs = positionMs
        bytesWritten = calculateBytesForMs(positionMs)

        // Restart playback from the new position
        sourceLine?.let { line ->
            try {
                line.stop()
                line.flush()
                line.close()
            } catch (_: Exception) {}
        }
        sourceLine = null
        ffmpegProcess?.destroyForcibly()
        ffmpegProcess = null
        playbackJob?.cancel()

        startPlayback(path, positionMs)
    }

    override fun getCurrentPosition(): Long {
        if (isPaused) return seekPositionMs
        val bytesPerMs = (sampleRate * channels * sampleSizeInBytes / 1000.0).toLong()
        return if (bytesPerMs > 0) seekPositionMs + (bytesWritten / bytesPerMs) else 0L
    }

    override fun getDuration(): Long = durationMs

    override fun isPlaying(): Boolean = !isStopped && !isPaused && (ffmpegProcess?.isAlive == true)

    override fun isLoaded(): Boolean = currentPath != null && !isStopped

    override fun setVolume(volume: Float) {
        this.volume = volume.coerceIn(0f, 1f)
        // Volume control via SourceDataLine is limited; we apply gain in the PCM processing
    }

    override fun release() {
        stop()
    }

    private fun probeDuration(path: String) {
        try {
            val pb = ProcessBuilder(
                ffmpegPath, "-i", path,
                "-f", "null", "-"
            )
            pb.redirectErrorStream(true)
            val process = pb.start()
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()

            // Parse duration from ffmpeg output: Duration: HH:MM:SS.ss
            val durationRegex = Regex("""Duration:\s*(\d+):(\d+):(\d+)\.(\d+)""")
            durationRegex.find(output)?.let { match ->
                val hours = match.groupValues[1].toLong()
                val minutes = match.groupValues[2].toLong()
                val seconds = match.groupValues[3].toLong()
                val centiseconds = match.groupValues[4].toLong()
                durationMs = (hours * 3600 + minutes * 60 + seconds) * 1000 + centiseconds * 10
            }

            // Parse sample rate and channels from ffmpeg output
            val audioRegex = Regex("""(\d+) Hz.*?(mono|stereo)""")
            audioRegex.find(output)?.let { match ->
                sampleRate = match.groupValues[1].toFloat()
                channels = if (match.groupValues[2] == "mono") 1 else 2
            }
        } catch (_: Exception) {
            durationMs = 0L
        }
    }

    private fun startPlayback(path: String, seekMs: Long) {
        playbackJob = scope.launch {
            try {
                val seekArgs = if (seekMs > 0) {
                    listOf("-ss", String.format("%.3f", seekMs / 1000.0))
                } else {
                    emptyList()
                }

                val command = listOf(
                    ffmpegPath,
                    *seekArgs.toTypedArray(),
                    "-i", path,
                    "-f", "s16le",
                    "-acodec", "pcm_s16le",
                    "-ar", "44100",
                    "-ac", "2",
                    "-"
                )

                val pb = ProcessBuilder(command)
                pb.redirectErrorStream(false)
                val process = pb.start()
                ffmpegProcess = process

                val format = AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100f, 16, 2, 4, 44100f, false)
                val info = DataLine.Info(SourceDataLine::class.java, format)

                if (!AudioSystem.isLineSupported(info)) {
                    withContext(Dispatchers.Main) {
                        onError?.invoke(Exception("Audio line not supported"))
                    }
                    return@launch
                }

                val line = AudioSystem.getLine(info) as SourceDataLine
                sourceLine = line
                line.open(format)
                line.start()

                sampleRate = 44100f
                channels = 2
                sampleSizeInBytes = 2

                val buffer = ByteArray(8192)
                val audioStream = BufferedInputStream(process.inputStream)

                while (isActive && !isStopped) {
                    if (isPaused) {
                        Thread.sleep(50)
                        continue
                    }

                    val bytesRead = audioStream.read(buffer)
                    if (bytesRead == -1) break

                    // Apply volume if needed
                    if (volume < 1.0f) {
                        applyVolume(buffer, bytesRead, volume)
                    }

                    line.write(buffer, 0, bytesRead)
                    bytesWritten += bytesRead
                }

                line.drain()
                line.stop()
                line.close()

                audioStream.close()
                process.destroyForcibly()

                if (!isStopped && isActive) {
                    withContext(Dispatchers.Main) {
                        onPlaybackComplete?.invoke()
                    }
                }
            } catch (e: Exception) {
                if (!isStopped) {
                    withContext(Dispatchers.Main) {
                        onError?.invoke(e)
                    }
                }
            }
        }
    }

    private fun applyVolume(buffer: ByteArray, length: Int, vol: Float) {
        // Apply volume gain to 16-bit PCM samples
        var i = 0
        while (i < length - 1) {
            val sample = (buffer[i].toInt() and 0xFF) or (buffer[i + 1].toInt() shl 8)
            val adjusted = (sample.toShort() * vol).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i] = (adjusted and 0xFF).toByte()
            buffer[i + 1] = (adjusted shr 8).toByte()
            i += 2
        }
    }

    private fun calculateBytesForMs(ms: Long): Long {
        val bytesPerMs = sampleRate * channels * sampleSizeInBytes / 1000.0
        return (ms * bytesPerMs).toLong()
    }

    private suspend fun <T> withContext(context: kotlin.coroutines.CoroutineContext, block: suspend () -> T): T {
        return kotlinx.coroutines.withContext(context) { block() }
    }
}
