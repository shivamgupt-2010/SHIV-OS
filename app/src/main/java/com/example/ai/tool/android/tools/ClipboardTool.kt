package com.example.ai.tool.android.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.example.ai.tool.BaseTool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*

class ClipboardTool(private val context: Context) : BaseTool() {
    override val name = "copy_to_clipboard"
    override val description = "Copies the provided text to the device clipboard."

    override suspend fun execute(args: JsonObject): String = withContext(Dispatchers.Main) {
        val text = args["text"]?.jsonPrimitive?.content
            ?: return@withContext "Error: text is required"
        
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("ShivAI", text)
            clipboard.setPrimaryClip(clip)
            "Successfully copied to clipboard."
        } catch (e: Exception) {
            "Failed to copy to clipboard: ${e.message}"
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
                    put("description", "The text to copy to the clipboard")
                }
            }
            putJsonArray("required") {
                add("text")
            }
        }
    }
}
