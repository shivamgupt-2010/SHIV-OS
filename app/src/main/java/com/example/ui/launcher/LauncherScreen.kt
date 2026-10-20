package com.example.ui.launcher

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.core.launcher.AppInfo
import com.example.core.launcher.ComplexityMode
import com.example.core.launcher.ProfileType
import com.example.data.local.entity.LauncherLayoutEntity
import com.example.ui.components.CosmicBackground
import com.example.ui.components.TrishulIcon
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

fun isDefaultLauncher(context: Context): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.resolveActivity(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        }
        resolveInfo?.activityInfo?.packageName == context.packageName
    } catch (e: Exception) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ShivOSLiteLauncherScreen(
    viewModel: LauncherViewModel,
    onNavigate: (String) -> Unit,
    onOpenSidebar: () -> Unit
) {
    val context = LocalContext.current
    var isDefaultHome by remember { mutableStateOf(isDefaultLauncher(context)) }
    val installedApps by viewModel.installedApps.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()
    val activeComplexity by viewModel.activeComplexityMode.collectAsState()
    val activeProfile by viewModel.activeProfile.collectAsState()
    val layoutItems by viewModel.layoutItems.collectAsState()
    val dockItems by viewModel.dockItems.collectAsState()

    var showHomeEditModal by remember { mutableStateOf(false) }
    var selectedAppForOptions by remember { mutableStateOf<AppInfo?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    // Live time & date
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFmt = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTime = timeFmt.format(now)
            currentDate = dateFmt.format(now)
            kotlinx.coroutines.delay(1000)
        }
    }

    // Breathing glow animation respecting reduced motion
    val infiniteTransition = rememberInfiniteTransition(label = "GlowTransition")
    val glowScale by if (currentTheme.isReducedMotion) {
        remember { mutableFloatStateOf(1.0f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0.97f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "GlowScale"
        )
    }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -60) {
                        onNavigate("all_apps")
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        showHomeEditModal = true
                    }
                )
            }
    ) {
        // Dynamic Cosmic Background
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
                    // Left Menu Pill
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onOpenSidebar),
                        color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.7f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, ShivTheme.colors.cardBorder.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = ShivTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ShivOS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ShivTheme.colors.primary
                            )
                        }
                    }

                    // Center Mode Indicator Pill (Clickable to switch complexity)
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                val nextMode = when (activeComplexity) {
                                    ComplexityMode.SIMPLE -> ComplexityMode.BALANCED
                                    ComplexityMode.BALANCED -> ComplexityMode.ADVANCED
                                    ComplexityMode.ADVANCED -> ComplexityMode.SIMPLE
                                }
                                viewModel.setComplexityMode(nextMode)
                                Toast.makeText(context, "Switched to ${nextMode.title}", Toast.LENGTH_SHORT).show()
                            },
                        color = ShivTheme.colors.surface.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, ShivTheme.colors.primary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TrishulIcon(size = 16.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = activeComplexity.title.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                ),
                                color = ShivTheme.colors.primary
                            )
                        }
                    }

                    // Right Studio Button
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onNavigate("studio") },
                        color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.7f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, ShivTheme.colors.cardBorder.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier.padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = "SHIV Studio", tint = ShivTheme.colors.primary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Beautiful Minimalist Live Clock Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.scale(glowScale)
                ) {
                    Text(
                        text = if (currentTime.isNotEmpty()) currentTime else "07:30",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = if (activeComplexity == ComplexityMode.SIMPLE) 72.sp else 62.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = (-2).sp
                        ),
                        color = ShivTheme.colors.textPrimary
                    )
                    Text(
                        text = if (currentDate.isNotEmpty()) currentDate else "Monday, September 14",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = ShivTheme.colors.primary.copy(alpha = 0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Fluid Glassmorphic Quick Search Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(ShivTheme.cardRadius))
                        .clickable { onNavigate("all_apps") },
                    shape = RoundedCornerShape(ShivTheme.cardRadius),
                    color = ShivTheme.colors.surface.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, ShivTheme.colors.primary.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = ShivTheme.colors.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Search apps, settings & system...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = ShivTheme.colors.textSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(ShivTheme.colors.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Apps,
                                contentDescription = "All Apps",
                                tint = ShivTheme.colors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ShivAI Cognitive Capsule Widget
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(ShivTheme.cardRadius))
                        .clickable { onNavigate("chat") },
                    shape = RoundedCornerShape(ShivTheme.cardRadius),
                    color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, ShivTheme.colors.primary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TrishulIcon(size = 20.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Ask ShivAI",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ShivTheme.colors.textPrimary
                                )
                                Text(
                                    text = "Cognitive reasoning, research & actions",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ShivTheme.colors.textSecondary
                                )
                            }
                        }
                        IconButton(
                            onClick = { onNavigate("chat") },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(ShivTheme.colors.primary.copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice", tint = ShivTheme.colors.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Dynamic App Grid (conforming to theme grid columns & rows)
                val gridCols = currentTheme.gridColumns.coerceIn(3, 6)
                val maxItems = when (activeComplexity) {
                    ComplexityMode.SIMPLE -> 8
                    ComplexityMode.BALANCED -> 12
                    ComplexityMode.ADVANCED -> 20
                }
                val homeDisplayApps = installedApps.take(maxItems)

                if (homeDisplayApps.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QUICK ACCESS (${activeProfile.title})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = ShivTheme.colors.textSecondary.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Long press empty area to edit",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = ShivTheme.colors.textSecondary.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridCols),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        userScrollEnabled = false
                    ) {
                        items(homeDisplayApps, key = { it.packageName }) { app ->
                            AppGridItemWithLongPress(
                                app = app,
                                iconShape = currentTheme.iconShape,
                                scalePercent = currentTheme.iconScalePercent,
                                onClick = { viewModel.onAppClicked(app.packageName) },
                                onLongClick = { selectedAppForOptions = app }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pinned App Dock (flagship fixed bottom dock apps)
                val dockCount = currentTheme.dockCount.coerceIn(3, 6)
                val dockApps = installedApps.drop(homeDisplayApps.size).take(dockCount)

                if (dockApps.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(ShivTheme.cardRadius)),
                        shape = RoundedCornerShape(ShivTheme.cardRadius),
                        color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.75f),
                        border = BorderStroke(1.dp, ShivTheme.colors.cardBorder.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            dockApps.forEach { app ->
                                AppGridItemWithLongPress(
                                    app = app,
                                    iconShape = currentTheme.iconShape,
                                    scalePercent = currentTheme.iconScalePercent,
                                    isDock = true,
                                    onClick = { viewModel.onAppClicked(app.packageName) },
                                    onLongClick = { selectedAppForOptions = app }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Swipe Up for All Apps Drawer Indicator Pill
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onNavigate("all_apps") },
                    color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, ShivTheme.colors.cardBorder.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = "Swipe up",
                            tint = ShivTheme.colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Swipe up for all apps",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = ShivTheme.colors.textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // 1. Home Screen Edit Modal (Long-Press Empty Space)
        if (showHomeEditModal) {
            ModalBottomSheet(
                onDismissRequest = { showHomeEditModal = false },
                containerColor = ShivTheme.colors.surface,
                scrimColor = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TrishulIcon(size = 22.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Home Screen Options",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = ShivTheme.colors.textPrimary
                            )
                        }
                        IconButton(onClick = { showHomeEditModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = ShivTheme.colors.textSecondary)
                        }
                    }

                    HorizontalDivider(color = ShivTheme.colors.cardBorder.copy(alpha = 0.5f))

                    // Action buttons grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Customize in Studio
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    showHomeEditModal = false
                                    onNavigate("studio")
                                },
                            color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, ShivTheme.colors.primary.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Palette, contentDescription = null, tint = ShivTheme.colors.primary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("SHIV Studio", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = ShivTheme.colors.textPrimary)
                            }
                        }

                        // Switch Profile
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    val nextProfile = when (activeProfile) {
                                        ProfileType.EVERYDAY -> ProfileType.STUDENT
                                        ProfileType.STUDENT -> ProfileType.DEVELOPER
                                        ProfileType.DEVELOPER -> ProfileType.DESIGNER
                                        ProfileType.DESIGNER -> ProfileType.EXECUTIVE
                                        else -> ProfileType.EVERYDAY
                                    }
                                    viewModel.applyProfile(nextProfile)
                                    Toast.makeText(context, "Applied ${nextProfile.title} Profile", Toast.LENGTH_SHORT).show()
                                },
                            color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, ShivTheme.colors.accent.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = ShivTheme.colors.accent, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Profile", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = ShivTheme.colors.textPrimary)
                            }
                        }

                        // Grid Density
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    val nextCols = if (currentTheme.gridColumns >= 5) 4 else 5
                                    viewModel.studioThemeEngine?.setGridDimensions(currentTheme.gridRows, nextCols)
                                    Toast.makeText(context, "Grid set to ${nextCols} columns", Toast.LENGTH_SHORT).show()
                                },
                            color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, ShivaAmber.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.GridOn, contentDescription = null, tint = ShivaAmber, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Grid ${currentTheme.gridColumns}x${currentTheme.gridRows}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = ShivTheme.colors.textPrimary)
                            }
                        }
                    }

                    // Undo & Reset Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val ok = viewModel.undoLastLayoutChange()
                                if (ok) {
                                    Toast.makeText(context, "Layout reverted to previous state", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "No previous layout in history", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Undo Layout")
                        }

                        Button(
                            onClick = { showResetConfirmDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ShivaRed.copy(alpha = 0.8f))
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // 2. App Long-Press Options Bottom Sheet
        if (selectedAppForOptions != null) {
            val app = selectedAppForOptions!!
            ModalBottomSheet(
                onDismissRequest = { selectedAppForOptions = null },
                containerColor = ShivTheme.colors.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ShivTheme.colors.textPrimary
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = ShivTheme.colors.textSecondary
                    )

                    HorizontalDivider(color = ShivTheme.colors.cardBorder.copy(alpha = 0.5f))

                    // Open App
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedAppForOptions = null
                                viewModel.onAppClicked(app.packageName)
                            },
                        color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Launch, contentDescription = null, tint = ShivTheme.colors.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Open Application", color = ShivTheme.colors.textPrimary, fontWeight = FontWeight.Medium)
                        }
                    }

                    // App Info
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedAppForOptions = null
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", app.packageName, null)
                                }
                                context.startActivity(intent)
                            },
                        color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = ShivTheme.colors.accent)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("App Details & Permissions", color = ShivTheme.colors.textPrimary, fontWeight = FontWeight.Medium)
                        }
                    }

                    // Protect with SHIV Lock
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedAppForOptions = null
                                val isLocked = viewModel.appLockManager?.isAppLocked(app.packageName) == true
                                viewModel.appLockManager?.setAppLocked(app.packageName, !isLocked)
                                Toast.makeText(
                                    context,
                                    if (!isLocked) "${app.name} protected by SHIV Lock" else "${app.name} unlocked",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                        color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = ShivaAmber)
                            Spacer(modifier = Modifier.width(12.dp))
                            val isCurrentlyLocked = viewModel.appLockManager?.isAppLocked(app.packageName) == true
                            Text(if (isCurrentlyLocked) "Remove SHIV Lock" else "Protect with SHIV Lock", color = ShivTheme.colors.textPrimary, fontWeight = FontWeight.Medium)
                        }
                    }

                    // Uninstall
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedAppForOptions = null
                                val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                                    data = Uri.parse("package:${app.packageName}")
                                }
                                context.startActivity(intent)
                            },
                        color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = ShivaRed)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Uninstall Application", color = ShivaRed, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }

        // 3. Reset Layout Confirmation Dialog
        if (showResetConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showResetConfirmDialog = false },
                title = { Text("Reset Layout to Defaults?") },
                text = { Text("This will restore default home screen apps and dock placements. Your installed apps remain safe.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetLayoutToDefaults()
                            showResetConfirmDialog = false
                            showHomeEditModal = false
                            Toast.makeText(context, "Layout reset to defaults", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ShivaRed)
                    ) {
                        Text("Reset Layout")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppGridItemWithLongPress(
    app: AppInfo,
    iconShape: String = "SQUIRCLE",
    scalePercent: Int = 100,
    isDock: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current
    val appIcon = remember(app.packageName) {
        loadAppIconBitmap(context, app.packageName)
    }

    val shapeCorner = when (iconShape) {
        "CIRCLE" -> CircleShape
        "ROUNDED_RECT" -> RoundedCornerShape(8.dp)
        "TEARDROP" -> RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 4.dp)
        else -> RoundedCornerShape(16.dp)
    }

    val baseSize = if (isDock) 52.dp else 56.dp
    val scaledSize = (baseSize.value * (scalePercent / 100f)).dp

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(scaledSize)
                .clip(shapeCorner)
                .background(ShivTheme.colors.surfaceVariant.copy(alpha = 0.65f))
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            if (appIcon != null) {
                androidx.compose.foundation.Image(
                    bitmap = appIcon,
                    contentDescription = app.name,
                    modifier = Modifier
                        .size(scaledSize - 6.dp)
                        .clip(shapeCorner),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            } else {
                val initial = app.name.firstOrNull()?.uppercase() ?: "A"
                val colorHash = app.packageName.hashCode()
                val accentColor = when (kotlin.math.abs(colorHash) % 5) {
                    0 -> ShivTheme.colors.primary
                    1 -> ShivaIndigo
                    2 -> ShivaPurple
                    3 -> ShivaPink
                    else -> ShivaOrange
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = shapeCorner,
                    color = accentColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = accentColor
                        )
                    }
                }
            }
        }

        if (!isDock) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = app.name,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                color = ShivTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LauncherScreen(
    viewModel: LauncherViewModel,
    onNavigate: (String) -> Unit,
    onOpenSidebar: () -> Unit
) {
    ShivOSLiteLauncherScreen(
        viewModel = viewModel,
        onNavigate = onNavigate,
        onOpenSidebar = onOpenSidebar
    )
}
