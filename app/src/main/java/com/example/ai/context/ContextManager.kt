package com.example.ai.context

import com.example.ai.memory.domain.repository.SemanticMemoryRepository
import com.example.ai.memory.pipeline.EmbeddingProvider
import com.example.ai.memory.pipeline.VectorSearchEngine
import com.example.ai.prompt.PromptBuilder
import com.example.core.utils.Result
import com.example.core.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContextManager(
    private val memoryRepository: SemanticMemoryRepository,
    private val embeddingProvider: EmbeddingProvider,
    private val vectorSearchEngine: VectorSearchEngine
) {
    @Volatile
    private var isCacheInitialized = false

    /**
     * Retrieves relevant memories using vector search and injects them into the PromptBuilder.
     */
    suspend fun injectContext(query: String, builder: PromptBuilder): PromptBuilder = withContext(Dispatchers.IO) {
        if (!isCacheInitialized) {
            vectorSearchEngine.updateCache(memoryRepository.getAllActiveMemories())
            isCacheInitialized = true
        }

        val queryEmbeddingResult = embeddingProvider.generateEmbedding(query)
        
        if (queryEmbeddingResult is Result.Success) {
            val queryEmbedding = queryEmbeddingResult.data
            
            // Search top K memories against cache
            val relevantMemories = vectorSearchEngine.search(
                queryEmbedding = queryEmbedding,
                topK = 5
            )
            
            // Inject them
            relevantMemories.forEach { memory ->
                builder.injectMemory("Memory (${memory.type.name})", memory.content)
            }
            
            Logger.d("Injected ${relevantMemories.size} memories into context.")
        } else {
            Logger.w("Failed to generate embedding for query: $query")
        }
        
        builder
    }
}
