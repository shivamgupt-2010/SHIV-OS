package com.example.core.family

import android.content.Context
import com.example.data.local.dao.FamilyPolicyDao
import com.example.data.local.entity.FamilyPolicyEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.security.MessageDigest
import java.util.Calendar

class FamilyManager(
    private val context: Context,
    private val familyDao: FamilyPolicyDao,
    private val scope: CoroutineScope
) {
    val policy: StateFlow<FamilyPolicyEntity> = familyDao.getPolicyFlow()
        .map { it ?: FamilyPolicyEntity() }
        .stateIn(scope, SharingStarted.Eagerly, FamilyPolicyEntity())

    init {
        scope.launch(Dispatchers.IO) {
            if (familyDao.getPolicy() == null) {
                familyDao.savePolicy(FamilyPolicyEntity())
            }
        }
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun verifyParentPin(pin: String): Boolean {
        val current = policy.value
        if (current.parentPinHash.isEmpty()) {
            return true // Not set yet
        }
        return hashPin(pin) == current.parentPinHash
    }

    fun setParentPin(newPin: String) {
        scope.launch(Dispatchers.IO) {
            val current = policy.value
            familyDao.savePolicy(current.copy(parentPinHash = hashPin(newPin), updatedAt = System.currentTimeMillis()))
        }
    }

    fun toggleSupervision(enabled: Boolean, parentPin: String): Boolean {
        if (!verifyParentPin(parentPin)) return false

        scope.launch(Dispatchers.IO) {
            val current = policy.value
            familyDao.savePolicy(current.copy(isSupervised = enabled, updatedAt = System.currentTimeMillis()))
        }
        return true
    }

    fun updateCurfew(enabled: Boolean, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        scope.launch(Dispatchers.IO) {
            val current = policy.value
            familyDao.savePolicy(
                current.copy(
                    curfewEnabled = enabled,
                    curfewStartMinute = startHour * 60 + startMinute,
                    curfewEndMinute = endHour * 60 + endMinute,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun setAllowedPackages(packages: List<String>) {
        scope.launch(Dispatchers.IO) {
            val current = policy.value
            val json = JSONArray(packages).toString()
            familyDao.savePolicy(current.copy(allowedPackagesJson = json, updatedAt = System.currentTimeMillis()))
        }
    }

    fun getAllowedPackages(): List<String> {
        val jsonStr = policy.value.allowedPackagesJson
        return try {
            val arr = JSONArray(jsonStr)
            List(arr.length()) { arr.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun isAppAllowedForChild(packageName: String): Boolean {
        val p = policy.value
        if (!p.isSupervised) return true

        // Essential emergency system packages are always permitted
        if (packageName == "com.android.dialer" || packageName == "com.google.android.dialer" ||
            packageName == "com.android.emergency" || packageName == context.packageName) {
            return true
        }

        // Check if currently within active curfew
        if (isCurfewActive()) {
            return false
        }

        val allowed = getAllowedPackages()
        if (allowed.isNotEmpty() && !allowed.contains(packageName)) {
            return false
        }

        return true
    }

    fun isCurfewActive(): Boolean {
        val p = policy.value
        if (!p.isSupervised || !p.curfewEnabled) return false

        // Check temporary parent extension
        if (System.currentTimeMillis() < p.extensionExpiresAt) {
            return false
        }

        val cal = Calendar.getInstance()
        val currentMinuteOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

        val start = p.curfewStartMinute
        val end = p.curfewEndMinute

        return if (start > end) {
            // Curfew spans overnight (e.g. 21:00 to 07:00)
            currentMinuteOfDay >= start || currentMinuteOfDay < end
        } else {
            currentMinuteOfDay in start until end
        }
    }

    fun grantTemporaryExtension(minutes: Int, parentPin: String): Boolean {
        if (!verifyParentPin(parentPin)) return false

        scope.launch(Dispatchers.IO) {
            val expiresAt = System.currentTimeMillis() + (minutes * 60 * 1000L)
            familyDao.applyTemporaryExtension(minutes, expiresAt)
        }
        return true
    }
}
