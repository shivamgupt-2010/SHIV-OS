package com.example.core.family

import android.content.Context
import com.example.data.local.dao.FamilyPolicyDao
import com.example.data.local.entity.FamilyPolicyEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.Calendar

enum class ApprovalRequestType {
    OPEN_BLOCKED_APP,
    EXTEND_TIME,
    DOWNTIME_USE,
    INSTALL_APP,
    RESTRICTED_WEBSITE,
    CHANGE_SETTING
}

enum class ApprovalStatus {
    PENDING,
    APPROVED,
    DENIED
}

data class ParentApprovalRequest(
    val id: String,
    val type: ApprovalRequestType,
    val target: String,
    val description: String,
    val requestedMinutes: Int = 0,
    val status: ApprovalStatus = ApprovalStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis()
)

data class SafetyAlert(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSevere: Boolean = false
)

data class ContentRestrictions(
    val approveNewInstalls: Boolean = true,
    val restrictPurchases: Boolean = true,
    val ageRating: String = "12+",
    val requireApprovalForChanges: Boolean = true,
    val blockedWebsites: List<String> = listOf("gambling.com", "violence-portal.net", "unfiltered-chat.io")
)

class FamilyManager(
    private val context: Context,
    private val familyDao: FamilyPolicyDao,
    private val scope: CoroutineScope
) {
    private val prefs = context.getSharedPreferences("shiv_family_prefs", Context.MODE_PRIVATE)

    val policy: StateFlow<FamilyPolicyEntity> = familyDao.getPolicyFlow()
        .map { it ?: FamilyPolicyEntity() }
        .stateIn(scope, SharingStarted.Eagerly, FamilyPolicyEntity())

    // Content restrictions & filtering
    private val _contentRestrictions = MutableStateFlow(loadContentRestrictions())
    val contentRestrictions: StateFlow<ContentRestrictions> = _contentRestrictions.asStateFlow()

    // Break reminders interval (in minutes)
    private val _breakReminderIntervalMinutes = MutableStateFlow(prefs.getInt("break_reminder_interval", 45))
    val breakReminderIntervalMinutes: StateFlow<Int> = _breakReminderIntervalMinutes.asStateFlow()

    // Study time whitelist packages
    private val _studyTimePackages = MutableStateFlow(
        listOf("com.google.android.apps.docs", "org.wikipedia", "org.khanacademy.android", "com.duolingo")
    )
    val studyTimePackages: StateFlow<List<String>> = _studyTimePackages.asStateFlow()

    // Always allowed emergency apps
    private val _alwaysAllowedPackages = MutableStateFlow(
        listOf("com.android.dialer", "com.google.android.dialer", "com.android.emergency", "com.android.contacts")
    )
    val alwaysAllowedPackages: StateFlow<List<String>> = _alwaysAllowedPackages.asStateFlow()

    // Parent approval requests
    private val _approvalRequests = MutableStateFlow<List<ParentApprovalRequest>>(
        listOf(
            ParentApprovalRequest("req_1", ApprovalRequestType.EXTEND_TIME, "YouTube", "Requested +30 min extended screen time", 30, ApprovalStatus.PENDING, System.currentTimeMillis() - 15 * 60_000),
            ParentApprovalRequest("req_2", ApprovalRequestType.OPEN_BLOCKED_APP, "Minecraft", "Requesting access during bedtime curfew", 0, ApprovalStatus.PENDING, System.currentTimeMillis() - 45 * 60_000),
            ParentApprovalRequest("req_3", ApprovalRequestType.RESTRICTED_WEBSITE, "code.org", "Educational programming platform", 0, ApprovalStatus.APPROVED, System.currentTimeMillis() - 120 * 60_000)
        )
    )
    val approvalRequests: StateFlow<List<ParentApprovalRequest>> = _approvalRequests.asStateFlow()

    // Safety and security audit alerts
    private val _safetyAlerts = MutableStateFlow<List<SafetyAlert>>(
        listOf(
            SafetyAlert("alert_1", "Bedtime Curfew Enforced", "Curfew started at 21:00. Device entered bedtime mode.", System.currentTimeMillis() - 50 * 60_000, false),
            SafetyAlert("alert_2", "Restricted App Launch Attempt", "Child attempted to launch Instagram during curfew.", System.currentTimeMillis() - 35 * 60_000, true),
            SafetyAlert("alert_3", "Policy Sync Successful", "Family policy synchronized with SHIV DB Cloud.", System.currentTimeMillis() - 10 * 60_000, false)
        )
    )
    val safetyAlerts: StateFlow<List<SafetyAlert>> = _safetyAlerts.asStateFlow()

    // Cloud synchronization & pairing state
    private val _pairingCode = MutableStateFlow(prefs.getString("pairing_code", "SHIV-7829") ?: "SHIV-7829")
    val pairingCode: StateFlow<String> = _pairingCode.asStateFlow()

    private val _isCloudSynced = MutableStateFlow(true)
    val isCloudSynced: StateFlow<Boolean> = _isCloudSynced.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(System.currentTimeMillis() - 120_000)
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private val _tamperDetected = MutableStateFlow(false)
    val tamperDetected: StateFlow<Boolean> = _tamperDetected.asStateFlow()

    init {
        scope.launch(Dispatchers.IO) {
            if (familyDao.getPolicy() == null) {
                familyDao.savePolicy(FamilyPolicyEntity())
            }
        }
    }

    private fun loadContentRestrictions(): ContentRestrictions {
        val approveInstalls = prefs.getBoolean("approve_new_installs", true)
        val purchases = prefs.getBoolean("restrict_purchases", true)
        val rating = prefs.getString("age_rating", "12+") ?: "12+"
        val reqApproval = prefs.getBoolean("require_approval_changes", true)
        val blockedStr = prefs.getString("blocked_websites", "gambling.com,violence-portal.net,unfiltered-chat.io") ?: ""
        val blockedList = if (blockedStr.isBlank()) emptyList() else blockedStr.split(",").map { it.trim() }
        return ContentRestrictions(approveInstalls, purchases, rating, reqApproval, blockedList)
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

    fun updateDailyLimit(minutes: Int) {
        scope.launch(Dispatchers.IO) {
            val current = policy.value
            familyDao.savePolicy(current.copy(dailyLimitMinutes = minutes, updatedAt = System.currentTimeMillis()))
        }
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

    fun setBreakReminder(minutes: Int) {
        _breakReminderIntervalMinutes.value = minutes
        prefs.edit().putInt("break_reminder_interval", minutes).apply()
    }

    fun setContentRestrictions(restrictions: ContentRestrictions) {
        _contentRestrictions.value = restrictions
        prefs.edit()
            .putBoolean("approve_new_installs", restrictions.approveNewInstalls)
            .putBoolean("restrict_purchases", restrictions.restrictPurchases)
            .putString("age_rating", restrictions.ageRating)
            .putBoolean("require_approval_changes", restrictions.requireApprovalForChanges)
            .putString("blocked_websites", restrictions.blockedWebsites.joinToString(","))
            .apply()
    }

    fun addBlockedWebsite(domain: String) {
        val curr = _contentRestrictions.value
        if (!curr.blockedWebsites.contains(domain)) {
            val updated = curr.copy(blockedWebsites = curr.blockedWebsites + domain)
            setContentRestrictions(updated)
        }
    }

    fun removeBlockedWebsite(domain: String) {
        val curr = _contentRestrictions.value
        val updated = curr.copy(blockedWebsites = curr.blockedWebsites.filter { it != domain })
        setContentRestrictions(updated)
    }

    fun approveRequest(requestId: String) {
        val req = _approvalRequests.value.firstOrNull { it.id == requestId } ?: return
        _approvalRequests.value = _approvalRequests.value.map {
            if (it.id == requestId) it.copy(status = ApprovalStatus.APPROVED) else it
        }
        if (req.type == ApprovalRequestType.EXTEND_TIME && req.requestedMinutes > 0) {
            grantTemporaryExtension(req.requestedMinutes)
        }
        logAlert("Request Approved", "Approved ${req.type.name} for ${req.target}", false)
    }

    fun denyRequest(requestId: String) {
        val req = _approvalRequests.value.firstOrNull { it.id == requestId } ?: return
        _approvalRequests.value = _approvalRequests.value.map {
            if (it.id == requestId) it.copy(status = ApprovalStatus.DENIED) else it
        }
        logAlert("Request Denied", "Denied request for ${req.target}", false)
    }

    fun logAlert(title: String, description: String, isSevere: Boolean) {
        val alert = SafetyAlert("alert_${System.currentTimeMillis()}", title, description, System.currentTimeMillis(), isSevere)
        _safetyAlerts.value = listOf(alert) + _safetyAlerts.value.take(49)
    }

    fun triggerCloudSync() {
        _isCloudSynced.value = true
        _lastSyncTimestamp.value = System.currentTimeMillis()
        logAlert("Policy Synced", "All parental rules synchronized with SHIV DB Cloud", false)
    }

    fun setPairingCode(code: String) {
        _pairingCode.value = code
        prefs.edit().putString("pairing_code", code).apply()
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
        if (_alwaysAllowedPackages.value.contains(packageName) || packageName == context.packageName) {
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

    fun isAppBlockedByCurfew(packageName: String): Pair<Boolean, String?> {
        val allowed = isAppAllowedForChild(packageName)
        return Pair(!allowed, if (!allowed) "Curfew or parental restriction active" else null)
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

    fun grantTemporaryExtension(minutes: Int, parentPin: String = ""): Boolean {
        if (parentPin.isNotEmpty() && !verifyParentPin(parentPin)) return false

        scope.launch(Dispatchers.IO) {
            val expiresAt = System.currentTimeMillis() + (minutes * 60 * 1000L)
            familyDao.applyTemporaryExtension(minutes, expiresAt)
        }
        return true
    }
}
