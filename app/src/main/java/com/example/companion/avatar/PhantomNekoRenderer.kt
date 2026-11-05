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
import kotlin.math.*

/**
 * Procedural Vector Canvas Renderer for Avatar 2: "Phantom Neko"
 * A shapeshifting companion combining the tactile, realistic charm of
 * Form A (The Mystic Multi-Tailed Fox-Cat) with Form B (The Neon Specter Ghost).
 */
@Composable
fun PhantomNekoRenderer(
    context: AvatarRenderContext,
    modifier: Modifier = Modifier
) {
    val isSlumber = context.sleepStage == InactivitySleepStage.DEEP_SLUMBER_0FPS

    val infiniteTransition = rememberInfiniteTransition(label = "neko_anim")
    val animTime by if (!isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 6.28318f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "time"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    // Ear twitch cycle (every few seconds)
    val earTwitch by if (!isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 3500
                    0f at 0
                    0f at 3000
                    -4f at 3100
                    4f at 3200
                    0f at 3300
                    0f at 3500
                },
                repeatMode = RepeatMode.Restart
            ),
            label = "ear"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Switch between Ghost Form (flying/phase-shifting) and Cat Form
        if (context.isGhostForm || context.state == AvatarState.FLYING_TO_TARGET) {
            drawNeonSpecterGhost(cx, cy, animTime)
        } else {
            drawFluffyMysticCat(cx, cy, animTime, earTwitch, context)
        }
    }
}

/**
 * FORM A: The Fluffy Multi-Tailed Mystic Fox-Cat.
 */
