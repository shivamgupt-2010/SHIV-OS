package com.example.core.launcher

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * Manages launcher mode switching, safe fallback, and installed app querying.
 */
class LauncherShellManager(context: Context) {
    private val contextRef = WeakReference(context)

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps = _installedApps.asStateFlow()

    private val _isLauncherModeActive = MutableStateFlow(false)
    val isLauncherModeActive = _isLauncherModeActive.asStateFlow()

    fun loadInstalledApps() {
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val ctx = contextRef.get() ?: return@launch
            val pm = ctx.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(intent, 0)
            
            val apps = resolveInfos.mapNotNull { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                if (packageName == ctx.packageName) return@mapNotNull null
                
                AppInfo(
                    packageName = packageName,
                    name = resolveInfo.loadLabel(pm).toString(),
                    launchActivity = resolveInfo.activityInfo.name
                )
            }.distinctBy { it.packageName }.sortedBy { it.name }
            
            _installedApps.value = apps
        }
    }

    fun setLauncherMode(active: Boolean) {
        _isLauncherModeActive.value = active
    }

    fun launchApp(packageName: String) {
        val ctx = contextRef.get() ?: return
        val launchIntent = ctx.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(launchIntent)
        }
    }
    
    // Emergency fallback to standard settings
    fun openAndroidSettings() {
        val ctx = contextRef.get() ?: return
        val intent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(intent)
    }
}

data class AppInfo(
    val packageName: String,
    val name: String,
    val launchActivity: String
)
