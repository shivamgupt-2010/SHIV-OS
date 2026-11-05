package com.example.companion.avatar

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.*

/**
 * Procedural Vector Canvas Renderer for Avatar 1: "Indie Cacoonaut"
 * Features Form A (Sacred Cocoon Shell) for sleep/startup,
 * and Form B (Celestial Diya Genie Spirit) for active engagement.
 */
@Composable
fun IndieCacoonautRenderer(
    context: AvatarRenderContext,
    modifier: Modifier = Modifier
) {
    // If in deep slumber, freeze animation timer to 0 to save battery!
    val isSlumber = context.sleepStage == InactivitySleepStage.DEEP_SLUMBER_0FPS

    val infiniteTransition = rememberInfiniteTransition(label = "indie_anim")
    val animTime by if (!isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 6.28318f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "time"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    val flameFlicker by if (!isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = -2.5f,
            targetValue = 2.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(120, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "flame"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        if (context.state == AvatarState.SLEEPING || context.sleepStage == InactivitySleepStage.DEEP_SLUMBER_0FPS) {
            drawSacredCocoonShell(cx, cy, flameFlicker, context.seasonalSkin)
        } else {
            drawCelestialDiyaSpirit(cx, cy, animTime, flameFlicker, context)
        }
    }
}

/**
 * FORM A: The Sacred Cocoon Dormant Shell with hanging Toran and Ground Diya.
 */
private fun DrawScope.drawSacredCocoonShell(
    cx: Float,
    cy: Float,
    flameFlicker: Float,
    skin: SeasonalSkin
) {
    // 1. Hanging Floral Toran
    val toranY = cy - 65f
    drawLine(
        color = Color(0xFFB45309),
        start = Offset(cx - 40f, toranY),
        end = Offset(cx + 40f, toranY),
        strokeWidth = 3f,
        cap = StrokeCap.Round
    )

    for (i in -2..2) {
        val beadX = cx + (i * 18f)
        drawLine(
            color = Color(0xFFF59E0B),
            start = Offset(beadX, toranY),
            end = Offset(beadX, toranY + 22f),
            strokeWidth = 1.5f
        )
        // Floral lotus bud
        drawCircle(
            color = Color(0xFFE11D48),
            radius = 3.5f,
            center = Offset(beadX, toranY + 24f)
        )
    }

    // 2. The Geometric Cocoon Shell
    val shellPath = Path().apply {
        moveTo(cx - 32f, cy - 35f)
        quadraticBezierTo(cx, cy - 55f, cx + 32f, cy - 35f)
        quadraticBezierTo(cx + 42f, cy + 15f, cx + 18f, cy + 45f)
        quadraticBezierTo(cx, cy + 55f, cx - 18f, cy + 45f)
        quadraticBezierTo(cx - 42f, cy + 15f, cx - 32f, cy - 35f)
        close()
    }

    // Terracotta-crimson rich gradient
    drawPath(
        path = shellPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF991B1B), Color(0xFF7F1D1D), Color(0xFF450A0A)),
            startY = cy - 55f,
            endY = cy + 55f
        )
    )
    drawPath(
        path = shellPath,
        color = Color(0xFFF59E0B),
        style = Stroke(width = 2.5f)
    )

    // Golden Turban Saree Diadem Band
    val bandPath = Path().apply {
        moveTo(cx - 28f, cy - 25f)
        quadraticBezierTo(cx, cy - 40f, cx + 28f, cy - 25f)
        lineTo(cx + 26f, cy - 18f)
        quadraticBezierTo(cx, cy - 33f, cx - 26f, cy - 18f)
        close()
    }
    drawPath(
        path = bandPath,
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFFF59E0B), Color(0xFFFEF08A), Color(0xFFF59E0B))
        )
    )

    // Serene Closed Eye Curve (Sleeping)
    val eyePath = Path().apply {
        moveTo(cx - 10f, cy + 2f)
        quadraticBezierTo(cx, cy + 8f, cx + 10f, cy + 2f)
    }
    drawPath(
        path = eyePath,
        color = Color(0xFFFDE68A),
        style = Stroke(width = 2f, cap = StrokeCap.Round)
    )

    // Red Tilak
    drawCircle(
        color = Color(0xFFDC2626),
        radius = 2.2f,
        center = Offset(cx, cy - 12f)
    )

    // 3. Resting Clay Diya on Ground
    val diyaY = cy + 62f
    drawClayDiya(cx, diyaY, flameFlicker, scale = 1.0f)
}

