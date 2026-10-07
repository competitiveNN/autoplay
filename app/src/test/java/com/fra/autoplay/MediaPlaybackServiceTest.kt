package com.fra.autoplay

import org.junit.Assert.*
import org.junit.Test

class MediaPlaybackServiceTest {

    @Test
    fun isHeadphone_returnsTrue_forWiredHeadphones() {
        val device = mockAudioDevice(AudioDeviceInfo.TYPE_WIRED_HEADPHONES)
        assertTrue(MediaPlaybackService.isHeadphone(device))
    }

    @Test
    fun isHeadphone_returnsTrue_forWiredHeadset() {
        val device = mockAudioDevice(AudioDeviceInfo.TYPE_WIRED_HEADSET)
        assertTrue(MediaPlaybackService.isHeadphone(device))
    }

    @Test
    fun isHeadphone_returnsTrue_forBluetoothHeadphones() {
        val device = mockAudioDevice(AudioDeviceInfo.TYPE_BLUETOOTH_HEADPHONES)
        assertTrue(MediaPlaybackService.isHeadphone(device))
    }

    @Test
    fun isHeadphone_returnsTrue_forBluetoothA2dp() {
        val device = mockAudioDevice(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)
        assertTrue(MediaPlaybackService.isHeadphone(device))
    }

    @Test
    fun isHeadphone_returnsTrue_forUsbHeadphones() {
        val device = mockAudioDevice(AudioDeviceInfo.TYPE_USB_HEADPHONES)
        assertTrue(MediaPlaybackService.isHeadphone(device))
    }

    @Test
    fun isHeadphone_returnsTrue_forUsbHeadset() {
        val device = mockAudioDevice(AudioDeviceInfo.TYPE_USB_HEADSET)
        assertTrue(MediaPlaybackService.isHeadphone(device))
    }

    @Test
    fun isHeadphone_returnsTrue_forDigitalHeadphones() {
        val device = mockAudioDevice(AudioDeviceInfo.TYPE_DIGITAL_HEADPHONES)
        assertTrue(MediaPlaybackService.isHeadphone(device))
    }

    @Test
    fun isHeadphone_returnsTrue_forHearingAid() {
        val device = mockAudioDevice(AudioDeviceInfo.TYPE_HEARING_AID)
        assertTrue(MediaPlaybackService.isHeadphone(device))
    }

    @Test
    fun isHeadphone_returnsFalse_forSpeaker() {
        val device = mockAudioDevice(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
        assertFalse(MediaPlaybackService.isHeadphone(device))
    }

    @Test
    fun isHeadphone_returnsFalse_forMicrophone() {
        val device = mockAudioDevice(AudioDeviceInfo.TYPE_BUILTIN_MIC)
        assertFalse(MediaPlaybackService.isHeadphone(device))
    }

    @Test
    fun isHeadphone_returnsFalse_forUnknownDevice() {
        val device = mockAudioDevice(999)
        assertFalse(MediaPlaybackService.isHeadphone(device))
    }

    private fun mockAudioDevice(type: Int): AudioDeviceInfo {
        return AudioDeviceInfo.Builder()
            .setId(0)
            .setProductName("test")
            .setAddress("test")
            .setType(type)
            .setIsSink(true)
            .build()
    }
}