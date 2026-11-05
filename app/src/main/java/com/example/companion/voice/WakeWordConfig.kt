package com.example.companion.voice

/**
 * Wake Word Configuration.
 * Default wake word: "Om Namas Shivaya" (and sacred variations),
 * plus fast conversational triggers ("Hey Shiv", "Shiv").
 */
object WakeWordConfig {
    const val DEFAULT_PRIMARY_WAKE_WORD = "om namas shivaya"

    val SACRED_WAKE_PATTERNS = listOf(
        "om namas shivaya",
        "om namah shivaya",
        "om namah shivay",
        "om namah shivaye",
        "ॐ नमः शिवाय",
        "नमः शिवाय",
        "hey shiv",
        "he shiv",
        "namaste shiv",
        "sun shiv",
        "suno shiv",
        "शिव",
        "नमस्ते शिव",
        "हे शिव"
    )

    fun matchesWakeWord(transcript: String): Boolean {
        val clean = transcript.trim().lowercase()
            .replace("[.,!?;:]".toRegex(), "")
        return SACRED_WAKE_PATTERNS.any { pattern ->
            clean.contains(pattern, ignoreCase = true)
        }
    }
}
