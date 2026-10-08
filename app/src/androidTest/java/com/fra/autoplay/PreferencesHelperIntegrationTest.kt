package com.fra.autoplay

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class PreferencesHelperIntegrationTest {

    private val context: Context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun preferencesHelper_defaultValues() {
        // Clear any existing preferences
        context.getSharedPreferences("autoplay_settings", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        // Test default values
        assertEquals(0L, PreferencesHelper.getResumeDelayMs(context))
        assertTrue(PreferencesHelper.isFilterHeadphones(context))
        assertTrue(PreferencesHelper.isBatteryReminderEnabled(context))
    }

    @Test
    fun preferencesHelper_resumeDelayPersists() {
        PreferencesHelper.setResumeDelayMs(context, 1500L)
        assertEquals(1500L, PreferencesHelper.getResumeDelayMs(context))

        PreferencesHelper.setResumeDelayMs(context, 0L)
        assertEquals(0L, PreferencesHelper.getResumeDelayMs(context))
    }

    @Test
    fun preferencesHelper_filterHeadphonesPersists() {
        PreferencesHelper.setFilterHeadphones(context, false)
        assertEquals(false, PreferencesHelper.isFilterHeadphones(context))

        PreferencesHelper.setFilterHeadphones(context, true)
        assertEquals(true, PreferencesHelper.isFilterHeadphones(context))
    }

    @Test
    fun preferencesHelper_batteryReminderPersists() {
        PreferencesHelper.setBatteryReminder(context, false)
        assertEquals(false, PreferencesHelper.isBatteryReminderEnabled(context))

        PreferencesHelper.setBatteryReminder(context, true)
        assertEquals(true, PreferencesHelper.isBatteryReminderEnabled(context))
    }

    @Test
    fun backupRestore_roundTripsOnDevice() {
        context.getSharedPreferences("autoplay_settings", Context.MODE_PRIVATE).edit().clear().commit()
        PreferencesHelper.migrateIfNeeded(context)
        PreferencesHelper.setResumeDelayMs(context, 1000L)
        PreferencesHelper.setAppTheme(context, "dark")
        PreferencesHelper.setDiagnosticsEnabled(context, true)
        val json = PreferencesHelper.exportToJson(context)
        assertTrue(json.contains("resume_delay_ms"))
        context.getSharedPreferences("autoplay_settings", Context.MODE_PRIVATE).edit().clear().commit()
        assertTrue(PreferencesHelper.importFromJson(context, json))
        assertEquals(1000L, PreferencesHelper.getResumeDelayMs(context))
        assertEquals("dark", PreferencesHelper.getAppTheme(context))
        assertTrue(PreferencesHelper.isDiagnosticsEnabled(context))
    }

    @Test
    fun migration_fromV3toV4_addsDiagnosticsKey() {
        context.getSharedPreferences("autoplay_settings", Context.MODE_PRIVATE)
            .edit().putInt("schema_version", 3).apply()
        PreferencesHelper.migrateIfNeeded(context)
        assertEquals(4, context.getSharedPreferences("autoplay_settings", Context.MODE_PRIVATE).getInt("schema_version", -1))
        assertTrue(!PreferencesHelper.isDiagnosticsEnabled(context))
    }

    @Test
    fun preferencesHelper_notifyPreferencesChanged_sendsBroadcast() {
        // This test verifies the intent action is correctly defined
        assertEquals(
            "com.fra.autoplay.action.PREFERENCES_CHANGED",
            PreferencesHelper.ACTION_PREFERENCES_CHANGED
        )
    }
}