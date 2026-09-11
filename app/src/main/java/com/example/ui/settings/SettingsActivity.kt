package com.example.ui.settings

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ShivAiApplication
import com.example.ui.theme.MyApplicationTheme

class SettingsActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val appContainer = (application as ShivAiApplication).container
        appContainer.permissionOrchestrator.refreshPermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val appContainer = (application as ShivAiApplication).container
        val permissionOrchestrator = appContainer.permissionOrchestrator
        
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SettingsScreen(
                        permissionOrchestrator = permissionOrchestrator,
                        shivAIPreferences = appContainer.shivAIPreferences,
                        shivAIClient = appContainer.shivAIClient,
                        onNavigateBack = { finish() },
                        onNavigateDiagnostics = {
                            startActivity(Intent(this@SettingsActivity, com.example.ui.diagnostics.DiagnosticsActivity::class.java))
                        },
                        onRequestUsageStats = {
                            try { startActivity(permissionOrchestrator.getUsageStatsIntent()) }
                            catch (e: Exception) { e.printStackTrace() }
                        },
                        onRequestOverlay = {
                            try { startActivity(permissionOrchestrator.getOverlayIntent()) }
                            catch (e: Exception) { e.printStackTrace() }
                        },
                        onRequestAccessibility = {
                            try { startActivity(permissionOrchestrator.getAccessibilityIntent()) }
                            catch (e: Exception) { e.printStackTrace() }
                        },
                        onRequestNotification = {
                            try { startActivity(permissionOrchestrator.getNotificationListenerIntent()) }
                            catch (e: Exception) { e.printStackTrace() }
                        },
                        onRequestBasicPermissions = {
                            permissionLauncher.launch(arrayOf(
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.READ_CONTACTS,
                                Manifest.permission.READ_CALENDAR,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ))
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val appContainer = (application as ShivAiApplication).container
        appContainer.permissionOrchestrator.refreshPermissions()
    }
}
