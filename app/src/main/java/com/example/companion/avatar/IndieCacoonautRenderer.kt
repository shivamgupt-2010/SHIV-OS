package com.example.companion.avatar

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * High-Fidelity Sprite Renderer for Avatar 1: "Indie Cacoonaut"
 * Renders the handcrafted sacred Cocoon & Celestial Diya Spirit illustrations
 * with smooth sine-wave levitation, ambient golden aura, and listening wave pulses.
 */
@Composable
fun IndieCacoonautRenderer(
    context: AvatarRenderContext,
    modifier: Modifier = Modifier
) {
    val isSlumber = context.sleepStage == InactivitySleepStage.DEEP_SLUMBER_0FPS

    // 1. Floating Levitation Sine-Wave Physics
    val infiniteTransition = rememberInfiniteTransition(label = "indie_physics")
    val levitationOffset by if (!isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = -6f,
            targetValue = 6f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "levitation"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    // 2. Breathing / Flame Pulsation
    val breathScale by if (!isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = 0.985f,
            targetValue = 1.015f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "breath"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    // 3. Listening Acoustic Wave Pulse
    // Keep the repeating wave completely out of the composition unless we are
    // actively listening. This avoids an unnecessary animation clock at idle.
    val waveRadius = if (context.state == AvatarState.LISTENING && !isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.35f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "listening_wave"
        ).value
    } else 1f

    // Select sprite resource based on state & slumber lifecycle
    val spriteRes = when {
        isSlumber || context.state == AvatarState.SLEEPING -> {
            R.drawable.avatar_indie_cocoon_slumber
        }
        context.state == AvatarState.WORKING -> {
            R.drawable.avatar_indie_holo
        }
        context.state == AvatarState.HAPPY || context.state == AvatarState.CELEBRATING -> {
            R.drawable.avatar_indie_happy
        }
        context.state == AvatarState.CURIOUS -> {
            R.drawable.avatar_indie_curious
        }
        context.state == AvatarState.THINKING || context.state == AvatarState.PROCESSING -> {
            R.drawable.avatar_indie_thinking
        }
        context.state == AvatarState.ALERT || context.state == AvatarState.STRICT_COACH -> {
            R.drawable.avatar_indie_excited
        }
        context.state == AvatarState.LISTENING -> {
            R.drawable.avatar_indie_listening
        }
        context.state == AvatarState.DROWSY -> {
            R.drawable.avatar_indie_sleepy
        }
        context.state == AvatarState.ERROR -> {
            R.drawable.avatar_indie_confused
        }
        else -> {
            R.drawable.avatar_indie_idle
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // A. Ambient Golden Aura & Listening Waves
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f + levitationOffset)
            val baseRadius = size.minDimension * 0.42f

            // Ambient warmth glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x55FFB300),
                        Color(0x22E65100),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.25f
                ),
                radius = baseRadius * 1.25f,
                center = center
            )

            // Radiating listening wave pulses
            if (context.state == AvatarState.LISTENING && !isSlumber) {
                drawCircle(
                    color = Color(0x88FFD54F),
                    radius = baseRadius * waveRadius,
                    center = center,
                    style = Stroke(width = 2.5f)
                )
            }
        }

        // B. High-Fidelity Character Sprite
        Box(
            modifier = Modifier
                .offset(y = levitationOffset.dp)
                .scale(breathScale)
                .fillMaxSize(0.92f),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(targetState = spriteRes, animationSpec = tween(300), label = "indie_sprite_fade") { targetRes ->
                if (targetRes == R.drawable.avatar_indie_cocoon_slumber) {
                    // Traditional Sacred Cocoon in elegant golden circular frame
                    Image(
                        painter = painterResource(id = targetRes),
                        contentDescription = "Sacred Cocoon Slumber",
                        modifier = Modifier
                            .fillMaxSize(0.85f)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = targetRes),
                        contentDescription = "Indie Cacoonaut Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
