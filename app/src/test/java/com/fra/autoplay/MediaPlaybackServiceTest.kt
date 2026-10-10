package com.fra.autoplay

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioPort
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

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
            assertTrue("Type $type should be recognized as headphone", MediaPlaybackService.isHeadphone(fakeAudioDeviceInfo(type)))
        }
        for (type in nonHeadphoneTypes) {
            assertFalse("Type $type should NOT be recognized as headphone", MediaPlaybackService.isHeadphone(fakeAudioDeviceInfo(type)))
        }
    }

    @Test
    fun isHeadphoneAllowed_filterOff_allowsEveryHeadphoneType() {
        val context = RuntimeEnvironment.getApplication()
        PreferencesHelper.setFilterHeadphones(context, false)
        val allTypes = intArrayOf(
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_BLE_HEADSET,
            AudioDeviceInfo.TYPE_USB_HEADSET,
            AudioDeviceInfo.TYPE_HEARING_AID
        )
        for (type in allTypes) {
            assertTrue("Filter off should allow type $type", MediaPlaybackService.isHeadphoneAllowed(context, fakeAudioDeviceInfo(type)))
        }
    }

    @Test
    fun isHeadphoneAllowed_filterOn_onlyWiredAndUsb() {
        val context = RuntimeEnvironment.getApplication()
        PreferencesHelper.setFilterHeadphones(context, true)
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
            assertTrue("Filter on should allow type $type", MediaPlaybackService.isHeadphoneAllowed(context, fakeAudioDeviceInfo(type)))
        }
        for (type in blockedTypes) {
            assertFalse("Filter on should block type $type", MediaPlaybackService.isHeadphoneAllowed(context, fakeAudioDeviceInfo(type)))
        }
    }

    // --- helpers ---

    private fun fakeAudioDeviceInfo(type: Int): AudioDeviceInfo {
        // AudioDeviceInfo is final and its constructor is package-private, and
        // getType() maps the internal AudioDevicePort.mType through
        // INT_TO_EXT_DEVICE_MAPPING. Build a real AudioDevicePort carrying the
        // internal port type that maps to the desired public type, then pass it
        // through the package-private constructor.
        val portClass = Class.forName("android.media.AudioDevicePort")
        val portCtor = portClass.getDeclaredConstructors().maxByOrNull { it.parameterCount }
            ?: error("no AudioDevicePort constructor available")
        portCtor.isAccessible = true
        val port = portCtor.newInstance(
            null, "test", intArrayOf(44100), intArrayOf(1), intArrayOf(0),
            intArrayOf(16), null, AudioPort.TYPE_DEVICE, "", intArrayOf(0), intArrayOf(0)
        )

        val mappingField = Class.forName("android.media.AudioDeviceInfo")
            .getDeclaredField("INT_TO_EXT_DEVICE_MAPPING").apply { isAccessible = true }
        val mapping = mappingField.get(null) as android.util.SparseIntArray
        val internalType = (0 until mapping.size())
            .firstOrNull { mapping.valueAt(it) == type }
            ?.let { mapping.keyAt(it) }
            ?: type

        val typeField = portClass.getDeclaredField("mType").apply { isAccessible = true }
        typeField.set(port, internalType)

        val infoClass = Class.forName("android.media.AudioDeviceInfo")
        val infoCtor = infoClass.getDeclaredConstructor(portClass).apply { isAccessible = true }
        val d = infoCtor.newInstance(port) as AudioDeviceInfo
        val actual = d.type
        check(actual == type || (type == 999 && actual == 0)) { "wanted $type got $actual (internal=$internalType)" }
        return d
    }
}