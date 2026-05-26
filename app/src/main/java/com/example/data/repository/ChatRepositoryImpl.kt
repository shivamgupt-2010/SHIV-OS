package com.example.data.repository

import com.example.core.utils.Result
import com.example.core.utils.DispatcherProvider
import com.example.data.local.dao.ChatHistoryDao
import com.example.data.local.entity.ChatHistoryEntity
import com.example.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class ChatRepositoryImpl(
    private val chatDao: ChatHistoryDao,
    private val dispatcherProvider: DispatcherProvider
    // private val apiService: ChatApiService // to be added when creating API
) : ChatRepository {

    override fun getChatHistory(sessionId: String): Flow<List<ChatHistoryEntity>> {
        return chatDao.getHistoryBySession(sessionId)
    }

    override suspend fun sendMessage(sessionId: String, content: String): Result<Unit> {
        // Here we would implement the offline-first logic
        // 1. Save to local DB (pending)
        val userMessage = ChatHistoryEntity(
            sessionId = sessionId,
            role = "user",
            content = content
        )
        chatDao.insertMessage(userMessage)

        // 2. Make Network Call -> API
        // 3. Save remote response to Local DB
        return Result.Success(Unit)
    }

    override suspend fun clearSession(sessionId: String) {
         chatDao.deleteSession(sessionId)
    }
}
