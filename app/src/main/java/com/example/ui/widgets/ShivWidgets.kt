package com.example.ui.widgets

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import com.example.core.wellbeing.WellbeingManager
import com.example.ui.components.TrishulIcon
import com.example.ui.theme.ShivTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * 1. Minimalist Analog Clock Widget
 */
@Composable
fun AnalogClockWidget(
    modifier: Modifier = Modifier,
    primaryColor: Color = ShivTheme.colors.primary,
    accentColor: Color = ShivTheme.colors.accent,
    surfaceColor: Color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.7f)
) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Calendar.getInstance()
            kotlinx.coroutines.delay(1000L)
        }
    }

    val hours = currentTime.get(Calendar.HOUR)
    val minutes = currentTime.get(Calendar.MINUTE)
    val seconds = currentTime.get(Calendar.SECOND)

    Surface(
        modifier = modifier
            .size(160.dp)
            .clip(CircleShape),
        shape = CircleShape,
        color = surfaceColor,
        border = BorderStroke(1.5.dp, primaryColor.copy(alpha = 0.4f))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f

            // Hour tick marks
            for (i in 0 until 12) {
                val angle = Math.toRadians((i * 30.0) - 90)
                val startRadius = radius - 10f
                val endRadius = radius - 3f
                val start = Offset(
                    center.x + (startRadius * cos(angle)).toFloat(),
                    center.y + (startRadius * sin(angle)).toFloat()
                )
                val end = Offset(
                    center.x + (endRadius * cos(angle)).toFloat(),
                    center.y + (endRadius * sin(angle)).toFloat()
                )
                drawLine(
                    color = primaryColor.copy(alpha = if (i % 3 == 0) 0.8f else 0.35f),
                    start = start,
                    end = end,
                    strokeWidth = if (i % 3 == 0) 3f else 1.5f,
                    cap = StrokeCap.Round
                )
            }

            // Hour Hand
            val hourAngle = Math.toRadians(((hours % 12 + minutes / 60.0) * 30.0) - 90)
            val hourLength = radius * 0.52f
            drawLine(
                color = Color.White,
                start = center,
                end = Offset(
                    center.x + (hourLength * cos(hourAngle)).toFloat(),
                    center.y + (hourLength * sin(hourAngle)).toFloat()
                ),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )

            // Minute Hand
            val minAngle = Math.toRadians(((minutes + seconds / 60.0) * 6.0) - 90)
            val minLength = radius * 0.72f
            drawLine(
                color = primaryColor,
                start = center,
                end = Offset(
                    center.x + (minLength * cos(minAngle)).toFloat(),
                    center.y + (minLength * sin(minAngle)).toFloat()
                ),
                strokeWidth = 2.8f,
                cap = StrokeCap.Round
            )

            // Second Hand
            val secAngle = Math.toRadians((seconds * 6.0) - 90)
            val secLength = radius * 0.82f
            drawLine(
                color = accentColor,
                start = center,
                end = Offset(
                    center.x + (secLength * cos(secAngle)).toFloat(),
                    center.y + (secLength * sin(secAngle)).toFloat()
                ),
                strokeWidth = 1.5f,
                cap = StrokeCap.Round
            )

            // Center Pin
            drawCircle(color = accentColor, radius = 4f, center = center)
        }
    }
}

/**
 * 2. Agenda & Calendar Glance Widget
 */
