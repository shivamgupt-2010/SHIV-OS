package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ShivaCyan
import com.example.ui.theme.ShivaIndigo
import com.example.ui.theme.ShivaPink
import com.example.ui.theme.ShivaPurple

/**
 * Renders the spiritual & modern ShivAI Trishul brand icon with electric gradient
 */
@Composable
fun TrishulIcon(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    tintGradient: List<Color> = listOf(ShivaCyan, ShivaIndigo, ShivaPurple, ShivaPink)
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val brush = Brush.verticalGradient(
            colors = tintGradient,
            startY = 0f,
            endY = h
        )

        val strokeW = w * 0.1f

        // Center shaft
        drawLine(
            brush = brush,
            start = Offset(w * 0.5f, h * 0.95f),
            end = Offset(w * 0.5f, h * 0.1f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )

        // Center spear tip
        val centerTip = Path().apply {
            moveTo(w * 0.5f, 0f)
            lineTo(w * 0.42f, h * 0.22f)
            lineTo(w * 0.58f, h * 0.22f)
            close()
        }
        drawPath(path = centerTip, brush = brush)

        // Left flame / prong curve
        val leftProng = Path().apply {
            moveTo(w * 0.5f, h * 0.52f)
            cubicTo(w * 0.22f, h * 0.48f, w * 0.12f, h * 0.32f, w * 0.16f, h * 0.18f)
            cubicTo(w * 0.22f, h * 0.28f, w * 0.32f, h * 0.36f, w * 0.44f, h * 0.38f)
        }
        drawPath(path = leftProng, brush = brush, style = Stroke(width = strokeW * 0.9f, cap = StrokeCap.Round))

        // Right flame / prong curve
        val rightProng = Path().apply {
            moveTo(w * 0.5f, h * 0.52f)
            cubicTo(w * 0.78f, h * 0.48f, w * 0.88f, h * 0.32f, w * 0.84f, h * 0.18f)
            cubicTo(w * 0.78f, h * 0.28f, w * 0.68f, h * 0.36f, w * 0.56f, h * 0.38f)
        }
        drawPath(path = rightProng, brush = brush, style = Stroke(width = strokeW * 0.9f, cap = StrokeCap.Round))
    }
}
