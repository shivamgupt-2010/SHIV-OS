package com.example.core.intelligence

import com.example.core.context.ContextAwarenessManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Usage Pattern Analyzer
 * Behavioral analytics: focus sessions, study timing, distraction patterns.
 */
class UsagePatternAnalyzer(private val contextAwarenessManager: ContextAwarenessManager) {

    private val _usageInsights = MutableStateFlow(UsageInsights())
    val usageInsights = _usageInsights.asStateFlow()

    private var currentSessionStartTime = System.currentTimeMillis()
    private var lastForegroundApp: String? = null

    fun updateUsage(newForegroundApp: String?) {
        val now = System.currentTimeMillis()
        
        if (newForegroundApp != lastForegroundApp) {
            val sessionDuration = now - currentSessionStartTime
            
            // Analyze the ended session
            if (lastForegroundApp != null) {
                analyzeSession(lastForegroundApp!!, sessionDuration)
            }
            
            lastForegroundApp = newForegroundApp
            currentSessionStartTime = now
        }
    }

    private fun analyzeSession(packageName: String, durationMs: Long) {
        val isDistraction = analyzeDistraction(packageName)
        
        _usageInsights.update { current ->
            current.copy(
                totalScreenTimeMs = current.totalScreenTimeMs + durationMs,
                distractionTimeMs = if (isDistraction) current.distractionTimeMs + durationMs else current.distractionTimeMs
            )
        }
    }

    private fun analyzeDistraction(packageName: String): Boolean {
        // Very basic definition of distraction apps for demonstration
        val distractionKeywords = listOf("instagram", "tiktok", "facebook", "twitter", "youtube")
        return distractionKeywords.any { packageName.contains(it) }
    }
}

data class UsageInsights(
    val totalScreenTimeMs: Long = 0,
    val focusSessionCount: Int = 0,
    val distractionTimeMs: Long = 0,
    val recommendedAction: String? = null
)
