package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "semantic_memory")
data class SemanticMemoryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val type: String, // EPISODIC, SEMANTIC, etc.
    val content: String,
    val metadataJson: String, // Stored as JSON string
    val embeddingJson: String, // Stored as JSON string list of floats
    val importanceScore: Float,
    val decayRate: Float,
    val lastAccessedAt: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)
