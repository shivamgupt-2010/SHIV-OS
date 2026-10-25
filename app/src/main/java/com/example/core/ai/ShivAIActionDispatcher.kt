package com.example.core.ai

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.core.family.FamilyManager
import com.example.core.launcher.LayoutManager
import com.example.core.lock.AppLockManager
import com.example.core.studio.StudioThemeEngine
import com.example.core.utils.Logger
import com.example.core.wellbeing.WellbeingManager
import org.json.JSONObject
import java.io.File

sealed class ShivAIAction {
    data class ApplyTheme(val presetKey: String, val reason: String) : ShivAIAction()
    data class ProposeLayout(val recommendedApps: List<String>, val reason: String) : ShivAIAction()
    data class StartFocusSession(val minutes: Int, val title: String) : ShivAIAction()
    data class OpenApp(val packageName: String, val appName: String) : ShivAIAction()
    data class SetAppBudget(val packageName: String, val dailyMinutes: Int) : ShivAIAction()
    data class OpenScreen(val route: String) : ShivAIAction()
    data class ShowMessage(val title: String, val message: String) : ShivAIAction()
    data class UndoLastAction(val reason: String = "User requested undo") : ShivAIAction()
    
    // User requested direct capabilities:
    data class CreateWebsite(val title: String, val filename: String, val htmlContent: String, val summary: String) : ShivAIAction()
    data class GenerateVideo(val prompt: String, val style: String, val durationSec: Int, val summary: String) : ShivAIAction()
    data class InstallApp(val packageName: String, val appName: String, val query: String, val summary: String) : ShivAIAction()
    data class ReplyChat(val platform: String, val recipient: String, val messageText: String, val summary: String) : ShivAIAction()
    data class AutomateTask(val taskType: String, val payloadJson: String, val summary: String) : ShivAIAction()

    data class InvalidAction(val error: String) : ShivAIAction()
}

