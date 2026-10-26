package com.example.core.media

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.media.*
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.view.Surface
import androidx.core.content.FileProvider
import com.example.core.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class VideoResult(
    val success: Boolean,
    val videoFile: File,
    val absolutePath: String,
    val durationSeconds: Int,
    val message: String
)

/**
 * Native On-Device Video Action Adapter:
 * Uses Android MediaCodec + MediaMuxer hardware acceleration to generate, render,
 * and export real MP4 videos (promotional videos, animated titles, visual clips) directly on-device.
 */
class VideoActionAdapter(private val context: Context) {

    private val moviesDir: File by lazy {
        val movies = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        val shivMovies = File(movies, "ShivAI")
        if (!shivMovies.exists()) {
            shivMovies.mkdirs()
        }
        shivMovies
    }

    /**
     * Generates a real on-device MP4 video with custom title, subtitle, and branding.
     */
    suspend fun generatePromoVideo(
        title: String,
        subtitle: String,
        durationSeconds: Int = 5,
        targetFilename: String? = null
    ): VideoResult = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = targetFilename ?: "SHIV_Promo_$timestamp.mp4"
        val outputFile = File(moviesDir, filename)

        val width = 720
        val height = 1280
        val fps = 30
        val totalFrames = durationSeconds * fps
        val bitRate = 2_500_000

        var encoder: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var inputSurface: Surface? = null

        try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            inputSurface = encoder.createInputSurface()
            encoder.start()

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var videoTrackIndex = -1
            var muxerStarted = false

            val bufferInfo = MediaCodec.BufferInfo()

            // Setup Paint tools for Canvas rendering
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 48f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#00F0FF") // Cyan Neon
                textSize = 28f
                textAlign = Paint.Align.CENTER
            }

            val trishulPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFD700") // Gold
                strokeWidth = 6f
                style = Paint.Style.STROKE
            }

            // Render frames onto the hardware Surface
            for (frame in 0 until totalFrames) {
                val canvas = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    inputSurface.lockHardwareCanvas()
                } else {
                    inputSurface.lockCanvas(null)
                }

                if (canvas != null) {
                    try {
                        val progress = frame.toFloat() / totalFrames.toFloat()

                        // 1. Draw dynamic cosmic gradient background
                        val gradient = LinearGradient(
                            0f, 0f, 0f, height.toFloat(),
                            intArrayOf(
                                Color.rgb((10 + 20 * progress).toInt(), (13 + 15 * progress).toInt(), (20 + 30 * progress).toInt()),
                                Color.rgb(5, 5, 12),
                                Color.rgb((20 * (1f - progress)).toInt(), (10 * progress).toInt(), (40 * progress).toInt())
                            ),
                            null,
                            Shader.TileMode.CLAMP
                        )
                        val bgPaint = Paint().apply { shader = gradient }
                        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

                        // 2. Draw animated glowing Trishul symbol in center
                        val centerX = width / 2f
                        val centerY = height / 2f - 80f
                        val pulse = (Math.sin(frame.toDouble() * 0.1) * 10).toFloat()

                        // Central spear
                        canvas.drawLine(centerX, centerY - 80f + pulse, centerX, centerY + 80f + pulse, trishulPaint)
                        // Left & Right prongs
                        canvas.drawLine(centerX - 40f, centerY - 60f + pulse, centerX - 40f, centerY + 20f + pulse, trishulPaint)
                        canvas.drawLine(centerX + 40f, centerY - 60f + pulse, centerX + 40f, centerY + 20f + pulse, trishulPaint)
                        canvas.drawLine(centerX - 40f, centerY + 20f + pulse, centerX + 40f, centerY + 20f + pulse, trishulPaint)

                        // 3. Draw Title & Subtitle
                        canvas.drawText(title, centerX, centerY + 180f, titlePaint)
                        canvas.drawText(subtitle, centerX, centerY + 230f, subtitlePaint)

                        // 4. Draw Animated Progress Bar
                        val barWidth = width * 0.6f
                        val barLeft = (width - barWidth) / 2f
                        val barTop = height - 120f
                        val barPaint = Paint().apply { color = Color.DKGRAY }
                        canvas.drawRoundRect(barLeft, barTop, barLeft + barWidth, barTop + 10f, 5f, 5f, barPaint)

                        val activePaint = Paint().apply { color = Color.parseColor("#00F0FF") }
                        canvas.drawRoundRect(barLeft, barTop, barLeft + (barWidth * progress), barTop + 10f, 5f, 5f, activePaint)

                    } finally {
                        inputSurface.unlockCanvasAndPost(canvas)
                    }
                }

                // Drain encoder output
                while (true) {
                    val outputBufferId = encoder.dequeueOutputBuffer(bufferInfo, 0)
                    if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        val newFormat = encoder.outputFormat
                        videoTrackIndex = muxer.addTrack(newFormat)
                        muxer.start()
                        muxerStarted = true
                    } else if (outputBufferId >= 0) {
                        val encodedData = encoder.getOutputBuffer(outputBufferId)
                        if (encodedData != null && muxerStarted && bufferInfo.size > 0) {
                            bufferInfo.presentationTimeUs = (frame * 1_000_000L / fps)
                            muxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                        }
                        encoder.releaseOutputBuffer(outputBufferId, false)
                    } else {
                        break
                    }
                }
            }

            // Signal end of stream
            encoder.signalEndOfInputStream()

            // Drain remaining frames
            var eos = false
            while (!eos) {
                val outputBufferId = encoder.dequeueOutputBuffer(bufferInfo, 10_000)
                if (outputBufferId >= 0) {
                    val encodedData = encoder.getOutputBuffer(outputBufferId)
                    if (encodedData != null && muxerStarted && bufferInfo.size > 0) {
                        muxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                    }
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        eos = true
                    }
                    encoder.releaseOutputBuffer(outputBufferId, false)
                } else if (outputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    break
                }
            }

            // Index with MediaStore so it immediately appears in Gallery / Photos
            MediaScannerConnection.scanFile(context, arrayOf(outputFile.absolutePath), arrayOf("video/mp4"), null)

            VideoResult(
                success = true,
                videoFile = outputFile,
                absolutePath = outputFile.absolutePath,
                durationSeconds = durationSeconds,
                message = "Video exported successfully to ${outputFile.absolutePath} (${outputFile.length() / 1024} KB)"
            )
        } catch (e: Exception) {
            Logger.e("Video generation error: ${e.message}", e)
            VideoResult(
                success = false,
                videoFile = outputFile,
                absolutePath = outputFile.absolutePath,
                durationSeconds = 0,
                message = "Video generation failed: ${e.message}"
            )
        } finally {
            try { encoder?.stop(); encoder?.release() } catch (_: Exception) {}
            try { inputSurface?.release() } catch (_: Exception) {}
            try { muxer?.stop(); muxer?.release() } catch (_: Exception) {}
        }
    }

    /**
     * Opens the video in the system player.
     */
    fun playVideo(videoFile: File): Boolean {
        return try {
            if (!videoFile.exists()) return false
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", videoFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "video/mp4")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Play Video"))
            true
        } catch (e: Exception) {
            Logger.e("Failed to play video: ${e.message}", e)
            false
        }
    }
}
