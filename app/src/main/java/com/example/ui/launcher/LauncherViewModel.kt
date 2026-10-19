package com.example.ui.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.family.FamilyManager
import com.example.core.launcher.AdaptiveAppPredictor
import com.example.core.launcher.LauncherShellManager
import com.example.core.launcher.LayoutManager
import com.example.core.lock.AppLockManager
import com.example.core.observability.EventType
import com.example.core.observability.TelemetryManager
import com.example.core.studio.StudioThemeEngine
import com.example.core.wellbeing.WellbeingManager
import com.example.data.local.dao.WorkflowDao
import com.example.data.local.entity.LauncherLayoutEntity
import com.example.data.local.entity.LauncherThemeEntity
import kotlinx.coroutines.flow.*

class LauncherViewModel(
    val launcherShellManager: LauncherShellManager,
    private val adaptiveAppPredictor: AdaptiveAppPredictor,
    private val workflowDao: WorkflowDao,
    private val telemetryManager: TelemetryManager,
    val layoutManager: LayoutManager? = null,
    val studioThemeEngine: StudioThemeEngine? = null,
    val appLockManager: AppLockManager? = null,
    val wellbeingManager: WellbeingManager? = null,
    val familyManager: FamilyManager? = null
) : ViewModel() {

    val installedApps = launcherShellManager.installedApps.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val predictedApps = adaptiveAppPredictor.predictedApps.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val layoutItems: StateFlow<List<LauncherLayoutEntity>> = layoutManager?.allLayoutItems
        ?: MutableStateFlow(emptyList())

    val dockItems: StateFlow<List<LauncherLayoutEntity>> = layoutManager?.dockItems
        ?: MutableStateFlow(emptyList())

    val currentTheme: StateFlow<LauncherThemeEntity> = studioThemeEngine?.currentTheme
        ?: MutableStateFlow(LauncherThemeEntity())

    val activeComplexityMode = layoutManager?.activeComplexityMode
        ?: MutableStateFlow(com.example.core.launcher.ComplexityMode.BALANCED)

    val activeProfile = layoutManager?.activeProfile
        ?: MutableStateFlow(com.example.core.launcher.ProfileType.EVERYDAY)

    // Security & Wellbeing Interception States
    val pendingAppLock = MutableStateFlow<String?>(null)
    val pendingIntentionalPrompt = MutableStateFlow<String?>(null)
    val pendingFamilyRestriction = MutableStateFlow<String?>(null)

    init {
        launcherShellManager.loadInstalledApps()
        launcherShellManager.setLauncherMode(true)
        telemetryManager.trackEvent(EventType.INFO, "LauncherViewModel", "Launcher initialized with upgraded subsystems")
    }

    fun onAppClicked(packageName: String) {
        // 1. Family Supervision Check
        if (familyManager != null && !familyManager.isAppAllowedForChild(packageName)) {
            pendingFamilyRestriction.value = packageName
            return
        }

        // 2. App Lock Check
        if (appLockManager != null && appLockManager.isAppLocked(packageName)) {
            pendingAppLock.value = packageName
            return
        }

        // 3. Intentional Opening Friction Check
        if (wellbeingManager != null && wellbeingManager.requiresIntentionalPrompt(packageName)) {
            pendingIntentionalPrompt.value = packageName
            return
        }

        launchDirectly(packageName)
    }

    fun unlockAndLaunch(packageName: String) {
        appLockManager?.markAppUnlocked(packageName)
        pendingAppLock.value = null
        launchDirectly(packageName)
    }

    fun confirmIntentionalLaunch(packageName: String) {
        pendingIntentionalPrompt.value = null
        launchDirectly(packageName)
    }

    fun dismissPrompts() {
        pendingAppLock.value = null
        pendingIntentionalPrompt.value = null
        pendingFamilyRestriction.value = null
    }

    fun setComplexityMode(mode: com.example.core.launcher.ComplexityMode) {
        layoutManager?.setComplexityMode(mode)
    }

    fun applyProfile(profile: com.example.core.launcher.ProfileType) {
        layoutManager?.applyProfile(profile)
    }

    fun undoLastLayoutChange(): Boolean {
        return layoutManager?.undoLastLayoutChange() ?: false
    }

    fun resetLayoutToDefaults() {
        layoutManager?.resetLayoutToDefaults()
    }

    fun toggleLayoutLock(locked: Boolean) {
        layoutManager?.toggleLayoutLock(locked)
    }

    private fun launchDirectly(packageName: String) {
        telemetryManager.trackEvent(EventType.TOOL_EXECUTION, "Launcher", "Launched App", mapOf("package" to packageName))
        launcherShellManager.launchApp(packageName)
    }
}
