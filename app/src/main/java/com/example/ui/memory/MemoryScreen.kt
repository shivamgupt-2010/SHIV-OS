package com.example.ui.memory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.memory.domain.model.SemanticMemory
import com.example.ai.memory.domain.repository.SemanticMemoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MemoryViewModel(private val repository: SemanticMemoryRepository) : ViewModel() {
    private val _memories = MutableStateFlow<List<SemanticMemory>>(emptyList())
    val memories = _memories.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    init {
        loadMemories()
    }

    fun loadMemories() {
        viewModelScope.launch {
            _memories.value = repository.getAllActiveMemories()
        }
    }

    fun rememberPreference(key: String, value: String, category: String = "preference") {
        if (key.isBlank() || value.isBlank()) return
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                repository.rememberCloud(key = key.trim(), value = value.trim(), category = category.trim())
            } finally {
                _isSyncing.value = false
                loadMemories()
            }
        }
    }

    fun deleteMemory(id: String) {
        viewModelScope.launch {
            repository.deleteMemory(id)
            loadMemories()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(viewModel: MemoryViewModel, onBack: () -> Unit) {
    val memories by viewModel.memories.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Long-Term Memory (ShivAI)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Info, contentDescription = "Teach ShivAI") },
                text = { Text("Teach Preference") }
            )
        }
    ) { innerPadding ->
        if (showAddDialog) {
            AddPreferenceDialog(
                onDismiss = { showAddDialog = false },
                onSave = { key, value, category ->
                    viewModel.rememberPreference(key, value, category)
                    showAddDialog = false
                }
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            item {
                Text(
                    text = "Persistent Knowledge & Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Preferences and facts stored here sync with ShivAI's cloud memory engine. ShivAI automatically recalls this context when answering your queries.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isSyncing) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (memories.isEmpty()) {
                item {
                    Text("No memories or preferences stored yet. Tap 'Teach Preference' to add one!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(memories, key = { it.id }) { memory ->
                    MemoryItem(memory, onDelete = { viewModel.deleteMemory(memory.id) })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun AddPreferenceDialog(
    onDismiss: () -> Unit,
    onSave: (key: String, value: String, category: String) -> Unit
) {
    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("preference") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Teach ShivAI a Preference") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Store facts or instructions that ShivAI will remember forever (e.g. coding style, preferred framework).",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("Key (e.g. coding_style)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Value (e.g. Prefers Python with FastAPI)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(key, value, category) },
                enabled = key.isNotBlank() && value.isNotBlank()
            ) {
                Text("Remember")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MemoryItem(memory: SemanticMemory, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = memory.type.name.replace("_", " "),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = memory.content,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Memory", tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Metadata explanations for Trust & Transparency
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(4.dp))
                val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                val dateStr = sdf.format(Date(memory.createdAt))
                Text(
                    "Stored on $dateStr | Score: ${memory.importanceScore}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (memory.metadata.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Context: " + memory.metadata.entries.joinToString { "${it.key}: ${it.value}" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
