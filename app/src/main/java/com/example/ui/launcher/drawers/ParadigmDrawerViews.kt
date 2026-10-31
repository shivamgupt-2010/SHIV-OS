package com.example.ui.launcher.drawers

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.launcher.AppInfo
import com.example.data.local.entity.LauncherThemeEntity
import com.example.ui.components.TrishulIcon
import com.example.ui.effects.holographicTilt
import com.example.ui.launcher.AppGridItem
import com.example.ui.theme.*

/**
 * 1. Text-Only Minimalist App Drawer:
 * Distraction-free typographic index grouped by alphabetical headers.
 */
@Composable
fun TextOnlyMinimalDrawer(
    apps: List<AppInfo>,
    searchQuery: String,
    onAppClick: (String) -> Unit,
    onAppLongClick: (AppInfo) -> Unit
) {
    val grouped = remember(apps) {
        apps.groupBy { it.name.firstOrNull()?.uppercaseChar() ?: '#' }.toSortedMap()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 32.dp, horizontal = 12.dp)
    ) {
        grouped.forEach { (char, charApps) ->
            item(key = "header_$char") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = char.toString(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        ),
                        color = ShivTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = ShivTheme.colors.cardBorder.copy(alpha = 0.35f)
                    )
                }
            }

            items(charApps, key = { it.packageName }) { app ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .combinedClickable(
                            onClick = { onAppClick(app.packageName) },
                            onLongClick = { onAppLongClick(app) }
                        ),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = app.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = ShivTheme.colors.textPrimary
                            )
                            Text(
                                text = app.packageName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                ),
                                color = ShivTheme.colors.textSecondary.copy(alpha = 0.7f)
                            )
                        }
                        Icon(
                            Icons.Default.ArrowOutward,
                            contentDescription = null,
                            tint = ShivTheme.colors.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2. Terminal / Cyberpunk Hacker CLI App Drawer:
 * Monospace phosphor green terminal interface with command execution simulation.
 */
@Composable
fun TerminalHackerDrawer(
    apps: List<AppInfo>,
    searchQuery: String,
    onAppClick: (String) -> Unit,
    onAppLongClick: (AppInfo) -> Unit
) {
    val phosphorGreen = Color(0xFF00FF66)
    val terminalBg = Color(0xFF080B08)
    var executingApp by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = terminalBg,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, phosphorGreen.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            // CLI Header Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "SHIV_OS // SHELL v3.8 [ROOT]",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = phosphorGreen
                )
                Text(
                    "${apps.size} APPS LOADED",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = phosphorGreen.copy(alpha = 0.7f)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = phosphorGreen.copy(alpha = 0.3f)
            )

            if (executingApp != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    color = phosphorGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, phosphorGreen)
                ) {
                    Text(
                        ">> EXEC bin/launch $executingApp [OK]",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = phosphorGreen
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(apps, key = { it.packageName }) { app ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .combinedClickable(
                                onClick = {
                                    executingApp = app.name
                                    onAppClick(app.packageName)
                                },
                                onLongClick = { onAppLongClick(app) }
                            ),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "[-rwxr-xr-x]",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                ),
                                color = phosphorGreen.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                app.name,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = phosphorGreen,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "PID:${kotlin.math.abs(app.packageName.hashCode()) % 9000 + 1000}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp
                                ),
                                color = phosphorGreen.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. Search-Gated Focus App Drawer:
 * Instant spotlight search layout where results appear with predictive cards.
 */
@Composable
fun SearchGatedDrawer(
    apps: List<AppInfo>,
    searchQuery: String,
    currentTheme: LauncherThemeEntity,
    onAppClick: (String) -> Unit,
    onAppLongClick: (AppInfo) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            if (searchQuery.isBlank()) "POPULAR & SUGGESTED" else "MATCHED APPS (${apps.size})",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = ShivTheme.colors.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            items(apps, key = { it.packageName }) { app ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .combinedClickable(
                            onClick = { onAppClick(app.packageName) },
                            onLongClick = { onAppLongClick(app) }
                        ),
                    color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ShivTheme.colors.primary.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ShivTheme.colors.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                app.name.firstOrNull()?.uppercase() ?: "A",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = ShivTheme.colors.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                app.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = ShivTheme.colors.textPrimary
                            )
                            Text(
                                app.packageName,
                                style = MaterialTheme.typography.labelSmall,
                                color = ShivTheme.colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = ShivTheme.colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 4. Tabbed Smart Sidebar App Drawer:
 * Vertical shelf categories on left, auto-scrolling content pane on right.
 */
@Composable
fun TabbedSidebarDrawer(
    apps: List<AppInfo>,
    searchQuery: String,
    currentTheme: LauncherThemeEntity,
    onAppClick: (String) -> Unit,
    onAppLongClick: (AppInfo) -> Unit
) {
    val shelves = listOf("All", "Comm", "Work", "Media", "Tools")
    var selectedShelf by remember { mutableStateOf("All") }

    val filtered = remember(apps, selectedShelf) {
        if (selectedShelf == "All") apps
        else apps.filter {
            val p = "${it.packageName} ${it.name}".lowercase()
            when (selectedShelf) {
                "Comm" -> p.contains("call") || p.contains("phone") || p.contains("chat") || p.contains("mail") || p.contains("messag")
                "Work" -> p.contains("doc") || p.contains("sheet") || p.contains("office") || p.contains("drive") || p.contains("calc")
                "Media" -> p.contains("music") || p.contains("video") || p.contains("photo") || p.contains("youtube")
                else -> true
            }
        }
    }

    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // Sticky Vertical Category Rail
        Surface(
            modifier = Modifier.width(68.dp).fillMaxHeight(),
            color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, ShivTheme.colors.cardBorder.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                shelves.forEach { shelf ->
                    val isSelected = selectedShelf == shelf
                    Surface(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedShelf = shelf },
                        color = if (isSelected) ShivTheme.colors.primary.copy(alpha = 0.25f) else Color.Transparent,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (isSelected) ShivTheme.colors.primary else Color.Transparent)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                shelf,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                color = if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }
        }

        // Right Content Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f).fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            items(filtered, key = { it.packageName }) { app ->
                AppGridItem(
                    app = app,
                    iconShape = currentTheme.iconShape,
                    scalePercent = currentTheme.iconScalePercent,
                    activeIconPack = currentTheme.activeIconPack,
                    onClick = { onAppClick(app.packageName) },
                    onLongClick = { onAppLongClick(app) }
                )
            }
        }
    }
}

