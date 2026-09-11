package com.example.ai.shivai.network

import com.example.ai.shivai.model.ShivAIChatRequest
import com.example.ai.shivai.model.ShivAIHealthResponse
import com.example.ai.shivai.model.ShivAIMemoryRequest
import com.example.ai.shivai.model.ShivAIMemoryResponse
import com.example.ai.shivai.model.ShivAIUnifiedResponse
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ShivAIApiService {

    @POST("chat")
    suspend fun chat(
        @Body request: ShivAIChatRequest
    ): ShivAIUnifiedResponse

    @POST("chat/stream")
    @Streaming
    suspend fun chatStream(
        @Body request: ShivAIChatRequest
    ): ResponseBody

    @GET("memory")
    suspend fun listMemories(
        @Query("query") query: String? = null,
        @Query("category") category: String? = null,
        @Query("limit") limit: Int = 50
    ): List<ShivAIMemoryResponse>

    @POST("memory")
    suspend fun remember(
        @Body request: ShivAIMemoryRequest
    ): ShivAIMemoryResponse

    @DELETE("memory/{id}")
    suspend fun forget(
        @Path("id") id: String
    ): ResponseBody

    @GET("health")
    suspend fun health(): ShivAIHealthResponse
}
