package com.example.companion.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.R
import com.example.companion.CompanionActivity
import com.example.companion.auth.ShivAuthGate
import com.example.companion.avatar.*
import com.example.companion.voice.ShivVoiceEngine
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * FloatingAvatarService:
 * Android Foreground Service managing the live floating 3D avatar companion over the OS.
 * Completely hardened against lifecycle and window manager crashes.
 */
class FloatingAvatarService : Service() {

    companion object {
        const val CHANNEL_ID = "shiv_companion_overlay_channel"
        const val NOTIFICATION_ID = 4048
        const val EXTRA_AVATAR_TYPE = "EXTRA_AVATAR_TYPE"

        fun start(context: Context, avatarType: AvatarType? = null) {
            val intent = Intent(context, FloatingAvatarService::class.java).apply {
                avatarType?.let { putExtra(EXTRA_AVATAR_TYPE, it.name) }
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e("FloatingAvatar", "Failed to start FloatingAvatarService: ${e.message}")
            }
        }

        fun stop(context: Context) {
            try {
                context.stopService(Intent(context, FloatingAvatarService::class.java))
            } catch (e: Exception) {
                Log.e("FloatingAvatar", "Failed to stop FloatingAvatarService: ${e.message}")
            }
        }
    }

    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private var windowLayoutParams: WindowManager.LayoutParams? = null

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val avatarRuntime = AvatarRuntimeController()
    private val speechCaption = MutableStateFlow<String?>("Om Namas Shivaya 🙏")
    private val activeTaskTitle = MutableStateFlow<String?>(null)

    private var voiceEngine: ShivVoiceEngine? = null
    private var sleepGovernor: InactivitySleepGovernor? = null
    private var eventResetJob: Job? = null
    private var overlayAttached = false

    private val customLifecycleOwner by lazy { OverlayLifecycleOwner() }

    override fun onCreate() {
        super.onCreate()
        try {
            customLifecycleOwner.onCreate()
            customLifecycleOwner.start()
        } catch (e: Exception) {
            Log.e("FloatingAvatar", "Lifecycle init warning: ${e.message}")
        }

        createNotificationChannel()
        val notification = buildForegroundNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e("FloatingAvatar", "startForeground fallback: ${e.message}")
            startForeground(NOTIFICATION_ID, notification)
        }

        serviceScope.launch {
            ShivAuthGate.verifyAuthentication(applicationContext)
            initVoiceAndSleepGovernor()
            setupFloatingWindow()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra(EXTRA_AVATAR_TYPE)?.let { avatarName ->
            try {
                val chosenType = AvatarType.valueOf(avatarName)
                avatarRuntime.selectAvatar(chosenType)
            } catch (_: Exception) {}
        }
        return START_NOT_STICKY
    }

    private fun initVoiceAndSleepGovernor() {
        try {
            voiceEngine = ShivVoiceEngine(applicationContext)

            sleepGovernor = InactivitySleepGovernor(
                context = applicationContext,
                onSlumberStateChanged = { newStage ->
                    avatarRuntime.applySleepStage(newStage)
                    if (newStage == InactivitySleepStage.DEEP_SLUMBER_0FPS) {
                        speechCaption.value = null
                    }
                },
                onDeviceShaken = {
                    avatarRuntime.showEvent(SituationalEvent.PHONE_SHAKE_TUMBLE)
                    speechCaption.value = "Whoa! That was quite a shake!"
                    eventResetJob?.cancel()
                    eventResetJob = serviceScope.launch {
                        delay(2500)
                        avatarRuntime.clearEvent()
                        speechCaption.value = null
                    }
                }
            )
            sleepGovernor?.start()

            // Sync audio amplitude with avatar mouth phonemes
            voiceEngine?.let { engine ->
                serviceScope.launch {
                    engine.amplitude.collect { amp ->
                        avatarRuntime.updateVoiceAmplitude(amp)
                    }
                }

                // Handle Wake Word "Om Namas Shivaya"
                serviceScope.launch {
                    engine.wakeWordDetected.collect {
                        sleepGovernor?.reportUserActivity()
                        avatarRuntime.reportInteraction(AvatarState.LISTENING)
                        speechCaption.value = "Listening to you, sir..."
                        engine.speak("Om Namas Shivaya. How can I help you today?") {
                            avatarRuntime.setState(AvatarState.IDLE)
                        }
                    }
                }

                engine.startWakeWordListening()
            }
        } catch (e: Exception) {
            Log.e("FloatingAvatar", "Voice/Sleep init warning: ${e.message}")
        }
    }

    private fun setupFloatingWindow() {
        if (overlayAttached) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Log.w("FloatingAvatar", "CanDrawOverlays permission is missing.")
            return
        }

        try {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            windowLayoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 80
                y = 350
            }

            val themeWrapper = ContextThemeWrapper(this, R.style.Theme_MyApplication)
            composeView = ComposeView(themeWrapper).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
                setViewTreeLifecycleOwner(customLifecycleOwner)
                setViewTreeViewModelStoreOwner(customLifecycleOwner)
                setViewTreeSavedStateRegistryOwner(customLifecycleOwner)

