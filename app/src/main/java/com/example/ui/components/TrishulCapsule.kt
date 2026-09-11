package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Trishul Capsule: Dynamic Island style floating widget for SHIV-OS.
 * Shows system health, active AI agent swarm status, and provides quick actions.
 */
@Composable
fun TrishulCapsule(
    modifier: Modifier = Modifier,
    isFocusModeActive: Boolean,
    onToggleFocusMode: () -> Unit,
    onOpenVoiceDictate: () -> Unit,
    onOpenVisionLens: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "capsule_pulse")

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (isExpanded) 22.dp else 30.dp))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(if (isExpanded) 22.dp else 30.dp),
        color = CosmicSurface.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    ShivaCyan.copy(alpha = pulseGlow),
                    ShivaIndigo.copy(alpha = 0.4f),
                    ShivaPurple.copy(alpha = pulseGlow)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Minimized Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(ShivaCyan.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        TrishulIcon(size = 16.dp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isFocusModeActive) "Tandav Focus Active" else "ShivAI Capsule",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isFocusModeActive) ShivaOrange else ShivaCyan
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isFocusModeActive) ShivaOrange else ShivaGreen)
                            )
                        }
                        Text(
                            text = if (isFocusModeActive) "Cosmic silence • Deep Work" else "All 6 Agents Standing By • Online",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Expanded Quick Controls Drawer
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = CosmicCardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Action 1: Tandav Focus
                        CapsuleActionItem(
                            icon = Icons.Default.SelfImprovement,
                            title = if (isFocusModeActive) "End Focus" else "Tandav Focus",
                            tint = ShivaOrange,
                            isActive = isFocusModeActive,
                            onClick = onToggleFocusMode
                        )

                        // Action 2: Shiv Dictate
                        CapsuleActionItem(
                            icon = Icons.Default.Mic,
                            title = "Shiv Dictate",
                            tint = ShivaCyan,
                            isActive = false,
                            onClick = onOpenVoiceDictate
                        )

                        // Action 3: Screen Vision Lens
                        CapsuleActionItem(
                            icon = Icons.Default.CameraAlt,
                            title = "Vision Lens",
                            tint = ShivaPurple,
                            isActive = false,
                            onClick = onOpenVisionLens
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CapsuleActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    tint: Color,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (isActive) tint.copy(alpha = 0.25f) else CosmicSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) tint else CosmicCardBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = tint, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = title, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium), color = TextPrimary)
        }
    }
}
