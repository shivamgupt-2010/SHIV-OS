package com.example.ai.voice.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.core.utils.Logger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

class AndroidTextToSpeech(private val context: Context) : TextToSpeechEngine {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    override var isSpeaking: Boolean = false
        private set

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Logger.e("TTS Language is not supported or missing data")
                } else {
                    isInitialized = true
                }
            } else {
                Logger.e("TTS Initialization failed")
            }
        }
    }

    override fun speak(text: String): Flow<TtsEvent> = callbackFlow {
        if (!isInitialized) {
            trySend(TtsEvent.Error("TTS not initialized"))
            close()
            return@callbackFlow
        }

        val utteranceId = UUID.randomUUID().toString()

        val listener = object : UtteranceProgressListener() {
            override fun onStart(id: String?) {
                if (id == utteranceId) {
                    isSpeaking = true
                    trySend(TtsEvent.Started)
                }
            }

            override fun onDone(id: String?) {
                if (id == utteranceId) {
                    isSpeaking = false
                    trySend(TtsEvent.Completed)
                    close()
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(id: String?) {
                if (id == utteranceId) {
                    isSpeaking = false
                    trySend(TtsEvent.Error("TTS Error"))
                    close()
                }
            }
        }

        tts?.setOnUtteranceProgressListener(listener)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

        awaitClose {
            // No cleanup required for individual stream, 
            // stop() handles cancelling speech globally.
        }
    }

    override fun stop() {
        tts?.stop()
        isSpeaking = false
    }

    override fun destroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
