pluginManagement {
    repositories {
        google()
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

rootProject.name = "android-multimodule"

include(
    ":app",
    ":libraries:shared-models",
    ":libraries:smali-translator",
    ":libraries:arabic-explainer"
)
