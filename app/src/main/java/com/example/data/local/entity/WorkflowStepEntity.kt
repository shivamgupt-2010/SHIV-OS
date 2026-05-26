package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "workflow_steps")
data class WorkflowStepEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val workflowId: String,
    val stepIndex: Int,
    val type: String, // TOOL_CALL, AI_REASONING, DELAY, CONDITION
    val actionPayloadJson: String, // The specific arguments for the tool or AI prompt
    val status: String, // PENDING, RUNNING, COMPLETED, FAILED
    val resultJson: String? = null,
    val retryCount: Int = 0
)
