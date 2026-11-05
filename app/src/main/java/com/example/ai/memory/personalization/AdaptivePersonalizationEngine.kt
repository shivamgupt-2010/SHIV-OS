package com.example.ai.memory.personalization

import android.content.Context
import com.example.ai.memory.domain.model.MemoryType
import com.example.ai.memory.domain.model.SemanticMemory
import com.example.ai.memory.domain.repository.SemanticMemoryRepository
import com.example.ai.memory.pipeline.EmbeddingProvider
import com.example.core.utils.Result
import com.example.data.local.dao.UserPreferencesDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * AdaptivePersonalizationEngine:
 * High-level bridge connecting semantic vector memory with the on-device
 * SelfEvolvingNeuroplasticityEngine.
 */
class AdaptivePersonalizationEngine(
    private val memoryRepository: SemanticMemoryRepository,
    private val embeddingProvider: EmbeddingProvider,
    private val neuroplasticEngine: SelfEvolvingNeuroplasticityEngine? = null
) {
    /**
     * Learns a behavioral pattern or preference from user interaction.
     */
    suspend fun learnPattern(
        description: String,
        isRoutine: Boolean = false,
        patternKey: String? = null,
        targetAgent: String = "CodingPilotAgent"
    ) = withContext(Dispatchers.IO) {
        val type = if (isRoutine) MemoryType.WORKFLOW else MemoryType.BEHAVIORAL
        
        val embeddingResult = embeddingProvider.generateEmbedding(description)
        val embedding = (embeddingResult as? Result.Success)?.data

        val memory = SemanticMemory(
            id = UUID.randomUUID().toString(),
            type = type,
            content = description,
            metadata = emptyMap(),
            embedding = embedding,
            importanceScore = 4f, // High importance for learned patterns
            lastAccessedAt = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis()
        )
        
        memoryRepository.saveMemory(memory)

        // Reinforce neuroplastic synaptic weight
        if (patternKey != null && neuroplasticEngine != null) {
            val category = if (isRoutine) {
                SelfEvolvingNeuroplasticityEngine.SynapseCategory.TIME_ROUTINE
            } else {
                SelfEvolvingNeuroplasticityEngine.SynapseCategory.INTENT_SHORTCUT
            }
            neuroplasticEngine.reinforcePattern(patternKey, targetAgent, category)
        }
    }

    /**
     * Reports user rejection/dismissal of an assistant suggestion to trigger synaptic unlearning.
     */
    suspend fun reportUserRejection(patternKey: String) {
        neuroplasticEngine?.punishPattern(patternKey)
    }

    /**
     * Retrieves the neuroplastic context prompt snippet for LLM prompts.
     */
    fun getNeuroplasticContextPrompt(): String {
        return neuroplasticEngine?.generateSystemPromptNeuroplasticContext() ?: ""
    }
}