@Composable
fun AgendaGlanceWidget(
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {}
) {
    val dateFmt = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    val todayText = remember { dateFmt.format(Date()) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ShivTheme.cardRadius))
            .clickable(onClick = onTap),
        shape = RoundedCornerShape(ShivTheme.cardRadius),
        color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, ShivTheme.colors.cardBorder.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ShivTheme.colors.primary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = ShivTheme.colors.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(todayText, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = ShivTheme.colors.textPrimary)
                Text("No conflicts scheduled • All clear today", style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ShivTheme.colors.textSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

/**
 * 3. Digital Wellbeing Glance Widget
 */
@Composable
fun WellbeingGlanceWidget(
    wellbeingManager: WellbeingManager,
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {}
) {
    val dailyUnlocks by wellbeingManager.dailyUnlockCount.collectAsState()
    val totalMinutes = remember { wellbeingManager.getTotalScreenTimeMinutesToday() }
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ShivTheme.cardRadius))
            .clickable(onClick = onTap),
        shape = RoundedCornerShape(ShivTheme.cardRadius),
        color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, ShivTheme.colors.cardBorder.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ShivTheme.colors.accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PieChart, contentDescription = null, tint = ShivTheme.colors.accent, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (hours > 0) "${hours}h ${mins}m" else "${mins}m screen time",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = ShivTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• $dailyUnlocks unlocks",
                        style = MaterialTheme.typography.labelSmall,
                        color = ShivTheme.colors.textSecondary
                    )
                }
                Text("Tap for detailed digital wellbeing dashboard", style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ShivTheme.colors.textSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

/**
 * 4. Quick Note Glance Widget
 */
@Composable
fun QuickNoteGlanceWidget(
    modifier: Modifier = Modifier,
    onOpenNotes: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ShivTheme.cardRadius))
            .clickable(onClick = onOpenNotes),
        shape = RoundedCornerShape(ShivTheme.cardRadius),
        color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, ShivTheme.colors.cardBorder.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ShivTheme.colors.primary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.EditNote, contentDescription = null, tint = ShivTheme.colors.primary, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("SHIV Notes & Doodles", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = ShivTheme.colors.textPrimary)
                Text("Capture thoughts, checklists, drawings & media", style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
            }
            Icon(Icons.Default.Add, contentDescription = "Add", tint = ShivTheme.colors.primary, modifier = Modifier.size(20.dp))
        }
    }
}

/**
 * 5. Android AppWidget Host Container (For 3rd Party Widgets)
 */
@Composable
fun HostedAppWidgetView(
    appWidgetHost: AppWidgetHost,
    appWidgetId: Int,
    appWidgetInfo: AppWidgetProviderInfo,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { ctx ->
            val hostView = appWidgetHost.createView(ctx, appWidgetId, appWidgetInfo)
            hostView.setAppWidget(appWidgetId, appWidgetInfo)
            hostView
        },
        modifier = modifier
    )
}


/**
 * 6 Custom Widget Archetypes for the Custom Widget Creation Studio
 */

@Composable
fun CustomClockWidget(
    modifier: Modifier = Modifier,
    surfaceStyle: String = "GLASS",
    primaryColor: Color = ShivTheme.colors.primary,
    accentColor: Color = ShivTheme.colors.accent,
    cornerRadiusDp: Int = 18
) {
    var timeStr by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFmt = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        while (true) {
            val now = Date()
            timeStr = timeFmt.format(now)
            dateStr = dateFmt.format(now)
            kotlinx.coroutines.delay(1000L)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadiusDp.dp)),
        shape = RoundedCornerShape(cornerRadiusDp.dp),
        color = when (surfaceStyle) {
            "MATTE" -> Color(0xFF141416)
            "HOLOGRAM" -> Color(0x33090D1F)
            "TERMINAL" -> Color(0xFF0A0C0A)
            else -> ShivTheme.colors.surfaceVariant.copy(alpha = 0.75f)
        },
        border = BorderStroke(
            if (surfaceStyle == "HOLOGRAM") 2.dp else 1.dp,
            when (surfaceStyle) {
                "TERMINAL" -> Color(0xFF00FF66)
                "HOLOGRAM" -> primaryColor
                else -> primaryColor.copy(alpha = 0.4f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = timeStr,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = if (surfaceStyle == "TERMINAL") Color(0xFF00FF66) else primaryColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateStr,
                style = MaterialTheme.typography.labelMedium,
                color = if (surfaceStyle == "TERMINAL") Color(0xFF00CC55) else ShivTheme.colors.textSecondary
            )
        }
    }
}

