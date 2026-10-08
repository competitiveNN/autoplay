package com.fra.autoplay

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.widget.RemoteViews

class AutoPlayWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.fra.autoplay.action.WIDGET_TOGGLE") {
            val isRunning = MediaPlaybackService.isRunning()
            val toggleIntent = Intent(context, MediaPlaybackService::class.java)
            if (isRunning) {
                toggleIntent.action = MediaPlaybackService.ACTION_STOP
            } else {
                toggleIntent.action = null // Start service normally
            }
            try {
                if (isRunning) {
                    context.stopService(toggleIntent)
                } else {
                    androidx.core.content.ContextCompat.startForegroundService(context, toggleIntent)
                }
            } catch (e: Exception) {
                // Ignore
            }
            // Update all widgets after toggle
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, AutoPlayWidgetProvider::class.java))
            for (id in ids) {
                updateWidget(context, appWidgetManager, id)
            }
        } else if (intent.action == PreferencesHelper.ACTION_PREFERENCES_CHANGED ||
                   intent.action == MediaPlaybackService.ACTION_SERVICE_STATE_CHANGED) {
            // Update widget when service state changes
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, AutoPlayWidgetProvider::class.java))
            for (id in ids) {
                updateWidget(context, appWidgetManager, id)
            }
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val isRunning = MediaPlaybackService.isRunning()
        val views = RemoteViews(context.packageName, R.layout.widget_autoplay)

        // Read widget config
        val prefs = context.getSharedPreferences("autoplay_widget_config", Context.MODE_PRIVATE)
        val transparency = prefs.getInt("widget_transparency_$appWidgetId", 0)
        val showIcon = prefs.getBoolean("widget_show_icon_$appWidgetId", true)
        val showLabel = prefs.getBoolean("widget_show_label_$appWidgetId", true)
        val compactMode = prefs.getBoolean("widget_compact_mode_$appWidgetId", false)

        // Apply transparency to background
        val bgColor = if (transparency > 0) {
            val alpha = (255 * (100 - transparency) / 100).toInt()
            Color.argb(alpha, 0, 0, 0)
        } else {
            Color.TRANSPARENT
        }
        views.setInt(R.id.widget_root, "setBackgroundColor", bgColor)

        // Show/hide icon
        views.setViewVisibility(R.id.widget_icon, if (showIcon) android.view.View.VISIBLE else android.view.View.GONE)

        // Show/hide label
        views.setViewVisibility(R.id.widget_status_text, if (showLabel) android.view.View.VISIBLE else android.view.View.GONE)

        // Compact mode - hide toggle button if compact
        if (compactMode) {
            views.setViewVisibility(R.id.widget_toggle_button, android.view.View.GONE)
        } else {
            views.setViewVisibility(R.id.widget_toggle_button, android.view.View.VISIBLE)
            views.setTextViewText(
                R.id.widget_toggle_button,
                context.getString(if (isRunning) R.string.widget_toggle_off else R.string.widget_toggle_on)
            )
        }

        views.setTextViewText(
            R.id.widget_status_text,
            context.getString(if (isRunning) R.string.widget_status_on else R.string.widget_status_off)
        )

        val toggleIntent = Intent("com.fra.autoplay.action.WIDGET_TOGGLE")
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, toggleIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.widget_toggle_button, pendingIntent)

        val appIntent = Intent(context, MainActivity::class.java)
        val appPendingIntent = PendingIntent.getActivity(
            context, 0, appIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.widget_icon, appPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, AutoPlayWidgetProvider::class.java))
            for (id in ids) {
                val provider = AutoPlayWidgetProvider()
                provider.updateWidget(context, appWidgetManager, id)
            }
        }
    }
}