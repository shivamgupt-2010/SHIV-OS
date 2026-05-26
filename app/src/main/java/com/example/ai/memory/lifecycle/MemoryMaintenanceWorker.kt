package com.example.ai.memory.lifecycle

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.core.utils.Logger

class MemoryMaintenanceWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    // Dependency Injection will supply MemoryLifecycleSystem in a real app, 
    // for this setup we assume it's created or injected via a WorkerFactory.
    // However, to keep it simple and compileable, we will mock the execution if not injected
    
    // var lifecycleSystem: MemoryLifecycleSystem? = null

    override suspend fun doWork(): Result {
        Logger.d("MemoryMaintenanceWorker started.")
        return try {
            val appContainer = (applicationContext as com.example.ShivAiApplication).container
            appContainer.memoryLifecycleSystem.runNightlyMaintenance()
            Logger.d("MemoryMaintenanceWorker finished successfully.")
            Result.success()
        } catch (e: Exception) {
            Logger.e("MemoryMaintenanceWorker failed.", e)
            Result.retry()
        }
    }
}
