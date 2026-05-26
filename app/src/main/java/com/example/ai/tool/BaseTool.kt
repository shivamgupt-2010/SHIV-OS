package com.example.ai.tool

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.putJsonArray

abstract class BaseTool {
    abstract val name: String
    abstract val description: String
    
    /**
     * Executes the tool logic with the provided arguments.
     */
    abstract suspend fun execute(args: JsonObject): String

    /**
     * Generates the JSON Schema declaration for Gemini tool integration.
     */
    abstract fun getDeclaration(): JsonObject
}
