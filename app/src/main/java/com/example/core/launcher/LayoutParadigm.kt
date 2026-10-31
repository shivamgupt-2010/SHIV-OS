package com.example.core.launcher

enum class LayoutParadigm(
    val key: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val badge: String = "PARADIGM"
) {
    GRID(
        key = "GRID",
        title = "Adaptive Grid",
        subtitle = "Fluid App Matrix",
        description = "Standard balanced Android grid with smart predictive dock and liquid spring reflow."
    ),
    TASKBAR_WINDOWING(
        key = "TASKBAR_WINDOWING",
        title = "Taskbar & Freeform",
        subtitle = "Desktop Class Multitasking",
        description = "Persistent translucent taskbar with running app dots, quick settings, and floating window cards.",
        badge = "DESKTOP"
    ),
    TABBED_SIDEBAR(
        key = "TABBED_SIDEBAR",
        title = "Tabbed Smart Sidebar",
        subtitle = "Edge-Swipe Productivity",
        description = "Multi-shelf edge drawer (Favorites, Tools, Media, Telemetry) with time-of-day auto-reordering.",
        badge = "PRODUCTIVE"
    ),
    TEXT_ONLY_MINIMAL(
        key = "TEXT_ONLY_MINIMAL",
        title = "Text-Only Minimalist",
        subtitle = "Mindful Digital Declutter",
        description = "Zero app icons, zero notification dots, pure distraction-free typography with instant cross-fades.",
        badge = "MINDFUL"
    ),
    SEARCH_GATED(
        key = "SEARCH_GATED",
        title = "Search-Gated Access",
        subtitle = "Intentional Focus Screen",
        description = "Clean empty canvas by default; apps dynamically emerge only as you type queries.",
        badge = "FOCUS"
    ),
    GLASSMORPHIC_BLUR(
        key = "GLASSMORPHIC_BLUR",
        title = "Glassmorphic Depth",
        subtitle = "Frosted Acrylic Luxury",
        description = "Deep Gaussian frosted blur, translucent smoked surfaces, and 3D specular light-beveled edges.",
        badge = "LUXURY"
    ),
    MAGAZINE_EDITORIAL(
        key = "MAGAZINE_EDITORIAL",
        title = "Magazine Editorial",
        subtitle = "High-Fashion Asymmetry",
        description = "Asymmetric column spans, oversized serif headlines, daily quote masthead, and page-curl transitions.",
        badge = "EDITORIAL"
    ),
    TERMINAL_HACKER(
        key = "TERMINAL_HACKER",
        title = "Terminal CLI Console",
        subtitle = "Cyberpunk Monospace Shell",
        description = "Phosphor green/amber on matte black, interactive shell prompt, live telemetry, and matrix boot cascade.",
        badge = "HACKER"
    ),
    HOLOGRAPHIC_3D(
        key = "HOLOGRAPHIC_3D",
        title = "3D Holographic Parallax",
        subtitle = "Spatial Perspective Depth",
        description = "Multi-layer cards with physical gyroscope/touch parallax tilt, specular sheen, and floating Z-depth.",
        badge = "SPATIAL"
    );

    companion object {
        fun fromKey(key: String?): LayoutParadigm {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: GRID
        }
    }
}
