package com.fra.autoplay

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class ScreengrabTest {

    @get:Rule
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val activityRule = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @Test
    fun captureMainScreen() {
        Screengrab.screenshot("main_screen")
    }

    @Test
    fun captureSettingsScreen() {
        val intent = Intent(ApplicationProvider.getApplicationContext<Context>(), SettingsActivity::class.java)
        val scenario = ActivityScenario.launch<SettingsActivity>(intent)

        Espresso.onView(ViewMatchers.withId(R.id.settings_container))
            .check(matches(ViewMatchers.isDisplayed()))

        Screengrab.screenshot("settings_screen")
        scenario.close()
    }
}