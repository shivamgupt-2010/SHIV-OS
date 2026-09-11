package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.ui.agents.AgentsScreen
import com.example.ui.conversation.ChatScreen
import com.example.ui.conversation.ChatViewModel
import com.example.ui.drawer.SidebarDrawer
import com.example.ui.launcher.AppDrawerScreen
import com.example.ui.launcher.LauncherScreen
import com.example.ui.launcher.LauncherViewModel
import com.example.ui.notes.NotesScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.tools.ToolsScreen

class LauncherActivity : ComponentActivity() {
    private val currentRouteFlow = kotlinx.coroutines.flow.MutableStateFlow("launcher")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            val serviceIntent = Intent(this, com.example.core.permissions.ShivAIPersistentService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        intent?.getStringExtra("EXTRA_TARGET_SCREEN")?.let {
            currentRouteFlow.value = it
        }

        setContent {
            MyApplicationTheme {
                val appContainer = (application as ShivAiApplication).container
                val coroutineScope = rememberCoroutineScope()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

                val launcherViewModel: LauncherViewModel = viewModel(
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

                val chatViewModel: ChatViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return ChatViewModel(
                                appContainer.centralOrchestrator,
                                appContainer.voiceSessionManager,
                                appContainer.appDatabase.chatHistoryDao(),
                                appContainer.launcherShellManager
                            ) as T
                        }
                    }
                )

                val prefs = getSharedPreferences("shivai_prefs", android.content.Context.MODE_PRIVATE)
                val hasSeenOnboardingState = remember {
                    mutableStateOf(prefs.getBoolean("has_seen_onboarding", false))
                }

                if (!hasSeenOnboardingState.value) {
                    com.example.ui.onboarding.OnboardingScreen(onComplete = {
                        prefs.edit().putBoolean("has_seen_onboarding", true).apply()
                        hasSeenOnboardingState.value = true
                    })
                } else {
                    val currentRoute by currentRouteFlow.collectAsState()

                    BackHandler(enabled = drawerState.isOpen || currentRoute != "launcher") {
                        if (drawerState.isOpen) {
                            coroutineScope.launch { drawerState.close() }
                        } else {
                            currentRouteFlow.value = "launcher"
                        }
                    }

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            SidebarDrawer(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    if (route == "settings") {
                                        startActivity(Intent(this@LauncherActivity, com.example.ui.settings.SettingsActivity::class.java))
                                    } else {
                                        currentRouteFlow.value = route
                                    }
                                },
                                onClose = { coroutineScope.launch { drawerState.close() } }
                            )
                        }
                    ) {
                        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                            AnimatedContent(
                                targetState = currentRoute,
                                label = "ScreenTransition"
                            ) { route ->
                                when (route) {
                                    "launcher" -> LauncherScreen(
                                        viewModel = launcherViewModel,
                                        onNavigate = { nextRoute ->
                                            if (nextRoute == "settings") {
                                                startActivity(Intent(this@LauncherActivity, com.example.ui.settings.SettingsActivity::class.java))
                                            } else {
                                                currentRouteFlow.value = nextRoute
                                            }
                                        },
                                        onOpenSidebar = { coroutineScope.launch { drawerState.open() } }
                                    )
                                    "chat" -> ChatScreen(
                                        viewModel = chatViewModel,
                                        onBack = { currentRouteFlow.value = "launcher" }
                                    )
                                    "agents" -> AgentsScreen(
                                        onSelectAgent = { _ -> currentRouteFlow.value = "chat" },
                                        onNavigate = { nextRoute ->
                                            if (nextRoute == "settings") {
                                                startActivity(Intent(this@LauncherActivity, com.example.ui.settings.SettingsActivity::class.java))
                                            } else {
                                                currentRouteFlow.value = nextRoute
                                            }
                                        },
                                        onOpenSidebar = { coroutineScope.launch { drawerState.open() } }
                                    )
                                    "notes" -> NotesScreen(
                                        onNavigate = { nextRoute ->
                                            if (nextRoute == "settings") {
                                                startActivity(Intent(this@LauncherActivity, com.example.ui.settings.SettingsActivity::class.java))
                                            } else {
                                                currentRouteFlow.value = nextRoute
                                            }
                                        },
                                        onOpenSidebar = { coroutineScope.launch { drawerState.open() } }
                                    )
                                    "tools" -> ToolsScreen(
                                        onNavigate = { nextRoute ->
                                            if (nextRoute == "settings") {
                                                startActivity(Intent(this@LauncherActivity, com.example.ui.settings.SettingsActivity::class.java))
                                            } else {
                                                currentRouteFlow.value = nextRoute
                                            }
                                        },
                                        onOpenSidebar = { coroutineScope.launch { drawerState.open() } }
                                    )
                                    "all_apps" -> AppDrawerScreen(
                                        viewModel = launcherViewModel,
                                        onBack = { currentRouteFlow.value = "launcher" }
                                    )
                                    else -> LauncherScreen(
                                        viewModel = launcherViewModel,
                                        onNavigate = { currentRouteFlow.value = it },
                                        onOpenSidebar = { coroutineScope.launch { drawerState.open() } }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra("EXTRA_TARGET_SCREEN")?.let {
            currentRouteFlow.value = it
        }
    }
}
