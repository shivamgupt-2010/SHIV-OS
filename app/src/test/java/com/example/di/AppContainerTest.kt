package com.example.di

import android.content.Context
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import androidx.test.core.app.ApplicationProvider

@RunWith(RobolectricTestRunner::class)
class AppContainerTest {

    @Test
    fun testAppContainerInitialization() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val container = AppContainer(context)
        
        // Access a few lazy properties to trigger initialization
        val db = container.appDatabase
        val orchestrator = container.centralOrchestrator
        val chatAgent = container.chatAgent
        val voiceSessionManager = container.voiceSessionManager
        val dashboardDao = container.appDatabase.workflowDao()
        println("AppContainer initialized successfully!")
    }
}
