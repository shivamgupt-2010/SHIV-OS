package com.example.core.files

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.core.utils.Logger
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class FileResult(
    val success: Boolean,
    val file: File,
    val absolutePath: String,
    val sizeBytes: Long,
    val message: String
)

data class FileItem(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val isDirectory: Boolean,
    val lastModified: Long
)

/**
 * Native On-Device File Action Adapter:
 * Creates, reads, lists, and organizes real files and directories on Android storage.
 * Respects user requested paths, defaults to Documents/ShivAI/, and indexes with MediaScanner.
 */
class FileActionAdapter(private val context: Context) {

    private val defaultShivDir: File by lazy {
        val docs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val shivFolder = File(docs, "ShivAI")
        if (!shivFolder.exists()) {
            shivFolder.mkdirs()
        }
        shivFolder
    }

    /**
     * Resolves the target file location from user prompt or explicit arguments.
     */
    fun resolveTargetFile(requestedPath: String?, defaultFilename: String = "shiv_document.txt"): File {
        if (requestedPath.isNullOrBlank()) {
            return File(defaultShivDir, defaultFilename)
        }

        val clean = requestedPath.trim().replace("\\", "/")

        val targetFile = when {
            clean.startsWith("/sdcard/") || clean.startsWith("/storage/") -> {
                File(clean)
            }
            clean.startsWith("Documents/", ignoreCase = true) || clean.startsWith("Document/", ignoreCase = true) -> {
                val sub = clean.substringAfter("/")
                val docs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                File(docs, sub)
            }
            clean.startsWith("Downloads/", ignoreCase = true) || clean.startsWith("Download/", ignoreCase = true) -> {
                val sub = clean.substringAfter("/")
                val dls = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                File(dls, sub)
            }
            clean.startsWith("Movies/", ignoreCase = true) -> {
                val sub = clean.substringAfter("/")
                val movies = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                File(movies, sub)
            }
            clean.startsWith("Pictures/", ignoreCase = true) -> {
                val sub = clean.substringAfter("/")
                val pics = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                File(pics, sub)
            }
            clean.contains("/") -> {
                // Relative path with folder specified
                File(Environment.getExternalStorageDirectory(), clean)
            }
            else -> {
                // Just a filename requested, put into /sdcard/Documents/ShivAI/
                File(defaultShivDir, clean)
            }
        }

        targetFile.parentFile?.mkdirs()
        return targetFile
    }

    /**
     * Creates a file on mobile storage and indexes it into Android MediaStore.
     */
    fun createFile(
        requestedPath: String?,
        filename: String,
        content: String
    ): FileResult {
        return try {
            val targetFile = if (!requestedPath.isNullOrBlank()) {
                resolveTargetFile(requestedPath, filename)
            } else {
                resolveTargetFile(filename, filename)
            }

            targetFile.parentFile?.mkdirs()
            targetFile.writeText(content, Charsets.UTF_8)

            // Notify Android system scanner so file is instantly visible in Files, PC, and MediaStore
            MediaScannerConnection.scanFile(
                context,
                arrayOf(targetFile.absolutePath),
                null
            ) { path, uri ->
                Logger.d("MediaScanner scanned $path -> $uri")
            }

            FileResult(
                success = true,
                file = targetFile,
                absolutePath = targetFile.absolutePath,
                sizeBytes = targetFile.length(),
                message = "File created successfully at ${targetFile.absolutePath} (${targetFile.length()} bytes)"
            )
        } catch (e: Exception) {
            Logger.e("Failed to create file: ${e.message}", e)
            // Fallback to app external files dir if root permission was denied
            try {
                val fallbackFolder = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "ShivAI").apply { mkdirs() }
                val fallbackFile = File(fallbackFolder, filename.ifBlank { "shiv_document.txt" })
                fallbackFile.writeText(content, Charsets.UTF_8)
                FileResult(
                    success = true,
                    file = fallbackFile,
                    absolutePath = fallbackFile.absolutePath,
                    sizeBytes = fallbackFile.length(),
                    message = "File saved to ${fallbackFile.absolutePath}"
                )
            } catch (fallbackEx: Exception) {
                FileResult(
                    success = false,
                    file = File(""),
                    absolutePath = "",
                    sizeBytes = 0,
                    message = "Error creating file: ${e.message}"
                )
            }
        }
    }

    /**
     * Reads a file from storage.
     */
    fun readFile(requestedPath: String): String {
        val targetFile = resolveTargetFile(requestedPath)
        if (!targetFile.exists()) {
            throw IllegalArgumentException("File does not exist: ${targetFile.absolutePath}")
        }
        if (targetFile.length() > 2 * 1024 * 1024) {
            throw IllegalStateException("File exceeds maximum readable size of 2 MB")
        }
        return targetFile.readText(Charsets.UTF_8)
    }

    /**
     * Lists files in a given directory.
     */
    fun listFiles(folderPath: String?, extension: String? = null): List<FileItem> {
        val folder = if (folderPath.isNullOrBlank()) {
            defaultShivDir
        } else {
            resolveTargetFile(folderPath)
        }

        if (!folder.exists() || !folder.isDirectory) {
            return emptyList()
        }

        return folder.listFiles()?.filter { file ->
            if (extension.isNullOrBlank()) true
            else file.extension.equals(extension.removePrefix("."), ignoreCase = true)
        }?.map { file ->
            FileItem(
                name = file.name,
                path = file.absolutePath,
                sizeBytes = if (file.isDirectory) 0 else file.length(),
                isDirectory = file.isDirectory,
                lastModified = file.lastModified()
            )
        }?.sortedByDescending { it.lastModified } ?: emptyList()
    }

    /**
     * Deletes a file.
     */
    fun deleteFile(requestedPath: String): Boolean {
        val file = resolveTargetFile(requestedPath)
        if (!file.exists()) return false
        val deleted = file.delete()
        if (deleted) {
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
        }
        return deleted
    }

    /**
     * Opens the created file in the user's preferred viewer app (e.g. text editor, browser, pdf viewer).
     */
    fun openFile(file: File): Boolean {
        return try {
            if (!file.exists()) return false
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val mimeType = when (file.extension.lowercase()) {
                "html", "htm" -> "text/html"
                "txt", "md", "json", "xml", "csv", "py", "kt", "js" -> "text/plain"
                "pdf" -> "application/pdf"
                "mp4" -> "video/mp4"
                "png", "jpg", "jpeg", "webp" -> "image/*"
                "apk" -> "application/vnd.android.package-archive"
                else -> "*/*"
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "Open with").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            Logger.e("Failed to open file: ${file.absolutePath}", e)
            false
        }
    }
}
