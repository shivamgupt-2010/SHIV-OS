package com.example.ai.voice.session

import com.example.ai.orchestrator.CentralOrchestrator
import com.example.ai.voice.audio.AudioResourceManager
import com.example.ai.voice.stt.SpeechToTextEngine
import com.example.ai.voice.stt.SttResult
import com.example.ai.voice.tts.TextToSpeechEngine
import com.example.ai.voice.tts.TtsEvent
import com.example.core.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class VoiceSessionState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

class VoiceSessionManager(
    private val sttEngine: SpeechToTextEngine,
    private val ttsEngine: TextToSpeechEngine,
    private val audioResourceManager: AudioResourceManager,
    private val orchestrator: CentralOrchestrator
) {
    private val _state = MutableStateFlow(VoiceSessionState.IDLE)
    val state: StateFlow<VoiceSessionState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var currentListeningJob: Job? = null
    private var currentSpeakingJob: Job? = null

    /**
     * Starts the voice interaction loop.
     */
    fun startInteraction() {
        if (!audioResourceManager.requestAudioFocus()) {
            Logger.e("Failed to acquire audio focus for VoiceSession.")
            _state.value = VoiceSessionState.ERROR
            return
        }

        interruptOngoingOperations()
        startListening()
    }

    /**
     * Interrupts any ongoing TTS or Orchestrator behavior, 
     * useful for push-to-talk to barge in.
     */
    fun interruptAndListen() {
        interruptOngoingOperations()
        startListening()
    }

    /**
     * Ends the voice session entirely.
     */
    fun endSession() {
        interruptOngoingOperations()
        audioResourceManager.abandonAudioFocus()
        _state.value = VoiceSessionState.IDLE
    }

    private fun startListening() {
        _state.value = VoiceSessionState.LISTENING
        
        currentListeningJob = scope.launch {
            sttEngine.startListening().collectLatest { result ->
                when (result) {
                    is SttResult.Partial -> {
                         // Real-time UI updates could consume this
                    }
                    is SttResult.Final -> {
                        if (result.text.isNotBlank()) {
                            processUserQuery(result.text)
                        } else {
                            // Empty transcription, revert to idle or listen again
                            Logger.d("Empty STT result")
                            endSession()
                        }
                    }
                    is SttResult.Error -> {
                        Logger.e("STT Error: ${result.message}")
                        _state.value = VoiceSessionState.ERROR
                        endSession()
                    }
                }
            }
        }
    }

    private fun processUserQuery(query: String) {
        _state.value = VoiceSessionState.THINKING
        currentListeningJob?.cancel()

        scope.launch {
            try {
                // Here we call Orchestrator, await response, and then speak it.
                // Note: Realistically, real-time streaming engines chunk tokens directly into
                // the TTS engine. For simplicity, we process the whole query string.
                val responseResult = orchestrator.processTask(query, "voice_session")
                
                if (responseResult is com.example.core.utils.Result.Success) {
                    speakResponse(responseResult.data)
                } else {
                    speakResponse("I'm sorry, I encountered an error while processing that.")
                }
            } catch (e: Exception) {
                Logger.e("Error processing voice query", e)
                speakResponse("Something went wrong.")
            }
        }
    }

    private fun speakResponse(text: String) {
        _state.value = VoiceSessionState.SPEAKING
        
        currentSpeakingJob = scope.launch(Dispatchers.Main) {
            ttsEngine.speak(text).collectLatest { event ->
                when (event) {
                    is TtsEvent.Started -> {
                        Logger.d("TTS Started")
                    }
                    is TtsEvent.Completed -> {
                        Logger.d("TTS Completed")
                        // Start listening again naturally for back-and-forth conversational loops
                        // Or end session based on adaptive logic
                        endSession() 
                    }
                    is TtsEvent.Error -> {
                        Logger.e("TTS Error: ${event.message}")
                        _state.value = VoiceSessionState.ERROR
                        endSession()
                    }
                }
            }
        }
    }

    private fun interruptOngoingOperations() {
        currentListeningJob?.cancel()
        currentSpeakingJob?.cancel()
        sttEngine.stopListening()
        ttsEngine.stop()
    }
}
