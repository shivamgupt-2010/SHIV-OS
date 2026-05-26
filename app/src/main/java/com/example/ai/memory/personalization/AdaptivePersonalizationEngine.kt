package com.example.ai.memory.personalization

import com.example.ai.memory.domain.model.MemoryType
import com.example.ai.memory.domain.model.SemanticMemory
import com.example.ai.memory.domain.repository.SemanticMemoryRepository
import com.example.ai.memory.pipeline.EmbeddingProvider
import com.example.core.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class AdaptivePersonalizationEngine(
    private val memoryRepository: SemanticMemoryRepository,
    private val embeddingProvider: EmbeddingProvider
) {
    /**
     * Learns a behavioral pattern or preference from user interaction.
     */
    suspend fun learnPattern(description: String, isRoutine: Boolean = false) = withContext(Dispatchers.IO) {
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
    }
}
