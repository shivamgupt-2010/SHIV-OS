package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_lock_policies")
data class AppLockPolicyEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val isLocked: Boolean = true,
    val biometricAllowed: Boolean = true,
    val relockTimeoutSeconds: Int = 0, // 0 = Immediately upon exit, 60 = 1 minute, -1 = Screen lock
    val lastUnlockedTimestamp: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)
