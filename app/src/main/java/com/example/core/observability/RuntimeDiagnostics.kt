package com.example.core.observability

import com.example.core.utils.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Crash-safe diagnostics, workflow failure recovery, anomaly detection.
 */
class RuntimeDiagnostics(private val telemetryManager: TelemetryManager) {

    // A top-level exception handler to prevent app crashes from bad coroutines if injected appropriately
    val globalExceptionHandler = CoroutineExceptionHandler { _, exception ->
        telemetryManager.trackEvent(
            EventType.ERROR,
            "RuntimeDiagnostics",
            "Uncaught Coroutine Exception: ${exception.message}",
            mapOf("stacktrace" to exception.stackTraceToString())
        )
        Logger.e("Uncaught Coroutine Exception", exception, "RuntimeDiagnostics")
    }

    // Monitor runaway workflows (e.g., executing too many steps)
    fun checkRunawayWorkflow(workflowId: String, currentStep: Int, threshold: Int = 50) {
        if (currentStep > threshold) {
            telemetryManager.trackEvent(
                EventType.ANOMALY,
                "RuntimeDiagnostics",
                "Runaway workflow detected",
                mapOf("workflowId" to workflowId, "currentStep" to currentStep)
            )
            // Logic to potentially pause or abort the workflow would go here
        }
    }
}
