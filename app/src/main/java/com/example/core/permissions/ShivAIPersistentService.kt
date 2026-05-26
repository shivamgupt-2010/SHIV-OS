package com.example.core.permissions

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ShivAIPersistentService : Service() {

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1, notification)
        
        scope.launch {
            // Keep AI state active
            // Monitor device triggers
        }
        
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "shivai_persistent",
                "ShivAI Persistent Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps AI operations running in background"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "shivai_persistent")
            .setContentTitle("ShivAI Active")
            .setContentText("Listening for contextual triggers.")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now) // Temporary icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
