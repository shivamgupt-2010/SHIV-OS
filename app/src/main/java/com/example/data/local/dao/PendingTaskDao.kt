package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.PendingTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingTaskDao {
    @Query("SELECT * FROM pending_task WHERE status = 'pending' ORDER BY priority DESC, timestamp ASC")
    fun getPendingTasks(): Flow<List<PendingTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: PendingTaskEntity)

    @Query("UPDATE pending_task SET status = :status WHERE id = :id")
    suspend fun updateTaskStatus(id: String, status: String)

    @Query("DELETE FROM pending_task WHERE id = :id")
    suspend fun deleteTask(id: String)
}
