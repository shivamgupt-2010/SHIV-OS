package com.example.ai.memory.domain.repository

import com.example.ai.memory.domain.model.SemanticMemory
import com.example.ai.memory.security.MemorySecurityProvider
import com.example.data.local.dao.SemanticMemoryDao
import com.example.data.local.entity.SemanticMemoryEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import com.example.ai.memory.domain.model.MemoryType

class SemanticMemoryRepository(
    private val memoryDao: SemanticMemoryDao,
    private val securityProvider: MemorySecurityProvider,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {

    suspend fun saveMemory(memory: SemanticMemory) {
        val entity = memory.toEntity()
        memoryDao.insertMemory(entity)
    }

    suspend fun getMemoryById(id: String): SemanticMemory? {
        return memoryDao.getMemoryById(id)?.toDomain()
    }

    suspend fun getAllActiveMemories(): List<SemanticMemory> {
        return memoryDao.getAllActiveMemories().map { it.toDomain() }
    }
    
    suspend fun getMemoriesByType(type: MemoryType): List<SemanticMemory> {
        return memoryDao.getMemoriesByType(type.name).map { it.toDomain() }
    }
    
    suspend fun deleteMemory(id: String) {
        memoryDao.deleteMemory(id)
    }

    private fun SemanticMemory.toEntity(): SemanticMemoryEntity {
        return SemanticMemoryEntity(
            id = id,
            type = type.name,
            content = securityProvider.encrypt(content), // Encrypted content
            metadataJson = securityProvider.encrypt(json.encodeToString(metadata)), // Encrypted metadata
            embeddingJson = embedding?.let { json.encodeToString(it) } ?: "[]",
            importanceScore = importanceScore,
            decayRate = decayRate,
            lastAccessedAt = lastAccessedAt,
            createdAt = createdAt,
            isArchived = isArchived
        )
    }

    private fun SemanticMemoryEntity.toDomain(): SemanticMemory {
        return SemanticMemory(
            id = id,
            type = enumValueOf<MemoryType>(type),
            content = securityProvider.decrypt(content), // Decrypted content
            metadata = try { json.decodeFromString(securityProvider.decrypt(metadataJson)) } catch (e: Exception) { emptyMap() },
            embedding = try { 
                val list = json.decodeFromString<List<Float>>(embeddingJson) 
                if (list.isEmpty()) null else list
            } catch (e: Exception) { null },
            importanceScore = importanceScore,
            decayRate = decayRate,
            lastAccessedAt = lastAccessedAt,
            createdAt = createdAt,
            isArchived = isArchived
        )
    }
}
