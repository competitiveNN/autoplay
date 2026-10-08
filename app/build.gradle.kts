import org.gradle.api.attributes.java.TargetJvmEnvironment
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.paparazzi)
    alias(libs.plugins.baselineprofile)
    id("org.gradle.jacoco")
}

android {
    namespace = "com.fra.autoplay"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.fra.autoplay"
        minSdk = 26
        targetSdk = 34
        versionCode = 10
        versionName = "1.9.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        val releaseKeystore = file("release.keystore")
        create("release").apply {
            if (releaseKeystore.exists()) {
                storeFile = releaseKeystore
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "autoplay"
                keyAlias = System.getenv("KEY_ALIAS") ?: "autoplay"
                keyPassword = System.getenv("KEY_PASSWORD") ?: "autoplay"
            } else {
                // Fall back to the debug keystore so local builds still sign.
                storeFile = signingConfigs.getByName("debug").storeFile
                storePassword = signingConfigs.getByName("debug").storePassword
                keyAlias = signingConfigs.getByName("debug").keyAlias
                keyPassword = signingConfigs.getByName("debug").keyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isTestCoverageEnabled = true
            // AGP 8.5 deprecates isTestCoverageEnabled; keep for JaCoCo .exec generation.
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
        compose = false
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    lint {
        baseline = file("lint-baseline.xml")
    }
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
    reportsDirectory.set(layout.buildDirectory.dir("reports/jacoco"))
}

val jacocoTestReport = tasks.register("jacocoTestReport", JacocoReport::class.java) {
    dependsOn("testDebugUnitTest")

    val sourceDirs = files("src/main/java", "src/main/kotlin")
    sourceDirectories.setFrom(sourceDirs)

    val classDirs = fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
        exclude("**/R.class", "**/R\$*.class", "**/BuildConfig.class", "**/Manifest*.class", "**/databinding/*", "**/androidx/**")
    }
    classDirectories.setFrom(classDirs)

    executionData.setFrom(layout.buildDirectory.file("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"))

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(true)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.work)
    // Firebase Crashlytics (optional, controlled by diagnostics preference)
    implementation(libs.firebase.bom)
    implementation(libs.crashlytics)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.paparazzi)
    testImplementation(libs.mockito.android)

    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation("androidx.test:runner:1.6.1")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.contrib)
    androidTestImplementation(libs.espresso.intents)

    // Paparazzi's layoutlib/sdk-common are compiled against Guava's -jre variant, but an Android
    // project resolves Guava's -android variant where Sets.toImmutableEnumSet is not public.
    // See https://github.com/cashapp/paparazzi/issues/1231 and google/guava#6904.
    constraints {
        "testImplementation"("com.google.guava:guava") {
            attributes {
                attribute(
                    TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE,
                    objects.named(TargetJvmEnvironment::class.java, TargetJvmEnvironment.STANDARD_JVM)
                )
            }
            because("LayoutLib and sdk-common depend on Guava's -jre published variant (paparazzi#906)")
        }
    }
}

