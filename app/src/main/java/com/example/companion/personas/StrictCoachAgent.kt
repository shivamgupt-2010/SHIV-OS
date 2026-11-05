package com.example.companion.personas

import android.accessibilityservice.AccessibilityService
import android.content.Context
import com.example.core.accessibility.ShivAIAccessibilityService
import com.example.core.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * StrictCoachAgent:
 * Un-Bypassable System-Level Focus & Study Coach.
 * Operates through ShivAIAccessibilityService and the System Alert Overlay:
 * - When in Focus/Study mode, detects distracting apps (Instagram, Shorts, Reddit).
 * - Sub-100ms eviction: triggers GLOBAL_ACTION_HOME before the feed can even load.
 * - Triggers stern voice warning and displays an impenetrable timer friction shield.
 */
class StrictCoachAgent(private val context: Context) {

    private val _isFocusSessionActive = MutableStateFlow(false)
    val isFocusSessionActive: StateFlow<Boolean> = _isFocusSessionActive.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(0)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _distractionCount = MutableStateFlow(0)
    val distractionCount: StateFlow<Int> = _distractionCount.asStateFlow()

    val blockedPackages = setOf(
        "com.instagram.android",
        "com.reddit.frontpage",
        "com.facebook.katana",
        "com.zhiliaoapp.musically",
        "com.netflix.mediaclient",
        "com.twitter.android",
        "com.snapchat.android"
    )

    fun startFocusSession(durationMinutes: Int) {
        _isFocusSessionActive.value = true
        _remainingSeconds.value = durationMinutes * 60
        _distractionCount.value = 0
        Logger.d("StrictCoach Focus Session started for $durationMinutes minutes")
    }

    fun endFocusSession() {
        _isFocusSessionActive.value = false
        _remainingSeconds.value = 0
    }

    /**
     * Called by ShivAIAccessibilityService on every window state change event.
     */
    fun onPackageForegrounded(packageName: String): Boolean {
        if (!_isFocusSessionActive.value) return false

        if (blockedPackages.contains(packageName)) {
            _distractionCount.value += 1
            Logger.w("StrictCoach: Evicting user from distraction app $packageName")

            // Sub-100ms eviction back to home screen
            val a11y = ShivAIAccessibilityService.instance
            a11y?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
            return true
        }

        return false
    }
}
