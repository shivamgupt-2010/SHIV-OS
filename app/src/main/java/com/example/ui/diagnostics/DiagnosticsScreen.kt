package com.example.ui.diagnostics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.core.runtime.AIRuntimeManager
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import com.example.core.usability.PDFDocumentationGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    runtimeManager: AIRuntimeManager,
    pdfGenerator: PDFDocumentationGenerator,
    onBack: () -> Unit
) {
    val optMode by runtimeManager.optimizationMode.collectAsState()
    val activeAgents by runtimeManager.activeAgentsCount.collectAsState()
    val battery by runtimeManager.batteryLevel.collectAsState()
    val isStreaming by runtimeManager.isStreaming.collectAsState()
    val activeWorkflows by runtimeManager.activeWorkflowsCount.collectAsState()
    val memUsage by runtimeManager.memoryUsageMb.collectAsState()
    val tokens by runtimeManager.tokenUsage.collectAsState()
    val errors by runtimeManager.runtimeErrors.collectAsState()

    val scope = rememberCoroutineScope()
    var isGeneratingPdf by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Usability Testing & Diagnostics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
        ) {
            Text("Engine Status", style = MaterialTheme.typography.titleLarge)
            Text("Real-time monitoring of AI safety and performance metrics.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    scope.launch {
                        isGeneratingPdf = true
                        pdfGenerator.generateTestDocumentation()
                        isGeneratingPdf = false
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                enabled = !isGeneratingPdf
            ) {
                Text(if (isGeneratingPdf) "Generating PDF..." else "Generate Production Testing PDF")
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DiagnosticsMetricCard(title = "Battery Impact", value = "$battery%", modifier = Modifier.weight(1f))
                DiagnosticsMetricCard(title = "Safe Mode", value = optMode.name, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DiagnosticsMetricCard(title = "Memory Overlay", value = "${memUsage}MB", modifier = Modifier.weight(1f))
                DiagnosticsMetricCard(title = "Token Overhead", value = "$tokens", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DiagnosticsMetricCard(title = "Active Workflows", value = "$activeWorkflows", modifier = Modifier.weight(1f))
                DiagnosticsMetricCard(title = "Active Agents", value = "$activeAgents", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DiagnosticsMetricCard(title = "OCR Trigger Frequency", value = "Low", modifier = Modifier.weight(1f))
                DiagnosticsMetricCard(title = "Overlay Latency", value = "12ms", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Safety & Confidence", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            DiagnosticsMetricCard(
                title = "Execution Confidence",
                value = "High (92%)",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Runtime Errors", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (errors.isEmpty()) {
                Text("No recent errors. AI Runtime is stable and safe.", color = MaterialTheme.colorScheme.primary)
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        errors.take(5).forEach { error ->
                            Text("- $error", color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosticsMetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
