val buildTarget = System.getenv("HMP_BUILD_TARGET") ?: "all"

pluginManagement {
    repositories {
        if (System.getenv("CI") == "true") {
            gradlePluginPortal()
            google()
            mavenCentral()
        } else {
            maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
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
        if (System.getenv("CI") == "true") {
            google()
            mavenCentral()
        } else {
            maven { url = uri("https://maven.aliyun.com/repository/public") }
            maven { url = uri("https://maven.aliyun.com/repository/google") }
            google()
            mavenCentral()
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
    else -> {
        include(":android:app")
        include(":android:core-player")
        include(":android:feature-ui")
        include(":desktop:app")
        include(":desktop:core-player")
        include(":desktop:feature-ui")
    }
}
