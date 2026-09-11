package com.example.core.permissions

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat
import com.example.LauncherActivity
import kotlinx.coroutines.*
import java.util.Locale

/**
 * ShivAIPersistentService:
 * Keeps ShivAI alive 24/7 in the background with Partial WakeLock.
 * Listens for "Hey ShivAI" or "ShivAI" even when the phone is sleeping / screen is off.
 * On wake word detection, powers on the display, emits sacred haptic feedback,
 * and launches the ShivAI assistant ready to converse.
 */
class ShivAIPersistentService : Service() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var partialWakeLock: PowerManager.WakeLock? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    // Phone went to sleep, ensure partial wake lock is active and restart listening
                    acquirePartialWakeLock()
                    startWakeWordListening()
                }
                Intent.ACTION_SCREEN_ON -> {
                    // Screen turned on
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquirePartialWakeLock()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenReceiver, filter)

        initSpeechRecognizer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1008, notification)

        startWakeWordListening()

        return START_STICKY
    }

    private fun acquirePartialWakeLock() {
        if (partialWakeLock?.isHeld != true) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            partialWakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "ShivAI:PersistentWakeLock"
            )?.apply {
                setReferenceCounted(false)
                acquire(24 * 60 * 60 * 1000L) // 24 hours
            }
        }
    }

    private fun initSpeechRecognizer() {
        mainHandler.post {
            try {
                if (SpeechRecognizer.isRecognitionAvailable(this)) {
                    speechRecognizer?.destroy()
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                        setRecognitionListener(createRecognitionListener())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startWakeWordListening() {
        mainHandler.post {
            if (isListening) return@post
            try {
                if (speechRecognizer == null) {
                    initSpeechRecognizer()
                }
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }
                isListening = true
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                isListening = false
                scheduleRestartListening(2000)
            }
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
            }

            override fun onError(error: Int) {
                isListening = false
                // Automatically restart listening after short backoff
                scheduleRestartListening(1500)
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                checkWakeWord(matches)
                scheduleRestartListening(1000)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (checkWakeWord(matches)) {
                    speechRecognizer?.stopListening()
                    isListening = false
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun checkWakeWord(matches: List<String>?): Boolean {
        if (matches == null) return false
        val wakeKeywords = listOf("hey shivai", "hey shiva", "shivai", "namaste shivai", "hey shiv", "shiv ai", "shiva")
        for (candidate in matches) {
            val lower = candidate.lowercase().trim()
            for (keyword in wakeKeywords) {
                if (lower.contains(keyword)) {
                    triggerWakeUpAndLaunch(lower)
                    return true
                }
            }
        }
        return false
    }

    private fun triggerWakeUpAndLaunch(query: String) {
        // 1. Wake up the screen if sleeping
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            @Suppress("DEPRECATION")
            val screenLock = powerManager?.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "ShivAI:WakeScreen"
            )
            screenLock?.acquire(4000)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Sacred Haptic feedback
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(180, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(180)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Launch LauncherActivity into Chat with active voice
        val launchIntent = Intent(this, LauncherActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("EXTRA_WAKE_WORD_ACTIVATED", true)
            putExtra("EXTRA_QUERY_TEXT", query)
            putExtra("EXTRA_TARGET_SCREEN", "chat")
        }
        startActivity(launchIntent)
    }

    private fun scheduleRestartListening(delayMs: Long) {
        mainHandler.postDelayed({
            startWakeWordListening()
        }, delayMs)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {}
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {}
        partialWakeLock?.release()
        scope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "shivai_persistent",
                "ShivAI Sentinel Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps ShivAI ready for 'Hey ShivAI' wake word and background tasks"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val launchIntent = Intent(this, LauncherActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "shivai_persistent")
            .setContentTitle("ShivAI Sentinel Active")
            .setContentText("Listening for 'Hey ShivAI' • Multi-agent ready")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}
