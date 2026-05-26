package com.example.ai.memory.pipeline

import com.example.core.utils.Result

interface EmbeddingProvider {
    /**
     * Generates an embedding vector for the given string.
     */
    suspend fun generateEmbedding(text: String): Result<List<Float>>
    
    /**
     * Generates embedding vectors for a batch of strings.
     */
    suspend fun generateEmbeddings(texts: List<String>): Result<List<List<Float>>>
}
