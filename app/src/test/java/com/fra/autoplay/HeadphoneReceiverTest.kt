package com.fra.autoplay

import android.content.Context
import android.content.Intent
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Bundle
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.ShadowPreferences
import org.robolectric.annotation.Config
import java.lang.reflect.Method

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class HeadphoneReceiverTest {

    @Test
    fun onReceive_ignoresNonHeadsetIntent() {
        val receiver = HeadphoneConnectionReceiver()
        val context = org.robolectric.Robolectric.buildApplication().application
        val intent = Intent("android.intent.action.SOME_OTHER_ACTION")
        // Should not throw; just return.
        receiver.onReceive(context, intent)
    }

    @Test
    fun onReceive_ignoresHeadphoneUnplug() {
        val receiver = HeadphoneConnectionReceiver()
        val context = org.robolectric.Robolectric.buildApplication().application
        val intent = Intent(Intent.ACTION_HEADSET_PLUG).apply {
            putExtra("state", 0)
        }
        receiver.onReceive(context, intent)
    }

    @Test
    fun onReceive_handlesServiceNotRunning() {
        val receiver = HeadphoneConnectionReceiver()
        val context = org.robolectric.Robolectric.buildApplication().application
        val intent = Intent(Intent.ACTION_HEADSET_PLUG).apply {
            putExtra("state", 1)
        }
        // Service not running -> isRunning() returns false -> no crash.
        receiver.onReceive(context, intent)
    }
}