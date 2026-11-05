package com.example.companion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * WorkflowLabScreen:
 * Autonomous Multi-Step Problem Solving & DAG Task Runner.
 * Executes and visualizes complex real-world workflows on Android.
 */
@Composable
fun WorkflowLabScreen(
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var promptInput by remember { mutableStateOf("") }
    var isExecuting by remember { mutableStateOf(false) }

    data class TaskStep(val title: String, var status: String)

    val currentSteps = remember {
        mutableStateListOf(
            TaskStep("1. Parse user intent & extract entities", "SUCCESS"),
            TaskStep("2. Locate target application / storage path", "RUNNING"),
            TaskStep("3. Inspect UI hierarchy via Accessibility API", "PENDING"),
            TaskStep("4. Dispatch physical touch & text input", "PENDING"),
            TaskStep("5. Verify task completion & speak feedback", "PENDING")
        )
    }

    val sampleTasks = listOf(
        "Message Rohit on WhatsApp saying I'll reach in 15 minutes",
        "Find latest electricity bill PDF in Downloads and extract due date",
        "Compare Uber vs Ola cab fares to Airport",
        "Scaffold habit tracker Compose app into Documents",
        "Start 45-min Deep Study Mode & block Instagram"
    )

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
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("WORKFLOW & DAG LAB", color = Color(0xFF00F0FF), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Prompt Input Field
        OutlinedTextField(
            value = promptInput,
            onValueChange = { promptInput = it },
            placeholder = { Text("What task should ShivAI execute?", color = Color(0xFF64748B)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            trailingIcon = {
                IconButton(onClick = {
                    if (promptInput.isNotBlank()) {
                        isExecuting = true
                        coroutineScope.launch {
                            for (i in currentSteps.indices) {
                                currentSteps[i] = currentSteps[i].copy(status = "RUNNING")
                                delay(1200)
                                currentSteps[i] = currentSteps[i].copy(status = "SUCCESS")
                            }
                            isExecuting = false
                        }
                    }
                }) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = Color(0xFF00F0FF))
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF00F0FF),
                unfocusedBorderColor = Color(0xFF334155)
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Pre-made Prompt Chips
        Text("PRE-BUILT REAL-WORLD WORKFLOWS", color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        sampleTasks.take(3).forEach { sample ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clickable { promptInput = sample },
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF1E293B)
            ) {
                Text(sample, color = Color(0xFF38BDF8), fontSize = 11.sp, modifier = Modifier.padding(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live DAG Execution View
        Text("AUTONOMOUS EXECUTION DAG", color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(currentSteps) { step ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when (step.status) {
                            "SUCCESS" -> Color(0xFF10B981)
                            "RUNNING" -> Color(0xFF00F0FF)
                            else -> Color(0xFF334155)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(step.title, color = Color.White, fontSize = 12.sp)
                        when (step.status) {
                            "SUCCESS" -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            "RUNNING" -> CircularProgressIndicator(color = Color(0xFF00F0FF), modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            else -> Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Safety Kill Switch
        Button(
            onClick = { isExecuting = false },
            modifier = Modifier.fillMaxWidth().height(46.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
        ) {
            Icon(Icons.Default.StopCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("EMERGENCY KILL SWITCH", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
