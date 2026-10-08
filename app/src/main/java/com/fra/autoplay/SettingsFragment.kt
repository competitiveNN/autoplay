package com.fra.autoplay

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : Fragment() {

    private lateinit var delaySwitch: Switch
    private lateinit var filterSwitch: Switch
    private lateinit var batterySwitch: Switch
    private lateinit var smartResumeSwitch: Switch
    private lateinit var delayValueText: TextView
    private lateinit var filterValueText: TextView
    private lateinit var smartResumeValueText: TextView
    private lateinit var excludedAppsButton: Button
    private lateinit var excludedAppsValueText: TextView
    private lateinit var aboutButton: Button
    private lateinit var themeSpinner: Spinner
    private lateinit var testResumeButton: Button
    private lateinit var diagnosticsSwitch: Switch
    private lateinit var diagnosticsValueText: TextView
    private lateinit var backupButton: Button
    private lateinit var restoreButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        delaySwitch = view.findViewById(R.id.delay_switch)
        filterSwitch = view.findViewById(R.id.filter_switch)
        batterySwitch = view.findViewById(R.id.battery_switch)
        smartResumeSwitch = view.findViewById(R.id.smart_resume_switch)
        delayValueText = view.findViewById(R.id.delay_value_text)
        filterValueText = view.findViewById(R.id.filter_value_text)
        smartResumeValueText = view.findViewById(R.id.smart_resume_value_text)
        excludedAppsButton = view.findViewById(R.id.excluded_apps_button)
        excludedAppsValueText = view.findViewById(R.id.excluded_apps_value_text)
        aboutButton = view.findViewById(R.id.about_button)
        themeSpinner = view.findViewById(R.id.theme_spinner)
        testResumeButton = view.findViewById(R.id.test_resume_button)
        diagnosticsSwitch = view.findViewById(R.id.diagnostics_switch)
        diagnosticsValueText = view.findViewById(R.id.diagnostics_value_text)
        backupButton = view.findViewById(R.id.backup_button)
        restoreButton = view.findViewById(R.id.restore_button)
        diagnosticsSwitch.isChecked = PreferencesHelper.isDiagnosticsEnabled(requireContext())
        updateDiagnosticsText()

        val delayMs = PreferencesHelper.getResumeDelayMs(requireContext())
        delayValueText.text = formatDelay(delayMs)
        delaySwitch.isChecked = delayMs > 0
        filterSwitch.isChecked = PreferencesHelper.isFilterHeadphones(requireContext())
        batterySwitch.isChecked = PreferencesHelper.isBatteryReminderEnabled(requireContext())
        smartResumeSwitch.isChecked = PreferencesHelper.isSmartResumeEnabled(requireContext())
        filterValueText.text = if (filterSwitch.isChecked) "Filtered" else "All types"
        updateSmartResumeText()
        updateExcludedAppsText()

        delaySwitch.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                showDelayDialog()
            } else {
                PreferencesHelper.setResumeDelayMs(requireContext(), 0)
                delayValueText.text = formatDelay(0)
                PreferencesHelper.notifyPreferencesChanged(requireContext())
            }
        }

        filterSwitch.setOnCheckedChangeListener { _, checked ->
            PreferencesHelper.setFilterHeadphones(requireContext(), checked)
            filterValueText.text = if (checked) "Filtered" else "All types"
            PreferencesHelper.notifyPreferencesChanged(requireContext())
        }

        batterySwitch.setOnCheckedChangeListener { _, checked ->
            PreferencesHelper.setBatteryReminder(requireContext(), checked)
        }

        smartResumeSwitch.setOnCheckedChangeListener { _, checked ->
            PreferencesHelper.setSmartResumeEnabled(requireContext(), checked)
            updateSmartResumeText()
            PreferencesHelper.notifyPreferencesChanged(requireContext())
        }

        excludedAppsButton.setOnClickListener {
            showExcludedAppsDialog()
        }

        themeSpinner.setSelection(when (PreferencesHelper.getAppTheme(requireContext())) {
            "light" -> 1
            "dark" -> 2
            else -> 0
        })

        themeSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                val theme = when (position) {
                    1 -> "light"
                    2 -> "dark"
                    else -> "system"
                }
                if (theme != PreferencesHelper.getAppTheme(requireContext())) {
                    PreferencesHelper.setAppTheme(requireContext(), theme)
                    applyTheme(theme)
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }

        testResumeButton.setOnClickListener {
            val intent = Intent(requireContext(), MediaPlaybackService::class.java).apply {
                action = "com.fra.autoplay.action.TEST_RESUME"
            }
            try {
                androidx.core.content.ContextCompat.startForegroundService(requireContext(), intent)
                Toast.makeText(requireContext(), R.string.settings_test_resume_sent, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), getString(R.string.error, e.message), Toast.LENGTH_LONG).show()
            }
        }

        diagnosticsSwitch.setOnCheckedChangeListener { _, checked ->
            PreferencesHelper.setDiagnosticsEnabled(requireContext(), checked)
            updateDiagnosticsText()
            Toast.makeText(
                requireContext(),
                if (checked) R.string.settings_diagnostics_enabled else R.string.settings_diagnostics_disabled,
                Toast.LENGTH_LONG
            ).show()
        }

        backupButton.setOnClickListener {
            val json = PreferencesHelper.exportToJson(requireContext())
            val clipboard = requireContext().getSystemService(android.content.ClipboardManager::class.java)
            clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("autoplay_backup", json))
            Toast.makeText(requireContext(), R.string.settings_backup_done, Toast.LENGTH_SHORT).show()
        }

        restoreButton.setOnClickListener {
            val clipboard = requireContext().getSystemService(android.content.ClipboardManager::class.java)
            val text = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
            if (!text.isNullOrBlank() && PreferencesHelper.importFromJson(requireContext(), text)) {
                Toast.makeText(requireContext(), R.string.settings_restore_done, Toast.LENGTH_SHORT).show()
                requireActivity().recreate()
            } else {
                Toast.makeText(requireContext(), R.string.settings_restore_failed, Toast.LENGTH_LONG).show()
            }
        }

        aboutButton.setOnClickListener {
            val intent = Intent(requireContext(), AboutActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showDelayDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_delay, null)
        val input = dialogView.findViewById<android.widget.EditText>(R.id.delay_input)
        val current = PreferencesHelper.getResumeDelayMs(requireContext())
        input.setText(current.toString())

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.settings_resume_delay_title)
            .setView(dialogView)
            .setPositiveButton(R.string.settings_resume_delay_dialog) { _, _ ->
                val text = input.text?.toString() ?: ""
                val ms = text.toLongOrNull()
                if (ms != null && ms >= 0) {
                    PreferencesHelper.setResumeDelayMs(requireContext(), ms)
                    delayValueText.text = formatDelay(ms)
                    PreferencesHelper.notifyPreferencesChanged(requireContext())
                } else {
                    Toast.makeText(requireContext(), R.string.settings_invalid_delay, Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showExcludedAppsDialog() {
        val excludedPackages = PreferencesHelper.getExcludedPackages(requireContext()).toMutableList()
        val allPackages = requireContext().packageManager
            .getInstalledApplications(0)
            .filter { it.packageName != requireContext().packageName }
            .sortedBy { it.packageName }
            .map { it.packageName }

        val checkedItems = allPackages.map { excludedPackages.contains(it) }.toBooleanArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.settings_exclude_apps_title)
            .setMultiChoiceItems(allPackages.toTypedArray(), checkedItems) { _, which, isChecked ->
                val pkg = allPackages[which]
                if (isChecked) {
                    PreferencesHelper.addExcludedPackage(requireContext(), pkg)
                } else {
                    PreferencesHelper.removeExcludedPackage(requireContext(), pkg)
                }
            }
            .setPositiveButton(android.R.string.ok) { _, _ ->
                updateExcludedAppsText()
                PreferencesHelper.notifyPreferencesChanged(requireContext())
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun updateExcludedAppsText() {
        val excluded = PreferencesHelper.getExcludedPackages(requireContext())
        excludedAppsValueText.text = if (excluded.isEmpty()) {
            "None"
        } else {
            "${excluded.size} app(s) excluded"
        }
    }

    private fun updateSmartResumeText() {
        val enabled = PreferencesHelper.isSmartResumeEnabled(requireContext())
        if (enabled) {
            val stats = PreferencesHelper.getUsageStats(requireContext())
            val total = stats["total_events"] ?: 0
            smartResumeValueText.text = if (total > 0) {
                "Learned from $total connections"
            } else {
                "Learning your patterns..."
            }
        } else {
            smartResumeValueText.text = "Disabled"
        }
    }

    private fun updateDiagnosticsText() {
        diagnosticsValueText.text = getString(
            if (PreferencesHelper.isDiagnosticsEnabled(requireContext()))
                R.string.settings_diagnostics_enabled else R.string.settings_diagnostics_disabled
        )
    }

    private fun applyTheme(theme: String) {
        val mode = when (theme) {
            "light" -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            else -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun formatDelay(ms: Long): String {
        return if (ms == 0L) "Immediate" else "${ms} ms"
    }
}
