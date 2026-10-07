package com.fra.autoplay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService

class MediaPlaybackService : Service() {

    companion object {
        private const val CHANNEL_ID = "autoplay_channel"
        private const val ACTION_STOP = "com.fra.autoplay.action.STOP"
        private const val NOTIFICATION_ID = 1
        private var running = false

        fun isRunning(): Boolean = running

        /** Returns true if the given audio device represents any kind of headphone. */
        fun isHeadphone(device: AudioDeviceInfo): Boolean = when (device.type) {
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_BLE_HEADSET,
            AudioDeviceInfo.TYPE_USB_HEADSET,
            AudioDeviceInfo.TYPE_HEARING_AID -> true
            else -> false
        }
    }

    private lateinit var audioManager: AudioManager
    private lateinit var mediaSessionManager: MediaSessionManager
    private var headphoneReceiver: HeadphoneConnectionReceiver? = null
    private var batteryOptimizationEnabled = false
    private var audioCallbackRegistered = false

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
            super.onAudioDevicesAdded(addedDevices)
            if (addedDevices.any { isHeadphone(it) }) {
                resumeMediaIfStopped()
            }
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
            super.onAudioDevicesRemoved(removedDevices)
            // Headphones unplugged: nothing to do for the "resume" feature, but we
            // keep the service alive so it can react to the next plug event.
        }
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            batteryOptimizationEnabled = !pm.isIgnoringBatteryOptimizations(packageName)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle stop action
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Handle test resume action
        if (intent?.action == "com.fra.autoplay.action.TEST_RESUME") {
            resumeMediaIfStopped()
            return START_STICKY
        }

        startForeground(NOTIFICATION_ID, buildNotification())
        running = true

        // Guard against double-registration on sticky restarts.
        if (!audioCallbackRegistered) {
            audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
            audioCallbackRegistered = true
        }

        // Register sticky broadcast receiver for headset events as fallback.
        // New instance each time to avoid "receiver already registered" errors.
        if (headphoneReceiver == null) {
            headphoneReceiver = HeadphoneConnectionReceiver()
            registerReceiver(headphoneReceiver, IntentFilter(Intent.ACTION_HEADSET_PLUG))
        }

        // Check current audio devices for already-connected headphones.
        checkCurrentDevices()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        running = false
        if (audioCallbackRegistered) {
            audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
            audioCallbackRegistered = false
        }
        headphoneReceiver?.let {
            try { unregisterReceiver(it) } catch (_: Exception) { /* ignore */ }
            headphoneReceiver = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun checkCurrentDevices() {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        if (devices.any { isHeadphone(it) }) {
            resumeMediaIfStopped()
        }
    }

    internal fun resumeMediaIfStopped() {
        try {
            val sessions = mediaSessionManager.getActiveSessions(null)
            // Prioritize sessions that support transport controls and are closer to playing.
            val candidates = sessions
                .filter { (it.flags and MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS.toLong()) != 0L }
                .sortedByDescending { it.playbackState?.lastPositionUpdateTime ?: 0L }

            for (controller in candidates) {
                val state = controller.playbackState?.state ?: continue
                // "not playing" -> resume. Handles both STOPPED and PAUSED states.
                if (state == android.media.session.PlaybackState.STATE_STOPPED ||
                    state == android.media.session.PlaybackState.STATE_PAUSED
                ) {
                    controller.transportControls.play()
                    // Only resume the most relevant session to avoid conflicts.
                    break
                }
            }
        } catch (_: SecurityException) {
            // Permission not granted; try fallback via media button injection.
            sendMediaButtonClick()
        }
    }

    @Suppress("DEPRECATION")
    private fun sendMediaButtonClick() {
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
        val stopPendingIntent = PendingIntent.getService(
            this, 0,
            Intent(this, MediaPlaybackService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(
                if (batteryOptimizationEnabled)
                    getString(R.string.notification_battery_warning)
                else getString(R.string.notification_text)
            )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(
                R.drawable.ic_stop_24,
                getString(R.string.notification_stop),
                stopPendingIntent
            )
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
