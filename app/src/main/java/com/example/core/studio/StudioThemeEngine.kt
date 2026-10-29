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
import java.util.UUID

data class ThemePreset(
    val key: String,
    val name: String,
    val category: String, // "LIGHT", "DARK", "EXPRESSIVE", "ADAPTIVE"
    val primary: Color,
    val accent: Color,
    val surface: Color,
    val background: Color,
    val surfaceVariant: Color = surface,
    val cardBorder: Color = accent.copy(alpha = 0.3f),
    val textPrimary: Color = if (surface.red * 0.299 + surface.green * 0.587 + surface.blue * 0.114 > 0.5) Color(0xFF0F172A) else Color(0xFFF8FAFC),
    val textSecondary: Color = if (surface.red * 0.299 + surface.green * 0.587 + surface.blue * 0.114 > 0.5) Color(0xFF64748B) else Color(0xFF94A3B8),
    val isDark: Boolean = true
)

data class SignatureIconPack(
    val key: String,
    val name: String,
    val description: String,
    val paletteDescription: String,
    val sampleColors: List<Color>,
    val badge: String = "SIGNATURE"
)

object StudioIconPackTokens {
    val PACKS: List<SignatureIconPack> = listOf(
        SignatureIconPack(
            key = "PACK_NEBULA",
            name = "Nebula Icons",
            description = "Neon line art, glowing edges, cyber aesthetics",
            paletteDescription = "Violet, Cyan, Deep Obsidian",
            sampleColors = listOf(Color(0xFF55E7FF), Color(0xFF7657FF), Color(0xFF090D1F))
        ),
        SignatureIconPack(
            key = "PACK_MONOFORM",
            name = "Monoform",
            description = "Minimal monochrome line art, soft geometric shapes, clean silhouettes",
            paletteDescription = "Matte Black, Crisp White, Charcoal",
            sampleColors = listOf(Color(0xFFFFFFFF), Color(0xFF18181B), Color(0xFF71717A))
        ),
        SignatureIconPack(
            key = "PACK_POPFORM",
            name = "Popform",
            description = "Bold geometric Bauhaus color-blocked icons",
            paletteDescription = "Coral, Cobalt, Lemon, Mint",
            sampleColors = listOf(Color(0xFFFF725E), Color(0xFF4169FF), Color(0xFFFFE66D), Color(0xFF34D399))
        ),
        SignatureIconPack(
            key = "PACK_GLASSCORE",
            name = "Glasscore",
            description = "Translucent frosted glass depth, refractive specular bevels",
            paletteDescription = "Frosted Graphite, Silver, Amber Specular",
            sampleColors = listOf(Color(0xFFE2E8F0), Color(0xFF353A40), Color(0xFFFFB547))
        ),
        SignatureIconPack(
            key = "PACK_ATELIER",
            name = "Atelier",
            description = "Refined editorial crests, bespoke luxury filigree",
            paletteDescription = "Champagne Gold, Forest Green, Burgundy",
            sampleColors = listOf(Color(0xFFD8BD80), Color(0xFF174C3C), Color(0xFF54121E))
        ),
        SignatureIconPack(
            key = "PACK_SOFTCRAFT",
            name = "Softcraft",
            description = "Tactile rounded 3D claymorphic puffy marshmallow icons",
            paletteDescription = "Sky Blue, Peach, Cream, Lilac",
            sampleColors = listOf(Color(0xFF93C5FD), Color(0xFFFDBA74), Color(0xFFDDD6FE))
        )
    )
}

