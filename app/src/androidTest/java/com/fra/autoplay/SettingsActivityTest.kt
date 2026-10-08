package com.fra.autoplay

import android.os.Bundle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.CoreMatchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule<SettingsActivity>(SettingsActivity::class.java)

    @Test
    fun settingsActivity_opensAndShowsFragment() {
        Espresso.onView(ViewMatchers.withId(R.id.settings_container))
            .check(matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun settingsFragment_showsAllPreferences() {
        // Check delay switch is displayed
        Espresso.onView(ViewMatchers.withId(R.id.delay_switch))
            .check(matches(ViewMatchers.isDisplayed()))

        // Check filter switch is displayed
        Espresso.onView(ViewMatchers.withId(R.id.filter_switch))
            .check(matches(ViewMatchers.isDisplayed()))

        // Check battery switch is displayed
        Espresso.onView(ViewMatchers.withId(R.id.battery_switch))
            .check(matches(ViewMatchers.isDisplayed()))

        // Check test resume button is displayed
        Espresso.onView(ViewMatchers.withId(R.id.test_resume_button))
            .check(matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun delaySwitch_toggleOpensDelayDialog() {
        Espresso.onView(ViewMatchers.withId(R.id.delay_switch))
            .perform(ViewActions.click())

        // Dialog should appear with title
        Espresso.onView(ViewMatchers.withText("Resume Delay"))
            .check(matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun filterSwitch_toggleChangesState() {
        val initialState = getSwitchState(R.id.filter_switch)
        
        Espresso.onView(ViewMatchers.withId(R.id.filter_switch))
            .perform(ViewActions.click())

        // Verify state changed
        val newState = getSwitchState(R.id.filter_switch)
        assert(newState != initialState)
    }

    @Test
    fun batterySwitch_toggleChangesState() {
        val initialState = getSwitchState(R.id.battery_switch)
        
        Espresso.onView(ViewMatchers.withId(R.id.battery_switch))
            .perform(ViewActions.click())

        val newState = getSwitchState(R.id.battery_switch)
        assert(newState != initialState)
    }

    @Test
    fun testResumeButton_isClickable() {
        Espresso.onView(ViewMatchers.withId(R.id.test_resume_button))
            .perform(ViewActions.click())
            .check(matches(ViewMatchers.isDisplayed()))
    }

    private fun getSwitchState(switchId: Int): Boolean {
        var state = false
        activityRule.scenario.onActivity { activity ->
            val switch = activity.findViewById<android.widget.Switch>(switchId)
            state = switch.isChecked
        }
        return state
    }
}