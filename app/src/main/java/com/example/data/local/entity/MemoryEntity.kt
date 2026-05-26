package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "memory")
data class MemoryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val memoryKey: String, // e.g., "user_name", "preferred_language"
    val memoryValue: String,
    val importanceScore: Float, // For priority retrieval
    val timestamp: Long = System.currentTimeMillis()
)
