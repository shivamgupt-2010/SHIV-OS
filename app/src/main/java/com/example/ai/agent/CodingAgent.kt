package com.example.ai.agent

import com.example.ai.context.ContextManager
import com.example.ai.gemini.GenerateContentRequest
import com.example.ai.gemini.GenerateContentResponse
import com.example.ai.gemini.GeminiClient
import com.example.ai.gemini.GenerationConfig
import com.example.ai.gemini.Content
import com.example.ai.prompt.PromptBuilder
import com.example.core.utils.Result
import com.example.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CodingAgent(
    private val geminiClient: GeminiClient,
    private val contextManager: ContextManager,
    private val chatRepository: ChatRepository
) : BaseAgent {
    override val type = AgentType.CODING
    override val name = "ShivAI Coding Agent"
    override val description = "Specialized in software engineering, architecture, and debugging."

    private val modelId = "gemini-3.1-pro-preview" 

    override suspend fun execute(prompt: String, sessionId: String, conversationContext: List<Content>): Result<GenerateContentResponse> {
        val request = buildRequest(prompt, sessionId, conversationContext)
        return geminiClient.generateContent(modelId, request)
    }

    override fun executeStream(prompt: String, sessionId: String): Flow<Result<String>> {
        val request = buildRequestStream(prompt, sessionId)
        return geminiClient.generateContentStream(modelId, request)
    }

    private fun buildRequestStream(prompt: String, sessionId: String): GenerateContentRequest {
        val builder = PromptBuilder()
            .setSystemIdentity("ShivAI Engineer", "You are a senior AI software engineer and system architect.")
            .addRule("Always outputs production-ready code with minimal surrounding chatter.")
            .addRule("Explain architecture decisions professionally.")
        return GenerateContentRequest(
            contents = builder.buildContents(prompt),
            systemInstruction = builder.buildSystemInstruction(),
            generationConfig = GenerationConfig(temperature = 0.2f)
        )
    }

    private suspend fun buildRequest(prompt: String, sessionId: String, conversationContext: List<Content> = emptyList()): GenerateContentRequest {
        val builder = PromptBuilder()
            .setSystemIdentity("ShivAI Engineer", "You are a senior AI software engineer and system architect.")
            .addRule("Always outputs production-ready code with minimal surrounding chatter.")
            
        contextManager.injectContext(prompt, builder)
        
        val history = chatRepository.getChatHistory(sessionId).first()
        history.takeLast(10).forEach { msg ->
            builder.addHistory(role = msg.role, text = msg.content)
        }

        conversationContext.forEach {
            builder.addHistoryContext(it)
        }

        return GenerateContentRequest(
            contents = builder.buildContents(prompt),
            systemInstruction = builder.buildSystemInstruction(),
            generationConfig = GenerationConfig(temperature = 0.2f)
        )
    }
}
