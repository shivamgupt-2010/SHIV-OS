package com.example.ui.ambient

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Ambient AI Presence Indicator
 * Can be used as a standalone indicator or a background glow.
 */
@Composable
fun AmbientActivityGlow(
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    isThinking: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive || isThinking) 1.2f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isThinking) 800 else 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isActive) 0.6f else if (isThinking) 0.8f else 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isThinking) 1000 else 3000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val color = when {
        isThinking -> MaterialTheme.colorScheme.primary
        isActive -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Box(
        modifier = modifier
            .scale(scale)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = alpha),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
fun SiriLikeWaveform(modifier: Modifier = Modifier, isSpeaking: Boolean) {
    // A simple representation of a voice waveform
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    
    Row(
        modifier = modifier.height(40.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val count = 5
        for (i in 0 until count) {
            val height by infiniteTransition.animateFloat(
                initialValue = 8f,
                targetValue = if (isSpeaking) (20..38).random().toFloat() else 8f,
                animationSpec = infiniteRepeatable(
                    animation = tween((300..600).random(), easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "waveform_height"
            )
            
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(height.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
