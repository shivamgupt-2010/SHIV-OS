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
    val itemType: String = "APP", // "APP", "WIDGET", "FOLDER", "SHORTCUT", "DOCK"
    val packageName: String = "",
    val activityName: String = "",
    val customLabel: String? = null,
    val folderId: String? = null,
    val appWidgetId: Int = -1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
