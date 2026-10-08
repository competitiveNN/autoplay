package com.fra.autoplay

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.fra.autoplay.compose.AutoPlaySettingsScreen

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                AutoPlaySettingsScreen(
                    onAboutClick = {
                        val intent = Intent(requireContext(), AboutActivity::class.java)
                        startActivity(intent)
                    },
                    onBackupClick = {
                        val json = PreferencesHelper.exportToJson(requireContext())
                        val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
                        clipboard?.setPrimaryClip(
                            android.content.ClipData.newPlainText("autoplay_backup", json)
                        )
                        Toast.makeText(requireContext(), R.string.settings_backup_done, Toast.LENGTH_SHORT).show()
                    },
                    onRestoreClick = {
                        val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
                        val text = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                        if (!text.isNullOrBlank() && PreferencesHelper.importFromJson(requireContext(), text)) {
                            Toast.makeText(requireContext(), R.string.settings_restore_done, Toast.LENGTH_SHORT).show()
                            requireActivity().recreate()
                        } else {
                            Toast.makeText(requireContext(), R.string.settings_restore_failed, Toast.LENGTH_LONG).show()
                        }
                    },
                    delayEnabled = PreferencesHelper.getResumeDelayMs(requireContext()) > 0,
                    delaySubtitle = formatDelay(PreferencesHelper.getResumeDelayMs(requireContext())),
                    filterEnabled = PreferencesHelper.isFilterHeadphones(requireContext()),
                    filterSubtitle = if (PreferencesHelper.isFilterHeadphones(requireContext())) "Filtered" else "All types",
                    batteryEnabled = PreferencesHelper.isBatteryReminderEnabled(requireContext()),
                    smartResumeEnabled = PreferencesHelper.isSmartResumeEnabled(requireContext()),
                    smartResumeSubtitle = buildSmartResumeSubtitle(requireContext()),
                    diagnosticsEnabled = PreferencesHelper.isDiagnosticsEnabled(requireContext()),
                    diagnosticsSubtitle = if (PreferencesHelper.isDiagnosticsEnabled(requireContext())) "Enabled" else "Disabled",
                    themeOptions = listOf("System", "Light", "Dark"),
                    selectedTheme = when (PreferencesHelper.getAppTheme(requireContext())) {
                        "light" -> "Light"
                        "dark" -> "Dark"
                        else -> "System"
                    },
                    onDelayToggle = { enabled ->
                        if (!enabled) {
                            PreferencesHelper.setResumeDelayMs(requireContext(), 0)
                            requireActivity().recreate()
                        }
                    },
                    onFilterToggle = { enabled ->
                        PreferencesHelper.setFilterHeadphones(requireContext(), enabled)
                        PreferencesHelper.notifyPreferencesChanged(requireContext())
                        requireActivity().recreate()
                    },
                    onBatteryToggle = { enabled ->
                        PreferencesHelper.setBatteryReminder(requireContext(), enabled)
                        requireActivity().recreate()
                    },
                    onSmartResumeToggle = { enabled ->
                        PreferencesHelper.setSmartResumeEnabled(requireContext(), enabled)
                        PreferencesHelper.notifyPreferencesChanged(requireContext())
                        requireActivity().recreate()
                    },
                    onDiagnosticsToggle = { enabled ->
                        PreferencesHelper.setDiagnosticsEnabled(requireContext(), enabled)
                        PreferencesHelper.notifyPreferencesChanged(requireContext())
                        requireActivity().recreate()
                    },
                    onThemeSelected = { theme ->
                        val newTheme = when (theme) {
                            "Light" -> "light"
                            "Dark" -> "dark"
                            else -> "system"
                        }
                        PreferencesHelper.setAppTheme(requireContext(), newTheme)
                        requireActivity().recreate()
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