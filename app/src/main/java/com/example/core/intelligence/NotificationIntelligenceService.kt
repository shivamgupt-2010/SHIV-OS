package com.example.core.intelligence

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.core.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Notification Intelligence System
 * Integrates as a NotificationListenerService to intercept, classify, and summarize notifications.
 */
data class IntelligentNotification(
    val packageName: String,
    val title: String,
    val content: String,
    val timestamp: Long,
    val inferredCategory: NotificationCategory,
    val suggestedAction: String? = null
)

class NotificationIntelligenceService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private val _notificationFlow = MutableSharedFlow<IntelligentNotification>(extraBufferCapacity = 50)
        val notificationFlow = _notificationFlow.asSharedFlow()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkg = sbn.packageName
        val extras = sbn.notification.extras
        val title = extras.getString(android.app.Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString() ?: ""

        val category = classifyNotification(pkg, title, text)
        val action = generateSuggestedAction(category, text)

        val intelligentNotif = IntelligentNotification(
            packageName = pkg,
            title = title,
            content = text,
            timestamp = sbn.postTime,
            inferredCategory = category,
            suggestedAction = action
        )

        Logger.d("Notification Posted: [${category.name}] $pkg - $title")
        
        serviceScope.launch {
            _notificationFlow.emit(intelligentNotif)
            
            // Auto-suppress or store contextually if focus mode is active
            // This is where we'd invoke the AI Contextual Memory Engine
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }

    private fun classifyNotification(pkg: String, title: String, text: String): NotificationCategory {
        val lowerText = text.lowercase()
        return when {
            pkg.contains("messaging") || pkg.contains("whatsapp") || pkg.contains("telegram") || pkg.contains("discord") -> NotificationCategory.COMMUNICATION
            pkg.contains("calendar") || lowerText.contains("meeting") || lowerText.contains("reminder") -> NotificationCategory.SCHEDULE
            pkg.contains("mail") || pkg.contains("gmail") -> NotificationCategory.WORK
            else -> NotificationCategory.GENERAL
        }
    }
    
    private fun generateSuggestedAction(category: NotificationCategory, text: String): String? {
        // Very basic semantic heuristic mock
        return when (category) {
            NotificationCategory.COMMUNICATION -> if (text.contains("?")) "Draft quick reply" else "Summarize thread"
            NotificationCategory.SCHEDULE -> "Prepare workflow context"
            NotificationCategory.WORK -> "Extract action items"
            else -> null
        }
    }
}

enum class NotificationCategory {
    COMMUNICATION, WORK, SCHEDULE, MEDIA, GENERAL, SYSTEM
}
