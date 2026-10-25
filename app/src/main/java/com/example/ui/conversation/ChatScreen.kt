package com.example.ui.conversation

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ai.voice.session.VoiceSessionState
import com.example.core.ai.ShivAIAction
import com.example.ui.components.CosmicBackground
import com.example.ui.components.TrishulIcon
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages by viewModel.messages.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val activeAgent by viewModel.activeAgent.collectAsState()
    val pendingAttachment by viewModel.pendingAttachment.collectAsState()
    val pendingAction by viewModel.pendingActionConfirmation.collectAsState()
    val currentlySpeakingId by viewModel.currentlySpeakingId.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    // Audio recording permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleVoiceInteraction()
        } else {
            Toast.makeText(context, "Microphone permission is required for voice interaction", Toast.LENGTH_SHORT).show()
        }
    }

    // File picker launcher for images and documents
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "attachment"
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            viewModel.attachFile(fileName, mimeType, uri.toString(), 1024 * 50)
        }
    }

    // Auto-scroll to bottom when new messages arrive or stream updates
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val lastMessage = messages.lastOrNull()
    LaunchedEffect(lastMessage?.content?.length) {
        if (messages.isNotEmpty() && listState.canScrollForward) {
            listState.scrollToItem(messages.size - 1)
        }
    }

    val availableAgents = remember {
        listOf(
            AgentPillData("executive", "Executive", Icons.Default.AutoAwesome),
            AgentPillData("reasoning", "Reasoning", Icons.Default.Psychology),
            AgentPillData("coding", "Code & Dev", Icons.Default.Code),
            AgentPillData("research", "Research", Icons.Default.TravelExplore),
            AgentPillData("study", "Study", Icons.Default.MenuBook),
            AgentPillData("general", "Automator", Icons.Default.Bolt)
        )
    }

    // Ambient aura animation
    val infiniteTransition = rememberInfiniteTransition(label = "ChatAura")
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraAlpha"
    )

    Box(modifier = modifier.fillMaxSize()) {
        CosmicBackground(showSilhouette = false)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ShivTheme.colors.surface.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, ShivTheme.colors.cardBorder.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                    ) {
                        // Main App Bar Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onBack) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = ShivTheme.colors.textPrimary
                                    )
                                }

                                // Pulsing Trishul Logo
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(ShivTheme.colors.primary.copy(alpha = 0.15f))
                                        .border(
                                            1.dp,
                                            ShivTheme.colors.primary.copy(alpha = auraAlpha),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    TrishulIcon(size = 18.dp)
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = "ShivAI Neural Matrix",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        ),
                                        color = ShivTheme.colors.textPrimary
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Status dot
                                        val statusColor = when {
                                            voiceState == VoiceSessionState.LISTENING -> ShivaRed
                                            messages.any { it.isStreaming } -> ShivaCyan
                                            else -> ShivTheme.colors.primary
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(statusColor)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = when {
                                                voiceState == VoiceSessionState.LISTENING -> "Listening... Speak now"
                                                messages.any { it.isStreaming } -> "Synthesizing response..."
                                                else -> "Online • GPT-OSS 120B Cognitive Core"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            color = ShivTheme.colors.textSecondary
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.createNewSession() }) {
                                    Icon(
                                        Icons.Default.AddComment,
                                        contentDescription = "New Conversation",
                                        tint = ShivTheme.colors.textPrimary
                                    )
                                }

                                Box {
                                    IconButton(onClick = { showMenu = true }) {
                                        Icon(
                                            Icons.Default.MoreVert,
                                            contentDescription = "Options",
                                            tint = ShivTheme.colors.textSecondary
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Export Conversation") },
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
                                            text = { Text("Clear History") },
                                            leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null) },
                                            onClick = {
                                                showMenu = false
                                                viewModel.createNewSession()
                                                Toast.makeText(context, "New chat started", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Agent Swarm Persona Carousel
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableAgents.forEach { agent ->
                                val isSelected = activeAgent == agent.tag
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { viewModel.selectAgent(agent.tag) },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) ShivTheme.colors.primary.copy(alpha = 0.22f) else ShivTheme.colors.surfaceVariant.copy(alpha = 0.6f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.cardBorder
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = agent.icon,
                                            contentDescription = null,
                                            tint = if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.textSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = agent.label,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            ),
                                            color = if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                Surface(
                    color = ShivTheme.colors.surface.copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, ShivTheme.colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        // Pending Attachment Chip
                        AnimatedVisibility(
                            visible = pendingAttachment != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            if (pendingAttachment != null) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = ShivTheme.colors.surfaceVariant,
                                    border = BorderStroke(1.dp, ShivTheme.colors.primary.copy(alpha = 0.5f)),
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
                                            Icon(
                                                Icons.Default.AttachFile,
                                                contentDescription = null,
                                                tint = ShivTheme.colors.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = pendingAttachment!!.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ShivTheme.colors.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.clearAttachment() },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove",
                                                tint = ShivTheme.colors.textSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Pending Action Confirmation Banner
                        AnimatedVisibility(
                            visible = pendingAction != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            if (pendingAction != null) {
                                ActionApprovalCard(
                                    action = pendingAction!!,
                                    onApprove = { viewModel.confirmAction(pendingAction!!) },
                                    onDismiss = { viewModel.dismissAction() }
                                )
                            }
                        }

                        // Voice Recording Indicator Bar
                        AnimatedVisibility(
                            visible = voiceState == VoiceSessionState.LISTENING,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = ShivaRed.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, ShivaRed.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        VoiceEqualizerWaveform(isSpeaking = true)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Listening to your voice...",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = Color.White
                                        )
                                    }
                                    TextButton(onClick = { viewModel.toggleVoiceInteraction() }) {
                                        Text("Stop", color = ShivaRed, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Input Controls Row
                        Surface(
                            shape = RoundedCornerShape(26.dp),
                            color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, ShivTheme.colors.cardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
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
                                        tint = ShivTheme.colors.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                TextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = {
                                        Text(
                                            "Ask ShivAI or enter a task...",
                                            color = ShivTheme.colors.textSecondary,
                                            fontSize = 14.sp
                                        )
                                    },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTextColor = ShivTheme.colors.textPrimary,
                                        unfocusedTextColor = ShivTheme.colors.textPrimary
                                    ),
                                    maxLines = 4
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                // Dynamic Mic / Send Morph Button
                                val hasContent = inputText.isNotBlank() || pendingAttachment != null
                                AnimatedContent(
                                    targetState = hasContent,
                                    transitionSpec = {
                                        scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut()
                                    },
                                    label = "SendMicMorph"
                                ) { isSend ->
                                    if (isSend) {
                                        IconButton(
                                            onClick = {
                                                val textToSend = inputText
                                                inputText = ""
                                                viewModel.sendMessage(textToSend)
                                            },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(ShivTheme.colors.primary)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Send,
                                                contentDescription = "Send",
                                                tint = if (ShivTheme.isDark) Color.Black else Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    } else {
                                        IconButton(
                                            onClick = {
                                                val hasPermission = ContextCompat.checkSelfPermission(
                                                    context,
                                                    Manifest.permission.RECORD_AUDIO
                                                ) == PackageManager.PERMISSION_GRANTED

                                                if (hasPermission) {
                                                    viewModel.toggleVoiceInteraction()
                                                } else {
                                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                }
                                            },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (voiceState != VoiceSessionState.IDLE) ShivaRed else ShivTheme.colors.primary.copy(alpha = 0.2f)
                                                )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Mic,
                                                contentDescription = "Voice Input",
                                                tint = if (voiceState != VoiceSessionState.IDLE) Color.White else ShivTheme.colors.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Futuristic Holographic Welcome Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(ShivTheme.cardRadius),
                            color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.85f),
                            border = BorderStroke(
                                1.dp,
                                Brush.horizontalGradient(
                                    listOf(
                                        ShivTheme.colors.primary.copy(alpha = 0.4f),
                                        ShivTheme.colors.accent.copy(alpha = 0.2f)
                                    )
                                )
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(22.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(ShivTheme.colors.primary.copy(alpha = 0.15f))
                                        .border(1.5.dp, ShivTheme.colors.primary.copy(alpha = 0.6f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    TrishulIcon(size = 28.dp)
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "ShivAI Cognitive Core",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                    color = ShivTheme.colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Real intelligence powered by GPT-OSS 120B & Groq.\nBuild websites, generate videos, automate tasks, or chat naturally.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ShivTheme.colors.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "SUGGESTED CAPABILITIES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = ShivTheme.colors.textSecondary.copy(alpha = 0.8f),
                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                        )

                        // Quick Prompt Cards
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QuickPromptCard(
                                    icon = Icons.Default.Language,
                                    title = "Build Website",
                                    subtitle = "Create modern portfolio HTML",
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.sendMessage("Create a complete single-file portfolio website for a creative developer with dark mode and modern animations.") }
                                )
                                QuickPromptCard(
                                    icon = Icons.Default.Palette,
                                    title = "Apply Theme",
                                    subtitle = "Switch to Cyan Neon",
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.sendMessage("Please switch my launcher theme to Cyan Neon.") }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QuickPromptCard(
                                    icon = Icons.Default.Videocam,
                                    title = "Generate Video",
                                    subtitle = "Cinematic video prompt",
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.sendMessage("Generate a cinematic sci-fi drone shot through a futuristic cyber city at sunset.") }
                                )
                                QuickPromptCard(
                                    icon = Icons.Default.Timer,
                                    title = "Focus Mode",
                                    subtitle = "Start 25m Pomodoro",
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.sendMessage("Start a 25-minute focus session.") }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QuickPromptCard(
                                    icon = Icons.Default.Apps,
                                    title = "Open App",
                                    subtitle = "Launch YouTube",
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.sendMessage("open YouTube") }
                                )
                                QuickPromptCard(
                                    icon = Icons.Default.Security,
                                    title = "Safety Audit",
                                    subtitle = "Check lock status",
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.sendMessage("Check and report my launcher security and app lock protection status.") }
                                )
                            }
                        }
                    }
                }

                items(messages, key = { it.id }) { message ->
                    RichMessageBubble(
                        message = message,
                        isSpeaking = currentlySpeakingId == message.id,
                        onToggleSpeak = { viewModel.toggleSpeak(message.id, message.content) },
                        onRetry = { viewModel.retryMessage(message.id) }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

data class AgentPillData(
    val tag: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun QuickPromptCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, ShivTheme.colors.cardBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(ShivTheme.colors.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ShivTheme.colors.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = ShivTheme.colors.textPrimary,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = ShivTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RichMessageBubble(
    message: ChatMessage,
    isSpeaking: Boolean,
    onToggleSpeak: () -> Unit,
    onRetry: () -> Unit
) {
    val context = LocalContext.current
    val isUser = message.role == "user"
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFmt.format(Date(message.timestamp)) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(ShivTheme.colors.surfaceVariant)
                    .border(1.dp, ShivTheme.colors.primary.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                TrishulIcon(size = 16.dp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 20.dp
            ),
            color = if (isUser) {
                ShivTheme.colors.primary
            } else {
                ShivTheme.colors.surfaceVariant.copy(alpha = 0.94f)
            },
            border = if (!isUser) {
                BorderStroke(
                    1.dp,
                    if (message.isError) ShivaRed.copy(alpha = 0.6f) else ShivTheme.colors.cardBorder
                )
            } else null,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                // Agent Tag on AI bubbles
                if (!isUser) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ShivTheme.colors.primary.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, ShivTheme.colors.primary.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "ShivAI ${message.agentTag?.replaceFirstChar { it.uppercase() } ?: "Executive"}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = ShivTheme.colors.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = ShivTheme.colors.textSecondary.copy(alpha = 0.7f)
                        )
                    }
                }

                // Attachment Preview if present
                if (message.attachment != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.25f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AttachFile,
                                contentDescription = null,
                                tint = if (isUser) Color.White else ShivTheme.colors.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = message.attachment.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isUser) Color.White else ShivTheme.colors.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Render Content (Formatted Markdown or Plain Text)
                if (message.content.contains("```")) {
                    FormattedMarkdownContent(message.content)
                } else {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                        color = if (isUser) {
                            if (ShivTheme.isDark) Color.Black else Color.White
                        } else {
                            ShivTheme.colors.textPrimary
                        }
                    )
                }

                // Streaming Thinking Indicator
                if (message.isStreaming) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ThinkingEqualizerWaveform()
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Synthesizing response...",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = ShivaCyan
                        )
                    }
                }

                // Bottom Metadata & Actions Row
                if (isUser) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = (if (ShivTheme.isDark) Color.Black else Color.White).copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.DoneAll,
                            contentDescription = "Delivered",
                            tint = (if (ShivTheme.isDark) Color.Black else Color.White).copy(alpha = 0.8f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                } else if (!message.isStreaming) {
                    // Actions for AI responses: Copy, Speak, Retry
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Copy button
                            IconButton(
                                onClick = {
                                    val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clip.setPrimaryClip(ClipData.newPlainText("ShivAI", message.content))
                                    Toast.makeText(context, "Copied response", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = ShivTheme.colors.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            // Text-To-Speech Read Aloud button
                            IconButton(
                                onClick = onToggleSpeak,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                    contentDescription = "Speak",
                                    tint = if (isSpeaking) ShivaCyan else ShivTheme.colors.textSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        if (message.isError) {
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
}

@Composable
fun ActionApprovalCard(
    action: ShivAIAction,
    onApprove: () -> Unit,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ActionGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ActionBorderAlpha"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ShivTheme.colors.surfaceVariant.copy(alpha = 0.96f),
        border = BorderStroke(1.5.dp, ShivTheme.colors.primary.copy(alpha = glowAlpha)),
        modifier = Modifier
            .padding(bottom = 6.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val actionIcon = when (action) {
                        is ShivAIAction.CreateWebsite -> Icons.Default.Language
                        is ShivAIAction.GenerateVideo -> Icons.Default.Videocam
                        is ShivAIAction.InstallApp -> Icons.Default.Download
                        is ShivAIAction.ReplyChat -> Icons.Default.Chat
                        is ShivAIAction.ApplyTheme -> Icons.Default.Palette
                        is ShivAIAction.StartFocusSession -> Icons.Default.Timer
                        else -> Icons.Default.AutoAwesome
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(ShivTheme.colors.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(actionIcon, contentDescription = null, tint = ShivTheme.colors.primary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (action) {
                            is ShivAIAction.CreateWebsite -> "Website Ready"
                            is ShivAIAction.GenerateVideo -> "Video Generation"
                            is ShivAIAction.InstallApp -> "App Install"
                            is ShivAIAction.ReplyChat -> "Compose Reply"
                            is ShivAIAction.ApplyTheme -> "Theme Change"
                            is ShivAIAction.StartFocusSession -> "Focus Session"
                            else -> "Launcher Automation"
                        },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = ShivTheme.colors.primary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ShivTheme.colors.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "User Approval Required",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = ShivTheme.colors.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (action) {
                    is ShivAIAction.CreateWebsite -> "${action.title} • ${action.summary}"
                    is ShivAIAction.GenerateVideo -> "Prompt: ${action.prompt} • Style: ${action.style}"
                    is ShivAIAction.InstallApp -> "Install ${action.appName} from Google Play Store"
                    is ShivAIAction.ReplyChat -> "Send via ${action.platform} to ${action.recipient}: '${action.messageText}'"
                    is ShivAIAction.ApplyTheme -> "Switch launcher theme to ${action.presetKey} (${action.reason})"
                    is ShivAIAction.StartFocusSession -> "Start a ${action.minutes}-minute deep focus mode"
                    is ShivAIAction.AutomateTask -> action.summary
                    else -> "Execute proposed launcher task upon your approval?"
                },
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = ShivTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Dismiss", color = ShivTheme.colors.textSecondary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = if (ShivTheme.isDark) Color.Black else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (action) {
                            is ShivAIAction.CreateWebsite -> "Open Website"
                            is ShivAIAction.GenerateVideo -> "Generate Video"
                            is ShivAIAction.InstallApp -> "Open Play Store"
                            is ShivAIAction.ReplyChat -> "Send Reply"
                            is ShivAIAction.ApplyTheme -> "Apply Theme"
                            else -> "Approve & Execute"
                        },
                        color = if (ShivTheme.isDark) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold
                    )
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
                // Code block with syntax styling & one-click copy
                val lines = part.trim().lines()
                val lang = lines.firstOrNull()?.trim() ?: ""
                val code = if (lines.size > 1) lines.drop(1).joinToString("\n") else part.trim()
                var isCopied by remember { mutableStateOf(false) }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF161622),
                    border = BorderStroke(1.dp, Color(0xFF2C2D3E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column {
                        // Code Header Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF202132))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = lang.ifBlank { "code" }.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ShivaCyan,
                                fontSize = 11.sp
                            )
                            Row(
                                modifier = Modifier.clickable {
                                    val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clip.setPrimaryClip(ClipData.newPlainText("Code", code))
                                    isCopied = true
                                    Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = if (isCopied) ShivaCyan else ShivTheme.colors.textSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isCopied) "Copied!" else "Copy",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = if (isCopied) ShivaCyan else ShivTheme.colors.textSecondary
                                )
                            }
                        }

                        // Code Body
                        Text(
                            text = code,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = Color(0xFFCDD6F4),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            } else if (part.isNotBlank()) {
                Text(
                    text = part.trim(),
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    color = ShivTheme.colors.textPrimary
                )
            }
        }
    }
}

@Composable
fun ThinkingEqualizerWaveform() {
    val infiniteTransition = rememberInfiniteTransition(label = "ThinkingEqualizer")
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(16.dp)
    ) {
        listOf(300, 450, 600, 400, 520).forEachIndexed { i, dur ->
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(dur, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$i"
            )
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((16 * scale).dp)
                    .clip(CircleShape)
                    .background(ShivaCyan)
            )
        }
    }
}

@Composable
fun VoiceEqualizerWaveform(isSpeaking: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "VoiceEqualizer")
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(20.dp)
    ) {
        listOf(280, 420, 580, 340, 500, 390).forEachIndexed { i, dur ->
            val scale by infiniteTransition.animateFloat(
                initialValue = if (isSpeaking) 0.25f else 0.2f,
                targetValue = if (isSpeaking) 1.0f else 0.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(dur, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "voice_bar_$i"
            )
            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height((20 * scale).dp)
                    .clip(CircleShape)
                    .background(ShivaRed)
            )
        }
    }
}
