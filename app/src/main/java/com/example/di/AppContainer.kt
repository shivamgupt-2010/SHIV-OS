package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.core.utils.Constants
import com.example.core.utils.DefaultDispatcherProvider
import com.example.core.utils.DispatcherProvider
import com.example.data.local.AppDatabase
import com.example.data.remote.interceptor.AuthInterceptor
import com.example.data.repository.ChatRepositoryImpl
import com.example.domain.repository.ChatRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType

class AppContainer(private val context: Context) {
    
    // Core Dependencies
    val dispatcherProvider: DispatcherProvider by lazy {
        DefaultDispatcherProvider()
    }

    // Database
    val appDatabase: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        )
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
    }

    // Network
    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val authInterceptor: AuthInterceptor by lazy {
        AuthInterceptor()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val networkJson: Json by lazy {
        Json { ignoreUnknownKeys = true }
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(networkJson.asConverterFactory("application/json".toMediaType()))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    // Observability Infrastructure
    val telemetryManager: com.example.core.observability.TelemetryManager by lazy {
        com.example.core.observability.TelemetryManager()
    }

    val runtimeDiagnostics: com.example.core.observability.RuntimeDiagnostics by lazy {
        com.example.core.observability.RuntimeDiagnostics(telemetryManager)
    }

    val performanceMonitor: com.example.core.observability.PerformanceMonitor by lazy {
        com.example.core.observability.PerformanceMonitor(context, telemetryManager)
    }

    val aiExecutionAnalyzer: com.example.core.observability.AIExecutionAnalyzer by lazy {
        com.example.core.observability.AIExecutionAnalyzer(telemetryManager)
    }

    // Contextual Awareness
    val contextAwarenessManager: com.example.core.context.ContextAwarenessManager by lazy {
        com.example.core.context.ContextAwarenessManager(context)
    }

    val screenContextLayer: com.example.core.context.ScreenContextLayer by lazy {
        com.example.core.context.ScreenContextLayer(
            com.example.core.intelligence.ScreenIntelligence.engine,
            ocrIntelligenceEngine,
            formulaRecognitionEngine
        )
    }

    // System Intelligence
    val usagePatternAnalyzer: com.example.core.intelligence.UsagePatternAnalyzer by lazy {
        com.example.core.intelligence.UsagePatternAnalyzer(contextAwarenessManager)
    }

    val eventIntelligenceLayer: com.example.core.intelligence.EventIntelligenceLayer by lazy {
        com.example.core.intelligence.EventIntelligenceLayer(contextAwarenessManager, telemetryManager)
    }

    val launcherCompatibilityLayer: com.example.core.intelligence.LauncherCompatibilityLayer by lazy {
        com.example.core.intelligence.LauncherCompatibilityLayer()
    }

    val ocrIntelligenceEngine: com.example.core.intelligence.OCRIntelligenceEngine by lazy {
        com.example.core.intelligence.OCRIntelligenceEngine()
    }

    val formulaRecognitionEngine: com.example.core.intelligence.FormulaRecognitionEngine by lazy {
        com.example.core.intelligence.FormulaRecognitionEngine()
    }

    // Launcher Systems
    val launcherShellManager: com.example.core.launcher.LauncherShellManager by lazy {
        com.example.core.launcher.LauncherShellManager(context)
    }

    val adaptiveAppPredictor: com.example.core.launcher.AdaptiveAppPredictor by lazy {
        com.example.core.launcher.AdaptiveAppPredictor(
            launcherShellManager,
            contextAwarenessManager,
            usagePatternAnalyzer
        )
    }

    // Repositories
    val chatRepository: ChatRepository by lazy {
        ChatRepositoryImpl(
            chatDao = appDatabase.chatHistoryDao(),
            dispatcherProvider = dispatcherProvider
        )
    }

    // Overlay Assistant
    val overlayAssistantManager: com.example.core.overlay.OverlayAssistantManager by lazy {
        com.example.core.overlay.OverlayAssistantManager(context, aiRuntimeManager, screenContextLayer)
    }

    // ShivAI Cloud Engine
    val shivAIPreferences: com.example.ai.shivai.config.ShivAIPreferences by lazy {
        com.example.ai.shivai.config.ShivAIPreferences(context)
    }

    val shivAIClient: com.example.ai.shivai.client.ShivAIClient by lazy {
        com.example.ai.shivai.client.ShivAIClient(shivAIPreferences)
    }

    val permissionOrchestrator: com.example.core.permissions.PermissionOrchestrator by lazy {
        com.example.core.permissions.PermissionOrchestrator(context)
    }

    val aiRuntimeManager: com.example.core.runtime.AIRuntimeManager by lazy {
        com.example.core.runtime.AIRuntimeManager(context, geminiClient, shivAIClient)
    }

    val safeRuntimeManager: com.example.core.runtime.SafeRuntimeManager by lazy {
        com.example.core.runtime.SafeRuntimeManager(aiRuntimeManager)
    }

    val pdfDocumentationGenerator: com.example.core.usability.PDFDocumentationGenerator by lazy {
        com.example.core.usability.PDFDocumentationGenerator(context)
    }

    // Android Engine & Tools
    val androidIntentEngine: com.example.ai.tool.android.AndroidIntentEngine by lazy {
        com.example.ai.tool.android.AndroidIntentEngine(context)
    }

    val permissionManager: com.example.ai.tool.android.PermissionManager by lazy {
        com.example.ai.tool.android.PermissionManager(context)
    }

    val toolRegistry: com.example.ai.tool.ToolRegistry by lazy {
        com.example.ai.tool.ToolRegistry(
            listOf(
                com.example.ai.tool.android.tools.OpenAppTool(androidIntentEngine),
                com.example.ai.tool.android.tools.SearchWebTool(androidIntentEngine),
                com.example.ai.tool.android.tools.AlarmTool(androidIntentEngine),
                com.example.ai.tool.android.tools.ShareTool(androidIntentEngine),
                com.example.ai.tool.android.tools.ClipboardTool(context),
                com.example.ai.tool.android.tools.NotificationTool(context),
                com.example.ai.tool.android.tools.FileReadTool(context),
                com.example.ai.tool.android.tools.FileWriteTool(context),
                com.example.ai.tool.android.tools.DeepLinkTool(androidIntentEngine)
            )
        )
    }

    val toolSecurityLayer: com.example.ai.tool.security.ToolSecurityLayer by lazy {
        com.example.ai.tool.security.ToolSecurityLayer()
    }

    val toolExecutionManager: com.example.ai.tool.ToolExecutionManager by lazy {
        com.example.ai.tool.ToolExecutionManager(toolRegistry, geminiClient, toolSecurityLayer)
    }

    val workflowManager: com.example.ai.tool.workflow.WorkflowManager by lazy {
        com.example.ai.tool.workflow.WorkflowManager(context)
    }

    // AI Memory & Intelligence Subsystems
    val semanticMemoryRepository: com.example.ai.memory.domain.repository.SemanticMemoryRepository by lazy {
        com.example.ai.memory.domain.repository.SemanticMemoryRepository(
            appDatabase.semanticMemoryDao(),
            memorySecurityProvider,
            shivAIClient = shivAIClient
        )
    }

    val embeddingProvider: com.example.ai.memory.pipeline.EmbeddingProvider by lazy {
        com.example.ai.memory.pipeline.GeminiEmbeddingProvider(geminiApiService)
    }

    val vectorSearchEngine: com.example.ai.memory.pipeline.VectorSearchEngine by lazy {
        com.example.ai.memory.pipeline.VectorSearchEngine()
    }

    val memoryCompressionEngine: com.example.ai.memory.compression.MemoryCompressionEngine by lazy {
        com.example.ai.memory.compression.MemoryCompressionEngine(geminiClient, embeddingProvider, semanticMemoryRepository)
    }

    val memoryLifecycleSystem: com.example.ai.memory.lifecycle.MemoryLifecycleSystem by lazy {
        com.example.ai.memory.lifecycle.MemoryLifecycleSystem(semanticMemoryRepository, memoryCompressionEngine)
    }

    val adaptivePersonalizationEngine: com.example.ai.memory.personalization.AdaptivePersonalizationEngine by lazy {
        com.example.ai.memory.personalization.AdaptivePersonalizationEngine(semanticMemoryRepository, embeddingProvider)
    }

    val memorySecurityProvider: com.example.ai.memory.security.MemorySecurityProvider by lazy {
        com.example.ai.memory.security.MemorySecurityProvider(appDatabase.semanticMemoryDao())
    }

    // AI Workflow & Autonomous Operations Subsystems
    val workflowRepository: com.example.ai.workflow.domain.repository.WorkflowRepository by lazy {
        com.example.ai.workflow.domain.repository.WorkflowRepository(appDatabase.workflowDao())
    }

    // AI Voice & Streaming Subsystems
    val audioResourceManager: com.example.ai.voice.audio.AudioResourceManager by lazy {
        com.example.ai.voice.audio.AudioResourceManager(context)
    }

    val speechToTextEngine: com.example.ai.voice.stt.SpeechToTextEngine by lazy {
        com.example.ai.voice.stt.AndroidSpeechToText(context)
    }

    val textToSpeechEngine: com.example.ai.voice.tts.TextToSpeechEngine by lazy {
        com.example.ai.voice.tts.AndroidTextToSpeech(context)
    }

    val wakeWordEngine: com.example.ai.voice.wake.WakeWordEngine by lazy {
        com.example.ai.voice.wake.WakeWordEngine()
    }

    val voiceSessionManager: com.example.ai.voice.session.VoiceSessionManager by lazy {
        com.example.ai.voice.session.VoiceSessionManager(
            speechToTextEngine,
            textToSpeechEngine,
            audioResourceManager,
            centralOrchestrator // Will instantiate lazy orchestrator
        )
    }

    val safetyManager: com.example.ai.workflow.engine.SafetyManager by lazy {
        com.example.ai.workflow.engine.SafetyManager(context)
    }

    val autonomousWorkflowEngine: com.example.ai.workflow.engine.AutonomousWorkflowEngine by lazy {
        com.example.ai.workflow.engine.AutonomousWorkflowEngine(workflowRepository, toolExecutionManager, safetyManager, centralOrchestrator)
    }

    val cognitiveTaskScheduler: com.example.ai.workflow.scheduler.CognitiveTaskScheduler by lazy {
        com.example.ai.workflow.scheduler.CognitiveTaskScheduler(context)
    }

    val proactiveSuggestionEngine: com.example.ai.workflow.intelligence.ProactiveSuggestionEngine by lazy {
        com.example.ai.workflow.intelligence.ProactiveSuggestionEngine(semanticMemoryRepository)
    }

    val adaptiveRoutineEngine: com.example.ai.workflow.intelligence.AdaptiveRoutineEngine by lazy {
        com.example.ai.workflow.intelligence.AdaptiveRoutineEngine(semanticMemoryRepository)
    }

    val backgroundIntelligenceSystem: com.example.ai.workflow.intelligence.BackgroundIntelligenceSystem by lazy {
        com.example.ai.workflow.intelligence.BackgroundIntelligenceSystem(memoryLifecycleSystem, adaptivePersonalizationEngine, proactiveSuggestionEngine)
    }

    val eventTriggerSystem: com.example.ai.workflow.trigger.EventTriggerSystem by lazy {
        com.example.ai.workflow.trigger.EventTriggerSystem()
    }

    val contextManager: com.example.ai.context.ContextManager by lazy {
        com.example.ai.context.ContextManager(semanticMemoryRepository, embeddingProvider, vectorSearchEngine)
    }

    val geminiApiService: com.example.ai.gemini.GeminiApiService by lazy {
        retrofit.create(com.example.ai.gemini.GeminiApiService::class.java)
    }

    val geminiClient: com.example.ai.gemini.GeminiClient by lazy {
        com.example.ai.gemini.GeminiClient(geminiApiService)
    }

    val chatAgent: com.example.ai.agent.ChatAgent by lazy {
        com.example.ai.agent.ChatAgent(geminiClient, contextManager, chatRepository, toolRegistry, shivAIClient)
    }

    val studyAgent: com.example.ai.agent.StudyAgent by lazy {
        com.example.ai.agent.StudyAgent(geminiClient, contextManager, chatRepository, shivAIClient)
    }

    val codingAgent: com.example.ai.agent.CodingAgent by lazy {
        com.example.ai.agent.CodingAgent(geminiClient, contextManager, chatRepository, shivAIClient)
    }

    val aiStateManager: com.example.ai.state.AIStateManager by lazy {
        com.example.ai.state.AIStateManager()
    }

    val centralOrchestrator: com.example.ai.orchestrator.CentralOrchestrator by lazy {
        com.example.ai.orchestrator.CentralOrchestrator(chatAgent, studyAgent, codingAgent, aiStateManager, toolExecutionManager, shivAIClient)
    }
}
