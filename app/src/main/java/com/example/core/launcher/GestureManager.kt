package com.example.core.launcher

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.reflect.Method

enum class LauncherGesture(val title: String, val description: String) {
    SWIPE_UP("Swipe Up", "Vertical swipe upwards on home wallpaper"),
    SWIPE_DOWN("Swipe Down", "Vertical swipe downwards from home screen"),
    DOUBLE_TAP("Double Tap", "Rapid double tap on empty home area"),
    PINCH_IN("Pinch In", "Two-finger pinch inward on wallpaper"),
    TWO_FINGER_SWIPE_DOWN("Two-Finger Swipe Down", "Simultaneous two-finger drag downwards")
}

enum class GestureAction(val title: String, val targetRoute: String?) {
    OPEN_APP_DRAWER("Open App Drawer", "all_apps"),
    OPEN_CHAT("Open ShivAI Chat", "chat"),
    OPEN_NOTIFICATIONS("Expand Notification Shade", null),
    OPEN_STUDIO("Open SHIV Studio", "studio"),
    OPEN_NOTES("Open Quick Notes", "notes"),
    OPEN_AUTOMATIONS("Open Automations Engine", "automations"),
    OPEN_SAFETY("Open Safety & Lock Center", "safety_security"),
    LOCK_SCREEN("Lock Screen", null),
    NONE("None (Disabled)", null)
}

/**
 * GestureManager:
 * Configurable, high-precision gesture dispatching engine.
 * Maps home screen touch gestures to system actions and launcher routes.
 */
class GestureManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("shiv_gesture_mappings", Context.MODE_PRIVATE)

    private val _mappings = MutableStateFlow<Map<LauncherGesture, GestureAction>>(loadMappings())
    val mappings: StateFlow<Map<LauncherGesture, GestureAction>> = _mappings.asStateFlow()

    private fun loadMappings(): Map<LauncherGesture, GestureAction> {
        val map = mutableMapOf<LauncherGesture, GestureAction>()
        for (gesture in LauncherGesture.entries) {
            val defaultAction = when (gesture) {
                LauncherGesture.SWIPE_UP -> GestureAction.OPEN_APP_DRAWER
                LauncherGesture.SWIPE_DOWN -> GestureAction.OPEN_NOTIFICATIONS
                LauncherGesture.DOUBLE_TAP -> GestureAction.OPEN_CHAT
                LauncherGesture.PINCH_IN -> GestureAction.OPEN_STUDIO
                LauncherGesture.TWO_FINGER_SWIPE_DOWN -> GestureAction.OPEN_NOTES
            }
            val savedName = prefs.getString(gesture.name, defaultAction.name)
            val action = try {
                GestureAction.valueOf(savedName ?: defaultAction.name)
            } catch (e: Exception) {
                defaultAction
            }
            map[gesture] = action
        }
        return map
    }

    fun setGestureAction(gesture: LauncherGesture, action: GestureAction) {
        val updated = _mappings.value.toMutableMap()
        updated[gesture] = action
        _mappings.value = updated
        prefs.edit().putString(gesture.name, action.name).apply()
    }

    fun executeGesture(activity: Activity, gesture: LauncherGesture, onNavigate: (String) -> Unit): Boolean {
        val action = _mappings.value[gesture] ?: return false
        when (action) {
            GestureAction.NONE -> return false
            GestureAction.OPEN_NOTIFICATIONS -> {
                expandNotificationPanel(activity)
                return true
            }
            GestureAction.LOCK_SCREEN -> {
                Toast.makeText(activity, "Lock gesture triggered", Toast.LENGTH_SHORT).show()
                return true
            }
            else -> {
                action.targetRoute?.let { route ->
                    onNavigate(route)
                    return true
                }
            }
        }
        return false
    }

    private fun expandNotificationPanel(activity: Activity) {
        try {
            val statusBarService = activity.getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val method: Method = statusBarManager.getMethod("expandNotificationsPanel")
            method.invoke(statusBarService)
        } catch (e: Exception) {
            try {
                // Fallback for some OEM skins
                val statusBarService = activity.getSystemService("statusbar")
                val statusBarManager = Class.forName("android.app.StatusBarManager")
                val method: Method = statusBarManager.getMethod("expand")
                method.invoke(statusBarService)
            } catch (e2: Exception) {
                Toast.makeText(activity, "Notification panel expansion unsupported by OEM", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
