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
            if (MediaPlaybackService.isRunning()) {
                val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                if (devices.any { MediaPlaybackService.isHeadphoneAllowed(context, it) }) {
                    resumeMedia(context)
                }
            }
        }
    }

    private fun resumeMedia(context: Context) {
        val intent = Intent(context, MediaPlaybackService::class.java).apply {
            action = "com.fra.autoplay.action.TEST_RESUME"
        }
        context.startForegroundService(intent)
    }
}