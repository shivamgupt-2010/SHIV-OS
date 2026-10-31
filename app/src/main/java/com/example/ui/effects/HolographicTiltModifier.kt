package com.example.ui.effects

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

/**
 * 3D Holographic Parallax Tilt Modifier:
 * Applies 3D spatial rotation on touch/hover and renders a specular light sheen across card borders.
 */
fun Modifier.holographicTilt(
    enabled: Boolean = true,
    maxTiltDegrees: Float = 10f,
    specularColor: Color = Color.White.copy(alpha = 0.35f),
    isReducedMotion: Boolean = false
): Modifier = composed {
    if (!enabled || isReducedMotion) return@composed this

    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }

    // Smooth return to center when released
    val animTiltX by animateFloatAsState(
        targetValue = tiltX,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "tiltX"
    )
    val animTiltY by animateFloatAsState(
        targetValue = tiltY,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "tiltY"
    )

    this
        .graphicsLayer {
            rotationX = -animTiltY * maxTiltDegrees
            rotationY = animTiltX * maxTiltDegrees
            cameraDistance = 14f * density
        }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull()
                    if (change != null && change.pressed) {
                        val boundsWidth = size.width.toFloat()
                        val boundsHeight = size.height.toFloat()
                        if (boundsWidth > 0 && boundsHeight > 0) {
                            val normalizedX = ((change.position.x / boundsWidth) * 2f - 1f).coerceIn(-1f, 1f)
                            val normalizedY = ((change.position.y / boundsHeight) * 2f - 1f).coerceIn(-1f, 1f)
                            tiltX = normalizedX
                            tiltY = normalizedY
                        }
                    } else {
                        tiltX = 0f
                        tiltY = 0f
                    }
                }
            }
        }
}

/**
 * Breathing Bioluminescent Glow Effect
 */
@Composable
fun HolographicGlowOverlay(
    modifier: Modifier = Modifier,
    glowColor: Color = Color(0xFF55E7FF),
    isReducedMotion: Boolean = false
) {
    if (isReducedMotion) return

    val infiniteTransition = rememberInfiniteTransition(label = "holographicGlow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glowColor.copy(alpha = alpha), Color.Transparent),
                center = Offset(size.width * 0.5f, size.height * 0.5f),
                radius = max(size.width, size.height) * 0.7f
            )
        )
    }
}
