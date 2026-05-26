package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.di.AppContainer
import com.example.ui.conversation.ChatScreen
import com.example.ui.conversation.ChatViewModel
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.memory.MemoryScreen
import com.example.ui.memory.MemoryViewModel
import com.example.ui.workflow.WorkflowScreen
import com.example.ui.workflow.WorkflowViewModel

@Composable
fun MainNavGraph(
    appContainer: AppContainer,
    navController: NavHostController = rememberNavController(),
    startDestination: String = "dashboard"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("dashboard") {
            val viewModel: DashboardViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return DashboardViewModel(
                            appContainer.appDatabase.workflowDao(),
                            appContainer.appDatabase.memoryDao()
                        ) as T
                    }
                }
            )
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToChat = { navController.navigate("chat") },
                onNavigateToWorkflows = { navController.navigate("workflows") },
                onNavigateToMemories = { navController.navigate("memories") }
            )
        }
        
        composable("chat") {
            val viewModel: ChatViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return ChatViewModel(
                            appContainer.centralOrchestrator,
                            appContainer.voiceSessionManager,
                            appContainer.appDatabase.chatHistoryDao()
                        ) as T
                    }
                }
            )
            ChatScreen(viewModel = viewModel)
        }
        
        composable("workflows") {
            val viewModel: WorkflowViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return WorkflowViewModel(appContainer.appDatabase.workflowDao()) as T
                    }
                }
            )
            WorkflowScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        
        composable("memories") {
            val viewModel: MemoryViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return MemoryViewModel(appContainer.semanticMemoryRepository) as T
                    }
                }
            )
            MemoryScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}
