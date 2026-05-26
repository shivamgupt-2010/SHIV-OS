package com.example.core.observability

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicInteger

/**
 * Agent performance metrics, semantic retrieval efficiency, token usages.
 */
class AIExecutionAnalyzer(private val telemetryManager: TelemetryManager) {

    private val _metrics = MutableStateFlow(ExecutionMetrics())
    val metrics = _metrics.asStateFlow()

    fun trackAiRequest(
        agentId: String,
        promptTokens: Int,
        completionTokens: Int,
        latencyMs: Long,
        successful: Boolean
    ) {
        telemetryManager.trackEvent(
            EventType.AI_REQUEST,
            "AIExecutionAnalyzer",
            "AI Request completed - Success: $successful, Latency: ${latencyMs}ms",
            mapOf(
                "agentId" to agentId,
                "promptTokens" to promptTokens,
                "completionTokens" to completionTokens,
                "latencyMs" to latencyMs
            )
        )

        // Update real-time metrics
        _metrics.value = _metrics.value.copy(
            totalTokensUsed = _metrics.value.totalTokensUsed + promptTokens + completionTokens,
            totalRequests = _metrics.value.totalRequests + 1,
            successfulRequests = if (successful) _metrics.value.successfulRequests + 1 else _metrics.value.successfulRequests,
            averageLatencyMs = calculateNewAverage(_metrics.value.averageLatencyMs, _metrics.value.totalRequests, latencyMs)
        )
    }

    private fun calculateNewAverage(currentAvg: Long, count: Int, newValue: Long): Long {
        if (count == 0) return newValue
        return ((currentAvg * count) + newValue) / (count + 1)
    }
    
    fun trackToolExecution(toolName: String, durationMs: Long, successful: Boolean) {
        telemetryManager.trackEvent(
            EventType.TOOL_EXECUTION,
            "AIExecutionAnalyzer",
            "Tool $toolName execution completed",
            mapOf("toolName" to toolName, "durationMs" to durationMs, "successful" to successful)
        )
    }
}

data class ExecutionMetrics(
    val totalTokensUsed: Int = 0,
    val totalRequests: Int = 0,
    val successfulRequests: Int = 0,
    val averageLatencyMs: Long = 0L
)
