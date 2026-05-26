package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.local.dao.ChatHistoryDao
import com.example.data.local.dao.MemoryDao
import com.example.data.local.dao.PendingTaskDao
import com.example.data.local.dao.UserPreferencesDao
import com.example.data.local.entity.ChatHistoryEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.PendingTaskEntity
import com.example.data.local.entity.UserPreferencesEntity

@Database(
    entities = [
        ChatHistoryEntity::class,
        MemoryEntity::class,
        PendingTaskEntity::class,
        UserPreferencesEntity::class,
        com.example.data.local.entity.SemanticMemoryEntity::class,
        com.example.data.local.entity.WorkflowEntity::class,
        com.example.data.local.entity.WorkflowStepEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatHistoryDao(): ChatHistoryDao
    abstract fun memoryDao(): MemoryDao
    abstract fun pendingTaskDao(): PendingTaskDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun semanticMemoryDao(): com.example.data.local.dao.SemanticMemoryDao
    abstract fun workflowDao(): com.example.data.local.dao.WorkflowDao
}
