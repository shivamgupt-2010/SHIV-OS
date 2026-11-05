package com.example.companion.avatar

import android.content.Context
import com.example.core.accessibility.ShivAIAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * AvatarKinematicsController:
 * Governs physical flight navigation across the phone screen,
 * target coordinates resolution, contact impact timing, and
 * synchronized execution with Android Accessibility Services.
 */
class AvatarKinematicsController(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _currentPosition = MutableStateFlow(OffsetPoint(60f, 200f))
    val currentPosition: StateFlow<OffsetPoint> = _currentPosition.asStateFlow()

    private val _dockedPosition = MutableStateFlow(OffsetPoint(60f, 200f))
    val dockedPosition: StateFlow<OffsetPoint> = _dockedPosition.asStateFlow()

    private val _isFlying = MutableStateFlow(false)
    val isFlying: StateFlow<Boolean> = _isFlying.asStateFlow()

    private val _activeTarget = MutableStateFlow<FlightTarget?>(null)
    val activeTarget: StateFlow<FlightTarget?> = _activeTarget.asStateFlow()

    private val _touchTensionAngle = MutableStateFlow(0f)
    val touchTensionAngle: StateFlow<Float> = _touchTensionAngle.asStateFlow()

    data class OffsetPoint(val x: Float, val y: Float)

    /**
     * Orders the avatar to physically fly to a UI target,
     * execute the physical touch animation, and dispatch the Android Accessibility action.
     */
    fun flyAndInteract(target: FlightTarget, onCompleted: (() -> Unit)? = null) {
        scope.launch {
            _isFlying.value = true
            _activeTarget.value = target

            val start = _currentPosition.value
            val destX = target.targetX
            val destY = target.targetY

            // 1. Flight Bezier Curve (Travel time ~ 240ms)
            val steps = 18
            val durationPerStep = target.durationMs / steps

            for (i in 1..steps) {
                val t = i.toFloat() / steps.toFloat()
                // Ease-out cubic formula
                val easeT = 1f - (1f - t).pow(3)

                val currX = start.x + (destX - start.x) * easeT
                // Arc flight path with slight upward curvature
                val arcY = sin(t * Math.PI.toFloat()) * -45f
                val currY = (start.y + (destY - start.y) * easeT) + arcY

                _currentPosition.value = OffsetPoint(currX, currY)
                delay(durationPerStep)
            }

            // Arrived directly at target UI bounds
            _currentPosition.value = OffsetPoint(destX, destY)

            // 2. Play Physical Contact & Dispatch OS Gesture
            when (target.actionType) {
                TouchActionType.TAP -> {
                    // Contact downward plunge & immediate OS tap
                    val a11y = ShivAIAccessibilityService.instance
                    if (a11y != null) {
                        a11y.performTapGesture(destX, destY)
                    }
                    delay(120) // Contact moment
                }
                TouchActionType.DRAG -> {
                    // Physical tension pull (lean back at 30 degrees)
                    _touchTensionAngle.value = -28f
                    delay(250)
                    _touchTensionAngle.value = 0f
                }
                TouchActionType.PUSH -> {
                    // Two-handed shove forward
                    delay(150)
                }
                TouchActionType.TYPE -> {
                    // Rapid fluttering
                    delay(300)
                }
            }

            // 3. Retreat smoothly to edge docked position
            val dock = _dockedPosition.value
            for (i in 1..steps) {
                val t = i.toFloat() / steps.toFloat()
                val easeT = 1f - (1f - t).pow(2)
                val currX = destX + (dock.x - destX) * easeT
                val currY = destY + (dock.y - destY) * easeT
                _currentPosition.value = OffsetPoint(currX, currY)
                delay(durationPerStep)
            }

            _currentPosition.value = dock
            _isFlying.value = false
            _activeTarget.value = null
            onCompleted?.invoke()
        }
    }

    fun updateDockedPosition(x: Float, y: Float) {
        _dockedPosition.value = OffsetPoint(x, y)
        if (!_isFlying.value) {
            _currentPosition.value = OffsetPoint(x, y)
        }
    }
}
