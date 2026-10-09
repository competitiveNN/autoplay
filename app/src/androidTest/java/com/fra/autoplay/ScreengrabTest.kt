package com.fra.autoplay

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.DecorViewScreenshotStrategy
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class ScreengrabTest {

    @get:Rule
    val localeTestRule = LocaleTestRule()

    @Test
    fun captureMainScreen() {
        val scenario = ActivityScenario.launch<MainActivity>(MainActivity::class.java)
        scenario.onActivity { activity ->
            Screengrab.screenshot("main_screen", DecorViewScreenshotStrategy(activity))
        }
        try {
            scenario.close()
        } catch (_: AssertionError) {}
    }

    @Test
    fun captureSettingsScreen() {
        val intent = Intent(ApplicationProvider.getApplicationContext<Context>(), SettingsActivity::class.java)
        val scenario = ActivityScenario.launch<SettingsActivity>(intent)
        scenario.onActivity { activity ->
            Screengrab.screenshot("settings_screen", DecorViewScreenshotStrategy(activity))
        }
        try {
            scenario.close()
        } catch (_: AssertionError) {}
    }
}