/**
 * 5. Taskbar Windowing App Drawer:
 * Desktop-style split start menu drawer with pinned applications.
 */
@Composable
fun TaskbarWindowingDrawer(
    apps: List<AppInfo>,
    searchQuery: String,
    currentTheme: LauncherThemeEntity,
    onAppClick: (String) -> Unit,
    onAppLongClick: (AppInfo) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Pinned Header Shelf
        Text(
            "PINNED FAVORITES",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = ShivTheme.colors.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            apps.take(6).forEach { app ->
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onAppClick(app.packageName) },
                    color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ShivTheme.colors.cardBorder.copy(alpha = 0.3f))
                ) {
                    Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(ShivTheme.colors.primary))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(app.name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = ShivTheme.colors.textPrimary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main All Applications Split Grid
        Text(
            "ALL APPLICATIONS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = ShivTheme.colors.textSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(currentTheme.gridColumns.coerceIn(3, 5)),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            items(apps, key = { it.packageName }) { app ->
                AppGridItem(
                    app = app,
                    iconShape = currentTheme.iconShape,
                    scalePercent = currentTheme.iconScalePercent,
                    activeIconPack = currentTheme.activeIconPack,
                    onClick = { onAppClick(app.packageName) },
                    onLongClick = { onAppLongClick(app) }
                )
            }
        }
    }
}

