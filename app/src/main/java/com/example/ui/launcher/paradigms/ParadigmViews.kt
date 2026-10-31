package com.example.ui.launcher.paradigms

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.launcher.AppInfo
import com.example.core.launcher.LayoutManager
import com.example.data.local.entity.LauncherThemeEntity
import com.example.ui.components.TrishulIcon
import com.example.ui.effects.holographicTilt
import com.example.ui.theme.ShivTheme
import java.text.SimpleDateFormat
import java.util.*

/**
 * 1. Taskbar Overlay & Freeform Windowing Paradigm
 */
@Composable
fun TaskbarWindowingLayout(
    apps: List<AppInfo>,
    currentTheme: LauncherThemeEntity,
    onAppClick: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenAiChat: () -> Unit
) {
    var activeFloatingApp by remember { mutableStateOf<AppInfo?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            // Top System Status Pill
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, ShivTheme.colors.cardBorder.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TrishulIcon(size = 18.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SHIV DESKTOP MODE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = ShivTheme.colors.primary)
                    }
                    IconButton(onClick = onOpenSearch, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = ShivTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Desktop Workspace with Freeform Cards
            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                // Workspace Left Column: Quick pinned apps
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("WORKSPACE APPS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = ShivTheme.colors.textSecondary)
                    apps.take(6).forEach { app ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { activeFloatingApp = app },
                            color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ShivTheme.colors.cardBorder.copy(alpha = 0.2f))
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(ShivTheme.colors.primary))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(app.name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = ShivTheme.colors.textPrimary)
                            }
                        }
                    }
                }

                // Workspace Right Column: Active Floating Window Card
                Column(modifier = Modifier.weight(1.2f)) {
                    Text("ACTIVE WINDOW", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = ShivTheme.colors.textSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    if (activeFloatingApp != null) {
                        val winApp = activeFloatingApp!!
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.85f),
                            shape = RoundedCornerShape(16.dp),
                            color = ShivTheme.colors.surface,
                            border = BorderStroke(1.5.dp, ShivTheme.colors.primary.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(winApp.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = ShivTheme.colors.textPrimary)
                                    Row {
                                        IconButton(onClick = { activeFloatingApp = null }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Close, contentDescription = "Close", tint = ShivTheme.colors.textSecondary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Button(
                                    onClick = { onAppClick(winApp.packageName) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.primary)
                                ) {
                                    Text("Open Application", fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f),
                            shape = RoundedCornerShape(16.dp),
                            color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, ShivTheme.colors.cardBorder.copy(alpha = 0.2f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Select an app to preview window", style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
                            }
                        }
                    }
                }
            }
        }

        // Persistent Floating Taskbar at Bottom
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xEE12141C),
            border = BorderStroke(1.2.dp, ShivTheme.colors.primary.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onOpenDrawer) {
                    Icon(Icons.Default.Apps, contentDescription = "Drawer", tint = ShivTheme.colors.primary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    apps.take(currentTheme.dockCount).forEach { app ->
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onAppClick(app.packageName) },
                            color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.7f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(app.name.take(1), fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary, fontSize = 12.sp)
                            }
                        }
                    }
                }
                IconButton(onClick = onOpenAiChat) {
                    TrishulIcon(size = 20.dp)
                }
            }
        }
    }
}

/**
 * 2. Tabbed Smart Sidebar Paradigm
 */
@Composable
fun TabbedSidebarLayout(
    apps: List<AppInfo>,
    onAppClick: (String) -> Unit,
    onOpenSearch: () -> Unit
) {
    var selectedShelf by remember { mutableIntStateOf(0) }
    val shelves = listOf("Favorites", "Tools", "Media", "Telemetry")

    Row(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        // Edge Sidebar Tabs
        Surface(
            modifier = Modifier.width(72.dp).fillMaxHeight(),
            shape = RoundedCornerShape(20.dp),
            color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.75f),
            border = BorderStroke(1.dp, ShivTheme.colors.primary.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                TrishulIcon(size = 24.dp)
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    shelves.forEachIndexed { index, title ->
                        val isSelected = selectedShelf == index
                        IconButton(
                            onClick = { selectedShelf = index },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ShivTheme.colors.primary.copy(alpha = 0.25f) else Color.Transparent)
                        ) {
                            Icon(
                                when (index) {
                                    0 -> Icons.Default.Star
                                    1 -> Icons.Default.Build
                                    2 -> Icons.Default.MusicNote
                                    else -> Icons.Default.Speed
                                },
                                contentDescription = title,
                                tint = if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.textSecondary
                            )
                        }
                    }
                }
                IconButton(onClick = onOpenSearch) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = ShivTheme.colors.textPrimary)
                }
            }
        }

        // Active Shelf Apps Display
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Text(
                "${shelves[selectedShelf].uppercase()} SHELF",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = ShivTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(apps.take(12), key = { it.packageName }) { app ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onAppClick(app.packageName) },
                        color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, ShivTheme.colors.cardBorder.copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(ShivTheme.colors.primary))
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(app.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ShivTheme.colors.textPrimary)
                                Text(app.packageName, style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. Text-Only Minimalist Paradigm
 */
@Composable
fun TextOnlyMinimalLayout(
    apps: List<AppInfo>,
    onAppClick: (String) -> Unit
) {
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFmt = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()) }
    val now = remember { Date() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Text(timeFmt.format(now), style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Light), color = ShivTheme.colors.textPrimary)
        Text(dateFmt.format(now), style = MaterialTheme.typography.titleSmall, color = ShivTheme.colors.textSecondary)
        Spacer(modifier = Modifier.height(32.dp))

        Text("ESSENTIALS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp), color = ShivTheme.colors.primary)
        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items(apps.take(10), key = { it.packageName }) { app ->
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Normal),
                    color = ShivTheme.colors.textPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAppClick(app.packageName) }
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * 4. Search-Gated Focus Paradigm
 */
