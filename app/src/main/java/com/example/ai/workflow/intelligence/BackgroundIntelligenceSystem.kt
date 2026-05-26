package com.example.ai.workflow.intelligence

import com.example.ai.memory.lifecycle.MemoryLifecycleSystem
import com.example.ai.memory.personalization.AdaptivePersonalizationEngine
import com.example.core.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Coordinates all high-level background intelligence tasks.
 */
class BackgroundIntelligenceSystem(
    private val memoryLifecycleSystem: MemoryLifecycleSystem,
    private val adaptivePersonalizationEngine: AdaptivePersonalizationEngine,
    private val proactiveSuggestionEngine: ProactiveSuggestionEngine
) {

    suspend fun runSweep() = withContext(Dispatchers.IO) {
        Logger.d("Running Background Intelligence Sweep...")
        
        // 1. Maintain memory
        memoryLifecycleSystem.runNightlyMaintenance()
        
        // 2. Generate new insights / routines
        // (In a fuller implementation, prompt gemini to analyze past 24h behavior)
        
        // 3. Prep proactive suggestions for tomorrow
        proactiveSuggestionEngine.generateDailySuggestions()
        
        Logger.d("Sweep Complete.")
    }
}
