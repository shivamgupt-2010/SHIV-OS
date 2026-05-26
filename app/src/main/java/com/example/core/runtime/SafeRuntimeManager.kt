package com.example.core.runtime

import com.example.core.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SystemHealthState {
    HEALTHY,
    DEGRADED_BATTERY,
    DEGRADED_MEMORY,
    DEADLOCK_DETECTED,
    CRITICAL
}

/**
 * Monitors and enforces safety limitations on the AI Engine.
 */
class SafeRuntimeManager(private val aiRuntimeManager: AIRuntimeManager) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _healthState = MutableStateFlow(SystemHealthState.HEALTHY)
    val healthState = _healthState.asStateFlow()

    init {
        startMonitoring()
    }

    private fun startMonitoring() {
        scope.launch {
            while (true) {
                delay(5000) // check every 5 seconds
                evaluateHealth()
            }
        }
    }

    private fun evaluateHealth() {
        val battery = aiRuntimeManager.batteryLevel.value
        val memMb = aiRuntimeManager.memoryUsageMb.value
        val workflows = aiRuntimeManager.activeWorkflowsCount.value
        
        var nextState = SystemHealthState.HEALTHY

        if (battery < 15) {
            nextState = SystemHealthState.DEGRADED_BATTERY
            // Optimization handled via internal broadcast receiver in AIRuntimeManager
        }
        
        if (memMb > 500) {
            nextState = SystemHealthState.DEGRADED_MEMORY
            // Mode handled locally or fallback to Safe-mode
        }

        if (workflows > 10) {
            nextState = SystemHealthState.DEADLOCK_DETECTED
            aiRuntimeManager.cancelStreaming()
        }

        if (_healthState.value != nextState) {
            Logger.w("System health transitioned to: \$nextState")
            _healthState.value = nextState
        }
    }
}
