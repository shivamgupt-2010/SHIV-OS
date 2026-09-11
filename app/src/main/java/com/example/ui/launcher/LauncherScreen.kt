package com.example.ui.launcher

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.agents.BottomBarTab
import com.example.ui.components.CosmicBackground
import com.example.ui.components.TrishulIcon
import com.example.ui.theme.*

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.components.LordShivaCenterpiece
import com.example.ui.components.TrishulCapsule

data class ActionCardItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val iconColor: Color
)

fun isDefaultLauncher(context: Context): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = context.packageManager.resolveActivity(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
        resolveInfo?.activityInfo?.packageName == context.packageName
    } catch (e: Exception) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherScreen(
    viewModel: LauncherViewModel,
    onNavigate: (String) -> Unit,
    onOpenSidebar: () -> Unit
) {
    val context = LocalContext.current
    var isDefaultHomePromptDismissed by remember { mutableStateOf(false) }
    var isDefaultHome by remember { mutableStateOf(isDefaultLauncher(context)) }
    var isFocusModeActive by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isDefaultHome = isDefaultLauncher(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val actionCards = listOf(
        ActionCardItem("chat", "Chat", Icons.Default.ChatBubble, ShivaCyan),
        ActionCardItem("agents", "Agents", Icons.Default.SmartToy, ShivaGreen),
        ActionCardItem("tools", "Tools", Icons.Default.Build, ShivaPurple),
        ActionCardItem("notes", "Notes", Icons.Default.Description, ShivaOrange),
        ActionCardItem("files", "Files", Icons.Default.Folder, ShivaBlue),
        ActionCardItem("more", "More", Icons.Default.Apps, ShivaPink)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -70) {
                        onNavigate("all_apps")
                    } else if (dragAmount > 70) {
                        onNavigate("chat")
                    }
                }
            }
    ) {
        // Cosmic background
        CosmicBackground(showSilhouette = false)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onOpenSidebar,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CosmicSurfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                    }

                    // Spiritual Trishul center logo badge
                    Surface(
                        color = CosmicSurfaceVariant.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TrishulIcon(size = 18.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SHIV-OS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ShivaCyan
                            )
                        }
                    }

                    IconButton(
                        onClick = { onNavigate("settings") },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CosmicSurfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary)
                    }
                }
            },
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CosmicSurface.copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BottomBarTab(icon = Icons.Default.Home, label = "Home", isSelected = true, onClick = {})
                        BottomBarTab(icon = Icons.Default.SmartToy, label = "Agents", isSelected = false, onClick = { onNavigate("agents") })
                        BottomBarTab(icon = Icons.Default.Description, label = "Notes", isSelected = false, onClick = { onNavigate("notes") })
                        BottomBarTab(icon = Icons.Default.Settings, label = "Settings", isSelected = false, onClick = { onNavigate("settings") })
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Trishul Dynamic Capsule
                TrishulCapsule(
                    isFocusModeActive = isFocusModeActive,
                    onToggleFocusMode = { isFocusModeActive = !isFocusModeActive },
                    onOpenVoiceDictate = { onNavigate("notes") },
                    onOpenVisionLens = { onNavigate("tools") }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Majestic Meditating Lord Shiva Centerpiece
                LordShivaCenterpiece(size = 140.dp)

                Spacer(modifier = Modifier.height(12.dp))

                // Brand Header: "ShivAI - Your AI. Your Way."
                Text(
                    text = "ShivAI",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your AI. Your Way.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Search Pill: "Ask ShivAI anything..."
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .clickable { onNavigate("chat") },
                    shape = RoundedCornerShape(26.dp),
                    color = CosmicSurface.copy(alpha = 0.9f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ShivaCyan.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ShivaCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Ask ShivAI anything...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = ShivaCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Default Home Launcher Request Banner - Automatically hides if already default
                if (!isDefaultHome && !isDefaultHomePromptDismissed) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        color = CosmicSurfaceVariant.copy(alpha = 0.85f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ShivaIndigo.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(ShivaIndigo.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Home, contentDescription = null, tint = ShivaCyan, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Set as Default Launcher",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Make ShivAI your default OS home screen.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = TextSecondary
                                )
                            }
                            Button(
                                onClick = {
                                    try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
                                            if (roleManager?.isRoleAvailable(RoleManager.ROLE_HOME) == true) {
                                                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                                                context.startActivity(intent)
                                            } else {
                                                context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                                            }
                                        } else {
                                            context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                                        }
                                    } catch (e: Exception) {
                                        try {
                                            context.startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                                        } catch (e2: Exception) {
                                            e2.printStackTrace()
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ShivaIndigo),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Enable", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }

                // 6 Action Cards Grid (3 columns x 2 rows)
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ActionCard(actionCards[0], modifier = Modifier.weight(1f)) { onNavigate("chat") }
                        ActionCard(actionCards[1], modifier = Modifier.weight(1f)) { onNavigate("agents") }
                        ActionCard(actionCards[2], modifier = Modifier.weight(1f)) { onNavigate("tools") }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ActionCard(actionCards[3], modifier = Modifier.weight(1f)) { onNavigate("notes") }
                        ActionCard(actionCards[4], modifier = Modifier.weight(1f)) {
                            try {
                                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                    type = "*/*"
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                onNavigate("tools")
                            }
                        }
                        ActionCard(actionCards[5], modifier = Modifier.weight(1f)) { onNavigate("all_apps") }
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}

@Composable
fun ActionCard(
    item: ActionCardItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = CosmicSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(item.iconColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = TextPrimary
            )
        }
    }
}