private fun DrawScope.drawFluffyMysticCat(
    cx: Float,
    cy: Float,
    animTime: Float,
    earTwitch: Float,
    context: AvatarRenderContext
) {
    val hoverY = sin(animTime * 1.6f) * 4f
    val currentCy = cy + hoverY
    val isSleeping = context.state == AvatarState.SLEEPING || context.sleepStage == InactivitySleepStage.DEEP_SLUMBER_0FPS

    // 1. Multiple Wispy Celestial Tails (Waving in zero-g)
    for (i in -2..2) {
        val tailAngle = (i * 0.28f) + sin(animTime + (i * 0.4f)) * 0.15f
        val tailPath = Path().apply {
            moveTo(cx + (i * 6f), currentCy + 14f)
            quadraticBezierTo(
                cx + (i * 26f) + (sin(animTime) * 10f),
                currentCy - 10f + (abs(i) * 6f),
                cx + (i * 38f) + (cos(animTime) * 8f),
                currentCy - 30f + (abs(i) * 12f)
            )
            quadraticBezierTo(
                cx + (i * 20f),
                currentCy + 5f,
                cx + (i * 4f),
                currentCy + 18f
            )
            close()
        }
        drawPath(
            path = tailPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF93C5FD), Color(0xFF3B82F6), Color(0x221D4ED8)),
                startY = currentCy - 35f,
                endY = currentCy + 20f
            )
        )
    }

    if (isSleeping) {
        // SLEEPING: The Fluffy Donut Loaf
        drawDonutLoafCat(cx, currentCy)
        return
    }

    // 2. Sitting Cat Body & Fur
    val bodyPath = Path().apply {
        moveTo(cx - 20f, currentCy + 5f)
        quadraticBezierTo(cx - 26f, currentCy + 32f, cx - 18f, currentCy + 42f)
        lineTo(cx + 18f, currentCy + 42f)
        quadraticBezierTo(cx + 26f, currentCy + 32f, cx + 20f, currentCy + 5f)
        close()
    }
    drawPath(
        path = bodyPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE), Color(0xFFBFDBFE))
        )
    )

    // Glowing Blue Collar with 'S' Medallion
    val collarPath = Path().apply {
        moveTo(cx - 15f, currentCy + 6f)
        quadraticBezierTo(cx, currentCy + 11f, cx + 15f, currentCy + 6f)
        lineTo(cx + 14f, currentCy + 12f)
        quadraticBezierTo(cx, currentCy + 17f, cx - 14f, currentCy + 12f)
        close()
    }
    drawPath(path = collarPath, color = Color(0xFF1E3A8A))
    // Glowing 'S' Pendant
    drawCircle(color = Color(0xFF38BDF8), radius = 5.5f, center = Offset(cx, currentCy + 18f))
    drawCircle(color = Color(0xFFFFFFFF), radius = 2.2f, center = Offset(cx, currentCy + 18f))

    // 3. Cute Chibi Cat Head
    drawCircle(
        color = Color(0xFFFFFFFF),
        radius = 24f,
        center = Offset(cx, currentCy - 14f)
    )

    // Celestial Cyan Crest Marking on Forehead
    val crestPath = Path().apply {
        moveTo(cx, currentCy - 28f)
        lineTo(cx + 3.5f, currentCy - 20f)
        lineTo(cx, currentCy - 16f)
        lineTo(cx - 3.5f, currentCy - 20f)
        close()
    }
    drawPath(path = crestPath, color = Color(0xFF00F0FF))

    // 4. Pointed Fox-Cat Ears (with twitch animation)
    drawCatEar(cx - 16f, currentCy - 28f, isLeft = true, twitch = earTwitch)
    drawCatEar(cx + 16f, currentCy - 28f, isLeft = false, twitch = -earTwitch)

    // 5. Expressive Happy / Anime Cat Eyes
    drawCatEyes(cx, currentCy - 14f, context.state, context.sleepStage)

    // Pink Button Nose & Whisker Dots
    drawCircle(color = Color(0xFFF472B6), radius = 1.8f, center = Offset(cx, currentCy - 8f))

    // Cat W-smile mouth
    val mouthPath = Path().apply {
        moveTo(cx - 5f, currentCy - 6f)
        quadraticBezierTo(cx - 2.5f, currentCy - 3.5f, cx, currentCy - 5.5f)
        quadraticBezierTo(cx + 2.5f, currentCy - 3.5f, cx + 5f, currentCy - 6f)
    }
    drawPath(path = mouthPath, color = Color(0xFF1E3A8A), style = Stroke(width = 1.6f, cap = StrokeCap.Round))

    // 6. Biscuit-Kneading Paws (Active during THINKING / PROCESSING)
    if (context.state == AvatarState.THINKING || context.state == AvatarState.PROCESSING) {
        val kneadLeft = sin(animTime * 6f) * 4f
        val kneadRight = cos(animTime * 6f) * 4f
        drawCircle(color = Color.White, radius = 5f, center = Offset(cx - 10f, currentCy + 36f + kneadLeft))
        drawCircle(color = Color.White, radius = 5f, center = Offset(cx + 10f, currentCy + 36f + kneadRight))
    } else {
        // Resting Front Paws
        drawCircle(color = Color.White, radius = 5f, center = Offset(cx - 8f, currentCy + 38f))
        drawCircle(color = Color.White, radius = 5f, center = Offset(cx + 8f, currentCy + 38f))
    }
}

/**
 * Cat Ear with inner pink fluff.
 */
private fun DrawScope.drawCatEar(
    x: Float,
    y: Float,
    isLeft: Boolean,
    twitch: Float
) {
    val earPath = Path().apply {
        if (isLeft) {
            moveTo(x - 8f, y + 10f)
            lineTo(x - 4f + twitch, y - 16f)
            lineTo(x + 10f, y + 6f)
        } else {
            moveTo(x + 8f, y + 10f)
            lineTo(x + 4f + twitch, y - 16f)
            lineTo(x - 10f, y + 6f)
        }
        close()
    }
    drawPath(path = earPath, color = Color(0xFFDBEAFE))
    // Pink interior
    val innerPath = Path().apply {
        if (isLeft) {
            moveTo(x - 5f, y + 8f)
            lineTo(x - 3f + (twitch * 0.7f), y - 11f)
            lineTo(x + 7f, y + 6f)
        } else {
            moveTo(x + 5f, y + 8f)
            lineTo(x + 3f + (twitch * 0.7f), y - 11f)
            lineTo(x - 7f, y + 6f)
        }
        close()
    }
    drawPath(path = innerPath, color = Color(0xFFF472B6))
}

/**
 * Cat Eyes with anime arches and drowsy squints.
 */
