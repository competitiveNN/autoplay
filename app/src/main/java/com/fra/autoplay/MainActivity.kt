package com.fra.autoplay

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.content.Context
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
    private lateinit var checkMediaButton: android.widget.Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toggleSwitch = findViewById(R.id.toggle_switch)
        statusText = findViewById(R.id.status_text)
        testButton = findViewById(R.id.test_button)
        batteryButton = findViewById(R.id.battery_button)
        checkMediaButton = findViewById(R.id.check_media_button)

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

    private fun checkMediaState() {
        if (!MediaPlaybackService.isRunning()) {
            Toast.makeText(this, R.string.service_not_running, Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val msm = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            val sessions = msm.getActiveSessions(null)
            if (sessions.isEmpty()) {
                Toast.makeText(this, R.string.no_media_sessions, Toast.LENGTH_LONG).show()
                return
            }
            val builder = StringBuilder()
            for (controller in sessions) {
                val pkg = controller.packageName
                val state = controller.playbackState?.state ?: -1
                val stateStr = when (state) {
                    MediaController.STATE_PLAYING -> "Playing"
                    MediaController.STATE_PAUSED -> "Paused"
                    MediaController.STATE_STOPPED -> "Stopped"
                    MediaController.STATE_BUFFERING -> "Buffering"
                    MediaController.STATE_CONNECTING -> "Connecting"
                    MediaController.STATE_FAST_FORWARDING -> "Fast Forward"
                    MediaController.STATE_REWINDING -> "Rewinding"
                    MediaController.STATE_SKIPPING_TO_NEXT -> "Skip Next"
                    MediaController.STATE_SKIPPING_TO_PREVIOUS -> "Skip Prev"
                    else -> "Unknown ()"
                }
                val hasControls = (controller.flags and MediaController.FLAG_HANDLES_TRANSPORT_CONTROLS) != 0
                builder.append(": ")
                if (hasControls) builder.append(" [controls]")
                builder.append("
")
            }
            Toast.makeText(this, builder.toString(), Toast.LENGTH_LONG).show()
        } catch (_: SecurityException) {
            Toast.makeText(this, R.string.no_media_permission, Toast.LENGTH_LONG).show()
        }
    }
}
}
