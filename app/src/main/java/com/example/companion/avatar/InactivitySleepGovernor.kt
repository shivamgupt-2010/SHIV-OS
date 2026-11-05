package com.example.companion.avatar

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

/**
 * InactivitySleepGovernor:
 * Enforces the strict 3-Minute Progressive Inactivity Sleep Lifecycle:
 * - 0s - 30s: ENGAGED_60FPS
 * - 30s - 120s: AMBIENT_30FPS
 * - 120s - 180s (2.5m - 3.0m): DROWSY_15FPS (Drooping eyes & yawns)
 * - 180s+ (3.0m mark): DEEP_SLUMBER_0FPS (Frozen 0 FPS, zero CPU wake-locks)
 *
 * Also monitors device accelerometer to trigger instant wakeup or startled tumble when shaken/dropped.
 */
class InactivitySleepGovernor(
    private val context: Context,
    private val onSlumberStateChanged: ((InactivitySleepStage) -> Unit)? = null,
    private val onDeviceShaken: (() -> Unit)? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : SensorEventListener {

    private val _sleepStage = MutableStateFlow(InactivitySleepStage.ENGAGED_60FPS)
    val sleepStage: StateFlow<InactivitySleepStage> = _sleepStage.asStateFlow()

    private var lastActivityTime = SystemClock.elapsedRealtime()
    private var tickerJob: Job? = null

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    fun start() {
        // Start 3-minute monitoring loop
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                delay(1000) // 1 second check
                evaluateInactivity()
            }
        }

        // Register accelerometer for physical device interaction
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        tickerJob?.cancel()
        sensorManager?.unregisterListener(this)
    }

    /**
     * Resets the 3-minute timer on any user touch, speech input, or task activity.
     */
    fun reportUserActivity() {
        lastActivityTime = SystemClock.elapsedRealtime()
        if (_sleepStage.value != InactivitySleepStage.ENGAGED_60FPS) {
            updateStage(InactivitySleepStage.ENGAGED_60FPS)
        }
    }

    private fun evaluateInactivity() {
        val elapsedSeconds = (SystemClock.elapsedRealtime() - lastActivityTime) / 1000

        val newStage = when {
            elapsedSeconds >= 180 -> InactivitySleepStage.DEEP_SLUMBER_0FPS // 3 minutes mark
            elapsedSeconds >= 120 -> InactivitySleepStage.DROWSY_15FPS     // 2 to 3 minutes
            elapsedSeconds >= 30 -> InactivitySleepStage.AMBIENT_30FPS     // 30s to 2 minutes
            else -> InactivitySleepStage.ENGAGED_60FPS                    // 0s to 30s
        }

        if (newStage != _sleepStage.value) {
            updateStage(newStage)
        }
    }

    private fun updateStage(stage: InactivitySleepStage) {
        _sleepStage.value = stage
        onSlumberStateChanged?.invoke(stage)
    }

    // Accelerometer Shake / Drop Detection
    private var lastAccelX = 0f
    private var lastAccelY = 0f
    private var lastAccelZ = 0f

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val deltaX = x - lastAccelX
        val deltaY = y - lastAccelY
        val deltaZ = z - lastAccelZ

        lastAccelX = x
        lastAccelY = y
        lastAccelZ = z

        val gForce = sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()).toFloat()

        if (gForce > 14f) { // Noticeable shake or drop
            reportUserActivity()
            onDeviceShaken?.invoke()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
