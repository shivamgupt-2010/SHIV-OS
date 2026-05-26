package com.example.ai.workflow.domain.model

enum class WorkflowStatus {
    PENDING, RUNNING, PAUSED, COMPLETED, FAILED, CANCELLED
}

enum class StepType {
    TOOL_CALL, AI_REASONING, DELAY, CONDITION
}

data class Workflow(
    val id: String,
    val name: String,
    val description: String,
    val status: WorkflowStatus,
    val currentStepIndex: Int,
    val steps: List<WorkflowStep>,
    val progressMetadata: Map<String, String>,
    val createdAt: Long,
    val updatedAt: Long
)

data class WorkflowStep(
    val id: String,
    val workflowId: String,
    val stepIndex: Int,
    val type: StepType,
    val actionPayload: Map<String, String>, // Instructions or Tool args
    val status: WorkflowStatus,
    val result: String?,
    val retryCount: Int
)
