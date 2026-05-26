package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "chat_history")
data class ChatHistoryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val role: String, // "user" or "agent"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
