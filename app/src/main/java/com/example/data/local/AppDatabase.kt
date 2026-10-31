package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*

@Database(
    entities = [
        ChatHistoryEntity::class,
        MemoryEntity::class,
        PendingTaskEntity::class,
        UserPreferencesEntity::class,
        SemanticMemoryEntity::class,
        WorkflowEntity::class,
        WorkflowStepEntity::class,
        LauncherLayoutEntity::class,
        LauncherThemeEntity::class,
        AppLockPolicyEntity::class,
        WellbeingBudgetEntity::class,
        FamilyPolicyEntity::class,
        SyncQueueEntity::class,
        NoteEntity::class,
        CustomWidgetEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatHistoryDao(): ChatHistoryDao
    abstract fun memoryDao(): MemoryDao
    abstract fun pendingTaskDao(): PendingTaskDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun semanticMemoryDao(): SemanticMemoryDao
    abstract fun workflowDao(): WorkflowDao
    abstract fun launcherLayoutDao(): LauncherLayoutDao
    abstract fun launcherThemeDao(): LauncherThemeDao
    abstract fun appLockDao(): AppLockDao
    abstract fun wellbeingDao(): WellbeingDao
    abstract fun familyPolicyDao(): FamilyPolicyDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun noteDao(): NoteDao
    abstract fun customWidgetDao(): CustomWidgetDao
}
