package com.example.ai.tool.android.tools

import android.content.Context
import com.example.ai.tool.BaseTool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.io.File

class FileWriteTool(private val context: Context) : BaseTool() {
    override val name = "write_file"
    override val description = "Writes text data to a local file in the app's internal storage."

    override suspend fun execute(args: JsonObject): String = withContext(Dispatchers.IO) {
        val fileName = args["file_name"]?.jsonPrimitive?.content
            ?: return@withContext "Error: file_name is required"
        val content = args["content"]?.jsonPrimitive?.content ?: ""
        
        try {
            val file = File(context.filesDir, fileName)
            file.writeText(content)
            "Successfully wrote to file: $fileName"
        } catch (e: Exception) {
            "Failed to write file: ${e.message}"
        }
    }

    override fun getDeclaration(): JsonObject = buildJsonObject {
        put("name", name)
        put("description", description)
        putJsonObject("parameters") {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("file_name") {
                    put("type", "STRING")
                    put("description", "Name of the file to write (e.g., 'notes.txt')")
                }
                putJsonObject("content") {
                    put("type", "STRING")
                    put("description", "Text content to write into the file")
                }
            }
            putJsonArray("required") {
                add("file_name")
                add("content")
            }
        }
    }
}
