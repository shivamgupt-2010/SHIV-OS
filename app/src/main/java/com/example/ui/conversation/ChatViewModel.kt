package com.example.ui.conversation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.orchestrator.CentralOrchestrator
import com.example.ai.voice.session.VoiceSessionManager
import com.example.ai.voice.session.VoiceSessionState
import com.example.core.ai.ShivAIAction
import com.example.core.ai.ShivAIActionDispatcher
import com.example.data.local.dao.ChatHistoryDao
import com.example.data.local.entity.ChatHistoryEntity
import com.shivai.sdk.DAGStep
import com.shivai.sdk.ShivAITaskResult
import com.shivai.sdk.SourceCitation
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

data class ChatAttachment(
    val name: String,
    val mimeType: String,
    val uriString: String,
    val sizeBytes: Long = 0
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String, // "user", "agent", "system"
    val content: String,
    val isStreaming: Boolean = false,
    val agentTag: String? = null,
    val attachment: ChatAttachment? = null,
    val dagSteps: List<DAGStep> = emptyList(),
    val citations: List<SourceCitation> = emptyList(),
    val proposedAction: ShivAIAction? = null,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

class ChatViewModel(
    private val orchestrator: CentralOrchestrator,
    private val voiceSessionManager: VoiceSessionManager,
    private val chatHistoryDao: ChatHistoryDao,
    private val launcherShellManager: com.example.core.launcher.LauncherShellManager? = null,
    private val actionDispatcher: ShivAIActionDispatcher? = null
) : ViewModel() {

    private val _currentSessionId = MutableStateFlow("main_chat_session")
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

    private val _activeAgent = MutableStateFlow("executive")
    val activeAgent: StateFlow<String> = _activeAgent.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _pendingAttachment = MutableStateFlow<ChatAttachment?>(null)
    val pendingAttachment: StateFlow<ChatAttachment?> = _pendingAttachment.asStateFlow()

    private val _pendingActionConfirmation = MutableStateFlow<ShivAIAction?>(null)
    val pendingActionConfirmation: StateFlow<ShivAIAction?> = _pendingActionConfirmation.asStateFlow()

    val voiceState: StateFlow<VoiceSessionState> = voiceSessionManager.state

    init {
        loadHistory(_currentSessionId.value)
    }

    fun selectAgent(agentTag: String) {
        _activeAgent.value = agentTag
    }

    fun attachFile(name: String, mimeType: String, uriString: String, sizeBytes: Long) {
        // Enforce 10MB file limit
        if (sizeBytes > 10 * 1024 * 1024) {
            _messages.update { current ->
                current + ChatMessage(
                    role = "system",
                    content = "Attachment exceeds maximum allowed size of 10 MB.",
                    isError = true
                )
            }
            return
        }
        _pendingAttachment.value = ChatAttachment(name, mimeType, uriString, sizeBytes)
    }

    fun clearAttachment() {
        _pendingAttachment.value = null
    }

    fun switchSession(newSessionId: String) {
        _currentSessionId.value = newSessionId
        loadHistory(newSessionId)
    }

    fun createNewSession() {
        val newId = "session_${System.currentTimeMillis()}"
        switchSession(newId)
    }

    private fun loadHistory(sessionId: String) {
        viewModelScope.launch {
            chatHistoryDao.getHistoryBySession(sessionId).collect { history ->
                _messages.value = history.map { entity ->
                    ChatMessage(
                        id = entity.id,
                        role = entity.role,
                        content = entity.content,
                        timestamp = entity.timestamp
                    )
                }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() && _pendingAttachment.value == null) return

        val currentAttachment = _pendingAttachment.value
        _pendingAttachment.value = null
        val session = _currentSessionId.value
        val agent = _activeAgent.value

        val userMsgId = UUID.randomUUID().toString()
        val userMsg = ChatMessage(
            id = userMsgId,
            role = "user",
            content = text,
            agentTag = agent,
            attachment = currentAttachment
        )

        _messages.update { it + userMsg }

        viewModelScope.launch {
            // Save user message to Room
            val entity = ChatHistoryEntity(
                id = userMsgId,
                sessionId = session,
                role = "user",
                content = text
            )
            chatHistoryDao.insertMessage(entity)

            // Direct local app launch interceptor ("open youtube", "launch camera")
            val clean = text.trim().lowercase()
            if ((clean.startsWith("open ") || clean.startsWith("launch ")) && launcherShellManager != null) {
                val targetApp = clean.removePrefix("open ").removePrefix("launch ").trim()
                val installed = launcherShellManager.installedApps.value
                val matched = installed.firstOrNull { it.name.contains(targetApp, ignoreCase = true) }
                    ?: installed.firstOrNull { it.packageName.contains(targetApp, ignoreCase = true) }

                if (matched != null) {
                    val respText = "Opening ${matched.name}..."
                    val agentMsg = ChatMessage(role = "agent", content = respText, agentTag = agent)
                    _messages.update { it + agentMsg }
                    chatHistoryDao.insertMessage(ChatHistoryEntity(sessionId = session, role = "agent", content = respText))
                    launcherShellManager.launchApp(matched.packageName)
                    return@launch
                }
            }

            // Check if text is a candidate for structured action
            if (actionDispatcher != null && (clean.contains("theme") || clean.contains("focus") || clean.contains("layout"))) {
                val candidateJson = when {
                    clean.contains("cyan") -> """{"schema":"SHIV_AI_ACTION_V1","action":"APPLY_THEME","payload":{"presetKey":"CYAN_NEON","reason":"User requested Cyan Neon theme"}}"""
                    clean.contains("gold") -> """{"schema":"SHIV_AI_ACTION_V1","action":"APPLY_THEME","payload":{"presetKey":"SOLAR_GOLD","reason":"User requested Solar Gold theme"}}"""
                    clean.contains("purple") -> """{"schema":"SHIV_AI_ACTION_V1","action":"APPLY_THEME","payload":{"presetKey":"ROYAL_PURPLE","reason":"User requested Royal Purple theme"}}"""
                    clean.contains("focus") -> """{"schema":"SHIV_AI_ACTION_V1","action":"START_FOCUS","payload":{"durationMinutes":25,"title":"Focus Mode"}}"""
                    else -> null
                }

                if (candidateJson != null) {
                    val action = actionDispatcher.parseAction(candidateJson)
                    if (action !is ShivAIAction.InvalidAction) {
                        _pendingActionConfirmation.value = action
                    }
                }
            }

            // Create temporary streaming agent message
            val tempId = UUID.randomUUID().toString()
            _messages.update { current ->
                current + ChatMessage(id = tempId, role = "agent", content = "", isStreaming = true, agentTag = agent)
            }

            try {
                var currentText = ""
                val promptWithContext = if (currentAttachment != null) {
                    "[Attachment: ${currentAttachment.name} (${currentAttachment.mimeType})]\n$text"
                } else {
                    text
                }

                orchestrator.processTaskStream(promptWithContext, session).collect { result ->
                    when (result) {
                        is com.example.core.utils.Result.Success -> {
                            currentText += result.data
                            _messages.update { current ->
                                current.map { if (it.id == tempId) it.copy(content = currentText) else it }
                            }
                        }
                        is com.example.core.utils.Result.Error -> {
                            currentText += "\n\nError: ${result.message}"
                            _messages.update { current ->
                                current.map { if (it.id == tempId) it.copy(content = currentText, isError = true) else it }
                            }
                        }
                        else -> {}
                    }
                }

                _messages.update { current ->
                    current.map { if (it.id == tempId) it.copy(isStreaming = false) else it }
                }

                // Stream finished, persist to Room DB
                val finalMsg = ChatHistoryEntity(id = tempId, sessionId = session, role = "agent", content = currentText)
                chatHistoryDao.insertMessage(finalMsg)
            } catch (e: Exception) {
                _messages.update { current ->
                    current.map { if (it.id == tempId) it.copy(content = "Error from ShivAI: ${e.message}", isStreaming = false, isError = true) else it }
                }
            }
        }
    }

    fun confirmAction(action: ShivAIAction) {
        actionDispatcher?.executeAction(action, isUserConfirmed = true)
        _pendingActionConfirmation.value = null
        _messages.update { current ->
            current + ChatMessage(role = "system", content = "Action applied successfully.")
        }
    }

    fun dismissAction() {
        _pendingActionConfirmation.value = null
    }

    fun retryMessage(messageId: String) {
        val msg = _messages.value.firstOrNull { it.id == messageId } ?: return
        if (msg.role == "user") {
            sendMessage(msg.content)
        }
    }

    fun exportChatAsText(): String {
        val sb = StringBuilder("# ShivAI Conversation Export\n")
        sb.append("Session: ${_currentSessionId.value}\n")
        sb.append("Exported: ${java.util.Date()}\n\n---\n\n")
        for (m in _messages.value) {
            val roleLabel = if (m.role == "user") "You" else "ShivAI (${m.agentTag ?: "General"})"
            sb.append("**$roleLabel**:\n${m.content}\n\n")
        }
        return sb.toString()
    }

    fun toggleVoiceInteraction() {
        if (voiceState.value == VoiceSessionState.IDLE || voiceState.value == VoiceSessionState.ERROR) {
            voiceSessionManager.startInteraction()
        } else {
            voiceSessionManager.endSession()
        }
    }
}
