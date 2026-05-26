package com.example.ai.tool.android

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.Settings
import com.example.core.utils.Logger
import com.example.core.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Single responsibility of launching standard Android Intents.
 */
class AndroidIntentEngine(private val context: Context) {

    suspend fun openUrl(url: String): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Result.Success(Unit)
        } catch (e: Exception) {
            Logger.e("Failed to open URL: $url", e)
            Result.Error(e)
        }
    }

    suspend fun searchWeb(query: String): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra("query", query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Result.Success(Unit)
        } catch (e: Exception) {
            Logger.e("Failed to search web: $query", e)
            Result.Error(e)
        }
    }

    suspend fun openApp(packageName: String): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                Result.Success(Unit)
            } else {
                Result.Error(Exception("App not installed: $packageName"))
            }
        } catch (e: Exception) {
            Logger.e("Failed to open app: $packageName", e)
            Result.Error(e)
        }
    }

    suspend fun setAlarm(hour: Int, minute: Int, message: String): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Result.Success(Unit)
        } catch (e: Exception) {
            Logger.e("Failed to set alarm", e)
            Result.Error(e)
        }
    }

    suspend fun shareText(text: String): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(shareIntent)
            Result.Success(Unit)
        } catch (e: Exception) {
            Logger.e("Failed to share text", e)
            Result.Error(e)
        }
    }
}
