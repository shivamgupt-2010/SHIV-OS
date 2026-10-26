package com.example.ui.workflow

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.workflow.domain.model.Workflow
import com.example.ai.workflow.domain.model.WorkflowStatus
import com.example.ai.workflow.domain.model.WorkflowStep
import com.example.ai.workflow.engine.*
import com.example.data.local.dao.WorkflowDao
import com.example.data.local.entity.WorkflowEntity
import com.example.data.local.entity.WorkflowStepEntity
import com.example.ui.components.CosmicBackground
import com.example.ui.components.TrishulIcon
import com.example.ui.theme.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AutomationsViewModel(
    private val automationEngine: ActionAutomationEngine,
    private val workflowDao: WorkflowDao
) : ViewModel() {

    val allWorkflows: StateFlow<List<WorkflowEntity>> = workflowDao.getAllWorkflowsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingApprovals: StateFlow<List<PendingApprovalItem>> = automationEngine.pendingApprovals
    val auditLogs: StateFlow<List<ExecutionAuditRecord>> = automationEngine.auditLogs

    fun getTemplates(): List<AutomationTemplate> = automationEngine.getTemplates()

    fun runTemplate(templateId: String) {
        viewModelScope.launch {
            automationEngine.launchTemplate(templateId)
        }
    }

    fun approveStep(workflowId: String, stepId: String) {
        viewModelScope.launch {
            automationEngine.approveStep(workflowId, stepId)
        }
    }

    fun rejectStep(workflowId: String, stepId: String) {
        viewModelScope.launch {
            automationEngine.rejectStep(workflowId, stepId)
        }
    }

    fun cancelWorkflow(workflowId: String) {
        viewModelScope.launch {
            automationEngine.cancelWorkflow(workflowId)
        }
    }

    suspend fun getStepsForWorkflow(workflowId: String): List<WorkflowStepEntity> {
        return workflowDao.getStepsForWorkflow(workflowId)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationsScreen(
    viewModel: AutomationsViewModel,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Active Tasks", "Approvals", "Templates", "Audit Trail")
    val pendingApprovals by viewModel.pendingApprovals.collectAsState()
    val allWorkflows by viewModel.allWorkflows.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    CosmicBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TrishulIcon(size = 20.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Action & Automation Engine",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ShivTheme.colors.textPrimary
                                )
                            }
                            Text(
                                "Autonomous On-Device Task Execution",
                                style = MaterialTheme.typography.labelSmall,
                                color = ShivTheme.colors.primary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ShivTheme.colors.textPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = ShivTheme.colors.surface.copy(alpha = 0.6f),
                    contentColor = ShivTheme.colors.primary,
                    divider = { HorizontalDivider(color = ShivTheme.colors.cardBorder) }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        title,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (index == 1 && pendingApprovals.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(
                                            shape = CircleShape,
                                            color = ShivaRed,
                                            modifier = Modifier.size(16.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    pendingApprovals.size.toString(),
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

                // Tab Content
                when (selectedTab) {
                    0 -> ActiveTasksTab(workflows = allWorkflows, viewModel = viewModel)
                    1 -> ApprovalsTab(approvals = pendingApprovals, viewModel = viewModel)
                    2 -> TemplatesTab(templates = viewModel.getTemplates(), onRun = { viewModel.runTemplate(it) })
                    3 -> AuditTrailTab(logs = auditLogs)
                }
            }
        }
    }
}

@Composable
fun ActiveTasksTab(workflows: List<WorkflowEntity>, viewModel: AutomationsViewModel) {
    if (workflows.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AccountTree, contentDescription = null, tint = ShivTheme.colors.textSecondary, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No active tasks running", style = MaterialTheme.typography.bodyMedium, color = ShivTheme.colors.textSecondary)
                Text("Trigger a template or give ShivAI a task in chat", style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary.copy(alpha = 0.7f))
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(workflows, key = { it.id }) { wf ->
                WorkflowTaskCard(workflow = wf, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun WorkflowTaskCard(workflow: WorkflowEntity, viewModel: AutomationsViewModel) {
    var expanded by remember { mutableStateOf(false) }
    var steps by remember { mutableStateOf<List<WorkflowStepEntity>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(workflow.id) {
        steps = viewModel.getStepsForWorkflow(workflow.id)
    }

    val statusColor = when (workflow.status) {
        "COMPLETED" -> Color(0xFF4CAF50)
        "RUNNING" -> ShivaCyan
        "PAUSED" -> Color(0xFFFFB300)
        "FAILED" -> ShivaRed
        else -> ShivTheme.colors.textSecondary
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, ShivTheme.colors.cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workflow.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = ShivTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = workflow.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = ShivTheme.colors.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, statusColor)
                ) {
                    Text(
                        text = workflow.status,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Step Progress Stepper
            if (steps.isNotEmpty()) {
                val completedSteps = steps.count { it.status == "COMPLETED" }
                val progress = completedSteps.toFloat() / steps.size.toFloat()

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = ShivaCyan,
                    trackColor = Color.Black.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Progress: $completedSteps/${steps.size} steps",
                        style = MaterialTheme.typography.labelSmall,
                        color = ShivTheme.colors.textSecondary
                    )
                    Text(
                        if (expanded) "Hide Steps ▲" else "View Steps ▼",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ShivTheme.colors.primary,
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    steps.forEachIndexed { idx, st ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            val icon = when (st.status) {
                                "COMPLETED" -> Icons.Default.CheckCircle
                                "RUNNING" -> Icons.Default.PlayCircle
                                "FAILED" -> Icons.Default.Error
                                else -> Icons.Default.HourglassEmpty
                            }
                            val tint = when (st.status) {
                                "COMPLETED" -> Color(0xFF4CAF50)
                                "RUNNING" -> ShivaCyan
                                "FAILED" -> ShivaRed
                                else -> ShivTheme.colors.textSecondary
                            }
                            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Step ${idx + 1}: ${st.actionPayloadJson.take(40)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ShivTheme.colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Controls
            if (workflow.status == "RUNNING" || workflow.status == "PAUSED") {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { viewModel.cancelWorkflow(workflow.id) }) {
                        Text("Cancel Task", color = ShivaRed, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ApprovalsTab(approvals: List<PendingApprovalItem>, viewModel: AutomationsViewModel) {
    if (approvals.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ShivTheme.colors.primary, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("All Clear — Zero Pending Approvals", style = MaterialTheme.typography.bodyMedium, color = ShivTheme.colors.textPrimary)
                Text("Sensitive actions requiring user permission will appear here", style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(approvals, key = { it.id }) { item ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.95f),
                    border = BorderStroke(1.5.dp, ShivaRed.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                item.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = ShivTheme.colors.textPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ShivaRed.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    item.riskLevel.name,
                                    color = ShivaRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Action: ${item.actionName}\nPayload: ${item.payload}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ShivTheme.colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.rejectStep(item.workflowId, item.stepId) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Reject", color = ShivaRed)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.approveStep(item.workflowId, item.stepId) },
                                colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.primary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Approve & Execute", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TemplatesTab(templates: List<AutomationTemplate>, onRun: (String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(templates, key = { it.id }) { tpl ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, ShivTheme.colors.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ShivTheme.colors.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ShivTheme.colors.primary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    tpl.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ShivTheme.colors.textPrimary
                                )
                                Text(
                                    tpl.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ShivTheme.colors.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        tpl.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = ShivTheme.colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "${tpl.steps.size} steps • ~${tpl.estimatedSeconds}s",
                            style = MaterialTheme.typography.labelSmall,
                            color = ShivTheme.colors.textSecondary
                        )

                        Button(
                            onClick = { onRun(tpl.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.primary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Instant Run", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuditTrailTab(logs: List<ExecutionAuditRecord>) {
    if (logs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.History, contentDescription = null, tint = ShivTheme.colors.textSecondary, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Audit Trail Empty", style = MaterialTheme.typography.bodyMedium, color = ShivTheme.colors.textPrimary)
                Text("Action execution outcomes and security audits will log here", style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
            }
        }
    } else {
        val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(logs, key = { it.id }) { log ->
                val outcomeColor = if (log.outcome == "SUCCESS") Color(0xFF4CAF50) else ShivaRed
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.75f),
                    border = BorderStroke(1.dp, ShivTheme.colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    log.actionName,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ShivTheme.colors.textPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    dateFormat.format(Date(log.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ShivTheme.colors.textSecondary
                                )
                            }
                            Text(
                                log.details,
                                style = MaterialTheme.typography.bodySmall,
                                color = ShivTheme.colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = outcomeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                log.outcome,
                                color = outcomeColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
