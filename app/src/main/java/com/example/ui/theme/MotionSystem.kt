package com.example.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class MotionPreset(
    val title: String,
    val description: String,
    val durationMs: Int
) {
    MINIMAL(
        title = "Minimal",
        description = "Snappy, battery-efficient, instantaneous navigation",
        durationMs = 120
    ),
    SMOOTH(
        title = "Smooth",
        description = "Balanced, fluid Android 14 physics and easing",
        durationMs = 280
    ),
    EXPRESSIVE(
        title = "Expressive",
        description = "Cosmic dynamic spring with playful bounce and depth",
        durationMs = 420
    );

    companion object {
        fun fromName(name: String?): MotionPreset {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: SMOOTH
        }
    }
}

/**
 * Purpose-Driven Animation Timing Hierarchy:
 * - MICRO (140ms): Quick buttons, haptic toggles, typing filters
 * - SPATIAL (300ms): App open/close scale reveals, grid snaps, page swipes
 * - MORPH (420ms): Fullscreen search expansion, drawer sheet rise, widget resize
 * - IMMERSIVE (650ms): Paradigm shifts, 3D holographic tilt entries, terminal boot cascade
 */
enum class AnimationTier(val durationMs: Int) {
    MICRO(140),
    SPATIAL(300),
    MORPH(420),
    IMMERSIVE(650)
}

object MotionSystem {
    fun getDuration(preset: MotionPreset, isReducedMotion: Boolean = false): Int {
        if (isReducedMotion) return 0
        return preset.durationMs
    }

    fun getEasing(preset: MotionPreset): Easing {
        return when (preset) {
            MotionPreset.MINIMAL -> LinearOutSlowInEasing
            MotionPreset.SMOOTH -> FastOutSlowInEasing
            MotionPreset.EXPRESSIVE -> CubicBezierEasing(0.2f, 1.25f, 0.3f, 1.0f)
        }
    }

    fun <T> getTweenSpec(
        preset: MotionPreset,
        isReducedMotion: Boolean = false,
        delayMillis: Int = 0
    ): TweenSpec<T> {
        val duration = getDuration(preset, isReducedMotion)
        return tween(
            durationMillis = duration,
            delayMillis = delayMillis,
            easing = getEasing(preset)
        )
    }

    fun <T> getTierSpec(
        tier: AnimationTier,
        isReducedMotion: Boolean = false,
        delayMillis: Int = 0
    ): TweenSpec<T> {
        if (isReducedMotion) {
            return tween(durationMillis = 0, delayMillis = 0)
        }
        val easing = when (tier) {
            AnimationTier.MICRO -> LinearOutSlowInEasing
            AnimationTier.SPATIAL -> FastOutSlowInEasing
            AnimationTier.MORPH -> CubicBezierEasing(0.25f, 1.25f, 0.3f, 1.0f)
            AnimationTier.IMMERSIVE -> CubicBezierEasing(0.16f, 1.0f, 0.3f, 1.0f)
        }
        return tween(
            durationMillis = tier.durationMs,
            delayMillis = delayMillis,
            easing = easing
        )
    }

    /**
     * Liquid Spring Spec for drag-and-drop, collision reflow, and elastic boundary resistance
     */
    fun <T> getLiquidSpringSpec(isReducedMotion: Boolean = false): SpringSpec<T> {
        if (isReducedMotion) {
            return spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessHigh
            )
        }
        return spring(
            dampingRatio = 0.68f, // Slightly underdamped for realistic fluid liquid rebound
            stiffness = Spring.StiffnessMediumLow
        )
    }

    fun <T> getSpringSpec(
        preset: MotionPreset,
        isReducedMotion: Boolean = false
    ): SpringSpec<T> {
        if (isReducedMotion) {
            return spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessHigh
            )
        }
        return when (preset) {
            MotionPreset.MINIMAL -> spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessHigh
            )
            MotionPreset.SMOOTH -> spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
            MotionPreset.EXPRESSIVE -> spring(
                dampingRatio = 0.62f,
                stiffness = 380f
            )
        }
    }
}

object DesignTokens {
    object Spacing {
        val xxs: Dp = 2.dp
        val xs: Dp = 4.dp
        val sm: Dp = 8.dp
        val md: Dp = 12.dp
        val lg: Dp = 16.dp
        val xl: Dp = 24.dp
        val xxl: Dp = 32.dp
        val xxxl: Dp = 48.dp
    }

    object CornerRadius {
        val xs: Dp = 4.dp
        val sm: Dp = 8.dp
        val md: Dp = 12.dp
        val lg: Dp = 16.dp
        val xl: Dp = 22.dp
        val xxl: Dp = 28.dp
        val pill: Dp = 999.dp
    }

    object Elevation {
        val none: Dp = 0.dp
        val low: Dp = 2.dp
        val medium: Dp = 6.dp
        val high: Dp = 12.dp
    }
}
