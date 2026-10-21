package com.example.ui.onboarding

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.ShivIdentityAuth
import com.example.core.launcher.ComplexityMode
import com.example.core.launcher.LayoutManager
import com.example.core.launcher.ProfileType
import com.example.core.studio.StudioThemeEngine
import com.example.core.studio.StudioThemeTokens
import com.example.ui.components.CosmicBackground
import com.example.ui.components.TrishulIcon
import com.example.ui.theme.*
import kotlinx.coroutines.delay

enum class OnboardingStage {
    BOOT_SIGNATURE,
    WELCOME_CAROUSEL,
    SETUP_WIZARD,
    AUTH_SCREEN
}

@Composable
fun OnboardingScreen(
    layoutManager: LayoutManager? = null,
    themeEngine: StudioThemeEngine? = null,
    onComplete: () -> Unit
) {
    var stage by remember { mutableStateOf(OnboardingStage.BOOT_SIGNATURE) }

    var selectedComplexity by remember { mutableStateOf(ComplexityMode.BALANCED) }
    var selectedProfile by remember { mutableStateOf(ProfileType.EVERYDAY) }
    var selectedThemePreset by remember { mutableStateOf("COSMIC_SHIVA") }
    var selectedIconShape by remember { mutableStateOf("SQUIRCLE") }
    var systemAutoTheme by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        CosmicBackground(showSilhouette = true)

        AnimatedContent(
            targetState = stage,
            label = "OnboardingStageTransition"
        ) { currentStage ->
            when (currentStage) {
                // 1. BOOT SIGNATURE ANIMATION
                OnboardingStage.BOOT_SIGNATURE -> {
                    BootSignatureScreen(
                        onFinished = { stage = OnboardingStage.WELCOME_CAROUSEL }
                    )
                }

                // 2. WELCOME CAROUSEL
                OnboardingStage.WELCOME_CAROUSEL -> {
                    WelcomeCarouselScreen(
                        onGetStarted = { stage = OnboardingStage.SETUP_WIZARD },
                        onLoginClicked = { stage = OnboardingStage.AUTH_SCREEN },
                        onSkip = {
                            layoutManager?.setComplexityMode(selectedComplexity)
                            layoutManager?.applyProfile(selectedProfile)
                            themeEngine?.applyPreset(selectedThemePreset)
                            themeEngine?.setIconShape(selectedIconShape)
                            stage = OnboardingStage.AUTH_SCREEN
                        }
                    )
                }

                // 3. 5-STEP SETUP WIZARD
                OnboardingStage.SETUP_WIZARD -> {
                    SetupWizardScreen(
                        selectedComplexity = selectedComplexity,
                        onComplexitySelected = { selectedComplexity = it },
                        selectedProfile = selectedProfile,
                        onProfileSelected = { selectedProfile = it },
                        selectedThemePreset = selectedThemePreset,
                        onThemePresetSelected = { selectedThemePreset = it },
                        selectedIconShape = selectedIconShape,
                        onIconShapeSelected = { selectedIconShape = it },
                        systemAutoTheme = systemAutoTheme,
                        onAutoThemeToggled = { systemAutoTheme = it },
                        onBackToWelcome = { stage = OnboardingStage.WELCOME_CAROUSEL },
                        onCompleteSetup = {
                            layoutManager?.setComplexityMode(selectedComplexity)
                            layoutManager?.applyProfile(selectedProfile)
                            themeEngine?.applyPreset(selectedThemePreset)
                            themeEngine?.setIconShape(selectedIconShape)
                            // Require authentication before entering launcher
                            stage = OnboardingStage.AUTH_SCREEN
                        }
                    )
                }

                // 4. SHIV IDENTITY SHARED AUTH
                OnboardingStage.AUTH_SCREEN -> {
                    ShivIdentityAuthScreen(
                        onBack = { stage = OnboardingStage.WELCOME_CAROUSEL },
                        onAuthSuccess = {
                            stage = OnboardingStage.SETUP_WIZARD
                        },
                        onContinueGuest = {
                            stage = OnboardingStage.SETUP_WIZARD
                        }
                    )
                }
            }
        }
    }
}