@Composable
fun SearchGatedLayout(
    apps: List<AppInfo>,
    onAppClick: (String) -> Unit,
    onOpenSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TrishulIcon(size = 48.dp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Intentional Focus",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = ShivTheme.colors.textPrimary
        )
        Text(
            "No distractions. Search to open any app.",
            style = MaterialTheme.typography.bodyMedium,
            color = ShivTheme.colors.textSecondary
        )
        Spacer(modifier = Modifier.height(28.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .clickable(onClick = onOpenSearch),
            color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.75f),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.5.dp, ShivTheme.colors.primary.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = ShivTheme.colors.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Tap to type & search apps...", style = MaterialTheme.typography.bodyMedium, color = ShivTheme.colors.textSecondary)
            }
        }
    }
}

/**
 * 5. Glassmorphic Immersive Blur Paradigm
 */
@Composable
fun GlassmorphicBlurLayout(
    apps: List<AppInfo>,
    onAppClick: (String) -> Unit,
    onOpenDrawer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Floating Frosted Hero Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = Color(0x3320252D),
            border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("GLASSMORPHIC DEPTH", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = Color(0xFF55E7FF))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Multi-layer Frosted Acrylic", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                Text("Refractive specular light sheen across ambient depth", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            }
        }

        // Frosted Glass Apps Grid
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(apps.take(8), key = { it.packageName }) { app ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onAppClick(app.packageName) },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0x2820252D),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0x4455E7FF)))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(app.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * 6. Magazine Editorial Asymmetric Paradigm
 */
@Composable
fun MagazineEditorialLayout(
    apps: List<AppInfo>,
    onAppClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Magazine Masthead
        Text(
            "THE DAILY DISPATCH",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
            color = ShivTheme.colors.primary
        )
        Text(
            "Design & Cognitive Architecture",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif),
            color = ShivTheme.colors.textPrimary
        )
        HorizontalDivider(thickness = 1.5.dp, color = ShivTheme.colors.textPrimary.copy(alpha = 0.4f))

        // Asymmetric Column Spans
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Large Hero App Card (Span 2)
            if (apps.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .weight(1.5f)
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onAppClick(apps[0].packageName) },
                    color = ShivTheme.colors.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Text("LEAD ARTICLE", style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.primary)
                        Text(apps[0].name, style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold), color = ShivTheme.colors.textPrimary)
                        Text("Open Feature App", style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
                    }
                }
            }

            // Stacked Side Cards
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                apps.drop(1).take(2).forEach { app ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(75.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onAppClick(app.packageName) },
                        color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.CenterStart) {
                            Text(app.name, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold), color = ShivTheme.colors.textPrimary)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 7. Terminal / Cyberpunk Hacker CLI Paradigm
 */
@Composable
fun TerminalHackerLayout(
    apps: List<AppInfo>,
    onAppClick: (String) -> Unit
) {
    val phosphorGreen = Color(0xFF00FF66)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0A0C0A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("SHIV-OS KERNEL v8.4.2 [TTY1]", fontFamily = FontFamily.Monospace, color = phosphorGreen, fontSize = 12.sp)
            Text("MEMORY: 8192 MB OK | ALL DAEMONS HEALTHY", fontFamily = FontFamily.Monospace, color = phosphorGreen.copy(alpha = 0.7f), fontSize = 11.sp)
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 1.dp, color = phosphorGreen.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(10.dp))

            Text("shiv@os:~$ ls -la /apps", fontFamily = FontFamily.Monospace, color = Color.White, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(apps.take(14), key = { it.packageName }) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAppClick(app.packageName) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("-rwxr-xr-x ", fontFamily = FontFamily.Monospace, color = phosphorGreen.copy(alpha = 0.5f), fontSize = 11.sp)
                        Text(app.name.lowercase().replace(" ", "_"), fontFamily = FontFamily.Monospace, color = phosphorGreen, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

/**
 * 8. 3D Holographic Parallax Paradigm
 */
@Composable
fun Holographic3DLayout(
    apps: List<AppInfo>,
    onAppClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Holographic Spatial Hero Card with 3D Tilt
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .holographicTilt(maxTiltDegrees = 14f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x33090D1F))
                .border(1.5.dp, Color(0xFF55E7FF), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Text("SPATIAL 3D PERSPECTIVE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF55E7FF))
                Text("Tilt Device to Experience Holographic Parallax Depth", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                Text("Gyroscope & Touch Reactive Specular Sheen", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
            }
        }

        // Holographic App Cards
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(apps.take(8), key = { it.packageName }) { app ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .holographicTilt(maxTiltDegrees = 8f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x2212172F))
                        .border(1.dp, Color(0x667657FF), RoundedCornerShape(14.dp))
                        .clickable { onAppClick(app.packageName) }
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFF55E7FF)))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(app.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                }
            }
        }
    }
}
