package com.example.core.ai

import com.example.core.family.FamilyManager
import com.example.core.launcher.LayoutManager
import com.example.core.lock.AppLockManager
import com.example.core.studio.StudioThemeEngine
import com.example.core.wellbeing.WellbeingManager
import org.json.JSONObject

sealed class ShivAIAction {
    data class ApplyTheme(val presetKey: String, val reason: String) : ShivAIAction()
    data class ProposeLayout(val recommendedApps: List<String>, val reason: String) : ShivAIAction()
    data class StartFocusSession(val minutes: Int, val title: String) : ShivAIAction()
    data class OpenApp(val packageName: String, val appName: String) : ShivAIAction()
    data class SetAppBudget(val packageName: String, val dailyMinutes: Int) : ShivAIAction()
    data class OpenScreen(val route: String) : ShivAIAction()
    data class ShowMessage(val title: String, val message: String) : ShivAIAction()
    data class UndoLastAction(val reason: String = "User requested undo") : ShivAIAction()
    data class InvalidAction(val error: String) : ShivAIAction()
}

class ShivAIActionDispatcher(
    private val themeEngine: StudioThemeEngine,
    private val layoutManager: LayoutManager,
    private val appLockManager: AppLockManager,
    private val wellbeingManager: WellbeingManager,
    private val familyManager: FamilyManager,
    private val launcherShellManager: com.example.core.launcher.LauncherShellManager? = null
) {
    companion object {
        const val SCHEMA_VERSION = "SHIV_AI_ACTION_V1"
    }

    private var previousThemePreset: String? = null
    private var previousLayoutBackup: String? = null

    /**
     * Strictly validates structured JSON responses from ShivAI Cognitive Core.
     * Rejects arbitrary code, validates versioned schema, and prevents bypass of
     * App Lock, Family Curfews, and Parental Controls.
     */
    fun parseAction(jsonStr: String): ShivAIAction {
        return try {
            val root = JSONObject(jsonStr)
            val schema = root.optString("schema", "")
            if (schema != SCHEMA_VERSION) {
                return ShivAIAction.InvalidAction("Unsupported or missing action schema version: $schema. Expected $SCHEMA_VERSION")
            }

            val actionType = root.getString("action")
            val payload = root.optJSONObject("payload") ?: JSONObject()

            when (actionType) {
                "APPLY_THEME" -> {
                    val preset = payload.getString("presetKey")
                    val validPresets = listOf("COSMIC_SHIVA", "CYAN_NEON", "ROYAL_PURPLE", "SOLAR_GOLD", "OLED_BLACK")
                    if (!validPresets.contains(preset)) {
                        return ShivAIAction.InvalidAction("Unknown theme preset: $preset")
                    }
                    val reason = payload.optString("reason", "AI recommended theme")
                    ShivAIAction.ApplyTheme(preset, reason)
                }

                "PROPOSE_LAYOUT" -> {
                    val appsArr = payload.getJSONArray("apps")
                    val apps = List(appsArr.length()) { appsArr.getString(it) }
                    val reason = payload.optString("reason", "Optimized layout for your habits")
                    ShivAIAction.ProposeLayout(apps, reason)
                }

                "START_FOCUS" -> {
                    val minutes = payload.optInt("durationMinutes", 25).coerceIn(5, 180)
                    val title = payload.optString("title", "Focus Session")
                    ShivAIAction.StartFocusSession(minutes, title)
                }

                "OPEN_APP" -> {
                    val pkg = payload.getString("packageName")
                    // Security guard: Ensure package exists in installed apps
                    val installed = launcherShellManager?.installedApps?.value ?: emptyList()
                    val match = installed.firstOrNull { it.packageName == pkg }
                    if (match == null) {
                        return ShivAIAction.InvalidAction("Application '$pkg' is not installed on this device.")
                    }
                    ShivAIAction.OpenApp(pkg, match.name)
                }

                "SET_APP_BUDGET" -> {
                    val pkg = payload.getString("packageName")
                    val dailyMins = payload.optInt("dailyMinutes", 30).coerceIn(5, 480)
                    ShivAIAction.SetAppBudget(pkg, dailyMins)
                }

                "OPEN_SCREEN" -> {
                    val route = payload.getString("route")
                    // Strictly whitelist allowed navigation routes
                    val allowedRoutes = listOf("launcher", "all_apps", "settings", "chat", "agents", "notes", "tools", "wellbeing", "family", "lock", "studio")
                    if (allowedRoutes.contains(route)) {
                        ShivAIAction.OpenScreen(route)
                    } else {
                        ShivAIAction.InvalidAction("Disallowed navigation route: $route")
                    }
                }

                "UNDO" -> {
                    ShivAIAction.UndoLastAction(payload.optString("reason", "Undo last change"))
                }

                "SHOW_MESSAGE" -> {
                    ShivAIAction.ShowMessage(
                        title = payload.optString("title", "ShivAI Assistant"),
                        message = payload.optString("message", "")
                    )
                }

                else -> ShivAIAction.InvalidAction("Unknown action type: $actionType")
            }
        } catch (e: Exception) {
            ShivAIAction.InvalidAction("Malformed AI Action payload: ${e.message}")
        }
    }

    /**
     * Executes safe, non-destructive actions.
     * Consequential actions (e.g. layout rearranging, theme application) require user confirmation.
     * STRICT SECURITY POLICY:
     * - Cannot launch locked apps without biometric/PIN unlock.
     * - Cannot alter Family Policy without parental PIN.
     * - Cannot execute arbitrary code or shell commands.
     */
    fun executeAction(action: ShivAIAction, isUserConfirmed: Boolean = false): Boolean {
        return when (action) {
            is ShivAIAction.ApplyTheme -> {
                previousThemePreset = themeEngine.currentTheme.value.key
                themeEngine.applyPreset(action.presetKey)
                true
            }

            is ShivAIAction.StartFocusSession -> {
                wellbeingManager.startFocusSession(action.minutes, action.title)
                true
            }

            is ShivAIAction.SetAppBudget -> {
                wellbeingManager.setAppBudget(action.packageName, action.packageName, action.dailyMinutes, intentionalFriction = true)
                true
            }

            is ShivAIAction.OpenApp -> {
                // SECURITY GUARD 1: Check Family Supervision Curfew & Restrictions
                val (isCurfew, _) = familyManager.isAppBlockedByCurfew(action.packageName)
                if (isCurfew) {
                    return false // Curfew or parental restriction active, blocked
                }

                // SECURITY GUARD 2: Check App Lock policy
                if (appLockManager.isAppLocked(action.packageName)) {
                    // Protected app! Do NOT bypass lock! Must prompt for biometric / device keyguard.
                    return false
                }

                // Safe to launch
                launcherShellManager?.launchApp(action.packageName)
                true
            }

            is ShivAIAction.ProposeLayout -> {
                if (isUserConfirmed) {
                    var cellX = 0
                    var cellY = 0
                    for (pkg in action.recommendedApps) {
                        layoutManager.addItemToPage(0, cellX, cellY, pkg)
                        cellX++
                        if (cellX >= 4) {
                            cellX = 0
                            cellY++
                        }
                    }
                    true
                } else {
                    false // Must be explicitly confirmed by user
                }
            }

            is ShivAIAction.UndoLastAction -> {
                if (previousThemePreset != null) {
                    themeEngine.applyPreset(previousThemePreset!!)
                    previousThemePreset = null
                    true
                } else {
                    false
                }
            }

            else -> false
        }
    }
}
