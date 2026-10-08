package com.fra.autoplay

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class AutoPlayApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PreferencesHelper.migrateIfNeeded(this)
        initDiagnosticsIfOptedIn()
        applyTheme()
        scheduleBatteryOptimizationCheck()
    }

    private fun initDiagnosticsIfOptedIn() {
        if (!PreferencesHelper.isDiagnosticsEnabled(this)) return
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val log = java.io.File(filesDir, "diagnostics_crash.log")
                val ts = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.ROOT).format(java.util.Date())
                log.appendText("[$ts] ${throwable::class.java.name}: ${throwable.message}\n${throwable.stackTraceToString()}\n---\n")
            } catch (_: Exception) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun applyTheme() {
        val theme = PreferencesHelper.getAppTheme(this)
        val mode = when (theme) {
            "light" -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            else -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun scheduleBatteryOptimizationCheck() {
        val workRequest = PeriodicWorkRequest.Builder(
            BatteryOptimizationWorker::class.java,
            BatteryOptimizationWorker.CHECK_INTERVAL_HOURS,
            TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                BatteryOptimizationWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }
}