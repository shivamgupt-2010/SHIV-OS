package com.example.data.remote.interceptor

import com.example.BuildConfig
import com.example.core.utils.Constants
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Example logic:
        // Automatically inject API key for Gemini or other services if needed
        // Since we are using BuildConfig to fetch API Keys securely:
        val geminiApiKey = BuildConfig.GEMINI_API_KEY
        
        val requestBuilder = originalRequest.newBuilder()
            .header(Constants.Auth.HEADER_AUTHORIZATION, "${Constants.Auth.BEARER}$geminiApiKey")

        return chain.proceed(requestBuilder.build())
    }
}
