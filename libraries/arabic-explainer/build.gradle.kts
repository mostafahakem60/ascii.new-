plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.example.arabic_explainer"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        targetSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    // Core Android & AppCompat
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)

    // Jetpack Architecture
    implementation(libs.bundles.lifecycle)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.activity.compose)

    // Coroutines
    implementation(libs.bundles.coroutines)

    // Hilt Dependency Injection
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

kapt {
    correctErrorTypes = true
}
