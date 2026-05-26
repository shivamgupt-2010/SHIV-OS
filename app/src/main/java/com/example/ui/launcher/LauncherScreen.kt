package com.example.ui.launcher

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.launcher.AppInfo
import com.example.core.intelligence.IntelligentNotification
import com.example.ui.ambient.AmbientActivityGlow
import com.example.ui.ambient.AdaptiveFocusWidget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherScreen(viewModel: LauncherViewModel, onOpenAssistant: () -> Unit, onOpenSettings: () -> Unit) {
    val predictedApps by viewModel.predictedApps.collectAsState(emptyList())
    val workflows by viewModel.activeWorkflows.collectAsState(emptyList())
    val notifications by viewModel.intelligentNotifications.collectAsState(emptyList())
    
    var isFocusMode by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            LauncherBottomBar(onOpenAssistant, onOpenSettings)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                AIStatusHeader(isFocusMode)
            }

            item {
                AdaptiveFocusWidget(
                    isFocusMode = isFocusMode,
                    onToggleFocus = { isFocusMode = !isFocusMode }
                )
            }

            if (notifications.isNotEmpty() && !isFocusMode) {
                item {
                    Text("Intelligent Notifications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(notifications) { notif ->
                            NotificationCard(notif)
                        }
                    }
                }
            }

            if (workflows.isNotEmpty()) {
                item {
                    Text("Focus & Workflows", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(workflows, key = { it.id }) { workflow ->
                            WorkflowWidgetCard(name = workflow.name, status = workflow.status)
                        }
                    }
                }
            }

            if (predictedApps.isNotEmpty()) {
                item {
                    Text("Suggested Apps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(predictedApps, key = { it.packageName }) { app ->
                            AppChip(app = app, onClick = { viewModel.onAppClicked(app.packageName) })
                        }
                    }
                }
            }
            
            item {
                Text("All Apps", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // App Drawer flat list representation
            item {
                val allApps by viewModel.installedApps.collectAsState()
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    allApps.forEach { app ->
                        AppListItem(app = app, onClick = { viewModel.onAppClicked(app.packageName) })
                    }
                }
            }
        }
    }
}

@Composable
fun AIStatusHeader(isFocusMode: Boolean) {
    Box(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        AmbientActivityGlow(
            modifier = Modifier.fillMaxSize().offset(x = (-40).dp, y = (-20).dp),
            isActive = true,
            isThinking = false
        )
        
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                "ShivAI", 
                style = MaterialTheme.typography.displaySmall, 
                fontWeight = FontWeight.Bold, 
                color = MaterialTheme.colorScheme.onBackground
            )
            AnimatedContent(targetState = isFocusMode, label = "status") { focus ->
                Text(
                    if (focus) "Deep focus active. Listening." else "Contextually aware. Listening.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun WorkflowWidgetCard(name: String, status: String) {
    Card(
        modifier = Modifier.width(220.dp).height(120.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Text(name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium, maxLines = 2)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(status, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun NotificationCard(notification: IntelligentNotification) {
    Card(
        modifier = Modifier.width(240.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(notification.inferredCategory.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(notification.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(notification.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f), maxLines = 2)
        }
    }
}

@Composable
fun AppChip(app: AppInfo, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Text(app.name, modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AppListItem(app: AppInfo, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant))
        Spacer(modifier = Modifier.width(16.dp))
        Text(app.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun LauncherBottomBar(onOpenAssistant: () -> Unit, onOpenSettings: () -> Unit) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        tonalElevation = 0.dp
    ) {
        IconButton(onClick = onOpenSettings, modifier = Modifier.padding(start = 16.dp)) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.weight(1f))
        FloatingActionButton(
            onClick = onOpenAssistant,
            containerColor = MaterialTheme.colorScheme.primary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Default.Mic, contentDescription = "Assistant", tint = MaterialTheme.colorScheme.onPrimary)
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { /* Open App Drawer */ }, modifier = Modifier.padding(end = 16.dp)) {
            Icon(Icons.Default.Apps, contentDescription = "Apps", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
