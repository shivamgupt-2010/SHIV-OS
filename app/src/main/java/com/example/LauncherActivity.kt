package com.example

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.launcher.LauncherScreen
import com.example.ui.launcher.LauncherViewModel
import com.example.ui.theme.MyApplicationTheme

class LauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val appContainer = (application as ShivAiApplication).container
                
                val viewModel: LauncherViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return LauncherViewModel(
                                appContainer.launcherShellManager,
                                appContainer.adaptiveAppPredictor,
                                appContainer.appDatabase.workflowDao(),
                                appContainer.telemetryManager
                            ) as T
                        }
                    }
                )
                
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val prefs = getSharedPreferences("shivai_prefs", android.content.Context.MODE_PRIVATE)
                    val hasSeenOnboardingState = androidx.compose.runtime.remember {
                        androidx.compose.runtime.mutableStateOf(prefs.getBoolean("has_seen_onboarding", false))
                    }

                    if (!hasSeenOnboardingState.value) {
                        com.example.ui.onboarding.OnboardingScreen(onComplete = {
                            prefs.edit().putBoolean("has_seen_onboarding", true).apply()
                            hasSeenOnboardingState.value = true
                        })
                    } else {
                        LauncherScreen(
                            viewModel = viewModel,
                            onOpenAssistant = {
                                try {
                                    if (android.provider.Settings.canDrawOverlays(this@LauncherActivity)) {
                                        appContainer.overlayAssistantManager.showOverlay()
                                    } else {
                                        val intent = Intent(this@LauncherActivity, com.example.ui.settings.SettingsActivity::class.java)
                                        startActivity(intent)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            onOpenSettings = {
                                try {
                                    val intent = Intent(this@LauncherActivity, com.example.ui.settings.SettingsActivity::class.java)
                                    startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
