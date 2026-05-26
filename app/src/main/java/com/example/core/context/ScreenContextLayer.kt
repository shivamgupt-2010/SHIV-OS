package com.example.core.context

import com.example.core.intelligence.ScreenIntelligenceEngine
import com.example.core.intelligence.OCRIntelligenceEngine
import com.example.core.intelligence.FormulaRecognitionEngine

/**
 * Screen Context Preparation Layer
 * Synthesizes semantic screen understanding from accessibility and OCR pipelines.
 */
class ScreenContextLayer(
    private val screenIntelligenceEngine: ScreenIntelligenceEngine,
    private val ocrIntelligenceEngine: OCRIntelligenceEngine,
    private val formulaRecognitionEngine: FormulaRecognitionEngine
) {

    fun analyzeScreenContents(): ScreenSemanticData {
        val screenCtx = screenIntelligenceEngine.currentScreenContext.value
        val ocrResult = ocrIntelligenceEngine.latestOCRResult.value
        
        val extractedText = mutableListOf<String>()
        val entities = mutableListOf<String>()
        var hasStudyMaterial = false
        
        // Add accessibility text
        if (screenCtx != null) {
            extractedText.addAll(screenCtx.visibleText)
            entities.addAll(screenCtx.interactiveElements)
        }
        
        // Add OCR text
        if (ocrResult != null && System.currentTimeMillis() - ocrResult.timestamp < 5000) {
            extractedText.addAll(ocrResult.textBlocks)
            
            if (ocrResult.hasEquations) {
                hasStudyMaterial = true
                val mathText = formulaRecognitionEngine.normalizeMathematicalText(ocrResult.textBlocks)
                extractedText.add("MATHEMATICS_DETECTED: (\${mathText.joinToString(\" \")})")
            }
        }

        return ScreenSemanticData(
            extractedText = extractedText.distinct(),
            detectedEntities = entities.distinct(),
            isStudySession = hasStudyMaterial || extractedText.any { it.contains("JEE") || it.contains("Physics") || it.contains("Chemistry") },
            confidenceScore = if (screenCtx != null && ocrResult != null) 0.9f else if (screenCtx != null || ocrResult != null) 0.5f else 0.0f
        )
    }

    fun prepareForLauncherMode() {
        // Initializes context windows designed specifically for the launcher overlay
    }
}

data class ScreenSemanticData(
    val extractedText: List<String>,
    val detectedEntities: List<String>, 
    val isStudySession: Boolean = false,
    val confidenceScore: Float
)
