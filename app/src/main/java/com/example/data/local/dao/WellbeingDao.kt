package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.WellbeingBudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WellbeingDao {
    @Query("SELECT * FROM wellbeing_budgets ORDER BY appName ASC")
    fun getAllBudgetsFlow(): Flow<List<WellbeingBudgetEntity>>

    @Query("SELECT * FROM wellbeing_budgets WHERE packageName = :packageName LIMIT 1")
    suspend fun getBudget(packageName: String): WellbeingBudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBudget(budget: WellbeingBudgetEntity)

    @Query("DELETE FROM wellbeing_budgets WHERE packageName = :packageName")
    suspend fun deleteBudget(packageName: String)
}
