package com.example.ai.orchestrator

import com.example.ai.agent.AgentType
import com.example.ai.agent.BaseAgent
import com.example.ai.agent.ChatAgent
import com.example.core.utils.Logger
import com.example.core.utils.Result
import com.example.ai.tool.ToolExecutionManager
import com.example.ai.gemini.Content
import com.example.ai.gemini.Part
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

class CentralOrchestrator(
    private val chatAgent: ChatAgent,
    private val studyAgent: com.example.ai.agent.StudyAgent,
    private val codingAgent: com.example.ai.agent.CodingAgent,
    private val stateManager: com.example.ai.state.AIStateManager,
    private val toolExecutionManager: ToolExecutionManager,
    private val shivAIClient: com.example.ai.shivai.client.ShivAIClient? = null
) {
    /**
     * Determines which agent is best suited for the task.
     * Simple keyword based classification for now, 
     * but will use LLM intent classification in the future.
     */
    private fun routeIntent(prompt: String): BaseAgent {
        val lowerPrompt = prompt.lowercase()
        return when {
            lowerPrompt.contains("study") || lowerPrompt.contains("learn") -> studyAgent
            lowerPrompt.contains("code") || lowerPrompt.contains("debug") || lowerPrompt.contains("architecture") -> codingAgent
            else -> chatAgent
        }
    }

    suspend fun processTask(prompt: String, sessionId: String): Result<String> {
        stateManager.updateState(status = com.example.ai.state.ExecutionStatus.ANALYZING_INTENT)
        val agent = routeIntent(prompt)
        Logger.d("Orchestrator routed task to: ${agent.name}")
        stateManager.updateState(agent = agent.name, status = com.example.ai.state.ExecutionStatus.EXECUTING)
        
        var currentPrompt = prompt
        val conversationContext = mutableListOf<Content>()
        var finalResult: Result<String>? = null
        var iterationCount = 0
        val MAX_ITERATIONS = 5
        
        while (finalResult == null && iterationCount < MAX_ITERATIONS) {
            iterationCount++
            when (val response = agent.execute(currentPrompt, sessionId, conversationContext)) {
                is Result.Success -> {
                    val candidate = response.data.candidates?.firstOrNull()
                    val parts = candidate?.content?.parts ?: emptyList()
                    
                    val functionCall = parts.firstOrNull { it.functionCall != null }?.functionCall
                    if (functionCall != null) {
                        try {
                            // We received a tool call
                            stateManager.updateState(status = com.example.ai.state.ExecutionStatus.WAITING_FOR_TOOL)
                            val name = functionCall["name"]?.toString() ?: "unknown"
                            Logger.d("Executing Tool Call: $name")
                            
                            // Add model's function call to conversation context
                            conversationContext.add(Content(role = "model", parts = listOf(Part(functionCall = functionCall))))

                            val functionResponseJson = toolExecutionManager.handleFunctionCall(functionCall)
                            
                            // Add function response back to context
                            conversationContext.add(Content(role = "function", parts = listOf(Part(functionResponse = functionResponseJson))))
                        } catch (e: Exception) {
                            Logger.e("Tool execution failed", e)
                            finalResult = Result.Error(e, "Tool execution failed: ${e.message}")
                        }
                        // We do not change currentPrompt as we want to re-evaluate with the original prompt but new context
                        // The loop will continue
                    } else {
                        val text = parts.firstOrNull()?.text
                        if (text != null) {
                            finalResult = Result.Success(text)
                        } else {
                            finalResult = Result.Error(IllegalStateException("Empty Response"))
                        }
                    }
                }
                is Result.Error -> finalResult = Result.Error(response.exception, response.message)
                is Result.Loading -> {}
            }
        }
        
        if (finalResult == null) {
            finalResult = Result.Error(IllegalStateException("Max iterations reached without a final answer."))
        }
        
        if (finalResult is Result.Success) {
            stateManager.updateState(status = com.example.ai.state.ExecutionStatus.COMPLETED)
        } else if (finalResult is Result.Error) {
            stateManager.updateState(status = com.example.ai.state.ExecutionStatus.FAILED, error = finalResult.message)
        }
        return finalResult
    }
    
    suspend fun processTaskStream(prompt: String, sessionId: String): Flow<Result<String>> {
         val agent = routeIntent(prompt)
         Logger.d("Orchestrator stream routed task to: ${agent.name}")
         if (shivAIClient != null) {
             val agentTag = when (agent) {
                 codingAgent -> "coding"
                 studyAgent -> "study"
                 else -> "general"
             }
             return shivAIClient.streamChat(
                 message = prompt,
                 conversationId = sessionId,
                 agent = agentTag
             )
         }
         return if (agent is ChatAgent) {
             agent.executeStreamReal(prompt, sessionId)
         } else {
             agent.executeStream(prompt, sessionId)
         }
    }
}
