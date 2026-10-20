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

enum class LockMethod {
    PIN,
    PATTERN,
    PASSWORD,
    BIOMETRICS
}

data class LockoutState(
    val failedAttempts: Int = 0,
    val isLockedOut: Boolean = false,
    val lockoutRemainingSeconds: Int = 0
)

data class SecurityLogEvent(
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String,
    val packageName: String,
    val details: String
)

class AppLockManager(
    private val context: Context,
    private val appLockDao: AppLockDao,
    private val scope: CoroutineScope
) {
    private val prefs = context.getSharedPreferences("shiv_app_lock_prefs", Context.MODE_PRIVATE)

    val policies: StateFlow<List<AppLockPolicyEntity>> = appLockDao.getAllPoliciesFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val lockedPackages: StateFlow<Set<String>> = appLockDao.getLockedPackagesFlow()
        .map { it.toSet() }
        .stateIn(scope, SharingStarted.Eagerly, emptySet())

    // In-memory session unlocked timestamps: packageName -> unlockTimestamp
    private val activeUnlockedSessions = ConcurrentHashMap<String, Long>()

    // Lock Method Preference
    private val _lockMethod = MutableStateFlow(
        LockMethod.valueOf(prefs.getString("lock_method", LockMethod.PIN.name) ?: LockMethod.PIN.name)
    )
    val lockMethod: StateFlow<LockMethod> = _lockMethod.asStateFlow()

    // Auto-relock timeout in seconds: 0 = Immediately, -1 = On screen lock, 60 = 1 min, 300 = 5 min, 900 = 15 min
    private val _autoRelockTimeoutSeconds = MutableStateFlow(prefs.getInt("auto_relock_timeout", 0))
    val autoRelockTimeoutSeconds: StateFlow<Int> = _autoRelockTimeoutSeconds.asStateFlow()

    // Tactile Vibration & Subtle Animation
    private val _hapticFeedbackEnabled = MutableStateFlow(prefs.getBoolean("haptic_feedback", true))
    val hapticFeedbackEnabled: StateFlow<Boolean> = _hapticFeedbackEnabled.asStateFlow()

    private val _subtleAnimationEnabled = MutableStateFlow(prefs.getBoolean("subtle_animation", true))
    val subtleAnimationEnabled: StateFlow<Boolean> = _subtleAnimationEnabled.asStateFlow()

    // Recents Task Privacy
    private val _preventRecentTasksPreview = MutableStateFlow(prefs.getBoolean("prevent_recents_preview", true))
    val preventRecentTasksPreview: StateFlow<Boolean> = _preventRecentTasksPreview.asStateFlow()

    // Disguise / Fake Crash Mode
    private val _disguiseCrashEnabled = MutableStateFlow(prefs.getBoolean("disguise_crash_enabled", false))
    val disguiseCrashEnabled: StateFlow<Boolean> = _disguiseCrashEnabled.asStateFlow()

    // Failed Attempts & Security Logs
    private val _failedAttempts = MutableStateFlow(0)
    val failedAttempts: StateFlow<Int> = _failedAttempts.asStateFlow()

    private val _lockoutUntilTimestamp = MutableStateFlow(0L)
    val lockoutUntilTimestamp: StateFlow<Long> = _lockoutUntilTimestamp.asStateFlow()

    private val _securityLogs = MutableStateFlow<List<SecurityLogEvent>>(
        listOf(
            SecurityLogEvent(System.currentTimeMillis() - 3600_000, "SYSTEM_ARMED", "com.example", "SHIV Lock active with biometric authentication"),
            SecurityLogEvent(System.currentTimeMillis() - 1800_000, "POLICY_SYNC", "com.android.chrome", "Lock policy confirmed for Browser")
        )
    )
    val securityLogs: StateFlow<List<SecurityLogEvent>> = _securityLogs.asStateFlow()

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
        val timeoutSeconds = if ((policy?.relockTimeoutSeconds ?: 0) != 0) {
            policy?.relockTimeoutSeconds ?: 0
        } else {
            _autoRelockTimeoutSeconds.value
        }

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
        _failedAttempts.value = 0
        scope.launch(Dispatchers.IO) {
            appLockDao.updateLastUnlocked(packageName, now)
        }
    }

    fun recordFailedAttempt(packageName: String) {
        val count = _failedAttempts.value + 1
        _failedAttempts.value = count
        val now = System.currentTimeMillis()

        if (count >= 5) {
            _lockoutUntilTimestamp.value = now + 60_000 // 60s lockout
            logSecurityEvent("LOCKOUT_TRIGGERED", packageName, "5 consecutive failed unlock attempts. 60s cooldown applied.")
        } else if (count >= 3) {
            _lockoutUntilTimestamp.value = now + 30_000 // 30s lockout
            logSecurityEvent("FAILED_ATTEMPT_THRESHOLD", packageName, "3 consecutive failed attempts.")
        } else {
            logSecurityEvent("FAILED_UNLOCK", packageName, "Failed authentication attempt #$count")
        }
    }

    fun isLockedOut(): Boolean {
        return System.currentTimeMillis() < _lockoutUntilTimestamp.value
    }

    fun getRemainingLockoutSeconds(): Int {
        val diff = _lockoutUntilTimestamp.value - System.currentTimeMillis()
        return if (diff > 0) (diff / 1000).toInt() else 0
    }

    private fun logSecurityEvent(type: String, pkg: String, detail: String) {
        val event = SecurityLogEvent(System.currentTimeMillis(), type, pkg, detail)
        _securityLogs.value = listOf(event) + _securityLogs.value.take(49)
    }

    fun setLockMethod(method: LockMethod) {
        _lockMethod.value = method
        prefs.edit().putString("lock_method", method.name).apply()
    }

    fun setAutoRelockTimeout(seconds: Int) {
        _autoRelockTimeoutSeconds.value = seconds
        prefs.edit().putInt("auto_relock_timeout", seconds).apply()
    }

    fun setHapticFeedback(enabled: Boolean) {
        _hapticFeedbackEnabled.value = enabled
        prefs.edit().putBoolean("haptic_feedback", enabled).apply()
    }

    fun setSubtleAnimation(enabled: Boolean) {
        _subtleAnimationEnabled.value = enabled
        prefs.edit().putBoolean("subtle_animation", enabled).apply()
    }

    fun setPreventRecentTasksPreview(enabled: Boolean) {
        _preventRecentTasksPreview.value = enabled
        prefs.edit().putBoolean("prevent_recents_preview", enabled).apply()
    }

    fun setDisguiseCrash(enabled: Boolean) {
        _disguiseCrashEnabled.value = enabled
        prefs.edit().putBoolean("disguise_crash_enabled", enabled).apply()
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
                        relockTimeoutSeconds = _autoRelockTimeoutSeconds.value,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            } else {
                appLockDao.removePolicy(packageName)
                activeUnlockedSessions.remove(packageName)
            }
        }
    }

    fun lockCategory(categoryPackages: List<Pair<String, String>>, lock: Boolean) {
        categoryPackages.forEach { (pkg, name) ->
            toggleAppLock(pkg, name, lock)
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
        if (_autoRelockTimeoutSeconds.value == -1) {
            activeUnlockedSessions.clear()
        }
    }
}
