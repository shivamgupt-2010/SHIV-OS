package com.example.companion.avatar

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Single writer for avatar state used by services and overlays.
 *
 * Keeping state transitions here avoids races between the voice pipeline,
 * sensor callbacks, and Compose event handlers. In particular, deep slumber
 * always wins over visual activity until a deliberate user interaction wakes
 * the companion again.
 */
class AvatarRuntimeController(
    initialContext: AvatarRenderContext = AvatarRenderContext()
) {
    private val _renderContext = MutableStateFlow(initialContext)
    val renderContext: StateFlow<AvatarRenderContext> = _renderContext.asStateFlow()

    fun selectAvatar(type: AvatarType) {
        _renderContext.update { it.copy(avatarType = type) }
    }

    fun reportInteraction(state: AvatarState = AvatarState.IDLE) {
        _renderContext.update {
            it.copy(
                state = state,
                sleepStage = InactivitySleepStage.ENGAGED_60FPS,
                audioAmplitude = 0f
            )
        }
    }

    fun applySleepStage(stage: InactivitySleepStage) {
        _renderContext.update { current ->
            val nextState = when (stage) {
                InactivitySleepStage.ENGAGED_60FPS,
                InactivitySleepStage.AMBIENT_30FPS -> {
                    if (current.state == AvatarState.SLEEPING || current.state == AvatarState.DROWSY) {
                        AvatarState.IDLE
                    } else {
                        current.state
                    }
                }
                InactivitySleepStage.DROWSY_15FPS -> {
                    if (current.state == AvatarState.IDLE) AvatarState.DROWSY else current.state
                }
                InactivitySleepStage.DEEP_SLUMBER_0FPS -> AvatarState.SLEEPING
            }

            current.copy(
                sleepStage = stage,
                state = nextState,
                audioAmplitude = if (stage == InactivitySleepStage.DEEP_SLUMBER_0FPS) 0f else current.audioAmplitude,
                isGhostForm = if (stage == InactivitySleepStage.DEEP_SLUMBER_0FPS) false else current.isGhostForm
            )
        }
    }

    fun updateVoiceAmplitude(amplitude: Float) {
        _renderContext.update { current ->
            if (current.sleepStage == InactivitySleepStage.DEEP_SLUMBER_0FPS) {
                current
            } else {
                val normalized = amplitude.coerceIn(0f, 1f)
                current.copy(
                    audioAmplitude = normalized,
                    state = when {
                        normalized > 0.05f -> AvatarState.SPEAKING
                        current.state == AvatarState.SPEAKING -> AvatarState.IDLE
                        else -> current.state
                    }
                )
            }
        }
    }

    fun showEvent(event: SituationalEvent, state: AvatarState = AvatarState.ALERT) {
        _renderContext.update {
            it.copy(
                situationalEvent = event,
                state = state,
                sleepStage = InactivitySleepStage.ENGAGED_60FPS
            )
        }
    }

    fun clearEvent() {
        _renderContext.update {
            it.copy(situationalEvent = SituationalEvent.NONE, state = AvatarState.IDLE)
        }
    }

    fun setState(state: AvatarState) {
        _renderContext.update { current ->
            if (current.sleepStage == InactivitySleepStage.DEEP_SLUMBER_0FPS) current
            else current.copy(state = state)
        }
    }
}
