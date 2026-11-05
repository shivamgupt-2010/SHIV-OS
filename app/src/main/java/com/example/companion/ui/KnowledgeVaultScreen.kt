package com.example.companion.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class VaultMemory(val category: String, val key: String, val value: String, val score: Int)

@Composable
fun KnowledgeVaultScreen(onBack: () -> Unit) {
    val memories = remember {
        mutableStateListOf(
            VaultMemory("Profile", "Primary project", "ShivAI Android Companion & Launcher", 96),
            VaultMemory("Preference", "Work style", "Focused sessions with short, concise prompts", 91),
            VaultMemory("Routine", "Study target", "Ninety minutes of deep learning practice each day", 88),
            VaultMemory("Automation", "Travel preference", "Choose a lower-cost route when commute time is similar", 79)
        )
    }
    var query by remember { mutableStateOf("") }
    var memoryToDelete by remember { mutableStateOf<VaultMemory?>(null) }
    var isAdding by remember { mutableStateOf(false) }
    var newKey by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }
    val visibleMemories = memories.filter { it.key.contains(query, ignoreCase = true) || it.value.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true) }

    CompanionBackground {
        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(Modifier.height(12.dp)) }
            item {
                CompanionTopBar(
                    title = "Knowledge Vault",
                    subtitle = "Your private, on-device context",
                    onBack = onBack,
                    action = {
                        IconButton(onClick = { isAdding = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add memory", tint = CompanionPalette.Green)
                        }
                    }
                )
            }
            item {
                CompanionSurface(accent = CompanionPalette.Green) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = CompanionPalette.Green)
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Private by default", color = CompanionPalette.Text, fontWeight = FontWeight.SemiBold)
                            Text("Memory stays on this device until you explicitly connect a service.", color = CompanionPalette.Muted, fontSize = 12.sp)
                        }
                        CompanionStatusPill("Local", CompanionPalette.Green)
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Search your memory") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CompanionPalette.Cyan) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CompanionPalette.Text,
                        unfocusedTextColor = CompanionPalette.Text,
                        focusedBorderColor = CompanionPalette.Cyan,
                        unfocusedBorderColor = CompanionPalette.Line,
                        cursorColor = CompanionPalette.Cyan
                    )
                )
            }
            item { CompanionSectionLabel("${visibleMemories.size} memories") }
            items(visibleMemories.size) { index ->
                val memory = visibleMemories[index]
                MemoryCard(memory = memory, onDelete = { memoryToDelete = memory })
            }
            item { Spacer(Modifier.height(28.dp)) }
        }
    }

    if (isAdding) {
        VaultEditDialog(
            key = newKey,
            value = newValue,
            onKeyChanged = { newKey = it },
            onValueChanged = { newValue = it },
            onDismiss = { isAdding = false },
            onSave = {
                if (newKey.isNotBlank() && newValue.isNotBlank()) {
                    memories.add(0, VaultMemory("Personal", newKey.trim(), newValue.trim(), 72))
                    newKey = ""
                    newValue = ""
                    isAdding = false
                }
            }
        )
    }
    memoryToDelete?.let { memory ->
        AlertDialog(
            onDismissRequest = { memoryToDelete = null },
            containerColor = CompanionPalette.PanelStrong,
            titleContentColor = CompanionPalette.Text,
            textContentColor = CompanionPalette.Muted,
            title = { Text("Remove this memory?") },
            text = { Text("This removes “${memory.key}” from the local vault.") },
            confirmButton = {
                TextButton(onClick = { memories.remove(memory); memoryToDelete = null }) { Text("Remove", color = CompanionPalette.Red) }
            },
            dismissButton = { TextButton(onClick = { memoryToDelete = null }) { Text("Keep", color = CompanionPalette.Cyan) } }
        )
    }
}

@Composable
private fun MemoryCard(memory: VaultMemory, onDelete: () -> Unit) {
    CompanionSurface(accent = CompanionPalette.Line) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                CompanionStatusPill(memory.category, CompanionPalette.Violet)
                Spacer(Modifier.height(10.dp))
                Text(memory.key, color = CompanionPalette.Text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(3.dp))
                Text(memory.value, color = CompanionPalette.Muted, fontSize = 12.sp, lineHeight = 17.sp)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Remove memory", tint = CompanionPalette.Quiet, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Relevance", color = CompanionPalette.Quiet, fontSize = 11.sp)
            Spacer(Modifier.width(8.dp))
            MeterBar(memory.score / 100f, CompanionPalette.Cyan, Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Text("${memory.score}%", color = CompanionPalette.Cyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun VaultEditDialog(
    key: String,
    value: String,
    onKeyChanged: (String) -> Unit,
    onValueChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CompanionPalette.PanelStrong,
        titleContentColor = CompanionPalette.Text,
        textContentColor = CompanionPalette.Muted,
        title = { Text("Save to your vault") },
        text = {
            Column {
                OutlinedTextField(value = key, onValueChange = onKeyChanged, label = { Text("Memory title") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = value, onValueChange = onValueChanged, label = { Text("What should ShivAI remember?") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = onSave, enabled = key.isNotBlank() && value.isNotBlank()) { Text("Save", color = CompanionPalette.Cyan) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = CompanionPalette.Muted) } }
    )
}
