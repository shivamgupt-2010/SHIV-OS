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
 * Procedural Vector Canvas Renderer for Avatar 3: "Authentic Cosmic Shiv"
 * The signature sovereign cyber-celestial persona.
 * Features the floating crystalline diamond crown, concentric iridescent orbital ribbons,
 * starry eyes, and dual floating holographic cyber HUD screens.
 */
@Composable
fun CosmicShivRenderer(
    context: AvatarRenderContext,
    modifier: Modifier = Modifier
) {
    val isSlumber = context.sleepStage == InactivitySleepStage.DEEP_SLUMBER_0FPS

    val infiniteTransition = rememberInfiniteTransition(label = "shiv_anim")
    val animTime by if (!isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 6.28318f,
            animationSpec = infiniteRepeatable(
                animation = tween(2600, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "time"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    // Ribbon spin angle
    val ribbonSpin by if (!isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(4500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "spin"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        if (context.state == AvatarState.SLEEPING || context.sleepStage == InactivitySleepStage.DEEP_SLUMBER_0FPS) {
            drawLotusMeditationShiv(cx, cy)
        } else {
            drawAwakenedCosmicShiv(cx, cy, animTime, ribbonSpin, context)
        }
    }
}

/**
 * Active Awakened Cosmic Shiv.
 */
private fun DrawScope.drawAwakenedCosmicShiv(
    cx: Float,
    cy: Float,
    animTime: Float,
    ribbonSpin: Float,
    context: AvatarRenderContext
) {
    val hoverY = sin(animTime * 1.8f) * 5f
    val currentCy = cy + hoverY

    // 1. Dual Concentric 3D Iridescent Energy Ribbons
    drawOrbitalRibbons(cx, currentCy, ribbonSpin, context.seasonalSkin)

    // 2. Cosmic Sinuous Spirit Tail
    val tailPath = Path().apply {
        moveTo(cx - 10f, currentCy + 18f)
        quadraticBezierTo(
            cx - 18f + sin(animTime) * 6f,
            currentCy + 45f,
            cx + sin(animTime * 1.4f) * 10f,
            currentCy + 72f
        )
        quadraticBezierTo(
            cx + 15f,
            currentCy + 48f,
            cx + 10f,
            currentCy + 18f
        )
        close()
    }
    drawPath(
        path = tailPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF38BDF8), Color(0xFF818CF8), Color(0x00C084FC)),
            startY = currentCy + 18f,
            endY = currentCy + 75f
        )
    )

    // 3. Cyber Chibi Torso with 'S' Crest
    val torsoPath = Path().apply {
        moveTo(cx - 14f, currentCy + 6f)
        lineTo(cx + 14f, currentCy + 6f)
        lineTo(cx + 10f, currentCy + 22f)
        lineTo(cx - 10f, currentCy + 22f)
        close()
    }
    drawPath(
        path = torsoPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
        )
    )
    drawPath(path = torsoPath, color = Color(0xFF38BDF8), style = Stroke(width = 1.4f))

    // Glowing 'S' Chest Core
    drawCircle(color = Color(0xFF00F0FF), radius = 3.5f, center = Offset(cx, currentCy + 14f))

    // 4. Iridescent Cyber-Celestial Chibi Head
    drawCircle(
        color = Color(0xFFF8FAFC),
        radius = 24f,
        center = Offset(cx, currentCy - 16f)
    )
    // Celestial Horn Ribbons on Sides of Head
    drawHeadWings(cx, currentCy - 16f, animTime)

    // Forehead Crystalline Diamond Gem
    drawDiamond(cx, currentCy - 28f, width = 7f, height = 11f, color = Color(0xFF00F0FF))

    // 5. Large Starry Anime Eyes
    drawStarryEyes(cx, currentCy - 16f, context.state, context.sleepStage)

    // 6. Audio-Reactive Mouth Phoneme
    drawCyberMouth(cx, currentCy - 6f, context.state, context.audioAmplitude)

    // 7. Floating Crystalline Crown Diamond (Halo)
    val haloY = currentCy - 48f + sin(animTime * 2.5f) * 3f
    drawFloatingDiamondHalo(cx, haloY)

    // 8. Situational: Dual Translucent Cyber HUD Screens (Deep Reasoning DAG)
    if (context.situationalEvent == SituationalEvent.REASONING_CHAIN_OF_THOUGHT ||
        context.state == AvatarState.PROCESSING ||
        context.state == AvatarState.THINKING
    ) {
        drawCyberHudScreens(cx, currentCy, animTime)
    }

    // 9. Seasonal: Dark Screen Conical Torch Beam
    if (context.seasonalSkin == SeasonalSkin.DARK_TORCH || context.darkScreenLuminance < 0.35f) {
        drawConicalTorchBeam(cx, currentCy)
    }
}

