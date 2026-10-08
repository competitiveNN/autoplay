package com.fra.autoplay

import android.content.Context
import android.content.Intent

object PreferencesHelper {
    private const val PREFS_NAME = "autoplay_settings"
    private const val KEY_RESUME_DELAY_MS = "resume_delay_ms"
    private const val KEY_FILTER_HEADPHONES = "filter_headphones"
    private const val KEY_BATTERY_REMINDER = "battery_reminder"
    private const val DEFAULT_DELAY_MS = 0L
    private const val DEFAULT_FILTER = true
    private const val DEFAULT_BATTERY_REMINDER = true

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getResumeDelayMs(context: Context): Long =
        prefs(context).getLong(KEY_RESUME_DELAY_MS, DEFAULT_DELAY_MS)

    fun setResumeDelayMs(context: Context, ms: Long) =
        prefs(context).edit().putLong(KEY_RESUME_DELAY_MS, ms).apply()

    fun isFilterHeadphones(context: Context): Boolean =
        prefs(context).getBoolean(KEY_FILTER_HEADPHONES, DEFAULT_FILTER)

    fun setFilterHeadphones(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean(KEY_FILTER_HEADPHONES, enabled).apply()

    fun isBatteryReminderEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_BATTERY_REMINDER, DEFAULT_BATTERY_REMINDER)

    fun setBatteryReminder(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean(KEY_BATTERY_REMINDER, enabled).apply()

    fun notifyPreferencesChanged(context: Context) {
        val intent = Intent(ACTION_PREFERENCES_CHANGED).apply {
            `package` = context.packageName
        }
        context.sendBroadcast(intent)
    }

    const val ACTION_PREFERENCES_CHANGED = "com.fra.autoplay.action.PREFERENCES_CHANGED"
}
