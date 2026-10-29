package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val category: String = "General",
    val colorHex: String = "#1E293B",
    val isPinned: Boolean = false,
    val isTrashed: Boolean = false,
    val checklistJson: String = "[]",
    val imageUrisJson: String = "[]",
    val drawingPathsJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
