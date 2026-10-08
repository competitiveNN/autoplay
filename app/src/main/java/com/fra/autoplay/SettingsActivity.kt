package com.fra.autoplay

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import com.fra.autoplay.compose.AutoPlaySettingsScreen

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val composeView = ComposeView(this).apply {
            setContent {
                AutoPlaySettingsScreen(
                    onAboutClick = {
                        val intent = android.content.Intent(this@SettingsActivity, AboutActivity::class.java)
                        startActivity(intent)
                    },
                    onBackupClick = {
                        val json = PreferencesHelper.exportToJson(this@SettingsActivity)
                        val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
                        clipboard?.setPrimaryClip(
                            android.content.ClipData.newPlainText("autoplay_backup", json)
                        )
                        Toast.makeText(this@SettingsActivity, R.string.settings_backup_done, Toast.LENGTH_SHORT).show()
                    },
                    onRestoreClick = {
                        val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
                        val text = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                        if (!text.isNullOrBlank() && PreferencesHelper.importFromJson(this@SettingsActivity, text)) {
                            Toast.makeText(this@SettingsActivity, R.string.settings_restore_done, Toast.LENGTH_SHORT).show()
                            recreate()
                        } else {
                            Toast.makeText(this@SettingsActivity, R.string.settings_restore_failed, Toast.LENGTH_LONG).show()
                        }
                    },
                    delayEnabled = PreferencesHelper.getResumeDelayMs(this@SettingsActivity) > 0,
                    delaySubtitle = formatDelay(PreferencesHelper.getResumeDelayMs(this@SettingsActivity)),
                    filterEnabled = PreferencesHelper.isFilterHeadphones(this@SettingsActivity),
                    filterSubtitle = if (PreferencesHelper.isFilterHeadphones(this@SettingsActivity)) "Filtered" else "All types",
                    batteryEnabled = PreferencesHelper.isBatteryReminderEnabled(this@SettingsActivity),
                    smartResumeEnabled = PreferencesHelper.isSmartResumeEnabled(this@SettingsActivity),
                    smartResumeSubtitle = buildSmartResumeSubtitle(this@SettingsActivity),
                    diagnosticsEnabled = PreferencesHelper.isDiagnosticsEnabled(this@SettingsActivity),
                    diagnosticsSubtitle = if (PreferencesHelper.isDiagnosticsEnabled(this@SettingsActivity)) "Enabled" else "Disabled",
                    themeOptions = listOf("System", "Light", "Dark"),
                    selectedTheme = when (PreferencesHelper.getAppTheme(this@SettingsActivity)) {
                        "light" -> "Light"
                        "dark" -> "Dark"
                        else -> "System"
                    },
                    onDelayToggle = { enabled ->
                        if (!enabled) {
                            PreferencesHelper.setResumeDelayMs(this@SettingsActivity, 0)
                            recreate()
                        } else {
                            // Show delay dialog - handled by original SettingsFragment
                            recreate()
                        }
                    },
                    onFilterToggle = { enabled ->
                        PreferencesHelper.setFilterHeadphones(this@SettingsActivity, enabled)
                        PreferencesHelper.notifyPreferencesChanged(this@SettingsActivity)
                        recreate()
                    },
                    onBatteryToggle = { enabled ->
                        PreferencesHelper.setBatteryReminder(this@SettingsActivity, enabled)
                        recreate()
                    },
                    onSmartResumeToggle = { enabled ->
                        PreferencesHelper.setSmartResumeEnabled(this@SettingsActivity, enabled)
                        PreferencesHelper.notifyPreferencesChanged(this@SettingsActivity)
                        recreate()
                    },
                    onDiagnosticsToggle = { enabled ->
                        PreferencesHelper.setDiagnosticsEnabled(this@SettingsActivity, enabled)
                        PreferencesHelper.notifyPreferencesChanged(this@SettingsActivity)
                        recreate()
                    },
                    onThemeSelected = { theme ->
                        val newTheme = when (theme) {
                            "Light" -> "light"
                            "Dark" -> "dark"
                            else -> "system"
                        }
                        PreferencesHelper.setAppTheme(this@SettingsActivity, newTheme)
                        recreate()
                    },
                    settingsTitle = getString(R.string.settings_title),
                    resumeDelayTitle = getString(R.string.settings_resume_delay_title),
                    filterHeadphonesTitle = getString(R.string.settings_filter_headphones_title),
                    batteryReminderTitle = getString(R.string.settings_battery_reminder_title),
                    smartResumeTitle = getString(R.string.settings_smart_resume_title),
                    themeTitle = getString(R.string.settings_theme_title),
                    diagnosticsTitle = getString(R.string.settings_diagnostics_title),
                    backupTitle = getString(R.string.settings_backup_title),
                    restoreTitle = getString(R.string.settings_restore_title),
                    aboutTitle = getString(R.string.about_title)
                )
            }
        }
        setContentView(composeView)
        supportActionBar?.let {
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayShowTitleEnabled(true)
            it.title = getString(R.string.settings_title)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun formatDelay(ms: Long): String {
        return if (ms == 0L) "Immediate" else "${ms} ms"
    }

    private fun buildSmartResumeSubtitle(context: android.content.Context): String {
        val enabled = PreferencesHelper.isSmartResumeEnabled(context)
        if (!enabled) return "Disabled"
        val stats = PreferencesHelper.getUsageStats(context)
        val total = stats["total_events"] ?: 0
        return if (total > 0) "Learned from $total connections" else "Learning your patterns..."
    }
}