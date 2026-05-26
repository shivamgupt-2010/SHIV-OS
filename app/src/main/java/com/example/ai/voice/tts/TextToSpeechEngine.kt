package com.example.ai.voice.tts

import kotlinx.coroutines.flow.Flow

interface TextToSpeechEngine {
    val isSpeaking: Boolean
    
    /**
     * Speaks the given text. Returns a Flow that emits progress/completion events.
     */
    fun speak(text: String): Flow<TtsEvent>
    
    /**
     * Immediately stops current playback and clears queue.
     */
    fun stop()
    
    fun destroy()
}

sealed class TtsEvent {
    object Started : TtsEvent()
    object Completed : TtsEvent()
    data class Error(val message: String) : TtsEvent()
}
