package com.example.core.observability

import com.example.core.utils.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID

/**
 * Production-grade telemetry system for tracking application execution,
 * AI requests, workflow timings, and orchestration logs.
 */
class TelemetryManager {
    
    private val _telemetryEvents = MutableSharedFlow<TelemetryEvent>(extraBufferCapacity = 100)
    val telemetryEvents = _telemetryEvents.asSharedFlow()
    
    fun trackEvent(
        eventType: EventType,
        tag: String,
        message: String,
        metadata: Map<String, Any> = emptyMap()
    ) {
        val event = TelemetryEvent(
            id = UUID.randomUUID().toString(),
            type = eventType,
            tag = tag,
            message = message,
            metadata = metadata,
            timestamp = System.currentTimeMillis()
        )
        
        Logger.d("[$eventType] $tag: $message")
        
        // Non-blocking emit
        _telemetryEvents.tryEmit(event)
    }
    
    fun startWorkflowTrace(workflowId: String, workflowName: String): Trace {
        trackEvent(EventType.WORKFLOW_START, "WorkflowTrace", "Started $workflowName", mapOf("workflowId" to workflowId))
        return Trace(workflowId, workflowName, this)
    }
}

enum class EventType {
    INFO, WARNING, ERROR, WORKFLOW_START, WORKFLOW_END, AI_REQUEST, AI_RESPONSE, TOOL_EXECUTION, ANOMALY
}

data class TelemetryEvent(
    val id: String,
    val type: EventType,
    val tag: String,
    val message: String,
    val metadata: Map<String, Any>,
    val timestamp: Long
)

class Trace(
    val id: String,
    val name: String,
    private val telemetryManager: TelemetryManager
) {
    private val startTime = System.currentTimeMillis()
    
    fun end() {
        val duration = System.currentTimeMillis() - startTime
        telemetryManager.trackEvent(
            EventType.WORKFLOW_END,
            "WorkflowTrace",
            "Ended $name",
            mapOf("workflowId" to id, "durationMs" to duration)
        )
    }
    
    fun fail(reason: String) {
        val duration = System.currentTimeMillis() - startTime
        telemetryManager.trackEvent(
            EventType.ERROR,
            "WorkflowTrace",
            "Failed $name: $reason",
            mapOf("workflowId" to id, "durationMs" to duration, "reason" to reason)
        )
    }
}
