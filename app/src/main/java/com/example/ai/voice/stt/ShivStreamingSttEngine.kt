package com.example.ai.voice.stt

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.example.core.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.sqrt

/**
 * ShivAI Zero-Beep Streaming Speech-to-Text Engine.
 * Records microphone audio directly via low-level AudioRecord with zero sound effects or beeps,
 * performs lightweight energy-based silence detection, and transcribes via SHIV AI V2 STT.
 */
class ShivStreamingSttEngine(
    private val context: Context,
    private val serverBaseUrl: String = "https://shiv-backend.onrender.com"
) : SpeechToTextEngine {

    override var isListening: Boolean = false
        private set

    private var audioRecord: AudioRecord? = null
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    @SuppressLint("MissingPermission")
    override fun startListening(): Flow<SttResult> = callbackFlow {
        isListening = true

        withContext(Dispatchers.IO) {
            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val bufferSize = maxOf(minBufferSize, 4096)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                trySend(SttResult.Error("AudioRecord initialization failed"))
                close()
                return@withContext
            }

            audioRecord?.startRecording()
            val audioBuffer = ByteArray(bufferSize)
            val pcmDataStream = ByteArrayOutputStream()
            var silenceFrames = 0
            val maxSilenceFrames = 25 // ~1.5s of silence

            while (isListening) {
                val bytesRead = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: -1
                if (bytesRead > 0) {
                    pcmDataStream.write(audioBuffer, 0, bytesRead)

                    // Calculate RMS
                    var sum = 0.0
                    for (i in 0 until bytesRead step 2) {
                        val sample = (audioBuffer[i + 1].toInt() shl 8) or (audioBuffer[i].toInt() and 0xFF)
                        sum += sample * sample
                    }
                    val rms = sqrt(sum / (bytesRead / 2))

                    if (rms < 500) {
                        silenceFrames++
                        if (silenceFrames >= maxSilenceFrames && pcmDataStream.size() > 16000) {
                            // Turn endpoint reached naturally
                            break
                        }
                    } else {
                        silenceFrames = 0
                    }
                }
            }

            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null

            // Send captured PCM/WAV to SHIV AI V2 STT
            val wavBytes = convertPcmToWav(pcmDataStream.toByteArray(), sampleRate)
            val transcribedText = uploadAndTranscribe(wavBytes)

            if (transcribedText.isNotBlank()) {
                trySend(SttResult.Final(transcribedText))
            } else {
                trySend(SttResult.Final(""))
            }
            close()
        }

        awaitClose {
            stopListening()
        }
    }

    override fun stopListening() {
        isListening = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) {
            Logger.e("Error stopping AudioRecord", e)
        }
    }

    override fun destroy() {
        stopListening()
    }

    private fun uploadAndTranscribe(wavBytes: BytesArray): String {
        return try {
            val boundary = "---GroqWhisperBoundary" + System.currentTimeMillis()
            val url = URL("https://api.groq.com/openai/v1/audio/transcriptions")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            val groqKey = com.example.ai.shivai.engine.ShivAIRealEngine.getGroqKey()
            conn.setRequestProperty("Authorization", "Bearer $groqKey")
            conn.setRequestProperty("User-Agent", "SHIV-OS/1.0")
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            conn.connectTimeout = 15000
            conn.readTimeout = 20000
            conn.doOutput = true

            conn.outputStream.use { os ->
                // Model parameter
                os.write("--$boundary\r\n".toByteArray())
                os.write("Content-Disposition: form-data; name=\"model\"\r\n\r\n".toByteArray())
                os.write("whisper-large-v3-turbo\r\n".toByteArray())

                // File parameter
                os.write("--$boundary\r\n".toByteArray())
                os.write("Content-Disposition: form-data; name=\"file\"; filename=\"speech.wav\"\r\n".toByteArray())
                os.write("Content-Type: audio/wav\r\n\r\n".toByteArray())
                os.write(wavBytes)
                os.write("\r\n--$boundary--\r\n".toByteArray())
                os.flush()
            }

            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(responseText)
                json.optString("text", "").trim()
            } else {
                val err = conn.errorStream?.bufferedReader()?.readText()
                Logger.e("Groq Whisper error ${conn.responseCode}: $err")
                ""
            }
        } catch (e: Exception) {
            Logger.e("Failed to upload audio to Groq Whisper STT", e)
            ""
        }
    }

    private fun convertPcmToWav(pcmData: ByteArray, sampleRate: Int): ByteArray {
        val totalAudioLen = pcmData.size
        val totalDataLen = totalAudioLen + 36
        val byteRate = 16 * sampleRate * 1 / 8

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte(); header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte(); header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
        header[20] = 1; header[21] = 0 // PCM
        header[22] = 1; header[23] = 0 // Mono
        header[24] = (sampleRate and 0xff).toByte(); header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte(); header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte(); header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte(); header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = 2; header[33] = 0 // 16-bit mono
        header[34] = 16; header[35] = 0 // 16 bits per sample
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte(); header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte(); header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        val wavData = ByteArray(44 + pcmData.size)
        System.arraycopy(header, 0, wavData, 0, 44)
        System.arraycopy(pcmData, 0, wavData, 44, pcmData.size)
        return wavData
    }
}

typealias BytesArray = ByteArray
