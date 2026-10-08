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
    private const val KEY_APP_THEME = "app_theme"
    private const val KEY_DIAGNOSTICS_ENABLED = "diagnostics_enabled"
    private const val DEFAULT_DELAY_MS = 0L
    private const val DEFAULT_FILTER = true
    private const val DEFAULT_BATTERY_REMINDER = true
    private const val DEFAULT_EXCLUDED_PACKAGES = ""
    private const val DEFAULT_SMART_RESUME = false
    private const val DEFAULT_CONNECTION_HISTORY = ""
    private const val DEFAULT_APP_THEME = "system"
    private const val DEFAULT_DIAGNOSTICS_ENABLED = false
    private const val KEY_SCHEMA_VERSION = "schema_version"
    private const val CURRENT_SCHEMA_VERSION = 4
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

    fun getAppTheme(context: Context): String =
        prefs(context).getString(KEY_APP_THEME, DEFAULT_APP_THEME) ?: DEFAULT_APP_THEME

    fun isDiagnosticsEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_DIAGNOSTICS_ENABLED, DEFAULT_DIAGNOSTICS_ENABLED)

    fun setDiagnosticsEnabled(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean(KEY_DIAGNOSTICS_ENABLED, enabled).apply()

    fun setAppTheme(context: Context, theme: String) =
        prefs(context).edit().putString(KEY_APP_THEME, theme).apply()

    fun migrateIfNeeded(context: Context) {
        val p = prefs(context)
        val version = p.getInt(KEY_SCHEMA_VERSION, 1)
        if (version >= CURRENT_SCHEMA_VERSION) return
        val editor = p.edit()
        if (version < 2) {
            // v1->v2: ensure smart_resume key exists
            if (!p.contains(KEY_SMART_RESUME_ENABLED)) {
                editor.putBoolean(KEY_SMART_RESUME_ENABLED, DEFAULT_SMART_RESUME)
            }
        }
        if (version < 3) {
            // v2->v3: ensure app_theme key exists, clamp resume delay
            if (!p.contains(KEY_APP_THEME)) {
                editor.putString(KEY_APP_THEME, DEFAULT_APP_THEME)
            }
            val delay = p.getLong(KEY_RESUME_DELAY_MS, DEFAULT_DELAY_MS)
            if (delay < 0 || delay > 10000) {
                editor.putLong(KEY_RESUME_DELAY_MS, DEFAULT_DELAY_MS)
            }
        }
        if (version < 4) {
            if (!p.contains(KEY_DIAGNOSTICS_ENABLED)) {
                editor.putBoolean(KEY_DIAGNOSTICS_ENABLED, DEFAULT_DIAGNOSTICS_ENABLED)
            }
        }
        editor.putInt(KEY_SCHEMA_VERSION, CURRENT_SCHEMA_VERSION).apply()
    }

    fun exportToJson(context: Context): String {
        val p = prefs(context)
        val map = p.all
        val sb = StringBuilder("{")
        var first = true
        for ((k, v) in map) {
            if (!first) sb.append(",")
            first = false
            sb.append("\"").append(k).append("\":")
            when (v) {
                is String -> sb.append("\"").append(v.replace("\\", "\\\\").replace("\"", "\\\"")).append("\"")
                is Boolean, is Long, is Int, is Float -> sb.append(v.toString())
                else -> sb.append("\"").append(v.toString().replace("\\", "\\\\").replace("\"", "\\\"")).append("\"")
            }
        }
        sb.append("}")
        return sb.toString()
    }

    fun importFromJson(context: Context, json: String): Boolean {
        return try {
            val trimmed = json.trim().removePrefix("{").removeSuffix("}")
            if (trimmed.isBlank()) return false
            val regex = Regex("\"([^\"]+)\":\\s*([^,{}]+)")
            val matches = regex.findAll(trimmed).toList()
            if (matches.isEmpty()) return false
            val editor = prefs(context).edit()
            for (match in matches) {
                val key = match.groupValues[1]
                val raw = match.groupValues[2].trim()
                when {
                    raw == "true" || raw == "false" -> editor.putBoolean(key, raw == "true")
                    raw.startsWith("\"") -> editor.putString(key, raw.removeSurrounding("\"").replace("\\\"", "\"").replace("\\\\", "\\"))
                    else -> raw.toLongOrNull()?.let { editor.putLong(key, it) } ?: editor.putString(key, raw.removeSurrounding("\""))
                }
            }
            editor.apply()
            notifyPreferencesChanged(context)
            true
        } catch (_: Exception) { false }
    }
}
