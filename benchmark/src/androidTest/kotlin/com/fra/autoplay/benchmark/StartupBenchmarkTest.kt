package com.fra.autoplay.benchmark

import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Startup benchmark for :app.
 *
 * Measures cold / warm / hot startup times with [MacrobenchmarkRule.measureRepeated].
 * Run with: ./gradlew :benchmark:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.fra.autoplay.benchmark.StartupBenchmarkTest
 *
 * Macrobenchmark runs on a device/emulator (not in CI without a device farm).
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmarkTest {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startupCold() = benchmarkRule.measureRepeated(
        packageName = "com.fra.autoplay",
        metrics = listOf(StartupTimingMetric()),
        compilationMode = CompilationMode.DEFAULT,
        startupMode = StartupMode.COLD,
        iterations = 5
    ) {
        pressHome()
        pressHome()
        // Cold start the launcher activity and wait for the first frame.
        startActivityAndWait()
        device.waitForIdle()
    }

    @Test
    fun startupWarm() = benchmarkRule.measureRepeated(
        packageName = "com.fra.autoplay",
        metrics = listOf(StartupTimingMetric()),
        compilationMode = CompilationMode.DEFAULT,
        startupMode = StartupMode.WARM,
        iterations = 5
    ) {
        pressHome()
        pressHome()
        startActivityAndWait()
        device.waitForIdle()
    }

    @Test
    fun startupHot() = benchmarkRule.measureRepeated(
        packageName = "com.fra.autoplay",
        metrics = listOf(StartupTimingMetric()),
        compilationMode = CompilationMode.DEFAULT,
        startupMode = StartupMode.HOT,
        iterations = 5
    ) {
        pressHome()
        pressHome()
        startActivityAndWait()
        device.waitForIdle()
    }
}