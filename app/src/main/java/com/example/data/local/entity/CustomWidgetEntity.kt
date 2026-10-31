package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_widget_templates")
data class CustomWidgetEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val archetype: String, // "CLOCK", "TELEMETRY", "WEATHER", "AGENDA", "MEDIA", "SHIV_AI"
    val surfaceStyle: String = "GLASS", // "GLASS", "MATTE", "HOLOGRAM", "TERMINAL", "OUTLINE"
    val primaryColorHex: String = "#55E7FF",
    val accentColorHex: String = "#7657FF",
    val backgroundColorHex: String = "#1A1A24",
    val cornerRadiusDp: Int = 18,
    val blurRadiusDp: Int = 16,
    val spanX: Int = 2,
    val spanY: Int = 2,
    val showBorder: Boolean = true,
    val typographyStyle: String = "DEFAULT",
    val configJson: String = "{}",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
