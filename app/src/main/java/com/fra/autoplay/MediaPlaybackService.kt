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
import android.media.session.MediaSessionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.getSystemService

class MediaPlaybackService : Service() {

    companion object {
        private const val TAG = "MediaPlaybackService"
        private const val CHANNEL_ID = "autoplay_channel"
        const val ACTION_STOP = "com.fra.autoplay.action.STOP"
        private const val NOTIFICATION_ID = 1
        private var running = false
        const val ACTION_SERVICE_STATE_CHANGED = "com.fra.autoplay.action.SERVICE_STATE_CHANGED"

        fun isRunning(): Boolean = running

        /** Returns true if the given audio device represents any kind of headphone. */
        @Suppress("NewApi") // TYPE_BLE_HEADSET / TYPE_HEARING_AID are API 31+ compile-time constants;
        // they are inlined and never reported on API 26-30 so the check is safe.
        fun isHeadphone(device: AudioDeviceInfo): Boolean = when (device.type) {
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_USB_HEADSET,
            AudioDeviceInfo.TYPE_BLE_HEADSET,
            AudioDeviceInfo.TYPE_HEARING_AID -> true
            else -> false
        }

        /** Returns true if the headphone type matches the user's filter preferences. */
        @Suppress("NewApi") // same API 31+ note as isHeadphone()
        fun isHeadphoneAllowed(context: Context, device: AudioDeviceInfo): Boolean {
            if (!PreferencesHelper.isFilterHeadphones(context)) {
                return true
            }
            return when (device.type) {
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_WIRED_HEADSET -> true
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLE_HEADSET,
                AudioDeviceInfo.TYPE_USB_HEADSET,
                AudioDeviceInfo.TYPE_HEARING_AID -> true
                else -> false
            }
        }
    }

