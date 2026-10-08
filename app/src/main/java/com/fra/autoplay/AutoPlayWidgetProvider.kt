package com.fra.autoplay

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
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

        views.setTextViewText(
            R.id.widget_status_text,
            context.getString(if (isRunning) R.string.widget_status_on else R.string.widget_status_off)
        )
        views.setTextViewText(
            R.id.widget_toggle_button,
            context.getString(if (isRunning) R.string.widget_toggle_off else R.string.widget_toggle_on)
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