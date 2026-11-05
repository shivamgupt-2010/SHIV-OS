package com.example.companion.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.companion.auth.ShivAuthGate
import com.example.companion.avatar.*
import com.example.companion.voice.ShivVoiceEngine
import com.example.core.utils.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * FloatingAvatarService:
 * Android Foreground Service managing the live floating avatar companion over the OS.
 * Strictly verifies authentication before displaying the companion overlay.
 */
class FloatingAvatarService : Service() {

    companion object {
        const val CHANNEL_ID = "shiv_companion_overlay_channel"
        const val NOTIFICATION_ID = 4048

        fun start(context: Context) {
            val intent = Intent(context, FloatingAvatarService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FloatingAvatarService::class.java))
        }
    }

    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val renderContext = MutableStateFlow(AvatarRenderContext())
    private val speechCaption = MutableStateFlow<String?>("Om Namas Shivaya 🙏")
    private val activeTaskTitle = MutableStateFlow<String?>(null)

    private lateinit var voiceEngine: ShivVoiceEngine
    private lateinit var sleepGovernor: InactivitySleepGovernor

    private val customLifecycleOwner = object : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        private val controller = SavedStateRegistryController.create(this)
        private val store = ViewModelStore()

        init {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            controller.performRestore(null)
        }

        override val lifecycle: Lifecycle get() = lifecycleRegistry
        override val viewModelStore: ViewModelStore get() = store
        override val savedStateRegistry: SavedStateRegistry get() = controller.savedStateRegistry

        fun start() {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }

        fun destroy() {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            store.clear()
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())

        // Strict Auth Gate Check
        serviceScope.launch {
            if (!ShivAuthGate.verifyAuthentication(applicationContext)) {
                Logger.w("FloatingAvatarService: User unauthenticated. Halting companion overlay.")
                stopSelf()
                return@launch
            }

            initVoiceAndSleepGovernor()
            setupFloatingWindow()
        }
    }

    private fun initVoiceAndSleepGovernor() {
        voiceEngine = ShivVoiceEngine(applicationContext)
        voiceEngine.startWakeWordListening()

        // Sync audio amplitude with avatar mouth phonemes
        serviceScope.launch {
            voiceEngine.amplitude.collect { amp ->
                renderContext.value = renderContext.value.copy(
                    audioAmplitude = amp,
                    state = if (amp > 0.05f) AvatarState.SPEAKING else renderContext.value.state
                )
            }
        }

        // Handle Wake Word "Om Namas Shivaya"
        serviceScope.launch {
            voiceEngine.wakeWordDetected.collect {
                sleepGovernor.reportUserActivity()
                renderContext.value = renderContext.value.copy(state = AvatarState.LISTENING)
                speechCaption.value = "Listening to you, sir..."
                voiceEngine.speak("Om Namas Shivaya. How can I help you today?") {
                    renderContext.value = renderContext.value.copy(state = AvatarState.IDLE)
                }
            }
        }

        // 3-Minute Progressive Inactivity Sleep Governor
        sleepGovernor = InactivitySleepGovernor(
            context = applicationContext,
            onSlumberStateChanged = { newStage ->
                renderContext.value = renderContext.value.copy(
                    sleepStage = newStage,
                    state = if (newStage == InactivitySleepStage.DEEP_SLUMBER_0FPS) AvatarState.SLEEPING
                           else if (newStage == InactivitySleepStage.DROWSY_15FPS) AvatarState.DROWSY
                           else renderContext.value.state
                )
                if (newStage == InactivitySleepStage.DEEP_SLUMBER_0FPS) {
                    speechCaption.value = null
                }
            },
            onDeviceShaken = {
                // Device shaken/dropped: playful tumble reaction
                renderContext.value = renderContext.value.copy(
                    situationalEvent = SituationalEvent.PHONE_SHAKE_TUMBLE,
                    state = AvatarState.ALERT
                )
                speechCaption.value = "Whoa! That was quite a shake!"
                serviceScope.launch {
                    delay(2500)
                    renderContext.value = renderContext.value.copy(
                        situationalEvent = SituationalEvent.NONE,
                        state = AvatarState.IDLE
                    )
                    speechCaption.value = null
                }
            }
        )
        sleepGovernor.start()
    }

    private fun setupFloatingWindow() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 80
            y = 350
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(customLifecycleOwner)
            setViewTreeViewModelStoreOwner(customLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(customLifecycleOwner)

            setContent {
                val currentCtx by renderContext.collectAsState()
                val caption by speechCaption.collectAsState()
                val activeTask by activeTaskTitle.collectAsState()

                FloatingCompanionOverlay(
                    renderContext = currentCtx,
                    speechCaption = caption,
                    activeTaskTitle = activeTask,
                    onAvatarTapped = {
                        sleepGovernor.reportUserActivity()
                        renderContext.value = currentCtx.copy(state = AvatarState.IDLE)
                        speechCaption.value = "Yes, sir?"
                    },
                    onDragDelta = { dx, dy ->
                        sleepGovernor.reportUserActivity()
                        layoutParams?.let { lp ->
                            lp.x += dx.toInt()
                            lp.y += dy.toInt()
                            windowManager?.updateViewLayout(this@apply, lp)
                        }
                    },
                    onEmergencyStop = {
                        activeTaskTitle.value = null
                        speechCaption.value = "Automation paused."
                        renderContext.value = currentCtx.copy(state = AvatarState.IDLE)
                    }
                )
            }
        }

        customLifecycleOwner.start()
        windowManager?.addView(composeView, layoutParams)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ShivAI Companion Active Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Runs the floating smart avatar and wake-word listener"
            }
            val mgr = getSystemService(NotificationManager::class.java)
            mgr?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ShivAI Companion Active")
            .setContentText("Listening for 'Om Namas Shivaya' • 3-Min Sleep Governor Active")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        voiceEngine.stopWakeWordListening()
        sleepGovernor.stop()
        customLifecycleOwner.destroy()
        composeView?.let { windowManager?.removeView(it) }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
