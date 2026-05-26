package com.example.ai.tool.android.tools

import com.example.ai.tool.BaseTool
import com.example.ai.tool.android.AndroidIntentEngine
import com.example.core.utils.Result
import kotlinx.serialization.json.*

class ShareTool(private val intentEngine: AndroidIntentEngine) : BaseTool() {
    override val name = "share_text"
    override val description = "Opens the Android share sheet to share text with other apps."

    override suspend fun execute(args: JsonObject): String {
        val text = args["text"]?.jsonPrimitive?.content
            ?: return "Error: text is required"
        
        return when (val result = intentEngine.shareText(text)) {
            is Result.Success -> "Successfully opened share sheet for text."
            is Result.Error -> "Failed to share text: ${result.message ?: "Unknown error"}"
            else -> "Unknown error"
        }
    }

    override fun getDeclaration(): JsonObject = buildJsonObject {
        put("name", name)
        put("description", description)
        putJsonObject("parameters") {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("text") {
                    put("type", "STRING")
                    put("description", "The text content to share")
                }
            }
            putJsonArray("required") {
                add("text")
            }
        }
    }
}
