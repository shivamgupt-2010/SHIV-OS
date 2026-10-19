package com.example.core.launcher

enum class ComplexityMode(
    val title: String,
    val description: String
) {
    SIMPLE(
        title = "Simple Mode",
        description = "Large touch targets, distraction-free essentials, high-contrast clarity"
    ),
    BALANCED(
        title = "Balanced Mode",
        description = "Flagship launcher experience — cognitive capsule, quick search, smart grid"
    ),
    ADVANCED(
        title = "Advanced Mode",
        description = "High-density grid, multi-agent telemetry, developer tools and terminal shortcuts"
    );

    companion object {
        fun fromName(name: String?): ComplexityMode {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: BALANCED
        }
    }
}

enum class ProfileType(
    val title: String,
    val description: String,
    val recommendedComplexity: ComplexityMode,
    val searchKeywords: List<String>
) {
    EVERYDAY(
        title = "Everyday Standard",
        description = "Essential communications, web browsing, weather, camera and daily tasks",
        recommendedComplexity = ComplexityMode.BALANCED,
        searchKeywords = listOf("phone", "dialer", "message", "sms", "chrome", "browser", "camera", "clock", "calendar", "maps")
    ),
    STUDENT(
        title = "Student & Academic",
        description = "Focused study toolkit: Calculator, notes, documents, drive, and ShivAI Study Agent",
        recommendedComplexity = ComplexityMode.BALANCED,
        searchKeywords = listOf("calc", "note", "doc", "pdf", "drive", "sheet", "dict", "reader", "classroom", "meet", "study")
    ),
    TEACHER(
        title = "Teacher & Educator",
        description = "Lesson planning, calendar, notes, attendance, spreadsheets, and video conferencing",
        recommendedComplexity = ComplexityMode.BALANCED,
        searchKeywords = listOf("calendar", "classroom", "note", "sheet", "doc", "presentation", "meet", "zoom", "timer", "clock")
    ),
    DESIGNER(
        title = "Creative Designer",
        description = "Visual inspiration, photo gallery, sketch, color tools, canvas, and portfolio",
        recommendedComplexity = ComplexityMode.BALANCED,
        searchKeywords = listOf("photo", "gallery", "draw", "sketch", "paint", "canvas", "design", "pinterest", "camera", "color")
    ),
    VIDEO_EDITOR(
        title = "Video Editor",
        description = "Quick camera capture, storage cleaner, media gallery, video players, and audio editors",
        recommendedComplexity = ComplexityMode.ADVANCED,
        searchKeywords = listOf("camera", "video", "capcut", "kinemaster", "gallery", "record", "audio", "file", "storage", "player")
    ),
    MARKETER(
        title = "Growth & Marketer",
        description = "Social media, web analytics, email campaigns, notes, calendar, and content scheduling",
        recommendedComplexity = ComplexityMode.BALANCED,
        searchKeywords = listOf("analytics", "ads", "social", "mail", "gmail", "linkedin", "twitter", "calendar", "note", "browser")
    ),
    DEVELOPER(
        title = "Developer & Engineer",
        description = "Terminal, GitHub, file manager, settings, code scratchpad, system monitor, and ShivAI Coding Agent",
        recommendedComplexity = ComplexityMode.ADVANCED,
        searchKeywords = listOf("term", "terminal", "git", "github", "code", "ide", "file", "setting", "browser", "chrome")
    ),
    BUSINESS_OWNER(
        title = "Business & Retail",
        description = "Point of Sale, calculator, billing, inventory, WhatsApp Business, banking, and invoices",
        recommendedComplexity = ComplexityMode.BALANCED,
        searchKeywords = listOf("calc", "bank", "pay", "invoice", "pos", "business", "sheet", "note", "message", "call")
    ),
    EXECUTIVE(
        title = "Executive & Manager",
        description = "Executive briefings, stock monitor, calendar, email, task tracking, and priority contacts",
        recommendedComplexity = ComplexityMode.ADVANCED,
        searchKeywords = listOf("mail", "outlook", "gmail", "calendar", "stock", "finance", "slack", "teams", "note", "news")
    ),
    CREATOR(
        title = "Content Creator",
        description = "Camera, microphone, voice recorder, YouTube Studio, social suites, and thumbnail editing",
        recommendedComplexity = ComplexityMode.BALANCED,
        searchKeywords = listOf("camera", "record", "studio", "youtube", "instagram", "tiktok", "voice", "gallery", "edit", "photo")
    ),
    FAMILY(
        title = "Family & Kids Safe",
        description = "Simplified large dialer, camera, gallery, kids educational games, and emergency SOS",
        recommendedComplexity = ComplexityMode.SIMPLE,
        searchKeywords = listOf("phone", "dialer", "message", "camera", "gallery", "photo", "game", "kids", "safe")
    ),
    ACCESSIBILITY(
        title = "Universal Accessibility",
        description = "High contrast, large touch targets, voice-first capsule, haptic feedback, and screen reader helpers",
        recommendedComplexity = ComplexityMode.SIMPLE,
        searchKeywords = listOf("phone", "dialer", "message", "assist", "voice", "setting", "clock", "camera")
    );

    companion object {
        fun fromName(name: String?): ProfileType {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: EVERYDAY
        }
    }
}
