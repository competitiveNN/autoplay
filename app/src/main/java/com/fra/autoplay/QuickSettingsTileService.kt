package com.fra.autoplay

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

@RequiresApi(Build.VERSION_CODES.N)
class QuickSettingsTileService : TileService() {

    override fun onTileAdded() {
        super.onTileAdded()
        updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        val isRunning = MediaPlaybackService.isRunning()
        val toggleIntent = Intent(this, MediaPlaybackService::class.java).apply {
            if (isRunning) {
                action = MediaPlaybackService.ACTION_STOP
            } else {
                action = null
            }
        }
        try {
            if (isRunning) {
                stopService(toggleIntent)
            } else {
                ContextCompat.startForegroundService(this, toggleIntent)
            }
        } catch (_: Exception) {
        }
        updateTile()
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        val isRunning = MediaPlaybackService.isRunning()
        tile.state = if (isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.app_name)
        tile.contentDescription = getString(R.string.quick_settings_tile_label)
        tile.updateTile()
    }
}