    private lateinit var audioManager: AudioManager
    private lateinit var mediaSessionManager: MediaSessionManager
    private var batteryOptimizationEnabled = false
    private var audioCallbackRegistered = false
    private var prefsReceiver: android.content.BroadcastReceiver? = null
    private val delayHandler = Handler(Looper.getMainLooper())
    private var resumeDelayMs: Long = 0

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
            super.onAudioDevicesAdded(addedDevices)
            val headphones = addedDevices.filter { isHeadphoneAllowed(this@MediaPlaybackService, it) }
            if (headphones.isNotEmpty()) {
                Log.d(TAG, "onAudioDevicesAdded: ${headphones.map { it.type }}")
                triggerResume()
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
        resumeDelayMs = PreferencesHelper.getResumeDelayMs(this)
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            batteryOptimizationEnabled = !pm.isIgnoringBatteryOptimizations(packageName)
        }
        // Listen for preference changes
        prefsReceiver = object : android.content.BroadcastReceiver() {
            @android.annotation.SuppressLint("MissingPermission")
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == PreferencesHelper.ACTION_PREFERENCES_CHANGED) {
                    resumeDelayMs = PreferencesHelper.getResumeDelayMs(context)
                    // Update battery optimization state in notification
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                        batteryOptimizationEnabled = !pm.isIgnoringBatteryOptimizations(context.packageName)
                    }
                    // Update notification with new battery state
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < 33) {
                        notificationManager.notify(NOTIFICATION_ID, buildNotification())
                    }
                }
            }
        }
        androidx.core.content.ContextCompat.registerReceiver(this, prefsReceiver, IntentFilter(PreferencesHelper.ACTION_PREFERENCES_CHANGED), androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle stop action
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Handle test resume action (also used by the notification Test Resume button
        // and by external callers to trigger resume when the service is running).
        if (intent?.action == "com.fra.autoplay.action.TEST_RESUME") {
            triggerResume()
            return START_STICKY
        }

        ServiceCompat.startForeground(
            this, NOTIFICATION_ID, buildNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            } else {
                0
            }
        )
        running = true
        sendBroadcast(Intent(ACTION_SERVICE_STATE_CHANGED))

        // Guard against double-registration on sticky restarts.
        if (!audioCallbackRegistered) {
            audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
            audioCallbackRegistered = true
        }

        // Check current audio devices for already-connected headphones.
        checkCurrentDevices()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        running = false
        sendBroadcast(Intent(ACTION_SERVICE_STATE_CHANGED))
        delayHandler.removeCallbacksAndMessages(null)
        if (audioCallbackRegistered) {
            audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
            audioCallbackRegistered = false
        }
        prefsReceiver?.let {
            try { unregisterReceiver(it) } catch (_: Exception) { /* ignore */ }
            prefsReceiver = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun checkCurrentDevices() {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val headphones = devices.filter { isHeadphoneAllowed(this, it) }
        if (headphones.isNotEmpty()) {
            Log.d(TAG, "checkCurrentDevices: headphones present ${headphones.map { it.type }}")
            triggerResume()
        } else {
            Log.d(TAG, "checkCurrentDevices: no headphones detected")
        }
    }

    fun triggerResume() {
        val context = this
        val delay = if (PreferencesHelper.isSmartResumeEnabled(context)) {
            PreferencesHelper.getSmartDelaySuggestion(context)
        } else {
            resumeDelayMs
        }
        Log.d(TAG, "triggerResume: delay=${delay}ms smart=${PreferencesHelper.isSmartResumeEnabled(context)}")

        if (delay > 0) {
            delayHandler.postDelayed({ resumeMediaIfStopped() }, delay)
        } else {
            resumeMediaIfStopped()
        }
    }

    fun resumeMediaIfStopped() {
        try {
            val sessions = mediaSessionManager.getActiveSessions(null)
            Log.d(TAG, "resumeMediaIfStopped: ${sessions.size} active session(s)")
            // Prioritize sessions that support transport controls and are closer to playing.
            @Suppress("ALWAYS_TRUE_OR_FALSE")
            val candidates = sessions
                .filter { it.transportControls != null }
                .filter { !PreferencesHelper.isPackageExcluded(this, it.packageName) }
                .sortedByDescending { it.playbackState?.lastPositionUpdateTime ?: 0L }

            var resumed = false
            for (controller in candidates) {
                val state = controller.playbackState?.state
                Log.d(TAG, "  candidate=${controller.packageName} state=$state")
                // "not playing" -> resume. Handles both STOPPED and PAUSED states.
                if (state == android.media.session.PlaybackState.STATE_STOPPED ||
                    state == android.media.session.PlaybackState.STATE_PAUSED
                ) {
                    controller.transportControls.play()
                    resumed = true
                    // Only resume the most relevant session to avoid conflicts.
                    break
                }
            }
            if (!resumed) {
                Log.d(TAG, "resumeMediaIfStopped: no session eligible for resume")
            }
        } catch (_: SecurityException) {
            // Permission not granted; try fallback via media button injection.
            Log.w(TAG, "resumeMediaIfStopped: MediaSessionManager access denied; falling back to media button")
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
        val testResumePendingIntent = PendingIntent.getService(
            this, 1,
            Intent(this, MediaPlaybackService::class.java).apply { action = "com.fra.autoplay.action.TEST_RESUME" },
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
            .addAction(
                android.R.drawable.ic_media_play,
                getString(R.string.notification_test_resume),
                testResumePendingIntent
            )
            .extend(
                androidx.core.app.NotificationCompat.WearableExtender()
                    .addAction(
                        NotificationCompat.Action(
                            android.R.drawable.ic_media_play,
                            getString(R.string.notification_test_resume),
                            testResumePendingIntent
                        )
                    )
                    .addAction(
                        NotificationCompat.Action(
                            R.drawable.ic_stop_24,
                            getString(R.string.notification_stop),
                            stopPendingIntent
                        )
                    )
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