private fun DrawScope.drawCatEyes(
    cx: Float,
    cy: Float,
    state: AvatarState,
    sleepStage: InactivitySleepStage
) {
    val isDrowsy = sleepStage == InactivitySleepStage.DROWSY_15FPS
    for (dir in listOf(-1, 1)) {
        val ex = cx + (dir * 10f)
        val ey = cy
        if (state == AvatarState.IDLE || state == AvatarState.CELEBRATING || isDrowsy) {
            // Cute inverted happy arc
            drawArc(
                color = Color(0xFF1E293B),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(ex - 4.5f, ey - 2.5f),
                size = Size(9f, 5f),
                style = Stroke(width = 2.2f, cap = StrokeCap.Round)
            )
        } else {
            // Big sparkling blue eyes
            drawCircle(color = Color(0xFF2563EB), radius = 4f, center = Offset(ex, ey))
            drawCircle(color = Color.White, radius = 1.5f, center = Offset(ex - 1.2f, ey - 1.2f))
        }
    }
}

/**
 * Curled up Donut Loaf (Deep sleep mode).
 */
private fun DrawScope.drawDonutLoafCat(cx: Float, cy: Float) {
    // Curled body oval
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFBFDBFE), Color(0xFF93C5FD)),
            center = Offset(cx, cy + 8f),
            radius = 35f
        ),
        topLeft = Offset(cx - 32f, cy - 14f),
        size = Size(64f, 44f)
    )
    // Tucked nose & closed eyes
    drawArc(
        color = Color(0xFF1E3A8A),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(cx - 8f, cy + 2f),
        size = Size(16f, 8f),
        style = Stroke(width = 2f, cap = StrokeCap.Round)
    )
    // Multitails wrapped tightly
    drawArc(
        color = Color(0xFF60A5FA),
        startAngle = 20f,
        sweepAngle = 160f,
        useCenter = false,
        topLeft = Offset(cx - 30f, cy - 8f),
        size = Size(60f, 38f),
        style = Stroke(width = 4f, cap = StrokeCap.Round)
    )
}

/**
 * FORM B: The Translucent Glowing Neon Specter Ghost.
 */
private fun DrawScope.drawNeonSpecterGhost(
    cx: Float,
    cy: Float,
    animTime: Float
) {
    val floatOffset = sin(animTime * 2.2f) * 6f
    val currentCy = cy + floatOffset

    // 1. Ambient Soft Neon Glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x5500F0FF), Color(0x33D946EF), Color(0x00000000)),
            center = Offset(cx, currentCy),
            radius = 52f
        ),
        radius = 52f,
        center = Offset(cx, currentCy)
    )

    // 2. Wavy Translucent Ghost Sheet Body
    val ghostPath = Path().apply {
        moveTo(cx - 24f, currentCy + 8f)
        // Rounded head top
        quadraticBezierTo(cx - 24f, currentCy - 34f, cx, currentCy - 34f)
        quadraticBezierTo(cx + 24f, currentCy - 34f, cx + 24f, currentCy + 8f)
        // Wavy skirt ripples
        quadraticBezierTo(cx + 18f, currentCy + 28f, cx + 12f, currentCy + 18f)
        quadraticBezierTo(cx + 6f, currentCy + 32f, cx, currentCy + 20f)
        quadraticBezierTo(cx - 6f, currentCy + 32f, cx - 12f, currentCy + 18f)
        quadraticBezierTo(cx - 18f, currentCy + 28f, cx - 24f, currentCy + 8f)
        close()
    }
    drawPath(
        path = ghostPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF38BDF8), Color(0xFF818CF8), Color(0xFFE879F9)),
            startY = currentCy - 34f,
            endY = currentCy + 28f
        )
    )
    drawPath(
        path = ghostPath,
        color = Color(0xFFFFFFFF),
        style = Stroke(width = 1.8f)
    )

    // 3. Cheerful Glowing White Eyes & Smile
    drawOval(color = Color.White, topLeft = Offset(cx - 12f, currentCy - 16f), size = Size(8f, 11f))
    drawOval(color = Color.White, topLeft = Offset(cx + 4f, currentCy - 16f), size = Size(8f, 11f))

    // Grinning friendly teeth smile
    val smilePath = Path().apply {
        moveTo(cx - 8f, currentCy - 1f)
        quadraticBezierTo(cx, currentCy + 7f, cx + 8f, currentCy - 1f)
        close()
    }
    drawPath(path = smilePath, color = Color.White)
}
