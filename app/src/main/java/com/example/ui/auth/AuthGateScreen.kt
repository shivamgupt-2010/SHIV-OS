package com.example.ui.auth

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.ShivIdentityAuth
import com.example.core.identity.UserProfileManager
import com.example.core.lock.AppLockManager
import com.example.ui.components.CosmicBackground
import com.example.ui.components.TridentIcon
import com.example.ui.security.NumericKeypad
import com.example.ui.security.PinDotsIndicator
import com.example.ui.theme.ShivTheme

/**
 * AuthGateScreen:
 * Strict Authentication Gate. No user can enter the launcher without signing in
 * via SHIV Identity or verifying/setting up their local Master Security PIN.
 */
@Composable
fun AuthGateScreen(
    userProfileManager: UserProfileManager,
    appLockManager: AppLockManager,
    onAuthenticated: () -> Unit
) {
    val context = LocalContext.current
    val isPinSet = remember { appLockManager.isMasterPinSet() }
    var authMode by remember { mutableStateOf(if (isPinSet) "ENTER_PIN" else "SETUP_PIN") }
    var enteredPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val remainingLockout by appLockManager.lockoutUntilTimestamp.collectAsState()

    val lockoutSeconds = remember(remainingLockout) { appLockManager.getRemainingLockoutSeconds() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07080E))
    ) {
        CosmicBackground(showSilhouette = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 36.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(ShivTheme.colors.primary.copy(alpha = 0.25f), ShivTheme.colors.accent.copy(alpha = 0.25f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    TridentIcon(tint = ShivTheme.colors.primary, size = 36.dp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SHIV Launcher Sentinel",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = when (authMode) {
                        "ENTER_PIN" -> "Enter your Master PIN to unlock device"
                        "SETUP_PIN" -> if (!isConfirming) "Create a 4-8 digit Master Security PIN" else "Confirm your Master Security PIN"
                        else -> "Sign in to authenticate"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )
            }

            // Central PIN / Keypad Area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                val currentInput = if (authMode == "SETUP_PIN" && isConfirming) confirmPin else enteredPin

                PinDotsIndicator(
                    count = 4,
                    filledCount = currentInput.length.coerceAtMost(4),
                    hasError = errorMessage != null
                )

                if (lockoutSeconds > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Too many failed attempts. Wait ${lockoutSeconds}s",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                } else if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                NumericKeypad(
                    onDigitClick = { digit ->
                        if (lockoutSeconds > 0) return@NumericKeypad
                        errorMessage = null

                        if (authMode == "ENTER_PIN") {
                            if (enteredPin.length < 8) {
                                val nextPin = enteredPin + digit
                                enteredPin = nextPin
                                if (nextPin.length >= 4) {
                                    if (appLockManager.verifyMasterPin(nextPin)) {
                                        userProfileManager.markAuthenticated("MASTER_PIN")
                                        onAuthenticated()
                                    } else if (nextPin.length >= 6) {
                                        errorMessage = "Incorrect PIN"
                                        enteredPin = ""
                                    }
                                }
                            }
                        } else if (authMode == "SETUP_PIN") {
                            if (!isConfirming) {
                                if (enteredPin.length < 8) enteredPin += digit
                            } else {
                                if (confirmPin.length < 8) {
                                    val nextConfirm = confirmPin + digit
                                    confirmPin = nextConfirm
                                    if (nextConfirm.length == enteredPin.length) {
                                        if (nextConfirm == enteredPin) {
                                            appLockManager.setMasterPin(enteredPin)
                                            userProfileManager.markAuthenticated("MASTER_PIN")
                                            onAuthenticated()
                                        } else {
                                            errorMessage = "PINs did not match. Restarting."
                                            enteredPin = ""
                                            confirmPin = ""
                                            isConfirming = false
                                        }
                                    }
                                }
                            }
                        }
                    },
                    onBackspaceClick = {
                        errorMessage = null
                        if (authMode == "ENTER_PIN" || (!isConfirming)) {
                            if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                        } else {
                            if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                        }
                    }
                )

                if (authMode == "SETUP_PIN" && !isConfirming) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (enteredPin.length < 4) {
                                errorMessage = "PIN must be at least 4 digits"
                            } else {
                                isConfirming = true
                                errorMessage = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.7f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.primary),
                        enabled = enteredPin.length >= 4
                    ) {
                        Text("Confirm PIN →", color = Color.White)
                    }
                }
            }

            // Bottom OAuth and Account Options
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedButton(
                    onClick = {
                        ShivIdentityAuth.startSignIn(context)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = ShivTheme.colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Sign in with SHIV Identity Cloud", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Encrypted local authentication • Zero telemetry leakage",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}
