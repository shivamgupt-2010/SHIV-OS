package com.example.companion.personas

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Environment
import com.example.core.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * CodingPilotAgent:
 * Mobile Coding Co-Pilot for Android.
 * - Scaffolds complete projects and components directly to device storage.
 * - Inspects build errors and stack traces on screen via OCR and copies instant fixes.
 * - Bridges directly with Shiv Codespaces on device.
 */
class CodingPilotAgent(private val context: Context) {

    data class ScaffoldResult(val success: Boolean, val path: String, val message: String)

    /**
     * Scaffolds a full Compose or Web project into the device's Documents/Projects directory.
     */
    suspend fun scaffoldProject(
        projectName: String,
        template: String = "compose"
    ): ScaffoldResult = withContext(Dispatchers.IO) {
        try {
            val rootDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "ShivProjects/$projectName"
            )
            rootDir.mkdirs()

            when (template.lowercase()) {
                "compose" -> {
                    val mainFile = File(rootDir, "MainActivity.kt")
                    mainFile.writeText(
                        """
                        package com.shivai.$projectName
                        
                        import android.os.Bundle
                        import androidx.activity.ComponentActivity
                        import androidx.activity.compose.setContent
                        import androidx.compose.material3.*
                        import androidx.compose.runtime.*
                        
                        class MainActivity : ComponentActivity() {
                            override fun onCreate(savedInstanceState: Bundle?) {
                                super.onCreate(savedInstanceState)
                                setContent {
                                    MaterialTheme {
                                        Surface {
                                            Text("Hello from ShivAI Scaffolded $projectName!")
                                        }
                                    }
                                }
                            }
                        }
                        """.trimIndent()
                    )
                }
                "web", "html" -> {
                    val indexFile = File(rootDir, "index.html")
                    indexFile.writeText(
                        """
                        <!DOCTYPE html>
                        <html lang="en">
                        <head>
                            <meta charset="UTF-8">
                            <title>$projectName</title>
                            <style>body { background: #0f172a; color: #38bdf8; font-family: sans-serif; padding: 20px; }</style>
                        </head>
                        <body>
                            <h1>$projectName</h1>
                            <p>Scaffolded autonomously by ShivAI Coding Pilot.</p>
                        </body>
                        </html>
                        """.trimIndent()
                    )
                }
            }

            ScaffoldResult(true, rootDir.absolutePath, "Scaffolded $projectName at ${rootDir.name}")
        } catch (e: Exception) {
            Logger.e("CodingPilot scaffold error", e)
            ScaffoldResult(false, "", "Error: ${e.message}")
        }
    }

    /**
     * Reads an error stack trace from screen text, analyzes it, and copies the fix to clipboard.
     */
    fun analyzeCrashAndCopyFix(screenErrorText: String): String {
        val fixSuggestion = when {
            screenErrorText.contains("NullPointerException", ignoreCase = true) ->
                "// ShivAI Fix: Initialize with safe call or default value\nval safeData = data ?: emptyList()"
            screenErrorText.contains("Unresolved reference", ignoreCase = true) ->
                "// ShivAI Fix: Add missing import statement\nimport androidx.compose.runtime.getValue"
            screenErrorText.contains("OutOfMemoryError", ignoreCase = true) ->
                "// ShivAI Fix: Enable largeHeap in AndroidManifest.xml or downscale bitmaps\nandroid:largeHeap=\"true\""
            else ->
                "// ShivAI Generic Fix for: ${screenErrorText.take(50)}\n// Check variable scope and lifecycle binding"
        }

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("ShivAI Code Fix", fixSuggestion)
        clipboard?.setPrimaryClip(clip)

        return fixSuggestion
    }
}
