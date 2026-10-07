package com.fra.autoplay

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var toggleSwitch: androidx.appcompat.widget.SwitchCompat
    private lateinit var statusText: TextView
    private lateinit var testButton: android.widget.Button
    private lateinit var batteryButton: android.widget.Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toggleSwitch = findViewById(R.id.toggle_switch)
        statusText = findViewById(R.id.status_text)
        testButton = findViewById(R.id.test_button)
        batteryButton = findViewById(R.id.battery_button)

        toggleSwitch.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            val intent = Intent(this, MediaPlaybackService::class.java)
            try {
                if (isChecked) {
                    ContextCompat.startForegroundService(this, intent)
                    Toast.makeText(this, R.string.service_started, Toast.LENGTH_SHORT).show()
                } else {
                    stopService(intent)
                    Toast.makeText(this, R.string.service_stopped, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.error, e.message), Toast.LENGTH_LONG).show()
            }
        }

        testButton.setOnClickListener {
            // Manually trigger media resume for testing.
            if (MediaPlaybackService.isRunning()) {
                val service = MediaPlaybackService()
                service.resumeMediaIfStopped()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val isRunning = MediaPlaybackService.isRunning()
        toggleSwitch.isChecked = isRunning
        statusText.text = getString(
            if (isRunning) R.string.status_on else R.string.status_off
        )
        statusText.setTextColor(
            getColor(if (isRunning) R.color.status_on else R.color.status_off)
        )
    }
}
