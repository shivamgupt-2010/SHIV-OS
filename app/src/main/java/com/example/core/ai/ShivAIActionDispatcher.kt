package com.example.core.ai

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.core.accessibility.ShivAIAccessibilityService
import com.example.core.apps.AppActionAdapter
import com.example.core.family.FamilyManager
import com.example.core.files.FileActionAdapter
import com.example.core.launcher.LayoutManager
import com.example.core.lock.AppLockManager
import com.example.core.media.VideoActionAdapter
import com.example.core.studio.StudioThemeEngine
import com.example.core.utils.Logger
import com.example.core.wellbeing.WellbeingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    // Core User Capabilities
    data class CreateWebsite(val title: String, val filename: String, val htmlContent: String, val summary: String) : ShivAIAction()
    data class GenerateVideo(val prompt: String, val style: String, val durationSec: Int, val summary: String) : ShivAIAction()
    data class InstallApp(val packageName: String, val appName: String, val query: String, val summary: String) : ShivAIAction()
    data class InstallLocalApk(val apkPath: String, val summary: String) : ShivAIAction()
    data class ReplyChat(val platform: String, val recipient: String, val messageText: String, val summary: String) : ShivAIAction()
    data class AutomateTask(val taskType: String, val payloadJson: String, val summary: String) : ShivAIAction()
    data class ShareContent(val text: String, val subject: String? = null, val targetPackage: String? = null, val summary: String) : ShivAIAction()
    data class SearchWeb(val query: String, val summary: String) : ShivAIAction()
    data class DeepLink(val uri: String, val summary: String) : ShivAIAction()
    data class MultiStepWorkflow(val title: String, val stepsJson: String, val summary: String) : ShivAIAction()

    // Real On-Device File & UI Actions
    data class CreateFile(val path: String?, val filename: String, val content: String, val summary: String) : ShivAIAction()
    data class ReadFile(val path: String, val summary: String) : ShivAIAction()
    data class ListFiles(val folder: String?, val extension: String?, val summary: String) : ShivAIAction()
    data class DeleteFile(val path: String, val summary: String) : ShivAIAction()
    data class ClickUI(val targetText: String, val exactMatch: Boolean = false, val summary: String) : ShivAIAction()
    data class TypeUI(val textToType: String, val targetField: String? = null, val summary: String) : ShivAIAction()
    data class ScrollUI(val forward: Boolean = true, val summary: String) : ShivAIAction()

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

    private val fileAdapter by lazy { FileActionAdapter(context) }
    private val appAdapter by lazy { AppActionAdapter(context) }
    private val videoAdapter by lazy { VideoActionAdapter(context) }
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var previousThemePreset: String? = null

    /**
     * Extracts structured action from model response text and returns clean display text + action.
     */
    fun parseActionFromText(rawText: String): Pair<String, ShivAIAction?> {
        val match = ACTION_REGEX.find(rawText)
        if (match != null) {
            val jsonStr = match.groupValues[1].trim()
            val cleanText = rawText.replace(match.value, "").trim()
            val action = parseAction(jsonStr)
            if (action !is ShivAIAction.InvalidAction) {
                return Pair(cleanText, action)
            }
        }

        // Smart fallback detection:
        // 1. Detect complete HTML code block intended as a website creation
        if (rawText.contains("```html") && (rawText.contains("<html") || rawText.contains("<!DOCTYPE html>"))) {
            val html = rawText.substringAfter("```html").substringBefore("```").trim()
            if (html.length > 60) {
                val titleMatch = Regex("<title>(.*?)</title>", RegexOption.IGNORE_CASE).find(html)
                val title = titleMatch?.groupValues?.get(1)?.trim() ?: "Generated Webpage"
                return Pair(
                    rawText,
                    ShivAIAction.CreateWebsite(
                        title = title,
                        filename = "index.html",
                        htmlContent = html,
                        summary = "Preview and launch '$title' in web browser."
                    )
                )
            }
        }

        // 2. Detect code blocks with a file header e.g. "Create a file..."
        if (rawText.contains("```") && (rawText.contains("# filename:") || rawText.contains("// filename:") || rawText.contains("/* filename:"))) {
            val headerLine = rawText.lines().firstOrNull { it.contains("filename:", ignoreCase = true) }
            val filename = headerLine?.substringAfter(":")?.trim()?.removeSuffix("*/")?.trim() ?: "code_output.txt"
            val codeBlock = rawText.substringAfter("```").substringAfter("\n").substringBefore("```").trim()
            if (codeBlock.isNotBlank()) {
                return Pair(
                    rawText,
                    ShivAIAction.CreateFile(
                        path = null,
                        filename = filename,
                        content = codeBlock,
                        summary = "Save '$filename' to Documents/ShivAI/"
                    )
                )
            }
        }

        return Pair(rawText, null)
    }

    /**
     * Strictly validates structured JSON responses from ShivAI Cognitive Core.
     */
    fun parseAction(jsonStr: String): ShivAIAction {
        return try {
            val root = JSONObject(jsonStr)
            val actionType = root.optString("action", "").uppercase()
            val payload = root.optJSONObject("payload") ?: root

            when (actionType) {
                // File Operations
                "CREATE_FILE", "WRITE_FILE", "SAVE_FILE" -> {
                    val path = payload.optString("path", null)
                    val filename = payload.optString("filename", "document.txt")
                    val content = payload.optString("content", "")
                    val summary = payload.optString("summary", "Create file: $filename")
                    ShivAIAction.CreateFile(path, filename, content, summary)
                }

                "READ_FILE" -> {
                    val path = payload.optString("path", "")
                    val summary = payload.optString("summary", "Read file: $path")
                    ShivAIAction.ReadFile(path, summary)
                }

                "LIST_FILES" -> {
                    val folder = payload.optString("folder", null)
                    val ext = payload.optString("extension", null)
                    val summary = payload.optString("summary", "List files in $folder")
                    ShivAIAction.ListFiles(folder, ext, summary)
                }

                "DELETE_FILE" -> {
                    val path = payload.optString("path", "")
                    val summary = payload.optString("summary", "Delete file: $path")
                    ShivAIAction.DeleteFile(path, summary)
                }

                // UI Automation Actions
                "CLICK_UI", "TAP_UI" -> {
                    val targetText = payload.optString("targetText", payload.optString("target", ""))
                    val exact = payload.optBoolean("exactMatch", false)
                    val summary = payload.optString("summary", "Tap on '$targetText'")
                    ShivAIAction.ClickUI(targetText, exact, summary)
                }

                "TYPE_UI", "ENTER_TEXT" -> {
                    val text = payload.optString("textToType", payload.optString("text", ""))
                    val targetField = payload.optString("targetField", null)
                    val summary = payload.optString("summary", "Type '$text'")
                    ShivAIAction.TypeUI(text, targetField, summary)
                }

                "SCROLL_UI" -> {
                    val forward = payload.optBoolean("forward", true)
                    val summary = payload.optString("summary", if (forward) "Scroll down" else "Scroll up")
                    ShivAIAction.ScrollUI(forward, summary)
                }

                // App Operations
                "OPEN_APP" -> {
                    val pkg = payload.optString("packageName", "")
                    val appName = payload.optString("appName", pkg)
                    val summary = payload.optString("summary", "Open $appName")
                    ShivAIAction.OpenApp(pkg, appName)
                }

                "INSTALL_APP" -> {
                    val pkg = payload.optString("packageName", "")
                    val appName = payload.optString("appName", "Requested App")
                    val query = payload.optString("query", appName)
                    val summary = payload.optString("summary", "Install $appName from Play Store")
                    ShivAIAction.InstallApp(pkg, appName, query, summary)
                }

                "INSTALL_APK", "INSTALL_LOCAL_APK" -> {
                    val apkPath = payload.optString("apkPath", "")
                    val summary = payload.optString("summary", "Install local package: $apkPath")
                    ShivAIAction.InstallLocalApk(apkPath, summary)
                }

                // Media & Web Creation
                "CREATE_WEBSITE" -> {
                    val title = payload.optString("title", "Generated Webpage")
                    val filename = payload.optString("filename", "index.html")
                    val html = payload.optString("html", "<h1>ShivAI Website</h1>")
                    val summary = payload.optString("summary", "Created website: $title")
                    ShivAIAction.CreateWebsite(title, filename, html, summary)
                }

                "GENERATE_VIDEO", "EDIT_VIDEO" -> {
                    val prompt = payload.optString("prompt", "Cinematic promotional video")
                    val style = payload.optString("style", "cinematic")
                    val duration = payload.optInt("duration", 5)
                    val summary = payload.optString("summary", "Generate video: $prompt")
                    ShivAIAction.GenerateVideo(prompt, style, duration, summary)
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

                "SHARE_CONTENT", "SHARE" -> {
                    val text = payload.optString("text", "")
                    val subject = payload.optString("subject", null)
                    val targetPackage = payload.optString("targetPackage", null)
                    val summary = payload.optString("summary", "Share content via Android Sharesheet")
                    ShivAIAction.ShareContent(text, subject, targetPackage, summary)
                }

                "SEARCH_WEB" -> {
                    val query = payload.optString("query", "")
                    val summary = payload.optString("summary", "Search web for: $query")
                    ShivAIAction.SearchWeb(query, summary)
                }

                "DEEP_LINK" -> {
                    val uri = payload.optString("uri", "")
                    val summary = payload.optString("summary", "Open deep link: $uri")
                    ShivAIAction.DeepLink(uri, summary)
                }

                "MULTI_STEP_WORKFLOW", "PLAN_TASK" -> {
                    val title = payload.optString("title", "Multi-Step Workflow")
                    val stepsArr = payload.optJSONArray("steps")?.toString() ?: "[]"
                    val summary = payload.optString("summary", "Execute multi-step workflow")
                    ShivAIAction.MultiStepWorkflow(title, stepsArr, summary)
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
     * Executes action with user approval check.
     */
    fun executeAction(action: ShivAIAction, isUserConfirmed: Boolean = false): Boolean {
        if (!isUserConfirmed && requiresConfirmation(action)) {
            return false
        }

        return try {
            when (action) {
                // 1. Real File Creation
                is ShivAIAction.CreateFile -> {
                    val result = fileAdapter.createFile(action.path, action.filename, action.content)
                    if (result.success) {
                        Toast.makeText(context, "Saved: ${result.file.name}", Toast.LENGTH_SHORT).show()
                        fileAdapter.openFile(result.file)
                    }
                    result.success
                }

                // 2. Real File Reading
                is ShivAIAction.ReadFile -> {
                    val content = fileAdapter.readFile(action.path)
                    Toast.makeText(context, "Read ${action.path} (${content.length} chars)", Toast.LENGTH_SHORT).show()
                    true
                }

                // 3. List Files
                is ShivAIAction.ListFiles -> {
                    val items = fileAdapter.listFiles(action.folder, action.extension)
                    Toast.makeText(context, "Found ${items.size} files", Toast.LENGTH_SHORT).show()
                    true
                }

                // 4. Delete File (Sensitive)
                is ShivAIAction.DeleteFile -> {
                    val deleted = fileAdapter.deleteFile(action.path)
                    Toast.makeText(context, if (deleted) "Deleted ${action.path}" else "File not found", Toast.LENGTH_SHORT).show()
                    deleted
                }

                // 5. UI Interaction: Click
                is ShivAIAction.ClickUI -> {
                    if (!ShivAIAccessibilityService.isServiceEnabled(context)) {
                        Toast.makeText(context, "Please enable ShivAI in Settings > Accessibility", Toast.LENGTH_LONG).show()
                        ShivAIAccessibilityService.promptEnableAccessibility(context)
                        return false
                    }
                    val clicked = ShivAIAccessibilityService.instance?.clickElementByText(action.targetText, action.exactMatch) ?: false
                    Toast.makeText(context, if (clicked) "Clicked: ${action.targetText}" else "Target not visible", Toast.LENGTH_SHORT).show()
                    clicked
                }

                // 6. UI Interaction: Type Text
                is ShivAIAction.TypeUI -> {
                    if (!ShivAIAccessibilityService.isServiceEnabled(context)) {
                        Toast.makeText(context, "Please enable ShivAI in Settings > Accessibility", Toast.LENGTH_LONG).show()
                        ShivAIAccessibilityService.promptEnableAccessibility(context)
                        return false
                    }
                    val entered = ShivAIAccessibilityService.instance?.enterText(action.textToType, action.targetField) ?: false
                    Toast.makeText(context, if (entered) "Typed: ${action.textToType}" else "No editable field found", Toast.LENGTH_SHORT).show()
                    entered
                }

                // 7. UI Interaction: Scroll
                is ShivAIAction.ScrollUI -> {
                    if (!ShivAIAccessibilityService.isServiceEnabled(context)) {
                        Toast.makeText(context, "Please enable ShivAI in Settings > Accessibility", Toast.LENGTH_LONG).show()
                        ShivAIAccessibilityService.promptEnableAccessibility(context)
                        return false
                    }
                    ShivAIAccessibilityService.instance?.performScroll(action.forward) ?: false
                }

                // 8. Open App
                is ShivAIAction.OpenApp -> {
                    val (isCurfew, _) = familyManager.isAppBlockedByCurfew(action.packageName)
                    if (isCurfew) return false
                    if (appLockManager.isAppLocked(action.packageName)) return false
                    val opened = appAdapter.openApp(action.appName.ifBlank { action.packageName })
                    if (!opened) {
                        launcherShellManager?.launchApp(action.packageName)
                    }
                    true
                }

                // 9. Install App from Store
                is ShivAIAction.InstallApp -> {
                    appAdapter.installFromPlayStore(action.appName.ifBlank { action.query }, action.packageName)
                }

                // 10. Install Local APK
                is ShivAIAction.InstallLocalApk -> {
                    appAdapter.installLocalApk(action.apkPath)
                }

                // 11. Create Website & Open in Browser
                is ShivAIAction.CreateWebsite -> {
                    val fileResult = fileAdapter.createFile(null, action.filename.ifBlank { "index.html" }, action.htmlContent)
                    if (fileResult.success) {
                        fileAdapter.openFile(fileResult.file)
                    }
                    fileResult.success
                }

                // 12. Video Generation (Hardware MediaCodec export + Pollinations AI fallback)
                is ShivAIAction.GenerateVideo -> {
                    coroutineScope.launch {
                        val videoResult = videoAdapter.generatePromoVideo(action.prompt, "SHIV-OS Automation")
                        if (videoResult.success) {
                            Toast.makeText(context, "Exported: ${videoResult.videoFile.name}", Toast.LENGTH_LONG).show()
                            videoAdapter.playVideo(videoResult.videoFile)
                        } else {
                            // Fallback to online generator
                            val videoUrl = "https://pollinations.ai/p/${Uri.encode(action.prompt)}?width=768&height=768&seed=42"
                            appAdapter.openDeepLink(videoUrl)
                        }
                    }
                    true
                }

                // 13. Reply Chat (WhatsApp/SMS)
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

                // 14. Automate Launcher Tasks
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

                // 15. Apply Theme
                is ShivAIAction.ApplyTheme -> {
                    previousThemePreset = themeEngine.currentTheme.value.themePreset
                    themeEngine.applyPreset(action.presetKey)
                    true
                }

                // 16. Start Focus Session
                is ShivAIAction.StartFocusSession -> {
                    wellbeingManager.startFocusSession(action.minutes, action.title)
                    true
                }

                // 17. Set App Budget
                is ShivAIAction.SetAppBudget -> {
                    wellbeingManager.setAppBudget(action.packageName, action.packageName, action.dailyMinutes, intentionalFriction = true)
                    true
                }

                // 18. Share Content
                is ShivAIAction.ShareContent -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, action.text)
                        if (!action.subject.isNullOrBlank()) putExtra(Intent.EXTRA_SUBJECT, action.subject)
                        if (!action.targetPackage.isNullOrBlank()) setPackage(action.targetPackage)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share via...").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                    true
                }

                // 19. Search Web
                is ShivAIAction.SearchWeb -> {
                    appAdapter.openDeepLink("https://www.google.com/search?q=${Uri.encode(action.query)}")
                }

                // 20. Deep Link
                is ShivAIAction.DeepLink -> {
                    appAdapter.openDeepLink(action.uri)
                }

                // 21. Multi-Step Workflow
                is ShivAIAction.MultiStepWorkflow -> {
                    Toast.makeText(context, "Executing Workflow: ${action.title}", Toast.LENGTH_LONG).show()
                    true
                }

                // 22. Undo
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

    /**
     * Determines whether an action requires explicit user confirmation before executing.
     * Safe actions (creating files, reading files, opening apps, searching web) execute immediately.
     * Sensitive/destructive actions (deleting files, installing apps, UI clicks, typing) require confirmation.
     */
    fun requiresConfirmation(action: ShivAIAction): Boolean {
        return when (action) {
            is ShivAIAction.DeleteFile,
            is ShivAIAction.InstallApp,
            is ShivAIAction.InstallLocalApk,
            is ShivAIAction.ClickUI,
            is ShivAIAction.TypeUI,
            is ShivAIAction.ReplyChat,
            is ShivAIAction.ApplyTheme,
            is ShivAIAction.AutomateTask -> true

            // Safe actions: run immediately!
            is ShivAIAction.CreateFile,
            is ShivAIAction.ReadFile,
            is ShivAIAction.ListFiles,
            is ShivAIAction.OpenApp,
            is ShivAIAction.CreateWebsite,
            is ShivAIAction.GenerateVideo,
            is ShivAIAction.SearchWeb,
            is ShivAIAction.DeepLink,
            is ShivAIAction.ScrollUI,
            is ShivAIAction.StartFocusSession,
            is ShivAIAction.ShareContent,
            is ShivAIAction.MultiStepWorkflow,
            is ShivAIAction.UndoLastAction -> false

            else -> false
        }
    }
}
