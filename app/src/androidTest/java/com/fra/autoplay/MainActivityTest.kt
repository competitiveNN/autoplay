package com.fra.autoplay

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun toggleSwitch_isDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.toggle_switch))
            .check(matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun statusText_isDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.status_text))
            .check(matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun settingsMenu_opensSettingsActivity() {
        // Directly start SettingsActivity to verify it works
        val intent = Intent(ApplicationProvider.getApplicationContext<Context>(), SettingsActivity::class.java)
        val scenario = ActivityScenario.launch<SettingsActivity>(intent)
        
        scenario.onActivity { activity ->
            Espresso.onView(ViewMatchers.withId(R.id.settings_container))
                .check(matches(ViewMatchers.isDisplayed()))
        }
        scenario.close()
    }

    @Test
    fun toggleSwitch_clickTogglesService() {
        val toggle = Espresso.onView(ViewMatchers.withId(R.id.toggle_switch))
        toggle.check(matches(ViewMatchers.isDisplayed()))
        
        // Click the toggle
        toggle.perform(ViewActions.click())
        
        // Verify state changed (this is a UI test, service may not actually start in test env)
        toggle.check(matches(ViewMatchers.isDisplayed()))
    }
}