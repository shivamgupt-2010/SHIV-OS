package com.example.ui.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.launcher.AdaptiveAppPredictor
import com.example.core.launcher.LauncherShellManager
import com.example.core.observability.TelemetryManager
import com.example.core.observability.EventType
import com.example.core.intelligence.NotificationIntelligenceService
import com.example.core.intelligence.IntelligentNotification
import com.example.data.local.dao.WorkflowDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.scan

class LauncherViewModel(
    private val launcherShellManager: LauncherShellManager,
    private val adaptiveAppPredictor: AdaptiveAppPredictor,
    private val workflowDao: WorkflowDao,
    private val telemetryManager: TelemetryManager
) : ViewModel() {

    val intelligentNotifications = NotificationIntelligenceService.notificationFlow.scan(emptyList<IntelligentNotification>()) { acc, notif ->
        (listOf(notif) + acc).take(10) // store latest 10
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

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

    val activeWorkflows = workflowDao.getActiveWorkflowsFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    init {
        launcherShellManager.loadInstalledApps()
        launcherShellManager.setLauncherMode(true)
        telemetryManager.trackEvent(EventType.INFO, "LauncherViewModel", "Launcher initialized")
    }

    fun onAppClicked(packageName: String) {
        telemetryManager.trackEvent(EventType.TOOL_EXECUTION, "Launcher", "Launched App", mapOf("package" to packageName))
        launcherShellManager.launchApp(packageName)
    }
    
    fun openSettings() {
        // Change to starting SettingsActivity, handled by the Activity via intent in LauncherActivity instead
        // We'll expose an event/flow for this if sticking strictly to ViewModel, but easier to just use an intent from Activity
        // For now, assume LauncherActivity handles navigation, or change this to trigger a StateFlow
    }
}