@Composable
fun CustomTelemetryWidget(
    modifier: Modifier = Modifier,
    surfaceStyle: String = "GLASS",
    primaryColor: Color = ShivTheme.colors.primary,
    accentColor: Color = ShivTheme.colors.accent,
    cornerRadiusDp: Int = 18
) {
    val context = LocalContext.current
    val batteryManager = remember { context.getSystemService(Context.BATTERY_SERVICE) as? android.os.BatteryManager }
    val batteryLevel = remember { batteryManager?.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 82 }

    val actManager = remember { context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager }
    val memInfo = remember {
        val info = android.app.ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(info)
        val usedMb = ((info.totalMem - info.availMem) / (1024 * 1024)).toInt()
        val totalMb = (info.totalMem / (1024 * 1024)).toInt()
        val percent = if (totalMb > 0) ((usedMb.toFloat() / totalMb) * 100).toInt() else 65
        Pair(percent, "$usedMb / $totalMb MB")
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadiusDp.dp)),
        shape = RoundedCornerShape(cornerRadiusDp.dp),
        color = when (surfaceStyle) {
            "MATTE" -> Color(0xFF141416)
            "HOLOGRAM" -> Color(0x33090D1F)
            "TERMINAL" -> Color(0xFF0A0C0A)
            else -> ShivTheme.colors.surfaceVariant.copy(alpha = 0.75f)
        },
        border = BorderStroke(
            if (surfaceStyle == "HOLOGRAM") 2.dp else 1.dp,
            if (surfaceStyle == "TERMINAL") Color(0xFFFFB547) else accentColor.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "HARDWARE TELEMETRY",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = if (surfaceStyle == "TERMINAL") Color(0xFFFFB547) else ShivTheme.colors.textSecondary
                )
                Text(
                    "ONLINE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (surfaceStyle == "TERMINAL") Color(0xFF00FF66) else primaryColor
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("RAM Usage: ${memInfo.first}%", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textPrimary)
                Text(memInfo.second, style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { memInfo.first / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = primaryColor,
                trackColor = ShivTheme.colors.cardBorder.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Battery Capacity", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textPrimary)
                Text("$batteryLevel%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = accentColor)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { batteryLevel / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = accentColor,
                trackColor = ShivTheme.colors.cardBorder.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun CustomWeatherWidget(
    modifier: Modifier = Modifier,
    surfaceStyle: String = "GLASS",
    primaryColor: Color = ShivTheme.colors.primary,
    accentColor: Color = ShivTheme.colors.accent,
    cornerRadiusDp: Int = 18
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadiusDp.dp)),
        shape = RoundedCornerShape(cornerRadiusDp.dp),
        color = when (surfaceStyle) {
            "MATTE" -> Color(0xFF141416)
            "HOLOGRAM" -> Color(0x33090D1F)
            "TERMINAL" -> Color(0xFF0A0C0A)
            else -> ShivTheme.colors.surfaceVariant.copy(alpha = 0.75f)
        },
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("WEATHER & HORIZON", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = ShivTheme.colors.textSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("24°C", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = primaryColor)
                Text("Partly Sunny • H: 27° L: 19°", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textPrimary)
            }
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.WbSunny, contentDescription = null, tint = accentColor, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
fun CustomAgendaWidget(
    modifier: Modifier = Modifier,
    surfaceStyle: String = "GLASS",
    primaryColor: Color = ShivTheme.colors.primary,
    accentColor: Color = ShivTheme.colors.accent,
    cornerRadiusDp: Int = 18
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadiusDp.dp)),
        shape = RoundedCornerShape(cornerRadiusDp.dp),
        color = when (surfaceStyle) {
            "MATTE" -> Color(0xFF141416)
            "HOLOGRAM" -> Color(0x33090D1F)
            "TERMINAL" -> Color(0xFF0A0C0A)
            else -> ShivTheme.colors.surfaceVariant.copy(alpha = 0.75f)
        },
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("UPCOMING AGENDA", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = ShivTheme.colors.textSecondary)
                }
                Text("Today", style = MaterialTheme.typography.labelSmall, color = primaryColor)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Design Review & System Architecture", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ShivTheme.colors.textPrimary)
            Text("14:30 - 15:30 • Google Meet", style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
        }
    }
}

