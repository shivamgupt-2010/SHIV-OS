package com.example.ai.tool.workflow

import android.content.Context
import androidx.work.*
import com.example.core.utils.Logger
import java.util.UUID
import java.util.concurrent.TimeUnit

class WorkflowManager(private val context: Context) {

    fun enqueueTask(taskId: String, payload: String, delayMinutes: Long = 0) {
        val inputData = Data.Builder()
            .putString("task_id", taskId)
            .putString("payload", payload)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
            
        val workRequest = OneTimeWorkRequestBuilder<BackgroundExecutionWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)

        if (delayMinutes > 0) {
            workRequest.setInitialDelay(delayMinutes, TimeUnit.MINUTES)
        }

        WorkManager.getInstance(context).enqueueUniqueWork(
            taskId,
            ExistingWorkPolicy.REPLACE, // Restart if already running
            workRequest.build()
        )
        Logger.d("Enqueued background task: $taskId (Delay: $delayMinutes min)")
    }

    fun cancelTask(taskId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(taskId)
        Logger.d("Cancelled background task: $taskId")
    }
}
