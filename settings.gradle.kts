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
        include(":shared-ui")
        include(":desktop:app")
        include(":desktop:core-player")
        // 仅为 shared-ui androidMain 的 project() 声明提供存在性（configure-on-demand 下
        // desktop 任务链不解析 android 变体，本模块不会被配置，无需 Android SDK）
        include(":android:core-player")
    }
    "android" -> {
        include(":android:app")
        include(":android:core-player")
        include(":shared-ui")
        // 仅为 shared-ui desktopMain 的 project() 声明提供存在性（android 任务链不配置本模块）
        include(":desktop:core-player")
    }
    else -> {
        include(":android:app")
        include(":android:core-player")
        include(":shared-ui")
        include(":desktop:app")
        include(":desktop:core-player")
    }
}
