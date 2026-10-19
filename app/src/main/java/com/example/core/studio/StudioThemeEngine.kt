package com.example.core.studio

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.example.data.local.dao.LauncherThemeDao
import com.example.data.local.entity.LauncherThemeEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject

data class ThemePreset(
    val key: String,
    val name: String,
    val primary: Color,
    val accent: Color,
    val surface: Color,
    val background: Color
)

object StudioThemeTokens {
    val PRESETS = mapOf(
        "COSMIC_SHIVA" to ThemePreset(
            key = "COSMIC_SHIVA",
            name = "Cosmic Shiva",
            primary = Color(0xFF00F5FF),
            accent = Color(0xFF8A2BE2),
            surface = Color(0xFF0E1019),
            background = Color(0xFF07080E)
        ),
        "CYAN_NEON" to ThemePreset(
            key = "CYAN_NEON",
            name = "Cyan Neon",
            primary = Color(0xFF00FFFF),
            accent = Color(0xFF0077FF),
            surface = Color(0xFF051015),
            background = Color(0xFF03090D)
        ),
        "ROYAL_PURPLE" to ThemePreset(
            key = "ROYAL_PURPLE",
            name = "Royal Mystic",
            primary = Color(0xFFDA70D6),
            accent = Color(0xFF800080),
            surface = Color(0xFF14081E),
            background = Color(0xFF0A0410)
        ),
        "SOLAR_GOLD" to ThemePreset(
            key = "SOLAR_GOLD",
            name = "Surya Solar",
            primary = Color(0xFFFFD700),
            accent = Color(0xFFFF8C00),
            surface = Color(0xFF181206),
            background = Color(0xFF0E0A03)
        ),
        "OLED_BLACK" to ThemePreset(
            key = "OLED_BLACK",
            name = "Pure OLED Minimal",
            primary = Color(0xFFFFFFFF),
            accent = Color(0xFF888888),
            surface = Color(0xFF111111),
            background = Color(0xFF000000)
        )
    )
}

class StudioThemeEngine(
    private val themeDao: LauncherThemeDao,
    private val scope: CoroutineScope
) {
    val currentTheme: StateFlow<LauncherThemeEntity> = themeDao.getThemeFlow()
        .map { it ?: LauncherThemeEntity() }
        .stateIn(scope, SharingStarted.Eagerly, LauncherThemeEntity())

    fun applyPreset(presetKey: String) {
        val preset = StudioThemeTokens.PRESETS[presetKey] ?: return
        scope.launch(Dispatchers.IO) {
            val current = currentTheme.value
            val updated = current.copy(
                themePreset = presetKey,
                primaryColorHex = String.format("#%06X", (0xFFFFFF and preset.primary.value.toInt())),
                accentColorHex = String.format("#%06X", (0xFFFFFF and preset.accent.value.toInt())),
                surfaceColorHex = String.format("#%06X", (0xFFFFFF and preset.surface.value.toInt())),
                updatedAt = System.currentTimeMillis()
            )
            themeDao.saveTheme(updated)
            com.example.core.launcher.AppIconManager.clearCache()
        }
    }

    fun setIconShape(shape: String) {
        scope.launch(Dispatchers.IO) {
            val current = currentTheme.value
            themeDao.saveTheme(current.copy(iconShape = shape, updatedAt = System.currentTimeMillis()))
            com.example.core.launcher.AppIconManager.clearCache()
        }
    }

    fun setIconScale(scalePercent: Int) {
        val clamped = scalePercent.coerceIn(80, 120)
        scope.launch(Dispatchers.IO) {
            val current = currentTheme.value
            themeDao.saveTheme(current.copy(iconScalePercent = clamped, updatedAt = System.currentTimeMillis()))
        }
    }

    fun setGridDimensions(rows: Int, columns: Int) {
        val safeRows = rows.coerceIn(4, 7)
        val safeCols = columns.coerceIn(4, 6)
        scope.launch(Dispatchers.IO) {
            val current = currentTheme.value
            themeDao.saveTheme(current.copy(gridRows = safeRows, gridColumns = safeCols, updatedAt = System.currentTimeMillis()))
        }
    }

    fun setDockCount(count: Int) {
        val safeCount = count.coerceIn(3, 6)
        scope.launch(Dispatchers.IO) {
            val current = currentTheme.value
            themeDao.saveTheme(current.copy(dockCount = safeCount, updatedAt = System.currentTimeMillis()))
        }
    }

    fun setReducedMotion(enabled: Boolean) {
        scope.launch(Dispatchers.IO) {
            val current = currentTheme.value
            themeDao.saveTheme(current.copy(isReducedMotion = enabled, updatedAt = System.currentTimeMillis()))
        }
    }

    fun setLayoutLocked(locked: Boolean) {
        scope.launch(Dispatchers.IO) {
            themeDao.setLayoutLocked(locked)
        }
    }


    // Export theme configuration as validated JSON string
    fun exportThemeJson(): String {
        val t = currentTheme.value
        val json = JSONObject()
        json.put("version", 1)
        json.put("themePreset", t.themePreset)
        json.put("primaryColorHex", t.primaryColorHex)
        json.put("accentColorHex", t.accentColorHex)
        json.put("surfaceColorHex", t.surfaceColorHex)
        json.put("iconShape", t.iconShape)
        json.put("iconScalePercent", t.iconScalePercent)
        json.put("gridRows", t.gridRows)
        json.put("gridColumns", t.gridColumns)
        json.put("dockCount", t.dockCount)
        json.put("isReducedMotion", t.isReducedMotion)
        return json.toString(2)
    }

    // Import and validate theme configuration with strict bounds checking
    fun importThemeJson(jsonStr: String): Boolean {
        return try {
            val json = JSONObject(jsonStr)
            val preset = json.optString("themePreset", "COSMIC_SHIVA")
            val iconShape = json.optString("iconShape", "SQUIRCLE")
            val scale = json.optInt("iconScalePercent", 100).coerceIn(80, 120)
            val rows = json.optInt("gridRows", 5).coerceIn(4, 7)
            val cols = json.optInt("gridColumns", 4).coerceIn(4, 6)
            val dock = json.optInt("dockCount", 5).coerceIn(3, 6)

            scope.launch(Dispatchers.IO) {
                val current = currentTheme.value
                val updated = current.copy(
                    themePreset = preset,
                    iconShape = iconShape,
                    iconScalePercent = scale,
                    gridRows = rows,
                    gridColumns = cols,
                    dockCount = dock,
                    isReducedMotion = json.optBoolean("isReducedMotion", false),
                    updatedAt = System.currentTimeMillis()
                )
                themeDao.saveTheme(updated)
                com.example.core.launcher.AppIconManager.clearCache()
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
