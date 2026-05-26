package com.example.ai.tool.android.tools

import com.example.ai.tool.BaseTool
import com.example.ai.tool.android.AndroidIntentEngine
import com.example.core.utils.Result
import kotlinx.serialization.json.*

class DeepLinkTool(private val intentEngine: AndroidIntentEngine) : BaseTool() {
    override val name = "open_deep_link"
    override val description = "Opens a deep link URI (like 'https://maps.google.com/...' or custom schema) to navigate the user or trigger another app."

    override suspend fun execute(args: JsonObject): String {
        val uri = args["uri"]?.jsonPrimitive?.content
            ?: return "Error: uri is required"
        
        return when (val result = intentEngine.openUrl(uri)) {
            is Result.Success -> "Successfully launched deep link: $uri"
            is Result.Error -> "Failed to launch deep link: ${result.message ?: "Unknown error"}"
            else -> "Unknown error"
        }
    }

    override fun getDeclaration(): JsonObject = buildJsonObject {
        put("name", name)
        put("description", description)
        putJsonObject("parameters") {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("uri") {
                    put("type", "STRING")
                    put("description", "The URI string to open (e.g. 'https://www.google.com' or 'geo:0,0?q=restaurants')")
                }
            }
            putJsonArray("required") {
                add("uri")
            }
        }
    }
}
