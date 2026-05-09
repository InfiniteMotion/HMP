import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("org.jetbrains.compose")
    alias(libs.plugins.kotlin.compose)
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

compose.desktop {
    application {
        mainClass = "com.hmp.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "HMP"
            packageVersion = project.findProperty("hmp.versionName")?.toString() ?: "1.0.0"
            description = "Hearable Music Player"
            vendor = "HMP"
        }
    }
}
