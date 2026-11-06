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
 * 3D Procedural Vector Renderer for Avatar 1: "Indie Cacoonaut".
 * Seamlessly morphs between:
 *  A) 3D Sacred Terracotta Cocoon (0 FPS Sleep / Deep Slumber)
 *  B) 3D Living Diya Spirit with dynamic procedural flame physics, ember particles,
 *     earthen clay 3D shading, and swinging golden marigold toran.
 * 100% Vector Math — Zero Cutouts.
 */
@Composable
fun IndieCacoonautRenderer(
    context: AvatarRenderContext,
    modifier: Modifier = Modifier
) {
    val isSlumber = context.sleepStage == InactivitySleepStage.DEEP_SLUMBER_0FPS || context.state == AvatarState.SLEEPING

    val infiniteTransition = rememberInfiniteTransition(label = "indie_3d_physics")

    val levitationBob by if (!isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = -9f,
            targetValue = 9f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "indie_levitation"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    val flameFlicker by if (!isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = -0.15f,
            targetValue = 0.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(180, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "flame_flicker"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    val toranSway by if (!isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = -0.08f,
            targetValue = 0.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(3200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "toran_sway"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    val breathScale by if (!isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "indie_breath"
        )
    } else {
        remember { mutableFloatStateOf(0.97f) }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f + levitationBob
            val scale = (size.minDimension * 0.42f) * breathScale

            // 3D Perspective Projection
            val cameraDist = 450f
            fun project3D(x: Float, y: Float, z: Float): Pair<Offset, Float> {
                val f = cameraDist / (cameraDist + z)
                return Pair(Offset(cx + x * f, cy + y * f), f)
            }

            // A. Warm Amber Volumetric Halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x88FFB300),
                        Color(0x44FF6F00),
                        Color(0x113E2723),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = scale * 1.55f
                ),
                radius = scale * 1.55f,
                center = Offset(cx, cy)
            )

            if (isSlumber) {
                // ==========================================
                // FORM A: 3D SACRED TERRACOTTA COCOON
                // ==========================================
                val cocoonH = scale * 1.15f
                val cocoonW = scale * 0.65f

                // Draw segmented 3D cocoon plates
                val segments = 7
                for (s in 0 until segments) {
                    val t = s.toFloat() / (segments - 1)
                    val y = -cocoonH * 0.5f + t * cocoonH
                    val radiusAtY = cocoonW * sin(PI * t).toFloat()

                    val (pLeft, _) = project3D(-radiusAtY, y, 0f)
                    val (pRight, _) = project3D(radiusAtY, y, 0f)
                    val (pFront, fF) = project3D(0f, y, radiusAtY * 0.7f)

                    // Plate arc
                    val platePath = Path().apply {
                        moveTo(pLeft.x, pLeft.y)
                        quadraticBezierTo(pFront.x, pFront.y + 12f * fF, pRight.x, pRight.y)
                        quadraticBezierTo(pFront.x, pFront.y - 12f * fF, pLeft.x, pLeft.y)
                        close()
                    }

                    val plateCol = if (s % 2 == 0) Color(0xFFD97706) else Color(0xFFB45309)
                    drawPath(platePath, plateCol)
                    drawPath(platePath, Color(0xFFFDE68A), style = Stroke(width = 1.5f))
                }

                // Sacred Golden Filigree Core / Diya glow from within
                drawCircle(
                    color = Color(0xFFFFD54F).copy(alpha = 0.85f),
                    radius = scale * 0.22f,
                    center = Offset(cx, cy)
                )
            } else {
                // ==========================================
                // FORM B: CELESTIAL LIVING DIYA SPIRIT
                // ==========================================

                // 1. 3D Terracotta Diya Base (Oil Lamp)
                val diyaY = scale * 0.28f
                val diyaWidth = scale * 0.75f
                val diyaHeight = scale * 0.35f

                // Diya 3D Elliptical Rim & Body
                val (diyaL, _) = project3D(-diyaWidth, diyaY, 0f)
                val (diyaR, _) = project3D(diyaWidth, diyaY, 0f)
                val (diyaFront, fD) = project3D(0f, diyaY + diyaHeight * 0.5f, diyaWidth * 0.65f)
                val (diyaBack, _) = project3D(0f, diyaY - diyaHeight * 0.4f, -diyaWidth * 0.65f)
                val (diyaBottom, _) = project3D(0f, diyaY + diyaHeight, 0f)

                // Earthen Lamp Body Path
                val bodyPath = Path().apply {
                    moveTo(diyaL.x, diyaL.y)
                    quadraticBezierTo(diyaFront.x, diyaFront.y, diyaR.x, diyaR.y)
                    quadraticBezierTo(diyaBottom.x, diyaBottom.y + 20f * fD, diyaL.x, diyaL.y)
                    close()
                }

                // Shaded Terracotta Clay Gradient
                drawPath(
                    path = bodyPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFEA580C), Color(0xFF9A3412), Color(0xFF431407)),
                        startY = diyaL.y,
                        endY = diyaBottom.y
                    )
                )
                drawPath(path = bodyPath, color = Color(0xFFFED7AA), style = Stroke(width = 1.8f))

                // Diya Inner Oil Well
                val rimPath = Path().apply {
                    moveTo(diyaL.x, diyaL.y)
                    quadraticBezierTo(diyaFront.x, diyaFront.y, diyaR.x, diyaR.y)
                    quadraticBezierTo(diyaBack.x, diyaBack.y, diyaL.x, diyaL.y)
                    close()
                }
                drawPath(
                    path = rimPath,
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFD54F), Color(0xFFD97706), Color(0xFF78350F)),
                        center = Offset(cx, diyaY),
                        radius = diyaWidth * 0.8f
                    )
                )

                // 2. Dynamic Procedural 3D Flickering Flame
                val flameBaseY = diyaY - scale * 0.05f
                val flameH = (scale * 0.85f) * (1f + context.audioAmplitude * 0.65f)
                val flameW = scale * 0.38f
                val fx = flameFlicker * scale * 0.3f

                val (tipP, fTip) = project3D(fx, flameBaseY - flameH, 0f)
                val (flameLeft, _) = project3D(-flameW, flameBaseY - flameH * 0.38f, flameW * 0.35f)
                val (flameRight, _) = project3D(flameW, flameBaseY - flameH * 0.38f, -flameW * 0.35f)
                val (flameBase, _) = project3D(0f, flameBaseY, 0f)

                // Outer Golden Flame Teardrop
                val flamePath = Path().apply {
                    moveTo(flameBase.x, flameBase.y)
                    cubicTo(flameLeft.x - 10f, flameLeft.y, flameLeft.x, tipP.y + 20f, tipP.x, tipP.y)
                    cubicTo(flameRight.x, tipP.y + 20f, flameRight.x + 10f, flameRight.y, flameBase.x, flameBase.y)
                    close()
                }

                drawPath(
                    path = flamePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFFBEB), Color(0xFFFBBF24), Color(0xFFF97316), Color(0xFFDC2626)),
                        startY = tipP.y,
                        endY = flameBase.y
                    )
                )

                // Inner Blue High-Energy Core
                val (blueTip, _) = project3D(fx * 0.3f, flameBaseY - flameH * 0.35f, 0f)
                val innerBluePath = Path().apply {
                    moveTo(flameBase.x, flameBase.y)
                    cubicTo(flameBase.x - 14f, flameBase.y - 10f, blueTip.x - 10f, blueTip.y + 10f, blueTip.x, blueTip.y)
                    cubicTo(blueTip.x + 10f, blueTip.y + 10f, flameBase.x + 14f, flameBase.y - 10f, flameBase.x, flameBase.y)
                    close()
                }
                drawPath(
                    path = innerBluePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF67E8F9), Color(0xFF0284C7), Color(0xFF1E3A8A)),
                        startY = blueTip.y,
                        endY = flameBase.y
                    )
                )

                // 3. Ascending Golden Ember Sparks in 3D Space
                val sparkCount = 9
                for (i in 0 until sparkCount) {
                    val angle = (i * 1.618f * 2 * PI + flameFlicker * 3f).toFloat()
                    val sparkDist = scale * (0.35f + 0.55f * (i.toFloat() / sparkCount))
                    val sx = sin(angle) * sparkDist * 0.5f + fx * (i + 1) * 0.2f
                    val sy = flameBaseY - flameH * 0.4f - (i.toFloat() / sparkCount) * (flameH * 0.95f)
                    val sz = cos(angle) * scale * 0.35f

                    val (spP, fS) = project3D(sx, sy, sz)
                    val spR = (3.5f + (i % 3) * 1.5f) * fS
                    drawCircle(Color(0xFFFDE047).copy(alpha = 0.85f), spR, spP)
                }

                // 4. Swaying Golden Marigold Garland (Toran)
                val toranY = diyaY + diyaHeight * 0.15f
                val toranPoints = 11
                val toranSpan = diyaWidth * 1.25f

                for (i in 0 until toranPoints) {
                    val t = (i.toFloat() / (toranPoints - 1)) * 2f - 1f // -1 to 1
                    val tx = t * toranSpan
                    val catenary = (1f - t * t) * (scale * 0.28f) + sin(toranSway * 10f + t * 4f) * 6f
                    val ty = toranY + catenary
                    val tz = cos(t * PI * 0.5f).toFloat() * scale * 0.35f

                    val (tp, fT) = project3D(tx, ty, tz)
                    val beadR = 5.5f * fT
                    val beadColor = if (i % 2 == 0) Color(0xFFF59E0B) else Color(0xFFEA580C)
                    drawCircle(beadColor, beadR, tp)
                    drawCircle(Color(0xFFFEF3C7), beadR * 0.35f, Offset(tp.x - beadR * 0.25f, tp.y - beadR * 0.25f))
                }
            }
        }
    }
}
