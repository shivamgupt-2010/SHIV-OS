package com.example.ui.wellbeing

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.launcher.AppInfo
import com.example.core.wellbeing.AppUsageRecord
import com.example.core.wellbeing.WellbeingManager
import com.example.ui.components.CosmicBackground
import com.example.ui.components.TrishulIcon
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellbeingScreen(
    wellbeingManager: WellbeingManager,
    installedApps: List<AppInfo>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(wellbeingManager.hasUsagePermission()) }
    var todayStats by remember { mutableStateOf<List<AppUsageRecord>>(emptyList()) }
    var totalMinutes by remember { mutableStateOf(0L) }
    val focusSession by wellbeingManager.focusSession.collectAsState()
    val budgets by wellbeingManager.budgets.collectAsState()

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            todayStats = wellbeingManager.getTodayUsageStats().take(10)
            totalMinutes = wellbeingManager.getTotalScreenTimeMinutesToday()
        }
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
                            Text("SHIV Wellbeing", fontWeight = FontWeight.Bold)
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
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Permission Card if Missing
                if (!hasPermission) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(ShivTheme.cardRadius),
                        colors = CardDefaults.cardColors(containerColor = ShivTheme.colors.surfaceVariant),
                        border = BorderStroke(1.dp, ShivaOrange.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = ShivaOrange)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Usage Access Required", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                            }
                            Text("To display real daily screen-time metrics and app budgets locally on your device, please grant Usage Access in Android Settings.", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                            Button(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ShivaOrange)
                            ) {
                                Text("Grant Usage Permission", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Screen Time Hero Card
                val hours = totalMinutes / 60
                val mins = totalMinutes % 60
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(ShivTheme.cardRadius),
                    colors = CardDefaults.cardColors(containerColor = ShivTheme.colors.surfaceVariant.copy(alpha = 0.8f)),
                    border = BorderStroke(1.dp, ShivTheme.colors.primary.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("SCREEN TIME TODAY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ShivTheme.colors.primary, letterSpacing = 1.5.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${hours}h ${mins}m",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Light,
                            color = ShivTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Usage data processed 100% locally on-device", style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
                    }
                }

                // Focus Session Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(ShivTheme.cardRadius),
                    colors = CardDefaults.cardColors(containerColor = ShivTheme.colors.surface.copy(alpha = 0.85f)),
                    border = BorderStroke(1.dp, if (focusSession.isActive) ShivTheme.colors.primary else ShivTheme.colors.cardBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HourglassTop, contentDescription = null, tint = ShivTheme.colors.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(focusSession.title, fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                            }
                            Surface(
                                color = if (focusSession.isActive) ShivTheme.colors.primary.copy(alpha = 0.2f) else ShivTheme.colors.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (focusSession.isActive) "RUNNING" else "READY",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (focusSession.isActive) ShivTheme.colors.primary else ShivTheme.colors.textSecondary
                                )
                            }
                        }

                        Text(
                            text = if (focusSession.isActive) "Focus active: notifications muted, distraction apps held back." else "Start a focused deep work or study session with zero distractions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ShivTheme.colors.textSecondary
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (!focusSession.isActive) {
                                Button(
                                    onClick = { wellbeingManager.startFocusSession(25, "Deep Work") },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Start 25m Session")
                                }
                                FilledTonalButton(
                                    onClick = { wellbeingManager.startFocusSession(45, "Study Flow") },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Start 45m Session")
                                }
                            } else {
                                Button(
                                    onClick = { wellbeingManager.stopFocusSession() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Stop Focus Session")
                                }
                            }
                        }
                    }
                }

                // Top Apps Usage List
                Text("App Usage Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)

                if (todayStats.isEmpty()) {
                    Text("No usage recorded yet today.", style = MaterialTheme.typography.bodyMedium, color = ShivTheme.colors.textSecondary)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        todayStats.forEach { stat ->
                            val appName = installedApps.firstOrNull { it.packageName == stat.packageName }?.name ?: stat.packageName
                            val appHours = stat.totalTimeInForegroundMinutes / 60
                            val appMins = stat.totalTimeInForegroundMinutes % 60
                            val pct = if (totalMinutes > 0) (stat.totalTimeInForegroundMinutes.toFloat() / totalMinutes).coerceIn(0f, 1f) else 0f

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = ShivTheme.colors.surface.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, ShivTheme.colors.cardBorder.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(appName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                                        Text(if (appHours > 0) "${appHours}h ${appMins}m" else "${appMins}m", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ShivTheme.colors.primary)
                                    }
                                    LinearProgressIndicator(
                                        progress = { pct },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                        color = ShivTheme.colors.primary,
                                        trackColor = ShivTheme.colors.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
