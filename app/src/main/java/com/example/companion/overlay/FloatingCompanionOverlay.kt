package com.example.companion.overlay

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.companion.avatar.*

/**
 * FloatingCompanionOverlay:
 * The interactive on-screen floating companion widget.
 * Features:
 * - High-definition sprite rendering with authentic illustrations (Indie, Fox/Specter, Cosmic Shiv)
 * - 3-minute progressive sleep lifecycle support (0 FPS deep slumber)
 * - Smooth, clamp-safe dragging across the device screen
 * - Luxury glassmorphic quick-action dock (AI, Code, Media, Web, Apps)
 */
@Composable
fun FloatingCompanionOverlay(
    renderContext: AvatarRenderContext,
    speechCaption: String?,
    activeTaskTitle: String?,
    onAvatarTapped: () -> Unit,
    onDragDelta: (Float, Float) -> Unit,
    onEmergencyStop: () -> Unit,
    onActionAI: () -> Unit = onAvatarTapped,
    onActionCode: () -> Unit = onAvatarTapped,
    onActionMedia: () -> Unit = onAvatarTapped,
    onActionWeb: () -> Unit = onAvatarTapped
) {
    var isDockExpanded by remember { mutableStateOf(false) }

    val accentColor = when (renderContext.avatarType) {
        AvatarType.INDIE_CACOONAUT -> Color(0xFFFFB300)
        AvatarType.PHANTOM_NEKO -> Color(0xFF2979FF)
        AvatarType.COSMIC_SHIV -> Color(0xFF00E5FF)
    }

    Box(
        modifier = Modifier
            .wrapContentSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDragDelta(dragAmount.x, dragAmount.y)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. Floating Speech Bubble (Appears when companion is speaking or listening)
            AnimatedVisibility(
                visible = !speechCaption.isNullOrBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .widthIn(max = 240.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xF2080A10),
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.7f))
                ) {
                    Text(
                        text = speechCaption ?: "",
                        color = Color(0xFFF1F5F9),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }

            // 2. Active High-Fidelity Avatar Viewport
            Box(
                modifier = Modifier
                    .size(125.dp)
                    .clickable {
                        onAvatarTapped()
                        isDockExpanded = !isDockExpanded
                    },
                contentAlignment = Alignment.Center
            ) {
                when (renderContext.avatarType) {
                    AvatarType.INDIE_CACOONAUT -> {
                        IndieCacoonautRenderer(context = renderContext)
                    }
                    AvatarType.PHANTOM_NEKO -> {
                        PhantomNekoRenderer(context = renderContext)
                    }
                    AvatarType.COSMIC_SHIV -> {
                        CosmicShivRenderer(context = renderContext)
                    }
                }
            }

            // 3. Reference-Accurate Quick Action Dock (Image 3 Peek Dock)
            AnimatedVisibility(
                visible = isDockExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .widthIn(max = 240.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xF5080A10),
                    shadowElevation = 10.dp,
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, accentColor.copy(alpha = 0.8f))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Hello Shiv! 💜 How can I help?",
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DockActionButton(icon = Icons.Default.AutoAwesome, tint = accentColor, label = "AI") {
                                onActionAI()
                            }
                            DockActionButton(icon = Icons.Default.Code, tint = Color(0xFF38BDF8), label = "Code") {
                                onActionCode()
                            }
                            DockActionButton(icon = Icons.Default.PlayArrow, tint = Color(0xFFA855F7), label = "Media") {
                                onActionMedia()
                            }
                            DockActionButton(icon = Icons.Default.Language, tint = Color(0xFF34D399), label = "Web") {
                                onActionWeb()
                            }
                            DockActionButton(icon = Icons.Default.StopCircle, tint = Color(0xFFEF4444), label = "Stop") {
                                onEmergencyStop()
                                isDockExpanded = false
                            }
                        }

                        if (!activeTaskTitle.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Task: $activeTaskTitle",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.5.dp),
                                color = accentColor,
                                trackColor = Color(0xFF1E293B)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DockActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(34.dp),
        shape = CircleShape,
        color = Color(0x33FFFFFF),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, tint.copy(alpha = 0.6f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
