package com.example.ui.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import com.example.ui.theme.ShivTheme

data class AppWidgetGroup(
    val appPackage: String,
    val appLabel: String,
    val appIcon: Drawable?,
    val widgets: List<AppWidgetProviderInfo>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppWidgetPickerDialog(
    onDismissRequest: () -> Unit,
    onWidgetSelected: (AppWidgetProviderInfo) -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val appWidgetManager = remember { AppWidgetManager.getInstance(context) }

    var searchQuery by remember { mutableStateOf("") }

    val widgetGroups = remember {
        val providers = appWidgetManager.installedProviders ?: emptyList()
        val grouped = providers.groupBy { it.provider.packageName }
        grouped.mapNotNull { (pkg, list) ->
            try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                val label = pm.getApplicationLabel(appInfo).toString()
                val icon = try { pm.getApplicationIcon(appInfo) } catch (e: Exception) { null }
                AppWidgetGroup(
                    appPackage = pkg,
                    appLabel = label,
                    appIcon = icon,
                    widgets = list
                )
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.appLabel.lowercase() }
    }

    val filteredGroups = remember(searchQuery, widgetGroups) {
        if (searchQuery.isBlank()) widgetGroups
        else widgetGroups.filter { group ->
            group.appLabel.contains(searchQuery, ignoreCase = true) ||
            group.widgets.any { it.loadLabel(pm).contains(searchQuery, ignoreCase = true) }
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
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
                        Icon(Icons.Default.Widgets, contentDescription = null, tint = ShivTheme.colors.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Add App Widget",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = ShivTheme.colors.textPrimary
                            )
                            Text(
                                "From installed apps",
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

                // Search Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, ShivTheme.colors.cardBorder.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = ShivTheme.colors.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search widgets & apps...", fontSize = 13.sp, color = ShivTheme.colors.textSecondary) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Widget List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredGroups, key = { it.appPackage }) { group ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // App Header
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                group.appIcon?.let { drawable ->
                                    val bitmap = remember(drawable) { drawable.toBitmap(48, 48).asImageBitmap() }
                                    Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    group.appLabel,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ShivTheme.colors.textPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "(${group.widgets.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ShivTheme.colors.textSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Provider cards
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                group.widgets.forEach { provider ->
                                    val label = provider.loadLabel(pm)
                                    val spanCols = (provider.minWidth / 70).coerceIn(1, 4)
                                    val spanRows = (provider.minHeight / 70).coerceIn(1, 4)

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { onWidgetSelected(provider) },
                                        color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, ShivTheme.colors.cardBorder.copy(alpha = 0.25f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    label,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                    color = ShivTheme.colors.textPrimary
                                                )
                                                Text(
                                                    "${spanCols}x${spanRows} Grid Size",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = ShivTheme.colors.primary
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = ShivTheme.colors.primary.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    "Add",
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = ShivTheme.colors.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
