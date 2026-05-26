package com.example.domain.repository

import com.example.core.utils.Result
import com.example.data.local.entity.ChatHistoryEntity
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChatHistory(sessionId: String): Flow<List<ChatHistoryEntity>>
    suspend fun sendMessage(sessionId: String, content: String): Result<Unit>
    suspend fun clearSession(sessionId: String)
}
