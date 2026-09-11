package com.example.ui.tools

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.agents.BottomBarTab
import com.example.ui.components.CosmicBackground
import com.example.ui.theme.*

data class ToolItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    onNavigate: (String) -> Unit,
    onOpenSidebar: () -> Unit
) {
    val context = LocalContext.current
    var showTranslatorDialog by remember { mutableStateOf(false) }
    var showWebSearchDialog by remember { mutableStateOf(false) }

    val tools = listOf(
        ToolItem("calc", "Calculator", "Quick calculations", Icons.Default.Calculate, ShivaBlue),
        ToolItem("translate", "Translator", "Multi-language", Icons.Default.Translate, ShivaGreen),
        ToolItem("files", "File Manager", "Browse & manage", Icons.Default.Folder, ShivaPurple),
        ToolItem("voice", "Voice to Text", "Speak → Text", Icons.Default.Mic, ShivaPink),
        ToolItem("image", "Image Tools", "Edit & generate", Icons.Default.Image, ShivaOrange),
        ToolItem("search", "Web Search", "Search the web", Icons.Default.Search, ShivaCyan)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        CosmicBackground(showSilhouette = false)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onOpenSidebar) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Build, contentDescription = null, tint = ShivaCyan, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tools",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
            },
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CosmicSurface.copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BottomBarTab(icon = Icons.Default.Home, label = "Home", isSelected = false, onClick = { onNavigate("launcher") })
                        BottomBarTab(icon = Icons.Default.SmartToy, label = "Agents", isSelected = false, onClick = { onNavigate("agents") })
                        BottomBarTab(icon = Icons.Default.Description, label = "Notes", isSelected = false, onClick = { onNavigate("notes") })
                        BottomBarTab(icon = Icons.Default.Settings, label = "Settings", isSelected = false, onClick = { onNavigate("settings") })
                    }
                }
            }
        ) { paddingValues ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(tools, key = { it.id }) { tool ->
                    ToolCard(
                        tool = tool,
                        onClick = {
                            when (tool.id) {
                                "calc" -> {
                                    try {
                                        val intent = Intent().apply {
                                            action = Intent.ACTION_MAIN
                                            addCategory(Intent.CATEGORY_APP_CALCULATOR)
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        onNavigate("chat")
                                    }
                                }
                                "translate" -> {
                                    showTranslatorDialog = true
                                }
                                "files" -> {
                                    try {
                                        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                            type = "*/*"
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                "voice" -> {
                                    onNavigate("chat")
                                }
                                "image" -> {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        onNavigate("chat")
                                    }
                                }
                                "search" -> {
                                    showWebSearchDialog = true
                                }
                            }
                        }
                    )
                }
            }
        }

        if (showTranslatorDialog) {
            var textToTranslate by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showTranslatorDialog = false },
                containerColor = CosmicSurface,
                title = { Text("ShivAI Translator", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Translate any sentence instantly via ShivAI:", color = TextSecondary)
                        OutlinedTextField(
                            value = textToTranslate,
                            onValueChange = { textToTranslate = it },
                            placeholder = { Text("Enter text to translate...", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShivaCyan,
                                unfocusedBorderColor = CosmicCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showTranslatorDialog = false
                            onNavigate("chat")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ShivaIndigo)
                    ) {
                        Text("Translate with AI")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTranslatorDialog = false }) {
                        Text("Close", color = TextSecondary)
                    }
                }
            )
        }

        if (showWebSearchDialog) {
            var searchWebQuery by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showWebSearchDialog = false },
                containerColor = CosmicSurface,
                title = { Text("Web Search", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = searchWebQuery,
                        onValueChange = { searchWebQuery = it },
                        placeholder = { Text("Search Google / Web...", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ShivaCyan,
                            unfocusedBorderColor = CosmicCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (searchWebQuery.isNotBlank()) {
                                val url = "https://www.google.com/search?q=" + Uri.encode(searchWebQuery)
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                                showWebSearchDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ShivaIndigo)
                    ) {
                        Text("Search Web")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWebSearchDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
fun ToolCard(tool: ToolItem, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        color = CosmicSurface,
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder)
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(tool.color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(tool.icon, contentDescription = null, tint = tool.color, modifier = Modifier.size(22.dp))
            }

            Column {
                Text(
                    text = tool.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Text(
                    text = tool.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextSecondary
                )
            }
        }
    }
}
