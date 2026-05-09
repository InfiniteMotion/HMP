plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(libs.koin.core)
                implementation(libs.kotlinx.coroutines)
            }
        }
    }
}
