package com.fra.autoplay.benchmark

import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI performance benchmark for :app — measures frame timing during scrolling/interaction.
 *
 * Run with: ./gradlew :benchmark:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.fra.autoplay.benchmark.FrameTimingBenchmarkTest
 *
 * Requires a device/emulator with API 29+ (FrameTimingMetric needs Perfetto tracing).
 */
@RunWith(AndroidJUnit4::class)
class FrameTimingBenchmarkTest {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun settingsScrollPerformance() = benchmarkRule.measureRepeated(
        packageName = "com.fra.autoplay",
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.DEFAULT,
        iterations = 10
    ) {
        // Navigate to Settings and scroll through the list.
        pressHome()
        startActivityAndWait()
        device.waitForIdle()

        // Open settings via the gear icon / action bar.
        // Since we don't have Espresso here, simulate a tap on the settings area.
        // In a real test, you'd use UiAutomator to find and click the settings button.
        // For now, we measure the frame timing of the main activity scroll.
        // TODO: Add UiAutomator interaction to actually scroll the settings fragment.
    }

    @Test
    fun mainActivityFrameTiming() = benchmarkRule.measureRepeated(
        packageName = "com.fra.autoplay",
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.DEFAULT,
        iterations = 10
    ) {
        pressHome()
        startActivityAndWait()
        device.waitForIdle()

        // Measure frame timing of the main activity (toggle ON/OFF, etc.)
        // TODO: Add UiAutomator interactions to exercise the main UI.
    }

    // Compose-specific benchmarks

    @Test
    fun composeSettingsScrollPerformance() = benchmarkRule.measureRepeated(
        packageName = "com.fra.autoplay",
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.DEFAULT,
        iterations = 10
    ) {
        pressHome()
        startActivityAndWait()
        device.waitForIdle()

        // Compose Settings screen rendering and scroll performance
        // TODO: Add UiAutomator interactions to scroll the Compose settings list.
    }

    @Test
    fun composeSettingsStartup() = benchmarkRule.measureRepeated(
        packageName = "com.fra.autoplay",
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.DEFAULT,
        iterations = 10
    ) {
        pressHome()
        startActivityAndWait()
        device.waitForIdle()

        // Measure Compose initial render performance
        // TODO: Add UiAutomator interactions to open the Compose settings screen.
    }
}