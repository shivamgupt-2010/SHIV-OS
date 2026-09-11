package com.example.ai.shivai.client

import com.example.ai.shivai.config.ShivAIPreferences
import com.example.ai.shivai.model.ShivAIChatRequest
import com.example.ai.shivai.model.ShivAIHealthResponse
import com.example.ai.shivai.model.ShivAIMemoryRequest
import com.example.ai.shivai.model.ShivAIMemoryResponse
import com.example.ai.shivai.model.ShivAIStreamChunk
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
import retrofit2.Retrofit
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
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
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

    // Method 2 & Method 5: Chat Turn
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
        } catch (e: Exception) {
            Logger.e("ShivAI Chat Request Failed", e)
            Result.Error(e, e.message ?: "Chat failed")
        }
    }

    // Method 3: Real-Time Word-by-Word Streaming (SSE) with Non-Streaming Fallback
    fun streamChat(
        message: String,
        conversationId: String? = null,
        agent: String? = null,
        customInstructions: String? = null,
        preferredProvider: String? = null,
        temperature: Double = 0.7,
        maxTokens: Int = 4096
    ): Flow<Result<String>> = flow {
        var emittedAny = false
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
            val responseBody = getApiService().chatStream(req)
            responseBody.byteStream().bufferedReader().use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line?.trim() ?: continue
                    if (currentLine.isEmpty() || !currentLine.startsWith("data:")) {
                        continue
                    }
                    val dataStr = currentLine.removePrefix("data:").trim()
                    if (dataStr == "[DONE]") {
                        break
                    }
                    try {
                        val element = json.parseToJsonElement(dataStr)
                        if (element is kotlinx.serialization.json.JsonObject) {
                            val delta = element["delta"]?.let {
                                if (it is kotlinx.serialization.json.JsonPrimitive) it.content else it.toString()
                            } ?: element["content"]?.let {
                                if (it is kotlinx.serialization.json.JsonPrimitive) it.content else it.toString()
                            } ?: ""

                            if (delta.isNotEmpty()) {
                                emittedAny = true
                                emit(Result.Success(delta))
                            }
                        }
                    } catch (e: Exception) {
                        Logger.d("Skipping unparseable SSE chunk: $dataStr, $e")
                    }
                }
            }
        } catch (e: Exception) {
            Logger.e("ShivAI Chat Stream Failed, trying fallback: ${e.message}", e)
        }

        // Robust Fallback: If streaming yielded nothing or failed, call non-streaming chat()!
        if (!emittedAny) {
            try {
                Logger.d("SSE stream yielded no tokens, falling back to non-streaming chat...")
                val fallbackRes = chat(
                    message = message,
                    conversationId = conversationId,
                    agent = agent,
                    customInstructions = customInstructions,
                    preferredProvider = preferredProvider,
                    temperature = temperature,
                    maxTokens = maxTokens
                )
                when (fallbackRes) {
                    is Result.Success -> {
                        if (fallbackRes.data.content.isNotEmpty()) {
                            emit(Result.Success(fallbackRes.data.content))
                        } else {
                            emit(Result.Error(Exception("Empty AI response"), "AI returned empty response"))
                        }
                    }
                    is Result.Error -> {
                        emit(Result.Error(fallbackRes.exception, fallbackRes.message))
                    }
                    else -> {}
                }
            } catch (fallbackEx: Exception) {
                Logger.e("Fallback chat also failed", fallbackEx)
                emit(Result.Error(fallbackEx, fallbackEx.message ?: "Chat failed"))
            }
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
            val req = ShivAIMemoryRequest(
                key = key,
                value = value,
                category = category,
                importance = importance
            )
            val response = getApiService().remember(req)
            Result.Success(response)
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
            val response = getApiService().listMemories(query = query, category = category, limit = limit)
            Result.Success(response)
        } catch (e: Exception) {
            Logger.e("ShivAI Memory Recall Failed", e)
            Result.Error(e, e.message ?: "Failed to recall memories")
        }
    }

    // Method 4: Long-Term Memory (Forget)
    suspend fun forget(memoryId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            getApiService().forget(memoryId)
            Result.Success(true)
        } catch (e: Exception) {
            Logger.e("ShivAI Memory Forget Failed", e)
            Result.Error(e, e.message ?: "Failed to forget memory")
        }
    }

    // Cloud Health & Latency Test
    suspend fun checkHealth(): Result<Pair<ShivAIHealthResponse, Long>> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val health = getApiService().health()
            val latency = System.currentTimeMillis() - startTime
            Result.Success(Pair(health, latency))
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            Logger.e("ShivAI Health Check Failed (${latency}ms)", e)
            Result.Error(e, e.message ?: "Health check failed")
        }
    }
}
