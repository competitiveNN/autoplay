package com.fra.autoplay

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class QuickSettingsTileServiceTest {

    @Test
    fun tileServiceClassIsTileService() {
        val clazz = QuickSettingsTileService::class.java
        assertTrue(android.service.quicksettings.TileService::class.java.isAssignableFrom(clazz))
        assertTrue(clazz.declaredMethods.any { it.name == "onClick" })
    }
}
