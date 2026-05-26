package com.example.ui.workflow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.dao.WorkflowDao
import com.example.data.local.entity.WorkflowEntity
import com.example.data.local.entity.WorkflowStepEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkflowViewModel(private val workflowDao: WorkflowDao) : ViewModel() {
    private val _workflows = MutableStateFlow<List<WorkflowEntity>>(emptyList())
    val workflows = _workflows.asStateFlow()

    private val _workflowSteps = MutableStateFlow<Map<String, List<WorkflowStepEntity>>>(emptyMap())
    val workflowSteps = _workflowSteps.asStateFlow()

    init {
        loadWorkflows()
    }

    private fun loadWorkflows() {
        viewModelScope.launch {
            val active = workflowDao.getActiveWorkflows()
            _workflows.value = active
            
            val stepsMap = mutableMapOf<String, List<WorkflowStepEntity>>()
            for (wf in active) {
                stepsMap[wf.id] = workflowDao.getStepsForWorkflow(wf.id)
            }
            _workflowSteps.value = stepsMap
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowScreen(viewModel: WorkflowViewModel, onBack: () -> Unit) {
    val workflows by viewModel.workflows.collectAsState()
    val steps by viewModel.workflowSteps.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workflow Visualization") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            if (workflows.isEmpty()) {
                item {
                    Text("No active workflows currently.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(workflows, key = { it.id }) { workflow ->
                    WorkflowDetailCard(workflow, steps[workflow.id] ?: emptyList())
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun WorkflowDetailCard(workflow: WorkflowEntity, steps: List<WorkflowStepEntity>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(workflow.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(workflow.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Execution Steps:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            
            steps.forEach { step ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = when(step.status) {
                        "COMPLETED" -> Icons.Default.CheckCircle
                        "RUNNING" -> Icons.Default.PlayCircle
                        "FAILED" -> Icons.Default.Error
                        else -> Icons.Default.HourglassEmpty
                    }
                    val tint = when(step.status) {
                        "COMPLETED" -> MaterialTheme.colorScheme.primary
                        "RUNNING" -> MaterialTheme.colorScheme.tertiary
                        "FAILED" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Step ${step.stepIndex}: ${step.type}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
