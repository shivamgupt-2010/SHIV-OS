package com.example.ui.notes

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.NoteEntity
import com.example.ui.components.CosmicBackground
import com.example.ui.components.TrishulIcon
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    onNavigate: (String) -> Unit,
    onOpenSidebar: () -> Unit
) {
    val context = LocalContext.current
    val activeNotes by viewModel.activeNotes.collectAsState()
    val trashedNotes by viewModel.trashedNotes.collectAsState()

    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var activeNoteForEdit by remember { mutableStateOf<NoteEntity?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

    val categories = listOf("All", "Personal", "Study", "Projects", "Ideas", "Trash")

    val displayedNotes = remember(activeNotes, trashedNotes, selectedCategory, searchQuery) {
        val baseList = if (selectedCategory == "Trash") trashedNotes else activeNotes
        baseList.filter { note ->
            val matchesCategory = if (selectedCategory == "All" || selectedCategory == "Trash") true
            else note.category.equals(selectedCategory, ignoreCase = true)

            val query = searchQuery.trim().lowercase()
            val matchesSearch = query.isEmpty() ||
                    note.title.lowercase().contains(query) ||
                    note.content.lowercase().contains(query)

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
                            Surface(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onOpenSidebar),
                                color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.6f),
                                shape = CircleShape
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = ShivTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            TrishulIcon(size = 20.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SHIV Notes",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = ShivTheme.colors.textPrimary
                            )
                        }

                        if (selectedCategory == "Trash") {
                            TextButton(
                                onClick = { viewModel.emptyTrash() },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Empty Trash", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    activeNoteForEdit = NoteEntity(category = if (selectedCategory != "All") selectedCategory else "Personal")
                                    isCreatingNew = true
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(ShivTheme.colors.primary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "New Note", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = ShivTheme.colors.surface.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, ShivTheme.colors.cardBorder.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = ShivTheme.colors.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search encrypted notes...", color = ShivTheme.colors.textSecondary, fontSize = 13.sp) },
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
                                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = ShivTheme.colors.textSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Filter Pills
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedCategory = cat },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) ShivTheme.colors.primary.copy(alpha = 0.22f) else ShivTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.cardBorder.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = cat,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                                    color = if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            if (displayedNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (selectedCategory == "Trash") Icons.Default.DeleteOutline else Icons.Default.NoteAlt,
                            contentDescription = null,
                            tint = ShivTheme.colors.textSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (selectedCategory == "Trash") "Trash is empty" else "No notes found in this category",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ShivTheme.colors.textSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(displayedNotes, key = { it.id }) { note ->
                        NoteCardItem(
                            note = note,
                            viewModel = viewModel,
                            isTrashMode = selectedCategory == "Trash",
                            onClick = {
                                if (selectedCategory != "Trash") {
                                    activeNoteForEdit = note
                                    isCreatingNew = false
                                }
                            }
                        )
                    }
                }
            }
        }

        // Note Editor / Drawing Sheet
        if (activeNoteForEdit != null) {
            RichNoteEditorDialog(
                initialNote = activeNoteForEdit!!,
                isNew = isCreatingNew,
                viewModel = viewModel,
                onDismiss = { activeNoteForEdit = null }
            )
        }
    }
}

