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
 * - Direct procedural rendering of the 3 chosen avatars (Indie, Cat/Ghost, Cosmic Shiv)
 * - 3-minute progressive sleep lifecycle support
 * - Draggable positioning across the phone screen
 * - Real-time speech bubble & autonomous task HUD
 */
@Composable
fun FloatingCompanionOverlay(
    renderContext: AvatarRenderContext,
    speechCaption: String?,
    activeTaskTitle: String?,
    onAvatarTapped: () -> Unit,
    onDragDelta: (Float, Float) -> Unit,
    onEmergencyStop: () -> Unit
) {
    var isHudExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
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
            // 1. Floating Speech Bubble (Appears when companion is speaking or thinking)
            AnimatedVisibility(
                visible = !speechCaption.isNullOrBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .widthIn(max = 240.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xEE0F172A),
                    shadowElevation = 6.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F0FF))
                ) {
                    Text(
                        text = speechCaption ?: "",
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            // 2. Active Themed Avatar Viewport
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clickable {
                        onAvatarTapped()
                        isHudExpanded = !isHudExpanded
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

            // 3. Autonomous Task HUD / Kill Switch
            AnimatedVisibility(
                visible = isHudExpanded || !activeTaskTitle.isNullOrBlank(),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Surface(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .widthIn(max = 260.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFA0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.2f.dp, Color(0xFF38BDF8))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ShivAI Companion",
                                color = Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = onEmergencyStop,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.StopCircle,
                                    contentDescription = "Stop Task",
                                    tint = Color(0xFFEF4444)
                                )
                            }
                        }

                        if (!activeTaskTitle.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Executing: $activeTaskTitle",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().height(3.dp),
                                color = Color(0xFF00F0FF),
                                trackColor = Color(0xFF1E293B)
                            )
                        }
                    }
                }
            }
        }
    }
}
