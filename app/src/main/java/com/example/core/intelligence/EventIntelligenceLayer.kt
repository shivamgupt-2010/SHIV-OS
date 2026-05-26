package com.example.core.intelligence

import com.example.core.context.ContextAwarenessManager
import com.example.core.observability.TelemetryManager
import com.example.core.observability.EventType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Event Intelligence Layer
 * Upgrades EventTriggerSystem to support contextual weighting and proactive logic.
 */
class EventIntelligenceLayer(
    private val contextAwarenessManager: ContextAwarenessManager,
    private val telemetryManager: TelemetryManager
) {
    private val _intelligentEvents = MutableSharedFlow<IntelligentEvent>(extraBufferCapacity = 50)
    val intelligentEvents = _intelligentEvents.asSharedFlow()

    fun evaluateEventCondition(triggerSource: String, data: String) {
        val deviceContext = contextAwarenessManager.deviceState.value
        
        // Example: Only trigger heavy workflows if charging or battery > 20%
        val isSafeToRunHeavyTask = deviceContext.isCharging || (deviceContext.isScreenOn)

        val priority = if (isSafeToRunHeavyTask) EventPriority.HIGH else EventPriority.LOW

        val event = IntelligentEvent(
            source = triggerSource,
            payload = data,
            priority = priority,
            contextSnapshot = deviceContext
        )

        telemetryManager.trackEvent(
            EventType.INFO,
            "EventIntelligence",
            "Evaluated event from $triggerSource with priority ${priority.name}",
            mapOf("priority" to priority.name)
        )

        _intelligentEvents.tryEmit(event)
    }
}

enum class EventPriority {
    CRITICAL, HIGH, NORMAL, LOW, BACKGROUND
}

data class IntelligentEvent(
    val source: String,
    val payload: String,
    val priority: EventPriority,
    val contextSnapshot: com.example.core.context.DeviceStateContext
)
