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
// 包含的项目结构
include(":RetrofitNetwork")
include(":OkHttp")
include(":ThreadSync")
include(":Utils")
include(":ThreadCommunication")
include(":ThreadAndroid")
include(":RxJava")
include(":IO")
include(":GradleBuild")
include(":GradlePlugin")
