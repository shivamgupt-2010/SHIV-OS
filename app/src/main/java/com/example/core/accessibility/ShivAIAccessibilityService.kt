package com.example.core.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.core.intelligence.ScreenIntelligence
import com.example.core.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import android.os.Build
import android.graphics.Bitmap
import android.hardware.display.DisplayManager
import android.accessibilityservice.AccessibilityService.ScreenshotResult
import com.example.ShivAiApplication

class ShivAIAccessibilityService : AccessibilityService() {

    companion object {
        var instance: ShivAIAccessibilityService? = null
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    private var lastEventTime = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Logger.d("ShivAIAccessibilityService connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Zero background processing when user is interacting with apps.
        // We do NOT take continuous screenshots or run OCR on window changes.
        // Screen capture and analysis are strictly on-demand when requested by user or ShivAI.
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

    override fun onInterrupt() {
        // Handle interrupt
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    fun executeSemanticAction(actionName: String, targetDescription: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        
        var foundNode: AccessibilityNodeInfo? = null
        
        fun traverse(node: AccessibilityNodeInfo) {
            val text = node.text?.toString() ?: ""
            val desc = node.contentDescription?.toString() ?: ""
            
            if (text.contains(targetDescription, ignoreCase = true) || desc.contains(targetDescription, ignoreCase = true)) {
                foundNode = node
                return
            }
            
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                if (child != null) traverse(child)
                if (foundNode != null) return
            }
        }
        
        traverse(rootNode)
        
        foundNode?.let { target ->
            return when (actionName.lowercase()) {
                "click" -> {
                    if (target.isClickable) {
                        target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    } else {
                        // Custom gesture tap if not directly clickable but reachable
                        val rect = android.graphics.Rect()
                        target.getBoundsInScreen(rect)
                        performTapGesture(rect.centerX().toFloat(), rect.centerY().toFloat())
                    }
                    true
                }
                "scroll_forward" -> target.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                else -> false
            }
        }
        
        return false
    }

    private fun performTapGesture(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        dispatchGesture(gesture, null, null)
    }
}
