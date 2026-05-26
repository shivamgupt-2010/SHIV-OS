package com.example.ai.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AIExecutionState(
    val activeTaskId: String? = null,
    val currentAgent: String? = null,
    val status: ExecutionStatus = ExecutionStatus.IDLE,
    val error: String? = null
)

enum class ExecutionStatus {
    IDLE,
    ANALYZING_INTENT,
    EXECUTING,
    WAITING_FOR_TOOL,
    COMPLETED,
    FAILED
}

class AIStateManager {
    private val _executionState = MutableStateFlow(AIExecutionState())
    val executionState: StateFlow<AIExecutionState> = _executionState.asStateFlow()

    fun updateState(
        taskId: String? = _executionState.value.activeTaskId,
        agent: String? = _executionState.value.currentAgent,
        status: ExecutionStatus = _executionState.value.status,
        error: String? = null
    ) {
        _executionState.value = _executionState.value.copy(
            activeTaskId = taskId,
            currentAgent = agent,
            status = status,
            error = error
        )
    }

    fun clearState() {
        _executionState.value = AIExecutionState()
    }
}
