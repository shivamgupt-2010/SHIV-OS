package com.example.ai.tool

import com.example.core.utils.Logger
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray

class ToolRegistry(initialTools: List<BaseTool> = emptyList()) {
    private val tools = mutableMapOf<String, BaseTool>()

    init {
        initialTools.forEach { registerTool(it) }
    }

    fun registerTool(tool: BaseTool) {
        tools[tool.name] = tool
    }

    fun getTool(name: String): BaseTool? {
        return tools[name]
    }

    fun getAllDeclarations(): JsonObject {
        return buildJsonObject {
            putJsonArray("functionDeclarations") {
                tools.values.forEach { tool ->
                    add(tool.getDeclaration())
                }
            }
        }
    }

    suspend fun executeTool(name: String, args: JsonObject): String {
        val tool = getTool(name)
        return if (tool != null) {
            try {
                tool.execute(args)
            } catch (e: Exception) {
                Logger.e("Tool execution failed: $name", e)
                "Error executing tool: ${e.message}"
            }
        } else {
             "Tool not found: $name"
        }
    }
}
