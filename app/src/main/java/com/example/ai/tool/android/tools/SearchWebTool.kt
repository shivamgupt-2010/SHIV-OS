package com.example.ai.tool.android.tools

import com.example.ai.tool.BaseTool
import com.example.ai.tool.android.AndroidIntentEngine
import com.example.core.utils.Result
import kotlinx.serialization.json.*

class SearchWebTool(private val intentEngine: AndroidIntentEngine) : BaseTool() {
    override val name = "search_web"
    override val description = "Performs a web search using the default browser or search app."

    override suspend fun execute(args: JsonObject): String {
        val query = args["query"]?.jsonPrimitive?.content
            ?: return "Error: query is required"
        
        return when (val result = intentEngine.searchWeb(query)) {
            is Result.Success -> "Successfully launched search for: $query"
            is Result.Error -> "Failed to search web: ${result.message ?: "Unknown error"}"
            else -> "Unknown error"
        }
    }

    override fun getDeclaration(): JsonObject = buildJsonObject {
        put("name", name)
        put("description", description)
        putJsonObject("parameters") {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("query") {
                    put("type", "STRING")
                    put("description", "The search query to search for")
                }
            }
            putJsonArray("required") {
                add("query")
            }
        }
    }
}
