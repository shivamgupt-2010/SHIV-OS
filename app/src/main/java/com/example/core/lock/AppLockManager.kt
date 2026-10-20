package com.example.core.lock

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import com.example.data.local.dao.AppLockDao
import com.example.data.local.entity.AppLockPolicyEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class AppLockManager(
    private val context: Context,
    private val appLockDao: AppLockDao,
    private val scope: CoroutineScope
) {
    val policies: StateFlow<List<AppLockPolicyEntity>> = appLockDao.getAllPoliciesFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val lockedPackages: StateFlow<Set<String>> = appLockDao.getLockedPackagesFlow()
        .map { it.toSet() }
        .stateIn(scope, SharingStarted.Eagerly, emptySet())

    // In-memory session unlocked timestamps: packageName -> unlockTimestamp
    private val activeUnlockedSessions = ConcurrentHashMap<String, Long>()

    fun canAuthenticate(): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        val isDeviceSecure = keyguardManager?.isDeviceSecure ?: false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val biometricManager = context.getSystemService(android.hardware.biometrics.BiometricManager::class.java)
            val hasBiometrics = biometricManager?.canAuthenticate(
                android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
            ) == android.hardware.biometrics.BiometricManager.BIOMETRIC_SUCCESS
            return isDeviceSecure || hasBiometrics
        }

        return isDeviceSecure
    }

    fun isAppLocked(packageName: String): Boolean {
        val isPolicyLocked = lockedPackages.value.contains(packageName)
        if (!isPolicyLocked) return false

        // Check if currently within un-lock grace period
        val unlockTime = activeUnlockedSessions[packageName] ?: 0L
        val policy = policies.value.firstOrNull { it.packageName == packageName }
        val timeoutSeconds = policy?.relockTimeoutSeconds ?: 0

        if (timeoutSeconds <= 0) {
            // Re-locks immediately on every launch
            return true
        }

        val elapsed = (System.currentTimeMillis() - unlockTime) / 1000
        return elapsed > timeoutSeconds
    }

    fun markAppUnlocked(packageName: String) {
        val now = System.currentTimeMillis()
        activeUnlockedSessions[packageName] = now
        scope.launch(Dispatchers.IO) {
            appLockDao.updateLastUnlocked(packageName, now)
        }
    }

    fun toggleAppLock(packageName: String, appName: String, isLocked: Boolean) {
        scope.launch(Dispatchers.IO) {
            if (isLocked) {
                appLockDao.savePolicy(
                    AppLockPolicyEntity(
                        packageName = packageName,
                        appName = appName,
                        isLocked = true,
                        biometricAllowed = true,
                        relockTimeoutSeconds = 0,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            } else {
                appLockDao.removePolicy(packageName)
                activeUnlockedSessions.remove(packageName)
            }
        }
    }

    fun setAppLocked(packageName: String, isLocked: Boolean, appName: String = "") {
        toggleAppLock(packageName, appName.ifEmpty { packageName }, isLocked)
    }

    fun setAppLocked(packageName: String, appName: String, isLocked: Boolean) {
        toggleAppLock(packageName, appName, isLocked)
    }

    fun updatePolicyTimeout(packageName: String, timeoutSeconds: Int) {
        scope.launch(Dispatchers.IO) {
            val existing = appLockDao.getPolicyForPackage(packageName)
            if (existing != null) {
                appLockDao.savePolicy(existing.copy(relockTimeoutSeconds = timeoutSeconds, updatedAt = System.currentTimeMillis()))
            }
        }
    }

    fun onScreenTurnedOff() {
        activeUnlockedSessions.clear()
    }
}
