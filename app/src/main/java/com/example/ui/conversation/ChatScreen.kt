package com.example.ui.conversation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ai.voice.session.VoiceSessionState
import com.example.ui.ambient.AmbientActivityGlow
import com.example.ui.ambient.SiriLikeWaveform

@Composable
fun ChatScreen(viewModel: ChatViewModel, modifier: Modifier = Modifier) {
    val messages by viewModel.messages.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    
    var inputText by remember { mutableStateOf("") }
    
    val isStreaming = voiceState == VoiceSessionState.THINKING || voiceState == VoiceSessionState.LISTENING

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Ambient background glow
        AmbientActivityGlow(
            modifier = Modifier.fillMaxSize().align(Alignment.Center),
            isActive = isStreaming,
            isThinking = voiceState == VoiceSessionState.THINKING
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Voice State overlay or header status
            VoiceStateIndicator(voiceState = voiceState)

            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                reverseLayout = false
            ) {
                items(messages) { message ->
                    ChatMessageItem(message)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask ShivAI...") },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    if (inputText.isBlank()) {
                        FloatingActionButton(
                            onClick = { viewModel.toggleVoiceInteraction() },
                            shape = CircleShape,
                            containerColor = if (voiceState != VoiceSessionState.IDLE) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp)
                        ) {
                            if (voiceState == VoiceSessionState.LISTENING || voiceState == VoiceSessionState.THINKING) {
                                SiriLikeWaveform(isSpeaking = true, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Input"
                                )
                            }
                        }
                    } else {
                        FloatingActionButton(
                            onClick = {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            },
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Message"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceStateIndicator(voiceState: VoiceSessionState) {
    AnimatedVisibility(
        visible = voiceState != VoiceSessionState.IDLE,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                if (voiceState == VoiceSessionState.LISTENING) {
                    SiriLikeWaveform(isSpeaking = true, modifier = Modifier.width(48.dp))
                } else if (voiceState == VoiceSessionState.THINKING) {
                    AmbientActivityGlow(modifier = Modifier.size(16.dp), isThinking = true)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = when(voiceState) {
                        VoiceSessionState.LISTENING -> "Listening..."
                        VoiceSessionState.THINKING -> "Thinking..."
                        VoiceSessionState.SPEAKING -> "Speaking..."
                        VoiceSessionState.ERROR -> "Error"
                        else -> ""
                    },
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    val isUser = message.role == "user"
    val containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (isUser) {
        RoundedCornerShape(24.dp, 24.dp, 4.dp, 24.dp)
    } else {
        RoundedCornerShape(24.dp, 24.dp, 24.dp, 4.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Surface(
            color = containerColor,
            contentColor = contentColor,
            shape = shape,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
