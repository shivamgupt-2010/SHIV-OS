package com.example.core.runtime

import com.example.ai.gemini.GeminiClient
import com.example.ai.gemini.GenerateContentRequest
import com.example.ai.gemini.Content
import com.example.ai.gemini.Part
import com.example.core.utils.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import android.content.Context
import android.os.BatteryManager
import android.content.Intent
import android.content.IntentFilter

enum class RuntimeOptimizationMode {
    MAX_PERFORMANCE,
    BALANCED,
    LOW_POWER
}

class AIRuntimeManager(
    private val context: Context,
    private val geminiClient: GeminiClient
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var streamingJob: Job? = null

    // State
    private val _optimizationMode = MutableStateFlow(RuntimeOptimizationMode.BALANCED)
    val optimizationMode = _optimizationMode.asStateFlow()

    private val _activeAgentsCount = MutableStateFlow(0)
    val activeAgentsCount = _activeAgentsCount.asStateFlow()
    
    private val _batteryLevel = MutableStateFlow(100)
    val batteryLevel = _batteryLevel.asStateFlow()

    private val _streamingResponse = MutableStateFlow("")
    val streamingResponse = _streamingResponse.asStateFlow()

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming = _isStreaming.asStateFlow()

    // Advanced State for Diagnostics
    private val _activeWorkflowsCount = MutableStateFlow(0)
    val activeWorkflowsCount = _activeWorkflowsCount.asStateFlow()

    private val _memoryUsageMb = MutableStateFlow(0)
    val memoryUsageMb = _memoryUsageMb.asStateFlow()

    private val _tokenUsage = MutableStateFlow(0)
    val tokenUsage = _tokenUsage.asStateFlow()

    private val _overlayHealth = MutableStateFlow("Healthy")
    val overlayHealth = _overlayHealth.asStateFlow()
    
    private val _runtimeErrors = MutableStateFlow<List<String>>(emptyList())
    val runtimeErrors = _runtimeErrors.asStateFlow()

    init {
        monitorBattery()
        startMemoryMonitoring()
    }

    private fun startMemoryMonitoring() {
        scope.launch {
            while (true) {
                val runtime = Runtime.getRuntime()
                val usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
                _memoryUsageMb.value = usedMemInMB.toInt()
                
                // Track memory usage over time, avoid explicit GC which blocks thread
                kotlinx.coroutines.delay(10000)
            }
        }
    }
    
    fun recordRuntimeError(error: String) {
        val currentList = _runtimeErrors.value.toMutableList()
        currentList.add(0, error)
        if (currentList.size > 20) currentList.removeLast()
        _runtimeErrors.value = currentList
    }

    fun trackTokenUsage(tokens: Int) {
        _tokenUsage.value += tokens
    }

    private fun monitorBattery() {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        
        if (level != -1 && scale != -1) {
            val pct = (level * 100) / scale.toFloat()
            _batteryLevel.value = pct.toInt()
            updateOptimizationMode(pct.toInt(), batteryStatus)
        }
    }
    
    private fun updateOptimizationMode(pct: Int, batteryStatus: Intent?) {
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL

        _optimizationMode.value = when {
            isCharging -> RuntimeOptimizationMode.MAX_PERFORMANCE
            pct < 20 -> RuntimeOptimizationMode.LOW_POWER
            else -> RuntimeOptimizationMode.BALANCED
        }
    }

    fun startStreamingSession(prompt: String, contextData: String) {
        val systemInstruction = "You are ShivAI, a helpful Android operating agent. Keep answers extremely concise to remain battery efficient."
        val mergedPrompt = "Context: $contextData\nUser: $prompt"

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(Part(text = mergedPrompt))
                )
            ),
            systemInstruction = Content(
                role = "system",
                parts = listOf(Part(text = systemInstruction))
            )
        )

        streamingJob?.cancel()
        _streamingResponse.value = ""
        _isStreaming.value = true

        streamingJob = scope.launch {
            _activeAgentsCount.value += 1
            try {
                geminiClient.generateContentStream("gemini-2.5-flash", request).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _streamingResponse.value += result.data
                        }
                        is Result.Error -> {
                            _streamingResponse.value += "\n[Error: ${result.exception.message}]"
                        }
                        is Result.Loading -> { }
                    }
                }
            } finally {
                _isStreaming.value = false
                _activeAgentsCount.value -= 1
            }
        }
    }

    fun cancelStreaming() {
        streamingJob?.cancel()
        _isStreaming.value = false
    }
}
