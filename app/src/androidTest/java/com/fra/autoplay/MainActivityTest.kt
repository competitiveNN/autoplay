package com.fra.autoplay

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @Test
    fun toggleSwitch_isDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.toggle_switch))
            .check(matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun testButton_isDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.test_button))
            .check(matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun statusText_isDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.status_text))
            .check(matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun iconImage_isDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.icon_image))
            .check(matches(ViewMatchers.isDisplayed()))
    }
}
