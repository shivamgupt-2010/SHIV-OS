package com.example.core.intelligence

import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

/**
 * Screen Intelligence Engine analyzes the UI tree of whatever is on screen
 * to provide context to ShivAI.
 */
class ScreenIntelligenceEngine {

    data class ScreenContext(
        val packageName: String,
        val visibleText: List<String>,
        val interactiveElements: List<String>,
        val activeInputFields: Boolean,
        val genericContext: String
    )

    private val _currentScreenContext = MutableStateFlow<ScreenContext?>(null)
    val currentScreenContext = _currentScreenContext.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)
    private var analysisJob: Job? = null
    private var lastAnalyzedText = 0

    fun analyzeScreen(rootNode: AccessibilityNodeInfo?, packageName: String) {
        if (rootNode == null) {
            _currentScreenContext.value = null
            return
        }
        
        // Privacy & Security: Sensitive apps blocking
        val sensitiveApps = listOf("com.android.settings", "com.banking", "google.pay", "paypal")
        if (sensitiveApps.any { packageName.contains(it, ignoreCase = true) }) {
            _currentScreenContext.value = ScreenContext(
                packageName = packageName,
                visibleText = listOf("SENSITIVE_CONTENT_MASKED"),
                interactiveElements = emptyList(),
                activeInputFields = false,
                genericContext = "Sensitive financial or settings app. Content viewing blocked for privacy."
            )
            return
        }

        analysisJob?.cancel()
        analysisJob = scope.launch {
            // Idle-state analysis: small delay to avoid processing in the middle of animations
            delay(300)

            try {
                val visibleText = mutableListOf<String>()
                val interactiveElements = mutableListOf<String>()
                var activeInputs = false

                fun traverseNode(node: AccessibilityNodeInfo?, depth: Int) {
                    if (node == null || depth > 20) return // Cap recursion depth

                    try {
                        // Privacy: Mask passwords
                        val isPassword = node.isPassword
                        val text = if (isPassword) "********" else node.text?.toString() ?: node.contentDescription?.toString()
                        
                        if (!text.isNullOrBlank()) {
                            visibleText.add(text)
                        }

                        if (node.isClickable && !isPassword) {
                            val desc = node.contentDescription?.toString() ?: node.text?.toString() ?: "Button"
                            interactiveElements.add(desc)
                        }

                        if (node.isEditable || node.isFocused) {
                            activeInputs = true
                        }
                    } catch (e: Exception) {
                        // Ignored: Node properties threw exception
                    }

                    for (i in 0 until node.childCount) {
                        try {
                            traverseNode(node.getChild(i), depth + 1)
                        } catch (e: Exception) {
                            // Ignored: child node might be detached
                        }
                    }
                }

                traverseNode(rootNode, 0)
                
                // Semantic diff checking - don't emit if nothing really changed
                val hash = visibleText.hashCode() + interactiveElements.hashCode()
                if (hash == lastAnalyzedText) return@launch
                
                lastAnalyzedText = hash

                // Extremely basic generic context summary
                val summary = if (visibleText.isNotEmpty()) {
                    "User is looking at ${visibleText.take(5).joinToString()}. There are ${interactiveElements.size} interactive elements."
                } else {
                    "Unknown screen structure."
                }

                _currentScreenContext.value = ScreenContext(
                    packageName = packageName,
                    visibleText = visibleText,
                    interactiveElements = interactiveElements,
                    activeInputFields = activeInputs,
                    genericContext = summary
                )
            } catch (e: Exception) {
                // Safely catch any accessibility node errors
                _currentScreenContext.value = ScreenContext(
                    packageName = packageName,
                    visibleText = emptyList(),
                    interactiveElements = emptyList(),
                    activeInputFields = false,
                    genericContext = "Error analyzing screen: ${e.message}"
                )
            }
        }
    }
}

object ScreenIntelligence {
    val engine = ScreenIntelligenceEngine()
}
