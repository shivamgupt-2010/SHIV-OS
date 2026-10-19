package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.shivai.sdk.DAGStep

enum class TaskExecutionStatus {
    QUEUED,
    RUNNING,
    WAITING_FOR_TOOL,
    WAITING_FOR_INPUT,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class TaskProgressInfo(
    val taskId: String,
    val title: String,
    val status: TaskExecutionStatus,
    val strategy: String = "Decomposed Workflow",
    val confidence: Double = 0.9,
    val steps: List<DAGStep> = emptyList(),
    val errorMessage: String? = null
)

@Composable
fun TaskStatusCard(
    task: TaskProgressInfo,
    onCancel: () -> Unit = {},
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val statusColor = when (task.status) {
        TaskExecutionStatus.COMPLETED -> ShivaGreen
        TaskExecutionStatus.RUNNING -> ShivaCyan
        TaskExecutionStatus.WAITING_FOR_TOOL, TaskExecutionStatus.WAITING_FOR_INPUT -> ShivaYellow
        TaskExecutionStatus.FAILED -> ShivaRed
        TaskExecutionStatus.CANCELLED -> TextSecondary
        TaskExecutionStatus.QUEUED -> ShivaIndigo
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(18.dp),
        color = CosmicSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = task.status.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Strategy: ${task.strategy} • ${(task.confidence * 100).toInt()}% confidence",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            if (task.status == TaskExecutionStatus.RUNNING) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = ShivaCyan)
            }

            // Expandable DAG Step Checklist
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = CosmicCardBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    if (task.steps.isEmpty()) {
                        Text("No sub-steps recorded.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    } else {
                        task.steps.forEachIndexed { index, step ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (step.status == "COMPLETED") Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (step.status == "COMPLETED") ShivaGreen else TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${index + 1}. ${step.action.replace('_', ' ').capitalize()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    if (task.errorMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Error: ${task.errorMessage}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ShivaRed
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (task.status == TaskExecutionStatus.RUNNING || task.status == TaskExecutionStatus.QUEUED) {
                            TextButton(onClick = onCancel) {
                                Text("Cancel Task", color = ShivaRed)
                            }
                        }
                        if (task.status == TaskExecutionStatus.FAILED) {
                            Button(
                                onClick = onRetry,
                                colors = ButtonDefaults.buttonColors(containerColor = ShivaCyan)
                            ) {
                                Text("Retry Task", color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
