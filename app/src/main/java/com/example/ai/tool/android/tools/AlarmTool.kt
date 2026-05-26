package com.example.ai.tool.android.tools

import com.example.ai.tool.BaseTool
import com.example.ai.tool.android.AndroidIntentEngine
import com.example.core.utils.Result
import kotlinx.serialization.json.*

class AlarmTool(private val intentEngine: AndroidIntentEngine) : BaseTool() {
    override val name = "set_alarm"
    override val description = "Sets an alarm on the device."

    override suspend fun execute(args: JsonObject): String {
        val hour = args["hour"]?.jsonPrimitive?.intOrNull
            ?: return "Error: hour is required and must be an integer (0-23)"
        val minute = args["minute"]?.jsonPrimitive?.intOrNull
            ?: return "Error: minute is required and must be an integer (0-59)"
        val message = args["message"]?.jsonPrimitive?.content ?: "Alarm"
        
        return when (val result = intentEngine.setAlarm(hour, minute, message)) {
            is Result.Success -> "Successfully set alarm for $hour:$minute with message: $message"
            is Result.Error -> "Failed to set alarm: ${result.message ?: "Unknown error"}"
            else -> "Unknown error"
        }
    }

    override fun getDeclaration(): JsonObject = buildJsonObject {
        put("name", name)
        put("description", description)
        putJsonObject("parameters") {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("hour") {
                    put("type", "INTEGER")
                    put("description", "The hour of the alarm (0-23)")
                }
                putJsonObject("minute") {
                    put("type", "INTEGER")
                    put("description", "The minute of the alarm (0-59)")
                }
                putJsonObject("message") {
                    put("type", "STRING")
                    put("description", "Optional message or label for the alarm")
                }
            }
            putJsonArray("required") {
                add("hour")
                add("minute")
            }
        }
    }
}
