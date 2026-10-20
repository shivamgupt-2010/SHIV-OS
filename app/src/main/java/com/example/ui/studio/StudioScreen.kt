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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.launcher.ComplexityMode
import com.example.core.launcher.LayoutManager
import com.example.core.launcher.ProfileType
import com.example.core.studio.StudioThemeEngine
import com.example.core.studio.StudioThemeTokens
import com.example.core.studio.ThemePreset
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
    val customThemes by themeEngine.customThemes.collectAsState()
    val activeComplexity = layoutManager?.activeComplexityMode?.collectAsState()?.value ?: ComplexityMode.BALANCED
    val activeProfile = layoutManager?.activeProfile?.collectAsState()?.value ?: ProfileType.EVERYDAY

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Appearance", "Layout", "Icons", "Widgets", "Gestures", "Animations", "Profiles")

    var selectedCategoryFilter by remember { mutableStateOf("All") }
    val categoryFilters = listOf("All", "Light", "Dark", "Expressive", "Adaptive", "Custom")

    var showImportDialog by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedJson by remember { mutableStateOf("") }

    var showCreateCustomDialog by remember { mutableStateOf(false) }
    var customThemeNameInput by remember { mutableStateOf("") }
    var customPrimaryHex by remember { mutableStateOf(theme.primaryColorHex) }
    var customAccentHex by remember { mutableStateOf(theme.accentColorHex) }
    var customSurfaceHex by remember { mutableStateOf(theme.surfaceColorHex) }
    var customBgHex by remember { mutableStateOf(theme.backgroundColorHex) }
    var customIsDark by remember { mutableStateOf(theme.isDark) }
    var customRadiusDp by remember { mutableIntStateOf(theme.cardCornerRadiusDp) }
    var customTypography by remember { mutableStateOf(theme.typographyStyle) }
    var customAnimation by remember { mutableStateOf(theme.animationPreset) }

    var renameTargetId by remember { mutableStateOf<String?>(null) }
    var renameInputText by remember { mutableStateOf("") }

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
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ShivTheme.colors.textPrimary)
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                exportedJson = themeEngine.exportThemeJson()
                                showExportDialog = true
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "Export Theme", tint = ShivTheme.colors.primary)
                            }
                            IconButton(onClick = {
                                importText = ""
                                showImportDialog = true
                            }) {
                                Icon(Icons.Default.Download, contentDescription = "Import Theme", tint = ShivTheme.colors.primary)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )

                    // 7 Studio Category Tabs
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = ShivTheme.colors.primary,
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

                // Interactive Live Theme Preview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(theme.cardCornerRadiusDp.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioThemeTokens.hexToColor(theme.surfaceColorHex)),
                    border = BorderStroke(1.dp, StudioThemeTokens.hexToColor(theme.accentColorHex).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Live Theme Preview",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = StudioThemeTokens.hexToColor(theme.textPrimaryColorHex)
                            )
                            Surface(
                                color = StudioThemeTokens.hexToColor(theme.primaryColorHex).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    if (theme.isCustomTheme) "CUSTOM • ${theme.customThemeName}" else "${theme.themePreset} • ${theme.themeCategory}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StudioThemeTokens.hexToColor(theme.primaryColorHex),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Live Mock Elements: Icon, Button, Pill, Text
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Icon Preview with active shape and scale
                            Box(
                                modifier = Modifier
                                    .size((36 * (theme.iconScalePercent / 100f)).dp)
                                    .clip(
                                        when (theme.iconShape) {
                                            "CIRCLE" -> CircleShape
                                            "ROUNDED_RECT" -> RoundedCornerShape(6.dp)
                                            "TEARDROP" -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp)
                                            else -> RoundedCornerShape(10.dp)
                                        }
                                    )
                                    .background(StudioThemeTokens.hexToColor(theme.primaryColorHex)),
                                contentAlignment = Alignment.Center
                            ) {
                                TrishulIcon(size = 18.dp)
                            }

                            // Sample Button with active radius
                            Button(
                                onClick = {},
                                shape = RoundedCornerShape((theme.cardCornerRadiusDp / 2).coerceAtLeast(4).dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StudioThemeTokens.hexToColor(theme.primaryColorHex)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Action", fontSize = 12.sp, color = if (theme.isDark) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                            }

                            // Secondary Chip
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = StudioThemeTokens.hexToColor(theme.accentColorHex).copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, StudioThemeTokens.hexToColor(theme.accentColorHex))
                            ) {
                                Text(
                                    "${theme.cardCornerRadiusDp}dp Radius",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StudioThemeTokens.hexToColor(theme.accentColorHex)
                                )
                            }
                        }

                        Text(
                            "Grid: ${theme.gridRows}x${theme.gridColumns} • Dock: ${theme.dockCount} apps • Typography: ${theme.typographyStyle} • Motion: ${theme.animationPreset}",
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioThemeTokens.hexToColor(theme.textSecondaryColorHex)
                        )
                    }
                }

                // TAB 0: APPEARANCE (COMPLETE THEME CATALOGUE + CUSTOM THEME CREATOR)
                if (selectedTab == 0) {
                    // Category Filter Tabs (Light, Dark, Expressive, Adaptive, Custom)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoryFilters.forEach { category ->
                            val isFilterSelected = selectedCategoryFilter == category
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedCategoryFilter = category },
                                color = if (isFilterSelected) ShivTheme.colors.primary.copy(alpha = 0.25f) else ShivTheme.colors.surfaceVariant.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (isFilterSelected) ShivTheme.colors.primary else ShivTheme.colors.cardBorder.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = category,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isFilterSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isFilterSelected) ShivTheme.colors.primary else ShivTheme.colors.textPrimary
                                )
                            }
                        }
                    }

                    // Create Custom Theme CTA Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Available Themes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ShivTheme.colors.textPrimary
                        )
                        FilledTonalButton(
                            onClick = {
                                customThemeNameInput = "My Custom Theme"
                                customPrimaryHex = theme.primaryColorHex
                                customAccentHex = theme.accentColorHex
                                customSurfaceHex = theme.surfaceColorHex
                                customBgHex = theme.backgroundColorHex
                                customIsDark = theme.isDark
                                customRadiusDp = theme.cardCornerRadiusDp
                                customTypography = theme.typographyStyle
                                customAnimation = theme.animationPreset
                                showCreateCustomDialog = true
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Theme", fontSize = 12.sp)
                        }
                    }

                    // Filtered Presets List
                    val filteredPresets = remember(selectedCategoryFilter) {
                        if (selectedCategoryFilter == "All") {
                            StudioThemeTokens.PRESETS.values.toList()
                        } else if (selectedCategoryFilter == "Custom") {
                            emptyList()
                        } else {
                            StudioThemeTokens.PRESETS.values.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        filteredPresets.forEach { preset ->
                            val isSelected = theme.themePreset == preset.key
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(theme.cardCornerRadiusDp.dp))
                                    .clickable { themeEngine.applyPreset(preset.key) },
                                color = if (isSelected) ShivTheme.colors.surfaceVariant else ShivTheme.colors.surface.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(theme.cardCornerRadiusDp.dp),
                                border = BorderStroke(1.dp, if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.cardBorder.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(preset.primary))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(preset.accent))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(preset.surface))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(preset.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = ShivTheme.colors.textPrimary)
                                            Text(preset.category, style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
                                        }
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = ShivTheme.colors.primary)
                                    }
                                }
                            }
                        }

                        // Saved Custom Themes Section
                        if (selectedCategoryFilter == "All" || selectedCategoryFilter == "Custom") {
                            if (customThemes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Your Custom Themes (${customThemes.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                                customThemes.forEach { customTheme ->
                                    val isSelected = theme.id == customTheme.id || (theme.isCustomTheme && theme.customThemeName == customTheme.customThemeName)
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(theme.cardCornerRadiusDp.dp))
                                            .clickable { themeEngine.applyCustomTheme(customTheme) },
                                        color = if (isSelected) ShivTheme.colors.surfaceVariant else ShivTheme.colors.surface.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(theme.cardCornerRadiusDp.dp),
                                        border = BorderStroke(1.dp, if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.cardBorder.copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(StudioThemeTokens.hexToColor(customTheme.primaryColorHex)))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(StudioThemeTokens.hexToColor(customTheme.accentColorHex)))
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(customTheme.customThemeName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                                                    Text("${customTheme.cardCornerRadiusDp}dp • ${customTheme.typographyStyle}", style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
                                                }
                                            }
                                            Row {
                                                IconButton(onClick = {
                                                    renameTargetId = customTheme.id
                                                    renameInputText = customTheme.customThemeName
                                                }) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Rename", tint = ShivTheme.colors.textSecondary, modifier = Modifier.size(18.dp))
                                                }
                                                IconButton(onClick = {
                                                    themeEngine.duplicateCustomTheme(customTheme.id, "${customTheme.customThemeName} (Copy)")
                                                    Toast.makeText(context, "Theme duplicated", Toast.LENGTH_SHORT).show()
                                                }) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = ShivTheme.colors.textSecondary, modifier = Modifier.size(18.dp))
                                                }
                                                IconButton(onClick = {
                                                    themeEngine.deleteCustomTheme(customTheme.id)
                                                    Toast.makeText(context, "Theme deleted", Toast.LENGTH_SHORT).show()
                                                }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ShivaRed, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Card Corner Radius Control Slider
                    Text("Card Corner Radius: ${theme.cardCornerRadiusDp}dp", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                    Slider(
                        value = theme.cardCornerRadiusDp.toFloat(),
                        onValueChange = { themeEngine.setCardCornerRadius(it.toInt()) },
                        valueRange = 0f..28f,
                        steps = 27,
                        colors = SliderDefaults.colors(
                            thumbColor = ShivTheme.colors.primary,
                            activeTrackColor = ShivTheme.colors.primary,
                            inactiveTrackColor = ShivTheme.colors.surfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Typography Selection Row
                    Text("Typography Style", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val typoOptions = listOf("DEFAULT" to "Cosmic", "MODERN" to "Modern", "SERIF" to "Serif", "MONO" to "Mono")
                        typoOptions.forEach { (key, label) ->
                            val isSelected = theme.typographyStyle == key
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { themeEngine.setTypographyStyle(key) },
                                color = if (isSelected) ShivTheme.colors.primary.copy(alpha = 0.25f) else ShivTheme.colors.surfaceVariant.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.cardBorder.copy(alpha = 0.4f))
                            ) {
                                Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontFamily = when (key) {
                                            "SERIF" -> FontFamily.Serif
                                            "MONO" -> FontFamily.Monospace
                                            else -> FontFamily.Default
                                        },
                                        color = if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Reset Factory Defaults CTA
                    OutlinedButton(
                        onClick = {
                            themeEngine.resetToDefaults()
                            Toast.makeText(context, "Reset theme to factory defaults", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ShivaRed)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset All Appearance Defaults")
                    }
                }

                // TAB 1: LAYOUT & COMPLEXITY
                if (selectedTab == 1) {
                    Text("Complexity Mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ComplexityMode.entries.forEach { mode ->
                            val isSelected = activeComplexity == mode
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(theme.cardCornerRadiusDp.dp))
                                    .clickable { layoutManager?.setComplexityMode(mode) },
                                color = if (isSelected) ShivTheme.colors.surfaceVariant else ShivTheme.colors.surface.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(theme.cardCornerRadiusDp.dp),
                                border = BorderStroke(1.dp, if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.cardBorder.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(mode.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                                        Text(mode.description, style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ShivTheme.colors.primary)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Grid Matrix Density", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val gridOptions = listOf(Pair(4, 4), Pair(4, 5), Pair(5, 5), Pair(6, 6))
                        gridOptions.forEach { (cols, rows) ->
                            val isSelected = theme.gridColumns == cols && theme.gridRows == rows
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { themeEngine.setGridDimensions(rows, cols) },
                                color = if (isSelected) ShivTheme.colors.primary.copy(alpha = 0.25f) else ShivTheme.colors.surface.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.cardBorder.copy(alpha = 0.3f))
                            ) {
                                Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                    Text("${cols}x$rows", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.textPrimary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Dock Apps: ${theme.dockCount}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(3, 4, 5, 6).forEach { count ->
                            val isSelected = theme.dockCount == count
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { themeEngine.setDockCount(count) },
                                color = if (isSelected) ShivTheme.colors.primary.copy(alpha = 0.25f) else ShivTheme.colors.surface.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.cardBorder.copy(alpha = 0.3f))
                            ) {
                                Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                    Text("$count Apps", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.textPrimary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(theme.cardCornerRadiusDp.dp),
                        colors = CardDefaults.cardColors(containerColor = ShivTheme.colors.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Lock Home Layout", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                                Text("Prevents accidental icon dragging or removal", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
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
                    Text("Icon Geometry & Shape", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        val shapes = listOf("SQUIRCLE", "CIRCLE", "ROUNDED_RECT", "TEARDROP")
                        shapes.forEach { shape ->
                            val isSelected = theme.iconShape == shape
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { themeEngine.setIconShape(shape) },
                                color = if (isSelected) ShivTheme.colors.primary.copy(alpha = 0.2f) else ShivTheme.colors.surface.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.cardBorder.copy(alpha = 0.3f))
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
                                            .background(if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.textSecondary)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = shape.take(6).lowercase().replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.textSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Icon Scale: ${theme.iconScalePercent}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                    Slider(
                        value = theme.iconScalePercent.toFloat(),
                        onValueChange = { themeEngine.setIconScale(it.toInt()) },
                        valueRange = 80f..120f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = ShivTheme.colors.primary,
                            activeTrackColor = ShivTheme.colors.primary,
                            inactiveTrackColor = ShivTheme.colors.surfaceVariant
                        )
                    )
                }

                // TAB 3: WIDGETS
                if (selectedTab == 3) {
                    Text("Home Screen Widgets & Indicators", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(theme.cardCornerRadiusDp.dp),
                        colors = CardDefaults.cardColors(containerColor = ShivTheme.colors.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("ShivAI Cognitive Capsule", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                                    Text("Display intelligent multimodal search capsule", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                                }
                                Switch(checked = true, onCheckedChange = {})
                            }

                            HorizontalDivider(color = ShivTheme.colors.cardBorder.copy(alpha = 0.4f))

                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Live Digital Clock", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                                    Text("Minimalist typographic clock with breathing glow", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                                }
                                Switch(checked = true, onCheckedChange = {})
                            }
                        }
                    }
                }

                // TAB 4: GESTURES
                if (selectedTab == 4) {
                    Text("Launcher Touch Gestures", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(theme.cardCornerRadiusDp.dp),
                        colors = CardDefaults.cardColors(containerColor = ShivTheme.colors.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SwipeUp, contentDescription = null, tint = ShivTheme.colors.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Swipe Up", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                                    Text("Opens All Apps Drawer with private search", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                                }
                            }
                            HorizontalDivider(color = ShivTheme.colors.cardBorder.copy(alpha = 0.4f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TouchApp, contentDescription = null, tint = ShivTheme.colors.accent)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Long Press Empty Area", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                                    Text("Opens Home Screen Options and Edit Modal", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                                }
                            }
                            HorizontalDivider(color = ShivTheme.colors.cardBorder.copy(alpha = 0.4f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SmartButton, contentDescription = null, tint = ShivaAmber)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Long Press App Icon", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                                    Text("Access SHIV Lock, App Details, and Uninstall", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                                }
                            }
                        }
                    }
                }

                // TAB 5: ANIMATIONS & MOTION
                if (selectedTab == 5) {
                    Text("Motion Engine Presets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        MotionPreset.entries.forEach { preset ->
                            val isSelected = theme.animationPreset.equals(preset.name, ignoreCase = true)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(theme.cardCornerRadiusDp.dp))
                                    .clickable { themeEngine.setAnimationPreset(preset.name) },
                                color = if (isSelected) ShivTheme.colors.primary.copy(alpha = 0.2f) else ShivTheme.colors.surfaceVariant.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(theme.cardCornerRadiusDp.dp),
                                border = BorderStroke(1.dp, if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.cardBorder.copy(alpha = 0.3f))
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Animation, contentDescription = null, tint = ShivTheme.colors.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${preset.title} (${preset.durationMs}ms)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                                        Text(preset.description, style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ShivTheme.colors.primary)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(theme.cardCornerRadiusDp.dp),
                        colors = CardDefaults.cardColors(containerColor = ShivTheme.colors.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Reduced Motion", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                                Text("Minimizes transitions for maximum battery efficiency", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
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
                    Text("Starter Experience Profiles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProfileType.entries.forEach { profile ->
                            val isSelected = activeProfile == profile
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(theme.cardCornerRadiusDp.dp))
                                    .clickable {
                                        layoutManager?.applyProfile(profile)
                                        Toast.makeText(context, "Applied ${profile.title} Profile", Toast.LENGTH_SHORT).show()
                                    },
                                color = if (isSelected) ShivTheme.colors.surfaceVariant else ShivTheme.colors.surface.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(theme.cardCornerRadiusDp.dp),
                                border = BorderStroke(1.dp, if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.cardBorder.copy(alpha = 0.4f))
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
                                        tint = if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.textSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(profile.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                                        Text(profile.description, style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ShivTheme.colors.primary)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Create Custom Theme Modal Dialog
        if (showCreateCustomDialog) {
            AlertDialog(
                onDismissRequest = { showCreateCustomDialog = false },
                title = { Text("Create Custom Theme") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = customThemeNameInput,
                            onValueChange = { customThemeNameInput = it },
                            label = { Text("Theme Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Primary Accent", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("#00F5FF", "#6366F1", "#10B981", "#F97316", "#EC4899", "#FBBF24").forEach { hex ->
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(StudioThemeTokens.hexToColor(hex))
                                        .clickable { customPrimaryHex = hex }
                                        .then(if (customPrimaryHex == hex) Modifier.padding(2.dp) else Modifier)
                                )
                            }
                        }

                        Text("Secondary Accent", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("#8A2BE2", "#38BDF8", "#34D399", "#EC4899", "#A855F7", "#F59E0B").forEach { hex ->
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(StudioThemeTokens.hexToColor(hex))
                                        .clickable { customAccentHex = hex }
                                )
                            }
                        }

                        Text("Surface & Background", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                Triple("#0E1019", "#07080E", true),  // Void Dark
                                Triple("#121212", "#000000", true),  // Pure OLED
                                Triple("#18181B", "#09090B", true),  // Dark Slate
                                Triple("#F8FAFC", "#FFFFFF", false), // Pure Light
                                Triple("#F5F2EB", "#FDFBF7", false), // Soft Cream
                                Triple("#F5EBE1", "#FAF3EB", false)  // Paper Sepia
                            ).forEach { (surf, bg, dark) ->
                                Surface(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            customSurfaceHex = surf
                                            customBgHex = bg
                                            customIsDark = dark
                                        },
                                    color = StudioThemeTokens.hexToColor(surf),
                                    border = BorderStroke(1.dp, Color.Gray)
                                ) {}
                            }
                        }

                        Text("Corner Radius: ${customRadiusDp}dp", style = MaterialTheme.typography.labelMedium)
                        Slider(
                            value = customRadiusDp.toFloat(),
                            onValueChange = { customRadiusDp = it.toInt() },
                            valueRange = 0f..28f,
                            steps = 27
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        themeEngine.saveCustomTheme(
                            name = customThemeNameInput,
                            primaryHex = customPrimaryHex,
                            accentHex = customAccentHex,
                            surfaceHex = customSurfaceHex,
                            bgHex = customBgHex,
                            isDark = customIsDark,
                            radiusDp = customRadiusDp,
                            typography = customTypography,
                            animation = customAnimation
                        )
                        Toast.makeText(context, "Saved & Applied '${customThemeNameInput}'", Toast.LENGTH_SHORT).show()
                        showCreateCustomDialog = false
                    }) {
                        Text("Save & Apply")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateCustomDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Rename Custom Theme Dialog
        if (renameTargetId != null) {
            AlertDialog(
                onDismissRequest = { renameTargetId = null },
                title = { Text("Rename Custom Theme") },
                text = {
                    OutlinedTextField(
                        value = renameInputText,
                        onValueChange = { renameInputText = it },
                        label = { Text("New Theme Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        themeEngine.renameCustomTheme(renameTargetId!!, renameInputText)
                        Toast.makeText(context, "Theme renamed", Toast.LENGTH_SHORT).show()
                        renameTargetId = null
                    }) {
                        Text("Rename")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { renameTargetId = null }) {
                        Text("Cancel")
                    }
                }
            )
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
