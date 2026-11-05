package com.example.companion.auth

import android.content.Context
import android.net.Uri
import com.example.auth.ShivIdentityAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * ShivAuthGate:
 * Strict Security Gate for ShivAI Android Companion.
 * Enforces the rule: "No user can use ShivAI without being authenticated".
 * Probes local session first, then queries the ShivMesh Auth Provider.
 */
object ShivAuthGate {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _authenticatedUser = MutableStateFlow<String?>("SHIV_User")
    val authenticatedUser: StateFlow<String?> = _authenticatedUser.asStateFlow()

    /**
     * Verifies session validity across local memory and the ShivMesh cross-app bus.
     */
    suspend fun verifyAuthentication(context: Context): Boolean = withContext(Dispatchers.IO) {
        // 1. Check local in-memory / SharedPreferences session
        val localToken = ShivIdentityAuth.accessToken
        val authState = ShivIdentityAuth.authState.value

        if (authState is ShivIdentityAuth.OAuthState.Authenticated) {
            _isAuthenticated.value = true
            _authenticatedUser.value = authState.username ?: "SHIV_User"
            return@withContext true
        }

        if (!localToken.isNullOrBlank()) {
            _isAuthenticated.value = true
            return@withContext true
        }

        // 2. Query ShivMesh ContentProvider for zero-touch cross-app sync
        try {
            val cursor = context.contentResolver.query(
                ShivMeshAuthProvider.CONTENT_URI,
                null,
                null,
                null,
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val isAuthed = it.getInt(it.getColumnIndexOrThrow("is_authenticated")) == 1
                    if (isAuthed) {
                        val token = it.getString(it.getColumnIndexOrThrow("access_token"))
                        val user = it.getString(it.getColumnIndexOrThrow("username"))
                        if (!token.isNullOrBlank()) {
                            _isAuthenticated.value = true
                            _authenticatedUser.value = user
                            return@withContext true
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // ContentProvider not present or signature mismatch
        }

        _isAuthenticated.value = false
        false
    }

    fun setAuthenticated(username: String, token: String) {
        _isAuthenticated.value = true
        _authenticatedUser.value = username
    }

    fun logout() {
        _isAuthenticated.value = false
        _authenticatedUser.value = null
    }
}