                setContent {
                    MaterialTheme {
                        val currentCtx: AvatarRenderContext by avatarRuntime.renderContext.collectAsState()
                        val caption by speechCaption.collectAsState()
                        val activeTask by activeTaskTitle.collectAsState()

                        FloatingCompanionOverlay(
                            renderContext = currentCtx,
                            speechCaption = caption,
                            activeTaskTitle = activeTask,
                            onAvatarTapped = {
                                sleepGovernor?.reportUserActivity()
                                avatarRuntime.reportInteraction(AvatarState.LISTENING)
                                speechCaption.value = "Yes, Shiv? I'm listening..."
                            },
                            onDragDelta = { dx, dy ->
                                sleepGovernor?.reportUserActivity()
                                avatarRuntime.reportInteraction()
                                windowLayoutParams?.let { lp ->
                                    val dm = resources.displayMetrics
                                    val viewW = composeView?.width?.takeIf { it > 0 } ?: 350
                                    val viewH = composeView?.height?.takeIf { it > 0 } ?: 350
                                    val maxX = (dm.widthPixels - viewW).coerceAtLeast(0)
                                    val maxY = (dm.heightPixels - viewH).coerceAtLeast(0)
                                    lp.x = (lp.x + dx.toInt()).coerceIn(0, maxX)
                                    lp.y = (lp.y + dy.toInt()).coerceIn(40, maxY)
                                    composeView?.let { cv ->
                                        try {
                                            windowManager?.updateViewLayout(cv, lp)
                                        } catch (e: Exception) {
                                            Log.e("FloatingAvatar", "updateViewLayout failed: ${e.message}")
                                        }
                                    }
                                }
                            },
                            onEmergencyStop = {
                                activeTaskTitle.value = null
                                speechCaption.value = "Automation paused."
                                avatarRuntime.reportInteraction()
                            },
                            onActionAI = {
                                sleepGovernor?.reportUserActivity()
                                avatarRuntime.reportInteraction(AvatarState.LISTENING)
                                speechCaption.value = "Listening to you, Shiv..."
                                voiceEngine?.speak("I am here, Shiv. What would you like to achieve?") {
                                    avatarRuntime.setState(AvatarState.IDLE)
                                }
                            },
                            onActionCode = {
                                sleepGovernor?.reportUserActivity()
                                avatarRuntime.reportInteraction(AvatarState.WORKING)
                                activeTaskTitle.value = "Coding Pilot Active"
                                speechCaption.value = "Coding Pilot online. Workspace primed."
                                voiceEngine?.speak("Coding Pilot online. Ready to scaffold and debug code.")
                            },
                            onActionMedia = {
                                sleepGovernor?.reportUserActivity()
                                avatarRuntime.reportInteraction(AvatarState.WORKING)
                                activeTaskTitle.value = "Media Matrix Active"
                                speechCaption.value = "Media Engine primed for video & voice."
                                voiceEngine?.speak("Media Matrix online. Ready to assemble video timelines.")
                            },
                            onActionWeb = {
                                sleepGovernor?.reportUserActivity()
                                avatarRuntime.reportInteraction(AvatarState.THINKING)
                                activeTaskTitle.value = "Web Intelligence"
                                speechCaption.value = "Scanning sovereign intelligence network..."
                                voiceEngine?.speak("Web research agent activated.")
                            }
                        )
                    }
                }
            }

            windowManager?.addView(composeView, windowLayoutParams)
            overlayAttached = true
            Log.i("FloatingAvatar", "Overlay attached successfully!")
        } catch (e: Exception) {
            Log.e("FloatingAvatar", "setupFloatingWindow error: ${e.message}")
            try {
                composeView?.disposeComposition()
            } catch (_: Exception) {}
            composeView = null
            overlayAttached = false
        }
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
        val launchIntent = Intent(this, CompanionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ShivAI Companion Active")
            .setContentText("Listening for 'Om Namas Shivaya' • 3-Min Sleep Governor Active")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        eventResetJob?.cancel()
        try {
            voiceEngine?.stopWakeWordListening()
        } catch (_: Exception) {}
        try {
            sleepGovernor?.stop()
        } catch (_: Exception) {}
        try {
            composeView?.let { view ->
                if (overlayAttached) windowManager?.removeViewImmediate(view)
                view.disposeComposition()
            }
        } catch (e: Exception) {
            Log.e("FloatingAvatar", "removeView failed: ${e.message}")
        }
        overlayAttached = false
        composeView = null
        try {
            customLifecycleOwner.destroy()
        } catch (_: Exception) {}
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

class OverlayLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    fun onCreate() {
        if (lifecycleRegistry.currentState == Lifecycle.State.INITIALIZED) {
            try {
                savedStateRegistryController.performAttach()
            } catch (_: Exception) {}
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            try {
                savedStateRegistryController.performRestore(null)
            } catch (_: Exception) {}
        }
    }

    fun start() {
        if (lifecycleRegistry.currentState == Lifecycle.State.CREATED) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        }
        if (lifecycleRegistry.currentState == Lifecycle.State.STARTED) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
    }

    fun pause() {
        if (lifecycleRegistry.currentState == Lifecycle.State.RESUMED) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        }
    }

    fun stop() {
        pause()
        if (lifecycleRegistry.currentState == Lifecycle.State.STARTED) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        }
    }

    fun destroy() {
        stop()
        if (lifecycleRegistry.currentState == Lifecycle.State.CREATED) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }
        store.clear()
    }
}
