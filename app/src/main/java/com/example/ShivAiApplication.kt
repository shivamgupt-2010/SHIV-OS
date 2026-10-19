package com.example

import android.app.Application
import android.os.Build
import android.util.Log
import com.example.di.AppContainer
import com.shivai.sdk.ShivAIClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShivAiApplication : Application() {
    lateinit var container: AppContainer

    companion object {
        lateinit var shivAI: ShivAIClient
    }

    override fun onCreate() {
        super.onCreate()
        
        // Official ShivAI Core SDK Bootstrap
        shivAI = ShivAIClient(
            apiKey = "shivai_launcher_auth_key_v1",
            baseUrl = "https://real-shivai.onrender.com"
        )

        container = AppContainer(this)

        // Ping Core and register boot event
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val health = shivAI.diagnostics.ping()
                Log.i("ShivAI", "Connected to ShivAI Cognitive Core: $health")
                shivAI.analytics.logEvent(
                    "LAUNCHER_BOOT", 
                    """{"os_version": ${Build.VERSION.SDK_INT}, "timestamp": ${System.currentTimeMillis()}}"""
                )
            } catch (e: Exception) {
                Log.w("ShivAI", "ShivAI Core offline or starting: ${e.message}")
            }
        }
    }
}
