package com.example.ai.shivai.client

import com.example.ai.shivai.config.ShivAIPreferences
import com.example.ai.shivai.model.ShivAIChatRequest
import com.example.ai.shivai.model.ShivAIHealthResponse
import com.example.ai.shivai.model.ShivAIMemoryRequest
import com.example.ai.shivai.model.ShivAIMemoryResponse
import com.example.ai.shivai.model.ShivAIUnifiedResponse
import com.example.ai.shivai.network.ShivAIApiService
import com.example.core.utils.Logger
import com.example.core.utils.Result
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import java.util.UUID
import java.util.concurrent.TimeUnit

class ShivAIClient(
    private val preferences: ShivAIPreferences
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val apiKey = preferences.getApiKey()
        val builder = original.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")

        if (apiKey.isNotBlank()) {
            builder.header("X-API-Key", apiKey)
            builder.header("Authorization", "Bearer $apiKey")
        }
        chain.proceed(builder.build())
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private var cachedBaseUrl: String? = null
    private var cachedService: ShivAIApiService? = null

    private fun getApiService(): ShivAIApiService {
        val currentBaseUrl = preferences.getBaseUrl().let {
            if (!it.endsWith("/")) "$it/" else it
        }

        if (cachedService == null || cachedBaseUrl != currentBaseUrl) {
            cachedBaseUrl = currentBaseUrl
            cachedService = Retrofit.Builder()
                .baseUrl(currentBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(ShivAIApiService::class.java)
        }
        return cachedService!!
    }

    private fun getCoreSdk(): com.shivai.sdk.ShivAIClient {
        return com.shivai.sdk.ShivAIClient(
            apiKey = preferences.getApiKey(),
            baseUrl = preferences.getBaseUrl()
        )
    }

    // Method 2 & Method 5: Chat Turn with Core Reasoning Plan
    suspend fun chat(
        message: String,
        conversationId: String? = null,
        agent: String? = null,
        customInstructions: String? = null,
        preferredProvider: String? = null,
        temperature: Double = 0.7,
        maxTokens: Int = 4096
    ): Result<ShivAIUnifiedResponse> = withContext(Dispatchers.IO) {
        try {
            val answer = getCoreSdk().ask(message)
            Result.Success(
                ShivAIUnifiedResponse(
                    id = UUID.randomUUID().toString(),
                    content = answer,
                    provider = "real-shivai-core",
                    conversationId = conversationId
                )
            )
        } catch (e: Exception) {
            Logger.e("Core Reasoning Failed, trying REST fallback: ${e.message}", e)
            try {
                val req = ShivAIChatRequest(
                    message = message,
                    conversationId = conversationId,
                    agent = agent,
                    customInstructions = customInstructions,
                    preferredProvider = preferredProvider,
                    temperature = temperature,
                    maxTokens = maxTokens,
                    userId = preferences.getUserId()
                )
                val response = getApiService().chat(req)
                Result.Success(response)
            } catch (fallbackEx: Exception) {
                Logger.e("REST fallback also failed", fallbackEx)
                Result.Error(fallbackEx, fallbackEx.message ?: "Chat failed")
            }
        }
    }

    // Method 3: Real-Time Stream Chat delegating to Core Reasoning Engine
    fun streamChat(
        message: String,
        conversationId: String? = null,
        agent: String? = null,
        customInstructions: String? = null,
        preferredProvider: String? = null,
        temperature: Double = 0.7,
        maxTokens: Int = 4096
    ): Flow<Result<String>> = flow {
        try {
            val answer = getCoreSdk().ask(message)
            if (answer.isNotBlank()) {
                emit(Result.Success(answer))
            } else {
                emit(Result.Error(Exception("Empty response"), "AI returned empty response"))
            }
        } catch (e: Exception) {
            Logger.e("Core streamChat error", e)
            emit(Result.Error(e, e.message ?: "Chat request failed"))
        }
    }.flowOn(Dispatchers.IO)

    // Method 4: Long-Term Memory (Remember)
    suspend fun remember(
        key: String,
        value: String,
        category: String = "general",
        importance: Double = 0.5
    ): Result<ShivAIMemoryResponse> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject()
                .put("value", value)
                .put("category", category)
                .put("importance", importance)
                .toString()
            getCoreSdk().memory.store(key, payload)
            Result.Success(
                ShivAIMemoryResponse(
                    id = "mem_${System.currentTimeMillis()}",
                    key = key,
                    value = value,
                    category = category,
                    importance = importance
                )
            )
        } catch (e: Exception) {
            Logger.e("ShivAI Memory Remember Failed", e)
            Result.Error(e, e.message ?: "Failed to remember fact")
        }
    }

    // Method 4: Long-Term Memory (Recall)
    suspend fun recall(
        query: String? = null,
        category: String? = null,
        limit: Int = 50
    ): Result<List<ShivAIMemoryResponse>> = withContext(Dispatchers.IO) {
        try {
            val targetKey = query ?: category ?: "default"
            val res = getCoreSdk().memory.retrieve(targetKey)
            val json = JSONObject(res)
            val dataObj = json.optJSONObject("data")
            val memories = mutableListOf<ShivAIMemoryResponse>()
            if (dataObj != null) {
                memories.add(
                    ShivAIMemoryResponse(
                        id = "mem_retrieved",
                        key = targetKey,
                        value = dataObj.toString()
                    )
                )
            }
            Result.Success(memories)
        } catch (e: Exception) {
            Logger.e("ShivAI Memory Recall Failed", e)
            Result.Error(e, e.message ?: "Failed to recall memories")
        }
    }

    // Method 4: Long-Term Memory (Forget)
    suspend fun forget(memoryId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            getCoreSdk().actions.execute("memory_forget", """{"id":"$memoryId"}""")
            Result.Success(true)
        } catch (e: Exception) {
            Logger.e("ShivAI Memory Forget Failed", e)
            Result.Error(e, e.message ?: "Failed to forget memory")
        }
    }

    // Method 5: Multi-Agent Task Dispatch with DAG Execution
    suspend fun dispatchTask(task: String, context: JSONObject = JSONObject()): Result<com.shivai.sdk.ShivAITaskResult> = withContext(Dispatchers.IO) {
        try {
            val result = getCoreSdk().dispatchTask(task, context)
            Result.Success(result)
        } catch (e: Exception) {
            Logger.e("ShivAI Task Dispatch Failed", e)
            Result.Error(e, e.message ?: "Task dispatch failed")
        }
    }

    // Method 6: Web Research & Synthesis
    suspend fun research(query: String): Result<com.shivai.sdk.ShivAISearchResult> = withContext(Dispatchers.IO) {
        try {
            val result = getCoreSdk().research(query)
            Result.Success(result)
        } catch (e: Exception) {
            Logger.e("ShivAI Research Failed", e)
            Result.Error(e, e.message ?: "Research failed")
        }
    }

    // Method 7: Step-by-Step Task Progress Stream
    fun streamTaskSteps(prompt: String, context: JSONObject = JSONObject()): Flow<com.shivai.sdk.DAGStep> {
        return getCoreSdk().streamTaskSteps(prompt, context)
    }

    // Method 8: Direct Action Execution
    suspend fun executeAction(actionType: String, payload: JSONObject = JSONObject()): Result<String> = withContext(Dispatchers.IO) {
        try {
            val res = getCoreSdk().actions.execute(actionType, payload.toString())
            Result.Success(res)
        } catch (e: Exception) {
            Logger.e("ShivAI Action Execution Failed", e)
            Result.Error(e, e.message ?: "Action execution failed")
        }
    }

    // Method 9: System State & Diagnostics
    suspend fun getSystemState(): Result<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val raw = getCoreSdk().diagnostics.getState()
            Result.Success(JSONObject(raw))
        } catch (e: Exception) {
            Logger.e("ShivAI State Fetch Failed", e)
            Result.Error(e, e.message ?: "State fetch failed")
        }
    }

    // Cloud Health & Latency Test
    suspend fun checkHealth(): Result<Pair<ShivAIHealthResponse, Long>> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val healthRaw = getCoreSdk().diagnostics.ping()
            val latency = System.currentTimeMillis() - startTime
            val healthJson = try { JSONObject(healthRaw) } catch (ignored: Exception) { null }
            val status = healthJson?.optString("status", "ok") ?: "ok"
            Result.Success(
                Pair(
                    ShivAIHealthResponse(
                        status = status,
                        service = "real-shivai-cognitive-core",
                        version = "1.0.0"
                    ),
                    latency
                )
            )
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            Logger.e("ShivAI Health Check Failed (${latency}ms)", e)
            Result.Error(e, e.message ?: "Health check failed")
        }
    }
}
