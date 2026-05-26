package com.example.core.usability

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.core.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PDFDocumentationGenerator(private val context: Context) {

    suspend fun generateTestDocumentation() = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 width, height
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas
            
            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 24f
                isFakeBoldText = true
            }
            
            val headerPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 18f
                isFakeBoldText = true
            }

            val bodyPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
            }

            var textY = 50f
            val startX = 50f
            val lineSpacing = 20f

            fun drawLine(text: String, paint: Paint) {
                // simple word wrap for PDF
                var start = 0
                while (start < text.length) {
                    var end = start + 80 // arbitrary char limit
                    if (end > text.length) end = text.length
                    canvas.drawText(text.substring(start, end), startX, textY, paint)
                    textY += lineSpacing
                    start = end
                }
            }

            canvas.drawText("ShivAI: Production Testing Documentation", startX, textY, titlePaint)
            textY += 40f

            drawLine("1. Overview", headerPaint)
            drawLine("ShivAI is a context-aware AI operating environment, transitioning from an engineering prototype to a stable, trustworthy human-tested platform.", bodyPaint)
            textY += 10f

            drawLine("2. Trust & Privacy Architecture", headerPaint)
            drawLine("- Semantic Memory: Encrypted locally via custom XOR cipher (stubbed for Android Keystore). Memories can be inspected, deleted, and audited.", bodyPaint)
            drawLine("- Vision & OCR: Runs purely on-device (ML Kit). No screens are sent to the cloud. Sensitive screens (passwords/banking) are masked natively.", bodyPaint)
            drawLine("- Permissions: Users can revoke OCR, Accessibility, or Overlays at any time. ShivAI degrades into 'Safe-Mode' seamlessly.", bodyPaint)
            textY += 10f
            
            drawLine("3. AI Safety Systems (SafeRuntimeManager)", headerPaint)
            drawLine("Monitors execution limits. Detects deadlock in Orchestrator loops and enforces constraints on token usage, memory pressure, and background activity.", bodyPaint)
            textY += 10f

            drawLine("4. Testing Instructions", headerPaint)
            drawLine("- Run `DiagnosticsScreen` to monitor system state.", bodyPaint)
            drawLine("- Check 'Memory Inspection (Deep Context)' to audit what the AI learns.", bodyPaint)
            drawLine("- Toggle permissions to test graceful degradation.", bodyPaint)
            
            pdfDocument.finishPage(page)

            // Save PDF
            val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val file = File(directory, "ShivAI_Production_Testing_Guide.pdf")
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            
            Logger.i("PDF Guide Generated at: \${file.absolutePath}")
            
        } catch (e: Exception) {
            Logger.e("Failed to generate PDF", e)
        }
    }
}
