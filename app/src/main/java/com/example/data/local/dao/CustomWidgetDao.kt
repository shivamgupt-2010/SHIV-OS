package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.CustomWidgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomWidgetDao {
    @Query("SELECT * FROM custom_widget_templates ORDER BY updatedAt DESC")
    fun getAllCustomWidgetsFlow(): Flow<List<CustomWidgetEntity>>

    @Query("SELECT * FROM custom_widget_templates WHERE id = :id LIMIT 1")
    suspend fun getWidgetById(id: String): CustomWidgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWidget(widget: CustomWidgetEntity)

    @Update
    suspend fun updateWidget(widget: CustomWidgetEntity)

    @Query("DELETE FROM custom_widget_templates WHERE id = :id")
    suspend fun deleteWidgetById(id: String)
}
