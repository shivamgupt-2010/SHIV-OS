package com.example.ai.voice.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.core.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * ShivAI High-Fidelity Neural TTS Engine.
 * Replaces default Google TTS with Microsoft Neural Hindi & Indian English voices via SHIV AI V2.
 * Streams audio seamlessly with no robotic tones.
 */
class ShivNeuralTtsEngine(
    private val context: Context,
    private val serverBaseUrl: String = "https://shiv-backend.onrender.com"
) : TextToSpeechEngine {

    private var mediaPlayer: MediaPlayer? = null
    override var isSpeaking: Boolean = false
        private set

    override fun speak(text: String): Flow<TtsEvent> = callbackFlow {
        isSpeaking = true
        trySend(TtsEvent.Started)

        withContext(Dispatchers.IO) {
            try {
                val url = URL("$serverBaseUrl/v2/voice/tts")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val payload = JSONObject().apply {
                    put("text", text)
                    put("voice", "hindi_female")
                }

                conn.outputStream.use { os ->
                    os.write(payload.toString().toByteArray())
                }

                if (conn.responseCode == 200) {
                    val tempAudioFile = File(context.cacheDir, "tts_response.mp3")
                    conn.inputStream.use { input ->
                        FileOutputStream(tempAudioFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        mediaPlayer?.release()
                        mediaPlayer = MediaPlayer().apply {
                            setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                    .setUsage(AudioAttributes.USAGE_ASSISTANT)
                                    .build()
                            )
                            setDataSource(tempAudioFile.absolutePath)
                            prepare()
                            setOnCompletionListener {
                                isSpeaking = false
                                trySend(TtsEvent.Completed)
                                close()
                            }
                            setOnErrorListener { _, what, extra ->
                                isSpeaking = false
                                trySend(TtsEvent.Error("MediaPlayer error: $what, $extra"))
                                close()
                                true
                            }
                            start()
                        }
                    }
                } else {
                    isSpeaking = false
                    trySend(TtsEvent.Error("HTTP Error: ${conn.responseCode}"))
                    close()
                }
            } catch (e: Exception) {
                Logger.e("ShivNeuralTtsEngine failed", e)
                isSpeaking = false
                trySend(TtsEvent.Error(e.message ?: "Unknown TTS Error"))
                close()
            }
        }

        awaitClose {
            stop()
        }
    }

    override fun stop() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.reset()
        } catch (e: Exception) {
            Logger.e("Error stopping MediaPlayer", e)
        }
        isSpeaking = false
    }

    override fun destroy() {
        stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
