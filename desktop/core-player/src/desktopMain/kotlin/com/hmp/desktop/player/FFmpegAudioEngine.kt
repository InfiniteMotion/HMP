package com.hmp.desktop.player

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
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
            val isWindows = System.getProperty("os.name").lowercase().contains("win")
            val javaHome = System.getProperty("java.home") ?: ""
            val ffmpegName = if (isWindows) "ffmpeg.exe" else "ffmpeg"

            // Bundled ffmpeg (inside packaged distribution's runtime/bin)
            val bundledCandidates = if (javaHome.isNotEmpty()) {
                listOf(File(javaHome, "bin/$ffmpegName"))
            } else emptyList()

            // System-installed ffmpeg
            val systemCandidates = listOf(
                File("/usr/bin/$ffmpegName"),
                File("/usr/local/bin/$ffmpegName"),
                File("${System.getProperty("user.home")}/ffmpeg/bin/$ffmpegName")
            )

            for (candidate in bundledCandidates + systemCandidates) {
                if (candidate.exists() && candidate.canExecute()) {
                    return candidate.absolutePath
                }
            }

            // Fallback: try PATH
            return ffmpegName
        }

    override fun play(path: String) {
        stop()
        currentPath = path
        isStopped = false
        isPaused = false
        seekPositionMs = 0L
        bytesWritten = 0L

        // First, probe duration and audio format
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
    }

    override fun release() {
        stop()
    }

    /**
     * Probe the audio file's duration, sample rate, and channel count using FFmpeg.
     * Guarantees process cleanup via try-finally.
     */
    private fun probeDuration(path: String) {
        var process: Process? = null
        try {
            process = ProcessBuilder(
                ffmpegPath, "-i", path,
                "-f", "null", "-"
            )
                .redirectErrorStream(true)
                .start()

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

            // Parse sample rate from ffmpeg output: "44100 Hz" or "48000 Hz"
            val sampleRateRegex = Regex("""(\d+) Hz""")
            sampleRateRegex.find(output)?.let { match ->
                val parsed = match.groupValues[1].toFloatOrNull()
                if (parsed != null && parsed > 0) {
                    sampleRate = parsed
                }
            }

            // Parse channel layout: "stereo", "mono", or "5.1", "7.1" etc.
            val channelRegex = Regex("""Audio:.*?,\s*(\d+)\s+Hz""")
            channelRegex.find(output)?.let { match ->
                val parsed = match.groupValues[1].toFloatOrNull()
                if (parsed != null && parsed > 0) {
                    sampleRate = parsed
                }
            }

            // Parse channel count from layout string
            val channelLayoutRegex = Regex("""(mono|stereo|[\d.]+\s*channels?)""")
            channelLayoutRegex.find(output)?.let { match ->
                val layout = match.groupValues[1].trim()
                channels = when {
                    layout == "mono" -> 1
                    layout == "stereo" -> 2
                    layout.contains("channel") -> {
                        // Extract number: "5.1 channels" -> 6
                        val numStr = layout.replace("channels", "").trim()
                        val num = numStr.replace(".", "").toIntOrNull()
                        num?.coerceIn(1, 8) ?: 2
                    }
                    else -> 2
                }
            }
        } catch (_: Exception) {
            durationMs = 0L
            // Keep default sampleRate (44100) and channels (2) on probe failure
        } finally {
            process?.destroyForcibly()
        }
    }

    /**
     * Start FFmpeg-based audio playback via Java Sound API.
     * Uses probed sample rate and channel count from [probeDuration].
     * Consumes stderr on a separate thread to prevent buffer deadlock.
     */
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
                    "-ar", sampleRate.toInt().toString(),
                    "-ac", channels.toString(),
                    "-"
                )

                val pb = ProcessBuilder(command)
                pb.redirectErrorStream(false)
                val process = pb.start()
                ffmpegProcess = process

                // Consume stderr on a background thread to prevent buffer deadlock
                val stderrThread = Thread({
                    try {
                        process.errorStream.bufferedReader().readText()
                    } catch (_: Exception) {}
                }, "ffmpeg-stderr-${path.hashCode()}")
                stderrThread.isDaemon = true
                stderrThread.start()

                val frameSize = channels * sampleSizeInBytes
                val format = AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    sampleRate,
                    16,
                    channels,
                    frameSize,
                    sampleRate,
                    false
                )
                val info = DataLine.Info(SourceDataLine::class.java, format)

                if (!AudioSystem.isLineSupported(info)) {
                    kotlinx.coroutines.withContext(Dispatchers.Main) {
                        onError?.invoke(Exception("Audio line not supported for format: ${sampleRate.toInt()}Hz ${channels}ch"))
                    }
                    return@launch
                }

                val line = AudioSystem.getLine(info) as SourceDataLine
                sourceLine = line
                line.open(format)
                line.start()

                sampleSizeInBytes = 2

                val buffer = ByteArray(8192)
                val audioStream = BufferedInputStream(process.inputStream)

                while (isActive && !isStopped) {
                    if (isPaused) {
                        delay(50)
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
                    kotlinx.coroutines.withContext(Dispatchers.Main) {
                        onPlaybackComplete?.invoke()
                    }
                }
            } catch (e: Exception) {
                if (!isStopped) {
                    kotlinx.coroutines.withContext(Dispatchers.Main) {
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
}
