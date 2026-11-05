package com.example.companion.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class WorkflowStatus { WAITING, RUNNING, COMPLETE, STOPPED }
private data class WorkflowStep(val title: String, val description: String, val status: WorkflowStatus = WorkflowStatus.WAITING)

/** Workflow planning UI. Execution uses a cancelable UI job so stopping a plan immediately halts local progress state. */
@Composable
fun WorkflowLabScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var prompt by remember { mutableStateOf("") }
    var executionJob by remember { mutableStateOf<Job?>(null) }
    val steps = remember {
        mutableStateListOf(
            WorkflowStep("Understand", "Read the request and establish constraints"),
            WorkflowStep("Prepare", "Locate only the apps and permissions needed"),
            WorkflowStep("Confirm", "Keep sensitive actions behind an approval gate"),
            WorkflowStep("Execute", "Perform the approved action in sequence"),
            WorkflowStep("Verify", "Check outcome and return a clear result")
        )
    }
    val isRunning = executionJob?.isActive == true

    fun stopWorkflow() {
        executionJob?.cancel()
        executionJob = null
        steps.indices.forEach { index ->
            if (steps[index].status == WorkflowStatus.RUNNING) {
                steps[index] = steps[index].copy(status = WorkflowStatus.STOPPED)
            }
        }
    }

    fun runWorkflow() {
        if (prompt.isBlank() || isRunning) return
        steps.indices.forEach { index -> steps[index] = steps[index].copy(status = WorkflowStatus.WAITING) }
        executionJob = scope.launch {
            try {
                steps.indices.forEach { index ->
                    steps[index] = steps[index].copy(status = WorkflowStatus.RUNNING)
                    delay(650)
                    steps[index] = steps[index].copy(status = WorkflowStatus.COMPLETE)
                }
            } finally {
                executionJob = null
            }
        }
    }

    CompanionBackground {
        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(12.dp)) }
            item { CompanionTopBar("Workflow Lab", "Plan a safe, auditable automation", onBack) }
            item {
                CompanionSurface(accent = CompanionPalette.Cyan) {
                    Text("What would you like ShivAI to do?", color = CompanionPalette.Text, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        placeholder = { Text("Describe a task in plain language") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CompanionPalette.Text,
                            unfocusedTextColor = CompanionPalette.Text,
                            focusedBorderColor = CompanionPalette.Cyan,
                            unfocusedBorderColor = CompanionPalette.Line,
                            cursorColor = CompanionPalette.Cyan
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    CompanionPrimaryButton(
                        text = if (isRunning) "Workflow running" else "Build workflow",
                        icon = Icons.Default.PlayArrow,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = prompt.isNotBlank() && !isRunning,
                        onClick = ::runWorkflow
                    )
                }
            }
            item { CompanionSectionLabel("Suggested starts") }
            item {
                val ideas = listOf(
                    "Build a focused 45-minute study session",
                    "Find my latest electricity bill",
                    "Prepare a morning briefing"
                )
                CompanionSurface(contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)) {
                    ideas.forEach { idea ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { prompt = idea }.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CompanionPalette.Violet, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(9.dp))
                            Text(idea, color = CompanionPalette.Muted, fontSize = 12.sp)
                        }
                    }
                }
            }
            item { CompanionSectionLabel("Execution plan") }
            items(steps.size) { index -> WorkflowStepRow(steps[index]) }
            item {
                CompanionSurface(accent = CompanionPalette.Red, onClick = ::stopWorkflow) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.StopCircle, contentDescription = null, tint = CompanionPalette.Red)
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Stop workflow", color = CompanionPalette.Text, fontWeight = FontWeight.SemiBold)
                            Text("Cancel unfinished local steps immediately", color = CompanionPalette.Muted, fontSize = 12.sp)
                        }
                        if (isRunning) CompanionStatusPill("Stop", CompanionPalette.Red)
                    }
                }
            }
            item { Spacer(Modifier.height(28.dp)) }
        }
    }
}

@Composable
private fun WorkflowStepRow(step: WorkflowStep) {
    val (color, icon, label) = when (step.status) {
        WorkflowStatus.WAITING -> Triple(CompanionPalette.Quiet, Icons.Default.HourglassEmpty, "Queued")
        WorkflowStatus.RUNNING -> Triple(CompanionPalette.Cyan, Icons.Default.PlayArrow, "Working")
        WorkflowStatus.COMPLETE -> Triple(CompanionPalette.Green, Icons.Default.CheckCircle, "Complete")
        WorkflowStatus.STOPPED -> Triple(CompanionPalette.Red, Icons.Default.Warning, "Stopped")
    }
    CompanionSurface(accent = color) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(step.title, color = CompanionPalette.Text, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(step.description, color = CompanionPalette.Muted, fontSize = 11.sp)
            }
            CompanionStatusPill(label, color)
        }
    }
}
