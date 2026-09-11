package com.example.ui.agents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CosmicBackground
import com.example.ui.components.TrishulIcon
import com.example.ui.theme.*

data class AgentItem(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val systemPrompt: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentsScreen(
    onSelectAgent: (String) -> Unit,
    onNavigate: (String) -> Unit,
    onOpenSidebar: () -> Unit
) {
    val agents = listOf(
        AgentItem(
            id = "research",
            name = "Research Agent",
            description = "Finds and summarizes information",
            icon = Icons.Default.Search,
            iconColor = ShivaCyan,
            systemPrompt = "You are ShivAI Research Agent. Provide deep, accurate research summaries."
        ),
        AgentItem(
            id = "coding",
            name = "Coding Agent",
            description = "Writes, debugs, and explains code",
            icon = Icons.Default.Code,
            iconColor = ShivaGreen,
            systemPrompt = "You are ShivAI Coding Agent. Expert in Kotlin, Python, React, and Android development."
        ),
        AgentItem(
            id = "study",
            name = "Study Agent",
            description = "Helps with JEE and study planning",
            icon = Icons.Default.School,
            iconColor = ShivaPurple,
            systemPrompt = "You are ShivAI Study Agent. Specialize in JEE Maths, Physics, Chemistry, and strategic revision."
        ),
        AgentItem(
            id = "creative",
            name = "Creative Agent",
            description = "Ideas, content, music, design",
            icon = Icons.Default.Palette,
            iconColor = ShivaOrange,
            systemPrompt = "You are ShivAI Creative Agent. Brainstorm ideas, music concepts, and design workflows."
        ),
        AgentItem(
            id = "business",
            name = "Business Agent",
            description = "Web, growth, automation",
            icon = Icons.Default.BusinessCenter,
            iconColor = ShivaBlue,
            systemPrompt = "You are ShivAI Business Agent. Specialize in product growth, marketing, and automation."
        ),
        AgentItem(
            id = "general",
            name = "General Agent",
            description = "Chat, tasks, anything",
            icon = Icons.Default.SmartToy,
            iconColor = ShivaIndigo,
            systemPrompt = "You are ShivAI General Assistant. Ready to help with any task."
        )
    )

    var isParallelSwarmRunning by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var swarmTaskTitle by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("Autonomous Device & Research Assistant") }
    var parallelProgress by androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }

    androidx.compose.runtime.LaunchedEffect(isParallelSwarmRunning) {
        if (isParallelSwarmRunning) {
            parallelProgress = 0.1f
            while (parallelProgress < 1f) {
                kotlinx.coroutines.delay(800)
                parallelProgress += 0.2f
            }
            kotlinx.coroutines.delay(1000)
            // Stays active in continuous swarm mode
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CosmicBackground(showSilhouette = false)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onOpenSidebar) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Agents Swarm",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = { onSelectAgent("general") }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Agent", tint = ShivaCyan)
                    }
                }
            },
            bottomBar = {
                // Dedicated Bottom Nav matching design
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CosmicSurface.copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BottomBarTab(icon = Icons.Default.Home, label = "Home", isSelected = false, onClick = { onNavigate("launcher") })
                        BottomBarTab(icon = Icons.Default.SmartToy, label = "Agents", isSelected = true, onClick = {})
                        BottomBarTab(icon = Icons.Default.Description, label = "Notes", isSelected = false, onClick = { onNavigate("notes") })
                        BottomBarTab(icon = Icons.Default.Settings, label = "Settings", isSelected = false, onClick = { onNavigate("settings") })
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Parallel Multi-Agent Swarm Banner
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp)),
                        shape = RoundedCornerShape(22.dp),
                        color = CosmicSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isParallelSwarmRunning) ShivaGreen else ShivaCyan.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (isParallelSwarmRunning) ShivaGreen else ShivaCyan)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isParallelSwarmRunning) "SWARM RUNNING PARALLEL" else "PARALLEL AGENT SWARM",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isParallelSwarmRunning) ShivaGreen else ShivaCyan
                                    )
                                }

                                Button(
                                    onClick = { isParallelSwarmRunning = !isParallelSwarmRunning },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isParallelSwarmRunning) ShivaOrange else ShivaIndigo
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isParallelSwarmRunning) "Stop Swarm" else "Run All Concurrently",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Execute multiple specialized AI agents simultaneously on background research, coding review, and study tasks without waiting.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )

                            if (isParallelSwarmRunning) {
                                Spacer(modifier = Modifier.height(10.dp))
                                LinearProgressIndicator(
                                    progress = { parallelProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = ShivaGreen,
                                    trackColor = CosmicCardBorder
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "⚡ 6 Agents actively computing in parallel background coroutines",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = ShivaCyan
                                )
                            }
                        }
                    }
                }

                items(agents, key = { it.id }) { agent ->
                    AgentCard(
                        agent = agent,
                        isParallelActive = isParallelSwarmRunning,
                        onClick = { onSelectAgent(agent.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AgentCard(
    agent: AgentItem,
    isParallelActive: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = CosmicSurface,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isParallelActive) agent.iconColor.copy(alpha = 0.6f) else CosmicCardBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(agent.iconColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(agent.icon, contentDescription = null, tint = agent.iconColor, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = agent.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    if (isParallelActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ShivaGreen.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, ShivaGreen.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "PARALLEL",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = ShivaGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = agent.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun BottomBarTab(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) ShivaCyan else TextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = if (isSelected) ShivaCyan else TextSecondary,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
