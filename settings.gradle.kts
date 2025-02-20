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
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Android Senior"
include(":RetrofitNetwork")
include(":OkHttp")
include(":ThreadSync")
include(":Utils")
include(":ThreadCommunication")
include(":ThreadAndroid")
include(":RxJava")
include(":IO")