object StudioThemeTokens {
    val PRESETS: Map<String, ThemePreset> = linkedMapOf(
        // --- 6 MARQUEE SIGNATURE THEMES ---
        "NEBULA" to ThemePreset(
            key = "NEBULA",
            name = "Nebula",
            category = "SIGNATURE",
            primary = Color(0xFF7657FF),
            accent = Color(0xFF55E7FF),
            surface = Color(0xFF12172F),
            background = Color(0xFF090D1F),
            surfaceVariant = Color(0xFF1A203F),
            cardBorder = Color(0x6655E7FF),
            textPrimary = Color(0xFFF0F3FF),
            textSecondary = Color(0xFF8E9BB8),
            isDark = true
        ),
        "AURA" to ThemePreset(
            key = "AURA",
            name = "Aura",
            category = "SIGNATURE",
            primary = Color(0xFF879879),
            accent = Color(0xFF4E6048),
            surface = Color(0xFFECE6D8),
            background = Color(0xFFF5F1E8),
            surfaceVariant = Color(0xFFDFD7C4),
            cardBorder = Color(0x4D879879),
            textPrimary = Color(0xFF272B26),
            textSecondary = Color(0xFF5B6458),
            isDark = false
        ),
        "PRISM" to ThemePreset(
            key = "PRISM",
            name = "Prism",
            category = "SIGNATURE",
            primary = Color(0xFF4169FF),
            accent = Color(0xFFFF725E),
            surface = Color(0xFFF7F8FC),
            background = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFEDF0FF),
            cardBorder = Color(0x4D4169FF),
            textPrimary = Color(0xFF1A1D2E),
            textSecondary = Color(0xFF686D87),
            isDark = false
        ),
        "FORGE" to ThemePreset(
            key = "FORGE",
            name = "Forge",
            category = "SIGNATURE",
            primary = Color(0xFFFFB547),
            accent = Color(0xFFFF9800),
            surface = Color(0xFF23272C),
            background = Color(0xFF17191C),
            surfaceVariant = Color(0xFF353A40),
            cardBorder = Color(0x66FFB547),
            textPrimary = Color(0xFFF2F0EA),
            textSecondary = Color(0xFF9FA6B2),
            isDark = true
        ),
        "NOIR_LUXE" to ThemePreset(
            key = "NOIR_LUXE",
            name = "Noir Luxe",
            category = "SIGNATURE",
            primary = Color(0xFFD8BD80),
            accent = Color(0xFF174C3C),
            surface = Color(0xFF1F1F1F),
            background = Color(0xFF151515),
            surfaceVariant = Color(0xFF2A2A2A),
            cardBorder = Color(0x66D8BD80),
            textPrimary = Color(0xFFF5F0E5),
            textSecondary = Color(0xFFA69F91),
            isDark = true
        ),
        "DAYBREAK" to ThemePreset(
            key = "DAYBREAK",
            name = "Daybreak",
            category = "SIGNATURE",
            primary = Color(0xFF3478C8),
            accent = Color(0xFF6DB8FF),
            surface = Color(0xFFEDF4FB),
            background = Color(0xFFF8FBFF),
            surfaceVariant = Color(0xFFDFECF8),
            cardBorder = Color(0x666DB8FF),
            textPrimary = Color(0xFF26384A),
            textSecondary = Color(0xFF5D738A),
            isDark = false
        ),

