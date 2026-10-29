package com.example.ui.security

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ShivTheme
import kotlin.math.sqrt

/**
 * Interactive 3x3 Dot Grid Pattern Lock View.
 * Supports smooth touch drag, connected line drawing, dot pulse, and error highlighting.
 */
@Composable
fun PatternLockView(
    modifier: Modifier = Modifier,
    hasError: Boolean = false,
    onPatternComplete: (String) -> Unit
) {
    var selectedDots by remember { mutableStateOf<List<Int>>(emptyList()) }
    var currentTouchPoint by remember { mutableStateOf<Offset?>(null) }
    val density = LocalDensity.current

    val primaryColor = ShivTheme.colors.primary
    val errorColor = MaterialTheme.colorScheme.error
    val dotColor = if (hasError) errorColor else primaryColor
    val lineColor = if (hasError) errorColor.copy(alpha = 0.8f) else primaryColor.copy(alpha = 0.85f)
    val inactiveDotColor = ShivTheme.colors.cardBorder.copy(alpha = 0.5f)

    LaunchedEffect(hasError) {
        if (hasError) {
            // Keep error color visible briefly, then reset pattern
            kotlinx.coroutines.delay(800)
            selectedDots = emptyList()
            currentTouchPoint = null
        }
    }

    Box(
        modifier = modifier
            .size(280.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        selectedDots = emptyList()
                        currentTouchPoint = offset
                        val dot = getHitDot(offset, size.width.toFloat(), size.height.toFloat())
                        if (dot != null) {
                            selectedDots = listOf(dot)
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        currentTouchPoint = change.position
                        val dot = getHitDot(change.position, size.width.toFloat(), size.height.toFloat())
                        if (dot != null && !selectedDots.contains(dot)) {
                            selectedDots = selectedDots + dot
                        }
                    },
                    onDragEnd = {
                        currentTouchPoint = null
                        if (selectedDots.size >= 3) {
                            val patternString = selectedDots.joinToString("")
                            onPatternComplete(patternString)
                        } else {
                            selectedDots = emptyList()
                        }
                    },
                    onDragCancel = {
                        currentTouchPoint = null
                        selectedDots = emptyList()
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellWidth = size.width / 3f
            val cellHeight = size.height / 3f
            val dotRadius = 8.dp.toPx()
            val activeDotRadius = 14.dp.toPx()
            val hitRadius = 28.dp.toPx()

            // 1. Calculate centers for all 9 dots
            val dotCenters = (0 until 9).map { index ->
                val col = index % 3
                val row = index / 3
                Offset(
                    x = cellWidth * col + cellWidth / 2f,
                    y = cellHeight * row + cellHeight / 2f
                )
            }

            // 2. Draw lines between selected dots
            if (selectedDots.size > 1) {
                for (i in 0 until selectedDots.size - 1) {
                    val start = dotCenters[selectedDots[i]]
                    val end = dotCenters[selectedDots[i + 1]]
                    drawLine(
                        color = lineColor,
                        start = start,
                        end = end,
                        strokeWidth = 6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // 3. Draw line from last selected dot to current touch position
            if (selectedDots.isNotEmpty() && currentTouchPoint != null) {
                val lastDotCenter = dotCenters[selectedDots.last()]
                drawLine(
                    color = lineColor,
                    start = lastDotCenter,
                    end = currentTouchPoint!!,
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 4. Draw all 9 dots (outer glow if active, inner circle)
            dotCenters.forEachIndexed { index, center ->
                val isSelected = selectedDots.contains(index)
                if (isSelected) {
                    drawCircle(
                        color = dotColor.copy(alpha = 0.25f),
                        radius = hitRadius,
                        center = center
                    )
                    drawCircle(
                        color = dotColor,
                        radius = activeDotRadius,
                        center = center
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = center
                    )
                } else {
                    drawCircle(
                        color = inactiveDotColor,
                        radius = dotRadius,
                        center = center
                    )
                }
            }
        }
    }
}

private fun getHitDot(point: Offset, totalWidth: Float, totalHeight: Float): Int? {
    val cellWidth = totalWidth / 3f
    val cellHeight = totalHeight / 3f
    val hitRadius = cellWidth * 0.38f

    for (index in 0 until 9) {
        val col = index % 3
        val row = index / 3
        val centerX = cellWidth * col + cellWidth / 2f
        val centerY = cellHeight * row + cellHeight / 2f

        val dx = point.x - centerX
        val dy = point.y - centerY
        val dist = sqrt(dx * dx + dy * dy)
        if (dist <= hitRadius) {
            return index
        }
    }
    return null
}
