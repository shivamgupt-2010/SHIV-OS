package com.example.companion.avatar

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * 3D Procedural Vector Renderer for Avatar 2: "Phantom Neko".
 * Seamlessly morphs between:
 *  A) 3D Faceted Mystic Celestial Fox with articulated twitching ears, glowing crystal eyes,
 *     and dynamic 3D multi-tail physics.
 *  B) 3D Translucent Cyan Specter Ghost with ethereal whisker trails and phase-shift aura.
 * 100% Vector Math — Zero Cutouts.
 */
@Composable
fun PhantomNekoRenderer(
    context: AvatarRenderContext,
    modifier: Modifier = Modifier
) {
    val isSlumber = context.sleepStage == InactivitySleepStage.DEEP_SLUMBER_0FPS || context.state == AvatarState.SLEEPING
    val isGhostMode = context.isGhostForm || context.state == AvatarState.FLYING_TO_TARGET || context.state == AvatarState.TOUCH_DRAGGING

    val infiniteTransition = rememberInfiniteTransition(label = "neko_3d_physics")

    val levitationBob by if (!isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = -10f,
            targetValue = 10f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "neko_levitation"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    val earTwitch by if (!isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = -0.1f,
            targetValue = 0.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(850, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "ear_twitch"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    val tailSwish by if (!isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = -0.35f,
            targetValue = 0.35f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "tail_swish"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    val ghostAuraPulse by if (!isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "ghost_aura"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f + levitationBob
            val scale = (size.minDimension * 0.42f) * ghostAuraPulse

            // 3D Perspective Projection
            val cameraDist = 450f
            fun project3D(x: Float, y: Float, z: Float): Pair<Offset, Float> {
                val f = cameraDist / (cameraDist + z)
                return Pair(Offset(cx + x * f, cy + y * f), f)
            }

            // A. Mystic Cyan & Violet Ethereal Aura
            val auraColor1 = if (isGhostMode) Color(0x9900E5FF) else Color(0x662979FF)
            val auraColor2 = if (isGhostMode) Color(0x4438BDF8) else Color(0x33A855F7)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(auraColor1, auraColor2, Color.Transparent),
                    center = Offset(cx, cy),
                    radius = scale * 1.55f
                ),
                radius = scale * 1.55f,
                center = Offset(cx, cy)
            )

            // B. 3D Multi-Tail Undulating Matrix (Back-pass: Tails behind head)
            val tailCount = 3
            for (tIdx in 0 until tailCount) {
                val tailOffsetAngle = (tIdx - 1) * 0.38f + tailSwish * (1f + tIdx * 0.2f)
                val tailSegments = 9
                val tailPoints = mutableListOf<Pair<Offset, Float>>()

                for (s in 0 until tailSegments) {
                    val progress = s.toFloat() / (tailSegments - 1)
                    val tailWave = sin(progress * PI + tailSwish * 3f + tIdx * 0.8f).toFloat()

                    val tx = sin(tailOffsetAngle) * (scale * 0.7f * progress) + tailWave * (scale * 0.25f)
                    val ty = scale * 0.2f + progress * (scale * 0.65f) - (progress * progress) * (scale * 0.3f)
                    val tz = -scale * 0.45f + progress * (scale * 0.35f) * cos(tailOffsetAngle).toFloat()

                    tailPoints.add(project3D(tx, ty, tz))
                }

                // Draw tail curve
                if (tailPoints.size >= 2) {
                    val tailPath = Path().apply {
                        moveTo(tailPoints.first().first.x, tailPoints.first().first.y)
                        for (i in 1 until tailPoints.size) {
                            val (pt, _) = tailPoints[i]
                            lineTo(pt.x, pt.y)
                        }
                    }

                    val tailColor = if (isGhostMode) Color(0xCC00E5FF) else Color(0xEE3B82F6)
                    val strokeW = (scale * 0.16f) * (1f - (tIdx * 0.15f))
                    drawPath(
                        path = tailPath,
                        color = tailColor,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                    drawPath(
                        path = tailPath,
                        color = Color(0xFFE0F2FE),
                        style = Stroke(width = strokeW * 0.35f, cap = StrokeCap.Round)
                    )
                }
            }

            if (isGhostMode) {
                // ==========================================
                // FORM B: 3D TRANSLUCENT CYAN SPECTER GHOST
                // ==========================================
                val ghostRadius = scale * 0.58f

                // Translucent Specter Core
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFE0F2FE), Color(0xCC00E5FF), Color(0x550284C7)),
                        center = Offset(cx, cy),
                        radius = ghostRadius
                    ),
                    radius = ghostRadius,
                    center = Offset(cx, cy)
                )

                // Ethereal Whisker Streamers
                for (side in listOf(-1, 1)) {
                    val wStartX = cx + side * (ghostRadius * 0.75f)
                    val wStartY = cy
                    val (wEndP, _) = project3D(side * scale * 0.95f, scale * 0.25f, -scale * 0.2f)

                    val wPath = Path().apply {
                        moveTo(wStartX, wStartY)
                        quadraticBezierTo(cx + side * scale * 1.1f, cy - scale * 0.1f, wEndP.x, wEndP.y)
                    }
                    drawPath(wPath, Color(0xFF67E8F9), style = Stroke(width = 3.5f, cap = StrokeCap.Round))
                }

                // Glowing Specter Eyes
                val eyeOffset = ghostRadius * 0.35f
                drawCircle(Color(0xFFFFFFFF), 7.5f, Offset(cx - eyeOffset, cy - ghostRadius * 0.15f))
                drawCircle(Color(0xFFFFFFFF), 7.5f, Offset(cx + eyeOffset, cy - ghostRadius * 0.15f))
                drawCircle(Color(0xFF00E5FF), 4f, Offset(cx - eyeOffset, cy - ghostRadius * 0.15f))
                drawCircle(Color(0xFF00E5FF), 4f, Offset(cx + eyeOffset, cy - ghostRadius * 0.15f))
            } else {
                // ==========================================
                // FORM A: 3D FACETED CELESTIAL FOX HEAD
                // ==========================================
                val headW = scale * 0.58f
                val headH = scale * 0.52f
                val headD = scale * 0.45f

                // 3D Articulated Fox Ears
                for (side in listOf(-1, 1)) {
                    val earBaseX = side * headW * 0.48f
                    val earBaseY = -headH * 0.65f
                    val earTwitchAngle = side * earTwitch

                    val (earBaseP1, _) = project3D(earBaseX - side * headW * 0.2f, earBaseY, 0f)
                    val (earBaseP2, _) = project3D(earBaseX + side * headW * 0.2f, earBaseY, 0f)
                    val (earTipP, fE) = project3D(
                        earBaseX + side * headW * 0.35f + sin(earTwitchAngle) * 20f,
                        earBaseY - headH * 0.65f,
                        headD * 0.2f
                    )

                    val earPath = Path().apply {
                        moveTo(earBaseP1.x, earBaseP1.y)
                        lineTo(earTipP.x, earTipP.y)
                        lineTo(earBaseP2.x, earBaseP2.y)
                        close()
                    }

                    // Outer ear
                    drawPath(earPath, Color(0xFF2563EB))
                    drawPath(earPath, Color(0xFF93C5FD), style = Stroke(width = 1.8f))

                    // Inner soft pink ear fluff
                    val innerEarPath = Path().apply {
                        moveTo(earBaseP1.x + (earTipP.x - earBaseP1.x) * 0.3f, earBaseP1.y + (earTipP.y - earBaseP1.y) * 0.3f)
                        lineTo(earTipP.x, earTipP.y)
                        lineTo(earBaseP2.x + (earTipP.x - earBaseP2.x) * 0.3f, earBaseP2.y + (earTipP.y - earBaseP2.y) * 0.3f)
                        close()
                    }
                    drawPath(innerEarPath, Color(0xFFF472B6))
                }

                // 3D Faceted Face Geometry
                val (noseP, fN) = project3D(0f, headH * 0.25f, headD * 0.85f)
                val (foreheadP, _) = project3D(0f, -headH * 0.65f, headD * 0.35f)
                val (leftCheekP, _) = project3D(-headW * 0.95f, 0f, 0f)
                val (rightCheekP, _) = project3D(headW * 0.95f, 0f, 0f)
                val (chinP, _) = project3D(0f, headH * 0.65f, headD * 0.2f)

                // Left cheek facet
                val leftFacet = Path().apply {
                    moveTo(foreheadP.x, foreheadP.y)
                    lineTo(leftCheekP.x, leftCheekP.y)
                    lineTo(noseP.x, noseP.y)
                    close()
                }
                drawPath(leftFacet, Color(0xFF3B82F6))

                // Right cheek facet
                val rightFacet = Path().apply {
                    moveTo(foreheadP.x, foreheadP.y)
                    lineTo(rightCheekP.x, rightCheekP.y)
                    lineTo(noseP.x, noseP.y)
                    close()
                }
                drawPath(rightFacet, Color(0xFF1D4ED8))

                // Chin / Muzzle facets
                val leftMuzzle = Path().apply {
                    moveTo(noseP.x, noseP.y)
                    lineTo(leftCheekP.x, leftCheekP.y)
                    lineTo(chinP.x, chinP.y)
                    close()
                }
                drawPath(leftMuzzle, Color(0xFFE0F2FE))

                val rightMuzzle = Path().apply {
                    moveTo(noseP.x, noseP.y)
                    lineTo(rightCheekP.x, rightCheekP.y)
                    lineTo(chinP.x, chinP.y)
                    close()
                }
                drawPath(rightMuzzle, Color(0xFFBAE6FD))

                // Edges highlight
                drawPath(leftFacet, Color(0xFF93C5FD), style = Stroke(width = 1.5f))
                drawPath(rightFacet, Color(0xFF93C5FD), style = Stroke(width = 1.5f))

                // Luminous Celestial Eyes
                val eyeY = -headH * 0.15f
                val eyeX = headW * 0.38f
                val (eyeLP, fEL) = project3D(-eyeX, eyeY, headD * 0.45f)
                val (eyeRP, fER) = project3D(eyeX, eyeY, headD * 0.45f)

                val eyeRadius = 7.5f * fEL
                for (ep in listOf(eyeLP, eyeRP)) {
                    drawCircle(Color(0xFFE0F2FE), eyeRadius, ep)
                    drawCircle(Color(0xFF00E5FF), eyeRadius * 0.65f, ep)
                    drawCircle(Color.White, eyeRadius * 0.28f, Offset(ep.x - eyeRadius * 0.25f, ep.y - eyeRadius * 0.25f))
                }

                // Cute Triangular Fox Nose
                drawCircle(Color(0xFF0F172A), 4.5f * fN, noseP)
            }
        }
    }
}
