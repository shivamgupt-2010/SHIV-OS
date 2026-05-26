package com.example.ai.agent

import com.example.ai.gemini.GenerateContentResponse
import com.example.ai.gemini.Content
import com.example.core.utils.Result
import kotlinx.coroutines.flow.Flow

interface BaseAgent {
    val type: AgentType
    val name: String
    val description: String

    suspend fun execute(prompt: String, sessionId: String, conversationContext: List<Content> = emptyList()): Result<GenerateContentResponse>
    fun executeStream(prompt: String, sessionId: String): Flow<Result<String>>
}
