package com.fra.autoplay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : Fragment() {

    private lateinit var delaySwitch: Switch
    private lateinit var filterSwitch: Switch
    private lateinit var batterySwitch: Switch
    private lateinit var delayValueText: TextView
    private lateinit var filterValueText: TextView

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
        delayValueText = view.findViewById(R.id.delay_value_text)
        filterValueText = view.findViewById(R.id.filter_value_text)

        val delayMs = PreferencesHelper.getResumeDelayMs(requireContext())
        delayValueText.text = formatDelay(delayMs)
        delaySwitch.isChecked = delayMs > 0
        filterSwitch.isChecked = PreferencesHelper.isFilterHeadphones(requireContext())
        batterySwitch.isChecked = PreferencesHelper.isBatteryReminderEnabled(requireContext())
        filterValueText.text = if (filterSwitch.isChecked) "Filtered" else "All types"

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

    private fun formatDelay(ms: Long): String {
        return if (ms == 0L) "Immediate" else "${ms} ms"
    }
}
