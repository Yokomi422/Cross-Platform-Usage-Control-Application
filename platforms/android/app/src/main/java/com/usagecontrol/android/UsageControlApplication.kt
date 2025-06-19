package com.usagecontrol.android

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.usagecontrol.android.workers.UsageMonitoringWorker
import com.usagecontrol.android.workers.DailyResetWorker
import com.usagecontrol.android.workers.FirebaseSyncWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class UsageControlApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        
        // Initialize WorkManager with Hilt
        WorkManager.initialize(this, workManagerConfiguration)
        
        // Schedule background workers
        scheduleBackgroundWork()
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
    
    private fun scheduleBackgroundWork() {
        // Schedule usage monitoring
        UsageMonitoringWorker.enqueueWork(this)
        
        // Schedule daily reset
        DailyResetWorker.enqueueWork(this)
        
        // Schedule Firebase sync
        FirebaseSyncWorker.enqueueWork(this)
    }
}
