package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "pending_task")
data class PendingTaskEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val agentType: String, // Which agent handles this
    val payload: String, // JSON payload for the task
    val priority: Int,
    val status: String, // "pending", "in_progress", "completed", "failed"
    val timestamp: Long = System.currentTimeMillis()
)
