package com.example.core.studio

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.example.ui.components.CosmicBackground
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream

enum class WallpaperType {
    COSMIC,
    CYBER_NEON,
    AURORA,
    MINIMAL_SLATE,
    SOLAR_DUSK,
    DEEP_OLED,
    SYSTEM_WALLPAPER,
    CUSTOM_IMAGE
}

data class WallpaperConfig(
    val type: WallpaperType = WallpaperType.COSMIC,
    val customImageUri: String? = null,
    val name: String = "Cosmic Shiva"
)

class WallpaperEngine(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("shiv_wallpaper_prefs", Context.MODE_PRIVATE)

    private val _currentWallpaper = MutableStateFlow(loadWallpaper())
    val currentWallpaper: StateFlow<WallpaperConfig> = _currentWallpaper.asStateFlow()

    private fun loadWallpaper(): WallpaperConfig {
        val typeName = prefs.getString("wallpaper_type", WallpaperType.COSMIC.name) ?: WallpaperType.COSMIC.name
        val uri = prefs.getString("custom_uri", null)
        val name = prefs.getString("wallpaper_name", "Cosmic Shiva") ?: "Cosmic Shiva"

        val type = try {
            WallpaperType.valueOf(typeName)
        } catch (e: Exception) {
            WallpaperType.COSMIC
        }

        return WallpaperConfig(type = type, customImageUri = uri, name = name)
    }

    fun setPreset(type: WallpaperType) {
        val name = when (type) {
            WallpaperType.COSMIC -> "Cosmic Shiva"
            WallpaperType.CYBER_NEON -> "Cyber Neon"
            WallpaperType.AURORA -> "Nordic Aurora"
            WallpaperType.MINIMAL_SLATE -> "Minimal Slate"
            WallpaperType.SOLAR_DUSK -> "Solar Dusk"
            WallpaperType.DEEP_OLED -> "Pure OLED Black"
            WallpaperType.SYSTEM_WALLPAPER -> "System Wallpaper"
            WallpaperType.CUSTOM_IMAGE -> "Custom Image"
        }
        val updated = WallpaperConfig(type = type, customImageUri = null, name = name)
        _currentWallpaper.value = updated
        prefs.edit()
            .putString("wallpaper_type", type.name)
            .remove("custom_uri")
            .putString("wallpaper_name", name)
            .apply()
    }

    fun setCustomImage(uri: Uri): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            val wallpapersDir = File(context.filesDir, "wallpapers").apply { mkdirs() }
            val destFile = File(wallpapersDir, "custom_wallpaper_${System.currentTimeMillis()}.jpg")
            
            FileOutputStream(destFile).use { out ->
                inputStream.copyTo(out)
            }
            inputStream.close()

            val updated = WallpaperConfig(
                type = WallpaperType.CUSTOM_IMAGE,
                customImageUri = destFile.absolutePath,
                name = "Custom Photo"
            )
            _currentWallpaper.value = updated
            prefs.edit()
                .putString("wallpaper_type", WallpaperType.CUSTOM_IMAGE.name)
                .putString("custom_uri", destFile.absolutePath)
                .putString("wallpaper_name", "Custom Photo")
                .apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

@Composable
fun LauncherWallpaperView(config: WallpaperConfig) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (config.type) {
            WallpaperType.COSMIC -> {
                CosmicBackground(showSilhouette = false)
            }
            WallpaperType.CYBER_NEON -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF050510),
                                    Color(0xFF0A102A),
                                    Color(0xFF00223A),
                                    Color(0xFF00111A)
                                )
                            )
                        )
                )
            }
            WallpaperType.AURORA -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF021B1A),
                                    Color(0xFF032824),
                                    Color(0xFF02161E),
                                    Color(0xFF050B14)
                                )
                            )
                        )
                )
            }
            WallpaperType.MINIMAL_SLATE -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF121417))
                )
            }
            WallpaperType.SOLAR_DUSK -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF1F0A18),
                                    Color(0xFF2E0F1F),
                                    Color(0xFF1E1026),
                                    Color(0xFF0A0714)
                                )
                            )
                        )
                )
            }
            WallpaperType.DEEP_OLED -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF000000))
                )
            }
            WallpaperType.CUSTOM_IMAGE -> {
                if (!config.customImageUri.isNullOrBlank()) {
                    val bitmap = remember(config.customImageUri) {
                        try {
                            BitmapFactory.decodeFile(config.customImageUri)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Wallpaper",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        CosmicBackground(showSilhouette = false)
                    }
                } else {
                    CosmicBackground(showSilhouette = false)
                }
            }
            WallpaperType.SYSTEM_WALLPAPER -> {
                CosmicBackground(showSilhouette = false)
            }
        }
    }
}
