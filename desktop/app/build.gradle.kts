import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.net.URI

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("org.jetbrains.compose")
    alias(libs.plugins.kotlin.compose)
}

// Desktop packaging requires a full JDK with jpackage (Android Studio JBR lacks it)
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(project(":desktop:core-player"))
                implementation(project(":desktop:feature-ui"))
                implementation(compose.desktop.currentOs)
                implementation(compose.runtime)
                implementation(compose.material3)
                implementation(libs.koin.core)
            }
        }
    }
}

// ── FFmpeg Download ─────────────────────────────────────────────────────

val ffmpegVersion = "7.1"
val ffmpegDir = layout.buildDirectory.dir("ffmpeg").get().asFile
val isWindows = org.gradle.internal.os.OperatingSystem.current().isWindows
val isMacOS = org.gradle.internal.os.OperatingSystem.current().isMacOsX
val isLinux = org.gradle.internal.os.OperatingSystem.current().isLinux

val ffmpegFileName = if (isWindows) "ffmpeg.exe" else "ffmpeg"

val downloadFFmpeg by tasks.registering {
    group = "desktop"
    description = "Download FFmpeg $ffmpegVersion static binary for current OS"
    notCompatibleWithConfigurationCache("uses project object references")
    outputs.file(File(ffmpegDir, ffmpegFileName))

    doLast {
        ffmpegDir.mkdirs()
        val target = File(ffmpegDir, ffmpegFileName)
        if (target.exists()) {
            println("FFmpeg already downloaded: $target")
            return@doLast
        }

        val url = when {
            isMacOS -> "https://evermeet.cx/ffmpeg/ffmpeg-${ffmpegVersion}.zip"
            isWindows -> "https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip"
            isLinux -> "https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-amd64-static.tar.xz"
            else -> throw GradleException("Unsupported OS for FFmpeg download")
        }

        println("Downloading FFmpeg from $url ...")
        val tempFile = File(ffmpegDir, "ffmpeg-download.tmp")
        URI(url).toURL().openStream().use { input ->
            tempFile.outputStream().use { output -> input.copyTo(output) }
        }

        val tempExtractDir = File(ffmpegDir, "extract")
        tempExtractDir.mkdirs()

        when {
            isMacOS || isWindows -> {
                // ZIP extraction
                project.copy {
                    from(project.zipTree(tempFile))
                    into(tempExtractDir)
                }
                // Find ffmpeg binary in extracted contents
                val found = tempExtractDir.walk().find {
                    it.name == ffmpegFileName && it.isFile
                } ?: throw GradleException("FFmpeg binary not found in archive")
                found.copyTo(target, overwrite = true)
                target.setExecutable(true)
            }
            isLinux -> {
                // tar.xz extraction via ProcessBuilder
                val pb = ProcessBuilder("tar", "xf", tempFile.absolutePath, "-C", tempExtractDir.absolutePath)
                pb.inheritIO()
                val exitCode = pb.start().waitFor()
                if (exitCode != 0) throw GradleException("tar extraction failed")
                val found = tempExtractDir.walk().find {
                    it.name == "ffmpeg" && it.isFile
                } ?: throw GradleException("FFmpeg binary not found in archive")
                found.copyTo(target, overwrite = true)
                target.setExecutable(true)
            }
        }

        tempFile.delete()
        tempExtractDir.deleteRecursively()
        println("FFmpeg downloaded to: $target")
    }
}

// ── Inject FFmpeg into native distribution ──────────────────────────────

val injectFFmpeg by tasks.registering {
    group = "desktop"
    description = "Copy FFmpeg binary into the packaged distribution"
    notCompatibleWithConfigurationCache("uses project object references")
    dependsOn(downloadFFmpeg)

    doLast {
        val binDir = layout.buildDirectory.dir("compose/binaries/main").get().asFile
        if (!binDir.exists()) {
            println("!! No distribution found, skipping FFmpeg injection")
            return@doLast
        }

        val ffmpegSrc = File(ffmpegDir, ffmpegFileName)

        // Locate the runtime bin directory based on format
        val targets = when {
            isMacOS -> {
                // HMP.app/Contents/runtime/Contents/Home/bin/
                binDir.walk().filter {
                    it.isDirectory && it.name == "bin"
                        && it.absolutePath.contains("runtime")
                        && it.absolutePath.endsWith("Home/bin")
                }.toList()
            }
            isWindows -> {
                // HMP/runtime/bin/
                binDir.walk().filter {
                    it.isDirectory && it.name == "bin"
                        && it.parentFile?.name == "runtime"
                }.toList()
            }
            isLinux -> {
                // HMP/lib/runtime/bin/
                binDir.walk().filter {
                    it.isDirectory && it.name == "bin"
                        && it.parentFile?.name == "runtime"
                }.toList()
            }
            else -> emptyList()
        }

        for (target in targets) {
            val dest = File(target, ffmpegFileName)
            ffmpegSrc.copyTo(dest, overwrite = true)
            dest.setExecutable(true)
            println("OK Injected FFmpeg -> ${dest.absolutePath}")
        }
    }
}

// ── Application Configuration ───────────────────────────────────────────

compose.desktop {
    application {
        mainClass = "com.hmp.desktop.MainKt"

        jvmArgs += listOf(
            "-Xmx512m",
            "-Dfile.encoding=UTF-8"
        )

        nativeDistributions {
            modules += listOf(
                "java.net.http",       // Ktor HTTP client
                "jdk.unsupported",     // sun.misc.Unsafe
                "java.desktop",        // javax.sound
                "java.management",     // JMX
                "java.sql",            // Room/JDBC
                "java.transaction.xa"  // JDBC transactions
            )
            packageName = "HMP"
            packageVersion = project.findProperty("hmp.versionName")?.toString() ?: "1.0.0"
            description = "Hearable Music Player - A cross-platform local music player"
            vendor = "HMP"

            // Format must match current OS — plugin validates at configuration time
            if (isMacOS) {
                targetFormats(TargetFormat.Dmg)
            } else if (isWindows) {
                targetFormats(TargetFormat.Msi)
            } else {
                targetFormats(TargetFormat.Deb, TargetFormat.AppImage)
            }

            macOS {
                iconFile.set(project.file("src/desktopMain/resources/icon.icns"))
                bundleID = "com.hmp.desktop"
                dmgPackageVersion = "1"
            }

            windows {
                iconFile.set(project.file("src/desktopMain/resources/icon.ico"))
                menu = true
                dirChooser = true
                upgradeUuid = "6ec556dd-5375-494f-ab38-f19bcdb497e7"
            }

            linux {
                iconFile.set(project.file("src/desktopMain/resources/icon.png"))
                debMaintainer = "hmp@hearmusic.app"
            }
        }
    }
}

// Wire FFmpeg injection after packaging
tasks.matching { it.name == "packageDistributionForCurrentOS" }.configureEach {
    finalizedBy(injectFFmpeg)
}
