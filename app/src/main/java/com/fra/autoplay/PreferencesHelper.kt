package com.fra.autoplay

import android.content.Context
import android.content.Intent
import androidx.annotation.Keep

object PreferencesHelper {
    private const val PREFS_NAME = "autoplay_settings"
    private const val KEY_RESUME_DELAY_MS = "resume_delay_ms"
    private const val KEY_FILTER_HEADPHONES = "filter_headphones"
    private const val KEY_BATTERY_REMINDER = "battery_reminder"
    private const val KEY_EXCLUDED_PACKAGES = "excluded_packages"
    private const val KEY_SMART_RESUME_ENABLED = "smart_resume_enabled"
    private const val KEY_CONNECTION_HISTORY = "connection_history"
    private const val DEFAULT_DELAY_MS = 0L
    private const val DEFAULT_FILTER = true
    private const val DEFAULT_BATTERY_REMINDER = true
    private const val DEFAULT_EXCLUDED_PACKAGES = ""
    private const val DEFAULT_SMART_RESUME = false
    private const val DEFAULT_CONNECTION_HISTORY = ""
    private const val MAX_HISTORY_ENTRIES = 100

    @Keep
    data class ConnectionEvent(
        val timestamp: Long,
        val hourOfDay: Int,
        val dayOfWeek: Int,
        val headphoneType: Int
    ) {
        fun toCsv(): String = "$timestamp,$hourOfDay,$dayOfWeek,$headphoneType"
        
        companion object {
            fun fromCsv(s: String): ConnectionEvent? {
                val parts = s.split(",")
                return if (parts.size == 4) {
                    ConnectionEvent(
                        parts[0].toLongOrNull() ?: 0L,
                        parts[1].toIntOrNull() ?: 0,
                        parts[2].toIntOrNull() ?: 0,
                        parts[3].toIntOrNull() ?: 0
                    )
                } else null
            }
        }
    }

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

    fun getExcludedPackages(context: Context): Set<String> {
        val csv = prefs(context).getString(KEY_EXCLUDED_PACKAGES, DEFAULT_EXCLUDED_PACKAGES) ?: ""
        return if (csv.isBlank()) emptySet() else csv.split(",").toSet()
    }

    fun setExcludedPackages(context: Context, packages: Set<String>) {
        val csv = packages.joinToString(",")
        prefs(context).edit().putString(KEY_EXCLUDED_PACKAGES, csv).apply()
    }

    fun addExcludedPackage(context: Context, packageName: String) {
        val current = getExcludedPackages(context).toMutableSet()
        current.add(packageName)
        setExcludedPackages(context, current)
    }

    fun removeExcludedPackage(context: Context, packageName: String) {
        val current = getExcludedPackages(context).toMutableSet()
        current.remove(packageName)
        setExcludedPackages(context, current)
    }

    fun isPackageExcluded(context: Context, packageName: String): Boolean =
        getExcludedPackages(context).contains(packageName)

    // Smart Resume Learning
    fun isSmartResumeEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SMART_RESUME_ENABLED, DEFAULT_SMART_RESUME)

    fun setSmartResumeEnabled(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean(KEY_SMART_RESUME_ENABLED, enabled).apply()

    fun recordConnectionEvent(context: Context, headphoneType: Int) {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = now
        val event = ConnectionEvent(
            timestamp = now,
            hourOfDay = calendar.get(java.util.Calendar.HOUR_OF_DAY),
            dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK),
            headphoneType = headphoneType
        )
        
        val history = getConnectionHistory(context).toMutableList()
        history.add(0, event)
        // Keep only last MAX_HISTORY_ENTRIES
        val trimmed = history.take(MAX_HISTORY_ENTRIES)
        
        val csv = trimmed.joinToString(";") { it.toCsv() }
        prefs(context).edit().putString(KEY_CONNECTION_HISTORY, csv).apply()
    }

    fun getConnectionHistory(context: Context): List<ConnectionEvent> {
        val csv = prefs(context).getString(KEY_CONNECTION_HISTORY, DEFAULT_CONNECTION_HISTORY) ?: ""
        return if (csv.isBlank()) {
            emptyList()
        } else {
            csv.split(";")
                .mapNotNull { ConnectionEvent.fromCsv(it) }
        }
    }

    fun clearConnectionHistory(context: Context) {
        prefs(context).edit().remove(KEY_CONNECTION_HISTORY).apply()
    }

    /** Returns a suggested delay based on historical patterns.
     *  If user typically connects headphones at certain times, suggest shorter delay. */
    fun getSmartDelaySuggestion(context: Context): Long {
        if (!isSmartResumeEnabled(context)) return getResumeDelayMs(context)
        
        val history = getConnectionHistory(context)
        if (history.size < 5) return getResumeDelayMs(context) // Not enough data
        
        val calendar = java.util.Calendar.getInstance()
        val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val currentDay = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        
        // Find events within ±2 hours of current time on same day of week
        val recentSimilar = history.filter { event ->
            event.dayOfWeek == currentDay &&
            kotlin.math.abs(event.hourOfDay - currentHour) <= 2 &&
            System.currentTimeMillis() - event.timestamp < 30L * 24 * 60 * 60 * 1000 // Last 30 days
        }
        
        // If we have 3+ similar events, user has a pattern - suggest immediate
        return if (recentSimilar.size >= 3) 0L else getResumeDelayMs(context)
    }

    fun getUsageStats(context: Context): Map<String, Int> {
        val history = getConnectionHistory(context)
        val byHour = history.groupBy { it.hourOfDay }.mapValues { it.value.size }
        val byDay = history.groupBy { it.dayOfWeek }.mapValues { it.value.size }
        val byType = history.groupBy { it.headphoneType }.mapValues { it.value.size }
        
        return mapOf(
            "total_events" to history.size,
            "peak_hour" to (byHour.maxByOrNull { it.value }?.key ?: -1),
            "peak_day" to (byDay.maxByOrNull { it.value }?.key ?: -1),
            "preferred_type" to (byType.maxByOrNull { it.value }?.key ?: -1)
        )
    }

    fun notifyPreferencesChanged(context: Context) {
        val intent = Intent(ACTION_PREFERENCES_CHANGED).apply {
            `package` = context.packageName
        }
        context.sendBroadcast(intent)
    }

    const val ACTION_PREFERENCES_CHANGED = "com.fra.autoplay.action.PREFERENCES_CHANGED"
}
