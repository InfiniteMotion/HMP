import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("org.jetbrains.compose")
    alias(libs.plugins.kotlin.compose)
}

group = "com.hmp"
version = "1.0.0"

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        compilerOptions {
            moduleName.set("storybook")
        }
        browser {
            commonWebpackConfig {
                outputFileName = "storybook.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            // Material Icons for Compose Multiplatform
            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
        }
    }
}

compose.resources {
    packageOfResClass = "hmp_storybook.generated.resources"
    publicResClass = true
}
