package com.example.ai.prompt

import com.example.ai.gemini.Content
import com.example.ai.gemini.Part

class PromptBuilder {
    private val systemInstructions = mutableListOf<String>()
    private val memoryContext = mutableMapOf<String, String>()
    private val history = mutableListOf<Content>()

    fun setSystemIdentity(role: String, goals: String): PromptBuilder {
        systemInstructions.add("Role: $role\nGoals: $goals")
        return this
    }

    fun addRule(rule: String): PromptBuilder {
        systemInstructions.add("Rule: $rule")
        return this
    }

    fun injectMemory(key: String, value: String): PromptBuilder {
        memoryContext[key] = value
        return this
    }

    fun addHistoryContext(content: Content): PromptBuilder {
        history.add(content)
        return this
    }

    fun addHistory(role: String, text: String): PromptBuilder {
        history.add(
            Content(
                role = role,
                parts = listOf(Part(text = text))
            )
        )
        return this
    }

    fun buildSystemInstruction(): Content? {
        if (systemInstructions.isEmpty() && memoryContext.isEmpty()) return null

        val sb = java.lang.StringBuilder()
        sb.append(systemInstructions.joinToString("\n"))
        sb.append("\n\n=== RELEVANT CONTEXT (MEMORY) ===\n")
        memoryContext.forEach { (key, value) ->
            sb.append("$key: $value\n")
        }

        return Content(parts = listOf(Part(text = sb.toString())))
    }

    fun buildContents(currentUserPrompt: String): List<Content> {
        val contents = history.toMutableList()
        contents.add(Content(role = "user", parts = listOf(Part(text = currentUserPrompt))))
        return contents
    }
}
