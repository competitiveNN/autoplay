package com.fra.autoplay

import android.content.Context
import android.content.Intent
import android.media.AudioDeviceInfo
import android.media.AudioManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MediaPlaybackServiceTest {

    @Test
    fun isHeadphone_constants_matchExpected() {
        val headphoneTypes = intArrayOf(
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_BLE_HEADSET,
            AudioDeviceInfo.TYPE_USB_HEADSET,
            AudioDeviceInfo.TYPE_HEARING_AID
        )

        val nonHeadphoneTypes = intArrayOf(
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
            AudioDeviceInfo.TYPE_BUILTIN_MIC,
            AudioDeviceInfo.TYPE_HDMI,
            AudioDeviceInfo.TYPE_LINE_ANALOG,
            AudioDeviceInfo.TYPE_LINE_DIGITAL,
            999
        )

        for (type in headphoneTypes) {
            assertTrue("Type $type should be recognized as headphone", isHeadphoneByReflection(type))
        }
        for (type in nonHeadphoneTypes) {
            assertFalse("Type $type should NOT be recognized as headphone", isHeadphoneByReflection(type))
        }
    }

    @Test
    fun isHeadphoneAllowed_filterOff_allowsEveryHeadphoneType() {
        val context = RuntimeEnvironment.getApplication()
        val allTypes = intArrayOf(
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_BLE_HEADSET,
            AudioDeviceInfo.TYPE_USB_HEADSET,
            AudioDeviceInfo.TYPE_HEARING_AID
        )
        for (type in allTypes) {
            val info = fakeAudioDeviceInfo(type)
            assertTrue("Filter off should allow type $type", isHeadphoneAllowedByReflection(context, info))
        }
    }

    @Test
    fun isHeadphoneAllowed_filterOn_onlyWiredAndUsb() {
        val context = RuntimeEnvironment.getApplication()
        setFilterHeadphones(context, true)
        try {
            val allowedTypes = intArrayOf(
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_WIRED_HEADSET,
                AudioDeviceInfo.TYPE_USB_HEADSET
            )
            val blockedTypes = intArrayOf(
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLE_HEADSET,
                AudioDeviceInfo.TYPE_HEARING_AID
            )
            for (type in allowedTypes) {
                val info = fakeAudioDeviceInfo(type)
                assertTrue("Filter on should allow type $type", isHeadphoneAllowedByReflection(context, info))
            }
            for (type in blockedTypes) {
                val info = fakeAudioDeviceInfo(type)
                assertFalse("Filter on should block type $type", isHeadphoneAllowedByReflection(context, info))
            }
        } finally {
            setFilterHeadphones(context, false)
        }
    }

    // --- helpers ---

    private fun isHeadphoneByReflection(type: Int): Boolean {
        val method = MediaPlaybackService::class.java.getDeclaredMethod("isHeadphone", AudioDeviceInfo::class.java)
        method.isAccessible = true
        return method.invoke(null, fakeAudioDeviceInfo(type)) as Boolean
    }

    private fun isHeadphoneAllowedByReflection(context: Context, device: AudioDeviceInfo): Boolean {
        val method = MediaPlaybackService::class.java.getDeclaredMethod("isHeadphoneAllowed", Context::class.java, AudioDeviceInfo::class.java)
        method.isAccessible = true
        return method.invoke(null, context, device) as Boolean
    }

    private fun fakeAudioDeviceInfo(type: Int): AudioDeviceInfo {
        // AudioDeviceInfo's constructor is hidden from third-party code; build one
        // via reflection on the most permissive constructor available in the SDK stub.
        val ctor = AudioDeviceInfo::class.java.declaredConstructors.maxByOrNull { it.parameterCount }
            ?: error("no constructor available")
        ctor.isAccessible = true
        val args = Array<Any?>(ctor.parameterCount) { i ->
            when (ctor.parameterTypes[i]) {
                Int::class.javaPrimitiveType, Int::class.java -> type
                String::class.java -> "test"
                else -> null
            }
        }
        return ctor.newInstance(*args) as AudioDeviceInfo
    }

    private fun setFilterHeadphones(context: Context, enabled: Boolean) {
        val prefs = contextSharedPreferences(context)
        prefs.edit().putBoolean("filter_headphones", enabled).apply()
    }

    private fun contextSharedPreferences(context: Context): android.content SharedPreferences =
        contextSharedPreferencesViaReflection(context)

    private fun contextSharedPreferencesViaReflection(context: Context): android.content SharedPreferences {
        val method = PreferencesHelper::class.java.getDeclaredMethod("prefs", Context::class.java)
        method.isAccessible = true
        return method.invoke(null, context) as android.content SharedPreferences
    }
}