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
import kotlinx.coroutines.flow.map

class ChatAgent(
    private val geminiClient: GeminiClient,
    private val contextManager: ContextManager,
    private val chatRepository: ChatRepository,
    private val toolRegistry: com.example.ai.tool.ToolRegistry
) : BaseAgent {
    override val type = AgentType.CHAT
    override val name = "ShivAI Chat Agent"
    override val description = "General conversational AI and default orchestrator fallback."

    private val modelId = "gemini-3.5-flash" // Fast responses for general talk

    override suspend fun execute(prompt: String, sessionId: String, conversationContext: List<Content>): Result<GenerateContentResponse> {
        val request = buildRequest(prompt, sessionId, conversationContext)
        return geminiClient.generateContent(modelId, request)
    }

    override fun executeStream(prompt: String, sessionId: String): Flow<Result<String>> {
        // Unfortunately flow builder does not easily support suspend call before emission in this signature,
        // so we would typically map or build it inside the flow.
        // For simplicity:
        throw NotImplementedError("To be implemented with Flow collector")
    }
    
    suspend fun executeStreamReal(prompt: String, sessionId: String): Flow<Result<String>> {
        val request = buildRequest(prompt, sessionId)
        return geminiClient.generateContentStream(modelId, request)
    }

    private suspend fun buildRequest(prompt: String, sessionId: String, conversationContext: List<Content> = emptyList()): GenerateContentRequest {
        val builder = PromptBuilder()
            .setSystemIdentity("ShivAI", "You are the personal AI companion of the user.")
            .addRule("Keep responses concise and helpful.")
            .addRule("Always speak in a professional yet approachable tone.")

        contextManager.injectContext(prompt, builder)

        val history = chatRepository.getChatHistory(sessionId).first() // get recent
        history.takeLast(10).forEach { msg ->
            builder.addHistory(role = msg.role, text = msg.content)
        }
        
        conversationContext.forEach {
            builder.addHistoryContext(it)
        }
        
        val toolsList = listOf(toolRegistry.getAllDeclarations())

        return GenerateContentRequest(
            contents = builder.buildContents(prompt),
            systemInstruction = builder.buildSystemInstruction(),
            tools = toolsList,
            generationConfig = GenerationConfig(
                temperature = 0.7f
            )
        )
    }
}
