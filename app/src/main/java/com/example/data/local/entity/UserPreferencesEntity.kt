package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey
    val key: String,
    val value: String,
    val timestamp: Long = System.currentTimeMillis()
)
