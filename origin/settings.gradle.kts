pluginManagement {
    repositories {
        gradlePluginPortal()
        google ()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Hearable Music Player"
include(":app")
include(":core-data")
include(":core-domain")
include(":core-player")
include(":feature-ui")
