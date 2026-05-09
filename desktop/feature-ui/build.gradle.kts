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
                implementation(compose.desktop.currentOs)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
                implementation(libs.koin.core)
                implementation(libs.koin.compose.multiplatform)
                implementation(libs.jaudiotagger)
                implementation(libs.kotlinx.coroutines)
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}

compose.resources {
    packageOfResClass = "com.hmp.desktop.generated.resources"
    publicResClass = true
}
