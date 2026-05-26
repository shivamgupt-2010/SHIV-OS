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

class StudyAgent(
    private val geminiClient: GeminiClient,
    private val contextManager: ContextManager,
    private val chatRepository: ChatRepository
) : BaseAgent {
    override val type = AgentType.STUDY
    override val name = "ShivAI Study Agent"
    override val description = "Specialized in learning, tutoring, and explaining complex concepts."

    private val modelId = "gemini-3.1-pro-preview" // Better reasoning for study

    override suspend fun execute(prompt: String, sessionId: String, conversationContext: List<Content>): Result<GenerateContentResponse> {
        val request = buildRequest(prompt, sessionId, conversationContext)
        return geminiClient.generateContent(modelId, request)
    }

    override fun executeStream(prompt: String, sessionId: String): Flow<Result<String>> {
        val request = buildRequestStream(prompt, sessionId) // normally we'd make this inside flow
        return geminiClient.generateContentStream(modelId, request)
    }

    private fun buildRequestStream(prompt: String, sessionId: String): GenerateContentRequest {
        val builder = PromptBuilder()
            .setSystemIdentity("ShivAI Tutor", "You are an expert tutor. Explain things via Socratic method and break down complex concepts into simple analogies.")
            .addRule("Do NOT just give the answer. Instead, ask questions to guide the student.")
        return GenerateContentRequest(
            contents = builder.buildContents(prompt),
            systemInstruction = builder.buildSystemInstruction(),
            generationConfig = GenerationConfig(temperature = 0.5f)
        )
    }

    private suspend fun buildRequest(prompt: String, sessionId: String, conversationContext: List<Content> = emptyList()): GenerateContentRequest {
        val builder = PromptBuilder()
            .setSystemIdentity("ShivAI Tutor", "You are an expert tutor. Explain things via Socratic method and break down complex concepts into simple analogies.")
            .addRule("Do NOT just give the answer. Instead, ask questions to guide the student.")

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
            generationConfig = GenerationConfig(temperature = 0.5f)
        )
    }
}
