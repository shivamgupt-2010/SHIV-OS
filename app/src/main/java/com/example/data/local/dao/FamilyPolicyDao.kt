package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.FamilyPolicyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyPolicyDao {
    @Query("SELECT * FROM family_policies WHERE id = 'active_family_policy' LIMIT 1")
    fun getPolicyFlow(): Flow<FamilyPolicyEntity?>

    @Query("SELECT * FROM family_policies WHERE id = 'active_family_policy' LIMIT 1")
    suspend fun getPolicy(): FamilyPolicyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePolicy(policy: FamilyPolicyEntity)

    @Query("UPDATE family_policies SET temporaryExtensionMinutes = :minutes, extensionExpiresAt = :expiresAt WHERE id = 'active_family_policy'")
    suspend fun applyTemporaryExtension(minutes: Int, expiresAt: Long)
}
