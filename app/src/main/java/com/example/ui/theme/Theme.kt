package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val CosmicColorScheme = darkColorScheme(
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

private val SolarLightColorScheme = lightColorScheme(
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
    darkTheme: Boolean = true, // Default to stunning cosmic dark mode
    dynamicColor: Boolean = false, // Preserve bespoke cosmic styling
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CosmicColorScheme
        else -> SolarLightColorScheme
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
