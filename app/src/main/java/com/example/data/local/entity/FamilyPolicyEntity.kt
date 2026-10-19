package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_policies")
data class FamilyPolicyEntity(
    @PrimaryKey
    val id: String = "active_family_policy",
    val childProfileName: String = "Child Profile",
    val isSupervised: Boolean = false,
    val parentPinHash: String = "",
    val curfewEnabled: Boolean = false,
    val curfewStartMinute: Int = 21 * 60, // 9:00 PM
    val curfewEndMinute: Int = 7 * 60,   // 7:00 AM
    val dailyLimitMinutes: Int = 120,    // 2 hours
    val allowedPackagesJson: String = "[]",
    val temporaryExtensionMinutes: Int = 0,
    val extensionExpiresAt: Long = 0L,
    val enrollmentCode: String? = null,
    val syncedWithBackend: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
