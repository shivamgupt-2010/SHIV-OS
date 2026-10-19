package com.example.ui.conversation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.voice.session.VoiceSessionState
import com.example.core.ai.ShivAIAction
import com.example.ui.ambient.SiriLikeWaveform
import com.example.ui.components.CosmicBackground
import com.example.ui.components.TrishulIcon
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val activeAgent by viewModel.activeAgent.collectAsState()
    val pendingAttachment by viewModel.pendingAttachment.collectAsState()
    val pendingAction by viewModel.pendingActionConfirmation.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showAgentSelector by remember { mutableStateOf(false) }

    // File picker launcher for images and documents
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "document.pdf"
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            viewModel.attachFile(fileName, mimeType, uri.toString(), 1024 * 50) // Mock 50KB or real size
        }
    }

    val availableAgents = listOf(
        Pair("executive", "Executive Agent (Global Orchestrator)"),
        Pair("reasoning", "Reasoning Agent (Logic & Strategies)"),
        Pair("coding", "Coding Agent (Dev & Architecture)"),
        Pair("study", "Study Agent (Curriculum & Practice)"),
        Pair("research", "Research Agent (Web & Citations)"),
        Pair("general", "General Companion (Chat & Tasks)")
    )

    Box(modifier = modifier.fillMaxSize()) {
        CosmicBackground(showSilhouette = false)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CosmicSurface.copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder.copy(alpha = 0.5f))
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onBack) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                                }
                                TrishulIcon(size = 22.dp)
                                Spacer(modifier = Modifier.width(8.dp))

                                // Agent Selector Pill
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { showAgentSelector = true },
                                    color = CosmicSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ShivaCyan.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = availableAgents.firstOrNull { it.first == activeAgent }?.second?.substringBefore(' ') ?: "ShivAI",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = ShivaCyan
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Agent", tint = ShivaCyan, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Row {
                                IconButton(onClick = { viewModel.createNewSession() }) {
                                    Icon(Icons.Default.AddComment, contentDescription = "New Chat", tint = TextSecondary)
                                }
                                Box {
                                    IconButton(onClick = { showMenu = true }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = TextSecondary)
                                    }
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Export Chat History") },
                                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                            onClick = {
                                                showMenu = false
                                                val text = viewModel.exportChatAsText()
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("ShivAI Export", text))
                                                Toast.makeText(context, "Conversation copied to clipboard!", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Clear Chat") },
                                            leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null) },
                                            onClick = {
                                                showMenu = false
                                                viewModel.createNewSession()
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Agent Selection Dropdown
                        DropdownMenu(
                            expanded = showAgentSelector,
                            onDismissRequest = { showAgentSelector = false }
                        ) {
                            availableAgents.forEach { (tag, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, color = if (tag == activeAgent) ShivaCyan else TextPrimary) },
                                    onClick = {
                                        viewModel.selectAgent(tag)
                                        showAgentSelector = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                Surface(
                    color = CosmicSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        // Pending Attachment Chip
                        if (pendingAttachment != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CosmicSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ShivaCyan.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .padding(bottom = 6.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = ShivaCyan, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = pendingAttachment!!.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextPrimary,
                                            maxLines = 1
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.clearAttachment() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        // Pending Action Confirmation Banner
                        if (pendingAction != null) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = ShivaIndigo.copy(alpha = 0.25f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ShivaCyan),
                                modifier = Modifier
                                    .padding(bottom = 6.dp)
                                    .fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ShivaCyan, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "ShivAI Action Proposed",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = ShivaCyan
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = when (val a = pendingAction!!) {
                                            is ShivAIAction.ApplyTheme -> "Apply theme preset: ${a.presetKey} (${a.reason})"
                                            is ShivAIAction.StartFocusSession -> "Start Focus Session: ${a.minutes} minutes"
                                            is ShivAIAction.ProposeLayout -> "Optimize home grid layout with ${a.recommendedApps.size} apps"
                                            else -> "Execute proposed launcher configuration?"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                        TextButton(onClick = { viewModel.dismissAction() }) {
                                            Text("Dismiss", color = TextSecondary)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { viewModel.confirmAction(pendingAction!!) },
                                            colors = ButtonDefaults.buttonColors(containerColor = ShivaCyan)
                                        ) {
                                            Text("Apply Action", color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Input Controls Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Attachment Button
                            IconButton(
                                onClick = { filePicker.launch("*/*") },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    Icons.Default.AttachFile,
                                    contentDescription = "Attach File",
                                    tint = ShivaCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            TextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Ask ShivAI or enter a task...", color = TextSecondary, fontSize = 14.sp) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                maxLines = 4
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            if (inputText.isNotBlank() || pendingAttachment != null) {
                                IconButton(
                                    onClick = {
                                        val textToSend = inputText
                                        inputText = ""
                                        viewModel.sendMessage(textToSend)
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(ShivaIndigo)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = { viewModel.toggleVoiceInteraction() },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (voiceState != VoiceSessionState.IDLE) ShivaRed else ShivaIndigo.copy(alpha = 0.2f)
                                        )
                                ) {
                                    if (voiceState == VoiceSessionState.LISTENING || voiceState == VoiceSessionState.THINKING) {
                                        SiriLikeWaveform(isSpeaking = true, modifier = Modifier.size(20.dp))
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = "Voice",
                                            tint = if (voiceState != VoiceSessionState.IDLE) Color.White else ShivaCyan,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
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
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            color = CosmicSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                TrishulIcon(size = 42.dp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "ShivAI Cognitive Core",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Powered by Multi-Agent Workforce & Knowledge Graph.\nAsk questions, dispatch workflows, or manage launcher actions.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Chips
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                PromptChip(
                                    text = "🌐 Research topic",
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.sendMessage("Research the latest advancements in quantum error correction.") }
                                )
                                PromptChip(
                                    text = "🎨 Apply Cyan Theme",
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.sendMessage("Please apply the cyan neon theme to my launcher.") }
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                PromptChip(
                                    text = "⏱️ Start Focus Mode",
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.sendMessage("Start a 25-minute focus session.") }
                                )
                                PromptChip(
                                    text = "📱 Open YouTube",
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.sendMessage("open YouTube") }
                                )
                            }
                        }
                    }
                }

                items(messages, key = { it.id }) { message ->
                    RichMessageBubble(
                        message = message,
                        onRetry = { viewModel.retryMessage(message.id) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun RichMessageBubble(
    message: ChatMessage,
    onRetry: () -> Unit
) {
    val context = LocalContext.current
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(CosmicSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                TrishulIcon(size = 16.dp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) ShivaIndigo else CosmicSurface,
            border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, if (message.isError) ShivaRed.copy(alpha = 0.5f) else CosmicCardBorder) else null,
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                // Attachment Preview if present
                if (message.attachment != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.2f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, tint = ShivaCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = message.attachment.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = ShivaCyan
                            )
                        }
                    }
                }

                // Render content (detect code blocks or plain text)
                if (message.content.contains("```")) {
                    FormattedMarkdownContent(message.content)
                } else {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isUser) Color.White else TextPrimary
                    )
                }

                if (message.isStreaming) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ShivAI is reasoning...",
                        style = MaterialTheme.typography.labelSmall,
                        color = ShivaCyan
                    )
                }

                if (message.isError) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onRetry,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = ShivaRed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry", color = ShivaRed, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormattedMarkdownContent(content: String) {
    val context = LocalContext.current
    val parts = content.split("```")

    Column {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                // Code block
                val lines = part.trim().lines()
                val lang = lines.firstOrNull() ?: ""
                val code = if (lines.size > 1) lines.drop(1).joinToString("\n") else part.trim()

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E1E2E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF313244)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = lang.ifBlank { "code" },
                                style = MaterialTheme.typography.labelSmall,
                                color = ShivaCyan
                            )
                            IconButton(
                                onClick = {
                                    val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clip.setPrimaryClip(ClipData.newPlainText("Code", code))
                                    Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondary, modifier = Modifier.size(14.dp))
                            }
                        }
                        Text(
                            text = code,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color(0xFFCDD6F4)
                        )
                    }
                }
            } else if (part.isNotBlank()) {
                Text(
                    text = part.trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
        }
    }
}
