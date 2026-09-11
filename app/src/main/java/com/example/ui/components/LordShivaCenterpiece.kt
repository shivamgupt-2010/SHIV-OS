package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

/**
 * A majestic, celestial Lord Shiva meditation centerpiece.
 * Features:
 * - Serene meditating Lord Shiva in Padmasana (lotus pose)
 * - Divine Prabhamandala (pulsing celestial aura)
 * - Crescent Moon (Chandra) shining on the Jata (matted locks)
 * - Sacred Trishul (Trident) with Damru drum
 * - Third Eye (Trinetra) glowing with divine wisdom
 * - Shimmering cosmic stardust
 */
@Composable
fun LordShivaCenterpiece(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shiva_aura")

    // Pulsing aura animation
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_scale"
    )

    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_alpha"
    )

    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_alpha"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            val cx = w * 0.5f
            val cy = h * 0.54f

            // 1. Divine Prabhamandala (Radiant Aura)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ShivaCyan.copy(alpha = auraAlpha * 0.45f),
                        ShivaIndigo.copy(alpha = auraAlpha * 0.3f),
                        ShivaPurple.copy(alpha = auraAlpha * 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy - h * 0.12f),
                    radius = (w * 0.46f) * auraScale
                )
            )

            // Inner golden/amber ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ShivaOrange.copy(alpha = auraAlpha * 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy - h * 0.12f),
                    radius = (w * 0.28f) * auraScale
                )
            )

            // 2. Cosmic Stardust Particles around Lord Shiva
            val particles = listOf(
                Offset(cx - w * 0.32f, cy - h * 0.26f),
                Offset(cx + w * 0.34f, cy - h * 0.22f),
                Offset(cx - w * 0.22f, cy - h * 0.38f),
                Offset(cx + w * 0.24f, cy - h * 0.36f),
                Offset(cx - w * 0.38f, cy - h * 0.05f),
                Offset(cx + w * 0.39f, cy - h * 0.04f),
                Offset(cx - w * 0.12f, cy - h * 0.42f),
                Offset(cx + w * 0.14f, cy - h * 0.40f)
            )
            particles.forEachIndexed { i, pt ->
                val pAlpha = if (i % 2 == 0) starAlpha else (1.4f - starAlpha).coerceIn(0.2f, 1f)
                val pRadius = if (i % 3 == 0) 2.2f else 1.5f
                drawCircle(color = ShivaCyan.copy(alpha = pAlpha * 0.9f), radius = pRadius, center = pt)
            }

            // Colors for Lord Shiva silhouette
            val bodyColor = Color(0xFF0F1528)
            val shadowColor = Color(0xFF090D1A)
            val highlightColor = ShivaCyan.copy(alpha = 0.35f)

            // 3. Lotus Base (Padmasana)
            // Crossed legs / dhoti base
            val baseLeft = cx - w * 0.34f
            val baseRight = cx + w * 0.34f
            val baseY = cy + h * 0.24f

            val lotusPath = Path().apply {
                moveTo(baseLeft, baseY)
                cubicTo(
                    cx - w * 0.28f, baseY + h * 0.08f,
                    cx - w * 0.12f, baseY + h * 0.10f,
                    cx, baseY + h * 0.06f
                )
                cubicTo(
                    cx + w * 0.12f, baseY + h * 0.10f,
                    cx + w * 0.28f, baseY + h * 0.08f,
                    baseRight, baseY
                )
                cubicTo(
                    cx + w * 0.32f, baseY - h * 0.08f,
                    cx + w * 0.18f, baseY - h * 0.06f,
                    cx, baseY - h * 0.03f
                )
                cubicTo(
                    cx - w * 0.18f, baseY - h * 0.06f,
                    cx - w * 0.32f, baseY - h * 0.08f,
                    baseLeft, baseY
                )
                close()
            }
            drawPath(lotusPath, color = bodyColor)
            drawPath(lotusPath, color = highlightColor, style = Stroke(width = 1.2f))

            // Folded knees ovals
            drawOval(
                color = shadowColor,
                topLeft = Offset(cx - w * 0.36f, baseY - h * 0.06f),
                size = Size(w * 0.20f, h * 0.11f)
            )
            drawOval(
                color = shadowColor,
                topLeft = Offset(cx + w * 0.16f, baseY - h * 0.06f),
                size = Size(w * 0.20f, h * 0.11f)
            )

            // 4. Meditative Torso & Arms
            val shoulderY = cy - h * 0.08f
            val waistY = cy + h * 0.15f
            val chestWidth = w * 0.24f
            val waistWidth = w * 0.14f

            val torsoPath = Path().apply {
                moveTo(cx - chestWidth * 0.5f, shoulderY)
                cubicTo(
                    cx - chestWidth * 0.55f, cy,
                    cx - waistWidth * 0.6f, cy + h * 0.08f,
                    cx - waistWidth * 0.5f, waistY
                )
                lineTo(cx + waistWidth * 0.5f, waistY)
                cubicTo(
                    cx + waistWidth * 0.6f, cy + h * 0.08f,
                    cx + chestWidth * 0.55f, cy,
                    cx + chestWidth * 0.5f, shoulderY
                )
                close()
            }
            drawPath(torsoPath, color = bodyColor)

            // Arms resting in Dhyana Mudra (meditation mudra on lap)
            val armPath = Path().apply {
                // Left arm
                moveTo(cx - chestWidth * 0.5f, shoulderY + 4f)
                cubicTo(
                    cx - w * 0.28f, cy,
                    cx - w * 0.24f, cy + h * 0.16f,
                    cx - w * 0.06f, cy + h * 0.18f
                )
                // Hands joined in lap
                lineTo(cx + w * 0.06f, cy + h * 0.18f)
                // Right arm
                cubicTo(
                    cx + w * 0.24f, cy + h * 0.16f,
                    cx + w * 0.28f, cy,
                    cx + chestWidth * 0.5f, shoulderY + 4f
                )
                lineTo(cx + chestWidth * 0.38f, shoulderY + 8f)
                cubicTo(
                    cx + w * 0.18f, cy + 8f,
                    cx + w * 0.14f, cy + h * 0.12f,
                    cx + w * 0.04f, cy + h * 0.14f
                )
                lineTo(cx - w * 0.04f, cy + h * 0.14f)
                cubicTo(
                    cx - w * 0.14f, cy + h * 0.12f,
                    cx - w * 0.18f, cy + 8f,
                    cx - chestWidth * 0.38f, shoulderY + 8f
                )
                close()
            }
            drawPath(armPath, color = bodyColor)

            // 5. Neck, Head & Jata (Matted Hair Crown)
            val neckY = shoulderY - h * 0.04f
            val headCenterY = neckY - h * 0.07f
            val headRadius = w * 0.085f

            // Neck
            val neckPath = Path().apply {
                moveTo(cx - w * 0.05f, shoulderY + 2f)
                lineTo(cx - w * 0.045f, neckY)
                lineTo(cx + w * 0.045f, neckY)
                lineTo(cx + w * 0.05f, shoulderY + 2f)
                close()
            }
            drawPath(neckPath, color = bodyColor)

            // Head (meditative face)
            drawCircle(
                color = bodyColor,
                radius = headRadius,
                center = Offset(cx, headCenterY)
            )

            // Jata-Mukuta (Towering matted crown of hair)
            val jataBaseY = headCenterY - headRadius * 0.6f
            val jataPeakY = headCenterY - headRadius * 2.2f
            val jataPath = Path().apply {
                moveTo(cx - headRadius * 0.95f, jataBaseY)
                cubicTo(
                    cx - headRadius * 1.1f, jataBaseY - h * 0.05f,
                    cx - headRadius * 0.6f, jataPeakY + h * 0.02f,
                    cx, jataPeakY
                )
                cubicTo(
                    cx + headRadius * 0.6f, jataPeakY + h * 0.02f,
                    cx + headRadius * 1.1f, jataBaseY - h * 0.05f,
                    cx + headRadius * 0.95f, jataBaseY
                )
                close()
            }
            drawPath(jataPath, color = bodyColor)

            // Top-knot bun
            drawCircle(
                color = bodyColor,
                radius = headRadius * 0.45f,
                center = Offset(cx, jataPeakY)
            )

            // 6. Sacred Crescent Moon (Chandra) on the Jata (Left side of crown)
            val moonX = cx - headRadius * 0.65f
            val moonY = jataPeakY + h * 0.035f
            val moonRadius = w * 0.032f

            // Glowing moon crescent
            val moonPath = Path().apply {
                moveTo(moonX, moonY - moonRadius)
                cubicTo(
                    moonX - moonRadius * 1.2f, moonY - moonRadius * 0.3f,
                    moonX - moonRadius * 1.2f, moonY + moonRadius * 0.3f,
                    moonX, moonY + moonRadius
                )
                cubicTo(
                    moonX - moonRadius * 0.4f, moonY + moonRadius * 0.2f,
                    moonX - moonRadius * 0.4f, moonY - moonRadius * 0.2f,
                    moonX, moonY - moonRadius
                )
                close()
            }
            // Moon aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ShivaCyan.copy(alpha = 0.8f), Color.Transparent),
                    center = Offset(moonX - 2f, moonY),
                    radius = moonRadius * 2.2f
                )
            )
            drawPath(moonPath, color = Color(0xFFE2F3FF))

            // 7. Akasha Ganga (Celestial Stream of Grace)
            val gangaPath = Path().apply {
                moveTo(cx + headRadius * 0.3f, jataPeakY + 4f)
                cubicTo(
                    cx + headRadius * 1.4f, jataPeakY - 8f,
                    cx + headRadius * 1.8f, jataPeakY + h * 0.04f,
                    cx + headRadius * 1.3f, jataPeakY + h * 0.09f
                )
            }
            drawPath(
                path = gangaPath,
                color = ShivaCyan.copy(alpha = 0.75f),
                style = Stroke(width = 2.0f)
            )

            // 8. Trinetra (Third Eye) - subtle radiant vertical slit on brow
            val eyeY = headCenterY - 2f
            drawLine(
                color = ShivaCyan,
                start = Offset(cx, eyeY - 4f),
                end = Offset(cx, eyeY + 4f),
                strokeWidth = 2.2f
            )
            drawCircle(
                color = ShivaOrange.copy(alpha = 0.9f),
                radius = 1.6f,
                center = Offset(cx, eyeY)
            )

            // 9. Sacred Trishul (Trident) on the right side
            val trishulX = cx + w * 0.32f
            val trishulBottomY = baseY + h * 0.04f
            val trishulTopY = cy - h * 0.34f

            // Golden/Cyan Trishul Shaft
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(ShivaCyan, ShivaIndigo, ShivaIndigo.copy(alpha = 0.7f))
                ),
                start = Offset(trishulX, trishulBottomY),
                end = Offset(trishulX, trishulTopY),
                strokeWidth = 2.8f
            )

            // Trishul Central Spear
            drawLine(
                color = ShivaCyan,
                start = Offset(trishulX, trishulTopY),
                end = Offset(trishulX, trishulTopY - h * 0.06f),
                strokeWidth = 3.2f
            )

            // Left curved prong
            val leftProng = Path().apply {
                moveTo(trishulX, trishulTopY + 4f)
                cubicTo(
                    trishulX - w * 0.045f, trishulTopY,
                    trishulX - w * 0.055f, trishulTopY - h * 0.035f,
                    trishulX - w * 0.035f, trishulTopY - h * 0.055f
                )
            }
            drawPath(leftProng, color = ShivaCyan, style = Stroke(width = 2.6f))

            // Right curved prong
            val rightProng = Path().apply {
                moveTo(trishulX, trishulTopY + 4f)
                cubicTo(
                    trishulX + w * 0.045f, trishulTopY,
                    trishulX + w * 0.055f, trishulTopY - h * 0.035f,
                    trishulX + w * 0.035f, trishulTopY - h * 0.055f
                )
            }
            drawPath(rightProng, color = ShivaCyan, style = Stroke(width = 2.6f))

            // 10. Sacred Damru (Hourglass Drum) tied to Trishul
            val damruY = trishulTopY + h * 0.08f
            val damruWidth = w * 0.038f
            val damruHeight = h * 0.032f

            val damruPath = Path().apply {
                moveTo(trishulX - damruWidth, damruY - damruHeight)
                lineTo(trishulX + damruWidth, damruY + damruHeight)
                lineTo(trishulX - damruWidth, damruY + damruHeight)
                lineTo(trishulX + damruWidth, damruY - damruHeight)
                close()
            }
            drawPath(damruPath, color = ShivaOrange, style = Fill)
            drawPath(damruPath, color = Color.White.copy(alpha = 0.8f), style = Stroke(width = 1f))

            // Sacred red ribbon thread from Damru
            val ribbonPath = Path().apply {
                moveTo(trishulX, damruY)
                cubicTo(
                    trishulX - 6f, damruY + 12f,
                    trishulX - 2f, damruY + 18f,
                    trishulX - 8f, damruY + 24f
                )
            }
            drawPath(ribbonPath, color = ShivaPink, style = Stroke(width = 1.5f))
        }
    }
}
