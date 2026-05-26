package com.example.ai.memory.pipeline

import com.example.ai.memory.domain.model.SemanticMemory
import kotlin.math.sqrt

class VectorSearchEngine {

    // Fast in-memory index cache
    private val memoryCache = java.util.concurrent.ConcurrentHashMap<String, SemanticMemory>()

    fun updateCache(memories: List<SemanticMemory>) {
        memories.forEach { memory ->
            memoryCache[memory.id] = memory
        }
    }
    
    fun removeFromCache(id: String) {
        memoryCache.remove(id)
    }

    /**
     * Finds the top K memories most similar to the query embedding.
     * Uses optimized Cosine Similarity on the provided memories or cache.
     */
    fun search(
        queryEmbedding: List<Float>,
        memories: List<SemanticMemory>? = null,
        topK: Int = 5,
        minSimilarity: Float = 0.6f
    ): List<SemanticMemory> {
        val targetMemories = memories ?: memoryCache.values.toList()
        
        // Fast float array allocations
        val qArr = queryEmbedding.toFloatArray()
        
        return targetMemories
            .asSequence()
            .filter { it.embedding != null && !it.isArchived }
            .map { memory ->
                val mArr = memory.embedding!!.toFloatArray()
                val similarity = fastCosineSimilarity(qArr, mArr)
                val score = calculateRelevanceScore(similarity, memory)
                memory.copy(relevanceScore = score)
            }
            .filter { it.relevanceScore >= minSimilarity }
            .sortedByDescending { it.relevanceScore }
            .take(topK)
            .toList()
    }

    private fun fastCosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
        if (v1.isEmpty() || v2.isEmpty() || v1.size != v2.size) return 0f

        var dotProduct = 0f
        var norm1 = 0f
        var norm2 = 0f

        for (i in v1.indices) {
            val a = v1[i]
            val b = v2[i]
            dotProduct += a * b
            norm1 += a * a
            norm2 += b * b
        }

        if (norm1 == 0f || norm2 == 0f) return 0f
        return dotProduct / (sqrt(norm1.toDouble()) * sqrt(norm2.toDouble())).toFloat()
    }

    /**
     * Relevance combines semantic similarity, importance, and recency/decay.
     */
    private fun calculateRelevanceScore(similarity: Float, memory: SemanticMemory): Float {
        val importanceBoost = (memory.importanceScore - 1f) * 0.05f 
        val timeSinceAccessed = System.currentTimeMillis() - memory.lastAccessedAt
        val hoursSinceAccessed = timeSinceAccessed / (1000 * 60 * 60).toFloat()
        
        val decayFactor = Math.exp((-memory.decayRate * hoursSinceAccessed).toDouble()).toFloat()

        return (similarity + importanceBoost) * decayFactor
    }
}
