package com.example.companion.ui

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.companion.auth.ShivAuthGate
import com.example.companion.avatar.AvatarType

/**
 * CompanionNavHost:
 * Central Navigation Graph for ShivAI Android Companion.
 * Enforces the mandatory Auth Gate before granting access to the Command Deck and Studios.
 */
@Composable
fun CompanionNavHost() {
    val navController = rememberNavController()
    var currentAvatar by remember { mutableStateOf(AvatarType.COSMIC_SHIV) }
    val isAuthenticated by ShivAuthGate.isAuthenticated.collectAsState()

    val startDestination = if (isAuthenticated) "command_deck" else "auth_gate"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("auth_gate") {
            AuthGateScreen(
                onAuthenticated = {
                    navController.navigate("command_deck") {
                        popUpTo("auth_gate") { inclusive = true }
                    }
                }
            )
        }

        composable("command_deck") {
            CommandDeckScreen(
                currentAvatar = currentAvatar,
                onNavigateToWorkflow = { navController.navigate("workflow_lab") },
                onNavigateToStudio = { navController.navigate("persona_studio") },
                onNavigateToMemory = { navController.navigate("knowledge_vault") },
                onNavigateToTelemetry = { navController.navigate("battery_telemetry") }
            )
        }

        composable("workflow_lab") {
            WorkflowLabScreen(onBack = { navController.popBackStack() })
        }

        composable("persona_studio") {
            PersonaStudioScreen(
                currentAvatar = currentAvatar,
                onAvatarSelected = { currentAvatar = it },
                onBack = { navController.popBackStack() }
            )
        }

        composable("knowledge_vault") {
            KnowledgeVaultScreen(onBack = { navController.popBackStack() })
        }

        composable("battery_telemetry") {
            BatteryTelemetryScreen(onBack = { navController.popBackStack() })
        }
    }
}