// =========================================================================================
// STAGE 1: BOOT SIGNATURE ANIMATION
// =========================================================================================
@Composable
fun BootSignatureScreen(onFinished: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "BootGlow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    var textAlpha by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        delay(600)
        textAlpha = 1f
        delay(2200)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onFinished() }
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glowing Trishul Ribbon
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(ShivaCyan.copy(alpha = 0.35f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                TrishulIcon(size = 80.dp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Emergence of Wordmark
            AnimatedVisibility(
                visible = textAlpha > 0f,
                enter = fadeIn(tween(800)) + expandVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SHIV LAUNCHER",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 4.sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Consciousness Meets Intelligence",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Light
                        ),
                        color = ShivaCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Tap anywhere to continue",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary.copy(alpha = 0.6f)
            )
        }
    }
}

// =========================================================================================
// STAGE 2: WELCOME CAROUSEL & PREVIEWS
// =========================================================================================
@Composable
fun WelcomeCarouselScreen(
    onGetStarted: () -> Unit,
    onLoginClicked: () -> Unit,
    onSkip: () -> Unit
) {
    var activeSlide by remember { mutableIntStateOf(0) }

    val slides = listOf(
        Pair("Your device. Your way.", "Clean, distraction-free homescreen with infinite customization and lightning responsiveness."),
        Pair("Everything in your flow.", "Fluid gestures, dock customization, and smart widgets that fit your daily rhythm."),
        Pair("Meet your AI workspace.", "ShivAI autonomous multi-agent workforce designed to assist, plan, and research on demand."),
        Pair("Built around you.", "Zero telemetry leakage, local biometric app locking, and complete on-device privacy.")
    )

    val previewPresets = listOf("Minimal", "Colorful", "Productivity", "Studio", "Gaming", "Classic", "Custom")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top row with Skip button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TrishulIcon(size = 28.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Welcome to SHIV Launcher", fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ShivTheme.colors.primary.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = ShivTheme.colors.primary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Protected", style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Center: Interactive Carousel
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = activeSlide,
                label = "WelcomeSlideContent"
            ) { idx ->
                val (title, description) = slides[idx]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(ShivTheme.colors.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (idx) {
                                0 -> Icons.Default.Smartphone
                                1 -> Icons.Default.AllInclusive
                                2 -> Icons.Default.AutoAwesome
                                else -> Icons.Default.Shield
                            },
                            contentDescription = null,
                            tint = ShivaCyan,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Carousel Dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                slides.indices.forEach { i ->
                    Box(
                        modifier = Modifier
                            .size(if (i == activeSlide) 20.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(if (i == activeSlide) ShivaCyan else CosmicCardBorder.copy(alpha = 0.6f))
                            .clickable { activeSlide = i }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Personalization Preview Presets Strip
            Text(
                text = "PERSONALIZATION PREVIEWS",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold),
                color = TextSecondary,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(previewPresets) { preset ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CosmicSurfaceVariant,
                        border = BorderStroke(1.dp, CosmicCardBorder.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = preset,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Action Buttons
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onGetStarted,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShivaIndigo)
            ) {
                Text("Get Started", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }

            OutlinedButton(
                onClick = onLoginClicked,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CosmicCardBorder)
            ) {
                Text("I have a SHIV Identity account", color = TextPrimary)
            }
        }
    }
}

// =========================================================================================
// STAGE 3: 5-STEP SETUP WIZARD
// =========================================================================================
@Composable
fun SetupWizardScreen(
    selectedComplexity: ComplexityMode,
    onComplexitySelected: (ComplexityMode) -> Unit,
    selectedProfile: ProfileType,
    onProfileSelected: (ProfileType) -> Unit,
    selectedThemePreset: String,
    onThemePresetSelected: (String) -> Unit,
    selectedIconShape: String,
    onIconShapeSelected: (String) -> Unit,
    systemAutoTheme: Boolean,
    onAutoThemeToggled: (Boolean) -> Unit,
    onBackToWelcome: () -> Unit,
    onCompleteSetup: () -> Unit
) {
    var stepIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Step Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (stepIndex > 0) stepIndex--
                else onBackToWelcome()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }

            // Step Indicator Dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { s ->
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (s == stepIndex) 22.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (s == stepIndex) ShivaCyan else CosmicCardBorder.copy(alpha = 0.5f))
                    )
                }
            }

            Text("Step ${stepIndex + 1}/5", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step Body
        AnimatedContent(
            targetState = stepIndex,
            label = "SetupStepsContent",
            modifier = Modifier.weight(1f, fill = false)
        ) { currentStep ->
            when (currentStep) {
                // STEP 1: EXPERIENCE PROFILES (12 Profiles)
                0 -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Choose Your Complexity & Select Your Persona Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Tailors home screen layout and dock to your workflow.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        ProfileType.entries.forEach { profile ->
                            val isSel = selectedProfile == profile
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onProfileSelected(profile) },
                                color = if (isSel) CosmicSurfaceVariant else CosmicSurface.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, if (isSel) ShivaCyan else CosmicCardBorder.copy(alpha = 0.3f))
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        when (profile) {
                                            ProfileType.STUDENT -> Icons.Default.School
                                            ProfileType.DEVELOPER -> Icons.Default.Code
                                            ProfileType.DESIGNER -> Icons.Default.Brush
                                            ProfileType.BUSINESS_OWNER -> Icons.Default.BusinessCenter
                                            ProfileType.FAMILY -> Icons.Default.FamilyRestroom
                                            ProfileType.ACCESSIBILITY -> Icons.Default.Accessibility
                                            else -> Icons.Default.Apps
                                        },
                                        contentDescription = null,
                                        tint = if (isSel) ShivaCyan else TextSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(profile.title, fontWeight = FontWeight.SemiBold, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                                        Text(profile.description, style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    if (isSel) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ShivaCyan, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // STEP 2: THEME SELECTION (11 Themes + Auto Toggle)
                1 -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Cosmic Theme & Geometry", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Cosmic and adaptive color palettes.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Auto Light/Dark Switch
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = CosmicSurfaceVariant,
                            border = BorderStroke(1.dp, CosmicCardBorder.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Match System Light / Dark", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("Auto-switches with OS display mode", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                                Switch(checked = systemAutoTheme, onCheckedChange = onAutoThemeToggled)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        StudioThemeTokens.PRESETS.values.take(8).forEach { preset ->
                            val isSel = selectedThemePreset == preset.key
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onThemePresetSelected(preset.key) },
                                color = if (isSel) CosmicSurfaceVariant else CosmicSurface.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, if (isSel) ShivaCyan else CosmicCardBorder.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(preset.primary))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(preset.accent))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(preset.name, fontWeight = FontWeight.Medium, color = TextPrimary)
                                    }
                                    if (isSel) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ShivaCyan, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // STEP 3: HOME LAYOUT & ICON GEOMETRY
                2 -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Layout Density & Geometry", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Configure home screen grid and icon shape.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Complexity options
                        ComplexityMode.entries.forEach { mode ->
                            val isSel = selectedComplexity == mode
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onComplexitySelected(mode) },
                                color = if (isSel) CosmicSurfaceVariant else CosmicSurface.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, if (isSel) ShivaCyan else CosmicCardBorder.copy(alpha = 0.3f))
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(mode.title, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                        Text(mode.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                    if (isSel) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ShivaCyan)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Icon Shape
                        Text("Icon Mask Shape", fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.align(Alignment.Start))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("SQUIRCLE", "CIRCLE", "ROUNDED_RECT", "TEARDROP").forEach { shape ->
                                val isSel = selectedIconShape == shape
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onIconShapeSelected(shape) },
                                    color = if (isSel) ShivaCyan.copy(alpha = 0.2f) else CosmicSurface.copy(alpha = 0.6f),
                                    border = BorderStroke(1.dp, if (isSel) ShivaCyan else CosmicCardBorder.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(
                                                    when (shape) {
                                                        "CIRCLE" -> CircleShape
                                                        "ROUNDED_RECT" -> RoundedCornerShape(4.dp)
                                                        "TEARDROP" -> RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomEnd = 10.dp, bottomStart = 2.dp)
                                                        else -> RoundedCornerShape(6.dp)
                                                    }
                                                )
                                                .background(if (isSel) ShivaCyan else TextSecondary)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(shape.take(4), style = MaterialTheme.typography.labelSmall, color = if (isSel) ShivaCyan else TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }

                // STEP 4: PERMISSIONS WITH SKIP
                3 -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ShivaCyan, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Zero-Leakage Privacy & Multi-Agent AI", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("System Integrations & Privacy: All permissions are optional and kept 100% on-device.", style = MaterialTheme.typography.bodySmall, color = TextSecondary, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))

                        val perms = listOf(
                            Triple("Usage Access", "Required for screen-time and wellbeing dashboard stats", Icons.Default.Equalizer),
                            Triple("Notification Access", "Enables unread badge counts on dock icons", Icons.Default.Notifications),
                            Triple("Assistant Overlay", "Allows quick floating access to ShivAI", Icons.Default.Layers),
                            Triple("Voice Capture", "Enables natural speech queries to ShivAI", Icons.Default.Mic)
                        )

                        perms.forEach { (name, explanation, icon) ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = CosmicSurfaceVariant,
                                border = BorderStroke(1.dp, CosmicCardBorder.copy(alpha = 0.3f))
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(icon, contentDescription = null, tint = ShivaCyan, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(name, fontWeight = FontWeight.SemiBold, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                                        Text(explanation, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    }
                                    TextButton(onClick = { /* Perm request */ }) {
                                        Text("Grant", color = ShivaCyan, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // STEP 5: LIVE HOME PREVIEW & COMPLETION
                4 -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = ShivaCyan, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Your SHIV Workspace is Ready", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Here is your personalized setup preview:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = CosmicSurfaceVariant,
                            border = BorderStroke(1.dp, ShivaCyan)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Selected Profile", color = TextSecondary)
                                    Text(selectedProfile.title, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Theme Preset", color = TextSecondary)
                                    Text(selectedThemePreset, fontWeight = FontWeight.Bold, color = ShivaCyan)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Density Mode", color = TextSecondary)
                                    Text(selectedComplexity.title, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Icon Shape", color = TextSecondary)
                                    Text(selectedIconShape, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bottom Button
        Button(
            onClick = {
                if (stepIndex < 4) stepIndex++
                else onCompleteSetup()
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ShivaIndigo)
        ) {
            Text(
                if (stepIndex < 4) "Next Step →" else "Enter SHIV Launcher →",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

// =========================================================================================
// STAGE 4: SHIV IDENTITY AUTH (SHARED LOGIN & REGISTRATION)
// =========================================================================================
@Composable
fun ShivIdentityAuthScreen(
    onBack: () -> Unit,
    onAuthSuccess: () -> Unit,
    onContinueGuest: () -> Unit
) {
    val context = LocalContext.current
    var isSignUpMode by remember { mutableStateOf(false) }

    var usernameOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailForRegister by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            TextButton(onClick = onContinueGuest) {
                Text("Continue as Guest", color = ShivaCyan, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            TrishulIcon(size = 48.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("SHIV Identity", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Sign in with your unified SHIV ecosystem account", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(20.dp))

            // Switcher Tabs
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { isSignUpMode = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (!isSignUpMode) ShivaIndigo else CosmicSurfaceVariant)
                ) {
                    Text("Sign In")
                }
                Button(
                    onClick = { isSignUpMode = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSignUpMode) ShivaIndigo else CosmicSurfaceVariant)
                ) {
                    Text("Create Account")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Inputs
            OutlinedTextField(
                value = usernameOrEmail,
                onValueChange = { usernameOrEmail = it },
                label = { Text("Username or Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )

            if (isSignUpMode) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = emailForRegister,
                    onValueChange = { emailForRegister = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                    Text("Remember me", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                }
                if (!isSignUpMode) {
                    TextButton(onClick = {
                        Toast.makeText(context, "Visit https://shiv-database.onrender.com/account/recover", Toast.LENGTH_LONG).show()
                    }) {
                        Text("Forgot password?", style = MaterialTheme.typography.labelSmall, color = ShivaCyan)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = {
                    isLoading = true
                    if (isSignUpMode) {
                        ShivIdentityAuth.registerAccount(context, usernameOrEmail, emailForRegister, password) { success, _ ->
                            isLoading = false
                            if (success) {
                                Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                onAuthSuccess()
                            }
                        }
                    } else {
                        ShivIdentityAuth.loginWithCredentials(context, usernameOrEmail, password, rememberMe) { success, _ ->
                            isLoading = false
                            if (success) {
                                Toast.makeText(context, "Welcome to SHIV Identity!", Toast.LENGTH_SHORT).show()
                                onAuthSuccess()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShivaCyan)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                } else {
                    Text(if (isSignUpMode) "Create SHIV Account" else "Sign In with SHIV", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { ShivIdentityAuth.startSignIn(context) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CosmicCardBorder)
            ) {
                Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = TextPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Continue via Web Browser (PKCE)", color = TextPrimary)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Non-mandatory: SHIV Launcher functions completely offline without an account.",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}
