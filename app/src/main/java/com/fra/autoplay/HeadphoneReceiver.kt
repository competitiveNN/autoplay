package com.fra.autoplay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import androidx.core.content.ContextCompat

class HeadphoneConnectionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "HeadphoneReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_HEADSET_PLUG) return

        val state = intent.getIntExtra("state", -1)
        if (state != 1) return

        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        if (!devices.any { MediaPlaybackService.isHeadphoneAllowed(context, it) }) return

        if (MediaPlaybackService.isRunning()) {
            // Service already active — trigger resume directly via the
            // TEST_RESUME action, which the service handles in onStartCommand.
            Log.d(TAG, "Headphones plugged while service running; triggering resume")
            val resumeIntent = Intent(context, MediaPlaybackService::class.java).apply {
                action = "com.fra.autoplay.action.TEST_RESUME"
            }
            context.startService(resumeIntent)
        } else {
            // Service not running — start it so it can register its audio
            // device callback and handle the next plug event.
            Log.d(TAG, "Headphones plugged while service stopped; starting service")
            ensureServiceRunning(context)
        }
    }

    /**
     * Starts [MediaPlaybackService] if it is not already running.
     *
     * On Android 8.0+ a foreground service must be started via
     * [Context.startForegroundService]. On Android 12+ starting a foreground
     * service from the background is generally not allowed; [Intent.ACTION_HEADSET_PLUG]
     * is not an exempted broadcast, so [IllegalStateException] (specifically
     * [android.app.ForegroundServiceStartNotAllowedException]) is caught and handled
     * gracefully.
     */
    private fun ensureServiceRunning(context: Context) {
        val intent = Intent(context, MediaPlaybackService::class.java)
        try {
            ContextCompat.startForegroundService(context, intent)
        } catch (e: IllegalStateException) {
            // Android 12+ background FGS start restriction, or app in a bad state.
            Log.w(TAG, "Cannot start foreground service from background on plug", e)
        }
    }
}