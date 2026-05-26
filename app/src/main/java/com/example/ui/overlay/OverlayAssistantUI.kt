package com.example.ui.overlay

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.intelligence.ScreenIntelligence
import com.example.ui.ambient.AmbientActivityGlow
import com.example.ui.ambient.SiriLikeWaveform

@Composable
fun OverlayAssistantUI(
    isExpanded: Boolean,
    aiRuntimeManager: com.example.core.runtime.AIRuntimeManager,
    screenContextLayer: com.example.core.context.ScreenContextLayer,
    onToggleExpand: () -> Unit,
    onClose: () -> Unit
) {
    var screenSemanticData by remember { mutableStateOf<com.example.core.context.ScreenSemanticData?>(null) }
    
    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            screenSemanticData = screenContextLayer.analyzeScreenContents()
        }
    }

    val streamingResponse by aiRuntimeManager.streamingResponse.collectAsState()
    val isStreaming by aiRuntimeManager.isStreaming.collectAsState()
    var isListening by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = isExpanded,
        label = "overlay_expand",
        transitionSpec = {
            fadeIn() + slideInVertically { it / 2 } togetherWith fadeOut() + slideOutVertically { it / 2 }
        }
    ) { expanded ->
        if (expanded) {
            // Expanded Chat / Assistant view
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp)),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "ShivAI Assistant",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            IconButton(onClick = onToggleExpand) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Minimize")
                            }
                            IconButton(onClick = onClose) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (screenSemanticData != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                val studyBadge = if (screenSemanticData?.isStudySession == true) "[STUDY MODE] " else ""
                                Text("Context: $studyBadge${screenSemanticData?.detectedEntities?.size ?: 0} elements", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(screenSemanticData?.extractedText?.take(3)?.joinToString() ?: "Looking at screen...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (streamingResponse.isNotEmpty()) {
                        Text(streamingResponse, style = MaterialTheme.typography.bodyLarge)
                    } else {
                        Text("How can I help you?", style = MaterialTheme.typography.bodyLarge)
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isListening || isStreaming) {
                            AmbientActivityGlow(isActive = true, modifier = Modifier.size(80.dp))
                        }
                        FloatingActionButton(
                            onClick = { 
                                isListening = !isListening
                                if (isListening) {
                                    val contextData = screenSemanticData?.extractedText?.joinToString() ?: "No context"
                                    aiRuntimeManager.startStreamingSession("Analyze this screen for me.", contextData)
                                } else {
                                    aiRuntimeManager.cancelStreaming()
                                }
                            },
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            if (isListening) {
                                SiriLikeWaveform(isSpeaking = true, modifier = Modifier.size(32.dp))
                            } else {
                                Icon(Icons.Default.Mic, contentDescription = "Listen")
                            }
                        }
                    }
                }
            }
        } else {
            // Floating minimized bubble
            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onToggleExpand),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AmbientActivityGlow(isActive = true, modifier = Modifier.fillMaxSize())
                    Icon(
                        Icons.Default.AutoAwesome, 
                        contentDescription = "Open ShivAI",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
