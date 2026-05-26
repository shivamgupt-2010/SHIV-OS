package com.example.ai.memory.compression

import com.example.ai.gemini.GenerateContentRequest
import com.example.ai.gemini.GenerateContentResponse
import com.example.ai.gemini.GeminiClient
import com.example.ai.gemini.Content
import com.example.ai.gemini.Part
import com.example.ai.memory.domain.model.MemoryType
import com.example.ai.memory.domain.model.SemanticMemory
import com.example.ai.memory.domain.repository.SemanticMemoryRepository
import com.example.ai.memory.pipeline.EmbeddingProvider
import com.example.core.utils.Logger
import com.example.core.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class MemoryCompressionEngine(
    private val geminiClient: GeminiClient,
    private val embeddingProvider: EmbeddingProvider,
    private val memoryRepository: SemanticMemoryRepository
) {
    // We use a cheaper model for async background compression
    private val compressionModel = "gemini-3.5-flash"

    /**
     * Summarizes older or low-importance memories into a compressed insight,
     * deletes the original granular memories, and saves the new insight.
     */
    suspend fun compressMemoriesCluster(memoriesToCompress: List<SemanticMemory>) = withContext(Dispatchers.IO) {
        if (memoriesToCompress.isEmpty()) return@withContext

        val combinedContent = memoriesToCompress.joinToString("\n---\n") { it.content }
        val prompt = "Analyze the following memories and compress them into a highly concise, information-dense summary that retains all semantic value and actionable insights. Do not lose key facts. \n\nMemories:\n$combinedContent"
        
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = "You are a highly efficient memory compression engine. Maintain facts while drastically reducing token count.")))
        )
        
        when (val response = geminiClient.generateContent(compressionModel, request)) {
            is Result.Success -> {
                val summary = response.data.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (summary != null) {
                    // Generate embedding for summary
                    val embeddingResult = embeddingProvider.generateEmbedding(summary)
                    val embedding = (embeddingResult as? Result.Success)?.data

                    val compressedMemory = SemanticMemory(
                        id = UUID.randomUUID().toString(),
                        type = MemoryType.SEMANTIC, // Upgraded to semantic factual memory
                        content = "Compressed Insight:\n$summary",
                        metadata = mapOf("compressed_from_count" to memoriesToCompress.size.toString()),
                        embedding = embedding,
                        lastAccessedAt = System.currentTimeMillis(),
                        createdAt = System.currentTimeMillis()
                    )

                    // Archive / Delete old
                    memoriesToCompress.forEach { memoryRepository.deleteMemory(it.id) }
                    
                    // Save new
                    memoryRepository.saveMemory(compressedMemory)
                    Logger.d("Successfully compressed ${memoriesToCompress.size} memories into 1.")
                }
            }
            is Result.Error -> {
                Logger.e("Memory compression failed", response.exception)
            }
            else -> {}
        }
    }
}
