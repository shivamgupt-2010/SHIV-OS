package com.example.ui.widgets

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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
