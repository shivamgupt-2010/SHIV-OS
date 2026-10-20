package com.example.core.permissions

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import com.example.LauncherActivity
import kotlinx.coroutines.*

/**
 * ShivAIPersistentService:
 * Keeps ShivAI runtime ready for background tasks and multi-agent coordination.
 * Strictly adheres to user privacy: Zero background microphone recording, zero sound effects/beeps.
 * Voice interaction is strictly on-demand when the user taps the mic in ShivAI.
 */
class ShivAIPersistentService : Service() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var partialWakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquirePartialWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1008, notification)
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

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
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
                description = "Keeps ShivAI ready for background tasks and multi-agent coordination"
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
            .setContentTitle("ShivAI Active")
            .setContentText("ShivAI Intelligent System Ready")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}
