package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey
    val id: String,
    val collection: String, // "launcher_layouts", "launcher_themes", "launcher_family", "wellbeing_summaries"
    val recordId: String,
    val action: String, // "UPSERT", "DELETE"
    val payloadJson: String,
    val retryCount: Int = 0,
    val status: String = "PENDING", // "PENDING", "IN_PROGRESS", "FAILED", "SYNCED"
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAttemptAt: Long = 0L
)
