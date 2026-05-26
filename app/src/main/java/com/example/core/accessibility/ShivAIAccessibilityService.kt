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
        if (event == null) return

        val rootNode = rootInActiveWindow
        val packageName = event.packageName?.toString() ?: "unknown"

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastEventTime > 500) { // 500ms debounce
                lastEventTime = currentTime
                
                scope.launch {
                    try {
                        ScreenIntelligence.engine.analyzeScreen(rootNode, packageName)
                    } catch (e: Exception) {
                        Logger.e("Accessibility Analysis Error", e)
                    }
                }
                
                // Trigger Visual Intelligence (OCR)
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
                                    bitmap?.let {
                                        // Hardware bitmaps can cause issues with ML Kit and need copy
                                        val softwareBitmap = it.copy(Bitmap.Config.ARGB_8888, false)
                                        val appContainer = (applicationContext as ShivAiApplication).container
                                        appContainer.ocrIntelligenceEngine.processScreenshot(softwareBitmap ?: it)
                                    }
                                    hwBuffer.close()
                                }

                                override fun onFailure(errorCode: Int) {
                                    Logger.e("Screenshot failed with code: \$errorCode")
                                }
                            }
                        )
                    } catch (e: Exception) {
                        Logger.e("Failed to request screenshot", e)
                    }
                }
            }
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
