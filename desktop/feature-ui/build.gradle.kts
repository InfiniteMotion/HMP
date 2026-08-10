plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("org.jetbrains.compose")
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.components.resources)
            }
        }

        val desktopMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(project(":shared"))
                implementation(project(":desktop:core-player"))
                implementation(compose.desktop.currentOs)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
                implementation(libs.koin.core)
                implementation(libs.koin.compose.multiplatform)
                implementation(libs.jaudiotagger)
                implementation(libs.kotlinx.coroutines)
                implementation(libs.kotlinx.serialization.json)
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
                implementation(libs.coil.compose.kmp)
                implementation(libs.coil.network.ktor3)
                implementation(libs.haze)
                implementation(libs.haze.materials)
            }
        }

        val desktopTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

compose.resources {
    packageOfResClass = "com.hmp.desktop.generated.resources"
    publicResClass = true
}