/**
 * FORM B: The Awakened Celestial Diya Genie Spirit.
 */
private fun DrawScope.drawCelestialDiyaSpirit(
    cx: Float,
    cy: Float,
    animTime: Float,
    flameFlicker: Float,
    context: AvatarRenderContext
) {
    val hoverOffset = sin(animTime * 1.5f) * 6f
    val currentCy = cy + hoverOffset

    // 1. Serpentine Saffron Spirit Tail
    val tailPath = Path().apply {
        moveTo(cx - 12f, currentCy + 25f)
        quadraticBezierTo(
            cx - 25f + sin(animTime) * 8f,
            currentCy + 55f,
            cx + sin(animTime * 1.2f) * 12f,
            currentCy + 80f
        )
        quadraticBezierTo(
            cx + 20f,
            currentCy + 60f,
            cx + 12f,
            currentCy + 25f
        )
        close()
    }
    drawPath(
        path = tailPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFEA580C), Color(0xFFF59E0B), Color(0x00FBBF24)),
            startY = currentCy + 25f,
            endY = currentCy + 85f
        )
    )

    // 2. Chibi Crimson-Gold Attire Body
    val bodyRect = Path().apply {
        moveTo(cx - 16f, currentCy + 10f)
        lineTo(cx + 16f, currentCy + 10f)
        lineTo(cx + 12f, currentCy + 28f)
        lineTo(cx - 12f, currentCy + 28f)
        close()
    }
    drawPath(
        path = bodyRect,
        color = Color(0xFF991B1B)
    )
    drawPath(
        path = bodyRect,
        color = Color(0xFFF59E0B),
        style = Stroke(width = 1.8f)
    )

    // 3. Chibi Head & Turban
    // Face skin
    drawCircle(
        color = Color(0xFFFFEDD5),
        radius = 26f,
        center = Offset(cx, currentCy - 12f)
    )

    // Crimson Royal Turban
    val turbanPath = Path().apply {
        moveTo(cx - 28f, currentCy - 12f)
        quadraticBezierTo(cx, currentCy - 48f, cx + 28f, currentCy - 12f)
        quadraticBezierTo(cx + 18f, currentCy - 28f, cx, currentCy - 30f)
        quadraticBezierTo(cx - 18f, currentCy - 28f, cx - 28f, currentCy - 12f)
        close()
    }
    drawPath(
        path = turbanPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFB91C1C), Color(0xFF991B1B))
        )
    )
    // Gold filigree band
    drawArc(
        color = Color(0xFFF59E0B),
        startAngle = 190f,
        sweepAngle = 160f,
        useCenter = false,
        topLeft = Offset(cx - 26f, currentCy - 32f),
        size = Size(52f, 32f),
        style = Stroke(width = 3.5f)
    )

    // Red Sacred Tilak / Bindi
    val tilakPath = Path().apply {
        moveTo(cx, currentCy - 24f)
        lineTo(cx + 2.5f, currentCy - 18f)
        lineTo(cx, currentCy - 15f)
        lineTo(cx - 2.5f, currentCy - 18f)
        close()
    }
    drawPath(path = tilakPath, color = Color(0xFFDC2626))

    // 4. Large Expressive Amber Anime Eyes
    drawAmberEyes(cx, currentCy - 12f, context.state, context.sleepStage, animTime)

    // 5. Delicate Smiling Mouth / Audio Reactive Phoneme
    drawMouth(cx, currentCy - 2f, context.state, context.audioAmplitude)

    // 6. Floating Diya Companion
    val diyaX = cx + 38f + sin(animTime * 2f) * 3f
    val diyaY = currentCy + 6f + cos(animTime * 1.5f) * 4f
    drawClayDiya(diyaX, diyaY, flameFlicker, scale = 0.95f)

    // 7. Situational: Chain-of-Thought Glowing Astrological Scroll
    if (context.situationalEvent == SituationalEvent.REASONING_CHAIN_OF_THOUGHT) {
        val scrollX = cx - 36f
        val scrollY = currentCy + 10f
        drawRect(
            color = Color(0xFFFEF3C7),
            topLeft = Offset(scrollX - 16f, scrollY - 24f),
            size = Size(32f, 36f)
        )
        drawRect(
            color = Color(0xFFF59E0B),
            topLeft = Offset(scrollX - 16f, scrollY - 24f),
            size = Size(32f, 36f),
            style = Stroke(width = 1.5f)
        )
        // Celestial glyph lines
        for (line in 0..3) {
            drawLine(
                color = Color(0xFFD97706),
                start = Offset(scrollX - 11f, scrollY - 18f + (line * 7f)),
                end = Offset(scrollX + 11f, scrollY - 18f + (line * 7f)),
                strokeWidth = 1.5f
            )
        }
    }
}

