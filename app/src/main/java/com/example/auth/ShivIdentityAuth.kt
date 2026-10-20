package com.example.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import okhttp3.FormBody
import org.json.JSONObject
import java.io.IOException
import java.security.MessageDigest
import java.security.SecureRandom

object ShivIdentityAuth {
    private const val TAG = "ShivIdentityAuth"
    private const val PREFS_NAME = "shiv_identity_oauth"
    private const val KEY_VERIFIER = "pkce_code_verifier"
    private const val KEY_STATE = "oauth_state"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_ID_TOKEN = "id_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_PROFILE = "user_profile_json"

    private const val ISSUER = "https://shiv-database.onrender.com"
    const val CLIENT_ID = "shiv_client_1d07df81d86aa737330cd3e9"
    private const val REDIRECT_URI = "shivlauncher://oauth/callback"

    var codeVerifier: String? = null
    var oauthState: String? = null

    private val _authState = MutableStateFlow<OAuthState>(OAuthState.LoggedOut)
    val authState: StateFlow<OAuthState> = _authState.asStateFlow()

    val accessToken: String?
        get() = (authState.value as? OAuthState.Authenticated)?.accessToken

    sealed class OAuthState {
        object LoggedOut : OAuthState()
        object Authenticating : OAuthState()
        data class Authenticated(val accessToken: String, val username: String? = null) : OAuthState()
        data class Error(val message: String) : OAuthState()
    }

    fun generatePkce(): Pair<String, String> {
        val bytes = ByteArray(64)
        SecureRandom().nextBytes(bytes)
        val verifier = Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        
        val sha256 = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII))
        val challenge = Base64.encodeToString(sha256, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        return Pair(verifier, challenge)
    }

    // Call this when user taps "Sign in with SHIV" on the Launcher
    fun startSignIn(context: Context) {
        val (verifier, challenge) = generatePkce()
        codeVerifier = verifier
        oauthState = Base64.encodeToString(ByteArray(16).also { SecureRandom().nextBytes(it) }, Base64.URL_SAFE or Base64.NO_WRAP)

        // Persist verifier and state so they survive background activity restarts
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_VERIFIER, codeVerifier)
            .putString(KEY_STATE, oauthState)
            .apply()

        _authState.value = OAuthState.Authenticating

        val authUrl = Uri.parse("$ISSUER/oauth/authorize").buildUpon()
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("scope", "openid profile email offline_access")
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("state", oauthState)
            .build()

        try {
            // Launch in Chrome Custom Tab
            CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
                .launchUrl(context, authUrl)
        } catch (e: Exception) {
            Log.w(TAG, "CustomTabs failed, launching standard browser intent", e)
            val fallbackIntent = Intent(Intent.ACTION_VIEW, authUrl).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallbackIntent)
        }
    }

    // Call this inside onNewIntent(intent: Intent) or onCreate(savedInstanceState: Bundle?) in MainActivity
    fun handleRedirect(context: Context, uri: Uri, onComplete: (Boolean, String?) -> Unit) {
        val code = uri.getQueryParameter("code")
        val state = uri.getQueryParameter("state")

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val expectedState = oauthState ?: prefs.getString(KEY_STATE, null)
        val currentVerifier = codeVerifier ?: prefs.getString(KEY_VERIFIER, null)

        if (state == null || state != expectedState || code == null) {
            val err = "State mismatch (expected=$expectedState, got=$state) or code missing"
            Log.e(TAG, err)
            _authState.value = OAuthState.Error(err)
            onComplete(false, err)
            return
        }

        // Exchange code for tokens
        val client = OkHttpClient()
        val formBody = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("redirect_uri", REDIRECT_URI)
            .add("client_id", CLIENT_ID)
            .add("code_verifier", currentVerifier ?: "")
            .build()

        val request = Request.Builder()
            .url("$ISSUER/oauth/token")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Token exchange network error: ${e.message}", e)
                _authState.value = OAuthState.Error(e.message ?: "Network error during authentication")
                onComplete(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string() ?: "{}"
                val json = JSONObject(bodyStr)
                if (response.isSuccessful) {
                    val accessToken = json.getString("access_token")
                    val idToken = json.optString("id_token")
                    val refreshToken = json.optString("refresh_token")

                    // Save tokens securely in SharedPreferences
                    prefs.edit()
                        .putString(KEY_ACCESS_TOKEN, accessToken)
                        .putString(KEY_ID_TOKEN, idToken)
                        .putString(KEY_REFRESH_TOKEN, refreshToken)
                        .remove(KEY_VERIFIER)
                        .remove(KEY_STATE)
                        .apply()

                    _authState.value = OAuthState.Authenticated(accessToken)
                    Log.i(TAG, "Successfully authenticated with SHIV Identity! Token acquired.")

                    // Fetch user info asynchronously
                    fetchUserInfo(accessToken) { profile ->
                        if (profile != null) {
                            prefs.edit().putString(KEY_USER_PROFILE, profile.toString()).apply()
                            val username = profile.optString("preferred_username", profile.optString("name", "User"))
                            _authState.value = OAuthState.Authenticated(accessToken, username)
                        }
                    }

                    onComplete(true, accessToken)
                } else {
                    val errorDesc = json.optString("error_description", json.optString("detail", "Authentication failed"))
                    Log.e(TAG, "OAuth Token error ($response.code): $errorDesc")
                    _authState.value = OAuthState.Error(errorDesc)
                    onComplete(false, errorDesc)
                }
            }
        })
    }

    // Convenience overload matching the exact signature from instructions
    fun handleRedirect(uri: Uri, onComplete: (Boolean, String?) -> Unit) {
        val code = uri.getQueryParameter("code")
        val state = uri.getQueryParameter("state")
        if (state != oauthState || code == null) {
            onComplete(false, "State mismatch or code missing")
            return
        }

        // Exchange code for tokens
        val client = OkHttpClient()
        val formBody = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("redirect_uri", REDIRECT_URI)
            .add("client_id", CLIENT_ID)
            .add("code_verifier", codeVerifier ?: "")
            .build()

        val request = Request.Builder()
            .url("$ISSUER/oauth/token")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onComplete(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body?.string() ?: "{}")
                if (response.isSuccessful) {
                    val accessToken = json.getString("access_token")
                    val idToken = json.optString("id_token")
                    val refreshToken = json.optString("refresh_token")

                    onComplete(true, accessToken)
                } else {
                    onComplete(false, json.optString("error_description"))
                }
            }
        })
    }

    fun fetchUserInfo(accessToken: String, callback: (JSONObject?) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("$ISSUER/oauth/userinfo")
            .addHeader("Authorization", "Bearer $accessToken")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    try {
                        val json = JSONObject(response.body?.string() ?: "{}")
                        callback(json)
                    } catch (e: Exception) {
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }
        })
    }

    fun getAccessToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getIdToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ID_TOKEN, null)
    }

    fun getRefreshToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun isLoggedIn(context: Context): Boolean {
        return !getAccessToken(context).isNullOrBlank()
    }

    fun signOut(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        codeVerifier = null
        oauthState = null
        _authState.value = OAuthState.LoggedOut
    }
}
