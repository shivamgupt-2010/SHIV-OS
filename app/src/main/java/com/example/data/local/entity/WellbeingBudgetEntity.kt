package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wellbeing_budgets")
data class WellbeingBudgetEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val dailyBudgetMinutes: Int = 60,
    val intentionalOpeningEnabled: Boolean = false,
    val category: String = "GENERAL", // "SOCIAL", "ENTERTAINMENT", "PRODUCTIVITY", "GAMES", "GENERAL"
    val updatedAt: Long = System.currentTimeMillis()
)
