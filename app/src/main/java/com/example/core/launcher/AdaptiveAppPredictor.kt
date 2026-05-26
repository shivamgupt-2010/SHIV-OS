package com.example.core.launcher

import com.example.core.context.ContextAwarenessManager
import com.example.core.intelligence.UsagePatternAnalyzer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar

class AdaptiveAppPredictor(
    private val launcherShellManager: LauncherShellManager,
    private val contextAwarenessManager: ContextAwarenessManager,
    private val usagePatternAnalyzer: UsagePatternAnalyzer
) {
    // Intelligent app surfacing based on context
    val predictedApps: Flow<List<AppInfo>> = combine(
        launcherShellManager.installedApps,
        contextAwarenessManager.deviceState
    ) { apps, deviceState ->
        
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        val scoredApps = apps.map { app ->
            var score = 0
            val pkg = app.packageName.lowercase()
            
            // Basic heuristics based on device state
            if (deviceState.isCharging && (pkg.contains("video") || pkg.contains("media") || pkg.contains("youtube"))) {
                score += 10
            }
            if (deviceState.isWifiConnected && (pkg.contains("social") || pkg.contains("insta") || pkg.contains("tiktok"))) {
                score += 5
            }
            if (!deviceState.isWifiConnected && (pkg.contains("game") || pkg.contains("video"))) {
                score -= 10 // Penalize data heavy apps without wifi
            }
            
            // Time based heuristics
            if (hour in 6..9 && (pkg.contains("news") || pkg.contains("weather") || pkg.contains("mail"))) {
                score += 15
            }
            if (hour in 18..23 && (pkg.contains("netflix") || pkg.contains("music") || pkg.contains("game"))) {
                score += 10
            }
            if (hour in 9..17 && (pkg.contains("slack") || pkg.contains("teams") || pkg.contains("calendar"))) {
                score += 12
            }
            
            Pair(app, score)
        }
        
        scoredApps.sortedByDescending { it.second }.map { it.first }.take(8)
    }
}
