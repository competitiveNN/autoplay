package com.fra.autoplay

import android.widget.FrameLayout
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test

class ScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "Theme.AutoPlay",
        maxPercentDifference = 0.5
    )

    @Test
    fun activityMain_screenshot() {
        paparazzi.snapshot(
            paparazzi.inflate(R.layout.activity_main)
        )
    }

    @Test
    fun fragmentSettings_screenshot() {
        paparazzi.snapshot(
            paparazzi.inflate(R.layout.fragment_settings)
        )
    }

    @Test
    fun activityAbout_screenshot() {
        paparazzi.snapshot(
            paparazzi.inflate(R.layout.activity_about)
        )
    }

    @Test
    fun widgetAutoplay_screenshot() {
        paparazzi.snapshot(
            paparazzi.inflate(R.layout.widget_autoplay)
        )
    }
}