@Composable
fun CustomMediaWidget(
    modifier: Modifier = Modifier,
    surfaceStyle: String = "GLASS",
    primaryColor: Color = ShivTheme.colors.primary,
    accentColor: Color = ShivTheme.colors.accent,
    cornerRadiusDp: Int = 18
) {
    var isPlaying by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadiusDp.dp)),
        shape = RoundedCornerShape(cornerRadiusDp.dp),
        color = when (surfaceStyle) {
            "MATTE" -> Color(0xFF141416)
            "HOLOGRAM" -> Color(0x33090D1F)
            "TERMINAL" -> Color(0xFF0A0C0A)
            else -> ShivTheme.colors.surfaceVariant.copy(alpha = 0.75f)
        },
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Now Playing", style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
                Text("Cosmic Resonance", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ShivTheme.colors.textPrimary)
                Text("SHIV Soundscapes • 3:42", style = MaterialTheme.typography.labelSmall, color = primaryColor)
            }
            IconButton(onClick = { isPlaying = !isPlaying }) {
                Icon(
                    if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                    contentDescription = "Play/Pause",
                    tint = primaryColor,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
fun CustomShivAiWidget(
    modifier: Modifier = Modifier,
    surfaceStyle: String = "GLASS",
    primaryColor: Color = ShivTheme.colors.primary,
    accentColor: Color = ShivTheme.colors.accent,
    cornerRadiusDp: Int = 18,
    onTap: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadiusDp.dp))
            .clickable(onClick = onTap),
        shape = RoundedCornerShape(cornerRadiusDp.dp),
        color = when (surfaceStyle) {
            "MATTE" -> Color(0xFF141416)
            "HOLOGRAM" -> Color(0x33090D1F)
            "TERMINAL" -> Color(0xFF0A0C0A)
            else -> ShivTheme.colors.surfaceVariant.copy(alpha = 0.75f)
        },
        border = BorderStroke(
            if (surfaceStyle == "HOLOGRAM") 2.dp else 1.2.dp,
            Brush.linearGradient(listOf(primaryColor, accentColor))
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrishulIcon(size = 24.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "SHIV AI Cognitive Assistant",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ShivTheme.colors.textPrimary
                )
                Text(
                    "Tap to ask, automate actions or research",
                    style = MaterialTheme.typography.labelSmall,
                    color = ShivTheme.colors.textSecondary
                )
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
        }
    }
}

/**
 * Universal dispatcher for in-launcher custom widgets
 */
@Composable
fun RenderCustomWidget(
    archetype: String,
    configJson: String = "",
    modifier: Modifier = Modifier,
    onTap: (() -> Unit)? = null
) {
    val style = remember(configJson) {
        when {
            configJson.contains("MATTE") -> "MATTE"
            configJson.contains("HOLOGRAM") -> "HOLOGRAM"
            configJson.contains("TERMINAL") -> "TERMINAL"
            else -> "GLASS"
        }
    }

    when (archetype.uppercase()) {
        "CLOCK" -> CustomClockWidget(modifier = modifier, surfaceStyle = style)
        "TELEMETRY" -> CustomTelemetryWidget(modifier = modifier, surfaceStyle = style)
        "WEATHER" -> CustomWeatherWidget(modifier = modifier, surfaceStyle = style)
        "AGENDA" -> CustomAgendaWidget(modifier = modifier, surfaceStyle = style)
        "MEDIA" -> CustomMediaWidget(modifier = modifier, surfaceStyle = style)
        "SHIV_AI" -> CustomShivAiWidget(modifier = modifier, surfaceStyle = style, onTap = onTap)
        else -> CustomClockWidget(modifier = modifier, surfaceStyle = style)
    }
}

/**
 * Embedded representation for installed system AppWidgets
 */
@Composable
fun RenderAppWidgetCard(
    packageName: String,
    providerClass: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val appName = remember(packageName) {
        try {
            val ai = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(ai).toString()
        } catch (e: Exception) {
            packageName.substringAfterLast('.')
        }
    }
    val appIcon = remember(packageName) {
        try {
            pm.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.8f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ShivTheme.colors.primary.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (appIcon != null) {
                Image(
                    bitmap = appIcon.toBitmap().asImageBitmap(),
                    contentDescription = appName,
                    modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ShivTheme.colors.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Widgets, contentDescription = null, tint = ShivTheme.colors.primary)
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ShivTheme.colors.textPrimary
                )
                Text(
                    text = "Installed AppWidget • Tap to launch",
                    style = MaterialTheme.typography.labelSmall,
                    color = ShivTheme.colors.textSecondary
                )
            }
            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = ShivTheme.colors.primary, modifier = Modifier.size(18.dp))
        }
    }
}

