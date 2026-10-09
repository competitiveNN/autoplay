package com.fra.autoplay

import android.app.Application
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.util.concurrent.TimeUnit

class AutoPlayApplication : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()

    override fun onCreate() {
        super.onCreate()
        // Instrumentation (and direct boot) can create the Application before
        // the user is unlocked, when credential-encrypted SharedPreferences
        // are unavailable. Defer prefs-dependent init until unlock.
        val userManager = getSystemService(android.os.UserManager::class.java)
        if (userManager != null && !userManager.isUserUnlocked) return
        PreferencesHelper.migrateIfNeeded(this)
        initCrashlyticsIfOptedIn()
        initDiagnosticsIfOptedIn()
        applyTheme()
        scheduleBatteryOptimizationCheck()
    }

    private fun initCrashlyticsIfOptedIn() {
        try {
            if (PreferencesHelper.isDiagnosticsEnabled(this)) {
                FirebaseApp.initializeApp(this)
                FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
            } else {
                // Only try to disable if Firebase is already initialized
                if (FirebaseApp.getApps(this).isNotEmpty()) {
                    FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
                }
            }
        } catch (_: Exception) {
            // Firebase not configured (e.g., test environment) — skip silently
        }
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