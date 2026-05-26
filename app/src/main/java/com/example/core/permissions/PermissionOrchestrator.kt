package com.example.core.permissions

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.text.TextUtils
import android.content.ComponentName

enum class AIReadinessState {
    FULL_AI_MODE,
    LIMITED_AI_MODE,
    OFFLINE_AI_MODE,
    RESTRICTED_MODE
}

data class PermissionHealth(
    val hasUsageStats: Boolean = false,
    val hasOverlay: Boolean = false,
    val hasNotificationAccess: Boolean = false,
    val hasAccessibility: Boolean = false,
    val hasRecordAudio: Boolean = false,
    val hasContacts: Boolean = false,
    val hasCalendar: Boolean = false,
    val hasExternalStorage: Boolean = false
)

class PermissionOrchestrator(private val context: Context) {

    private val _readinessState = MutableStateFlow(AIReadinessState.RESTRICTED_MODE)
    val readinessState = _readinessState.asStateFlow()

    private val _permissionHealth = MutableStateFlow(PermissionHealth())
    val permissionHealth = _permissionHealth.asStateFlow()

    fun refreshPermissions() {
        val health = PermissionHealth(
            hasUsageStats = checkUsageStatsPermission(),
            hasOverlay = checkOverlayPermission(),
            hasNotificationAccess = checkNotificationListenerPermission(),
            hasAccessibility = checkAccessibilityPermission(),
            hasRecordAudio = checkPermission(Manifest.permission.RECORD_AUDIO),
            hasContacts = checkPermission(Manifest.permission.READ_CONTACTS),
            hasCalendar = checkPermission(Manifest.permission.READ_CALENDAR),
            hasExternalStorage = checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        )
        
        _permissionHealth.value = health
        
        _readinessState.value = when {
            health.hasUsageStats && health.hasOverlay && health.hasAccessibility && health.hasNotificationAccess && health.hasRecordAudio -> AIReadinessState.FULL_AI_MODE
            health.hasUsageStats && health.hasOverlay -> AIReadinessState.LIMITED_AI_MODE
            else -> AIReadinessState.RESTRICTED_MODE
        }
        
        if (health.hasRecordAudio && health.hasContacts && health.hasCalendar && health.hasExternalStorage) {
            try {
                val intent = Intent(context, ShivAIPersistentService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun checkOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    private fun checkNotificationListenerPermission(): Boolean {
        val componentName = ComponentName(context, "com.example.core.intelligence.NotificationIntelligenceService")
        val enabledListeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return !TextUtils.isEmpty(enabledListeners) && enabledListeners.contains(componentName.flattenToString())
    }

    private fun checkAccessibilityPermission(): Boolean {
        val componentName = ComponentName(context, "com.example.core.accessibility.ShivAIAccessibilityService")
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        
        if (enabledServices.isNullOrEmpty()) return false
        
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            if (componentNameString.equals(componentName.flattenToString(), ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    fun getOverlayIntent(): Intent {
        return Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
    }
    
    fun getUsageStatsIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    }
    
    fun getAccessibilityIntent(): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    }
    
    fun getNotificationListenerIntent(): Intent {
        return Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
    }
}
