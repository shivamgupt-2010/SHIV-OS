package com.example.ui.effects

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import java.util.Calendar

enum class SolarPeriod(val title: String) {
    DAWN("Sunrise & Dawn"),
    NOON("Clear Daylight"),
    SUNSET("Golden Hour & Sunset"),
    MIDNIGHT("Deep Obsidian Starlight")
}

data class AmbientLightingState(
    val period: SolarPeriod,
    val ambientWarmth: Color,
    val rimLightColor: Color,
    val specularAlpha: Float,
    val shadowDepthAlpha: Float
)

object DaylightEngine {
    fun getCurrentSolarPeriod(): SolarPeriod {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..7 -> SolarPeriod.DAWN
            in 8..16 -> SolarPeriod.NOON
            in 17..19 -> SolarPeriod.SUNSET
            else -> SolarPeriod.MIDNIGHT
        }
    }

    fun getAmbientLighting(period: SolarPeriod = getCurrentSolarPeriod()): AmbientLightingState {
        return when (period) {
            SolarPeriod.DAWN -> AmbientLightingState(
                period = period,
                ambientWarmth = Color(0x33FFB547), // Soft amber warmth
                rimLightColor = Color(0x66FF8A80), // Rose gold rim
                specularAlpha = 0.35f,
                shadowDepthAlpha = 0.45f
            )
            SolarPeriod.NOON -> AmbientLightingState(
                period = period,
                ambientWarmth = Color(0x1A6DB8FF), // Sky blue clarity
                rimLightColor = Color(0x66FFFFFF), // Pure white specular
                specularAlpha = 0.50f,
                shadowDepthAlpha = 0.30f
            )
            SolarPeriod.SUNSET -> AmbientLightingState(
                period = period,
                ambientWarmth = Color(0x44FF725E), // Burnt coral sunset
                rimLightColor = Color(0x66C7B5FF), // Twilight lilac rim
                specularAlpha = 0.40f,
                shadowDepthAlpha = 0.55f
            )
            SolarPeriod.MIDNIGHT -> AmbientLightingState(
                period = period,
                ambientWarmth = Color(0x2600F5FF), // Cyber electric cyan
                rimLightColor = Color(0x557657FF), // Deep ultraviolet rim
                specularAlpha = 0.25f,
                shadowDepthAlpha = 0.70f
            )
        }
    }

    @Composable
    fun DaylightAtmosphereOverlay(modifier: Modifier = Modifier) {
        val lighting = remember { getAmbientLighting() }
        Canvas(modifier = modifier.fillMaxSize()) {
            // Ambient daylight glow emanating from top edge
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lighting.ambientWarmth,
                        lighting.rimLightColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = size.height * 0.75f
                )
            )
        }
    }
}

