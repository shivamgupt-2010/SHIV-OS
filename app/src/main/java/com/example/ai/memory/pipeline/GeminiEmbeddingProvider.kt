package com.example.ai.memory.pipeline

import com.example.ai.gemini.GeminiApiService
import com.example.ai.gemini.EmbedContentRequest
import com.example.ai.gemini.BatchEmbedContentsRequest
import com.example.ai.gemini.Content
import com.example.ai.gemini.Part
import com.example.BuildConfig
import com.example.core.utils.Logger
import com.example.core.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiEmbeddingProvider(
    private val apiService: GeminiApiService
) : EmbeddingProvider {

    // Using the recommended model for text embedding
    private val modelId = "text-embedding-004"

    override suspend fun generateEmbedding(text: String): Result<List<Float>> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                 return@withContext Result.Error(IllegalStateException("Gemini API Key missing!"))
            }

            val request = EmbedContentRequest(
                content = Content(parts = listOf(Part(text = text))),
                taskType = "RETRIEVAL_DOCUMENT"
            )

            val response = apiService.embedContent(modelId, apiKey, request)
            Result.Success(response.embedding.values)
        } catch (e: Exception) {
            Logger.e("Gemini Embedding Failed", e)
            Result.Error(e)
        }
    }

    override suspend fun generateEmbeddings(texts: List<String>): Result<List<List<Float>>> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                 return@withContext Result.Error(IllegalStateException("Gemini API Key missing!"))
            }

            val requests = texts.map { text ->
                EmbedContentRequest(
                    content = Content(parts = listOf(Part(text = text))),
                    taskType = "RETRIEVAL_DOCUMENT"
                )
            }

            val batchRequest = BatchEmbedContentsRequest(requests = requests)
            val response = apiService.batchEmbedContents(modelId, apiKey, batchRequest)

            Result.Success(response.embeddings.map { it.values })
        } catch (e: Exception) {
            Logger.e("Gemini Batch Embedding Failed", e)
            Result.Error(e)
        }
    }
}
