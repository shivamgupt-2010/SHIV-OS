package com.example.ui.launcher

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.launcher.AppInfo
import com.example.ui.components.CosmicBackground
import com.example.ui.components.TrishulIcon
import com.example.ui.theme.*

fun matchAppSearch(query: String, name: String, packageName: String): Boolean {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return true
    val n = name.lowercase()
    val p = packageName.lowercase()

    // 1. Direct substring match
    if (n.contains(q) || p.contains(q)) return true

    // 2. Acronym match (e.g. "yt" matches "YouTube")
    val words = n.split(" ", "_", "-")
    val acronym = words.mapNotNull { it.firstOrNull()?.lowercaseChar() }.joinToString("")
    if (acronym.startsWith(q)) return true

    // 3. Typo-tolerant subsequence match (characters in relative order)
    if (q.length in 2..8) {
        var qIdx = 0
        for (char in n) {
            if (char == q[qIdx]) {
                qIdx++
                if (qIdx == q.length) return true
            }
        }
    }
    return false
}

fun classifyAppCategory(packageName: String, name: String): String {
    val p = "$packageName $name".lowercase()
    return when {
        p.contains("call") || p.contains("phone") || p.contains("dial") || p.contains("messag") ||
                p.contains("sms") || p.contains("chat") || p.contains("mail") || p.contains("contact") -> "Communication"
        p.contains("doc") || p.contains("sheet") || p.contains("note") || p.contains("calc") ||
                p.contains("calendar") || p.contains("pdf") || p.contains("drive") || p.contains("office") -> "Productivity"
        p.contains("camera") || p.contains("photo") || p.contains("video") || p.contains("music") ||
                p.contains("media") || p.contains("audio") || p.contains("youtube") || p.contains("player") -> "Media"
        p.contains("setting") || p.contains("system") || p.contains("android") || p.contains("security") -> "System"
        else -> "Tools"
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppDrawerScreen(
    viewModel: LauncherViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {}
) {
    val allApps by viewModel.installedApps.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedAppForOptions by remember { mutableStateOf<AppInfo?>(null) }
    val context = LocalContext.current

    // Debounce search query by 300ms to eliminate recomposition thrash while typing
    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            debouncedQuery = ""
        } else {
            kotlinx.coroutines.delay(300L)
            debouncedQuery = searchQuery
        }
    }

    val categories = listOf("All", "Communication", "Productivity", "Media", "Tools", "System")

    val filteredApps by remember(allApps, debouncedQuery, selectedCategory) {
        derivedStateOf {
            allApps.filter { app ->
                val matchesCategory = if (selectedCategory == "All") true
                else classifyAppCategory(app.packageName, app.name) == selectedCategory

                val matchesText = matchAppSearch(debouncedQuery, app.name, app.packageName)
                matchesCategory && matchesText
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val currentWallpaper by viewModel.currentWallpaper.collectAsState()
        com.example.core.studio.LauncherWallpaperView(config = currentWallpaper)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onBack),
                            color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.6f),
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ShivTheme.colors.textPrimary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        TrishulIcon(size = 20.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "All Apps (${filteredApps.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ShivTheme.colors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 100% On-Device Private Search Bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = ShivTheme.colors.surface.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, ShivTheme.colors.primary.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = ShivTheme.colors.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Filter applications on-device...", color = ShivTheme.colors.textSecondary, fontSize = 14.sp) },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = ShivTheme.colors.textPrimary,
                                    unfocusedTextColor = ShivTheme.colors.textPrimary
                                ),
                                singleLine = true
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = ShivTheme.colors.textSecondary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Filter Pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            val isSelected = selectedCategory == category
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedCategory = category },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) ShivTheme.colors.primary.copy(alpha = 0.2f) else ShivTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.cardBorder.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = category,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                // If searching, show "Ask ShivAI" action pill
                if (searchQuery.isNotBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(ShivTheme.cardRadius))
                            .clickable { onNavigate("chat") },
                        color = ShivTheme.colors.primary.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, ShivTheme.colors.primary.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(ShivTheme.cardRadius)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TrishulIcon(size = 18.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ask ShivAI about \"$searchQuery\"",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ShivTheme.colors.textPrimary
                                )
                                Text(
                                    text = "Cognitive reasoning, web research & smart actions",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ShivTheme.colors.textSecondary
                                )
                            }
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = ShivTheme.colors.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(currentTheme.gridColumns.coerceIn(3, 5)),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        AppGridItem(
                            app = app,
                            iconShape = currentTheme.iconShape,
                            scalePercent = currentTheme.iconScalePercent,
                            onClick = { viewModel.onAppClicked(app.packageName) },
                            onLongClick = { selectedAppForOptions = app }
                        )
                    }
                }
            }
        }

        // App Long-Press Options Bottom Sheet
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

                    // App Details
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
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppGridItem(
    app: AppInfo,
    iconShape: String = "SQUIRCLE",
    scalePercent: Int = 100,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val cachedIcon = remember(app.packageName, iconShape) {
        com.example.core.launcher.AppIconManager.getCachedIcon(app.packageName, iconShape)
    }
    val appIcon = produceState(initialValue = cachedIcon, key1 = app.packageName, key2 = iconShape) {
        if (value == null) {
            value = com.example.core.launcher.AppIconManager.loadIcon(context, app.packageName, iconShape)
        }
    }.value

    val shapeCorner = when (iconShape) {
        "CIRCLE" -> CircleShape
        "ROUNDED_RECT" -> RoundedCornerShape(8.dp)
        "TEARDROP" -> RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 4.dp)
        else -> RoundedCornerShape(16.dp)
    }

    val baseSize = 56.dp
    val scaledSize = (baseSize.value * (scalePercent / 100f)).dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(scaledSize)
                .clip(shapeCorner)
                .background(ShivTheme.colors.surfaceVariant.copy(alpha = 0.6f))
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
                    0 -> ShivaCyan
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
