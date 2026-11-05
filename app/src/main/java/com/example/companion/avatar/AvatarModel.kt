package com.example.companion.avatar

/**
 * Core Avatar Domain Models & State Definitions.
 * Governs the 3 unified avatar personas, progressive 3-minute sleep stages,
 * situational reactions, and physical UI touch mechanics.
 */
enum class AvatarType(val title: String, val subtitle: String) {
    INDIE_CACOONAUT("Indie Cacoonaut", "The Sacred Cocoon & Celestial Diya Spirit"),
    PHANTOM_NEKO("Phantom Neko", "The Mystic Fox-Cat & Neon Specter"),
    COSMIC_SHIV("Authentic Cosmic Shiv", "The Sovereign Cyber-Celestial Spirit")
}

enum class AvatarState {
    IDLE,
    LISTENING,
    THINKING,
    PROCESSING,
    SPEAKING,
    DROWSY,
    SLEEPING,
    ALERT,
    STRICT_COACH,
    ERROR,
    CELEBRATING,
    TOUCH_DRAGGING,
    FLYING_TO_TARGET,
    TAP_CONTACT,
    HEAVY_PULL,
    DISMISS_PUSH,
    HAPPY,
    CURIOUS,
    WORKING,
    CONFIDENT,
    CONFUSED
}

enum class InactivitySleepStage(val targetFps: Int, val description: String) {
    ENGAGED_60FPS(60, "Active engagement, dynamic eye contact, 60-120 FPS"),
    AMBIENT_30FPS(30, "Ambient watch mode, casual glances, 30 FPS"),
    DROWSY_15FPS(15, "Drowsy fatigue fight, drooping eyelids, nodding head, 15 FPS"),
    DEEP_SLUMBER_0FPS(0, "Deep hibernation, static frozen vector, 0 CPU wake-locks, 0 FPS")
}

enum class SituationalEvent {
    NONE,
    CHARGING_SURGE,
    PHONE_SHAKE_TUMBLE,
    BATTERY_LOW_REMINDER,
    REASONING_CHAIN_OF_THOUGHT
}

enum class SeasonalSkin(val title: String) {
    DEFAULT("Sovereign Classic"),
    DIWALI_FESTIVE("Diwali Sparklers & Diya"),
    WINTER_FROST("Winter Ice Crystals & Mist"),
    MONSOON_RAIN("Monsoon Water Splashes"),
    DARK_TORCH("Conical Spotlight Torch")
}

enum class TouchActionType {
    TAP,
    DRAG,
    PUSH,
    TYPE
}

data class FlightTarget(
    val targetX: Float,
    val targetY: Float,
    val actionType: TouchActionType = TouchActionType.TAP,
    val label: String = "",
    val durationMs: Long = 260L
)

data class AvatarRenderContext(
    val avatarType: AvatarType = AvatarType.COSMIC_SHIV,
    val state: AvatarState = AvatarState.IDLE,
    val sleepStage: InactivitySleepStage = InactivitySleepStage.ENGAGED_60FPS,
    val seasonalSkin: SeasonalSkin = SeasonalSkin.DEFAULT,
    val situationalEvent: SituationalEvent = SituationalEvent.NONE,
    val audioAmplitude: Float = 0f, // 0.0 to 1.0 for mouth phoneme sync
    val touchTensionAngle: Float = 0f, // Tilt angle during heavy drags
    val isGhostForm: Boolean = false, // Phantom Neko form morph toggle
    val darkScreenLuminance: Float = 1.0f // 0.0 (pitch dark) to 1.0 (bright)
)