/**
 * Dual Concentric Iridescent Ribbons circling the body.
 */
private fun DrawScope.drawOrbitalRibbons(
    cx: Float,
    cy: Float,
    spin: Float,
    skin: SeasonalSkin
) {
    val ribbonColor1 = if (skin == SeasonalSkin.DIWALI_FESTIVE) Color(0xFFF59E0B) else Color(0xFF00F0FF)
    val ribbonColor2 = if (skin == SeasonalSkin.DIWALI_FESTIVE) Color(0xFFDC2626) else Color(0xFFC084FC)

    rotate(degrees = spin * 0.4f, pivot = Offset(cx, cy + 10f)) {
        drawOval(
            color = ribbonColor1.copy(alpha = 0.45f),
            topLeft = Offset(cx - 36f, cy - 2f),
            size = Size(72f, 22f),
            style = Stroke(width = 2.2f)
        )
        // Orbiting glowing cyan node
        drawCircle(
            color = ribbonColor1,
            radius = 3.5f,
            center = Offset(cx + 36f, cy + 9f)
        )
    }

    rotate(degrees = -spin * 0.6f, pivot = Offset(cx, cy + 10f)) {
        drawOval(
            color = ribbonColor2.copy(alpha = 0.35f),
            topLeft = Offset(cx - 44f, cy - 6f),
            size = Size(88f, 28f),
            style = Stroke(width = 1.6f)
        )
    }
}

private fun DrawScope.drawHeadWings(cx: Float, cy: Float, animTime: Float) {
    for (dir in listOf(-1, 1)) {
        val wingPath = Path().apply {
            moveTo(cx + (dir * 20f), cy)
            quadraticBezierTo(
                cx + (dir * 38f),
                cy - 18f,
                cx + (dir * 32f),
                cy - 32f
            )
            quadraticBezierTo(
                cx + (dir * 24f),
                cy - 12f,
                cx + (dir * 18f),
                cy - 4f
            )
            close()
        }
        drawPath(
            path = wingPath,
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF818CF8), Color(0xFF00F0FF))
            )
        )
    }
}

private fun DrawScope.drawFloatingDiamondHalo(cx: Float, cy: Float) {
    drawDiamond(cx, cy, width = 14f, height = 22f, color = Color(0xFF38BDF8))
    // Diamond Inner Shimmer
    drawDiamond(cx, cy, width = 7f, height = 11f, color = Color.White)
    // Halo Glow
    drawCircle(color = Color(0x3300F0FF), radius = 18f, center = Offset(cx, cy))
}

private fun DrawScope.drawDiamond(
    cx: Float,
    cy: Float,
    width: Float,
    height: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(cx, cy - height / 2f)
        lineTo(cx + width / 2f, cy)
        lineTo(cx, cy + height / 2f)
        lineTo(cx - width / 2f, cy)
        close()
    }
    drawPath(path = path, color = color)
}

private fun DrawScope.drawStarryEyes(
    cx: Float,
    cy: Float,
    state: AvatarState,
    sleepStage: InactivitySleepStage
) {
    val isDrowsy = sleepStage == InactivitySleepStage.DROWSY_15FPS
    val eyeH = if (isDrowsy) 4.5f else 11f

    for (dir in listOf(-1, 1)) {
        val ex = cx + (dir * 11f)
        val ey = cy
        // Violet/Cyan Starry Eye
        drawOval(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF6366F1), Color(0xFF0284C7))
            ),
            topLeft = Offset(ex - 5.5f, ey - eyeH / 2f),
            size = Size(11f, eyeH)
        )
        // 4-Pointed Star Glint
        drawLine(color = Color.White, start = Offset(ex - 2f, ey - 2f), end = Offset(ex + 2f, ey - 2f), strokeWidth = 1.2f)
        drawLine(color = Color.White, start = Offset(ex, ey - 4f), end = Offset(ex, ey), strokeWidth = 1.2f)
    }
}