        // --- CATEGORY A: LIGHT THEMES (8) ---
        "PURE_LIGHT" to ThemePreset(
            key = "PURE_LIGHT",
            name = "Pure Light",
            category = "LIGHT",
            primary = Color(0xFF0284C7),
            accent = Color(0xFF0369A1),
            surface = Color(0xFFF8FAFC),
            background = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFF1F5F9),
            cardBorder = Color(0xFFE2E8F0),
            textPrimary = Color(0xFF0F172A),
            textSecondary = Color(0xFF64748B),
            isDark = false
        ),
        "SOFT_LIGHT" to ThemePreset(
            key = "SOFT_LIGHT",
            name = "Soft Light",
            category = "LIGHT",
            primary = Color(0xFFD97706),
            accent = Color(0xFFB45309),
            surface = Color(0xFFF5F2EB),
            background = Color(0xFFFDFBF7),
            surfaceVariant = Color(0xFFEBE6DC),
            cardBorder = Color(0xFFE0D8C8),
            textPrimary = Color(0xFF1C1917),
            textSecondary = Color(0xFF78716C),
            isDark = false
        ),
        "MATERIAL_LIGHT" to ThemePreset(
            key = "MATERIAL_LIGHT",
            name = "Material Light",
            category = "LIGHT",
            primary = Color(0xFF1D4ED8),
            accent = Color(0xFF7C3AED),
            surface = Color(0xFFF1F5F9),
            background = Color(0xFFF8FAFC),
            surfaceVariant = Color(0xFFE2E8F0),
            cardBorder = Color(0xFFCBD5E1),
            textPrimary = Color(0xFF0F172A),
            textSecondary = Color(0xFF475569),
            isDark = false
        ),
        "MINIMAL_STUDIO" to ThemePreset(
            key = "MINIMAL_STUDIO",
            name = "Minimal Studio",
            category = "LIGHT",
            primary = Color(0xFF18181B),
            accent = Color(0xFF52525B),
            surface = Color(0xFFF4F4F5),
            background = Color(0xFFFAFAFA),
            surfaceVariant = Color(0xFFE4E4E7),
            cardBorder = Color(0xFFD4D4D8),
            textPrimary = Color(0xFF18181B),
            textSecondary = Color(0xFF71717A),
            isDark = false
        ),
        "AURORA_LIGHT" to ThemePreset(
            key = "AURORA_LIGHT",
            name = "Aurora Light",
            category = "LIGHT",
            primary = Color(0xFF0EA5E9),
            accent = Color(0xFF8B5CF6),
            surface = Color(0xFFF0FDF4),
            background = Color(0xFFF8FAFC),
            surfaceVariant = Color(0xFFE0F2FE),
            cardBorder = Color(0xFFBAE6FD),
            textPrimary = Color(0xFF0F172A),
            textSecondary = Color(0xFF0369A1),
            isDark = false
        ),
        "NATURE_LIGHT" to ThemePreset(
            key = "NATURE_LIGHT",
            name = "Nature Light",
            category = "LIGHT",
            primary = Color(0xFF15803D),
            accent = Color(0xFF854D0E),
            surface = Color(0xFFF0FDF4),
            background = Color(0xFFF7FEE7),
            surfaceVariant = Color(0xFFDCFCE7),
            cardBorder = Color(0xFFBBF7D0),
            textPrimary = Color(0xFF14532D),
            textSecondary = Color(0xFF166534),
            isDark = false
        ),
        "PASTEL_LIGHT" to ThemePreset(
            key = "PASTEL_LIGHT",
            name = "Pastel Light",
            category = "LIGHT",
            primary = Color(0xFF8B5CF6),
            accent = Color(0xFFEC4899),
            surface = Color(0xFFFAF5FF),
            background = Color(0xFFFFF1F2),
            surfaceVariant = Color(0xFFF3E8FF),
            cardBorder = Color(0xFFE9D5FF),
            textPrimary = Color(0xFF3B0764),
            textSecondary = Color(0xFF7E22CE),
            isDark = false
        ),
        "PAPER" to ThemePreset(
            key = "PAPER",
            name = "Paper",
            category = "LIGHT",
            primary = Color(0xFF78350F),
            accent = Color(0xFFB45309),
            surface = Color(0xFFF5EBE1),
            background = Color(0xFFFAF3EB),
            surfaceVariant = Color(0xFFEADBCC),
            cardBorder = Color(0xFFDAC7B2),
            textPrimary = Color(0xFF292524),
            textSecondary = Color(0xFF78716C),
            isDark = false
        ),

        // --- CATEGORY B: DARK THEMES (7) ---
        "PURE_DARK" to ThemePreset(
            key = "PURE_DARK",
            name = "Pure Dark",
            category = "DARK",
            primary = Color(0xFF38BDF8),
            accent = Color(0xFF818CF8),
            surface = Color(0xFF121212),
            background = Color(0xFF000000),
            surfaceVariant = Color(0xFF1E1E1E),
            cardBorder = Color(0xFF2C2C2C),
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFF94A3B8),
            isDark = true
        ),
        "MIDNIGHT" to ThemePreset(
            key = "MIDNIGHT",
            name = "Midnight",
            category = "DARK",
            primary = Color(0xFF38BDF8),
            accent = Color(0xFF818CF8),
            surface = Color(0xFF0C1222),
            background = Color(0xFF050814),
            surfaceVariant = Color(0xFF141D36),
            cardBorder = Color(0xFF1E2B4D),
            textPrimary = Color(0xFFF1F5F9),
            textSecondary = Color(0xFF94A3B8),
            isDark = true
        ),
        "AURORA_DARK" to ThemePreset(
            key = "AURORA_DARK",
            name = "Aurora Dark",
            category = "DARK",
            primary = Color(0xFF34D399),
            accent = Color(0xFFA78BFA),
            surface = Color(0xFF0B132B),
            background = Color(0xFF050814),
            surfaceVariant = Color(0xFF132247),
            cardBorder = Color(0xFF1E356D),
            textPrimary = Color(0xFFF1F5F9),
            textSecondary = Color(0xFF94A3B8),
            isDark = true
        ),
        "GRAPHITE" to ThemePreset(
            key = "GRAPHITE",
            name = "Graphite",
            category = "DARK",
            primary = Color(0xFFA1A1AA),
            accent = Color(0xFFE4E4E7),
            surface = Color(0xFF27272A),
            background = Color(0xFF09090B),
            surfaceVariant = Color(0xFF3F3F46),
            cardBorder = Color(0xFF52525B),
            textPrimary = Color(0xFFFAFAFA),
            textSecondary = Color(0xFFA1A1AA),
            isDark = true
        ),
        "AMOLED" to ThemePreset(
            key = "AMOLED",
            name = "AMOLED High-Contrast",
            category = "DARK",
            primary = Color(0xFF00F5FF),
            accent = Color(0xFF00FFA3),
            surface = Color(0xFF0A0A0A),
            background = Color(0xFF000000),
            surfaceVariant = Color(0xFF141414),
            cardBorder = Color(0xFF222222),
            textPrimary = Color(0xFFFFFFFF),
            textSecondary = Color(0xFF888888),
            isDark = true
        ),
        "FOREST_DARK" to ThemePreset(
            key = "FOREST_DARK",
            name = "Forest Dark",
            category = "DARK",
            primary = Color(0xFF10B981),
            accent = Color(0xFF34D399),
            surface = Color(0xFF062C1E),
            background = Color(0xFF02150E),
            surfaceVariant = Color(0xFF0B422F),
            cardBorder = Color(0xFF135C42),
            textPrimary = Color(0xFFECFDF5),
            textSecondary = Color(0xFF6EE7B7),
            isDark = true
        ),
        "STUDIO_DARK" to ThemePreset(
            key = "STUDIO_DARK",
            name = "Studio Dark",
            category = "DARK",
            primary = Color(0xFF6366F1),
            accent = Color(0xFFA855F7),
            surface = Color(0xFF18181B),
            background = Color(0xFF0E0E11),
            surfaceVariant = Color(0xFF27272A),
            cardBorder = Color(0xFF3F3F46),
            textPrimary = Color(0xFFF4F4F5),
            textSecondary = Color(0xFFA1A1AA),
            isDark = true
        ),

        // --- CATEGORY C: EXPRESSIVE THEMES (10) ---
        "OCEAN" to ThemePreset(
            key = "OCEAN",
            name = "Ocean",
            category = "EXPRESSIVE",
            primary = Color(0xFF06B6D4),
            accent = Color(0xFF3B82F6),
            surface = Color(0xFF082F49),
            background = Color(0xFF031726),
            surfaceVariant = Color(0xFF0C4A6E),
            cardBorder = Color(0xFF0369A1),
            textPrimary = Color(0xFFF0FDFA),
            textSecondary = Color(0xFF7DD3FC),
            isDark = true
        ),
        "SUNSET" to ThemePreset(
            key = "SUNSET",
            name = "Sunset",
            category = "EXPRESSIVE",
            primary = Color(0xFFF97316),
            accent = Color(0xFFEC4899),
            surface = Color(0xFF2E1020),
            background = Color(0xFF180812),
            surfaceVariant = Color(0xFF4A1532),
            cardBorder = Color(0xFF701A46),
            textPrimary = Color(0xFFFFF1F2),
            textSecondary = Color(0xFFF472B6),
            isDark = true
        ),
        "COSMIC_SHIVA" to ThemePreset(
            key = "COSMIC_SHIVA",
            name = "Cosmic Shiva",
            category = "EXPRESSIVE",
            primary = Color(0xFF00F5FF),
            accent = Color(0xFF8A2BE2),
            surface = Color(0xFF0E1019),
            background = Color(0xFF07080E),
            surfaceVariant = Color(0xFF1A2130),
            cardBorder = Color(0xFF252E42),
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFF94A3B8),
            isDark = true
        ),
        "CYAN_NEON" to ThemePreset(
            key = "CYAN_NEON",
            name = "Cyan Neon",
            category = "EXPRESSIVE",
            primary = Color(0xFF00FFFF),
            accent = Color(0xFF0077FF),
            surface = Color(0xFF051015),
            background = Color(0xFF03090D),
            surfaceVariant = Color(0xFF0C222E),
            cardBorder = Color(0xFF13384D),
            textPrimary = Color(0xFFF0FDFA),
            textSecondary = Color(0xFF67E8F9),
            isDark = true
        ),
        "GLASS" to ThemePreset(
            key = "GLASS",
            name = "Glass-Inspired",
            category = "EXPRESSIVE",
            primary = Color(0xFF38BDF8),
            accent = Color(0xFFF472B6),
            surface = Color(0xFF1E293B),
            background = Color(0xFF0F172A),
            surfaceVariant = Color(0xFF334155),
            cardBorder = Color(0xFF475569),
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFF94A3B8),
            isDark = true
        ),
        "MONOCHROME" to ThemePreset(
            key = "MONOCHROME",
            name = "Monochrome",
            category = "EXPRESSIVE",
            primary = Color(0xFFE2E8F0),
            accent = Color(0xFF94A3B8),
            surface = Color(0xFF1E293B),
            background = Color(0xFF020617),
            surfaceVariant = Color(0xFF334155),
            cardBorder = Color(0xFF475569),
            textPrimary = Color(0xFFFFFFFF),
            textSecondary = Color(0xFF94A3B8),
            isDark = true
        ),
        "COLORFUL_POP" to ThemePreset(
            key = "COLORFUL_POP",
            name = "Colorful Pop",
            category = "EXPRESSIVE",
            primary = Color(0xFFF43F5E),
            accent = Color(0xFFFBBF24),
            surface = Color(0xFF1E1B4B),
            background = Color(0xFF0F0C29),
            surfaceVariant = Color(0xFF312E81),
            cardBorder = Color(0xFF4338CA),
            textPrimary = Color(0xFFFFFFFF),
            textSecondary = Color(0xFFA5B4FC),
            isDark = true
        ),
        "GRADIENT_FLOW" to ThemePreset(
            key = "GRADIENT_FLOW",
            name = "Gradient Flow",
            category = "EXPRESSIVE",
            primary = Color(0xFF8B5CF6),
            accent = Color(0xFF06B6D4),
            surface = Color(0xFF1E1035),
            background = Color(0xFF0C0517),
            surfaceVariant = Color(0xFF2E1065),
            cardBorder = Color(0xFF4C1D95),
            textPrimary = Color(0xFFFDF4FF),
            textSecondary = Color(0xFFC084FC),
            isDark = true
        ),
        "MINIMAL_COLOR" to ThemePreset(
            key = "MINIMAL_COLOR",
            name = "Minimal Color",
            category = "EXPRESSIVE",
            primary = Color(0xFF6366F1),
            accent = Color(0xFF818CF8),
            surface = Color(0xFF1E293B),
            background = Color(0xFF0F172A),
            surfaceVariant = Color(0xFF334155),
            cardBorder = Color(0xFF475569),
            textPrimary = Color(0xFFF1F5F9),
            textSecondary = Color(0xFF94A3B8),
            isDark = true
        ),
        "CUSTOM_ACCENT" to ThemePreset(
            key = "CUSTOM_ACCENT",
            name = "Custom Accent",
            category = "EXPRESSIVE",
            primary = Color(0xFF00F5FF),
            accent = Color(0xFFA855F7),
            surface = Color(0xFF12141F),
            background = Color(0xFF080910),
            surfaceVariant = Color(0xFF1B1E2E),
            cardBorder = Color(0xFF2B3048),
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFF94A3B8),
            isDark = true
        ),

        // --- CORE LEGACY PRESETS (PRESERVED) ---
        "ROYAL_PURPLE" to ThemePreset(
            key = "ROYAL_PURPLE",
            name = "Royal Mystic",
            category = "EXPRESSIVE",
            primary = Color(0xFFDA70D6),
            accent = Color(0xFF800080),
            surface = Color(0xFF14081E),
            background = Color(0xFF0A0410),
            surfaceVariant = Color(0xFF220D33),
            cardBorder = Color(0xFF3B1758),
            textPrimary = Color(0xFFFAF5FF),
            textSecondary = Color(0xFFD8B4FE),
            isDark = true
        ),
        "SOLAR_GOLD" to ThemePreset(
            key = "SOLAR_GOLD",
            name = "Surya Solar",
            category = "EXPRESSIVE",
            primary = Color(0xFFFFD700),
            accent = Color(0xFFFF8C00),
            surface = Color(0xFF181206),
            background = Color(0xFF0E0A03),
            surfaceVariant = Color(0xFF2E220A),
            cardBorder = Color(0xFF47340E),
            textPrimary = Color(0xFFFEFCE8),
            textSecondary = Color(0xFFFDE047),
            isDark = true
        ),
        "OLED_BLACK" to ThemePreset(
            key = "OLED_BLACK",
            name = "Pure OLED Minimal",
            category = "DARK",
            primary = Color(0xFFFFFFFF),
            accent = Color(0xFF888888),
            surface = Color(0xFF111111),
            background = Color(0xFF000000),
            surfaceVariant = Color(0xFF1E1E1E),
            cardBorder = Color(0xFF2A2A2A),
            textPrimary = Color(0xFFFFFFFF),
            textSecondary = Color(0xFF888888),
            isDark = true
        ),

        // --- CATEGORY D: ADAPTIVE THEMES (3) ---
        "ADAPTIVE_SYSTEM" to ThemePreset(
            key = "ADAPTIVE_SYSTEM",
            name = "System Auto (Light/Dark)",
            category = "ADAPTIVE",
            primary = Color(0xFF38BDF8),
            accent = Color(0xFF6366F1),
            surface = Color(0xFF131722),
            background = Color(0xFF0A0D14),
            surfaceVariant = Color(0xFF1A2130),
            cardBorder = Color(0xFF252E42),
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFF94A3B8),
            isDark = true
        ),
        "ADAPTIVE_WALLPAPER" to ThemePreset(
            key = "ADAPTIVE_WALLPAPER",
            name = "Material You Dynamic",
            category = "ADAPTIVE",
            primary = Color(0xFF38BDF8),
            accent = Color(0xFFA855F7),
            surface = Color(0xFF131722),
            background = Color(0xFF0A0D14),
            surfaceVariant = Color(0xFF1A2130),
            cardBorder = Color(0xFF252E42),
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFF94A3B8),
            isDark = true
        ),
        "ADAPTIVE_SCHEDULE" to ThemePreset(
            key = "ADAPTIVE_SCHEDULE",
            name = "Day & Night Schedule",
            category = "ADAPTIVE",
            primary = Color(0xFFFFD700),
            accent = Color(0xFF38BDF8),
            surface = Color(0xFF181206),
            background = Color(0xFF0E0A03),
            surfaceVariant = Color(0xFF2E220A),
            cardBorder = Color(0xFF47340E),
            textPrimary = Color(0xFFFEFCE8),
            textSecondary = Color(0xFFFDE047),
            isDark = true
        )
    )

    fun colorToHex(color: Color): String {
        val argb = color.value.toLong()
        val r = ((argb shr 48) and 0xFF).toInt()
        val g = ((argb shr 40) and 0xFF).toInt()
        val b = ((argb shr 32) and 0xFF).toInt()
        return String.format("#%02X%02X%02X", r, g, b)
    }

    fun hexToColor(hex: String, fallback: Color = Color.White): Color {
        return try {
            val clean = hex.trim().removePrefix("#")
            val parsed = if (clean.length == 6) {
                (0xFF000000 or clean.toLong(16)).toInt()
            } else if (clean.length == 8) {
                clean.toLong(16).toInt()
            } else {
                return fallback
            }
            Color(parsed)
        } catch (e: Exception) {
            fallback
        }
    }
}

