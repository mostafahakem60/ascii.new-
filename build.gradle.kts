// Root build.gradle.kts - Multi-module Android project setup with Kotlin DSL

plugins {
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
    kotlin("android") version "1.9.20" apply false
    kotlin("kapt") version "1.9.20" apply false
    kotlin("plugin.serialization") version "1.9.20" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

subprojects {
    // Apply common configurations to all modules
    pluginManager.withPlugin("com.android.application") {
        configureAndroidApp(project)
    }

    pluginManager.withPlugin("com.android.library") {
        configureAndroidLibrary(project)
    }
}

fun configureAndroidApp(project: Project) {
    project.extensions.configure<com.android.build.gradle.AppExtension> {
        compileSdkVersion(34)
        defaultConfig {
            minSdk = 24
            targetSdk = 34
            versionCode = 1
            versionName = "1.0.0"
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        kotlinOptions {
            jvmTarget = "17"
        }
    }
}

fun configureAndroidLibrary(project: Project) {
    project.extensions.configure<com.android.build.gradle.LibraryExtension> {
        compileSdkVersion(34)
        defaultConfig {
            minSdk = 24
            targetSdk = 34
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        kotlinOptions {
            jvmTarget = "17"
        }
    }
}
