package com.example.ui.conversation

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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.voice.session.VoiceSessionState
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
    val messages by viewModel.messages.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    var inputText by remember { mutableStateOf("") }

    val quickPrompts = listOf(
        "💡 Explain a concept",
        "✍️ Write / edit something",
        "📅 Plan my day",
        "⚡ Solve a problem"
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            TrishulIcon(size = 24.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ShivAI Chat",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }

                        IconButton(onClick = { /* Menu */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextSecondary)
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
                    Row(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = ShivaCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))

                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Type your message...", color = TextSecondary, fontSize = 14.sp) },
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

                        Spacer(modifier = Modifier.width(8.dp))

                        if (inputText.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val textToSend = inputText
                                    inputText = ""
                                    viewModel.sendMessage(textToSend)
                                },
                                modifier = Modifier
                                    .size(42.dp)
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
                                    .size(42.dp)
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
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // If chat is empty, show Hero Welcome Card + Quick Prompt Chips
                if (messages.isEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(20.dp))

                        // Hero Welcome Card matching Screen 3
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(26.dp),
                            color = CosmicSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                TrishulIcon(size = 48.dp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Hello Shivam!",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "I'm ShivAI. Your personal AI assistant.\nHow can I help you today?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Quick Prompt Chips (2x2 Grid)
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                PromptChip(
                                    text = quickPrompts[0],
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.sendMessage("Explain the core concepts of quantum computing simply.") }
                                )
                                PromptChip(
                                    text = quickPrompts[1],
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.sendMessage("Help me write and edit a concise project roadmap.") }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                PromptChip(
                                    text = quickPrompts[2],
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.sendMessage("Plan an efficient 12-hour high-focus daily schedule.") }
                                )
                                PromptChip(
                                    text = quickPrompts[3],
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.sendMessage("I have a complex technical problem. Help me debug it step by step.") }
                                )
                            }
                        }
                    }
                }

                // Messages list
                items(messages, key = { it.id }) { message ->
                    MessageBubble(message)
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
fun PromptChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = CosmicSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = TextPrimary
            )
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CosmicSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                TrishulIcon(size = 18.dp)
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            color = if (isUser) ShivaIndigo else CosmicSurface,
            border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder) else null,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) Color.White else TextPrimary
                )
                if (message.isStreaming) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ShivAI is thinking...",
                        style = MaterialTheme.typography.labelSmall,
                        color = ShivaCyan
                    )
                }
            }
        }
    }
}
