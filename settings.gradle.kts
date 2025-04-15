pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven(url = "https://repository.liferay.com/nexus/content/repositories/public/")

        // Include this to resolve local AAR files in /libs
        flatDir {
            dirs("app/libs", "eidsdk/libs")
        }
    }
}

rootProject.name = "Viettel"

// Include both modules
include(":app")
include(":eidsdk")