/**
 * 6. Glassmorphic Blur App Drawer:
 * Frosted acrylic glass cards with glowing specular light rims.
 */
@Composable
fun GlassmorphicBlurDrawer(
    apps: List<AppInfo>,
    searchQuery: String,
    currentTheme: LauncherThemeEntity,
    onAppClick: (String) -> Unit,
    onAppLongClick: (AppInfo) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(currentTheme.gridColumns.coerceIn(3, 5)),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        items(apps, key = { it.packageName }) { app ->
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .combinedClickable(
                        onClick = { onAppClick(app.packageName) },
                        onLongClick = { onAppLongClick(app) }
                    ),
                shape = RoundedCornerShape(18.dp),
                color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.65f),
                border = BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.45f),
                            ShivTheme.colors.primary.copy(alpha = 0.25f)
                        )
                    )
                )
            ) {
                AppGridItem(
                    app = app,
                    iconShape = currentTheme.iconShape,
                    scalePercent = currentTheme.iconScalePercent,
                    activeIconPack = currentTheme.activeIconPack,
                    onClick = { onAppClick(app.packageName) },
                    onLongClick = { onAppLongClick(app) }
                )
            }
        }
    }
}

/**
 * 7. Magazine Editorial App Drawer:
 * Asymmetric editorial cards with high-contrast typography and magazine sections.
 */
@Composable
fun MagazineEditorialDrawer(
    apps: List<AppInfo>,
    searchQuery: String,
    currentTheme: LauncherThemeEntity,
    onAppClick: (String) -> Unit,
    onAppLongClick: (AppInfo) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Featured Hero App Card
        val heroApp = apps.firstOrNull()
        if (heroApp != null) {
            item(key = "hero_feature") {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { onAppClick(heroApp.packageName) },
                    color = ShivTheme.colors.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.5.dp, ShivTheme.colors.primary.copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("EDITORIAL SELECTION", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp), color = ShivTheme.colors.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(heroApp.name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black), color = ShivTheme.colors.textPrimary)
                            Text(heroApp.packageName, style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
                        }
                        Icon(Icons.Default.Star, contentDescription = null, tint = ShivTheme.colors.primary, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }

        items(apps.drop(1), key = { it.packageName }) { app ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .combinedClickable(
                        onClick = { onAppClick(app.packageName) },
                        onLongClick = { onAppLongClick(app) }
                    ),
                color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.55f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ShivTheme.colors.cardBorder.copy(alpha = 0.3f))
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(ShivTheme.colors.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(app.name.firstOrNull()?.uppercase() ?: "A", fontWeight = FontWeight.Bold, color = ShivTheme.colors.primary)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(app.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ShivTheme.colors.textPrimary)
                        Text(app.packageName, style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = ShivTheme.colors.textSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

/**
 * 8. 3D Holographic Parallax App Drawer:
 * Parallax tilt depth cards with specular rim lighting.
 */
@Composable
fun Holographic3DDrawer(
    apps: List<AppInfo>,
    searchQuery: String,
    currentTheme: LauncherThemeEntity,
    onAppClick: (String) -> Unit,
    onAppLongClick: (AppInfo) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(currentTheme.gridColumns.coerceIn(3, 5)),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        items(apps, key = { it.packageName }) { app ->
            Box(
                modifier = Modifier
                    .holographicTilt(
                        enabled = !currentTheme.isReducedMotion,
                        maxTiltDegrees = 12f
                    )
            ) {
                AppGridItem(
                    app = app,
                    iconShape = currentTheme.iconShape,
                    scalePercent = currentTheme.iconScalePercent,
                    activeIconPack = currentTheme.activeIconPack,
                    onClick = { onAppClick(app.packageName) },
                    onLongClick = { onAppLongClick(app) }
                )
            }
        }
    }
}
