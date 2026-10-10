package com.fra.autoplay

import android.content.Intent
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HeadphoneReceiverTest {

    @Test
    fun onReceive_ignoresNonHeadsetIntent() {
        val receiver = HeadphoneConnectionReceiver()
        val context = RuntimeEnvironment.getApplication()
        val intent = Intent("android.intent.action.SOME_OTHER_ACTION")
        // Should not throw; just return.
        receiver.onReceive(context, intent)
    }

    @Test
    fun onReceive_ignoresHeadphoneUnplug() {
        val receiver = HeadphoneConnectionReceiver()
        val context = RuntimeEnvironment.getApplication()
        val intent = Intent(Intent.ACTION_HEADSET_PLUG).apply {
            putExtra("state", 0)
        }
        receiver.onReceive(context, intent)
    }

    @Test
    fun onReceive_handlesServiceNotRunning() {
        val receiver = HeadphoneConnectionReceiver()
        val context = RuntimeEnvironment.getApplication()
        val intent = Intent(Intent.ACTION_HEADSET_PLUG).apply {
            putExtra("state", 1)
        }
        // Service not running -> isRunning() returns false -> attempts to start
        // the service. Must not crash.
        receiver.onReceive(context, intent)
    }

    @Test
    fun onReceive_handlesServiceRunning() {
        val receiver = HeadphoneConnectionReceiver()
        val context = RuntimeEnvironment.getApplication()
        val intent = Intent(Intent.ACTION_HEADSET_PLUG).apply {
            putExtra("state", 1)
        }
        // Simulate a running service so ensureServiceRunning() returns early.
        setServiceRunning(true)
        try {
            receiver.onReceive(context, intent)
        } finally {
            setServiceRunning(false)
        }
    }

    private fun setServiceRunning(value: Boolean) {
        val field = MediaPlaybackService::class.java.getDeclaredField("running")
        field.isAccessible = true
        field.set(null, value)
    }
}
