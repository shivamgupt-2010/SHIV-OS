package com.example.core.context

import android.app.usage.UsageStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.PowerManager
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

/**
 * Contextual Awareness Engine
 * Detects device state, foreground app, screen state, charging state, connectivity, etc.
 */
class ContextAwarenessManager(context: Context) {
    private val contextRef = WeakReference(context)

    private val _deviceState = MutableStateFlow(DeviceStateContext())
    val deviceState = _deviceState.asStateFlow()

    fun refreshContext() {
        val ctx = contextRef.get() ?: return

        // Connectivity
        val connManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connManager.activeNetwork
        val caps = connManager.getNetworkCapabilities(network)
        val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val isCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        val hasInternet = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        // Battery / Charging
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            ctx.registerReceiver(null, ifilter)
        }
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        // Power Management (Screen state)
        val powerManager = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isScreenOn = powerManager.isInteractive

        // Foreground App (Requires PACKAGE_USAGE_STATS permission, basic best-effort here)
        val foregroundApp = getForegroundAppBestEffort(ctx)

        _deviceState.value = DeviceStateContext(
            isWifiConnected = isWifi,
            isCellularConnected = isCellular,
            hasInternet = hasInternet,
            isCharging = isCharging,
            isScreenOn = isScreenOn,
            currentForegroundAppPackage = foregroundApp
        )
    }

    private fun getForegroundAppBestEffort(ctx: Context): String? {
        // This requires PACKAGE_USAGE_STATS permission to be accurate.
        val usageStatsManager = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time)
        
        var topPackage: String? = null
        var lastTimeUsed = 0L
        if (stats != null) {
            for (usageStats in stats) {
                if (usageStats.lastTimeUsed > lastTimeUsed) {
                    topPackage = usageStats.packageName
                    lastTimeUsed = usageStats.lastTimeUsed
                }
            }
        }
        return topPackage
    }
}

data class DeviceStateContext(
    val isWifiConnected: Boolean = false,
    val isCellularConnected: Boolean = false,
    val hasInternet: Boolean = false,
    val isCharging: Boolean = false,
    val isScreenOn: Boolean = true,
    val currentForegroundAppPackage: String? = null
)
