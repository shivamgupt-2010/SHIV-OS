package com.example.ui.widgets

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ShivTheme

data class WidgetArchetypeItem(
    val key: String,
    val title: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomWidgetStudioDialog(
    onDismissRequest: () -> Unit,
    onSaveAndPlace: (archetype: String, surfaceStyle: String, radiusDp: Int) -> Unit
) {
    val archetypes = remember {
        listOf(
            WidgetArchetypeItem("CLOCK", "Clock", "Minimal digital & date time"),
            WidgetArchetypeItem("TELEMETRY", "Telemetry", "RAM, Battery & CPU monitor"),
            WidgetArchetypeItem("WEATHER", "Weather", "Daylight horizon & temperature"),
            WidgetArchetypeItem("AGENDA", "Agenda", "Upcoming calendar glance"),
            WidgetArchetypeItem("MEDIA", "Media Pill", "Quick music controller"),
            WidgetArchetypeItem("SHIV_AI", "ShivAI Action", "Cognitive assistant prompt")
        )
    }

    val surfaceStyles = remember {
        listOf(
            Pair("GLASS", "Frosted Glass"),
            Pair("MATTE", "Obsidian Matte"),
            Pair("HOLOGRAM", "Neon Hologram"),
            Pair("TERMINAL", "Phosphor CLI")
        )
    }

    var selectedArchetype by remember { mutableStateOf("CLOCK") }
    var selectedSurfaceStyle by remember { mutableStateOf("GLASS") }
    var selectedRadiusDp by remember { mutableIntStateOf(18) }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(24.dp),
            color = ShivTheme.colors.surface,
            border = BorderStroke(1.5.dp, ShivTheme.colors.primary.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = ShivTheme.colors.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Custom Widget Studio",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = ShivTheme.colors.textPrimary
                            )
                            Text(
                                "Build & customize bespoke widgets",
                                style = MaterialTheme.typography.labelSmall,
                                color = ShivTheme.colors.textSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ShivTheme.colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Live Preview Area
                    Text(
                        "LIVE WIDGET PREVIEW",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = ShivTheme.colors.primary
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(selectedRadiusDp.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .padding(8.dp)
                    ) {
                        when (selectedArchetype) {
                            "CLOCK" -> CustomClockWidget(
                                surfaceStyle = selectedSurfaceStyle,
                                cornerRadiusDp = selectedRadiusDp
                            )
                            "TELEMETRY" -> CustomTelemetryWidget(
                                surfaceStyle = selectedSurfaceStyle,
                                cornerRadiusDp = selectedRadiusDp
                            )
                            "WEATHER" -> CustomWeatherWidget(
                                surfaceStyle = selectedSurfaceStyle,
                                cornerRadiusDp = selectedRadiusDp
                            )
                            "AGENDA" -> CustomAgendaWidget(
                                surfaceStyle = selectedSurfaceStyle,
                                cornerRadiusDp = selectedRadiusDp
                            )
                            "MEDIA" -> CustomMediaWidget(
                                surfaceStyle = selectedSurfaceStyle,
                                cornerRadiusDp = selectedRadiusDp
                            )
                            else -> CustomShivAiWidget(
                                surfaceStyle = selectedSurfaceStyle,
                                cornerRadiusDp = selectedRadiusDp
                            )
                        }
                    }

                    // 1. Archetype Selector
                    Text(
                        "1. Choose Widget Archetype",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = ShivTheme.colors.textPrimary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        archetypes.forEach { item ->
                            val isSelected = selectedArchetype == item.key
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedArchetype = item.key },
                                color = if (isSelected) ShivTheme.colors.primary.copy(alpha = 0.25f) else ShivTheme.colors.surfaceVariant.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.cardBorder.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                                    Text(
                                        item.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.textPrimary
                                    )
                                    Text(
                                        item.description,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = ShivTheme.colors.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    // 2. Surface Style Selector
                    Text(
                        "2. Surface Texture & Glow",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = ShivTheme.colors.textPrimary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        surfaceStyles.forEach { (key, title) ->
                            val isSelected = selectedSurfaceStyle == key
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedSurfaceStyle = key },
                                color = if (isSelected) ShivTheme.colors.primary.copy(alpha = 0.25f) else ShivTheme.colors.surfaceVariant.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.cardBorder.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    title,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.textPrimary
                                )
                            }
                        }
                    }

                    // 3. Corner Radius Slider
                    Text(
                        "3. Corner Rounding: ${selectedRadiusDp}dp",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = ShivTheme.colors.textPrimary
                    )
                    Slider(
                        value = selectedRadiusDp.toFloat(),
                        onValueChange = { selectedRadiusDp = it.toInt() },
                        valueRange = 0f..28f,
                        steps = 6,
                        colors = SliderDefaults.colors(
                            thumbColor = ShivTheme.colors.primary,
                            activeTrackColor = ShivTheme.colors.primary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action CTA
                Button(
                    onClick = {
                        onSaveAndPlace(selectedArchetype, selectedSurfaceStyle, selectedRadiusDp)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.primary)
                ) {
                    Icon(Icons.Default.Widgets, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save & Place on Home Grid", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
