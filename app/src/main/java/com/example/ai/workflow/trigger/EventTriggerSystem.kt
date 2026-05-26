package com.example.ai.workflow.trigger

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class TriggerType {
    TIME, APP_USAGE, LOCATION, SYSTEM_EVENT, INTENT, VOICE
}

data class Event(
    val type: TriggerType,
    val payload: Map<String, String>,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Subsystem where the OS/App events are pumped.
 * The workflow engine listens to this to jump-start or continue workflows.
 */
class EventTriggerSystem {
    private val _eventFlow = MutableSharedFlow<Event>(extraBufferCapacity = 64)
    val eventFlow = _eventFlow.asSharedFlow()

    suspend fun emitEvent(event: Event) {
        _eventFlow.emit(event)
    }
}
