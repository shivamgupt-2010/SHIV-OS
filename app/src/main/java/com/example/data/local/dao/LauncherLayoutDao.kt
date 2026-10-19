package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.LauncherLayoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LauncherLayoutDao {
    @Query("SELECT * FROM launcher_layouts ORDER BY pageIndex ASC, cellY ASC, cellX ASC")
    fun getAllLayoutItemsFlow(): Flow<List<LauncherLayoutEntity>>

    @Query("SELECT * FROM launcher_layouts WHERE pageIndex = :pageIndex ORDER BY cellY ASC, cellX ASC")
    fun getItemsForPageFlow(pageIndex: Int): Flow<List<LauncherLayoutEntity>>

    @Query("SELECT * FROM launcher_layouts WHERE itemType = 'DOCK' ORDER BY cellX ASC")
    fun getDockItemsFlow(): Flow<List<LauncherLayoutEntity>>

    @Query("SELECT * FROM launcher_layouts WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: String): LauncherLayoutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: LauncherLayoutEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<LauncherLayoutEntity>)

    @Update
    suspend fun updateItem(item: LauncherLayoutEntity)

    @Query("DELETE FROM launcher_layouts WHERE id = :id")
    suspend fun deleteItemById(id: String)

    @Query("DELETE FROM launcher_layouts")
    suspend fun clearAllLayouts()
}
