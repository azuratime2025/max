

pluginManagement {
    repositories {
        gradlePluginPortal()
        // Shift Maven Central up so KSP plugin is resolved here first:
        mavenCentral()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Include JitPack if you're using libraries hosted there
        maven { url = uri("https://jitpack.io") }
    }

}


rootProject.name = "crashcourse"
include(":app")