package com.example.core.intelligence

class FormulaRecognitionEngine {

    /**
     * Attempts to normalize OCR text into symbolic representation if it looks like math.
     */
    fun normalizeMathematicalText(rawOcrBlocks: List<String>): List<String> {
        val mathBlocks = mutableListOf<String>()
        val mathPattern = Regex("(?=.*[0-9])(?=.*[=+\\-*/∫∑])")

        for (block in rawOcrBlocks) {
            if (mathPattern.containsMatchIn(block) || block.contains("x") || block.contains("y")) {
                // Normalize some common OCR mistakes in math
                val normalized = block
                    .replace(" ", "")
                    .replace("O", "0") // Simplistic mistake correction
                    .replace("l", "1")
                    .replace("I", "1")
                
                // If it still looks like an equation
                if (normalized.contains("=")) {
                    mathBlocks.add(normalized)
                }
            }
        }
        
        return mathBlocks
    }
}
