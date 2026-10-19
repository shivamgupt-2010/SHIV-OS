package com.example.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.launcher.ComplexityMode
import com.example.core.launcher.LayoutManager
import com.example.core.launcher.ProfileType
import com.example.core.studio.StudioThemeEngine
import com.example.core.studio.StudioThemeTokens
import com.example.ui.components.CosmicBackground
import com.example.ui.components.TrishulIcon
import com.example.ui.theme.*

@Composable
fun OnboardingScreen(
    layoutManager: LayoutManager? = null,
    themeEngine: StudioThemeEngine? = null,
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }

    var selectedComplexity by remember { mutableStateOf(ComplexityMode.BALANCED) }
    var selectedProfile by remember { mutableStateOf(ProfileType.EVERYDAY) }
    var selectedThemePreset by remember { mutableStateOf("COSMIC_SHIVA") }
    var selectedIconShape by remember { mutableStateOf("SQUIRCLE") }

    Box(modifier = Modifier.fillMaxSize()) {
        CosmicBackground(showSilhouette = true)

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
            // Header: Step Indicator & Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 0) {
                    IconButton(onClick = { currentStep-- }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous", tint = TextPrimary)
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { step ->
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (step == currentStep) 24.dp else 10.dp)
                                .clip(CircleShape)
                                .background(if (step == currentStep) ShivaCyan else CosmicCardBorder.copy(alpha = 0.5f))
                        )
                    }
                }

                Text(
                    text = "${currentStep + 1}/5",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body: Step Content
            AnimatedContent(
                targetState = currentStep,
                label = "OnboardingStepTransition",
                modifier = Modifier.weight(1f, fill = false)
            ) { step ->
                when (step) {
                    // STEP 1: Welcome & Vision
                    0 -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TrishulIcon(size = 72.dp)
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Welcome to SHIV Launcher",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "SIMPLE BY DEFAULT.\nPOWERFUL ON DEMAND.\nCUSTOMIZABLE BY EVERYONE.\nAI-POWERED WHEN USEFUL.\nACCESSIBLE WITHOUT COMPROMISE.",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.1.sp,
                                    lineHeight = 22.sp
                                ),
                                color = ShivaCyan,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "A flagship Android launcher integrated with on-device privacy, universal customization, and the cognitive ShivAI multi-agent workforce.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // STEP 2: Complexity Mode
                    1 -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Choose Your Complexity",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tailor how much information and density you want.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            ComplexityMode.entries.forEach { mode ->
                                val isSelected = selectedComplexity == mode
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { selectedComplexity = mode },
                                    color = if (isSelected) CosmicSurfaceVariant else CosmicSurface.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, if (isSelected) ShivaCyan else CosmicCardBorder.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = when (mode) {
                                                ComplexityMode.SIMPLE -> Icons.Default.FilterVintage
                                                ComplexityMode.BALANCED -> Icons.Default.AutoAwesome
                                                ComplexityMode.ADVANCED -> Icons.Default.Terminal
                                            },
                                            contentDescription = null,
                                            tint = if (isSelected) ShivaCyan else TextSecondary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = mode.title,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = mode.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ShivaCyan)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // STEP 3: Starter Experience Profile
                    2 -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Select Your Persona Profile",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "SHIV seeds your dock and home screen with real installed apps matching your daily workflow.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            val topProfiles = listOf(
                                ProfileType.EVERYDAY,
                                ProfileType.STUDENT,
                                ProfileType.DEVELOPER,
                                ProfileType.DESIGNER,
                                ProfileType.EXECUTIVE,
                                ProfileType.FAMILY
                            )

                            topProfiles.forEach { profile ->
                                val isSelected = selectedProfile == profile
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { selectedProfile = profile },
                                    color = if (isSelected) CosmicSurfaceVariant else CosmicSurface.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, if (isSelected) ShivaPurple else CosmicCardBorder.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = when (profile) {
                                                ProfileType.EVERYDAY -> Icons.Default.Home
                                                ProfileType.STUDENT -> Icons.Default.School
                                                ProfileType.DEVELOPER -> Icons.Default.Code
                                                ProfileType.DESIGNER -> Icons.Default.Brush
                                                ProfileType.EXECUTIVE -> Icons.Default.BusinessCenter
                                                ProfileType.FAMILY -> Icons.Default.FamilyRestroom
                                                else -> Icons.Default.Apps
                                            },
                                            contentDescription = null,
                                            tint = if (isSelected) ShivaPurple else TextSecondary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = profile.title,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = profile.description,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ShivaPurple)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // STEP 4: Aesthetics & Theme
                    3 -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Cosmic Theme & Geometry",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Choose your color preset and icon mask.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Theme Presets
                            Text("Color Palette Preset", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary, modifier = Modifier.align(Alignment.Start))
                            Spacer(modifier = Modifier.height(8.dp))

                            StudioThemeTokens.PRESETS.values.take(4).forEach { preset ->
                                val isSelected = selectedThemePreset == preset.key
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedThemePreset = preset.key },
                                    color = if (isSelected) CosmicSurfaceVariant else CosmicSurface.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (isSelected) ShivaCyan else CosmicCardBorder.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(preset.primary))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(preset.accent))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(preset.name, color = TextPrimary, fontWeight = FontWeight.Medium)
                                        }
                                        if (isSelected) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ShivaCyan)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Icon Shape
                            Text("Icon Mask Shape", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary, modifier = Modifier.align(Alignment.Start))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                listOf("SQUIRCLE", "CIRCLE", "ROUNDED_RECT", "TEARDROP").forEach { shape ->
                                    val isSelected = selectedIconShape == shape
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { selectedIconShape = shape },
                                        color = if (isSelected) ShivaCyan.copy(alpha = 0.2f) else CosmicSurface.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, if (isSelected) ShivaCyan else CosmicCardBorder.copy(alpha = 0.3f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(
                                                        when (shape) {
                                                            "CIRCLE" -> CircleShape
                                                            "ROUNDED_RECT" -> RoundedCornerShape(6.dp)
                                                            "TEARDROP" -> RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 2.dp)
                                                            else -> RoundedCornerShape(8.dp)
                                                        }
                                                    )
                                                    .background(if (isSelected) ShivaCyan else TextSecondary)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = shape.take(6).lowercase().replaceFirstChar { it.uppercase() },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isSelected) ShivaCyan else TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // STEP 5: Multimodal ShivAI & Privacy Guard
                    4 -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = ShivaCyan, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Zero-Leakage Privacy & Multi-Agent AI",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "1. App search is 100% on-device; no keystrokes are sent over the network.\n\n" +
                                        "2. Protected apps require biometric authentication through SHIV Lock.\n\n" +
                                        "3. Autonomous actions require explicit user approval.\n\n" +
                                        "4. You can inspect or clear AI memory at any time in the Memory Inspection dashboard.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer: Next / Complete Button
            Button(
                onClick = {
                    if (currentStep < 4) {
                        currentStep++
                    } else {
                        // Apply selections
                        layoutManager?.setComplexityMode(selectedComplexity)
                        layoutManager?.applyProfile(selectedProfile)
                        themeEngine?.applyPreset(selectedThemePreset)
                        themeEngine?.setIconShape(selectedIconShape)
                        onComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShivaIndigo)
            ) {
                Text(
                    text = if (currentStep < 4) "Continue" else "Finish Setup & Launch",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (currentStep < 4) Icons.Default.ArrowForward else Icons.Default.RocketLaunch,
                    contentDescription = null
                )
            }
        }
    }
}