class StudioThemeEngine(
    private val themeDao: LauncherThemeDao,
    private val scope: CoroutineScope
) {
    init {
        scope.launch(Dispatchers.IO) {
            themeDao.getThemeFlow().filterNotNull().collect { t ->
                com.example.core.launcher.AppIconManager.setActiveIconPackKey(t.activeIconPack)
            }
        }
    }

    val currentTheme: StateFlow<LauncherThemeEntity> = themeDao.getThemeFlow()
        .map { it ?: LauncherThemeEntity() }
        .stateIn(scope, SharingStarted.Eagerly, LauncherThemeEntity())

    val customThemes: StateFlow<List<LauncherThemeEntity>> = themeDao.getCustomThemesFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun applyPreset(presetKey: String) {
        val preset = StudioThemeTokens.PRESETS[presetKey] ?: return
        scope.launch(Dispatchers.IO) {
            val current = currentTheme.value
            val updated = current.copy(
                themePreset = presetKey,
                themeCategory = preset.category,
                isCustomTheme = false,
                customThemeName = preset.name,
                primaryColorHex = StudioThemeTokens.colorToHex(preset.primary),
                accentColorHex = StudioThemeTokens.colorToHex(preset.accent),
                surfaceColorHex = StudioThemeTokens.colorToHex(preset.surface),
                backgroundColorHex = StudioThemeTokens.colorToHex(preset.background),
                surfaceVariantColorHex = StudioThemeTokens.colorToHex(preset.surfaceVariant),
                cardBorderColorHex = StudioThemeTokens.colorToHex(preset.cardBorder),
                textPrimaryColorHex = StudioThemeTokens.colorToHex(preset.textPrimary),
                textSecondaryColorHex = StudioThemeTokens.colorToHex(preset.textSecondary),
                isDark = preset.isDark,
                adaptiveMode = if (preset.category == "ADAPTIVE") {
                    when (presetKey) {
                        "ADAPTIVE_SYSTEM" -> "SYSTEM"
                        "ADAPTIVE_WALLPAPER" -> "WALLPAPER"
                        "ADAPTIVE_SCHEDULE" -> "SCHEDULE"
                        else -> "NONE"
                    }
                } else "NONE",
                updatedAt = System.currentTimeMillis()
            )
            themeDao.saveTheme(updated)
            com.example.core.launcher.AppIconManager.clearCache()
        }
    }

    fun applyCustomTheme(theme: LauncherThemeEntity) {
        scope.launch(Dispatchers.IO) {
            val current = currentTheme.value
            val updated = theme.copy(
                id = "current_theme",
                isCustomTheme = true,
                gridRows = current.gridRows,
                gridColumns = current.gridColumns,
                dockCount = current.dockCount,
                isLayoutLocked = current.isLayoutLocked,
                updatedAt = System.currentTimeMillis()
            )
            themeDao.saveTheme(updated)
            com.example.core.launcher.AppIconManager.clearCache()
        }
    }

    fun saveCustomTheme(
        name: String,
        primaryHex: String,
        accentHex: String,
        surfaceHex: String,
        bgHex: String,
        isDark: Boolean,
        radiusDp: Int,
        typography: String,
        animation: String
    ) {
        val cleanName = name.trim().ifEmpty { "Custom Theme" }
        val id = "custom_" + UUID.randomUUID().toString()
        val textPrimaryHex = if (isDark) "#F8FAFC" else "#0F172A"
        val textSecondaryHex = if (isDark) "#94A3B8" else "#64748B"
        val surfaceVariantHex = surfaceHex // will blend
        val borderHex = accentHex

        val newTheme = LauncherThemeEntity(
            id = id,
            themePreset = "CUSTOM_" + UUID.randomUUID().toString().take(6).uppercase(),
            themeCategory = "CUSTOM",
            isCustomTheme = true,
            customThemeName = cleanName,
            primaryColorHex = primaryHex,
            accentColorHex = accentHex,
            surfaceColorHex = surfaceHex,
            backgroundColorHex = bgHex,
            surfaceVariantColorHex = surfaceVariantHex,
            cardBorderColorHex = borderHex,
            textPrimaryColorHex = textPrimaryHex,
            textSecondaryColorHex = textSecondaryHex,
            isDark = isDark,
            cardCornerRadiusDp = radiusDp.coerceIn(0, 28),
            typographyStyle = typography,
            animationPreset = animation,
            updatedAt = System.currentTimeMillis()
        )

        scope.launch(Dispatchers.IO) {
            themeDao.saveTheme(newTheme)
            // Also apply it as active theme
            applyCustomTheme(newTheme)
        }
    }

    fun renameCustomTheme(id: String, newName: String) {
        scope.launch(Dispatchers.IO) {
            val list = customThemes.value
            val match = list.firstOrNull { it.id == id } ?: return@launch
            val updated = match.copy(customThemeName = newName.trim().ifEmpty { "Custom Theme" }, updatedAt = System.currentTimeMillis())
            themeDao.saveTheme(updated)
            if (currentTheme.value.id == id || (currentTheme.value.isCustomTheme && currentTheme.value.customThemeName == match.customThemeName)) {
                themeDao.saveTheme(currentTheme.value.copy(customThemeName = updated.customThemeName))
            }
        }
    }

    fun duplicateCustomTheme(id: String, newName: String) {
        scope.launch(Dispatchers.IO) {
            val list = customThemes.value
            val match = list.firstOrNull { it.id == id } ?: return@launch
            val newId = "custom_" + UUID.randomUUID().toString()
            val duplicated = match.copy(
                id = newId,
                customThemeName = newName.trim().ifEmpty { "${match.customThemeName} Copy" },
                updatedAt = System.currentTimeMillis()
            )
            themeDao.saveTheme(duplicated)
        }
    }

    fun deleteCustomTheme(id: String) {
        scope.launch(Dispatchers.IO) {
            themeDao.deleteTheme(id)
            if (currentTheme.value.id == id) {
                applyPreset("COSMIC_SHIVA")
            }
        }
    }

    fun setActiveIconPack(packKey: String) {
        scope.launch(Dispatchers.IO) {
            val current = currentTheme.value
            themeDao.saveTheme(current.copy(activeIconPack = packKey, updatedAt = System.currentTimeMillis()))
            com.example.core.launcher.AppIconManager.setActiveIconPackKey(packKey)
        }
    }

    fun resetToDefaults() {
        applyPreset("NEBULA")
        setActiveIconPack("PACK_NEBULA")
        setIconShape("SQUIRCLE")
        setIconScale(100)
        setGridDimensions(5, 4)
        setDockCount(5)
        setReducedMotion(false)
        setCardCornerRadius(18)
        setTypographyStyle("DEFAULT")
        setAnimationPreset("SMOOTH")
    }

    fun setCardCornerRadius(radiusDp: Int) {
        val clamped = radiusDp.coerceIn(0, 28)
        scope.launch(Dispatchers.IO) {
            val current = currentTheme.value
            themeDao.saveTheme(current.copy(cardCornerRadiusDp = clamped, updatedAt = System.currentTimeMillis()))
        }
    }

    fun setTypographyStyle(style: String) {
        scope.launch(Dispatchers.IO) {
            val current = currentTheme.value
            themeDao.saveTheme(current.copy(typographyStyle = style, updatedAt = System.currentTimeMillis()))
        }
    }

    fun setAnimationPreset(preset: String) {
        scope.launch(Dispatchers.IO) {
            val current = currentTheme.value
            themeDao.saveTheme(current.copy(animationPreset = preset, updatedAt = System.currentTimeMillis()))
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
        json.put("version", 2)
        json.put("themePreset", t.themePreset)
        json.put("themeCategory", t.themeCategory)
        json.put("isCustomTheme", t.isCustomTheme)
        json.put("customThemeName", t.customThemeName)
        json.put("primaryColorHex", t.primaryColorHex)
        json.put("accentColorHex", t.accentColorHex)
        json.put("surfaceColorHex", t.surfaceColorHex)
        json.put("backgroundColorHex", t.backgroundColorHex)
        json.put("surfaceVariantColorHex", t.surfaceVariantColorHex)
        json.put("cardBorderColorHex", t.cardBorderColorHex)
        json.put("textPrimaryColorHex", t.textPrimaryColorHex)
        json.put("textSecondaryColorHex", t.textSecondaryColorHex)
        json.put("isDark", t.isDark)
        json.put("cardCornerRadiusDp", t.cardCornerRadiusDp)
        json.put("typographyStyle", t.typographyStyle)
        json.put("animationPreset", t.animationPreset)
        json.put("adaptiveMode", t.adaptiveMode)
        json.put("iconShape", t.iconShape)
        json.put("iconScalePercent", t.iconScalePercent)
        json.put("gridRows", t.gridRows)
        json.put("gridColumns", t.gridColumns)
        json.put("dockCount", t.dockCount)
        json.put("isReducedMotion", t.isReducedMotion)
        json.put("activeIconPack", t.activeIconPack)
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
            val radius = json.optInt("cardCornerRadiusDp", 18).coerceIn(0, 28)
            val isDark = json.optBoolean("isDark", true)
            val typography = json.optString("typographyStyle", "DEFAULT")
            val animation = json.optString("animationPreset", "SMOOTH")
            val activeIconPack = json.optString("activeIconPack", "PACK_NEBULA")

            // Validate hex codes
            fun validateHex(hex: String, fallback: String): String {
                val clean = hex.trim().removePrefix("#")
                return if (clean.length == 6 && clean.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }) {
                    "#$clean"
                } else fallback
            }

            val primaryHex = validateHex(json.optString("primaryColorHex", "#00F5FF"), "#00F5FF")
            val accentHex = validateHex(json.optString("accentColorHex", "#8A2BE2"), "#8A2BE2")
            val surfaceHex = validateHex(json.optString("surfaceColorHex", "#0E1019"), "#0E1019")
            val bgHex = validateHex(json.optString("backgroundColorHex", "#07080E"), "#07080E")

            scope.launch(Dispatchers.IO) {
                val current = currentTheme.value
                val updated = current.copy(
                    themePreset = preset,
                    themeCategory = json.optString("themeCategory", if (isDark) "DARK" else "LIGHT"),
                    isCustomTheme = json.optBoolean("isCustomTheme", false),
                    customThemeName = json.optString("customThemeName", preset),
                    primaryColorHex = primaryHex,
                    accentColorHex = accentHex,
                    surfaceColorHex = surfaceHex,
                    backgroundColorHex = bgHex,
                    isDark = isDark,
                    cardCornerRadiusDp = radius,
                    typographyStyle = typography,
                    animationPreset = animation,
                    iconShape = iconShape,
                    iconScalePercent = scale,
                    gridRows = rows,
                    gridColumns = cols,
                    dockCount = dock,
                    activeIconPack = activeIconPack,
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
