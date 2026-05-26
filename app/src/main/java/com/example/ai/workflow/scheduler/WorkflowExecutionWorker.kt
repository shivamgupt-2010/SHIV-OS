package com.example.ai.workflow.scheduler

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.core.utils.Logger

/**
 * Executes a specific workflow.
 */
class WorkflowExecutionWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    // Dependency injection handles actual engine in real app
    override suspend fun doWork(): Result {
        val workflowId = inputData.getString("workflowId") ?: return Result.failure()
        Logger.d("WorkflowExecutionWorker executing workflow: $workflowId")
        
        return try {
            val appContainer = (applicationContext as com.example.ShivAiApplication).container
            val engine = appContainer.autonomousWorkflowEngine
            engine.executeWorkflow(workflowId)
            Result.success()
        } catch (e: Exception) {
            Logger.e("Workflow execution failed", e)
            Result.retry()
        }
    }
}
