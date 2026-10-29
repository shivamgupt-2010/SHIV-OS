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

enum class FocusModeType(val title: String, val defaultMinutes: Int, val description: String) {
    STUDY("Study Mode", 45, "Blocks games & entertainment, permits educational apps"),
    WORK("Work Mode", 60, "Mutes casual notifications, keeps communication tools"),
    SLEEP("Sleep Mode", 480, "Downtime enforcement, silent mode & dimmed ambient"),
    DEEP_FOCUS("Deep Focus", 25, "Pomodoro interval with absolute distraction blocking"),
    CUSTOM("Custom Mode", 30, "User-configured duration and app exceptions")
}

data class FocusSession(
    val title: String,
    val modeType: FocusModeType = FocusModeType.DEEP_FOCUS,
    val durationMinutes: Int,
    val remainingSeconds: Int,
    val isActive: Boolean,
    val isPaused: Boolean = false
)

data class DayUsageSummary(
    val dayLabel: String,
    val totalMinutes: Long
)

class WellbeingManager(
    private val context: Context,
    private val wellbeingDao: WellbeingDao,
    private val scope: CoroutineScope
) {
    private val prefs = context.getSharedPreferences("shiv_wellbeing_prefs", Context.MODE_PRIVATE)

    val budgets: StateFlow<List<WellbeingBudgetEntity>> = wellbeingDao.getAllBudgetsFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val _focusSession = MutableStateFlow(
        FocusSession(FocusModeType.DEEP_FOCUS.title, FocusModeType.DEEP_FOCUS, 25, 25 * 60, false)
    )
    val focusSession: StateFlow<FocusSession> = _focusSession.asStateFlow()

    private val _dailyUnlockCount = MutableStateFlow(prefs.getInt("unlock_count_today", 42))
    val dailyUnlockCount: StateFlow<Int> = _dailyUnlockCount.asStateFlow()

    private val _dailyNotificationCount = MutableStateFlow(prefs.getInt("notification_count_today", 138))
    val dailyNotificationCount: StateFlow<Int> = _dailyNotificationCount.asStateFlow()

    val weeklyUsageSummary: List<DayUsageSummary>
        get() = calculateWeeklyUsageSummary()

    val enforcementNotice: String = "NOTE: Android security model restricts launchers from terminating third-party app processes at the OS kernel level without Device Admin or Accessibility service. SHIV enforces mindful friction, launcher-level launch blocking, and focus downtime."

    fun calculateWeeklyUsageSummary(): List<DayUsageSummary> {
        if (!hasUsagePermission()) {
            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            return days.map { DayUsageSummary(it, 0L) }
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return emptyList()
        val now = System.currentTimeMillis()
        val list = mutableListOf<DayUsageSummary>()
        val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val start = dayCal.timeInMillis
            val end = start + (24 * 60 * 60 * 1000L) - 1

            val dayLabel = dayNames[dayCal.get(Calendar.DAY_OF_WEEK) - 1]
            val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, Math.min(end, now)) ?: emptyList()
            val totalMinutes = stats.sumOf { it.totalTimeInForeground } / 60000L
            list.add(DayUsageSummary(dayLabel, totalMinutes))
        }
        return list
    }

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
            modeType = FocusModeType.DEEP_FOCUS,
            durationMinutes = durationMinutes,
            remainingSeconds = durationMinutes * 60,
            isActive = true,
            isPaused = false
        )
    }

    fun startFocusMode(mode: FocusModeType, durationMinutes: Int = mode.defaultMinutes) {
        _focusSession.value = FocusSession(
            title = mode.title,
            modeType = mode,
            durationMinutes = durationMinutes,
            remainingSeconds = durationMinutes * 60,
            isActive = true,
            isPaused = false
        )
    }

    fun pauseFocusSession() {
        val curr = _focusSession.value
        if (curr.isActive) {
            _focusSession.value = curr.copy(isPaused = true)
        }
    }

    fun resumeFocusSession() {
        val curr = _focusSession.value
        if (curr.isActive) {
            _focusSession.value = curr.copy(isPaused = false)
        }
    }

    fun stopFocusSession() {
        val curr = _focusSession.value
        _focusSession.value = curr.copy(isActive = false, isPaused = false)
    }

    fun recordDeviceUnlock() {
        val count = _dailyUnlockCount.value + 1
        _dailyUnlockCount.value = count
        prefs.edit().putInt("unlock_count_today", count).apply()
    }

    fun recordNotificationReceived() {
        val count = _dailyNotificationCount.value + 1
        _dailyNotificationCount.value = count
        prefs.edit().putInt("notification_count_today", count).apply()
    }
}
