package com.fra.autoplay

import android.content.Context
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PreferencesHelperTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("autoplay_settings", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun migrate_setsSchemaVersionAndBackfillsKeys() {
        PreferencesHelper.migrateIfNeeded(context)
        val prefs = context.getSharedPreferences("autoplay_settings", Context.MODE_PRIVATE)
        assertEquals(4, prefs.getInt("schema_version", -1))
        assertTrue(prefs.contains("app_theme"))
        assertTrue(prefs.contains("smart_resume_enabled"))
    }

    @Test
    fun migrate_isIdempotent() {
        PreferencesHelper.migrateIfNeeded(context)
        PreferencesHelper.setAppTheme(context, "dark")
        PreferencesHelper.migrateIfNeeded(context)
        assertEquals("dark", PreferencesHelper.getAppTheme(context))
    }

    @Test
    fun migrate_clampsInvalidDelay() {
        context.getSharedPreferences("autoplay_settings", Context.MODE_PRIVATE)
            .edit().putLong("resume_delay_ms", 99999L).putInt("schema_version", 2).apply()
        PreferencesHelper.migrateIfNeeded(context)
        // out-of-range delay should be reset to default (0)
        assertEquals(0L, PreferencesHelper.getResumeDelayMs(context))
    }

    @Test
    fun exportImport_roundTrips() {
        PreferencesHelper.setResumeDelayMs(context, 500L)
        PreferencesHelper.setAppTheme(context, "light")
        PreferencesHelper.setFilterHeadphones(context, false)
        val json = PreferencesHelper.exportToJson(context)
        assertTrue(json.contains("resume_delay_ms"))
        context.getSharedPreferences("autoplay_settings", Context.MODE_PRIVATE).edit().clear().commit()
        assertTrue(PreferencesHelper.importFromJson(context, json))
        assertEquals(500L, PreferencesHelper.getResumeDelayMs(context))
        assertEquals("light", PreferencesHelper.getAppTheme(context))
        assertFalse(PreferencesHelper.isFilterHeadphones(context))
    }

    @Test
    fun import_invalidJsonReturnsFalse() {
        assertFalse(PreferencesHelper.importFromJson(context, "not json at all"))
        // Empty still returns false (no content)
        assertFalse(PreferencesHelper.importFromJson(context, "{}"))
    }

    @Test
    fun diagnostics_roundTrip() {
        assertFalse(PreferencesHelper.isDiagnosticsEnabled(context))
        PreferencesHelper.setDiagnosticsEnabled(context, true)
        assertTrue(PreferencesHelper.isDiagnosticsEnabled(context))
        PreferencesHelper.setDiagnosticsEnabled(context, false)
        assertFalse(PreferencesHelper.isDiagnosticsEnabled(context))
    }

    @Test
    fun backupRestore_includesDiagnostics() {
        PreferencesHelper.setDiagnosticsEnabled(context, true)
        val json = PreferencesHelper.exportToJson(context)
        context.getSharedPreferences("autoplay_settings", Context.MODE_PRIVATE).edit().clear().commit()
        PreferencesHelper.migrateIfNeeded(context)
        assertTrue(PreferencesHelper.importFromJson(context, json))
        assertTrue(PreferencesHelper.isDiagnosticsEnabled(context))
    }

    @Test
    fun appTheme_roundTrip() {
        PreferencesHelper.setAppTheme(context, "dark")
        assertEquals("dark", PreferencesHelper.getAppTheme(context))
        PreferencesHelper.setAppTheme(context, "system")
        assertEquals("system", PreferencesHelper.getAppTheme(context))
    }
}
