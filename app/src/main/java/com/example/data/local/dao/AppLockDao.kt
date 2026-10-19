package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.AppLockPolicyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppLockDao {
    @Query("SELECT * FROM app_lock_policies ORDER BY appName ASC")
    fun getAllPoliciesFlow(): Flow<List<AppLockPolicyEntity>>

    @Query("SELECT * FROM app_lock_policies WHERE packageName = :packageName LIMIT 1")
    suspend fun getPolicyForPackage(packageName: String): AppLockPolicyEntity?

    @Query("SELECT packageName FROM app_lock_policies WHERE isLocked = 1")
    fun getLockedPackagesFlow(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePolicy(policy: AppLockPolicyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePolicies(policies: List<AppLockPolicyEntity>)

    @Query("UPDATE app_lock_policies SET lastUnlockedTimestamp = :timestamp WHERE packageName = :packageName")
    suspend fun updateLastUnlocked(packageName: String, timestamp: Long)

    @Query("DELETE FROM app_lock_policies WHERE packageName = :packageName")
    suspend fun removePolicy(packageName: String)
}
