package com.fra.autoplay

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var toggleSwitch: androidx.appcompat.widget.SwitchCompat
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toggleSwitch = findViewById(R.id.toggle_switch)
        statusText = findViewById(R.id.status_text)

        toggleSwitch.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            val intent = Intent(this, MediaPlaybackService::class.java)
            if (isChecked) {
                ContextCompat.startForegroundService(this, intent)
            } else {
                stopService(intent)
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
