package com.example.ui.diagnostics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ShivAiApplication
import com.example.ui.theme.MyApplicationTheme

class DiagnosticsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val appContainer = (application as ShivAiApplication).container
        
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    DiagnosticsScreen(
                        runtimeManager = appContainer.aiRuntimeManager,
                        pdfGenerator = appContainer.pdfDocumentationGenerator,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}
