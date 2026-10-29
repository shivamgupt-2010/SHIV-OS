package com.example.core.launcher

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import com.example.core.context.ContextAwarenessManager
import com.example.core.intelligence.UsagePatternAnalyzer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar

class AdaptiveAppPredictor(
    private val context: Context,
    private val launcherShellManager: LauncherShellManager,
    private val contextAwarenessManager: ContextAwarenessManager,
    private val usagePatternAnalyzer: UsagePatternAnalyzer
) {
    fun hasUsagePermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? android.app.AppOpsManager ?: return false
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    // Real intelligent app surfacing based on Android UsageStatsManager + contextual state
    val predictedApps: Flow<List<AppInfo>> = combine(
        launcherShellManager.installedApps,
        contextAwarenessManager.deviceState
    ) { apps, deviceState ->
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7) // Look back 7 days for rich frequency signal
        }
        val startTime = cal.timeInMillis
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        val usageStatsMap = if (hasUsagePermission()) {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            usm?.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, now)
                ?.groupBy { it.packageName }
                ?.mapValues { entry ->
                    val totalForegroundMs = entry.value.sumOf { it.totalTimeInForeground }
                    val lastUsed = entry.value.maxOfOrNull { it.lastTimeUsed } ?: 0L
                    Pair(totalForegroundMs, lastUsed)
                } ?: emptyMap()
        } else {
            emptyMap()
        }

        val scoredApps = apps.map { app ->
            val pkg = app.packageName
            var score = 0.0

            val stats = usageStatsMap[pkg]
            if (stats != null) {
                val totalForegroundMs = stats.first
                val lastUsed = stats.second
                // 1. Recency Score (higher if used in last 24h)
                val hoursSinceLastUse = if (lastUsed > 0) ((now - lastUsed) / (1000 * 60 * 60)).coerceAtLeast(0) else 168
                val recencyScore = (100.0 / (hoursSinceLastUse + 1.0)) * 1.5

                // 2. Frequency & Engagement Score (minutes used)
                val minutesUsed = (totalForegroundMs / 60000.0).coerceAtMost(600.0)
                val engagementScore = minutesUsed * 0.5

                score += recencyScore + engagementScore
            }

            // 3. Time-of-day contextual affinity boosts
            val pkgLower = pkg.lowercase()
            if (hour in 6..9 && (pkgLower.contains("news") || pkgLower.contains("weather") || pkgLower.contains("mail") || pkgLower.contains("clock"))) {
                score += 15.0
            }
            if (hour in 9..17 && (pkgLower.contains("work") || pkgLower.contains("slack") || pkgLower.contains("teams") || pkgLower.contains("calendar") || pkgLower.contains("doc"))) {
                score += 20.0
            }
            if (hour in 18..23 && (pkgLower.contains("entertainment") || pkgLower.contains("media") || pkgLower.contains("music") || pkgLower.contains("video") || pkgLower.contains("game"))) {
                score += 15.0
            }

            // 4. Device State affinities
            if (deviceState.isCharging && (pkgLower.contains("youtube") || pkgLower.contains("video") || pkgLower.contains("netflix"))) {
                score += 10.0
            }

            Pair(app, score)
        }

        // If no usage permissions or all scores 0, fallback to recently installed / top apps
        val sorted = scoredApps.sortedByDescending { it.second }.map { it.first }
        if (usageStatsMap.isEmpty()) {
            apps.take(8)
        } else {
            sorted.take(8)
        }
    }
}
