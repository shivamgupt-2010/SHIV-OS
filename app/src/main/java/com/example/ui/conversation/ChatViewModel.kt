package com.example.ui.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.orchestrator.CentralOrchestrator
import com.example.ai.voice.session.VoiceSessionManager
import com.example.ai.voice.session.VoiceSessionState
import com.example.data.local.dao.ChatHistoryDao
import com.example.data.local.entity.ChatHistoryEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String,
    val content: String,
    val isStreaming: Boolean = false
)

class ChatViewModel(
    private val orchestrator: CentralOrchestrator,
    private val voiceSessionManager: VoiceSessionManager,
    private val chatHistoryDao: ChatHistoryDao
) : ViewModel() {
    private val sessionId = "main_chat_session"

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    val voiceState: StateFlow<VoiceSessionState> = voiceSessionManager.state

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            chatHistoryDao.getHistoryBySession(sessionId).collect { history ->
                _messages.value = history.map { 
                    ChatMessage(id = it.id, role = it.role, content = it.content) 
                }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        val userMsg = ChatHistoryEntity(sessionId = sessionId, role = "user", content = text)
        viewModelScope.launch {
            chatHistoryDao.insertMessage(userMsg)
            
            // Add a temporary streaming agent message
            val tempId = UUID.randomUUID().toString()
            _messages.update { current ->
                current + ChatMessage(id = tempId, role = "agent", content = "", isStreaming = true)
            }

            try {
                // Assuming processTaskStream is implemented in CentralOrchestrator
                var currentText = ""
                orchestrator.processTaskStream(text, sessionId).collect { result ->
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
                                current.map { if (it.id == tempId) it.copy(content = currentText) else it }
                            }
                        }
                        else -> {}
                    }
                }
                // Stream finished, save to DB
                val finalMsg = ChatHistoryEntity(id = tempId, sessionId = sessionId, role = "agent", content = currentText)
                chatHistoryDao.insertMessage(finalMsg)
            } catch (e: Exception) {
                 val errorMsg = ChatHistoryEntity(id = tempId, sessionId = sessionId, role = "agent", content = "Fatal Error: ${e.message}")
                 chatHistoryDao.insertMessage(errorMsg)
            }
        }
    }

    fun toggleVoiceInteraction() {
        if (voiceState.value == VoiceSessionState.IDLE || voiceState.value == VoiceSessionState.ERROR) {
            voiceSessionManager.startInteraction()
        } else {
            voiceSessionManager.endSession()
        }
    }
}
