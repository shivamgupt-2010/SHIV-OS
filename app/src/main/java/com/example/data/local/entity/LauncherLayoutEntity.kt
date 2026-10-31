package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "launcher_layouts")
data class LauncherLayoutEntity(
    @PrimaryKey
    val id: String,
    val pageIndex: Int = 0,
    val cellX: Int = 0,
    val cellY: Int = 0,
    val spanX: Int = 1,
    val spanY: Int = 1,
    val itemType: String = "APP", // "APP", "WIDGET", "FOLDER", "SHORTCUT", "DOCK", "CUSTOM_WIDGET"
    val packageName: String = "",
    val activityName: String = "",
    val customLabel: String? = null,
    val folderId: String? = null,
    val appWidgetId: Int = -1,
    val widgetArchetype: String? = null, // "CLOCK", "TELEMETRY", "WEATHER", "AGENDA", "MEDIA", "SHIV_AI"
    val widgetConfigJson: String? = null,
    val isFloatingWindow: Boolean = false,
    val windowX: Float = 0f,
    val windowY: Float = 0f,
    val windowWidthDp: Int = 280,
    val windowHeightDp: Int = 360,
    val isMinimized: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
