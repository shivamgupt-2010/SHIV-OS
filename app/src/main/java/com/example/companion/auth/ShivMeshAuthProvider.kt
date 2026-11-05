package com.example.companion.auth

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.example.auth.ShivIdentityAuth

/**
 * ShivMeshAuthProvider:
 * High-Security Signature-Protected ContentProvider for Zero-Touch Cross-App Auth.
 * Allows SHIV-OS, Shiv Codespaces, Shiv Media Editor, and ShivAI Companion
 * to share active JWT authentication and SHV-ID profiles without user login prompts.
 */
class ShivMeshAuthProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.shivai.auth.mesh"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/token")
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val caller = callingPackage ?: return null
        val ctx = context ?: return null

        // 1. Strict Cryptographic Signature Validation
        val sigMatch = ctx.packageManager.checkSignatures(caller, ctx.packageName)
        if (sigMatch != PackageManager.SIGNATURE_MATCH) {
            // Reject unauthorized third-party apps
            return null
        }

        // 2. Return Active Session Tokens from ShivIdentityAuth
        val cursor = MatrixCursor(arrayOf("access_token", "username", "is_authenticated"))
        val token = ShivIdentityAuth.accessToken
        val state = ShivIdentityAuth.authState.value

        if (state is ShivIdentityAuth.OAuthState.Authenticated) {
            cursor.addRow(arrayOf(state.accessToken, state.username ?: "SHIV_User", 1))
        } else if (!token.isNullOrBlank()) {
            cursor.addRow(arrayOf(token, "SHIV_User", 1))
        } else {
            cursor.addRow(arrayOf("", "", 0))
        }

        return cursor
    }

    override fun getType(uri: Uri): String = "vnd.android.cursor.item/vnd.com.shivai.auth.token"
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
