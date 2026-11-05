package com.example.companion.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.companion.avatar.*
import com.example.companion.overlay.FloatingAvatarService
import com.example.core.accessibility.ShivAIAccessibilityService

/**
 * CommandDeckScreen:
 * The Central Command & Control Deck for ShivAI Companion.
 * Features the large interactive Avatar Stage, System Readiness Matrix,
 * Autonomy Mode controls, and Live Voice Orb.
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
    var selectedAutonomyMode by remember { mutableStateOf("Full Autonomy") }

    val hasA11yPermission = remember { ShivAIAccessibilityService.isServiceEnabled(context) }
    val hasOverlayPermission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF020617), Color(0xFF0F172A), Color(0xFF020617))
                )
            )
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SHIV AI COMPANION",
                    color = Color(0xFF00F0FF),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Wake Word: 'Om Namas Shivaya'",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }
            IconButton(onClick = onNavigateToStudio) {
                Icon(Icons.Default.Palette, contentDescription = "Studio", tint = Color(0xFF38BDF8))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Interactive Avatar Stage Viewport
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF1E293B))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Render chosen avatar with interactive tap
                val renderContext = AvatarRenderContext(avatarType = currentAvatar)
                Box(modifier = Modifier.size(170.dp)) {
                    when (currentAvatar) {
                        AvatarType.INDIE_CACOONAUT -> IndieCacoonautRenderer(renderContext)
                        AvatarType.PHANTOM_NEKO -> PhantomNekoRenderer(renderContext)
                        AvatarType.COSMIC_SHIV -> CosmicShivRenderer(renderContext)
                    }
                }

                // Overlay Status Badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xCC020617)
                ) {
                    Text(
                        text = currentAvatar.title,
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Floating Overlay Master Toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Floating Smart Avatar", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Show avatar over all apps with 3-min sleep governor", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
                Switch(
                    checked = isOverlayEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                                context.startActivity(intent)
                            } else {
                                FloatingAvatarService.start(context)
                                isOverlayEnabled = true
                            }
                        } else {
                            FloatingAvatarService.stop(context)
                            isOverlayEnabled = false
                        }
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00F0FF))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. System Readiness Matrix
        Text("SYSTEM READINESS", color = Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReadinessChip("Accessibility", hasA11yPermission) {
                ShivAIAccessibilityService.promptEnableAccessibility(context)
            }
            ReadinessChip("Overlay Window", hasOverlayPermission) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
                }
            }
            ReadinessChip("Neural Voice", true) {}
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Quick Action Control Grid
        Text("SUPER-AGENT PERSONAS", color = Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionTile("Coding Pilot", Icons.Default.Code, Color(0xFF38BDF8), Modifier.weight(1f)) {
                onNavigateToWorkflow()
            }
            ActionTile("Media Editor", Icons.Default.Movie, Color(0xFFEC4899), Modifier.weight(1f)) {
                onNavigateToWorkflow()
            }
            ActionTile("Strict Coach", Icons.Default.Timer, Color(0xFFF59E0B), Modifier.weight(1f)) {
                onNavigateToWorkflow()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionTile("Memory Vault", Icons.Default.Storage, Color(0xFF10B981), Modifier.weight(1f)) {
                onNavigateToMemory()
            }
            ActionTile("Battery Stats", Icons.Default.BatteryChargingFull, Color(0xFFA855F7), Modifier.weight(1f)) {
                onNavigateToTelemetry()
            }
        }
    }
}

@Composable
private fun ReadinessChip(label: String, isReady: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isReady) Color(0x2210B981) else Color(0x22EF4444),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isReady) Color(0xFF10B981) else Color(0xFFEF4444))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isReady) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isReady) Color(0xFF10B981) else Color(0xFFEF4444),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ActionTile(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
