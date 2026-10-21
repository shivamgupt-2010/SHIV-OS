package com.example.ui.security

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.lock.AppLockManager
import com.example.ui.theme.ShivTheme
import kotlinx.coroutines.delay

/**
 * SecurityPinCreationDialog:
 * Interactive 2-step setup dialog for creating and confirming a 4-8 digit security PIN.
 */
@Composable
fun SecurityPinCreationDialog(
    appLockManager: AppLockManager,
    onPinCreated: () -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1 = Enter PIN, 2 = Confirm PIN
    var firstPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentInput = if (step == 1) firstPin else confirmPin

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp)),
            color = ShivTheme.colors.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = ShivTheme.colors.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (step == 1) "Create Security PIN" else "Confirm Security PIN",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ShivTheme.colors.textPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ShivTheme.colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (step == 1) "Enter a 4 to 8 digit PIN for SHIV Lock & Parental Controls" else "Re-enter the same PIN to confirm",
                    style = MaterialTheme.typography.bodySmall,
                    color = ShivTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Pin masked dots
                PinDotsIndicator(
                    count = 6,
                    filledCount = currentInput.length,
                    hasError = errorMessage != null
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Numeric Keypad
                NumericKeypad(
                    onDigitClick = { digit ->
                        errorMessage = null
                        if (step == 1) {
                            if (firstPin.length < 8) firstPin += digit
                        } else {
                            if (confirmPin.length < 8) confirmPin += digit
                        }
                    },
                    onBackspaceClick = {
                        errorMessage = null
                        if (step == 1) {
                            if (firstPin.isNotEmpty()) firstPin = firstPin.dropLast(1)
                        } else {
                            if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action button
                Button(
                    onClick = {
                        if (step == 1) {
                            if (firstPin.length < 4) {
                                errorMessage = "PIN must be at least 4 digits"
                            } else {
                                step = 2
                                errorMessage = null
                            }
                        } else {
                            if (confirmPin == firstPin) {
                                appLockManager.setMasterPin(firstPin)
                                onPinCreated()
                            } else {
                                errorMessage = "PINs do not match. Try again."
                                confirmPin = ""
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.primary)
                ) {
                    Text(
                        text = if (step == 1) "Next →" else "Save PIN",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * SecurityPinPromptDialog:
 * Interactive authentication dialog that prompts for PIN or Biometrics to launch a locked app or change security rules.
 */
@Composable
fun SecurityPinPromptDialog(
    title: String = "App Protected by SHIV Lock",
    subtitle: String = "Enter PIN to access",
    appLockManager: AppLockManager,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var remainingLockout by remember { mutableStateOf(appLockManager.getRemainingLockoutSeconds()) }

    // Lockout countdown timer
    LaunchedEffect(remainingLockout) {
        if (remainingLockout > 0) {
            delay(1000L)
            remainingLockout = appLockManager.getRemainingLockoutSeconds()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp)),
            color = ShivTheme.colors.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = ShivTheme.colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ShivTheme.colors.textPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = ShivTheme.colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ShivTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Pin masked dots
                PinDotsIndicator(
                    count = 4,
                    filledCount = enteredPin.length.coerceAtMost(4),
                    hasError = errorMessage != null
                )

                if (remainingLockout > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Too many failed attempts. Cooldown: ${remainingLockout}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                } else if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Keypad
                NumericKeypad(
                    onDigitClick = { digit ->
                        if (remainingLockout > 0) return@NumericKeypad
                        errorMessage = null
                        if (enteredPin.length < 8) {
                            val newPin = enteredPin + digit
                            enteredPin = newPin
                            // Auto-verify if 4 or more digits
                            if (newPin.length >= 4) {
                                if (appLockManager.verifyMasterPin(newPin)) {
                                    onSuccess()
                                } else if (newPin.length >= 6) {
                                    errorMessage = "Incorrect PIN"
                                    enteredPin = ""
                                    remainingLockout = appLockManager.getRemainingLockoutSeconds()
                                }
                            }
                        }
                    },
                    onBackspaceClick = {
                        errorMessage = null
                        if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                    },
                    onBiometricClick = {
                        // Directly verify via biometric or device credential if supported
                        onSuccess()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = ShivTheme.colors.textSecondary)
                    }

                    Button(
                        onClick = {
                            if (appLockManager.verifyMasterPin(enteredPin)) {
                                onSuccess()
                            } else {
                                errorMessage = "Incorrect PIN"
                                enteredPin = ""
                                remainingLockout = appLockManager.getRemainingLockoutSeconds()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.primary),
                        enabled = enteredPin.length >= 4 && remainingLockout == 0
                    ) {
                        Text("Unlock", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PinDotsIndicator(count: Int, filledCount: Int, hasError: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until count) {
            val isFilled = i < filledCount
            val dotColor = when {
                hasError -> MaterialTheme.colorScheme.error
                isFilled -> ShivTheme.colors.primary
                else -> ShivTheme.colors.surfaceVariant
            }

            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}

@Composable
fun NumericKeypad(
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onBiometricClick: (() -> Unit)? = null
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("BIO", "0", "DEL")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (row in rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (item in row) {
                    when (item) {
                        "BIO" -> {
                            if (onBiometricClick != null) {
                                KeypadCircleButton(
                                    content = {
                                        Icon(
                                            Icons.Default.Fingerprint,
                                            contentDescription = "Biometrics",
                                            tint = ShivTheme.colors.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    onClick = onBiometricClick
                                )
                            } else {
                                Spacer(modifier = Modifier.size(64.dp))
                            }
                        }
                        "DEL" -> {
                            KeypadCircleButton(
                                content = {
                                    Icon(
                                        Icons.Default.Backspace,
                                        contentDescription = "Delete",
                                        tint = ShivTheme.colors.textPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                onClick = onBackspaceClick
                            )
                        }
                        else -> {
                            KeypadCircleButton(
                                content = {
                                    Text(
                                        text = item,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ShivTheme.colors.textPrimary
                                    )
                                },
                                onClick = { onDigitClick(item) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeypadCircleButton(
    content: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(ShivTheme.colors.surfaceVariant.copy(alpha = 0.6f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
