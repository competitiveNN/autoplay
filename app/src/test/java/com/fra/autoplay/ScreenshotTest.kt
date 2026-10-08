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

    // Compose-based screenshots
    @Test
    fun composeSettingsScreen_screenshot() {
        paparazzi.snapshot {
            com.fra.autoplay.compose.AutoPlaySettingsScreen(
                onAboutClick = {},
                onBackupClick = {},
                onRestoreClick = {},
                delayEnabled = false,
                delaySubtitle = "Immediate",
                filterEnabled = true,
                filterSubtitle = "Filtered",
                batteryEnabled = true,
                smartResumeEnabled = true,
                smartResumeSubtitle = "Learned from 42 connections",
                diagnosticsEnabled = false,
                diagnosticsSubtitle = "Disabled",
                themeOptions = listOf("System", "Light", "Dark"),
                selectedTheme = "System",
                onDelayToggle = {},
                onFilterToggle = {},
                onBatteryToggle = {},
                onSmartResumeToggle = {},
                onDiagnosticsToggle = {},
                onThemeSelected = {},
                settingsTitle = "Settings",
                resumeDelayTitle = "Resume Delay",
                filterHeadphonesTitle = "Filter Headphones",
                batteryReminderTitle = "Battery Reminder",
                smartResumeTitle = "Smart Resume",
                themeTitle = "Theme",
                diagnosticsTitle = "Diagnostics",
                backupTitle = "Backup",
                restoreTitle = "Restore",
                aboutTitle = "About"
            )
        }
    }

    @Test
    fun composeSwitchSettingItem_screenshot() {
        paparazzi.snapshot {
            com.fra.autoplay.compose.SwitchSettingItem(
                title = "Test Switch",
                subtitle = "Subtitle text",
                checked = true,
                onCheckedChange = {}
            )
        }
    }

    @Test
    fun composeButtonSettingItem_screenshot() {
        paparazzi.snapshot {
            com.fra.autoplay.compose.ButtonSettingItem(
                title = "Test Button",
                subtitle = "Tap to open",
                onClick = {}
            )
        }
    }

    @Test
    fun composeDropdownSettingItem_screenshot() {
        paparazzi.snapshot {
            com.fra.autoplay.compose.DropdownSettingItem(
                title = "Theme",
                options = listOf("System", "Light", "Dark"),
                selectedOption = "System",
                onOptionSelected = {}
            )
        }
    }
}