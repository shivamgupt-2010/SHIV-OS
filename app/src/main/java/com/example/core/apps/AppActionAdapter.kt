package com.example.core.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.example.core.utils.Logger
import java.io.File

/**
 * AppActionAdapter:
 * Handles opening installed apps, searching & installing apps from Play Store,
 * and installing local APK packages via Android's PackageInstaller / FileProvider.
 */
class AppActionAdapter(private val context: Context) {

    /**
     * Opens an app by package name or matching display label.
     */
    fun openApp(query: String): Boolean {
        val clean = query.trim()
        val pm = context.packageManager

        // 1. Direct package match
        val directIntent = pm.getLaunchIntentForPackage(clean)
        if (directIntent != null) {
            directIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(directIntent)
            return true
        }

        // 2. Fuzzy match against installed launcher apps
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList = pm.queryIntentActivities(mainIntent, 0)

        // Find exact match first, then partial match
        val matchedApp = resolveInfoList.firstOrNull {
            val label = it.loadLabel(pm).toString()
            label.equals(clean, ignoreCase = true)
        } ?: resolveInfoList.firstOrNull {
            val label = it.loadLabel(pm).toString()
            label.contains(clean, ignoreCase = true)
        } ?: resolveInfoList.firstOrNull {
            it.activityInfo.packageName.contains(clean, ignoreCase = true)
        }

        if (matchedApp != null) {
            val pkg = matchedApp.activityInfo.packageName
            val launchIntent = pm.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return true
            }
        }

        return false
    }

    /**
     * Prompts Android package installer to install a local APK file.
     */
    fun installLocalApk(apkPath: String): Boolean {
        return try {
            val file = File(apkPath)
            if (!file.exists() || !file.name.endsWith(".apk", ignoreCase = true)) {
                Logger.e("APK file not found or invalid: $apkPath")
                return false
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
            true
        } catch (e: Exception) {
            Logger.e("Failed to prompt APK installation: ${e.message}", e)
            false
        }
    }

    /**
     * Opens Google Play Store to install an application.
     */
    fun installFromPlayStore(appNameOrQuery: String, packageName: String? = null): Boolean {
        return try {
            val marketUri = if (!packageName.isNullOrBlank()) {
                Uri.parse("market://details?id=$packageName")
            } else {
                Uri.parse("market://search?q=${Uri.encode(appNameOrQuery)}")
            }

            val intent = Intent(Intent.ACTION_VIEW, marketUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val webFallbackUri = if (!packageName.isNullOrBlank()) {
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            } else {
                Uri.parse("https://play.google.com/store/search?q=${Uri.encode(appNameOrQuery)}&c=apps")
            }

            val pm = context.packageManager
            if (intent.resolveActivity(pm) != null) {
                context.startActivity(intent)
            } else {
                context.startActivity(Intent(Intent.ACTION_VIEW, webFallbackUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
            true
        } catch (e: Exception) {
            Logger.e("Failed to open Play Store: ${e.message}", e)
            false
        }
    }

    /**
     * Opens a deep link or web URI.
     */
    fun openDeepLink(uriString: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Logger.e("Failed to open deep link: $uriString", e)
            false
        }
    }
}
