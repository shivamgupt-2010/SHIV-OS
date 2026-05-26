package com.example.ai.memory.lifecycle

import com.example.ai.memory.domain.repository.SemanticMemoryRepository
import com.example.ai.memory.compression.MemoryCompressionEngine
import com.example.core.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MemoryLifecycleSystem(
    private val memoryRepository: SemanticMemoryRepository,
    private val compressionEngine: MemoryCompressionEngine
) {

    /**
     * Executes nightly background maintenance.
     * Pruning old memories, running compression on clustered memories.
     */
    suspend fun runNightlyMaintenance() = withContext(Dispatchers.IO) {
        Logger.d("Starting Memory Nightly Maintenance...")
        val allMemories = memoryRepository.getAllActiveMemories()
        
        // Find memories that haven't been accessed in a while (e.g. 7 days) and are low importance
        val currentTime = System.currentTimeMillis()
        val staleThreshold = 7L * 24L * 60L * 60L * 1000L // 7 days
        
        val staleMemories = allMemories.filter { 
            (currentTime - it.lastAccessedAt) > staleThreshold && it.importanceScore < 3f
        }

        if (staleMemories.isNotEmpty()) {
            Logger.d("Found ${staleMemories.size} stale memories to compress/archive.")
            
            // Chunk them into clusters (Very simple clustering right now -> chunk by 10)
            staleMemories.chunked(10).forEach { cluster ->
                compressionEngine.compressMemoriesCluster(cluster)
            }
        }
        
        Logger.d("Memory Nightly Maintenance Complete.")
    }
}
