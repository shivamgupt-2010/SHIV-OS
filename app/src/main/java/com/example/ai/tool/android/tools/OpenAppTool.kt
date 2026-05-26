package com.example.ai.tool.android.tools

import com.example.ai.tool.BaseTool
import com.example.ai.tool.android.AndroidIntentEngine
import com.example.core.utils.Result
import kotlinx.serialization.json.*

class OpenAppTool(private val intentEngine: AndroidIntentEngine) : BaseTool() {
    override val name = "open_app"
    override val description = "Opens an application installed on the Android device by its package name."

    override suspend fun execute(args: JsonObject): String {
        val packageName = args["package_name"]?.jsonPrimitive?.content
            ?: return "Error: package_name is required"
        
        return when (val result = intentEngine.openApp(packageName)) {
            is Result.Success -> "Successfully launched app: $packageName"
            is Result.Error -> "Failed to launch app: ${result.message ?: "Unknown error"}"
            else -> "Unknown error"
        }
    }

    override fun getDeclaration(): JsonObject = buildJsonObject {
        put("name", name)
        put("description", description)
        putJsonObject("parameters") {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("package_name") {
                    put("type", "STRING")
                    put("description", "The full package name of the app to launch (e.g., 'com.google.android.youtube')")
                }
            }
            putJsonArray("required") {
                add("package_name")
            }
        }
    }
}