private fun DrawScope.drawCyberMouth(
    cx: Float,
    cy: Float,
    state: AvatarState,
    amplitude: Float
) {
    if (state == AvatarState.SPEAKING) {
        val h = 2f + (amplitude * 8f)
        drawOval(color = Color(0xFF0F172A), topLeft = Offset(cx - 3.5f, cy - h / 2f), size = Size(7f, h))
    } else {
        drawArc(
            color = Color(0xFF0284C7),
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(cx - 3.5f, cy - 2f),
            size = Size(7f, 4f),
            style = Stroke(width = 1.6f, cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawCyberHudScreens(cx: Float, cy: Float, animTime: Float) {
    for (dir in listOf(-1, 1)) {
        val screenX = cx + (dir * 46f)
        val screenY = cy + 4f
        drawRoundRect(
            color = Color(0x3300F0FF),
            topLeft = Offset(screenX - 18f, screenY - 14f),
            size = Size(36f, 28f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        drawRoundRect(
            color = Color(0xFF00F0FF),
            topLeft = Offset(screenX - 18f, screenY - 14f),
            size = Size(36f, 28f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
            style = Stroke(width = 1.2f)
        )
        // Code lines
        for (i in 0..2) {
            drawLine(
                color = Color(0xFF38BDF8),
                start = Offset(screenX - 13f, screenY - 8f + (i * 7f)),
                end = Offset(screenX + 13f, screenY - 8f + (i * 7f)),
                strokeWidth = 1.2f
            )
        }
    }
}

private fun DrawScope.drawConicalTorchBeam(cx: Float, cy: Float) {
    val beamPath = Path().apply {
        moveTo(cx, cy + 22f)
        lineTo(cx + 45f, cy + 90f)
        lineTo(cx - 45f, cy + 90f)
        close()
    }
    drawPath(
        path = beamPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0x66FEF08A), Color(0x0000F0FF)),
            startY = cy + 22f,
            endY = cy + 90f
        )
    )
}

/**
 * Serene Zero-G Lotus Meditation Pose (Deep Slumber Freeze 0 FPS).
 */
private fun DrawScope.drawLotusMeditationShiv(cx: Float, cy: Float) {
    // Glowing Blue Sigil Disc on Ground
    drawOval(
        color = Color(0x5500F0FF),
        topLeft = Offset(cx - 32f, cy + 30f),
        size = Size(64f, 16f)
    )
    drawOval(
        color = Color(0xFF00F0FF),
        topLeft = Offset(cx - 32f, cy + 30f),
        size = Size(64f, 16f),
        style = Stroke(width = 1.5f)
    )

    // Cross-Legged Lotus Base
    val legsPath = Path().apply {
        moveTo(cx - 24f, cy + 24f)
        quadraticBezierTo(cx, cy + 32f, cx + 24f, cy + 24f)
        close()
    }
    drawPath(path = legsPath, color = Color(0xFF0F172A))
    drawPath(path = legsPath, color = Color(0xFF38BDF8), style = Stroke(width = 1.4f))

    // Meditating Torso & Head
    drawCircle(color = Color(0xFFF8FAFC), radius = 22f, center = Offset(cx, cy - 8f))

    // Closed Serene Curved Eyelids
    for (dir in listOf(-1, 1)) {
        val ex = cx + (dir * 10f)
        val ey = cy - 8f
        drawArc(
            color = Color(0xFF0284C7),
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(ex - 4f, ey - 2f),
            size = Size(8f, 5f),
            style = Stroke(width = 2f, cap = StrokeCap.Round)
        )
    }

    // Dim Night-light Diamond Crown
    drawDiamond(cx, cy - 38f, width = 10f, height = 16f, color = Color(0xAA38BDF8))
}
