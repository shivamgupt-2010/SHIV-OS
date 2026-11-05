package com.example.companion.avatar

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R

/**
 * High-Fidelity Sprite Renderer for Avatar 2: "Phantom Neko"
 * Displays the adorable 9-Tailed Mystic Celestial Fox while stationary/calm,
 * and morphs into the 3D Translucent Neon Specter Ghost when flying or dragging UI.
 */
@Composable
fun PhantomNekoRenderer(
    context: AvatarRenderContext,
    modifier: Modifier = Modifier
) {
    val isSlumber = context.sleepStage == InactivitySleepStage.DEEP_SLUMBER_0FPS

    val infiniteTransition = rememberInfiniteTransition(label = "neko_physics")
    val levitationOffset by if (!isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "levitation"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    val swayAngle by if (!isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = -2.5f,
            targetValue = 2.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "sway"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    // Only create the repeating acoustic pulse while listening; idle and
    // sleeping avatars do not keep a redundant animation clock alive.
    val waveRadius = if (context.state == AvatarState.LISTENING && !isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.35f,
            animationSpec = infiniteRepeatable(
                animation = tween(1100, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "listening_wave"
        ).value
    } else 1f

    // Select form: Morph to Ghost during rapid flight or heavy drag; Fox while calm
    val isGhostMode = context.isGhostForm ||
            context.state == AvatarState.FLYING_TO_TARGET ||
            context.state == AvatarState.TOUCH_DRAGGING

    val spriteRes = if (isGhostMode) {
        R.drawable.avatar_neko_specter_flight
    } else {
        R.drawable.avatar_neko_fox_idle
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // A. Electric Neon Aura & Listening Rings
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f + levitationOffset)
            val baseRadius = size.minDimension * 0.44f

            // Aura glow: Cyan & Neon Magenta
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x5500E5FF),
                        Color(0x33D500F9),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.3f
                ),
                radius = baseRadius * 1.3f,
                center = center
            )

            // Radiating listening wave pulses
            if (context.state == AvatarState.LISTENING && !isSlumber) {
                drawCircle(
                    color = Color(0x9900E5FF),
                    radius = baseRadius * waveRadius,
                    center = center,
                    style = Stroke(width = 2.5f)
                )
            }
        }

        // B. Character Sprite with Smooth Levitation & Crossfade
        Box(
            modifier = Modifier
                .offset(y = levitationOffset.dp)
                .rotate(if (isGhostMode) swayAngle * 2f else swayAngle)
                .fillMaxSize(0.94f),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(targetState = spriteRes, animationSpec = tween(350), label = "neko_morph_fade") { targetRes ->
                Image(
                    painter = painterResource(id = targetRes),
                    contentDescription = if (isGhostMode) "Phantom Specter Flight" else "Mystic Fox Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
