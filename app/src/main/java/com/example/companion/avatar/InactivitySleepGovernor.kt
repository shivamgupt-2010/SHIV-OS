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
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : SensorEventListener {

    private val _sleepStage = MutableStateFlow(InactivitySleepStage.ENGAGED_60FPS)
    val sleepStage: StateFlow<InactivitySleepStage> = _sleepStage.asStateFlow()

    private var lastActivityTime = SystemClock.elapsedRealtime()
    private var tickerJob: Job? = null
    private var isStarted = false
    private var isSensorRegistered = false

    private val sensorManager = context.applicationContext
        .getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    fun start() {
        if (isStarted) return
        isStarted = true
        lastActivityTime = SystemClock.elapsedRealtime()
        startTickerIfNeeded()

        // The normal sampling rate is enough for a deliberate shake and costs
        // materially less battery than the UI refresh sampling rate.
        if (!isSensorRegistered) {
            accelerometer?.let {
                isSensorRegistered = sensorManager?.registerListener(
                    this,
                    it,
                    SensorManager.SENSOR_DELAY_NORMAL
                ) == true
            }
        }
    }

    fun stop() {
        isStarted = false
        tickerJob?.cancel()
        tickerJob = null
        if (isSensorRegistered) {
            sensorManager?.unregisterListener(this)
            isSensorRegistered = false
        }
    }

    /**
     * Resets the 3-minute timer on any user touch, speech input, or task activity.
     */
    fun reportUserActivity() {
        if (!isStarted) return
        lastActivityTime = SystemClock.elapsedRealtime()
        if (_sleepStage.value != InactivitySleepStage.ENGAGED_60FPS) {
            updateStage(InactivitySleepStage.ENGAGED_60FPS)
        }
        startTickerIfNeeded()
    }

    private fun startTickerIfNeeded() {
        if (!isStarted || tickerJob?.isActive == true) return
        tickerJob = scope.launch {
            while (isActive && isStarted) {
                val stage = evaluateInactivity()
                // Once the companion reaches deep slumber, leave no timer
                // running. reportUserActivity() restarts the ticker on wake.
                if (stage == InactivitySleepStage.DEEP_SLUMBER_0FPS) break
                delay(nextCheckDelayMillis())
            }
        }
    }

    private fun evaluateInactivity(): InactivitySleepStage {
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
        return newStage
    }

    /**
     * Checks rapidly only around the active boundary. The ambient and drowsy
     * intervals can be evaluated less often with no visible behavioural cost.
     */
    private fun nextCheckDelayMillis(): Long = when (_sleepStage.value) {
        InactivitySleepStage.ENGAGED_60FPS -> 1_000L
        InactivitySleepStage.AMBIENT_30FPS -> 5_000L
        InactivitySleepStage.DROWSY_15FPS -> 5_000L
        InactivitySleepStage.DEEP_SLUMBER_0FPS -> Long.MAX_VALUE
    }

    private fun updateStage(stage: InactivitySleepStage) {
        _sleepStage.value = stage
        onSlumberStateChanged?.invoke(stage)
    }

    // Accelerometer Shake / Drop Detection
    private var lastAccelX = 0f
    private var lastAccelY = 0f
    private var lastAccelZ = 0f
    private var lastShakeAt = 0L

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

        val now = SystemClock.elapsedRealtime()
        if (gForce > 14f && now - lastShakeAt > 900L) { // Noticeable shake or drop
            lastShakeAt = now
            reportUserActivity()
            onDeviceShaken?.invoke()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