class ShivAIActionDispatcher(
    private val context: Context,
    private val themeEngine: StudioThemeEngine,
    private val layoutManager: LayoutManager,
    private val appLockManager: AppLockManager,
    private val wellbeingManager: WellbeingManager,
    private val familyManager: FamilyManager,
    private val launcherShellManager: com.example.core.launcher.LauncherShellManager? = null
) {
    companion object {
        const val SCHEMA_VERSION = "SHIV_AI_ACTION_V1"
        private val ACTION_REGEX = Regex("<<<ACTION:(.*?)>>>", RegexOption.DOT_MATCHES_ALL)
    }

    private var previousThemePreset: String? = null

    /**
     * Extracts structured action from model response text and returns clean display text + action.
     */
    fun parseActionFromText(rawText: String): Pair<String, ShivAIAction?> {
        val match = ACTION_REGEX.find(rawText) ?: return Pair(rawText, null)
        val jsonStr = match.groupValues[1].trim()
        val cleanText = rawText.replace(match.value, "").trim()
        val action = parseAction(jsonStr)
        return Pair(cleanText, if (action !is ShivAIAction.InvalidAction) action else null)
    }

    /**
     * Strictly validates structured JSON responses from ShivAI Cognitive Core.
     */
    fun parseAction(jsonStr: String): ShivAIAction {
        return try {
            val root = JSONObject(jsonStr)
            val actionType = root.optString("action", "")
            val payload = root.optJSONObject("payload") ?: root

            when (actionType) {
                "CREATE_WEBSITE" -> {
                    val title = payload.optString("title", "Generated Webpage")
                    val filename = payload.optString("filename", "index.html")
                    val html = payload.optString("html", "<h1>ShivAI Website</h1>")
                    val summary = payload.optString("summary", "Created website: $title")
                    ShivAIAction.CreateWebsite(title, filename, html, summary)
                }

                "GENERATE_VIDEO", "EDIT_VIDEO" -> {
                    val prompt = payload.optString("prompt", "Cinematic drone video")
                    val style = payload.optString("style", "cinematic")
                    val duration = payload.optInt("duration", 5)
                    val summary = payload.optString("summary", "Generate video: $prompt")
                    ShivAIAction.GenerateVideo(prompt, style, duration, summary)
                }

                "INSTALL_APP" -> {
                    val pkg = payload.optString("packageName", "")
                    val appName = payload.optString("appName", "Requested App")
                    val query = payload.optString("query", appName)
                    val summary = payload.optString("summary", "Install $appName from Google Play Store")
                    ShivAIAction.InstallApp(pkg, appName, query, summary)
                }

                "REPLY_CHAT" -> {
                    val platform = payload.optString("platform", "WHATSAPP")
                    val recipient = payload.optString("recipient", "Contact")
                    val text = payload.optString("messageText", "")
                    val summary = payload.optString("summary", "Reply via $platform to $recipient")
                    ShivAIAction.ReplyChat(platform, recipient, text, summary)
                }

                "AUTOMATE_TASK" -> {
                    val taskType = payload.optString("taskType", "SET_THEME")
                    val params = payload.optJSONObject("parameters")?.toString() ?: "{}"
                    val summary = payload.optString("summary", "Automate task: $taskType")
                    ShivAIAction.AutomateTask(taskType, params, summary)
                }

                "APPLY_THEME" -> {
                    val preset = payload.optString("presetKey", "CYAN_NEON")
                    val reason = payload.optString("reason", "AI recommended theme")
                    ShivAIAction.ApplyTheme(preset, reason)
                }

                "PROPOSE_LAYOUT" -> {
                    val appsArr = payload.optJSONArray("apps")
                    val apps = if (appsArr != null) List(appsArr.length()) { appsArr.getString(it) } else emptyList()
                    val reason = payload.optString("reason", "Optimized layout for your habits")
                    ShivAIAction.ProposeLayout(apps, reason)
                }

                "START_FOCUS" -> {
                    val minutes = payload.optInt("durationMinutes", 25).coerceIn(5, 180)
                    val title = payload.optString("title", "Focus Session")
                    ShivAIAction.StartFocusSession(minutes, title)
                }

                "OPEN_APP" -> {
                    val pkg = payload.optString("packageName", "")
                    val installed = launcherShellManager?.installedApps?.value ?: emptyList()
                    val match = installed.firstOrNull { it.packageName == pkg }
                    if (match == null) {
                        ShivAIAction.InvalidAction("Application '$pkg' is not installed.")
                    } else {
                        ShivAIAction.OpenApp(pkg, match.name)
                    }
                }

                "SET_APP_BUDGET" -> {
                    val pkg = payload.optString("packageName", "")
                    val dailyMins = payload.optInt("dailyMinutes", 30).coerceIn(5, 480)
                    ShivAIAction.SetAppBudget(pkg, dailyMins)
                }

                "OPEN_SCREEN" -> {
                    val route = payload.optString("route", "launcher")
                    ShivAIAction.OpenScreen(route)
                }

                "UNDO" -> {
                    ShivAIAction.UndoLastAction(payload.optString("reason", "Undo last change"))
                }

                else -> ShivAIAction.InvalidAction("Unknown action type: $actionType")
            }
        } catch (e: Exception) {
            ShivAIAction.InvalidAction("Malformed AI Action payload: ${e.message}")
        }
    }

    /**
     * Executes approved action with user confirmation.
     */
    fun executeAction(action: ShivAIAction, isUserConfirmed: Boolean = false): Boolean {
        if (!isUserConfirmed && requiresConfirmation(action)) {
            return false
        }

        return try {
            when (action) {
                is ShivAIAction.CreateWebsite -> {
                    // Save HTML to internal app storage or shared downloads
                    val websitesDir = File(context.filesDir, "websites").apply { mkdirs() }
                    val file = File(websitesDir, action.filename.ifBlank { "index.html" })
                    file.writeText(action.htmlContent)
                    
                    // Launch browser / HTML viewer intent
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "text/html")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    val resInfoList = context.packageManager.queryIntentActivities(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
                    for (resolveInfo in resInfoList) {
                        context.grantUriPermission(resolveInfo.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooser = Intent.createChooser(intent, "Preview Website").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(chooser)
                    true
                }

                is ShivAIAction.GenerateVideo -> {
                    // Open Pollinations AI / Video Generator URL in browser
                    val videoUrl = "https://pollinations.ai/p/${Uri.encode(action.prompt)}?width=768&height=768&seed=42"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    true
                }

                is ShivAIAction.InstallApp -> {
                    val intent = if (action.packageName.isNotBlank()) {
                        Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${action.packageName}"))
                    } else {
                        Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=${Uri.encode(action.query)}"))
                    }.apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    true
                }

                is ShivAIAction.ReplyChat -> {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, action.messageText)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        if (action.platform.equals("WHATSAPP", ignoreCase = true)) {
                            setPackage("com.whatsapp")
                        }
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Send reply via...").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                    true
                }

                is ShivAIAction.AutomateTask -> {
                    when (action.taskType.uppercase()) {
                        "SET_THEME" -> {
                            val params = JSONObject(action.payloadJson)
                            val preset = params.optString("presetKey", "CYAN_NEON")
                            themeEngine.applyPreset(preset)
                        }
                        "LOCK_APP" -> {
                            val params = JSONObject(action.payloadJson)
                            val pkg = params.optString("packageName", "")
                            val appName = params.optString("appName", pkg)
                            if (pkg.isNotBlank()) appLockManager.toggleAppLock(pkg, appName, true)
                        }
                    }
                    true
                }

                is ShivAIAction.ApplyTheme -> {
                    previousThemePreset = themeEngine.currentTheme.value.themePreset
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
                    val (isCurfew, _) = familyManager.isAppBlockedByCurfew(action.packageName)
                    if (isCurfew) return false
                    if (appLockManager.isAppLocked(action.packageName)) return false
                    launcherShellManager?.launchApp(action.packageName)
                    true
                }

                is ShivAIAction.ProposeLayout -> {
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
                }

                is ShivAIAction.UndoLastAction -> {
                    if (previousThemePreset != null) {
                        themeEngine.applyPreset(previousThemePreset!!)
                        previousThemePreset = null
                        true
                    } else false
                }

                else -> false
            }
        } catch (e: Exception) {
            Logger.e("Failed to execute action $action", e)
            false
        }
    }

    private fun requiresConfirmation(action: ShivAIAction): Boolean {
        return when (action) {
            is ShivAIAction.CreateWebsite,
            is ShivAIAction.GenerateVideo,
            is ShivAIAction.InstallApp,
            is ShivAIAction.ReplyChat,
            is ShivAIAction.AutomateTask,
            is ShivAIAction.ApplyTheme,
            is ShivAIAction.ProposeLayout -> true
            else -> false
        }
    }
}
