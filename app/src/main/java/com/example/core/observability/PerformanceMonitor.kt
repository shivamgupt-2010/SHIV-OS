package com.example.core.observability

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

/**
 * Monitores system performance: battery, memory, overall load.
 */
class PerformanceMonitor(context: Context, private val telemetryManager: TelemetryManager) {
    private val contextRef = WeakReference(context)

    private val _systemMetrics = MutableStateFlow(SystemMetrics())
    val systemMetrics = _systemMetrics.asStateFlow()

    fun updateMetrics() {
        val ctx = contextRef.get() ?: return
        
        // Memory Usage
        val activityManager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val availableMemMb = memoryInfo.availMem / (1024 * 1024)
        val lowMemory = memoryInfo.lowMemory
        
        // Battery Stats
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            ctx.registerReceiver(null, ifilter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct: Float = if (level != -1 && scale != -1) level * 100 / scale.toFloat() else -1f

        _systemMetrics.value = SystemMetrics(
            availableMemoryMb = availableMemMb,
            isLowMemory = lowMemory,
            batteryPercentage = batteryPct
        )

        telemetryManager.trackEvent(
            EventType.INFO,
            "PerformanceMonitor",
            "System metrics updated - Battery: $batteryPct%, MemAvailable: ${availableMemMb}MB",
            mapOf("battery" to batteryPct, "availableMemoryMb" to availableMemMb)
        )
    }
}

data class SystemMetrics(
    val availableMemoryMb: Long = 0,
    val isLowMemory: Boolean = false,
    val batteryPercentage: Float = -1f,
    val estimatedCpuLoad: Float = 0f // Placeholder for more advanced CPU tracking
)
