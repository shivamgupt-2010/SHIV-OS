package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.studio.StudioThemeTokens
import com.example.data.local.entity.LauncherThemeEntity
import java.util.Calendar

data class ShivThemeTokens(
    val primary: Color = ShivaCyan,
    val onPrimary: Color = Color.White,
    val accent: Color = ShivaPurple,
    val onAccent: Color = Color.White,
    val surface: Color = CosmicSurface,
    val onSurface: Color = TextPrimary,
    val surfaceVariant: Color = CosmicSurfaceVariant,
    val onSurfaceVariant: Color = TextSecondary,
    val background: Color = CosmicBackground,
    val onBackground: Color = TextPrimary,
    val cardBorder: Color = CosmicCardBorder,
    val textPrimary: Color = TextPrimary,
    val textSecondary: Color = TextSecondary,
    val isDark: Boolean = true,
    val cardCornerRadiusDp: Int = 18,
    val iconScalePercent: Int = 100,
    val typographyStyle: String = "DEFAULT",
    val animationPreset: MotionPreset = MotionPreset.SMOOTH
)

val LocalShivThemeTokens = staticCompositionLocalOf { ShivThemeTokens() }

object ShivTheme {
    val colors: ShivThemeTokens
        @Composable
        get() = LocalShivThemeTokens.current

    val cardRadius: Dp
        @Composable
        get() = LocalShivThemeTokens.current.cardCornerRadiusDp.dp

    val iconScale: Float
        @Composable
        get() = LocalShivThemeTokens.current.iconScalePercent / 100f

    val isDark: Boolean
        @Composable
        get() = LocalShivThemeTokens.current.isDark
}

val CosmicColorScheme = darkColorScheme(
    primary = ShivaIndigo,
    onPrimary = TextPrimary,
    primaryContainer = CosmicSurfaceVariant,
    onPrimaryContainer = ShivaCyan,
    secondary = ShivaCyan,
    onSecondary = CosmicBackground,
    secondaryContainer = CosmicSurfaceVariant,
    onSecondaryContainer = TextPrimary,
    tertiary = ShivaPurple,
    onTertiary = TextPrimary,
    background = CosmicBackground,
    onBackground = TextPrimary,
    surface = CosmicSurface,
    onSurface = TextPrimary,
    surfaceVariant = CosmicSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CosmicCardBorder
)

