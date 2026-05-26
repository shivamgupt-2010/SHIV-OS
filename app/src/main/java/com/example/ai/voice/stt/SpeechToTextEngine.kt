package com.example.ai.voice.stt

import kotlinx.coroutines.flow.Flow

sealed class SttResult {
    data class Partial(val text: String) : SttResult()
    data class Final(val text: String) : SttResult()
    data class Error(val message: String) : SttResult()
}

interface SpeechToTextEngine {
    val isListening: Boolean
    fun startListening(): Flow<SttResult>
    fun stopListening()
    fun destroy()
}
