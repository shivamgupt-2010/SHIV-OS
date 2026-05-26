package com.example.ai.workflow.engine

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

class SafetyManager(private val context: Context) {
    
    // Limits
    private var globalExecutionsToday = 0
    private val maxDailyExecutions = 500

    fun canExecute(): Boolean {
        if (globalExecutionsToday > maxDailyExecutions) {
            return false
        }
        
        // Battery Check
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        
        val batteryPct = level * 100 / scale.toFloat()
        
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        
        // Prevent complex AI background tasks if battery is below 15% and not charging
        if (batteryPct < 15 && !isCharging) {
            return false
        }

        return true
    }

    fun recordExecution() {
        globalExecutionsToday++
    }
}
