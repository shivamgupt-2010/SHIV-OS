package com.example.core.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.core.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * ShivAIAccessibilityService:
 * Official Accessibility Engine for SHIV-OS.
 * Enables ShivAI to interact with visible UI (click buttons, enter text, scroll, tap coordinates)
 * only upon user request and with explicit user authorization.
 */
class ShivAIAccessibilityService : AccessibilityService() {

    companion object {
        var instance: ShivAIAccessibilityService? = null

        /**
         * Checks whether ShivAIAccessibilityService is currently enabled in Android Accessibility Settings.
         */
        fun isServiceEnabled(context: Context): Boolean {
            if (instance != null) return true
            val expectedServiceName = "${context.packageName}/${ShivAIAccessibilityService::class.java.canonicalName}"
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            return enabledServices.split(":").any { it.equals(expectedServiceName, ignoreCase = true) }
        }

        /**
         * Guides user directly to Android Accessibility Settings so they can toggle the service ON.
         */
        fun promptEnableAccessibility(context: Context) {
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Logger.e("Failed to open accessibility settings", e)
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Logger.d("ShivAIAccessibilityService connected and ready for UI automation")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Zero passive background tracking. Only active when ShivAI actions are executed.
    }

    override fun onInterrupt() {
        Logger.d("ShivAIAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    /**
     * Finds and clicks a UI component matching text or content description.
     */
    fun clickElementByText(targetText: String, exactMatch: Boolean = false): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        var targetNode: AccessibilityNodeInfo? = null

        fun traverse(node: AccessibilityNodeInfo) {
            val text = node.text?.toString() ?: ""
            val desc = node.contentDescription?.toString() ?: ""

            val match = if (exactMatch) {
                text.equals(targetText, ignoreCase = true) || desc.equals(targetText, ignoreCase = true)
            } else {
                text.contains(targetText, ignoreCase = true) || desc.contains(targetText, ignoreCase = true)
            }

            if (match) {
                targetNode = node
                return
            }

            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                if (child != null) traverse(child)
                if (targetNode != null) return
            }
        }

        traverse(rootNode)

        targetNode?.let { node ->
            return if (node.isClickable) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            } else {
                val rect = Rect()
                node.getBoundsInScreen(rect)
                performTapGesture(rect.centerX().toFloat(), rect.centerY().toFloat())
                true
            }
        }

        return false
    }

    /**
     * Types text into the currently focused or first available editable field.
     */
    fun enterText(text: String, targetDescription: String? = null): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        var editableNode: AccessibilityNodeInfo? = null

        fun traverse(node: AccessibilityNodeInfo) {
            if (node.isEditable) {
                if (targetDescription.isNullOrBlank()) {
                    editableNode = node
                    return
                } else {
                    val desc = (node.contentDescription?.toString() ?: "") + (node.hintText?.toString() ?: "")
                    if (desc.contains(targetDescription, ignoreCase = true)) {
                        editableNode = node
                        return
                    }
                }
            }

            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                if (child != null) traverse(child)
                if (editableNode != null) return
            }
        }

        traverse(rootNode)

        editableNode?.let { node ->
            node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            val args = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        }

        return false
    }

    /**
     * Scrolls the current view forward or backward.
     */
    fun performScroll(forward: Boolean = true): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val action = if (forward) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        return rootNode.performAction(action)
    }

    /**
     * Dispatches a tap gesture at exact screen coordinates (x, y).
     */
    fun performTapGesture(x: Float, y: Float): Boolean {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 80)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, null, null)
    }

    /**
     * Dispatches a swipe gesture from (startX, startY) to (endX, endY).
     */
    fun performSwipeGesture(startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long = 300): Boolean {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, null, null)
    }

    /**
     * Returns all visible texts from the active window so ShivAI understands what's on screen.
     */
    fun getVisibleScreenText(): List<String> {
        val root = rootInActiveWindow ?: return emptyList()
        val textList = mutableListOf<String>()

        fun traverse(node: AccessibilityNodeInfo) {
            val text = node.text?.toString()?.trim()
            val desc = node.contentDescription?.toString()?.trim()
            if (!text.isNullOrBlank()) textList.add(text)
            if (!desc.isNullOrBlank() && desc != text) textList.add(desc)

            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                if (child != null) traverse(child)
            }
        }

        traverse(root)
        return textList
    }

    /**
     * Captures screen on-demand only when ShivAI or the user explicitly requests screen analysis.
     */
    fun captureScreenOnDemand(onComplete: (Bitmap?) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                takeScreenshot(
                    android.view.Display.DEFAULT_DISPLAY,
                    applicationContext.mainExecutor,
                    object : TakeScreenshotCallback {
                        override fun onSuccess(screenshotResult: ScreenshotResult) {
                            val hwBuffer = screenshotResult.hardwareBuffer
                            val colorSpace = screenshotResult.colorSpace
                            val bitmap = Bitmap.wrapHardwareBuffer(hwBuffer, colorSpace)
                            val softwareBitmap = bitmap?.copy(Bitmap.Config.ARGB_8888, false)
                            hwBuffer.close()
                            onComplete(softwareBitmap ?: bitmap)
                        }

                        override fun onFailure(errorCode: Int) {
                            Logger.e("On-demand screenshot failed with code: $errorCode")
                            onComplete(null)
                        }
                    }
                )
            } catch (e: Exception) {
                Logger.e("Failed to request on-demand screenshot", e)
                onComplete(null)
            }
        } else {
            onComplete(null)
        }
    }
}
