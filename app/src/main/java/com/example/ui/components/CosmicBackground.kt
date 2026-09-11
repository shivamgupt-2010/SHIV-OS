package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CosmicBackground
import com.example.ui.theme.ShivaBlue
import com.example.ui.theme.ShivaCyan
import com.example.ui.theme.ShivaIndigo
import com.example.ui.theme.ShivaOrange
import com.example.ui.theme.ShivaPurple

/**
 * Renders the cosmic Lord Shiva meditation atmosphere with starry sky,
 * glowing nebula, distant mountain ridges, and meditative silhouette with Trishul.
 */
@Composable
fun CosmicBackground(
    modifier: Modifier = Modifier,
    showSilhouette: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF06080E),
                        Color(0xFF0B101D),
                        Color(0xFF131A2D),
                        Color(0xFF1F1B38),
                        Color(0xFF2C1635),
                        Color(0xFF1A1224),
                        CosmicBackground
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw glowing nebula aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ShivaIndigo.copy(alpha = 0.25f),
                        ShivaPurple.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.5f, height * 0.22f),
                    radius = width * 0.65f
                )
            )

            // 2. Horizon warm glow (sunrise behind mountains)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ShivaOrange.copy(alpha = 0.18f),
                        ShivaPurple.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.75f, height * 0.32f),
                    radius = width * 0.45f
                )
            )

            // 3. Stars in the upper cosmos
            val starPositions = listOf(
                Offset(width * 0.12f, height * 0.06f),
                Offset(width * 0.28f, height * 0.03f),
                Offset(width * 0.45f, height * 0.08f),
                Offset(width * 0.68f, height * 0.04f),
                Offset(width * 0.85f, height * 0.07f),
                Offset(width * 0.92f, height * 0.14f),
                Offset(width * 0.08f, height * 0.18f),
                Offset(width * 0.22f, height * 0.15f),
                Offset(width * 0.38f, height * 0.19f),
                Offset(width * 0.82f, height * 0.21f),
                Offset(width * 0.15f, height * 0.28f),
                Offset(width * 0.88f, height * 0.30f)
            )
            starPositions.forEachIndexed { i, pos ->
                val starAlpha = if (i % 2 == 0) 0.8f else 0.5f
                val radius = if (i % 3 == 0) 2.2f else 1.4f
                drawCircle(color = Color.White.copy(alpha = starAlpha), radius = radius, center = pos)
            }

            if (showSilhouette) {
                val headerBaseY = height * 0.34f

                // 4. Distant Mountain Silhouettes
                val mountainPath = Path().apply {
                    moveTo(0f, headerBaseY)
                    lineTo(width * 0.2f, headerBaseY - 45f)
                    lineTo(width * 0.45f, headerBaseY - 15f)
                    lineTo(width * 0.7f, headerBaseY - 55f)
                    lineTo(width * 0.85f, headerBaseY - 25f)
                    lineTo(width, headerBaseY - 40f)
                    lineTo(width, headerBaseY + 60f)
                    lineTo(0f, headerBaseY + 60f)
                    close()
                }
                drawPath(
                    path = mountainPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E162B).copy(alpha = 0.7f), Color.Transparent),
                        startY = headerBaseY - 60f,
                        endY = headerBaseY + 60f
                    )
                )

                // 5. Lord Shiva in Meditation Silhouette (Right side of cosmic sky)
                val shivaCenterX = width * 0.78f
                val shivaCenterY = headerBaseY - 40f
                val shivaColor = Color(0xFF0F1424)

                // Head & Jata (Top knot)
                drawCircle(color = shivaColor, radius = 22f, center = Offset(shivaCenterX, shivaCenterY - 38f))
                drawCircle(color = shivaColor, radius = 13f, center = Offset(shivaCenterX, shivaCenterY - 56f)) // Jata

                // Crescent Moon on Jata
                drawCircle(
                    color = ShivaCyan.copy(alpha = 0.85f),
                    radius = 5f,
                    center = Offset(shivaCenterX + 10f, shivaCenterY - 58f)
                )

                // Shoulders & Torso in lotus pose
                val bodyPath = Path().apply {
                    moveTo(shivaCenterX - 18f, shivaCenterY - 20f)
                    lineTo(shivaCenterX + 18f, shivaCenterY - 20f)
                    lineTo(shivaCenterX + 38f, shivaCenterY + 18f)
                    lineTo(shivaCenterX - 38f, shivaCenterY + 18f)
                    close()
                }
                drawPath(path = bodyPath, color = shivaColor)

                // Lotus legs base
                drawOval(
                    color = shivaColor,
                    topLeft = Offset(shivaCenterX - 45f, shivaCenterY + 10f),
                    size = androidx.compose.ui.geometry.Size(90f, 32f)
                )

                // Trishul (Trident) standing beside
                val trishulX = shivaCenterX - 32f
                val trishulTopY = shivaCenterY - 75f
                val trishulBottomY = shivaCenterY + 28f

                // Shaft
                drawLine(
                    color = ShivaCyan.copy(alpha = 0.9f),
                    start = Offset(trishulX, trishulBottomY),
                    end = Offset(trishulX, trishulTopY),
                    strokeWidth = 3f
                )
                // Middle prong
                drawLine(
                    color = ShivaCyan.copy(alpha = 0.9f),
                    start = Offset(trishulX, trishulTopY),
                    end = Offset(trishulX, trishulTopY - 14f),
                    strokeWidth = 3f
                )
                // Left curve prong
                val leftProng = Path().apply {
                    moveTo(trishulX, trishulTopY + 4f)
                    cubicTo(trishulX - 10f, trishulTopY, trishulX - 12f, trishulTopY - 8f, trishulX - 8f, trishulTopY - 12f)
                }
                drawPath(leftProng, color = ShivaCyan.copy(alpha = 0.9f), style = Stroke(width = 3f))

                // Right curve prong
                val rightProng = Path().apply {
                    moveTo(trishulX, trishulTopY + 4f)
                    cubicTo(trishulX + 10f, trishulTopY, trishulX + 12f, trishulTopY - 8f, trishulX + 8f, trishulTopY - 12f)
                }
                drawPath(rightProng, color = ShivaCyan.copy(alpha = 0.9f), style = Stroke(width = 3f))

                // Damru (small hourglass)
                val damruY = trishulTopY + 16f
                val damruPath = Path().apply {
                    moveTo(trishulX - 5f, damruY - 4f)
                    lineTo(trishulX + 5f, damruY + 4f)
                    lineTo(trishulX - 5f, damruY + 4f)
                    lineTo(trishulX + 5f, damruY - 4f)
                    close()
                }
                drawPath(damruPath, color = ShivaOrange.copy(alpha = 0.9f), style = Fill)
            }
        }
    }
}
