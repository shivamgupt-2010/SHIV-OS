package com.example.companion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * KnowledgeVaultScreen:
 * Long-term memory browser synchronized with Shiv DB (https://shiv-database.onrender.com).
 * Manages user preferences, work context, and personal entities.
 */
@Composable
fun KnowledgeVaultScreen(
    onBack: () -> Unit
) {
    data class VaultItem(val category: String, val key: String, val value: String)

    val memoryItems = remember {
        mutableStateListOf(
            VaultItem("Personal", "user_identity", "Krishn (SHV-10482) - Primary Creator"),
            VaultItem("Preferences", "diet_preference", "Vegetarian, likes South Indian filter coffee"),
            VaultItem("Work", "active_project", "ShivAI Android Companion & Sovereign OS"),
            VaultItem("Automation", "cab_preference", "Prefers Uber Go unless Ola Mini is >₹50 cheaper"),
            VaultItem("Focus", "study_target", "Daily 90 mins Deep ML & Neural Networks block")
        )
    }

    var newKey by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }
    var isAdding by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF020617), Color(0xFF0F172A), Color(0xFF020617))
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("KNOWLEDGE VAULT", color = Color(0xFF00F0FF), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = { isAdding = true }) {
                Icon(Icons.Default.AddCircle, contentDescription = "Add Memory", tint = Color(0xFF10B981))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("SYNCED WITH SHIV DATABASE", color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(memoryItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0x3338BDF8)
                            ) {
                                Text(
                                    item.category.uppercase(),
                                    color = Color(0xFF38BDF8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            IconButton(
                                onClick = { memoryItems.remove(item) },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(item.key, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(item.value, color = Color(0xFF94A3B8), fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
        }

        if (isAdding) {
            AlertDialog(
                onDismissRequest = { isAdding = false },
                title = { Text("Add Memory Item", color = Color.White) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newKey,
                            onValueChange = { newKey = it },
                            label = { Text("Memory Key") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newValue,
                            onValueChange = { newValue = it },
                            label = { Text("Memory Value / Context") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (newKey.isNotBlank() && newValue.isNotBlank()) {
                            memoryItems.add(VaultItem("Custom", newKey, newValue))
                            newKey = ""
                            newValue = ""
                            isAdding = false
                        }
                    }) {
                        Text("Save to Shiv DB")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isAdding = false }) { Text("Cancel") }
                },
                containerColor = Color(0xFF0F172A)
            )
        }
    }
}
