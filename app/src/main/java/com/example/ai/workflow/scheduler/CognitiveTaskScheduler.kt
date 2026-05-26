package com.example.ai.workflow.scheduler

import android.content.Context
import androidx.work.*
import com.example.core.utils.Logger
import java.util.concurrent.TimeUnit

class CognitiveTaskScheduler(private val context: Context) {

    /**
     * Schedules periodic reasoning or memory maintenance cycles.
     */
    fun schedulePeriodicCognitiveCycle() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiresDeviceIdle(true)
            .build()
            
        val workRequest = PeriodicWorkRequestBuilder<CognitiveCycleWorker>(12, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
            
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "PeriodicCognitiveCycle",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        Logger.d("Scheduled periodic cognitive cycle.")
    }
    
    fun scheduleAdaptiveWorkflow(workflowId: String, delayMinutes: Long) {
        val inputData = Data.Builder()
            .putString("workflowId", workflowId)
            .build()
            
        val workRequest = OneTimeWorkRequestBuilder<WorkflowExecutionWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setInputData(inputData)
            .build()
            
        WorkManager.getInstance(context).enqueueUniqueWork(
            "AdaptiveWorkflow_$workflowId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        Logger.d("Scheduled adaptive workflow $workflowId in $delayMinutes minutes")
    }
}