/**
 * Draws the ornamental terracotta clay Diya with dynamic glowing flame.
 */
private fun DrawScope.drawClayDiya(
    x: Float,
    y: Float,
    flicker: Float,
    scale: Float = 1.0f
) {
    val w = 26f * scale
    val h = 13f * scale

    // Clay Bowl
    val bowlPath = Path().apply {
        moveTo(x - w / 2f, y)
        quadraticBezierTo(x, y + h, x + w / 2f, y)
        close()
    }
    drawPath(
        path = bowlPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFB45309), Color(0xFF78350F))
        )
    )
    drawPath(
        path = bowlPath,
        color = Color(0xFFF59E0B),
        style = Stroke(width = 1.5f)
    )

    // Outer Aura Glow
    drawCircle(
        color = Color(0x33F59E0B),
        radius = 24f * scale,
        center = Offset(x, y - 8f)
    )

    // Organic Flickering Flame
    val flameTipX = x + flicker
    val flameTipY = y - (24f * scale)
    val flamePath = Path().apply {
        moveTo(x - (5f * scale), y - 2f)
        quadraticBezierTo(x - (8f * scale), y - (12f * scale), flameTipX, flameTipY)
        quadraticBezierTo(x + (8f * scale), y - (12f * scale), x + (5f * scale), y - 2f)
        close()
    }
    drawPath(
        path = flamePath,
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFFEF08A), Color(0xFFEA580C)),
            center = Offset(x, y - 8f),
            radius = 16f * scale
        )
    )
}

/**
 * Expressive Amber Eyes with blinks, yawns, and state reactions.
 */
private fun DrawScope.drawAmberEyes(
    cx: Float,
    cy: Float,
    state: AvatarState,
    sleepStage: InactivitySleepStage,
    animTime: Float
) {
    val eyeSpacing = 11f
    val isDrowsy = sleepStage == InactivitySleepStage.DROWSY_15FPS
    val isAlert = state == AvatarState.ALERT || state == AvatarState.STRICT_COACH

    // Eyelid heights
    val eyeH = when {
        isDrowsy -> 3.5f // Heavy droop
        isAlert -> 9.5f // Wide alert
        state == AvatarState.CELEBRATING -> 2.0f // Happy squint
        else -> 7.0f
    }

    for (dir in listOf(-1, 1)) {
        val ex = cx + (dir * eyeSpacing)
        val ey = cy

        if (state == AvatarState.CELEBRATING) {
            // Inverted happy arch
            drawArc(
                color = Color(0xFF78350F),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(ex - 5f, ey - 3f),
                size = Size(10f, 6f),
                style = Stroke(width = 2.2f, cap = StrokeCap.Round)
            )
        } else {
            // Amber gradient iris
            drawOval(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFD97706), Color(0xFF78350F))
                ),
                topLeft = Offset(ex - 4.5f, ey - eyeH / 2f),
                size = Size(9f, eyeH)
            )
            // Starry eye reflection dot
            drawCircle(
                color = Color.White,
                radius = 1.8f,
                center = Offset(ex - 1.5f, ey - eyeH / 4f)
            )
        }
    }
}

private fun DrawScope.drawMouth(
    cx: Float,
    cy: Float,
    state: AvatarState,
    amplitude: Float
) {
    if (state == AvatarState.SPEAKING) {
        val openH = 2f + (amplitude * 7f)
        drawOval(
            color = Color(0xFF991B1B),
            topLeft = Offset(cx - 3.5f, cy - openH / 2f),
            size = Size(7f, openH)
        )
    } else {
        // Gentle warm smile
        drawArc(
            color = Color(0xFF78350F),
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(cx - 3.5f, cy - 2f),
            size = Size(7f, 4f),
            style = Stroke(width = 1.8f, cap = StrokeCap.Round)
        )
    }
}
