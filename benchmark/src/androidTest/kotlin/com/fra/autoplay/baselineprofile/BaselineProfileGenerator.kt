package com.fra.autoplay.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates the baseline profile for :app.
 *
 * Run with:
 *   ./gradlew :benchmark:generateBaselineProfile
 *
 * The [BaselineProfileRule.collect] block receives an [androidx.benchmark.macro.MacrobenchmarkScope]
 * which launches the app by package name, so no Activity reference is needed here.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() {
        baselineProfileRule.collect(
            packageName = "com.fra.autoplay",
            maxIterations = 5
        ) {
            pressHome()
            pressHome()
            // Cold start the launcher activity and wait for the first frame.
            startActivityAndWait()
            // Interact with the main UI to exercise the startup path.
            device.waitForIdle()
        }
    }
}