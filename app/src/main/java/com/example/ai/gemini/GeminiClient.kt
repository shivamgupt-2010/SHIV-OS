package com.example.ai.gemini

import com.example.BuildConfig
import com.example.core.utils.Logger
import com.example.core.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

class GeminiClient(private val apiService: GeminiApiService) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun generateContent(
        model: String,
        request: GenerateContentRequest
    ): Result<GenerateContentResponse> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                 return@withContext Result.Error(IllegalStateException("Gemini API Key missing!"))
            }

            val response = apiService.generateContent(model, apiKey, request)
            Result.Success(response)
        } catch (e: Exception) {
            Logger.e("Gemini Request Failed", e)
            Result.Error(e)
        }
    }

    fun generateContentStream(
        model: String,
        request: GenerateContentRequest
    ): Flow<Result<String>> = flow {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
             emit(Result.Error(IllegalStateException("Gemini API Key missing!")))
             return@flow
        }

        try {
            val response = apiService.generateContentStream(model, apiKey, request)
            response.byteStream().bufferedReader().use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    try {
                        val parsed = json.parseToJsonElement(line!!).jsonObject
                        val text = parsed["candidates"]?.jsonArray
                            ?.getOrNull(0)?.jsonObject
                            ?.get("content")?.jsonObject
                            ?.get("parts")?.jsonArray
                            ?.getOrNull(0)?.jsonObject
                            ?.get("text")?.jsonPrimitive?.content
                        
                        if (text != null) {
                            emit(Result.Success(text))
                        }
                    } catch (e: Exception) {
                        // ignore broken chunks
                    }
                }
            }
        } catch (e: Exception) {
             Logger.e("Gemini Stream Failed", e)
             emit(Result.Error(e))
        }
    }.flowOn(Dispatchers.IO)
}
