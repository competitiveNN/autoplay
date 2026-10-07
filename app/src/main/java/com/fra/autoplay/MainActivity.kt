package com.fra.autoplay

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var toggleSwitch: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toggleSwitch = findViewById(R.id.toggle_switch)

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
        toggleSwitch.isChecked = MediaPlaybackService.isRunning()
    }
}