val SolarLightColorScheme = lightColorScheme(
    primary = ShivaIndigo,
    onPrimary = TextPrimary,
    primaryContainer = SolarSurfaceVariant,
    onPrimaryContainer = ShivaBlue,
    secondary = ShivaCyan,
    onSecondary = SolarSurface,
    secondaryContainer = SolarSurfaceVariant,
    onSecondaryContainer = SolarTextPrimary,
    tertiary = ShivaPurple,
    onTertiary = TextPrimary,
    background = SolarBackground,
    onBackground = SolarTextPrimary,
    surface = SolarSurface,
    onSurface = SolarTextPrimary,
    surfaceVariant = SolarSurfaceVariant,
    onSurfaceVariant = SolarTextSecondary,
    outline = SolarCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    theme: LauncherThemeEntity? = null,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val systemInDark = isSystemInDarkTheme()

    // Determine effective darkness based on active theme
    val effectiveIsDark = when {
        theme?.adaptiveMode == "SYSTEM" -> systemInDark
        theme?.adaptiveMode == "SCHEDULE" -> {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            hour !in 6..18
        }
        theme != null -> theme.isDark
        else -> darkTheme
    }

    // Dynamic colors from theme entity or presets
    val primaryColor = if (theme != null) StudioThemeTokens.hexToColor(theme.primaryColorHex, ShivaCyan) else if (effectiveIsDark) ShivaCyan else ShivaIndigo
    val accentColor = if (theme != null) StudioThemeTokens.hexToColor(theme.accentColorHex, ShivaPurple) else ShivaPurple
    val surfaceColor = if (theme != null) StudioThemeTokens.hexToColor(theme.surfaceColorHex, if (effectiveIsDark) CosmicSurface else SolarSurface) else if (effectiveIsDark) CosmicSurface else SolarSurface
    val backgroundColor = if (theme != null) StudioThemeTokens.hexToColor(theme.backgroundColorHex, if (effectiveIsDark) CosmicBackground else SolarBackground) else if (effectiveIsDark) CosmicBackground else SolarBackground
    val surfaceVariantColor = if (theme != null) StudioThemeTokens.hexToColor(theme.surfaceVariantColorHex, if (effectiveIsDark) CosmicSurfaceVariant else SolarSurfaceVariant) else if (effectiveIsDark) CosmicSurfaceVariant else SolarSurfaceVariant
    val cardBorderColor = if (theme != null) StudioThemeTokens.hexToColor(theme.cardBorderColorHex, if (effectiveIsDark) CosmicCardBorder else SolarCardBorder) else if (effectiveIsDark) CosmicCardBorder else SolarCardBorder
    val textPrimaryColor = if (theme != null) StudioThemeTokens.hexToColor(theme.textPrimaryColorHex, if (effectiveIsDark) TextPrimary else SolarTextPrimary) else if (effectiveIsDark) TextPrimary else SolarTextPrimary
    val textSecondaryColor = if (theme != null) StudioThemeTokens.hexToColor(theme.textSecondaryColorHex, if (effectiveIsDark) TextSecondary else SolarTextSecondary) else if (effectiveIsDark) TextSecondary else SolarTextSecondary

    val useMonet = (dynamicColor || theme?.adaptiveMode == "WALLPAPER") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
        useMonet -> {
            if (effectiveIsDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        theme != null -> {
            if (effectiveIsDark) {
                darkColorScheme(
                    primary = primaryColor,
                    onPrimary = Color.White,
                    primaryContainer = surfaceVariantColor,
                    onPrimaryContainer = primaryColor,
                    secondary = accentColor,
                    onSecondary = Color.White,
                    secondaryContainer = surfaceVariantColor,
                    onSecondaryContainer = accentColor,
                    background = backgroundColor,
                    onBackground = textPrimaryColor,
                    surface = surfaceColor,
                    onSurface = textPrimaryColor,
                    surfaceVariant = surfaceVariantColor,
                    onSurfaceVariant = textSecondaryColor,
                    outline = cardBorderColor
                )
            } else {
                lightColorScheme(
                    primary = primaryColor,
                    onPrimary = Color.White,
                    primaryContainer = surfaceVariantColor,
                    onPrimaryContainer = primaryColor,
                    secondary = accentColor,
                    onSecondary = Color.White,
                    secondaryContainer = surfaceVariantColor,
                    onSecondaryContainer = accentColor,
                    background = backgroundColor,
                    onBackground = textPrimaryColor,
                    surface = surfaceColor,
                    onSurface = textPrimaryColor,
                    surfaceVariant = surfaceVariantColor,
                    onSurfaceVariant = textSecondaryColor,
                    outline = cardBorderColor
                )
            }
        }
        effectiveIsDark -> CosmicColorScheme
        else -> SolarLightColorScheme
    }

    val tokens = ShivThemeTokens(
        primary = primaryColor,
        onPrimary = if (effectiveIsDark) Color.White else Color.Black,
        accent = accentColor,
        onAccent = Color.White,
        surface = surfaceColor,
        onSurface = textPrimaryColor,
        surfaceVariant = surfaceVariantColor,
        onSurfaceVariant = textSecondaryColor,
        background = backgroundColor,
        onBackground = textPrimaryColor,
        cardBorder = cardBorderColor,
        textPrimary = textPrimaryColor,
        textSecondary = textSecondaryColor,
        isDark = effectiveIsDark,
        cardCornerRadiusDp = theme?.cardCornerRadiusDp ?: 18,
        iconScalePercent = theme?.iconScalePercent ?: 100,
        typographyStyle = theme?.typographyStyle ?: "DEFAULT",
        animationPreset = MotionPreset.fromName(theme?.animationPreset)
    )

    CompositionLocalProvider(LocalShivThemeTokens provides tokens) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }
}
