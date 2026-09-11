package com.example.ai.shivai.config

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ShivAIPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _baseUrl = MutableStateFlow(getBaseUrl())
    val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()

    private val _userId = MutableStateFlow(getUserId())
    val userId: StateFlow<String> = _userId.asStateFlow()

    private val _apiKey = MutableStateFlow(getApiKey())
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    fun getBaseUrl(): String {
        val url = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        return url.trimEnd('/')
    }

    fun setBaseUrl(url: String) {
        val sanitized = url.trim().trimEnd('/')
        prefs.edit().putString(KEY_BASE_URL, sanitized).apply()
        _baseUrl.value = sanitized
    }

    fun getUserId(): String {
        return prefs.getString(KEY_USER_ID, DEFAULT_USER_ID) ?: DEFAULT_USER_ID
    }

    fun setUserId(id: String) {
        val sanitized = id.trim()
        prefs.edit().putString(KEY_USER_ID, sanitized).apply()
        _userId.value = sanitized
    }

    fun getApiKey(): String {
        return prefs.getString(KEY_API_KEY, DEFAULT_API_KEY) ?: DEFAULT_API_KEY
    }

    fun setApiKey(key: String) {
        val sanitized = key.trim()
        prefs.edit().putString(KEY_API_KEY, sanitized).apply()
        _apiKey.value = sanitized
    }

    companion object {
        private const val PREFS_NAME = "shivai_cloud_preferences"
        private const val KEY_BASE_URL = "shivai_base_url"
        private const val KEY_USER_ID = "shivai_user_id"
        private const val KEY_API_KEY = "shivai_api_key"

        const val DEFAULT_BASE_URL = "https://shivai-backend.onrender.com/api/v1"
        const val DEFAULT_USER_ID = "shivam"
        const val DEFAULT_API_KEY = "shivai-production-key-2026"
    }
}
