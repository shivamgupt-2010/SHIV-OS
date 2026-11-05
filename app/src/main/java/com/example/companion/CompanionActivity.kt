package com.example.companion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.companion.ui.CompanionNavHost

/**
 * CompanionActivity:
 * Standalone Android Entry Point for the ShivAI Digital Assistant & Operating Layer.
 * Houses the Auth Gate, Command Deck, Workflow Lab, Persona Studio, and Battery Telemetry.
 */
class CompanionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF020617)
            ) {
                CompanionNavHost()
            }
        }
    }
}
