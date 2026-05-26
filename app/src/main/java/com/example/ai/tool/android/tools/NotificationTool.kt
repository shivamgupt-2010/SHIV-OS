package com.example.ai.tool.android.tools

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.ai.tool.BaseTool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*

class NotificationTool(private val context: Context) : BaseTool() {
    override val name = "send_notification"
    override val description = "Sends a local system notification to the user."

    private val channelId = "shivai_agent_channel"

    override suspend fun execute(args: JsonObject): String = withContext(Dispatchers.Main) {
        val title = args["title"]?.jsonPrimitive?.content ?: "ShivAI"
        val message = args["message"]?.jsonPrimitive?.content
            ?: return@withContext "Error: message is required"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "ShivAI Agent Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Just using a random ID for now or timestamp
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        "Successfully sent notification."
    }

    override fun getDeclaration(): JsonObject = buildJsonObject {
        put("name", name)
        put("description", description)
        putJsonObject("parameters") {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("title") {
                    put("type", "STRING")
                    put("description", "Notification title")
                }
                putJsonObject("message") {
                    put("type", "STRING")
                    put("description", "Notification message content")
                }
            }
            putJsonArray("required") {
                add("message")
            }
        }
    }
}
