package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "launcher_themes")
data class LauncherThemeEntity(
    @PrimaryKey
    val id: String = "current_theme",
    val themePreset: String = "COSMIC_SHIVA",
    val themeCategory: String = "EXPRESSIVE", // "LIGHT", "DARK", "EXPRESSIVE", "ADAPTIVE", "CUSTOM"
    val isCustomTheme: Boolean = false,
    val customThemeName: String = "Cosmic Shiva",
    val primaryColorHex: String = "#00F5FF",
    val accentColorHex: String = "#8A2BE2",
    val surfaceColorHex: String = "#0E1019",
    val backgroundColorHex: String = "#07080E",
    val surfaceVariantColorHex: String = "#1A2130",
    val cardBorderColorHex: String = "#252E42",
    val textPrimaryColorHex: String = "#F8FAFC",
    val textSecondaryColorHex: String = "#94A3B8",
    val isDark: Boolean = true,
    val cardCornerRadiusDp: Int = 18, // 0 - 28
    val typographyStyle: String = "DEFAULT", // "DEFAULT", "MODERN", "SERIF", "MONO"
    val animationPreset: String = "SMOOTH", // "MINIMAL", "SMOOTH", "EXPRESSIVE"
    val adaptiveMode: String = "NONE", // "NONE", "SYSTEM", "WALLPAPER", "SCHEDULE"
    val iconShape: String = "SQUIRCLE", // "CIRCLE", "SQUIRCLE", "ROUNDED_RECT", "TEARDROP"
    val iconScalePercent: Int = 100, // 80 - 120
    val gridRows: Int = 5,
    val gridColumns: Int = 4,
    val dockCount: Int = 5,
    val isLayoutLocked: Boolean = false,
    val isReducedMotion: Boolean = false,
    val wallpaperPreset: String = "DEEP_COSMOS",
    val activeIconPack: String = "PACK_NEBULA",
    val layoutParadigm: String = "GRID", // "GRID", "TASKBAR_WINDOWING", "TABBED_SIDEBAR", "TEXT_ONLY_MINIMAL", "SEARCH_GATED", "GLASSMORPHIC_BLUR", "MAGAZINE_EDITORIAL", "TERMINAL_HACKER", "HOLOGRAPHIC_3D"
    val updatedAt: Long = System.currentTimeMillis()
)
