package com.example.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.agents.BottomBarTab
import com.example.ui.components.CosmicBackground
import com.example.ui.theme.*

data class ShivNote(
    val id: String,
    val title: String,
    val subtitle: String,
    val date: String,
    val category: String,
    val icon: ImageVector,
    val iconColor: Color,
    val content: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onNavigate: (String) -> Unit,
    onOpenSidebar: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var activeNoteToView by remember { mutableStateOf<ShivNote?>(null) }

    val defaultNotes = remember {
        mutableStateListOf(
            ShivNote(
                id = "1",
                title = "JEE 2028 Roadmap",
                subtitle = "Maths | Physics | Chemistry",
                date = "12 Aug 2026",
                category = "JEE",
                icon = Icons.Default.School,
                iconColor = ShivaRed,
                content = "Comprehensive roadmap for JEE: Deep revision of Calculus, Electrodynamics, and Organic mechanisms."
            ),
            ShivNote(
                id = "2",
                title = "ShivAI Development Plan",
                subtitle = "APIs | Hosting | Future Ecosystem",
                date = "10 Aug 2026",
                category = "Projects",
                icon = Icons.Default.Shield,
                iconColor = ShivaGreen,
                content = "Deploy high-throughput cloud endpoints, expand offline intent recognizer, and optimize on-device memory."
            ),
            ShivNote(
                id = "3",
                title = "Music Releases",
                subtitle = "Navratri | Janmashtami | Diwali",
                date = "8 Aug 2026",
                category = "Ideas",
                icon = Icons.Default.MusicNote,
                iconColor = ShivaPink,
                content = "Devotional and electronic fusion tracks scheduled for festival releases."
            ),
            ShivNote(
                id = "4",
                title = "Fitness Goals",
                subtitle = "5'8\" | 58-60kg | 2 months plan",
                date = "6 Aug 2026",
                category = "Personal",
                icon = Icons.Default.FitnessCenter,
                iconColor = ShivaOrange,
                content = "Daily calorie surplus, clean protein intake, 4-day push/pull/legs split."
            ),
            ShivNote(
                id = "5",
                title = "Daily Log",
                subtitle = "Discipline | Focus | Progress",
                date = "5 Aug 2026",
                category = "Personal",
                icon = Icons.Default.MenuBook,
                iconColor = ShivaCyan,
                content = "Focus on deep work blocks without interruptions. Track daily coding and study milestones."
            )
        )
    }

    val categories = listOf("All", "JEE", "Ideas", "Projects", "Personal")

    val filteredNotes = remember(selectedCategory, searchQuery, defaultNotes.size) {
        defaultNotes.filter { note ->
            val matchesCategory = (selectedCategory == "All" || note.category.equals(selectedCategory, ignoreCase = true))
            val matchesSearch = searchQuery.isBlank() ||
                    note.title.contains(searchQuery, ignoreCase = true) ||
                    note.subtitle.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CosmicBackground(showSilhouette = false)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onOpenSidebar) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Shiv Notes",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Note", tint = ShivaCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Search notes
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = CosmicSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search notes...", color = TextSecondary, fontSize = 13.sp) },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Categories Horizontal Scroll
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            val isSelected = cat == selectedCategory
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedCategory = cat },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) ShivaIndigo else CosmicSurfaceVariant,
                                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder) else null
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color.White else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
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
                        BottomBarTab(icon = Icons.Default.Description, label = "Notes", isSelected = true, onClick = {})
                        BottomBarTab(icon = Icons.Default.Settings, label = "Settings", isSelected = false, onClick = { onNavigate("settings") })
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredNotes, key = { it.id }) { note ->
                    NoteCard(note = note, onClick = { activeNoteToView = note })
                }
            }
        }

        // Add Note Dialog
        if (showAddDialog) {
            var newTitle by remember { mutableStateOf("") }
            var newSubtitle by remember { mutableStateOf("") }
            var newCategory by remember { mutableStateOf("Ideas") }
            var newContent by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                containerColor = CosmicSurface,
                titleContentColor = TextPrimary,
                textContentColor = TextSecondary,
                title = { Text("New Shiv Note", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("Title") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShivaCyan,
                                unfocusedBorderColor = CosmicCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        OutlinedTextField(
                            value = newSubtitle,
                            onValueChange = { newSubtitle = it },
                            label = { Text("Tags / Subtitle") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShivaCyan,
                                unfocusedBorderColor = CosmicCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        OutlinedTextField(
                            value = newContent,
                            onValueChange = { newContent = it },
                            label = { Text("Content") },
                            modifier = Modifier.height(100.dp),
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
                            if (newTitle.isNotBlank()) {
                                defaultNotes.add(
                                    0,
                                    ShivNote(
                                        id = System.currentTimeMillis().toString(),
                                        title = newTitle,
                                        subtitle = if (newSubtitle.isNotBlank()) newSubtitle else "General Note",
                                        date = "Today",
                                        category = newCategory,
                                        icon = Icons.Default.Description,
                                        iconColor = ShivaCyan,
                                        content = newContent
                                    )
                                )
                                showAddDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ShivaIndigo)
                    ) {
                        Text("Save Note")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }

        // View Note Dialog
        activeNoteToView?.let { note ->
            AlertDialog(
                onDismissRequest = { activeNoteToView = null },
                containerColor = CosmicSurface,
                title = { Text(note.title, color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(note.subtitle, color = ShivaCyan, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(note.date, color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            if (note.content.isNotBlank()) note.content else note.subtitle,
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { activeNoteToView = null }, colors = ButtonDefaults.buttonColors(containerColor = ShivaIndigo)) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun NoteCard(note: ShivNote, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = CosmicSurface,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(note.iconColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(note.icon, contentDescription = null, tint = note.iconColor, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = note.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Text(
                text = note.date,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = TextTertiary
            )
        }
    }
}
