package com.example.ui.agents

import androidx.compose.animation.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CosmicBackground
import com.example.ui.components.TrishulIcon
import com.example.ui.theme.*
import com.shivai.sdk.DAGStep
import com.shivai.sdk.ShivAIClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class AgentItem(
    val id: String,
    val name: String,
    val role: String,
    val description: String,
    val capabilities: List<String>,
    val icon: ImageVector,
    val iconColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentsScreen(
    onSelectAgent: (String) -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onOpenSidebar: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val client = remember { ShivAIClient() }

    var liveAgentsOnline by remember { mutableStateOf(10) }
    var activeWorkflowsCount by remember { mutableStateOf(0) }
    var memoryEpisodesCount by remember { mutableStateOf(0) }
    var systemStatus by remember { mutableStateOf("Online") }
    var isCheckingState by remember { mutableStateOf(false) }

    // Task Execution State
    var selectedAgentForTask by remember { mutableStateOf<AgentItem?>(null) }
    var customTaskPrompt by remember { mutableStateOf("") }
    var isTaskExecuting by remember { mutableStateOf(false) }
    var taskResultText by remember { mutableStateOf<String?>(null) }
    var executedSteps by remember { mutableStateOf<List<DAGStep>>(emptyList()) }

    // Real ShivAI Cognitive Workforce
    val agents = listOf(
        AgentItem(
            id = "executive",
            name = "ExecutiveAgent",
            role = "Global Orchestrator",
            description = "Translates high-level user intent into system-wide strategies and monitors priority conflicts.",
            capabilities = listOf("Executive Decisions", "Multi-Agent Coordination", "Priority Balancing"),
            icon = Icons.Default.AccountTree,
            iconColor = ShivaCyan
        ),
        AgentItem(
            id = "reasoning",
            name = "ReasoningAgent",
            role = "Cognitive Reasoner",
            description = "Analyzes complex logic, evaluates multiple execution strategies, and estimates confidence.",
            capabilities = listOf("Logic Validation", "Strategy Selection", "Confidence Scoring"),
            icon = Icons.Default.Psychology,
            iconColor = ShivaPurple
        ),
        AgentItem(
            id = "planning",
            name = "PlanningAgent",
            role = "DAG Workflow Architect",
            description = "Decomposes reasoning strategies into formal Directed Acyclic Graph (DAG) task steps.",
            capabilities = listOf("DAG Decomposition", "Dependency Resolution", "Step Scheduling"),
            icon = Icons.Default.Timeline,
            iconColor = ShivaBlue
        ),
        AgentItem(
            id = "context",
            name = "ContextAgent",
            role = "Context & Memory Assembler",
            description = "Aggregates hierarchical memory, workspace knowledge, and active workflows into prompt boundaries.",
            capabilities = listOf("Context Boundary Assembly", "Episodic Recall", "Knowledge Objects"),
            icon = Icons.Default.Layers,
            iconColor = ShivaIndigo
        ),
        AgentItem(
            id = "workflow",
            name = "WorkflowAgent",
            role = "Pipeline Executor",
            description = "Coordinates synchronous and asynchronous multi-agent task execution and tracks progress.",
            capabilities = listOf("DAG Execution", "Node Retry Logic", "Trace Recording"),
            icon = Icons.Default.SettingsEthernet,
            iconColor = ShivaGreen
        ),
        AgentItem(
            id = "research",
            name = "ResearchAgent",
            role = "Web & Knowledge Synthesizer",
            description = "Dispatches search queries, scrapes verified sources, extracts tables, and generates citations.",
            capabilities = listOf("Web Research", "Source Verification", "Citation Synthesis"),
            icon = Icons.Default.Search,
            iconColor = ShivaOrange
        ),
        AgentItem(
            id = "coding",
            name = "CodingAgent",
            role = "Software Architect",
            description = "Writes, reviews, and refactors Kotlin, Jetpack Compose, TypeScript, and Python code.",
            capabilities = listOf("Code Generation", "Bug Diagnosis", "Architecture Review"),
            icon = Icons.Default.Code,
            iconColor = ShivaGreen
        ),
        AgentItem(
            id = "study",
            name = "StudyAgent",
            role = "Academic Companion",
            description = "Specializes in structured curriculum planning, JEE revision, and interactive practice problems.",
            capabilities = listOf("Concept Mastery", "Practice Generator", "Revision Timetables"),
            icon = Icons.Default.School,
            iconColor = ShivaYellow
        ),
        AgentItem(
            id = "execution_pool",
            name = "ExecutionAgentPool",
            role = "Tool & Action Worker",
            description = "Stateless drones executing device actions, local file management, and system notifications.",
            capabilities = listOf("File I/O", "Notifications", "Reminders", "Calendar"),
            icon = Icons.Default.BuildCircle,
            iconColor = ShivaPink
        ),
        AgentItem(
            id = "memory",
            name = "MemoryAgent",
            role = "Knowledge Consolidator",
            description = "Runs background memory maintenance, associative graph optimization, and garbage collection.",
            capabilities = listOf("Memory Consolidation", "Graph Pruning", "Predictive Caching"),
            icon = Icons.Default.Save,
            iconColor = ShivaPurple
        )
    )

    // Query real backend state on load
    LaunchedEffect(Unit) {
        isCheckingState = true
        withContext(Dispatchers.IO) {
            try {
                val stateRaw = client.diagnostics.getState()
                val json = JSONObject(stateRaw)
                liveAgentsOnline = json.optInt("agentsOnline", 10)
                systemStatus = json.optString("status", "active")
                val memoryObj = json.optJSONObject("memory")
                if (memoryObj != null) {
                    memoryEpisodesCount = memoryObj.optInt("episodesCount", 0)
                    activeWorkflowsCount = memoryObj.optInt("activeWorkflows", 0)
                }
            } catch (ignored: Exception) {}
            isCheckingState = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CosmicBackground(showSilhouette = false)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CosmicSurface.copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onOpenSidebar) {
                                Icon(Icons.Default.Menu, contentDescription = "Sidebar", tint = TextPrimary)
                            }
                            TrishulIcon(size = 24.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cognitive Workforce",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }

                        // Live status badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (systemStatus == "active") ShivaGreen.copy(alpha = 0.15f) else ShivaRed.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (systemStatus == "active") ShivaGreen else ShivaRed)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (systemStatus == "active") ShivaGreen else ShivaRed)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$liveAgentsOnline Online",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (systemStatus == "active") ShivaGreen else ShivaRed
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // System Telemetry Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = CosmicSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "ShivAI Cognitive Core Architecture",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Hierarchical Multi-Agent Workforce & Shared Memory Bus",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                                Icon(Icons.Default.Hub, contentDescription = null, tint = ShivaCyan, modifier = Modifier.size(24.dp))
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricCard(title = "Agents", value = "$liveAgentsOnline", modifier = Modifier.weight(1f))
                                MetricCard(title = "Episodes", value = "$memoryEpisodesCount", modifier = Modifier.weight(1f))
                                MetricCard(title = "Workflows", value = "$activeWorkflowsCount", modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "SELECT AGENT FOR AUTONOMOUS TASK",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ShivaCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Agents Directory List
                items(agents, key = { it.id }) { agent ->
                    AgentCard(
                        agent = agent,
                        isSelected = selectedAgentForTask?.id == agent.id,
                        onClick = {
                            selectedAgentForTask = agent
                            customTaskPrompt = "Autonomous request for ${agent.name}: "
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Bottom padding for task panel
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            // Task Dispatch Modal Dialog
            if (selectedAgentForTask != null) {
                AlertDialog(
                    onDismissRequest = {
                        if (!isTaskExecuting) selectedAgentForTask = null
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                isTaskExecuting = true
                                taskResultText = null
                                executedSteps = emptyList()

                                coroutineScope.launch {
                                    try {
                                        val res = client.dispatchTask(customTaskPrompt)
                                        taskResultText = res.directAnswer ?: "Strategy: ${res.strategy} (Confidence: ${(res.confidence * 100).toInt()}%)"
                                        executedSteps = res.steps
                                    } catch (e: Exception) {
                                        taskResultText = "Execution failed: ${e.message}"
                                    } finally {
                                        isTaskExecuting = false
                                    }
                                }
                            },
                            enabled = !isTaskExecuting && customTaskPrompt.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = ShivaCyan)
                        ) {
                            Text(if (isTaskExecuting) "Executing..." else "Dispatch Task", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { selectedAgentForTask = null },
                            enabled = !isTaskExecuting
                        ) {
                            Text("Cancel", color = TextSecondary)
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(selectedAgentForTask!!.icon, contentDescription = null, tint = selectedAgentForTask!!.iconColor, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(selectedAgentForTask!!.name, color = TextPrimary)
                        }
                    },
                    text = {
                        Column {
                            Text(
                                text = selectedAgentForTask!!.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = customTaskPrompt,
                                onValueChange = { customTaskPrompt = it },
                                label = { Text("Task Description") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 4,
                                enabled = !isTaskExecuting
                            )

                            if (isTaskExecuting) {
                                Spacer(modifier = Modifier.height(14.dp))
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = ShivaCyan)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Reasoning pipeline active on server...", style = MaterialTheme.typography.labelSmall, color = ShivaCyan)
                            }

                            if (taskResultText != null) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = CosmicSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Execution Output", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = ShivaCyan)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(taskResultText!!, style = MaterialTheme.typography.bodySmall, color = TextPrimary)

                                        if (executedSteps.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            executedSteps.forEach { step ->
                                                Text("• ${step.action}: ${step.status}", style = MaterialTheme.typography.labelSmall, color = ShivaGreen)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    containerColor = CosmicSurface,
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = CosmicSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ShivaCyan)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
fun AgentCard(
    agent: AgentItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) ShivaIndigo.copy(alpha = 0.2f) else CosmicSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) ShivaCyan else CosmicCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(agent.iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(agent.icon, contentDescription = null, tint = agent.iconColor, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = agent.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = agent.role,
                        style = MaterialTheme.typography.labelSmall,
                        color = agent.iconColor
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = agent.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    agent.capabilities.take(2).forEach { cap ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CosmicSurfaceVariant
                        ) {
                            Text(
                                text = cap,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomBarTab(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) ShivaCyan else TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) ShivaCyan else TextSecondary
        )
    }
}


