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
import kotlin.math.abs

data class IconPackInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable? = null
)

/**
 * High-performance App Icon Loader with memory LRU caching, external icon pack support,
 * and 6 signature custom icon packs with adaptive shape masking and unthemed app fallbacks.
 */
object AppIconManager {
    private const val CACHE_SIZE = 250 // Keep up to 250 icons in fast memory
    private val memoryCache = object : LruCache<String, ImageBitmap>(CACHE_SIZE) {}
    
    @Volatile
    var activeIconPackKey: String = "PACK_NEBULA"
        private set

    @Volatile
    var activeIconPackPackage: String? = null
        private set

    fun setActiveIconPackKey(packKey: String) {
        if (activeIconPackKey != packKey) {
            activeIconPackKey = packKey
            clearCache()
        }
    }

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
        val pack = activeIconPackPackage ?: activeIconPackKey
        val cacheKey = "${pack}_${packageName}_$iconShape"
        return memoryCache.get(cacheKey)
    }

    suspend fun loadIcon(context: Context, packageName: String, iconShape: String = "SQUIRCLE"): ImageBitmap? {
        val pack = activeIconPackPackage ?: activeIconPackKey
        val cacheKey = "${pack}_${packageName}_$iconShape"
        memoryCache.get(cacheKey)?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                val pm = context.packageManager
                val externalDrawable = loadFromIconPack(context, packageName)
                val originalBitmap = if (externalDrawable != null) {
                    drawableToBitmap(externalDrawable)
                } else {
                    val appDrawable = try { pm.getApplicationIcon(packageName) } catch (e: Exception) { null }
                    if (appDrawable != null) drawableToBitmap(appDrawable) else null
                }

                if (originalBitmap == null) return@withContext null

                val styledBitmap = if (activeIconPackPackage != null) {
                    applyShapeMask(originalBitmap, iconShape)
                } else {
                    renderSignatureIconPack(originalBitmap, packageName, activeIconPackKey, iconShape)
                }

                val composeBitmap = styledBitmap.asImageBitmap()
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

    private fun createShapePath(size: Float, shape: String): Path {
        val path = Path()
        val rect = RectF(0f, 0f, size, size)
        when (shape.uppercase()) {
            "CIRCLE" -> {
                path.addCircle(size / 2f, size / 2f, size / 2f, Path.Direction.CW)
            }
            "ROUNDED_RECT" -> {
                path.addRoundRect(rect, size * 0.22f, size * 0.22f, Path.Direction.CW)
            }
            "TEARDROP" -> {
                val radii = floatArrayOf(
                    size * 0.5f, size * 0.5f,
                    size * 0.5f, size * 0.5f,
                    size * 0.15f, size * 0.15f,
                    size * 0.5f, size * 0.5f
                )
                path.addRoundRect(rect, radii, Path.Direction.CW)
            }
            "SQUIRCLE" -> {
                val r = size * 0.32f
                path.addRoundRect(rect, r, r, Path.Direction.CW)
            }
            else -> {
                path.addRoundRect(rect, size * 0.28f, size * 0.28f, Path.Direction.CW)
            }
        }
        return path
    }

    private fun applyShapeMask(source: Bitmap, shape: String): Bitmap {
        val size = Math.min(source.width, source.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())

        val path = createShapePath(size.toFloat(), shape)
        canvas.drawPath(path, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(source, null, rect, paint)
        return output
    }

    /**
     * Renders an app icon according to one of the 6 signature icon pack designs:
     * 1. PACK_NEBULA: Cyberpunk neon contour vector line art with cyan/violet glowing border
     * 2. PACK_MONOFORM: Minimalist monochrome high-contrast silhouette on pure matte black
     * 3. PACK_POPFORM: Bold Bauhaus/Memphis dual-tone color-blocked geometric shapes
     * 4. PACK_GLASSCORE: Translucent frosted glassmorphism with 3D refractive beveled specular depth
     * 5. PACK_ATELIER: High-end bespoke editorial craft with metallic champagne gold linework
     * 6. PACK_SOFTCRAFT: Tactile 3D claymorphic puffy marshmallow icons with soft ambient shadows
     */
    private fun renderSignatureIconPack(
        source: Bitmap,
        packageName: String,
        packKey: String,
        iconShape: String
    ): Bitmap {
        val size = 144
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val shapePath = createShapePath(size.toFloat(), iconShape)
        val hash = abs(packageName.hashCode())

        // Clip entire canvas to the chosen icon shape
        canvas.clipPath(shapePath)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val glyphPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }

        val glyphScale = 0.58f
        val glyphSize = (size * glyphScale).toInt()
        val glyphOffset = (size - glyphSize) / 2f
        val glyphDestRect = RectF(glyphOffset, glyphOffset, glyphOffset + glyphSize, glyphOffset + glyphSize)

        when (packKey) {
            "PACK_MONOFORM" -> {
                // Base: Deep Matte Charcoal
                bgPaint.color = Color.rgb(0x14, 0x14, 0x16)
                canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

                // High-contrast monochrome silhouette
                val cm = ColorMatrix().apply {
                    setSaturation(0f)
                }
                val scale = 1.8f
                val translate = (-0.35f * scale) * 255f
                val contrast = ColorMatrix(floatArrayOf(
                    scale, 0f, 0f, 0f, translate,
                    0f, scale, 0f, 0f, translate,
                    0f, 0f, scale, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                ))
                contrast.preConcat(cm)
                glyphPaint.colorFilter = ColorMatrixColorFilter(contrast)
                canvas.drawBitmap(source, null, glyphDestRect, glyphPaint)

                // Crisp border
                strokePaint.color = Color.rgb(0x2E, 0x2E, 0x34)
                strokePaint.strokeWidth = 2f
                canvas.drawPath(shapePath, strokePaint)
            }

            "PACK_POPFORM" -> {
                // Bauhaus / Memphis dual-tone color blocking
                val palettes = listOf(
                    Pair(Color.rgb(0xFF, 0x72, 0x5E), Color.rgb(0x41, 0x69, 0xFF)), // Coral & Cobalt
                    Pair(Color.rgb(0xFF, 0xE6, 0x6D), Color.rgb(0x34, 0xD3, 0x99)), // Lemon & Mint
                    Pair(Color.rgb(0x41, 0x69, 0xFF), Color.rgb(0xFF, 0x72, 0x5E)), // Cobalt & Coral
                    Pair(Color.rgb(0xC7, 0xB5, 0xFF), Color.rgb(0xFF, 0xE6, 0x6D))  // Lilac & Lemon
                )
                val pair = palettes[hash % palettes.size]

                // Draw top/bottom or diagonal split
                bgPaint.color = pair.first
                canvas.drawRect(0f, 0f, size.toFloat(), size / 2f, bgPaint)
                bgPaint.color = pair.second
                canvas.drawRect(0f, size / 2f, size.toFloat(), size.toFloat(), bgPaint)

                // Center icon drawn in clean white silhouette
                val cm = ColorMatrix(floatArrayOf(
                    0f, 0f, 0f, 0f, 255f,
                    0f, 0f, 0f, 0f, 255f,
                    0f, 0f, 0f, 0f, 255f,
                    0f, 0f, 0f, 0.92f, 0f
                ))
                glyphPaint.colorFilter = ColorMatrixColorFilter(cm)
                canvas.drawBitmap(source, null, glyphDestRect, glyphPaint)

                strokePaint.color = Color.argb(60, 255, 255, 255)
                strokePaint.strokeWidth = 2f
                canvas.drawPath(shapePath, strokePaint)
            }

            "PACK_GLASSCORE" -> {
                // Translucent smoked acrylic glassmorphism
                bgPaint.color = Color.argb(215, 0x20, 0x25, 0x2D)
                canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

                // Ambient reflection
                val amberGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(55, 0xFF, 0xB5, 0x47)
                }
                canvas.drawCircle(size * 0.75f, size * 0.75f, size * 0.45f, amberGlow)

                // Inner glyph drawn with specular sheen
                val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(90, 0, 0, 0)
                }
                canvas.drawRect(glyphDestRect.left + 2f, glyphDestRect.top + 3f, glyphDestRect.right + 2f, glyphDestRect.bottom + 3f, shadowPaint)
                canvas.drawBitmap(source, null, glyphDestRect, glyphPaint)

                // Top-Left to Bottom-Right specular glass bevel highlight
                val bevelShader = LinearGradient(
                    0f, 0f, size.toFloat(), size.toFloat(),
                    intArrayOf(Color.argb(180, 255, 255, 255), Color.argb(20, 255, 255, 255), Color.argb(80, 0xFF, 0xB5, 0x47)),
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP
                )
                strokePaint.shader = bevelShader
                strokePaint.strokeWidth = 3f
                canvas.drawPath(shapePath, strokePaint)
            }

            "PACK_ATELIER" -> {
                // Deep Forest Green or Burgundy Wine base
                val baseColors = listOf(
                    Color.rgb(0x17, 0x4C, 0x3C), // Forest Green
                    Color.rgb(0x54, 0x12, 0x1E)  // Burgundy Wine
                )
                bgPaint.color = baseColors[hash % baseColors.size]
                canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

                // Delicate Champagne Gold duotone glyph tint
                val goldFilter = ColorMatrix(floatArrayOf(
                    0.85f, 0f, 0f, 0f, 216f * 0.15f,
                    0f, 0.74f, 0f, 0f, 189f * 0.2f,
                    0f, 0f, 0.50f, 0f, 128f * 0.2f,
                    0f, 0f, 0f, 1f, 0f
                ))
                glyphPaint.colorFilter = ColorMatrixColorFilter(goldFilter)
                canvas.drawBitmap(source, null, glyphDestRect, glyphPaint)

                // Metallic Champagne Gold filigree border
                strokePaint.color = Color.rgb(0xD8, 0xBD, 0x80)
                strokePaint.strokeWidth = 2.5f
                canvas.drawPath(shapePath, strokePaint)
            }

            "PACK_SOFTCRAFT" -> {
                // 3D puffy claymorphic pastel colors
                val pastels = listOf(
                    Pair(Color.rgb(0x93, 0xC5, 0xFD), Color.rgb(0x60, 0xA5, 0xFA)), // Sky Blue & Shade
                    Pair(Color.rgb(0xFD, 0xBA, 0x74), Color.rgb(0xFB, 0x92, 0x3C)), // Peach & Shade
                    Pair(Color.rgb(0xA7, 0xF3, 0xD0), Color.rgb(0x6E, 0xE7, 0xB7)), // Mint & Shade
                    Pair(Color.rgb(0xDD, 0xD6, 0xFE), Color.rgb(0xC4, 0xB5, 0xFD))  // Lilac & Shade
                )
                val (clayColor, clayShade) = pastels[hash % pastels.size]

                // Soft ambient pillow shadow at bottom right
                bgPaint.color = clayShade
                canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)
                bgPaint.color = clayColor
                canvas.drawCircle(size * 0.46f, size * 0.46f, size * 0.55f, bgPaint)

                // Embossed glyph
                canvas.drawBitmap(source, null, glyphDestRect, glyphPaint)

                strokePaint.color = Color.argb(70, 255, 255, 255)
                strokePaint.strokeWidth = 2f
                canvas.drawPath(shapePath, strokePaint)
            }

            else -> {
                // PACK_NEBULA (Default Signature Pack)
                // Dark Obsidian space base
                bgPaint.color = Color.rgb(0x0D, 0x11, 0x24)
                canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

                // Electric Cyan & Violet luminescence matrix
                val cyberMatrix = ColorMatrix(floatArrayOf(
                    0.3f, 0.2f, 0.1f, 0f, 50f,
                    0.1f, 0.7f, 0.3f, 0f, 130f,
                    0.2f, 0.3f, 0.9f, 0f, 210f,
                    0f,   0f,   0f,   1f, 0f
                ))
                glyphPaint.colorFilter = ColorMatrixColorFilter(cyberMatrix)
                canvas.drawBitmap(source, null, glyphDestRect, glyphPaint)

                // Dual-color neon glowing edge (Cyan to Violet)
                val neonShader = LinearGradient(
                    0f, 0f, size.toFloat(), size.toFloat(),
                    intArrayOf(Color.rgb(0x55, 0xE7, 0xFF), Color.rgb(0x76, 0x57, 0xFF)),
                    null,
                    Shader.TileMode.CLAMP
                )
                strokePaint.shader = neonShader
                strokePaint.strokeWidth = 3.5f
                canvas.drawPath(shapePath, strokePaint)
            }
        }

        return output
    }

    fun clearCache() {
        memoryCache.evictAll()
    }
}
