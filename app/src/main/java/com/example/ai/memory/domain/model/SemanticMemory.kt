package com.example.ai.memory.domain.model

enum class MemoryType {
    EPISODIC,
    SEMANTIC,
    CONTEXTUAL,
    BEHAVIORAL,
    PREFERENCE,
    GOAL,
    WORKFLOW
}

data class SemanticMemory(
    val id: String,
    val type: MemoryType,
    val content: String,
    val metadata: Map<String, String>,
    val embedding: List<Float>?,
    val relevanceScore: Float = 0f,
    val importanceScore: Float = 1f, // 1 to 5
    val decayRate: Float = 0.01f, // How fast this memory loses relevance if not accessed
    val lastAccessedAt: Long,
    val createdAt: Long,
    val isArchived: Boolean = false
)
