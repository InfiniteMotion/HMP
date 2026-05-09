import java.nio.file.Files
import java.nio.file.StandardCopyOption

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidx.room) apply false
    id("org.jetbrains.compose") version libs.versions.composeMultiplatform.get() apply false
}

val versionName: String = findProperty("hmp.versionName")?.toString() ?: "unknown"
val versionCode: String = findProperty("hmp.versionCode")?.toString() ?: "0"
val projectDirFile: File = projectDir
val isMacOS: Boolean = org.gradle.internal.os.OperatingSystem.current().isMacOsX

// ── Android ──────────────────────────────────────────────────────────────

tasks.register("releaseAndroid") {
    group = "release"
    description = "构建 Android Release APK + AAB，输出到 releases/android/"
    notCompatibleWithConfigurationCache("release copy task")

    dependsOn(":android:app:assembleRelease", ":android:app:bundleRelease")

    doLast {
        val outDir = projectDirFile.resolve("releases/android")
        outDir.mkdirs()

        val apkSrc = projectDirFile.resolve("android/app/build/outputs/apk/release/app-release.apk")
        val aabSrc = projectDirFile.resolve("android/app/build/outputs/bundle/release/app-release.aab")

        if (apkSrc.exists()) {
            Files.copy(apkSrc.toPath(), outDir.resolve("HMP-v${versionName}-release.apk").toPath(), StandardCopyOption.REPLACE_EXISTING)
            println("OK APK -> releases/android/HMP-v${versionName}-release.apk")
        } else {
            println("!! APK not found: $apkSrc")
        }

        if (aabSrc.exists()) {
            Files.copy(aabSrc.toPath(), outDir.resolve("HMP-v${versionName}-release.aab").toPath(), StandardCopyOption.REPLACE_EXISTING)
            println("OK AAB -> releases/android/HMP-v${versionName}-release.aab")
        } else {
            println("!! AAB not found: $aabSrc")
        }
    }
}

// ── iOS ──────────────────────────────────────────────────────────────────

tasks.register("releaseIos") {
    group = "release"
    description = "构建 iOS Release Archive (仅 macOS)"
    notCompatibleWithConfigurationCache("release copy task")

    if (isMacOS) {
        dependsOn(":shared:linkReleaseFrameworkIosArm64")

        doLast {
            val outDir = projectDirFile.resolve("releases/ios")
            outDir.mkdirs()

            val workspace = projectDirFile.resolve("ios/HMP.xcworkspace")
            val archivePath = outDir.resolve("HMP-v${versionName}.xcarchive").absolutePath

            val pb = ProcessBuilder(
                "xcodebuild",
                "-workspace", workspace.absolutePath,
                "-scheme", "HMP",
                "-configuration", "Release",
                "-sdk", "iphoneos",
                "-archivePath", archivePath,
                "archive"
            )
            pb.inheritIO()
            val exitCode = pb.start().waitFor()
            if (exitCode != 0) throw GradleException("xcodebuild failed with exit code $exitCode")
            println("OK Archive -> releases/ios/HMP-v${versionName}.xcarchive")
        }
    } else {
        doLast {
            println("!! iOS build requires macOS, skipped")
        }
    }
}

// ── Desktop ──────────────────────────────────────────────────────────────

tasks.register("releaseDesktop") {
    group = "release"
    description = "构建 Desktop Release 分发包，输出到 releases/desktop/"
    notCompatibleWithConfigurationCache("release copy task")

    dependsOn(":desktop:app:packageDistributionForCurrentOS")

    doLast {
        val outDir = projectDirFile.resolve("releases/desktop")
        outDir.mkdirs()

        val distDir = projectDirFile.resolve("desktop/app/build/compose/binaries/main")
        if (distDir.exists()) {
            distDir.listFiles()?.forEach { f ->
                if (f.isFile && (f.extension == "msi" || f.extension == "dmg" || f.extension == "deb")) {
                    Files.copy(f.toPath(), outDir.resolve(f.name).toPath(), StandardCopyOption.REPLACE_EXISTING)
                    println("OK Desktop -> releases/desktop/${f.name}")
                }
            }
        } else {
            println("!! Desktop distribution not found: $distDir")
        }
    }
}

// ── Storybook ────────────────────────────────────────────────────────────

tasks.register("releaseStorybook") {
    group = "release"
    description = "构建 Storybook 离线包，输出到 releases/storybook/"
    notCompatibleWithConfigurationCache("release copy task")

    dependsOn(":storybook:wasmJsBrowserProductionWebpack")

    doLast {
        val outDir = projectDirFile.resolve("releases/storybook")
        outDir.deleteRecursively()
        outDir.mkdirs()

        val webpackDir = projectDirFile.resolve("storybook/build/kotlin-webpack/wasmJs/productionExecutable")
        val resDir = projectDirFile.resolve("storybook/build/processedResources/wasmJs/main")

        webpackDir.listFiles()?.forEach { f ->
            Files.copy(f.toPath(), outDir.resolve(f.name).toPath(), StandardCopyOption.REPLACE_EXISTING)
        }

        val indexHtml = resDir.resolve("index.html")
        if (indexHtml.exists()) {
            Files.copy(indexHtml.toPath(), outDir.resolve("index.html").toPath(), StandardCopyOption.REPLACE_EXISTING)
        }

        val composeRes = resDir.resolve("composeResources")
        if (composeRes.exists()) {
            composeRes.copyRecursively(outDir.resolve("composeResources"), overwrite = true)
        }

        println("OK Storybook -> releases/storybook/")
    }
}

// ── 总入口 ───────────────────────────────────────────────────────────────

tasks.register("release") {
    group = "release"
    description = "构建所有平台 Release 产物"
    notCompatibleWithConfigurationCache("release copy task")

    dependsOn("releaseAndroid", "releaseDesktop", "releaseStorybook")
    if (isMacOS) dependsOn("releaseIos")

    doLast {
        val iosNote = if (!isMacOS) " (requires macOS)" else ""
        println("")
        println("=====================================")
        println("  HMP v${versionName} (build ${versionCode}) Release Done")
        println("=====================================")
        println("  Output:    releases/")
        println("  Android:   releases/android/")
        println("  Desktop:   releases/desktop/")
        println("  iOS:       releases/ios/${iosNote}")
        println("  Storybook: releases/storybook/")
        println("=====================================")
    }
}
