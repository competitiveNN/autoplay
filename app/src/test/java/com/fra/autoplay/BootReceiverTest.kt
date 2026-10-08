package com.fra.autoplay

import android.content.Context
import android.content.Intent
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BootReceiverTest {

    @Test
    fun onReceive_ignoresNonBootIntent() {
        val receiver = BootReceiver()
        val context = RuntimeEnvironment.getApplication()
        val intent = Intent("android.intent.action.SOME_OTHER_ACTION")
        receiver.onReceive(context, intent)
    }

    @Test
    fun onReceive_handlesBootCompleted() {
        val receiver = BootReceiver()
        val context = RuntimeEnvironment.getApplication()
        val intent = Intent(Intent.ACTION_BOOT_COMPLETED)
        receiver.onReceive(context, intent)
    }
}