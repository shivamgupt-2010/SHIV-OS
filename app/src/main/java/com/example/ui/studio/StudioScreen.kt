package com.example.ui.studio

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.launcher.ComplexityMode
import com.example.core.launcher.LayoutManager
import com.example.core.launcher.ProfileType
import com.example.core.studio.StudioThemeEngine
import com.example.core.studio.StudioThemeTokens
import com.example.ui.components.CosmicBackground
import com.example.ui.components.TrishulIcon
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(
    themeEngine: StudioThemeEngine,
    layoutManager: LayoutManager? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val theme by themeEngine.currentTheme.collectAsState()
    val activeComplexity = layoutManager?.activeComplexityMode?.collectAsState()?.value ?: ComplexityMode.BALANCED
    val activeProfile = layoutManager?.activeProfile?.collectAsState()?.value ?: ProfileType.EVERYDAY

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Appearance", "Layout", "Icons", "Widgets", "Gestures", "Animations", "Profiles")

    var showImportDialog by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedJson by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        CosmicBackground(showSilhouette = false)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(modifier = Modifier.statusBarsPadding()) {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TrishulIcon(size = 22.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("SHIV Studio", fontWeight = FontWeight.Bold)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                exportedJson = themeEngine.exportThemeJson()
                                showExportDialog = true
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "Export Theme", tint = ShivaCyan)
                            }
                            IconButton(onClick = {
                                importText = ""
                                showImportDialog = true
                            }) {
                                Icon(Icons.Default.Download, contentDescription = "Import Theme", tint = ShivaCyan)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )

                    // 7 Studio Category Tabs
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = ShivaCyan,
                        edgePadding = 16.dp,
                        divider = {}
                    ) {
                        tabs.forEachIndexed { index, tabTitle ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = tabTitle,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                }
                            )
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Live Dynamic Theme & Studio Metrics Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(alpha = 0.8f)),
                    border = BorderStroke(1.dp, ShivaCyan.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Active Configuration",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Surface(color = ShivaCyan.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    "${theme.themePreset} • ${activeComplexity.title}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ShivaCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            "Grid: ${theme.gridRows}x${theme.gridColumns} • Dock: ${theme.dockCount} apps • Icon: ${theme.iconShape} (${theme.iconScalePercent}%) • Profile: ${activeProfile.title}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // TAB 0: APPEARANCE
                if (selectedTab == 0) {
                    Text("Theme Palette Presets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        StudioThemeTokens.PRESETS.values.forEach { preset ->
                            val isSelected = theme.themePreset == preset.key
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { themeEngine.applyPreset(preset.key) },
                                color = if (isSelected) CosmicSurfaceVariant else CosmicSurface.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, if (isSelected) ShivaCyan else CosmicCardBorder.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(preset.primary))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(preset.accent))
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Text(preset.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = TextPrimary)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = ShivaCyan)
                                    }
                                }
                            }
                        }
                    }
                }

                // TAB 1: LAYOUT & COMPLEXITY
                if (selectedTab == 1) {
                    Text("Complexity Mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ComplexityMode.entries.forEach { mode ->
                            val isSelected = activeComplexity == mode
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { layoutManager?.setComplexityMode(mode) },
                                color = if (isSelected) CosmicSurfaceVariant else CosmicSurface.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, if (isSelected) ShivaCyan else CosmicCardBorder.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(mode.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text(mode.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ShivaCyan)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Grid Matrix Density", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val gridOptions = listOf(Pair(4, 4), Pair(4, 5), Pair(5, 5), Pair(6, 6))
                        gridOptions.forEach { (cols, rows) ->
                            val isSelected = theme.gridColumns == cols && theme.gridRows == rows
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { themeEngine.setGridDimensions(rows, cols) },
                                color = if (isSelected) ShivaIndigo.copy(alpha = 0.3f) else CosmicSurface.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (isSelected) ShivaCyan else CosmicCardBorder.copy(alpha = 0.3f))
                            ) {
                                Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                    Text("${cols}x$rows", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (isSelected) ShivaCyan else TextPrimary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Dock Apps: ${theme.dockCount}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(3, 4, 5, 6).forEach { count ->
                            val isSelected = theme.dockCount == count
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { themeEngine.setDockCount(count) },
                                color = if (isSelected) ShivaIndigo.copy(alpha = 0.3f) else CosmicSurface.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (isSelected) ShivaCyan else CosmicCardBorder.copy(alpha = 0.3f))
                            ) {
                                Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                    Text("$count Apps", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (isSelected) ShivaCyan else TextPrimary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Lock Home Layout", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Prevents accidental icon dragging or removal", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Switch(
                                checked = theme.isLayoutLocked,
                                onCheckedChange = { themeEngine.setLayoutLocked(it) }
                            )
                        }
                    }
                }

                // TAB 2: ICONS
                if (selectedTab == 2) {
                    Text("Icon Geometry & Shape", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        val shapes = listOf("SQUIRCLE", "CIRCLE", "ROUNDED_RECT", "TEARDROP")
                        shapes.forEach { shape ->
                            val isSelected = theme.iconShape == shape
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { themeEngine.setIconShape(shape) },
                                color = if (isSelected) ShivaCyan.copy(alpha = 0.2f) else CosmicSurface.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, if (isSelected) ShivaCyan else CosmicCardBorder.copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(
                                                when (shape) {
                                                    "CIRCLE" -> CircleShape
                                                    "ROUNDED_RECT" -> RoundedCornerShape(6.dp)
                                                    "TEARDROP" -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp)
                                                    else -> RoundedCornerShape(10.dp)
                                                }
                                            )
                                            .background(if (isSelected) ShivaCyan else TextSecondary)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = shape.take(6).lowercase().replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) ShivaCyan else TextSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Icon Scale: ${theme.iconScalePercent}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Slider(
                        value = theme.iconScalePercent.toFloat(),
                        onValueChange = { themeEngine.setIconScale(it.toInt()) },
                        valueRange = 80f..120f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = ShivaCyan,
                            activeTrackColor = ShivaCyan,
                            inactiveTrackColor = CosmicSurfaceVariant
                        )
                    )
                }

                // TAB 3: WIDGETS
                if (selectedTab == 3) {
                    Text("Home Screen Widgets & Indicators", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("ShivAI Cognitive Capsule", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("Display intelligent multimodal search capsule", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                Switch(checked = true, onCheckedChange = {})
                            }

                            Divider(color = CosmicCardBorder.copy(alpha = 0.4f))

                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Live Digital Clock", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("Minimalist typographic clock with breathing glow", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                Switch(checked = true, onCheckedChange = {})
                            }
                        }
                    }
                }

                // TAB 4: GESTURES
                if (selectedTab == 4) {
                    Text("Launcher Touch Gestures", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SwipeUp, contentDescription = null, tint = ShivaCyan)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Swipe Up", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("Opens All Apps Drawer with private search", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                            Divider(color = CosmicCardBorder.copy(alpha = 0.4f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TouchApp, contentDescription = null, tint = ShivaPurple)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Long Press Empty Area", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("Opens Home Screen Options and Edit Modal", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                            Divider(color = CosmicCardBorder.copy(alpha = 0.4f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SmartButton, contentDescription = null, tint = ShivaAmber)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Long Press App Icon", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("Access SHIV Lock, App Details, and Uninstall", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                        }
                    }
                }

                // TAB 5: ANIMATIONS & MOTION
                if (selectedTab == 5) {
                    Text("Motion Engine Presets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        MotionPreset.entries.forEach { preset ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp)),
                                color = CosmicSurfaceVariant.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, ShivaCyan.copy(alpha = 0.3f))
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Animation, contentDescription = null, tint = ShivaCyan)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${preset.title} (${preset.durationMs}ms)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text(preset.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Reduced Motion", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Minimizes transitions for maximum battery efficiency", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Switch(
                                checked = theme.isReducedMotion,
                                onCheckedChange = { themeEngine.setReducedMotion(it) }
                            )
                        }
                    }
                }

                // TAB 6: PROFILES
                if (selectedTab == 6) {
                    Text("Starter Experience Profiles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProfileType.entries.forEach { profile ->
                            val isSelected = activeProfile == profile
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        layoutManager?.applyProfile(profile)
                                        Toast.makeText(context, "Applied ${profile.title} Profile", Toast.LENGTH_SHORT).show()
                                    },
                                color = if (isSelected) CosmicSurfaceVariant else CosmicSurface.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, if (isSelected) ShivaCyan else CosmicCardBorder.copy(alpha = 0.4f))
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (profile) {
                                            ProfileType.EVERYDAY -> Icons.Default.Home
                                            ProfileType.STUDENT -> Icons.Default.School
                                            ProfileType.TEACHER -> Icons.Default.CastForEducation
                                            ProfileType.DESIGNER -> Icons.Default.Brush
                                            ProfileType.VIDEO_EDITOR -> Icons.Default.Movie
                                            ProfileType.MARKETER -> Icons.Default.Campaign
                                            ProfileType.DEVELOPER -> Icons.Default.Code
                                            ProfileType.BUSINESS_OWNER -> Icons.Default.Storefront
                                            ProfileType.EXECUTIVE -> Icons.Default.BusinessCenter
                                            ProfileType.CREATOR -> Icons.Default.VideoCall
                                            ProfileType.FAMILY -> Icons.Default.FamilyRestroom
                                            ProfileType.ACCESSIBILITY -> Icons.Default.AccessibilityNew
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) ShivaCyan else TextSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(profile.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text(profile.description, style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ShivaCyan)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Export Dialog
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text("Exported Theme JSON") },
                text = {
                    Column {
                        Text("Copy your theme tokens to share or backup:", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = exportedJson,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().height(150.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        clipboardManager.setText(AnnotatedString(exportedJson))
                        Toast.makeText(context, "Theme JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    }) {
                        Text("Copy to Clipboard")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExportDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        // Import Dialog
        if (showImportDialog) {
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                title = { Text("Import Theme JSON") },
                text = {
                    Column {
                        Text("Paste validated theme tokens JSON:", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = importText,
                            onValueChange = { importText = it },
                            placeholder = { Text("{\"themePreset\": \"CYAN_NEON\", ...}") },
                            modifier = Modifier.fillMaxWidth().height(150.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val ok = themeEngine.importThemeJson(importText)
                        if (ok) {
                            Toast.makeText(context, "Theme imported successfully!", Toast.LENGTH_SHORT).show()
                            showImportDialog = false
                        } else {
                            Toast.makeText(context, "Invalid theme JSON!", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Apply Theme")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
