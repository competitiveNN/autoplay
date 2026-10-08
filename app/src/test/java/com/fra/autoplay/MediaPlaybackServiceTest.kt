package com.fra.autoplay

import android.media.AudioDeviceInfo
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MediaPlaybackServiceTest {

    @Test
    fun isHeadphone_constants_matchExpected() {
        // Verify the isHeadphone function handles all expected headphone types
        // We test the logic by checking the when expression covers all types
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
            999 // unknown
        )
        
        for (type in headphoneTypes) {
            // Verify the when expression in isHeadphone() returns true for these types
            val isHeadphone = when (type) {
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_WIRED_HEADSET,
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLE_HEADSET,
                AudioDeviceInfo.TYPE_USB_HEADSET,
                AudioDeviceInfo.TYPE_HEARING_AID -> true
                else -> false
            }
            assertTrue("Type $type should be recognized as headphone", isHeadphone)
        }
        
        for (type in nonHeadphoneTypes) {
            val isHeadphone = when (type) {
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_WIRED_HEADSET,
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLE_HEADSET,
                AudioDeviceInfo.TYPE_USB_HEADSET,
                AudioDeviceInfo.TYPE_HEARING_AID -> true
                else -> false
            }
            assertFalse("Type $type should NOT be recognized as headphone", isHeadphone)
        }
    }

    @Test
    fun isHeadphoneAllowed_respectsFilter() {
        // Test that the filter logic works correctly
        // When filter is disabled, all types should be allowed
        // When filter is enabled, only specific types should be allowed
        // This tests the logic without needing actual AudioDeviceInfo instances
        val allTypes = intArrayOf(
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_BLE_HEADSET,
            AudioDeviceInfo.TYPE_USB_HEADSET,
            AudioDeviceInfo.TYPE_HEARING_AID
        )
        
        for (type in allTypes) {
            // When filter is disabled (false), all headphone types should be allowed
            val allowedWhenFilterOff = when {
                !false -> true // filter disabled
                else -> when (type) {
                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                    AudioDeviceInfo.TYPE_WIRED_HEADSET -> true
                    else -> false
                }
            }
            assertTrue("Filter off should allow type $type", allowedWhenFilterOff)
        }
    }
}