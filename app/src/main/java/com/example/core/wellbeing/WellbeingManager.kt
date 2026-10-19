package com.example.core.wellbeing

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import com.example.data.local.dao.WellbeingDao
import com.example.data.local.entity.WellbeingBudgetEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class AppUsageRecord(
    val packageName: String,
    val totalTimeInForegroundMinutes: Long,
    val lastTimeUsed: Long
)

data class FocusSession(
    val title: String,
    val durationMinutes: Int,
    val remainingSeconds: Int,
    val isActive: Boolean
)

class WellbeingManager(
    private val context: Context,
    private val wellbeingDao: WellbeingDao,
    private val scope: CoroutineScope
) {
    val budgets: StateFlow<List<WellbeingBudgetEntity>> = wellbeingDao.getAllBudgetsFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val _focusSession = MutableStateFlow(FocusSession("Study & Deep Work", 25, 25 * 60, false))
    val focusSession: StateFlow<FocusSession> = _focusSession.asStateFlow()

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

    fun getTodayUsageStats(): List<AppUsageRecord> {
        if (!hasUsagePermission()) return emptyList()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return emptyList()

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis
        val now = System.currentTimeMillis()

        val stats: List<UsageStats> = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfDay,
            now
        ) ?: emptyList()

        return stats.filter { it.totalTimeInForeground > 60_000 } // Over 1 min
            .map {
                AppUsageRecord(
                    packageName = it.packageName,
                    totalTimeInForegroundMinutes = it.totalTimeInForeground / 60_000,
                    lastTimeUsed = it.lastTimeUsed
                )
            }
            .sortedByDescending { it.totalTimeInForegroundMinutes }
    }

    fun getTotalScreenTimeMinutesToday(): Long {
        return getTodayUsageStats().sumOf { it.totalTimeInForegroundMinutes }
    }

    fun setAppBudget(packageName: String, appName: String, budgetMinutes: Int, intentionalFriction: Boolean) {
        scope.launch(Dispatchers.IO) {
            wellbeingDao.saveBudget(
                WellbeingBudgetEntity(
                    packageName = packageName,
                    appName = appName,
                    dailyBudgetMinutes = budgetMinutes,
                    intentionalOpeningEnabled = intentionalFriction,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteAppBudget(packageName: String) {
        scope.launch(Dispatchers.IO) {
            wellbeingDao.deleteBudget(packageName)
        }
    }

    fun isAppOverBudget(packageName: String): Boolean {
        val budget = budgets.value.firstOrNull { it.packageName == packageName } ?: return false
        val todayUsage = getTodayUsageStats().firstOrNull { it.packageName == packageName }?.totalTimeInForegroundMinutes ?: 0L
        return todayUsage >= budget.dailyBudgetMinutes
    }

    fun requiresIntentionalPrompt(packageName: String): Boolean {
        val budget = budgets.value.firstOrNull { it.packageName == packageName }
        return budget?.intentionalOpeningEnabled == true
    }

    fun startFocusSession(durationMinutes: Int = 25, title: String = "Deep Work") {
        _focusSession.value = FocusSession(
            title = title,
            durationMinutes = durationMinutes,
            remainingSeconds = durationMinutes * 60,
            isActive = true
        )
    }

    fun stopFocusSession() {
        val curr = _focusSession.value
        _focusSession.value = curr.copy(isActive = false)
    }
}
