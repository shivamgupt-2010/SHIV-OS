package com.example.companion.avatar

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * High-Fidelity Sprite Renderer for Avatar 3: "Authentic Cosmic Shiv"
 * Renders the iridescent cyber-celestial spirit with diamond halo,
 * orbital ribbons, and starry galaxy eyes across all emotional & autonomous states.
 */
@Composable
fun CosmicShivRenderer(
    context: AvatarRenderContext,
    modifier: Modifier = Modifier
) {
    val isSlumber = context.sleepStage == InactivitySleepStage.DEEP_SLUMBER_0FPS

    val infiniteTransition = rememberInfiniteTransition(label = "cosmic_physics")
    val levitationOffset by if (!isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = -7f,
            targetValue = 7f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "levitation"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    val breathScale by if (!isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = 0.985f,
            targetValue = 1.015f,
            animationSpec = infiniteRepeatable(
                animation = tween(1900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "breath"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    val waveRadius by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "listening_wave"
    )

    // Select sprite resource based on state & slumber
    val spriteRes = when {
        isSlumber || context.state == AvatarState.SLEEPING -> {
            R.drawable.avatar_cosmic_take_a_break
        }
        context.state == AvatarState.WORKING -> {
            R.drawable.avatar_cosmic_working
        }
        context.state == AvatarState.HAPPY || context.state == AvatarState.CELEBRATING -> {
            R.drawable.avatar_cosmic_happy
        }
        context.state == AvatarState.CURIOUS -> {
            R.drawable.avatar_cosmic_curious
        }
        context.state == AvatarState.THINKING || context.state == AvatarState.PROCESSING -> {
            R.drawable.avatar_cosmic_thinking
        }
        context.state == AvatarState.ALERT || context.state == AvatarState.STRICT_COACH -> {
            R.drawable.avatar_cosmic_excited
        }
        context.state == AvatarState.LISTENING -> {
            R.drawable.avatar_cosmic_listening
        }
        context.state == AvatarState.DROWSY -> {
            R.drawable.avatar_cosmic_sleepy
        }
        context.state == AvatarState.ERROR -> {
            R.drawable.avatar_cosmic_confused
        }
        else -> {
            R.drawable.avatar_cosmic_idle
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // A. Celestial Cyan Aura & Stellar Listening Ripples
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f + levitationOffset)
            val baseRadius = size.minDimension * 0.42f

            // Galactic Aura Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x5500E5FF),
                        Color(0x337C4DFF),
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

        // B. High-Fidelity Character Sprite with Crossfade
        Box(
            modifier = Modifier
                .offset(y = levitationOffset.dp)
                .scale(breathScale)
                .fillMaxSize(0.92f),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(targetState = spriteRes, animationSpec = tween(300), label = "cosmic_sprite_fade") { targetRes ->
                Image(
                    painter = painterResource(id = targetRes),
                    contentDescription = "Cosmic Shiv Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
