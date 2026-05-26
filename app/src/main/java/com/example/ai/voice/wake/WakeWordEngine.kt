package com.example.ai.voice.wake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Prepares the architecture for a background Wake Word engine (e.g., Porcupine).
 */
class WakeWordEngine {

    private val _wakeWordDetected = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val wakeWordDetected: Flow<Unit> = _wakeWordDetected.asSharedFlow()

    private var isListening = false

    fun startListening() {
        if (isListening) return
        isListening = true
        // Initialize offline wake word detector here in the future
    }

    fun stopListening() {
        isListening = false
        // Tear down offline wake word detector here
    }

    // Mock testing
    suspend fun simulateWakeWord() {
        _wakeWordDetected.emit(Unit)
    }
}
