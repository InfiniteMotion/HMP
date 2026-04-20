pluginManagement {
    repositories {
        gradlePluginPortal()
        google ()
        mavenCentral()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Hearable Music Player"
include(":shared")
include(":android:app")
include(":android:core-data")
include(":android:core-player")
include(":android:feature-ui")
