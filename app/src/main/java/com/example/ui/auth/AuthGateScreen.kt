package com.example.ui.auth

import android.app.Activity
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.ShivIdentityAuth
import com.example.core.identity.UserProfileManager
import com.example.core.lock.AppLockManager
import com.example.core.lock.LockMethod
import com.example.ui.components.CosmicBackground
import com.example.ui.components.TrishulIcon
import com.example.ui.security.NumericKeypad
import com.example.ui.security.PatternLockView
import com.example.ui.security.PinDotsIndicator
import com.example.ui.security.SecurityPinCreationDialog
import com.example.ui.security.SecurityRecoveryResetDialog
import com.example.ui.theme.ShivTheme
import kotlinx.coroutines.delay

/**
 * AuthGateScreen:
 * Strict Authentication Gate. Automatically adapts to the user's active LockMethod
 * (PIN, Pattern, Password, Biometrics) and supports recovery question reset,
 * biometric verification, and SHIV Identity Cloud OAuth.
 */
@Composable
fun AuthGateScreen(
    userProfileManager: UserProfileManager,
    appLockManager: AppLockManager,
    onAuthenticated: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val isCredentialSet = remember { appLockManager.isMasterPinSet() }
    val lockMethod by appLockManager.lockMethod.collectAsState()

    var showSetupDialog by remember { mutableStateOf(!isCredentialSet) }
    var showRecoveryDialog by remember { mutableStateOf(false) }

    var enteredPin by remember { mutableStateOf("") }
    var enteredPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val remainingLockoutState by appLockManager.lockoutUntilTimestamp.collectAsState()
    var remainingLockout by remember { mutableStateOf(appLockManager.getRemainingLockoutSeconds()) }

    // Lockout countdown timer
    LaunchedEffect(remainingLockoutState, remainingLockout) {
        if (remainingLockout > 0) {
            delay(1000L)
            remainingLockout = appLockManager.getRemainingLockoutSeconds()
        }
    }

    // Auto-trigger biometrics if selected method is BIOMETRICS
    LaunchedEffect(Unit) {
        if (lockMethod == LockMethod.BIOMETRICS && activity != null && remainingLockout == 0 && isCredentialSet) {
            appLockManager.authenticateWithBiometrics(
                activity,
                title = "Unlock SHIV-OS Sentinel",
                subtitle = "Touch fingerprint sensor or look at camera"
            ) { success ->
                if (success) {
                    userProfileManager.markAuthenticated("BIOMETRIC")
                    onAuthenticated()
                }
            }
        }
    }

    if (showSetupDialog) {
        SecurityPinCreationDialog(
            appLockManager = appLockManager,
            onPinCreated = {
                showSetupDialog = false
                userProfileManager.markAuthenticated("MASTER_SETUP")
                onAuthenticated()
            },
            onDismiss = {
                // Cannot dismiss gate without setup if lock is enabled
            }
        )
    }

    if (showRecoveryDialog) {
        SecurityRecoveryResetDialog(
            appLockManager = appLockManager,
            onSuccess = {
                showRecoveryDialog = false
                userProfileManager.markAuthenticated("RECOVERY_RESET")
                onAuthenticated()
            },
            onDismiss = { showRecoveryDialog = false }
        )
    }

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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(ShivTheme.colors.primary.copy(alpha = 0.25f), ShivTheme.colors.accent.copy(alpha = 0.25f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    TrishulIcon(size = 32.dp, tintGradient = listOf(ShivTheme.colors.primary, ShivTheme.colors.accent))
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "SHIV Launcher Sentinel",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = when (lockMethod) {
                        LockMethod.PIN -> "Enter your 4-8 digit Master PIN"
                        LockMethod.PATTERN -> "Draw your Master Pattern to unlock"
                        LockMethod.PASSWORD -> "Enter your Master Password"
                        LockMethod.BIOMETRICS -> "Verify Biometrics or enter fallback PIN"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )
            }

            // Central Authentication Input Area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (remainingLockout > 0) {
                    Text(
                        text = "Too many failed attempts. Wait ${remainingLockout}s",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                } else if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                when (lockMethod) {
                    LockMethod.PIN, LockMethod.BIOMETRICS -> {
                        PinDotsIndicator(
                            count = 4,
                            filledCount = enteredPin.length.coerceAtMost(4),
                            hasError = errorMessage != null
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        NumericKeypad(
                            onDigitClick = { digit ->
                                if (remainingLockout > 0) return@NumericKeypad
                                errorMessage = null
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
                            },
                            onBackspaceClick = {
                                errorMessage = null
                                if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                            }
                        )

                        if (lockMethod == LockMethod.BIOMETRICS && activity != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    appLockManager.authenticateWithBiometrics(
                                        activity,
                                        title = "Unlock SHIV-OS",
                                        subtitle = "Verify fingerprint or face"
                                    ) { success ->
                                        if (success) {
                                            userProfileManager.markAuthenticated("BIOMETRIC")
                                            onAuthenticated()
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ShivTheme.colors.primary)
                            ) {
                                Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scan Fingerprint / Face")
                            }
                        }
                    }

                    LockMethod.PATTERN -> {
                        PatternLockView(
                            onPatternCompleted = { pattern ->
                                if (remainingLockout > 0) return@PatternLockView
                                errorMessage = null
                                if (appLockManager.verifyMasterPattern(pattern)) {
                                    userProfileManager.markAuthenticated("MASTER_PATTERN")
                                    onAuthenticated()
                                } else {
                                    errorMessage = "Pattern incorrect"
                                }
                            },
                            isError = errorMessage != null,
                            modifier = Modifier.size(280.dp)
                        )
                    }

                    LockMethod.PASSWORD -> {
                        OutlinedTextField(
                            value = enteredPassword,
                            onValueChange = {
                                enteredPassword = it
                                errorMessage = null
                            },
                            label = { Text("Password") },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = ShivTheme.colors.primary,
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.fillMaxWidth(0.85f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (remainingLockout > 0) return@Button
                                if (appLockManager.verifyMasterPassword(enteredPassword)) {
                                    userProfileManager.markAuthenticated("MASTER_PASSWORD")
                                    onAuthenticated()
                                } else {
                                    errorMessage = "Incorrect Password"
                                }
                            },
                            modifier = Modifier.fillMaxWidth(0.85f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.primary),
                            enabled = enteredPassword.isNotBlank()
                        ) {
                            Text("Unlock Sentinel", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Forgot Credentials / Recovery Option
                TextButton(
                    onClick = { showRecoveryDialog = true }
                ) {
                    Text(
                        "Forgot credentials? Recover with security question",
                        color = ShivTheme.colors.accent,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Bottom OAuth and Account Options
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedButton(
                    onClick = {
                        ShivIdentityAuth.startSignIn(context)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
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

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Encrypted local authentication • Zero telemetry leakage",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}
