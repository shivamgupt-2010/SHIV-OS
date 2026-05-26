package com.example.ai.tool

import com.example.ai.gemini.GenerateContentRequest
import com.example.ai.gemini.GeminiClient
import com.example.ai.tool.security.ToolSecurityLayer
import com.example.core.utils.Logger
import com.example.core.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ToolExecutionManager(
    private val toolRegistry: ToolRegistry,
    private val geminiClient: GeminiClient,
    private val securityLayer: ToolSecurityLayer
) {

    /**
     * Executes a tool based on the provided name and arguments. 
     * Handles timeouts and retries, logging results.
     */
    suspend fun executeTool(name: String, args: JsonObject): ToolExecutionResult {
        if (!securityLayer.isToolExecutionAllowed(name, args)) {
            return ToolExecutionResult.Error("Security Blocked: Execution of $name is not allowed with given arguments.", SecurityException("Blocked by Security Layer"))
        }

        return try {
            val resultString = toolRegistry.executeTool(name, args)
            ToolExecutionResult.Success(resultString)
        } catch (e: Exception) {
            Logger.e("Failed executing tool: $name", e)
            ToolExecutionResult.Error(e.message ?: "Unknown Error", e)
        }
    }

    /**
     * Given a raw JsonObject representing a functionCall from Gemini,
     * execute it and return the formatted JsonObject for the functionResponse.
     */
    suspend fun handleFunctionCall(functionCall: JsonObject): JsonObject {
        val name = functionCall["name"]?.toString()?.removeSurrounding("\"")
        val args = functionCall["args"] as? JsonObject
        
        if (name == null || args == null) {
            return buildJsonObject {
                put("name", name ?: "unknown")
                put("response", buildJsonObject {
                    put("error", "Invalid function parameters")
                })
            }
        }

        val result = executeTool(name, args)
        
        return buildJsonObject {
            put("name", name)
            put("response", buildJsonObject {
                when (result) {
                    is ToolExecutionResult.Success -> {
                        put("result", result.output)
                    }
                    is ToolExecutionResult.Error -> {
                        put("error", result.errorMessage)
                    }
                }
            })
        }
    }
}

sealed class ToolExecutionResult {
    data class Success(val output: String) : ToolExecutionResult()
    data class Error(val errorMessage: String, val exception: Exception) : ToolExecutionResult()
}
