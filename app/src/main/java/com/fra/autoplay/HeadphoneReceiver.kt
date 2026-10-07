package com.fra.autoplay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build

class HeadphoneConnectionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_HEADSET_PLUG) return

        val state = intent.getIntExtra("state", -1)
        if (state == 1) {
            // Headphone plugged in
            if (MediaPlaybackService.isRunning()) {
                val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                    for (device in devices) {
                        if (device.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                            device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET
                        ) {
                            resumeMedia(context)
                            break
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    if (am.isWiredHeadsetOn) {
                        resumeMedia(context)
                    }
                }
            }
        }
    }

    private fun resumeMedia(context: Context) {
        try {
            val msm = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as android.media.session.MediaSessionManager
            val sessions = msm.getActiveSessions(null)
            for (controller in sessions) {
                if (controller.playbackState != null &&
                    controller.playbackState.state == android.media.session.PlaybackState.STATE_STOPPED
                ) {
                    controller.transportControls.play()
                }
            }
        } catch (_: SecurityException) {
            // Fallback: send media button
            val startTime = android.os.SystemClock.uptimeMillis()
            val down = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                putExtra(Intent.EXTRA_KEY_EVENT,
                    android.view.KeyEvent(startTime, startTime,
                        android.view.KeyEvent.ACTION_DOWN,
                        android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0))
            }
            val up = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                putExtra(Intent.EXTRA_KEY_EVENT,
                    android.view.KeyEvent(startTime, startTime,
                        android.view.KeyEvent.ACTION_UP,
                        android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0))
            }
            context.sendOrderedBroadcast(down, null)
            context.sendOrderedBroadcast(up, null)
        }
    }
}
