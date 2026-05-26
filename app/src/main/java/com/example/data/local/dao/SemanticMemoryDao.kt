package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.SemanticMemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SemanticMemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: SemanticMemoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemories(memories: List<SemanticMemoryEntity>)

    @Update
    suspend fun updateMemory(memory: SemanticMemoryEntity)

    @Query("SELECT * FROM semantic_memory WHERE isArchived = 0")
    suspend fun getAllActiveMemories(): List<SemanticMemoryEntity>

    @Query("SELECT * FROM semantic_memory WHERE id = :id")
    suspend fun getMemoryById(id: String): SemanticMemoryEntity?

    @Query("SELECT * FROM semantic_memory WHERE type = :type AND isArchived = 0")
    suspend fun getMemoriesByType(type: String): List<SemanticMemoryEntity>

    @Query("DELETE FROM semantic_memory WHERE id = :id")
    suspend fun deleteMemory(id: String)
    
    @Query("DELETE FROM semantic_memory WHERE isArchived = 1")
    suspend fun deleteArchivedMemories()

    @Query("DELETE FROM semantic_memory")
    suspend fun deleteAll()
}
