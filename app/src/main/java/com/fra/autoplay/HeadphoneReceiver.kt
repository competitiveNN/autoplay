package com.fra.autoplay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.SystemClock
import android.view.KeyEvent

class HeadphoneConnectionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_HEADSET_PLUG) return

        val state = intent.getIntExtra("state", -1)
        if (state == 1) {
            // Headphone plugged in
            if (MediaPlaybackService.isRunning()) {
                val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                if (devices.any { MediaPlaybackService.isHeadphone(it) }) {
                    resumeMedia(context)
                }
            }
        }
    }

    private fun resumeMedia(context: Context) {
        try {
            val msm = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as android.media.session.MediaSessionManager
            val sessions = msm.getActiveSessions(null)
            for (controller in sessions) {
                val state = controller.playbackState?.state ?: continue
                if (state == android.media.session.PlaybackState.STATE_STOPPED ||
                    state == android.media.session.PlaybackState.STATE_PAUSED
                ) {
                    controller.transportControls.play()
                }
            }
        } catch (_: SecurityException) {
            // Fallback: send media button
            val startTime = SystemClock.uptimeMillis()
            val down = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                putExtra(Intent.EXTRA_KEY_EVENT,
                    KeyEvent(startTime, startTime,
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0))
            }
            val up = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                putExtra(Intent.EXTRA_KEY_EVENT,
                    KeyEvent(startTime, startTime,
                        KeyEvent.ACTION_UP,
                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0))
            }
            context.sendOrderedBroadcast(down, null)
            context.sendOrderedBroadcast(up, null)
        }
    }
}
