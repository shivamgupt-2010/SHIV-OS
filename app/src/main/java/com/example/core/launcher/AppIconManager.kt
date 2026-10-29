package com.example.core.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class IconPackInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable? = null
)

/**
 * High-performance App Icon Loader with memory LRU caching, external icon pack support,
 * and custom shape masking. Eliminates main-thread IPC during scrolling.
 */
object AppIconManager {
    private const val CACHE_SIZE = 250 // Keep up to 250 icons in fast memory
    private val memoryCache = object : LruCache<String, ImageBitmap>(CACHE_SIZE) {}
    
    @Volatile
    var activeIconPackPackage: String? = null
        private set

    fun setActiveIconPack(packPackage: String?) {
        if (activeIconPackPackage != packPackage) {
            activeIconPackPackage = packPackage
            clearCache()
        }
    }

    fun getCachedIcon(cacheKey: String): ImageBitmap? {
        return memoryCache.get(cacheKey)
    }

    fun getCachedIcon(packageName: String, iconShape: String = "SQUIRCLE"): ImageBitmap? {
        val pack = activeIconPackPackage ?: "system"
        val cacheKey = "${pack}_${packageName}_$iconShape"
        return memoryCache.get(cacheKey) ?: memoryCache.get(packageName)
    }

    suspend fun loadIcon(context: Context, packageName: String, iconShape: String = "SQUIRCLE"): ImageBitmap? {
        val pack = activeIconPackPackage ?: "system"
        val cacheKey = "${pack}_${packageName}_$iconShape"
        memoryCache.get(cacheKey)?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                val pm = context.packageManager
                val drawable = loadFromIconPack(context, packageName) ?: pm.getApplicationIcon(packageName)
                val originalBitmap = drawableToBitmap(drawable)
                val shapedBitmap = applyShapeMask(originalBitmap, iconShape)
                val composeBitmap = shapedBitmap.asImageBitmap()
                memoryCache.put(cacheKey, composeBitmap)
                composeBitmap
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun loadFromIconPack(context: Context, targetPackageName: String): Drawable? {
        val packPkg = activeIconPackPackage ?: return null
        return try {
            val pm = context.packageManager
            val packRes = pm.getResourcesForApplication(packPkg)
            // Try sanitized package name formats used by Nova/Apex icon packs
            val sanitized = targetPackageName.replace(".", "_").lowercase()
            var resId = packRes.getIdentifier(sanitized, "drawable", packPkg)
            if (resId == 0) {
                val shortName = targetPackageName.substringAfterLast(".").lowercase()
                resId = packRes.getIdentifier(shortName, "drawable", packPkg)
            }
            if (resId != 0) {
                packRes.getDrawable(resId, null)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getInstalledIconPacks(context: Context): List<IconPackInfo> {
        val pm = context.packageManager
        val list = mutableListOf<IconPackInfo>()
        val seen = mutableSetOf<String>()

        val filterActions = listOf(
            "com.novalauncher.THEME",
            "org.adw.launcher.THEMES",
            "com.gau.go.launcherex.theme",
            "com.dlto.atom.launcher.THEME",
            "com.teslacoilsw.launcher.THEME"
        )

        for (action in filterActions) {
            val intent = Intent(action)
            val activities = pm.queryIntentActivities(intent, 0)
            for (resolve in activities) {
                val pkg = resolve.activityInfo.packageName
                if (pkg != context.packageName && seen.add(pkg)) {
                    val label = resolve.loadLabel(pm).toString()
                    val icon = try { resolve.loadIcon(pm) } catch (e: Exception) { null }
                    list.add(IconPackInfo(pkg, label, icon))
                }
            }
        }
        return list
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null && !drawable.bitmap.isRecycled) {
            return drawable.bitmap
        }

        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 144
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 144
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun applyShapeMask(source: Bitmap, shape: String): Bitmap {
        val size = Math.min(source.width, source.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())

        val path = Path()
        when (shape.uppercase()) {
            "CIRCLE" -> {
                path.addCircle(size / 2f, size / 2f, size / 2f, Path.Direction.CW)
            }
            "ROUNDED_RECT" -> {
                path.addRoundRect(rect, size * 0.22f, size * 0.22f, Path.Direction.CW)
            }
            "TEARDROP" -> {
                val radii = floatArrayOf(
                    size * 0.5f, size * 0.5f, // Top-Left
                    size * 0.5f, size * 0.5f, // Top-Right
                    size * 0.15f, size * 0.15f, // Bottom-Right
                    size * 0.5f, size * 0.5f  // Bottom-Left
                )
                path.addRoundRect(rect, radii, Path.Direction.CW)
            }
            "SQUIRCLE" -> {
                // Continuous curvature squircle
                val r = size * 0.32f
                path.addRoundRect(rect, r, r, Path.Direction.CW)
            }
            else -> {
                path.addRoundRect(rect, size * 0.28f, size * 0.28f, Path.Direction.CW)
            }
        }

        canvas.drawPath(path, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(source, null, rect, paint)
        return output
    }

    fun clearCache() {
        memoryCache.evictAll()
    }
}
