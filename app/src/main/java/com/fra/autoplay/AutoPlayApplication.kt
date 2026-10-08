package com.fra.autoplay

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class AutoPlayApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        scheduleBatteryOptimizationCheck()
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