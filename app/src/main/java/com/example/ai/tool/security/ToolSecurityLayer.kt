package com.example.ai.tool.security

import android.util.Log
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

class ToolSecurityLayer {

    // E.g. block sharing passwords or block opening malicious apps
    private val blockedPackages = listOf("com.android.settings.Reset")
    
    fun isToolExecutionAllowed(toolName: String, args: JsonObject): Boolean {
        if (toolName == "open_app") {
            val pkg = args["package_name"]?.jsonPrimitive?.content
            if (blockedPackages.contains(pkg)) {
                Log.w("ShivAI", "Blocked execution of open_app for package: $pkg")
                return false
            }
        }
        return true
    }
}
