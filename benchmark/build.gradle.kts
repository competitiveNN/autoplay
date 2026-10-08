plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("androidx.baselineprofile")
}

android {
    namespace = "com.fra.autoplay.benchmark"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        // Macrobenchmark tests run in the :app process; target the app package.
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // Macrobenchmark measures the release build of :app, so fall back
            // to the release build type when the debug variant is unavailable.
            matchingFallbacks.add("release")
        }
        release {
            isMinifyEnabled = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    testOptions {
        unitTests.isIncludeAndroidResources = false
    }
}

dependencies {
    implementation(libs.androidx.benchmark.macro)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation("androidx.test:runner:1.5.0")
    androidTestImplementation(project(":app"))
}