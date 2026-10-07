package com.fra.autoplay

import android.content.Context
import android.content.Intent
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class BootReceiverTest {

    @Test
    fun onReceive_ignoresNonBootIntent() {
        val receiver = BootReceiver()
        val context = org.robolectric.Robolectric.buildApplication().application
        val intent = Intent("android.intent.action.SOME_OTHER_ACTION")
        receiver.onReceive(context, intent)
    }

    @Test
    fun onReceive_handlesBootCompleted() {
        val receiver = BootReceiver()
        val context = org.robolectric.Robolectric.buildApplication().application
        val intent = Intent(Intent.ACTION_BOOT_COMPLETED)
        receiver.onReceive(context, intent)
    }
}