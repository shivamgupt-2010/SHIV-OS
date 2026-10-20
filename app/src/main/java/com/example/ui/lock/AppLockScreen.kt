package com.example.ui.lock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.launcher.AppInfo
import com.example.core.lock.AppLockManager
import com.example.ui.components.CosmicBackground
import com.example.ui.components.TrishulIcon
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLockScreen(
    appLockManager: AppLockManager,
    installedApps: List<AppInfo>,
    onBack: () -> Unit
) {
    val lockedPackages by appLockManager.lockedPackages.collectAsState()
    val canAuth = remember { appLockManager.canAuthenticate() }
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CosmicBackground(showSilhouette = false)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TrishulIcon(size = 22.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("SHIV Lock", fontWeight = FontWeight.Bold)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ShivTheme.colors.textPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Security Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(ShivTheme.cardRadius),
                    colors = CardDefaults.cardColors(containerColor = ShivTheme.colors.surfaceVariant.copy(alpha = 0.8f)),
                    border = BorderStroke(1.dp, if (canAuth) ShivTheme.colors.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (canAuth) Icons.Default.Fingerprint else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (canAuth) ShivTheme.colors.primary else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Biometric & Device Auth", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                            }
                            Surface(
                                color = if (canAuth) ShivTheme.colors.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (canAuth) "PROTECTED" else "SETUP PIN/BIOMETRICS",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (canAuth) ShivTheme.colors.primary else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        Text(
                            "Locks selected applications with biometric fingerprint, face unlock, or device PIN fallback. Locked apps require authentication each time they are launched from SHIV Launcher.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ShivTheme.colors.textSecondary
                        )
                    }
                }

                // Search Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = ShivTheme.colors.surface.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, ShivTheme.colors.primary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = ShivTheme.colors.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search installed apps...", color = ShivTheme.colors.textSecondary) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = ShivTheme.colors.textPrimary,
                                unfocusedTextColor = ShivTheme.colors.textPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = ShivTheme.colors.textSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                // Apps List
                Text("Applications (${lockedPackages.size} locked)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = ShivTheme.colors.textSecondary)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        val isLocked = lockedPackages.contains(app.packageName)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp)),
                            color = ShivTheme.colors.surface.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (isLocked) ShivTheme.colors.primary.copy(alpha = 0.5f) else ShivTheme.colors.cardBorder.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(if (isLocked) ShivTheme.colors.primary.copy(alpha = 0.2f) else ShivTheme.colors.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                            contentDescription = null,
                                            tint = if (isLocked) ShivTheme.colors.primary else ShivTheme.colors.textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(app.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                                        Text(app.packageName, style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary, maxLines = 1)
                                    }
                                }
                                Switch(
                                    checked = isLocked,
                                    onCheckedChange = { appLockManager.toggleAppLock(app.packageName, app.name, it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
