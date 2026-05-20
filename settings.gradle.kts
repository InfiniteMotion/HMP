val buildTarget = System.getenv("HMP_BUILD_TARGET") ?: "all"
val isCI = System.getenv("CI") == "true"

pluginManagement {
    repositories {
        if (!isCI) {
            maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        }
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        if (!isCI) {
            maven { url = uri("https://maven.aliyun.com/repository/public") }
            maven { url = uri("https://maven.aliyun.com/repository/google") }
        }
        google()
        mavenCentral()
        // Kotlin/Wasm — Node.js
        ivy {
            name = "Node.js Distributions"
            url = uri("https://nodejs.org/dist")
            patternLayout {
                artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
            }
            metadataSources { artifact() }
            content { includeModule("org.nodejs", "node") }
        }
        // Kotlin/Wasm — Yarn
        ivy {
            name = "Yarn Distributions"
            url = uri("https://github.com/yarnpkg/yarn/releases/download")
            patternLayout {
                artifact("v[revision]/[artifact](-v[revision]).[ext]")
            }
            metadataSources { artifact() }
            content { includeModule("com.yarnpkg", "yarn") }
        }
        // Kotlin/Wasm — Binaryen
        ivy {
            name = "Binaryen Distributions"
            url = uri("https://github.com/WebAssembly/binaryen/releases/download")
            patternLayout {
                artifact("version_[revision]/[artifact]-version_[revision]-[classifier].[ext]")
            }
            metadataSources { artifact() }
            content { includeModule("com.github.webassembly", "binaryen") }
        }
    }
}

rootProject.name = "HMP"
include(":shared")

when (buildTarget) {
    "desktop" -> {
        include(":desktop:app")
        include(":desktop:core-player")
        include(":desktop:feature-ui")
    }
    "android" -> {
        include(":android:app")
        include(":android:core-player")
        include(":android:feature-ui")
    }
    "storybook" -> {
        include(":android:app")
        include(":android:core-player")
        include(":android:feature-ui")
        include(":storybook")
    }
    else -> {
        include(":android:app")
        include(":android:core-player")
        include(":android:feature-ui")
        include(":desktop:app")
        include(":desktop:core-player")
        include(":desktop:feature-ui")
        include(":storybook")
    }
}
