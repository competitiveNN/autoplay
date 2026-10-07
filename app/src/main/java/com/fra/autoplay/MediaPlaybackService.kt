package com.fra.autoplay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService

class MediaPlaybackService : Service() {

    companion object {
        private const val CHANNEL_ID = "autoplay_channel"
        private const val NOTIFICATION_ID = 1
        private var running = false

        fun isRunning(): Boolean = running
    }

    private lateinit var audioManager: AudioManager
    private lateinit var mediaSessionManager: MediaSessionManager
    private var headphoneReceiver: HeadphoneConnectionReceiver? = null

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
            super.onDevicesAdded(addedDevices)
            for (device in addedDevices) {
                if (device.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                    device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET
                ) {
                    resumeMediaIfStopped()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        running = true

        audioManager.registerAudioDeviceCallback(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioDeviceCallback, null
            } else {
                @Suppress("DEPRECATION")
                object : AudioManager.OnAudioFocusChangeListener {
                    override fun onAudioFocusChange(focusChange: Int) {}
                } as AudioManager.OnAudioFocusChangeListener,
                null
            )
        )

        // Register sticky broadcast receiver for headset events as fallback
        headphoneReceiver = HeadphoneConnectionReceiver()
        val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        registerReceiver(headphoneReceiver, filter)

        // Check current audio devices for already-connected headphones
        checkCurrentDevices()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        running = false
        audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
        headphoneReceiver?.let { unregisterReceiver(it) }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun checkCurrentDevices() {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        for (device in devices) {
            if (device.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET
            ) {
                resumeMediaIfStopped()
                break
            }
        }
    }

    private fun resumeMediaIfStopped() {
        try {
            val sessions = mediaSessionManager.getActiveSessions(null)
            for (controller in sessions) {
                if (controller.playbackState != null &&
                    controller.playbackState.state == android.media.session.PlaybackState.STATE_STOPPED
                ) {
                    controller.transportControls.play()
                }
            }
        } catch (_: SecurityException) {
            // Permission not granted; try fallback via media button
            sendMediaButtonDownUp()
        }
    }

    @Suppress("DEPRECATION")
    private fun sendMediaButtonDownUp() {
        val startTime = SystemClock.uptimeMillis()
        val downIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
            putExtra(
                Intent.EXTRA_KEY_EVENT,
                android.view.KeyEvent(startTime, startTime,
                    android.view.KeyEvent.ACTION_DOWN,
                    android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0)
            )
        }
        val upIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
            putExtra(
                Intent.EXTRA_KEY_EVENT,
                android.view.KeyEvent(startTime, startTime,
                    android.view.KeyEvent.ACTION_UP,
                    android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0)
            )
        }
        sendOrderedBroadcast(downIntent, null)
        sendOrderedBroadcast(upIntent, null)
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AutoPlay Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
