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
        handleAuthRedirect(intent)
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
                                appContainer.telemetryManager,
                                appContainer.layoutManager,
                                appContainer.studioThemeEngine,
                                appContainer.appLockManager,
                                appContainer.wellbeingManager,
                                appContainer.familyManager
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
                                appContainer.launcherShellManager,
                                appContainer.shivAIActionDispatcher
                            ) as T
                        }
                    }
                )

                val prefs = getSharedPreferences("shivai_prefs", android.content.Context.MODE_PRIVATE)
                val hasSeenOnboardingState = remember {
                    mutableStateOf(prefs.getBoolean("has_seen_onboarding", false))
                }

                if (!hasSeenOnboardingState.value) {
                    com.example.ui.onboarding.OnboardingScreen(
                        layoutManager = appContainer.layoutManager,
                        themeEngine = appContainer.studioThemeEngine,
                        onComplete = {
                            prefs.edit().putBoolean("has_seen_onboarding", true).apply()
                            hasSeenOnboardingState.value = true
                        }
                    )
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
                                        onBack = { currentRouteFlow.value = "launcher" },
                                        onNavigate = { currentRouteFlow.value = it }
                                    )
                                    "studio" -> com.example.ui.studio.StudioScreen(
                                        themeEngine = appContainer.studioThemeEngine,
                                        layoutManager = appContainer.layoutManager,
                                        onBack = { currentRouteFlow.value = "launcher" }
                                    )
                                    "lock" -> com.example.ui.lock.AppLockScreen(
                                        appLockManager = appContainer.appLockManager,
                                        installedApps = launcherViewModel.installedApps.collectAsState().value,
                                        onBack = { currentRouteFlow.value = "launcher" }
                                    )
                                    "wellbeing" -> com.example.ui.wellbeing.WellbeingScreen(
                                        wellbeingManager = appContainer.wellbeingManager,
                                        installedApps = launcherViewModel.installedApps.collectAsState().value,
                                        onBack = { currentRouteFlow.value = "launcher" }
                                    )
                                    "family" -> com.example.ui.family.FamilyScreen(
                                        familyManager = appContainer.familyManager,
                                        installedApps = launcherViewModel.installedApps.collectAsState().value,
                                        onBack = { currentRouteFlow.value = "launcher" }
                                    )
                                    "memory" -> com.example.ui.memory.MemoryScreen(
                                        viewModel = com.example.ui.memory.MemoryViewModel(appContainer.semanticMemoryRepository),
                                        onBack = { currentRouteFlow.value = "launcher" }
                                    )
                                    else -> LauncherScreen(
                                        viewModel = launcherViewModel,
                                        onNavigate = { currentRouteFlow.value = it },
                                        onOpenSidebar = { coroutineScope.launch { drawerState.open() } }
                                    )
                                }
                            }

                            // 1. App Lock Prompt Dialog
                            val lockedPkg by launcherViewModel.pendingAppLock.collectAsState()
                            if (lockedPkg != null) {
                                AlertDialog(
                                    onDismissRequest = { launcherViewModel.dismissPrompts() },
                                    icon = { Icon(androidx.compose.material.icons.Icons.Default.Info, contentDescription = null, tint = com.example.ui.theme.ShivaCyan) },
                                    title = { Text("App Protected by SHIV Lock") },
                                    text = { Text("Authentication required to launch this application.", style = MaterialTheme.typography.bodyMedium) },
                                    confirmButton = {
                                        Button(onClick = { launcherViewModel.unlockAndLaunch(lockedPkg!!) }) {
                                            Text("Authenticate & Launch")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { launcherViewModel.dismissPrompts() }) {
                                            Text("Cancel")
                                        }
                                    }
                                )
                            }

                            // 2. Intentional Opening Friction Dialog
                            val frictionPkg by launcherViewModel.pendingIntentionalPrompt.collectAsState()
                            if (frictionPkg != null) {
                                AlertDialog(
                                    onDismissRequest = { launcherViewModel.dismissPrompts() },
                                    icon = { Icon(androidx.compose.material.icons.Icons.Default.Star, contentDescription = null, tint = com.example.ui.theme.ShivaCyan) },
                                    title = { Text("Mindful Pause") },
                                    text = { Text("Take a deep breath. Are you opening this app intentionally or by habit?", style = MaterialTheme.typography.bodyMedium) },
                                    confirmButton = {
                                        Button(onClick = { launcherViewModel.confirmIntentionalLaunch(frictionPkg!!) }) {
                                            Text("Open App")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { launcherViewModel.dismissPrompts() }) {
                                            Text("Put Down Phone")
                                        }
                                    }
                                )
                            }

                            // 3. Family Supervision Restriction Dialog
                            val familyBlockedPkg by launcherViewModel.pendingFamilyRestriction.collectAsState()
                            if (familyBlockedPkg != null) {
                                AlertDialog(
                                    onDismissRequest = { launcherViewModel.dismissPrompts() },
                                    icon = { Icon(androidx.compose.material.icons.Icons.Default.Person, contentDescription = null, tint = com.example.ui.theme.ShivaOrange) },
                                    title = { Text("Restricted by SHIV Family") },
                                    text = { Text("This application is currently restricted under child supervision or active bedtime curfew.", style = MaterialTheme.typography.bodyMedium) },
                                    confirmButton = {
                                        Button(onClick = { launcherViewModel.dismissPrompts() }) {
                                            Text("OK")
                                        }
                                    }
                                )
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
        handleAuthRedirect(intent)
        intent.getStringExtra("EXTRA_TARGET_SCREEN")?.let {
            currentRouteFlow.value = it
        }
    }

    private fun handleAuthRedirect(intent: Intent?) {
        val uri = intent?.data
        if (uri != null && uri.scheme == "shivlauncher" && uri.host == "oauth" && uri.path == "/callback") {
            com.example.auth.ShivIdentityAuth.handleRedirect(this, uri) { success, result ->
                runOnUiThread {
                    if (success) {
                        android.widget.Toast.makeText(this, "SHIV Identity authenticated successfully!", android.widget.Toast.LENGTH_LONG).show()
                    } else {
                        android.widget.Toast.makeText(this, "Authentication failed: $result", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
