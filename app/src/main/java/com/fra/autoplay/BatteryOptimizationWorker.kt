package com.fra.autoplay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BatteryOptimizationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val CHANNEL_ID = "autoplay_battery_channel"
        private const val NOTIFICATION_ID = 2
        const val WORK_NAME = "BatteryOptimizationCheck"
        const val CHECK_INTERVAL_HOURS = 4L
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Only check if battery reminder is enabled in preferences
            if (!PreferencesHelper.isBatteryReminderEnabled(applicationContext)) {
                return@withContext Result.success()
            }

            // Check if battery optimization is enabled for this app
            val isOptimizing = isBatteryOptimizationEnabled(applicationContext)
            
            if (isOptimizing) {
                showBatteryOptimizationNotification(applicationContext)
            } else {
                // Battery optimization is disabled, remove any existing notification
                removeBatteryOptimizationNotification(applicationContext)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun isBatteryOptimizationEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false
        }
        val pm = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        return !pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    private fun showBatteryOptimizationNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create channel if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AutoPlay Battery Optimization",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Warnings about battery optimization affecting AutoPlay"
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("AutoPlay: Battery optimization enabled")
            .setContentText("Disable battery optimization for reliable headphone detection")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun removeBatteryOptimizationNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}