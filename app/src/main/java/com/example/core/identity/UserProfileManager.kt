package com.example.core.identity

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserProfile(
    val displayName: String = "Shivam Gupta",
    val bio: String = "Creator • Builder • Dreamer",
    val email: String = "shivam@shivai.com",
    val photoUri: String? = null,
    val isAuthenticated: Boolean = false,
    val authMethod: String = "LOCAL_SECURE",
    val updatedAt: Long = System.currentTimeMillis()
)

class UserProfileManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("shiv_user_profile_prefs", Context.MODE_PRIVATE)

    private val _profile = MutableStateFlow(loadProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private fun loadProfile(): UserProfile {
        val isAuth = prefs.getBoolean("is_authenticated", false)
        val name = prefs.getString("display_name", "Shivam Gupta") ?: "Shivam Gupta"
        val bio = prefs.getString("bio", "Creator • Builder • Dreamer") ?: "Creator • Builder • Dreamer"
        val email = prefs.getString("email", "shivam@shivai.com") ?: "shivam@shivai.com"
        val photo = prefs.getString("photo_uri", null)
        val method = prefs.getString("auth_method", "LOCAL_SECURE") ?: "LOCAL_SECURE"
        val updated = prefs.getLong("updated_at", System.currentTimeMillis())

        return UserProfile(
            displayName = name,
            bio = bio,
            email = email,
            photoUri = photo,
            isAuthenticated = isAuth,
            authMethod = method,
            updatedAt = updated
        )
    }

    fun updateProfile(displayName: String, bio: String, photoUri: String?) {
        val updated = _profile.value.copy(
            displayName = displayName.ifBlank { "Shivam Gupta" },
            bio = bio.ifBlank { "Creator • Builder • Dreamer" },
            photoUri = photoUri,
            updatedAt = System.currentTimeMillis()
        )
        _profile.value = updated
        prefs.edit()
            .putString("display_name", updated.displayName)
            .putString("bio", updated.bio)
            .putString("photo_uri", updated.photoUri)
            .putLong("updated_at", updated.updatedAt)
            .apply()
    }

    fun markAuthenticated(method: String, email: String? = null, name: String? = null) {
        val current = _profile.value
        val updated = current.copy(
            isAuthenticated = true,
            authMethod = method,
            email = email ?: current.email,
            displayName = name ?: current.displayName,
            updatedAt = System.currentTimeMillis()
        )
        _profile.value = updated
        prefs.edit()
            .putBoolean("is_authenticated", true)
            .putString("auth_method", method)
            .putString("email", updated.email)
            .putString("display_name", updated.displayName)
            .putLong("updated_at", updated.updatedAt)
            .apply()
    }

    fun logout() {
        val updated = _profile.value.copy(
            isAuthenticated = false,
            updatedAt = System.currentTimeMillis()
        )
        _profile.value = updated
        prefs.edit()
            .putBoolean("is_authenticated", false)
            .putLong("updated_at", updated.updatedAt)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return _profile.value.isAuthenticated
    }
}
