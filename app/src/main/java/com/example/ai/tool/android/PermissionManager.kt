package com.example.ai.tool.android

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class PermissionManager(private val context: Context) {

    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Advanced permission handling like requesting runtime permissions requires
    // an Activity context. The tools run in generic scope, so they generally
    // expect permissions to be granted via a central UI first or report a "need_permission" state.
}
