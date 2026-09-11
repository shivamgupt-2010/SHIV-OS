package com.example.ai.shivai.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShivAIChatRequest(
    @SerialName("message") val message: String,
    @SerialName("conversation_id") val conversationId: String? = null,
    @SerialName("agent") val agent: String? = null,
    @SerialName("custom_instructions") val customInstructions: String? = null,
    @SerialName("preferred_provider") val preferredProvider: String? = null,
    @SerialName("temperature") val temperature: Double? = 0.7,
    @SerialName("max_tokens") val maxTokens: Int? = 4096,
    @SerialName("user_id") val userId: String? = null
)

@Serializable
data class ShivAIUsage(
    @SerialName("prompt_tokens") val promptTokens: Int? = 0,
    @SerialName("completion_tokens") val completionTokens: Int? = 0,
    @SerialName("total_tokens") val totalTokens: Int? = 0
)

@Serializable
data class ShivAIUnifiedResponse(
    @SerialName("id") val id: String? = null,
    @SerialName("content") val content: String,
    @SerialName("model") val model: String? = null,
    @SerialName("provider") val provider: String? = null,
    @SerialName("finish_reason") val finishReason: String? = null,
    @SerialName("latency_ms") val latencyMs: Double? = null,
    @SerialName("conversation_id") val conversationId: String? = null,
    @SerialName("usage") val usage: ShivAIUsage? = null
)

@Serializable
data class ShivAIStreamChunk(
    @SerialName("delta") val delta: String = "",
    @SerialName("model") val model: String? = null,
    @SerialName("provider") val provider: String? = null,
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class ShivAIMemoryRequest(
    @SerialName("key") val key: String,
    @SerialName("value") val value: String,
    @SerialName("category") val category: String? = "general",
    @SerialName("importance") val importance: Double? = 0.5
)

@Serializable
data class ShivAIMemoryResponse(
    @SerialName("id") val id: String,
    @SerialName("key") val key: String,
    @SerialName("value") val value: String,
    @SerialName("category") val category: String = "general",
    @SerialName("importance") val importance: Double = 0.5,
    @SerialName("confidence") val confidence: Double? = 1.0,
    @SerialName("source") val source: String? = "mobile_launcher",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class ShivAIHealthResponse(
    @SerialName("status") val status: String,
    @SerialName("service") val service: String? = null,
    @SerialName("version") val version: String? = null
)