@Composable
fun NoteCardItem(
    note: NoteEntity,
    viewModel: NotesViewModel,
    isTrashMode: Boolean,
    onClick: () -> Unit
) {
    val dateFmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val dateStr = remember(note.updatedAt) { dateFmt.format(Date(note.updatedAt)) }
    val checklist = remember(note.checklistJson) { viewModel.parseChecklist(note.checklistJson) }
    val images = remember(note.imageUrisJson) { viewModel.parseImageUris(note.imageUrisJson) }
    val doodlePaths = remember(note.drawingPathsJson) { viewModel.parseDoodlePaths(note.drawingPathsJson) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, if (note.isPinned) ShivTheme.colors.primary.copy(alpha = 0.6f) else ShivTheme.colors.cardBorder.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Category, Date, Pin / Trash Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ShivTheme.colors.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = note.category.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = ShivTheme.colors.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(dateStr, style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
                    Spacer(modifier = Modifier.width(6.dp))

                    if (isTrashMode) {
                        IconButton(onClick = { viewModel.restoreFromTrash(note.id) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Restore, contentDescription = "Restore", tint = ShivTheme.colors.primary, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { viewModel.deletePermanently(note.id) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        IconButton(onClick = { viewModel.togglePin(note.id, note.isPinned) }, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = if (note.isPinned) Icons.Default.PushPin else Icons.Default.OutlinedFlag,
                                contentDescription = "Pin",
                                tint = if (note.isPinned) ShivTheme.colors.primary else ShivTheme.colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(onClick = { viewModel.moveToTrash(note.id) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Trash", tint = ShivTheme.colors.textSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (note.title.isNotBlank()) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ShivTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (note.content.isNotBlank()) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = ShivTheme.colors.textSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Checklist Preview
            if (checklist.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val doneCount = checklist.count { it.isChecked }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Checklist, contentDescription = null, tint = ShivTheme.colors.primary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$doneCount/${checklist.size} completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = ShivTheme.colors.primary
                    )
                }
            }

            // Media & Doodle Badges
            if (images.isNotEmpty() || doodlePaths.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (images.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ShivTheme.colors.surface
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = ShivTheme.colors.accent, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${images.size} photo", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = ShivTheme.colors.textSecondary)
                            }
                        }
                    }
                    if (doodlePaths.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ShivTheme.colors.surface
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Brush, contentDescription = null, tint = ShivaCyan, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Doodle", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = ShivTheme.colors.textSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Rich Note Editor Dialog with Formatted Text, Checklists, Photo Picker & Doodle Canvas.
 */
@Composable
fun RichNoteEditorDialog(
    initialNote: NoteEntity,
    isNew: Boolean,
    viewModel: NotesViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialNote.title) }
    var content by remember { mutableStateOf(initialNote.content) }
    var category by remember { mutableStateOf(initialNote.category) }
    var isPinned by remember { mutableStateOf(initialNote.isPinned) }

    var checklistItems by remember { mutableStateOf(viewModel.parseChecklist(initialNote.checklistJson)) }
    var newChecklistInput by remember { mutableStateOf("") }

    var imageUris by remember { mutableStateOf(viewModel.parseImageUris(initialNote.imageUrisJson)) }
    var doodlePaths by remember { mutableStateOf(viewModel.parseDoodlePaths(initialNote.drawingPathsJson)) }

    var currentTab by remember { mutableIntStateOf(0) } // 0 = Note, 1 = Checklists, 2 = Doodle Canvas

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUris = imageUris + uri.toString()
        }
    }

    // Auto-save on back or dismiss
    val saveCurrentState = {
        val updated = initialNote.copy(
            title = title,
            content = content,
            category = category,
            isPinned = isPinned,
            checklistJson = viewModel.serializeChecklist(checklistItems),
            imageUrisJson = viewModel.serializeImageUris(imageUris),
            drawingPathsJson = viewModel.serializeDoodlePaths(doodlePaths),
            updatedAt = System.currentTimeMillis()
        )
        viewModel.saveNote(updated)
    }

    Dialog(
        onDismissRequest = {
            saveCurrentState()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = ShivTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Editor Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        saveCurrentState()
                        onDismiss()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close", tint = ShivTheme.colors.textPrimary)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { isPinned = !isPinned }) {
                            Icon(
                                imageVector = if (isPinned) Icons.Default.PushPin else Icons.Default.OutlinedFlag,
                                contentDescription = "Pin",
                                tint = if (isPinned) ShivTheme.colors.primary else ShivTheme.colors.textSecondary
                            )
                        }

                        IconButton(onClick = {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add Photo", tint = ShivTheme.colors.accent)
                        }

                        Button(
                            onClick = {
                                saveCurrentState()
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.primary)
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mode Tabs (Text, Checklists, Doodle Canvas)
                TabRow(
                    selectedTabIndex = currentTab,
                    containerColor = Color.Transparent,
                    contentColor = ShivTheme.colors.primary
                ) {
                    Tab(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        text = { Text("Note & Text", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        text = { Text("Checklist (${checklistItems.size})", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        text = { Text("Doodle Canvas", fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (currentTab) {
                    0 -> {
                        // Standard Note Text Editor
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            TextField(
                                value = title,
                                onValueChange = { title = it },
                                placeholder = { Text("Title", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ShivTheme.colors.textSecondary) },
                                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = ShivTheme.colors.textPrimary,
                                    unfocusedTextColor = ShivTheme.colors.textPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Attached Images Carousel
                            if (imageUris.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(imageUris) { uriStr ->
                                        Box(
                                            modifier = Modifier
                                                .size(110.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                        ) {
                                            val bmp = remember(uriStr) {
                                                try {
                                                    val stream = context.contentResolver.openInputStream(android.net.Uri.parse(uriStr))
                                                    android.graphics.BitmapFactory.decodeStream(stream)?.asImageBitmap()
                                                } catch (e: Exception) {
                                                    null
                                                }
                                            }
                                            if (bmp != null) {
                                                Image(
                                                    bitmap = bmp,
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier.fillMaxSize().background(ShivTheme.colors.surfaceVariant),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.Image, contentDescription = null, tint = ShivTheme.colors.textSecondary)
                                                }
                                            }
                                            IconButton(
                                                onClick = { imageUris = imageUris.filter { it != uriStr } },
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .align(Alignment.TopEnd)
                                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            TextField(
                                value = content,
                                onValueChange = { content = it },
                                placeholder = { Text("Write your thoughts, roadmap, or ideas...", color = ShivTheme.colors.textSecondary) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = ShivTheme.colors.textPrimary,
                                    unfocusedTextColor = ShivTheme.colors.textPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false)
                            )
                        }
                    }

                    1 -> {
                        // Interactive Checklists Editor
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newChecklistInput,
                                    onValueChange = { newChecklistInput = it },
                                    placeholder = { Text("Add new checklist task...") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (newChecklistInput.isNotBlank()) {
                                            checklistItems = checklistItems + ChecklistItem(text = newChecklistInput.trim())
                                            newChecklistInput = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.primary)
                                ) {
                                    Text("Add")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(checklistItems, key = { it.id }) { item ->
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.6f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = item.isChecked,
                                                onCheckedChange = { checked ->
                                                    checklistItems = checklistItems.map {
                                                        if (it.id == item.id) it.copy(isChecked = checked) else it
                                                    }
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = item.text,
                                                modifier = Modifier.weight(1f),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                                                ),
                                                color = if (item.isChecked) ShivTheme.colors.textSecondary else ShivTheme.colors.textPrimary
                                            )
                                            IconButton(
                                                onClick = { checklistItems = checklistItems.filter { it.id != item.id } },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = ShivTheme.colors.textSecondary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // Interactive Doodle Canvas
                        DoodleCanvasView(
                            existingPaths = doodlePaths,
                            onPathsChanged = { doodlePaths = it }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive Doodle & Sketching Canvas
 */
@Composable
fun DoodleCanvasView(
    existingPaths: List<DoodlePath>,
    onPathsChanged: (List<DoodlePath>) -> Unit
) {
    val paths = remember { mutableStateListOf<DoodlePath>().apply { addAll(existingPaths) } }
    var activeStrokeColorHex by remember { mutableStateOf("#00F5FF") }
    var activeStrokeWidth by remember { mutableFloatStateOf(6f) }
    var currentDrawPoints by remember { mutableStateOf<List<Pair<Float, Float>>>(emptyList()) }

    val colors = listOf("#00F5FF", "#8A2BE2", "#10B981", "#F59E0B", "#EF4444", "#FFFFFF")

    Column(modifier = Modifier.fillMaxSize()) {
        // Doodle Toolbar: Color Picker, Stroke Width, Undo, Clear
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                colors.forEach { hex ->
                    val color = Color(android.graphics.Color.parseColor(hex))
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (activeStrokeColorHex == hex) 2.5.dp else 0.dp,
                                color = if (activeStrokeColorHex == hex) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { activeStrokeColorHex = hex }
                    )
                }
            }

            Row {
                IconButton(
                    onClick = {
                        if (paths.isNotEmpty()) {
                            paths.removeAt(paths.size - 1)
                            onPathsChanged(paths.toList())
                        }
                    },
                    enabled = paths.isNotEmpty()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = ShivTheme.colors.textPrimary)
                }
                IconButton(
                    onClick = {
                        paths.clear()
                        onPathsChanged(emptyList())
                    },
                    enabled = paths.isNotEmpty()
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Drawing Canvas
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, ShivTheme.colors.cardBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            color = Color(0xFF090D14)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(activeStrokeColorHex, activeStrokeWidth) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentDrawPoints = listOf(Pair(offset.x, offset.y))
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentDrawPoints = currentDrawPoints + Pair(change.position.x, change.position.y)
                            },
                            onDragEnd = {
                                if (currentDrawPoints.size > 1) {
                                    val newPath = DoodlePath(
                                        colorHex = activeStrokeColorHex,
                                        strokeWidth = activeStrokeWidth,
                                        points = currentDrawPoints
                                    )
                                    paths.add(newPath)
                                    onPathsChanged(paths.toList())
                                }
                                currentDrawPoints = emptyList()
                            }
                        )
                    }
            ) {
                // Render finished paths
                paths.forEach { doodle ->
                    val strokeColor = try {
                        Color(android.graphics.Color.parseColor(doodle.colorHex))
                    } catch (e: Exception) {
                        Color.Cyan
                    }
                    if (doodle.points.size > 1) {
                        val path = Path().apply {
                            moveTo(doodle.points[0].first, doodle.points[0].second)
                            for (i in 1 until doodle.points.size) {
                                lineTo(doodle.points[i].first, doodle.points[i].second)
                            }
                        }
                        drawPath(
                            path = path,
                            color = strokeColor,
                            style = Stroke(
                                width = doodle.strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                // Render current active drawing stroke
                if (currentDrawPoints.size > 1) {
                    val activeColor = try {
                        Color(android.graphics.Color.parseColor(activeStrokeColorHex))
                    } catch (e: Exception) {
                        Color.Cyan
                    }
                    val path = Path().apply {
                        moveTo(currentDrawPoints[0].first, currentDrawPoints[0].second)
                        for (i in 1 until currentDrawPoints.size) {
                            lineTo(currentDrawPoints[i].first, currentDrawPoints[i].second)
                        }
                    }
                    drawPath(
                        path = path,
                        color = activeColor,
                        style = Stroke(
                            width = activeStrokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}
