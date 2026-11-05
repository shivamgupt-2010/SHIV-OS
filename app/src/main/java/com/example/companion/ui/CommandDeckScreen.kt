package com.example.companion.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsVoice
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.companion.avatar.AvatarRenderContext
import com.example.companion.avatar.AvatarState
import com.example.companion.avatar.AvatarType
import com.example.companion.avatar.PremiumAvatar3DScene
import com.example.companion.overlay.FloatingAvatarService
import com.example.core.accessibility.ShivAIAccessibilityService

/**
 * The companion's control centre. It intentionally contains no perpetual UI
 * animations: the avatar owns its lifecycle-aware animation budget, while this
 * surface only updates in response to input or system state.
 */
@Composable
fun CommandDeckScreen(
    currentAvatar: AvatarType,
    onNavigateToWorkflow: () -> Unit,
    onNavigateToStudio: () -> Unit,
    onNavigateToMemory: () -> Unit,
    onNavigateToTelemetry: () -> Unit
) {
    val context = LocalContext.current
    var isOverlayEnabled by remember { mutableStateOf(false) }
    var isListeningPreview by remember { mutableStateOf(false) }
    val hasAccessibility = remember { ShivAIAccessibilityService.isServiceEnabled(context) }
    val hasOverlayPermission = remember {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
    }

    CompanionBackground {
        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(12.dp)) }
            item {
                CompanionTopBar(
                    title = "ShivAI Companion",
                    subtitle = "Private, on-device assistance",
                    action = {
                        IconButton(onClick = onNavigateToStudio) {
                            Icon(Icons.Default.Palette, contentDescription = "Open Persona Studio", tint = CompanionPalette.Cyan)
                        }
                    }
                )
            }
            item {
                CommandAvatarStage(
                    currentAvatar = currentAvatar,
                    isListening = isListeningPreview,
                    onVoicePressed = { isListeningPreview = !isListeningPreview }
                )
            }
            item {
                OverlayControl(
                    isEnabled = isOverlayEnabled,
                    onChanged = { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                            context.startActivity(
                                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                            )
                        } else if (enabled) {
                            FloatingAvatarService.start(context, currentAvatar)
                            isOverlayEnabled = true
                        } else {
                            FloatingAvatarService.stop(context)
                            isOverlayEnabled = false
                        }
                    }
                )
            }
            item { CompanionSectionLabel("System readiness") }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReadinessCard(
                        label = "Accessibility",
                        ready = hasAccessibility,
                        icon = Icons.Default.Accessibility,
                        modifier = Modifier.weight(1f)
                    ) { ShivAIAccessibilityService.promptEnableAccessibility(context) }
                    ReadinessCard(
                        label = "Overlay",
                        ready = hasOverlayPermission,
                        icon = Icons.Default.AutoAwesome,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
                        }
                    }
                    ReadinessCard(
                        label = "Voice",
                        ready = true,
                        icon = Icons.Default.SettingsVoice,
                        modifier = Modifier.weight(1f),
                        onClick = {}
                    )
                }
            }
            item { CompanionSectionLabel("Capabilities") }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CompanionActionTile(
                        title = "Coding Pilot",
                        description = "Plan and run safe device workflows",
                        icon = Icons.Default.Code,
                        accent = CompanionPalette.Cyan,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToWorkflow
                    )
                    CompanionActionTile(
                        title = "Media Lab",
                        description = "Transcribe and shape media tasks",
                        icon = Icons.Default.Movie,
                        accent = CompanionPalette.Violet,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToWorkflow
                    )
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CompanionActionTile(
                        title = "Focus Coach",
                        description = "Guard focused work sessions",
                        icon = Icons.Default.Timer,
                        accent = CompanionPalette.Amber,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToWorkflow
                    )
                    CompanionActionTile(
                        title = "Memory Vault",
                        description = "Review what ShivAI remembers",
                        icon = Icons.Default.Memory,
                        accent = CompanionPalette.Green,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToMemory
                    )
                }
            }
            item {
                CompanionSurface(onClick = onNavigateToTelemetry, accent = CompanionPalette.Violet) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = CompanionPalette.Violet, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Efficiency monitor", color = CompanionPalette.Text, fontWeight = FontWeight.SemiBold)
                            Text("Review sleep state, rendering, and wake-lock health", color = CompanionPalette.Muted, fontSize = 12.sp)
                        }
                        CompanionStatusPill("Healthy", CompanionPalette.Green)
                    }
                }
            }
            item { Spacer(Modifier.height(28.dp)) }
        }
    }
}

@Composable
private fun CommandAvatarStage(
    currentAvatar: AvatarType,
    isListening: Boolean,
    onVoicePressed: () -> Unit
) {
    CompanionSurface(
        modifier = Modifier.fillMaxWidth(),
        accent = if (isListening) CompanionPalette.Cyan else CompanionPalette.Line,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(278.dp),
            contentAlignment = Alignment.Center
        ) {
            PremiumAvatar3DScene(
                renderContext = AvatarRenderContext(
                    avatarType = currentAvatar,
                    state = if (isListening) AvatarState.LISTENING else AvatarState.IDLE
                ),
                modifier = Modifier.size(218.dp)
            )
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                CompanionStatusPill(
                    label = if (isListening) "Listening" else "Ready",
                    color = if (isListening) CompanionPalette.Cyan else CompanionPalette.Green,
                    leading = if (isListening) Icons.Default.GraphicEq else Icons.Default.CheckCircle
                )
                Spacer(Modifier.height(8.dp))
                Text(currentAvatar.title, color = CompanionPalette.Text, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("On-device companion", color = CompanionPalette.Muted, fontSize = 11.sp)
            }
            CompanionStatusPill(
                label = "Tap to ${if (isListening) "stop" else "preview voice"}",
                color = CompanionPalette.Violet,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .clickable(onClick = onVoicePressed)
            )
        }
    }
}

@Composable
private fun OverlayControl(isEnabled: Boolean, onChanged: (Boolean) -> Unit) {
    CompanionSurface(accent = if (isEnabled) CompanionPalette.Cyan else CompanionPalette.Line) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(CompanionPalette.Cyan.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CompanionPalette.Cyan)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Floating companion", color = CompanionPalette.Text, fontWeight = FontWeight.SemiBold)
                Text("Overlays other apps and sleeps after inactivity", color = CompanionPalette.Muted, fontSize = 12.sp)
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CompanionPalette.Ink,
                    checkedTrackColor = CompanionPalette.Cyan,
                    uncheckedThumbColor = CompanionPalette.Muted,
                    uncheckedTrackColor = CompanionPalette.PanelStrong
                )
            )
        }
    }
}

@Composable
private fun ReadinessCard(
    label: String,
    ready: Boolean,
    icon: ImageVector,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val color = if (ready) CompanionPalette.Green else CompanionPalette.Amber
    CompanionSurface(
        modifier = modifier,
        accent = color,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
        onClick = onClick
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(9.dp))
        Text(label, color = CompanionPalette.Text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Spacer(Modifier.height(3.dp))
        Text(if (ready) "Ready" else "Set up", color = color, fontSize = 10.sp)
    }
}
