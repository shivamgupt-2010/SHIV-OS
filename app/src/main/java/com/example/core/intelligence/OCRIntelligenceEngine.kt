package com.example.core.intelligence

import android.graphics.Bitmap
import com.example.core.utils.Logger
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class OCRIntelligenceEngine {

    data class OCRResult(
        val fullText: String,
        val textBlocks: List<String>,
        val hasEquations: Boolean,
        val timestamp: Long
    )

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    private val _latestOCRResult = MutableStateFlow<OCRResult?>(null)
    val latestOCRResult = _latestOCRResult.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)
    private var ocrJob: Job? = null
    
    // Thermal/Battery throttling variables
    private var lastOcrTime = 0L
    private val MIN_OCR_INTERVAL_MS = 2000L // Don't run OCR more often than every 2 seconds

    /**
     * Process a screenshot for OCR understanding.
     * Uses throttling to save battery and reduce CPU usage.
     */
    fun processScreenshot(bitmap: Bitmap, force: Boolean = false) {
        val currentTime = System.currentTimeMillis()
        if (!force && currentTime - lastOcrTime < MIN_OCR_INTERVAL_MS) {
            Logger.d("OCR scan skipped - throttled to save battery.")
            return
        }
        
        ocrJob?.cancel()
        ocrJob = scope.launch {
            try {
                lastOcrTime = System.currentTimeMillis()
                val image = InputImage.fromBitmap(bitmap, 0)
                
                recognizer.process(image)
                    .addOnSuccessListener { text ->
                        val blocks = text.textBlocks.map { it.text }
                        val fullText = text.text
                        
                        // Very basic heuristic for equations
                        val hasEquations = blocks.any { it.contains("∫") || it.contains("∑") || it.contains("=") || it.contains("+") || it.contains("-") }

                        Logger.d("OCR completed. Extracted ${blocks.size} blocks. Has Equations: $hasEquations")
                        
                        _latestOCRResult.value = OCRResult(
                            fullText = fullText,
                            textBlocks = blocks,
                            hasEquations = hasEquations,
                            timestamp = System.currentTimeMillis()
                        )
                    }
                    .addOnFailureListener { e ->
                        Logger.e("OCR Intelligence failed", e as Exception)
                    }
                
            } catch (e: Exception) {
                Logger.e("OCR Intelligence failed setup", e)
            }
        }
    }
}
