package com.example.ai.tool.android.tools

import android.content.Context
import com.example.ai.tool.BaseTool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.io.File

class FileReadTool(private val context: Context) : BaseTool() {
    override val name = "read_file"
    override val description = "Reads text data from a local file in the app's internal storage."

    override suspend fun execute(args: JsonObject): String = withContext(Dispatchers.IO) {
        val fileName = args["file_name"]?.jsonPrimitive?.content
            ?: return@withContext "Error: file_name is required"
        
        try {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                val content = file.readText()
                "File Content of $fileName:\n$content"
            } else {
                "Error: File $fileName does not exist."
            }
        } catch (e: Exception) {
            "Failed to read file: ${e.message}"
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
                    put("description", "Name of the file to read (e.g., 'notes.txt')")
                }
            }
            putJsonArray("required") {
                add("file_name")
            }
        }
    }
}
