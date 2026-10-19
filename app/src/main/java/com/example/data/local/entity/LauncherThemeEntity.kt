package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "launcher_themes")
data class LauncherThemeEntity(
    @PrimaryKey
    val id: String = "current_theme",
    val themePreset: String = "COSMIC_SHIVA", // "COSMIC_SHIVA", "CYAN_NEON", "ROYAL_PURPLE", "SOLAR_GOLD", "OLED_BLACK"
    val primaryColorHex: String = "#00F5FF",
    val accentColorHex: String = "#8A2BE2",
    val surfaceColorHex: String = "#0A0B10",
    val iconShape: String = "SQUIRCLE", // "CIRCLE", "SQUIRCLE", "ROUNDED_RECT", "TEARDROP"
    val iconScalePercent: Int = 100, // 80 - 120
    val gridRows: Int = 5,
    val gridColumns: Int = 4,
    val dockCount: Int = 5,
    val isLayoutLocked: Boolean = false,
    val isReducedMotion: Boolean = false,
    val wallpaperPreset: String = "DEEP_COSMOS",
    val updatedAt: Long = System.currentTimeMillis()
)
