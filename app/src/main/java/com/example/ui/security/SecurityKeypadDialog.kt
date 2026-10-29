package com.example.ui.security

import android.app.Activity
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.lock.AppLockManager
import com.example.core.lock.LockMethod
import com.example.ui.theme.ShivTheme
import kotlinx.coroutines.delay

/**
 * Universal Security Credential Creation Dialog:
 * Allows configuring PIN, Pattern, or Password, plus a mandatory Security Recovery Question.
 */
@Composable
fun SecurityPinCreationDialog(
    appLockManager: AppLockManager,
    onPinCreated: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf(appLockManager.lockMethod.value) }
    var step by remember { mutableStateOf(1) } // 1 = Enter Credential, 2 = Confirm Credential, 3 = Recovery Setup
    var firstCredential by remember { mutableStateOf("") }
    var confirmCredential by remember { mutableStateOf("") }
    var recoveryQuestion by remember { mutableStateOf("What was your first childhood pet's name?") }
    var recoveryAnswer by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    val currentInput = if (step == 1) firstCredential else confirmCredential

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
                // Header
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
                            text = when (step) {
                                1 -> "Setup ${selectedMethod.name}"
                                2 -> "Confirm ${selectedMethod.name}"
                                else -> "Recovery Setup"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ShivTheme.colors.textPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ShivTheme.colors.textSecondary)
                    }
                }

                // Method Selector Tabs (Step 1 only)
                if (step == 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(LockMethod.PIN, LockMethod.PATTERN, LockMethod.PASSWORD).forEach { method ->
                            val isSelected = selectedMethod == method
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedMethod = method
                                        firstCredential = ""
                                        confirmCredential = ""
                                        errorMessage = null
                                    },
                                color = if (isSelected) ShivTheme.colors.primary.copy(alpha = 0.2f) else ShivTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = method.name,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.textSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Content based on step and method
                when (step) {
                    1, 2 -> {
                        when (selectedMethod) {
                            LockMethod.PIN -> {
                                PinDotsIndicator(
                                    count = 4,
                                    filledCount = currentInput.length.coerceAtMost(4),
                                    hasError = errorMessage != null
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                NumericKeypad(
                                    onDigitClick = { digit ->
                                        errorMessage = null
                                        if (currentInput.length < 8) {
                                            if (step == 1) firstCredential += digit
                                            else confirmCredential += digit
                                        }
                                    },
                                    onBackspaceClick = {
                                        errorMessage = null
                                        if (step == 1 && firstCredential.isNotEmpty()) firstCredential = firstCredential.dropLast(1)
                                        if (step == 2 && confirmCredential.isNotEmpty()) confirmCredential = confirmCredential.dropLast(1)
                                    }
                                )
                            }

                            LockMethod.PATTERN -> {
                                Text(
                                    text = if (step == 1) "Connect at least 4 dots" else "Draw the same pattern to confirm",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ShivTheme.colors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                PatternLockView(
                                    hasError = errorMessage != null,
                                    onPatternComplete = { pattern ->
                                        if (step == 1) {
                                            firstCredential = pattern
                                            step = 2
                                        } else {
                                            confirmCredential = pattern
                                            if (confirmCredential == firstCredential) {
                                                step = 3 // Move to recovery
                                            } else {
                                                errorMessage = "Pattern does not match. Try again."
                                                confirmCredential = ""
                                            }
                                        }
                                    }
                                )
                            }

                            LockMethod.PASSWORD -> {
                                OutlinedTextField(
                                    value = currentInput,
                                    onValueChange = {
                                        errorMessage = null
                                        if (step == 1) firstCredential = it else confirmCredential = it
                                    },
                                    placeholder = { Text(if (step == 1) "Enter master password" else "Confirm master password") },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Toggle password visibility"
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            LockMethod.BIOMETRICS -> {
                                Text("Biometric uses device credentials. Please set a backup PIN.", color = ShivTheme.colors.textSecondary)
                            }
                        }

                        // Next / Confirm Button for PIN and Password
                        if (selectedMethod != LockMethod.PATTERN) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    if (step == 1) {
                                        if (firstCredential.length < 4) {
                                            errorMessage = "Must be at least 4 characters"
                                        } else {
                                            step = 2
                                        }
                                    } else {
                                        if (firstCredential == confirmCredential) {
                                            step = 3 // Move to recovery question
                                        } else {
                                            errorMessage = "Credentials do not match. Restarting."
                                            firstCredential = ""
                                            confirmCredential = ""
                                            step = 1
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.primary),
                                enabled = currentInput.length >= 4
                            ) {
                                Text(if (step == 1) "Continue" else "Confirm & Next")
                            }
                        }
                    }

                    3 -> {
                        // Recovery Setup Step
                        Text(
                            text = "Set a recovery question to regain access if you forget your ${selectedMethod.name}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ShivTheme.colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = recoveryQuestion,
                            onValueChange = { recoveryQuestion = it },
                            label = { Text("Security Question") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = recoveryAnswer,
                            onValueChange = { recoveryAnswer = it },
                            label = { Text("Your Secret Answer") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (recoveryAnswer.trim().length < 2) {
                                    errorMessage = "Please enter a valid recovery answer."
                                } else {
                                    // Save credentials
                                    when (selectedMethod) {
                                        LockMethod.PIN -> appLockManager.setMasterPin(firstCredential)
                                        LockMethod.PASSWORD -> appLockManager.setMasterPassword(firstCredential)
                                        LockMethod.PATTERN -> appLockManager.setMasterPattern(firstCredential)
                                        LockMethod.BIOMETRICS -> appLockManager.setMasterPin(firstCredential)
                                    }
                                    appLockManager.setLockMethod(selectedMethod)
                                    appLockManager.setSecurityRecovery(recoveryQuestion, recoveryAnswer)
                                    appLockManager.setShivLockEnabled(true)
                                    onPinCreated()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.primary),
                            enabled = recoveryAnswer.isNotBlank()
                        ) {
                            Text("Save & Activate SHIV Lock")
                        }
                    }
                }
            }
        }
    }
}

/**
 * SecurityPinPromptDialog:
 * Adaptive authentication prompt. Automatically adapts to the user's active LockMethod (PIN, Pattern, Password, Biometrics).
 * Uses real Android BiometricPrompt and supports security recovery reset.
 */
@Composable
fun SecurityPinPromptDialog(
    title: String = "App Protected by SHIV Lock",
    subtitle: String = "Enter credentials to access",
    appLockManager: AppLockManager,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lockMethod by appLockManager.lockMethod.collectAsState()

    var enteredPin by remember { mutableStateOf("") }
    var enteredPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var remainingLockout by remember { mutableStateOf(appLockManager.getRemainingLockoutSeconds()) }

    // Lockout countdown timer
    LaunchedEffect(remainingLockout) {
        if (remainingLockout > 0) {
            delay(1000L)
            remainingLockout = appLockManager.getRemainingLockoutSeconds()
        }
    }

    // Auto-trigger biometrics if selected method is BIOMETRICS
    LaunchedEffect(Unit) {
        if (lockMethod == LockMethod.BIOMETRICS && activity != null && remainingLockout == 0) {
            appLockManager.authenticateWithBiometrics(activity, title = title) { success ->
                if (success) onSuccess()
            }
        }
    }

    if (showRecoveryDialog) {
        SecurityRecoveryResetDialog(
            appLockManager = appLockManager,
            onSuccess = {
                showRecoveryDialog = false
                onSuccess()
            },
            onDismiss = { showRecoveryDialog = false }
        )
        return
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
                // Header
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

                Spacer(modifier = Modifier.height(18.dp))

                if (remainingLockout > 0) {
                    Text(
                        text = "Too many failed attempts. Cooldown: ${remainingLockout}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                } else if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Authentication Input based on active lock method
                when (lockMethod) {
                    LockMethod.PIN -> {
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
                                    val newPin = enteredPin + digit
                                    enteredPin = newPin
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
                                if (activity != null && appLockManager.canAuthenticate()) {
                                    appLockManager.authenticateWithBiometrics(activity, title = title) { success ->
                                        if (success) onSuccess()
                                    }
                                }
                            }
                        )
                    }

                    LockMethod.PATTERN -> {
                        PatternLockView(
                            hasError = errorMessage != null,
                            onPatternComplete = { pattern ->
                                if (remainingLockout > 0) return@PatternLockView
                                errorMessage = null
                                if (appLockManager.verifyMasterPattern(pattern)) {
                                    onSuccess()
                                } else {
                                    errorMessage = "Incorrect pattern"
                                    remainingLockout = appLockManager.getRemainingLockoutSeconds()
                                }
                            }
                        )
                    }

                    LockMethod.PASSWORD -> {
                        OutlinedTextField(
                            value = enteredPassword,
                            onValueChange = {
                                errorMessage = null
                                enteredPassword = it
                            },
                            label = { Text("Master Password") },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (appLockManager.verifyMasterPassword(enteredPassword)) {
                                    onSuccess()
                                } else {
                                    errorMessage = "Incorrect password"
                                    enteredPassword = ""
                                    remainingLockout = appLockManager.getRemainingLockoutSeconds()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.primary),
                            enabled = enteredPassword.isNotBlank() && remainingLockout == 0
                        ) {
                            Text("Unlock")
                        }
                    }

                    LockMethod.BIOMETRICS -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.Fingerprint,
                                contentDescription = "Biometrics",
                                tint = ShivTheme.colors.primary,
                                modifier = Modifier.size(64.dp)
                            )
                            Button(
                                onClick = {
                                    if (activity != null) {
                                        appLockManager.authenticateWithBiometrics(activity, title = title) { success ->
                                            if (success) onSuccess()
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.primary)
                            ) {
                                Text("Scan Fingerprint / Face")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer: Recovery Link & Dismiss
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (appLockManager.isRecoveryConfigured()) {
                        TextButton(onClick = { showRecoveryDialog = true }) {
                            Text("Forgot Credential?", color = ShivTheme.colors.accent, fontSize = 12.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = ShivTheme.colors.textSecondary)
                    }
                }
            }
        }
    }
}

/**
 * SecurityRecoveryResetDialog:
 * Verifies security answer to allow resetting forgotten PIN, Pattern, or Password.
 */
@Composable
fun SecurityRecoveryResetDialog(
    appLockManager: AppLockManager,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    val question = remember { appLockManager.getSecurityQuestion() }
    var answer by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isVerified by remember { mutableStateOf(false) }

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
                Text(
                    text = "Credential Recovery",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ShivTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = question,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ShivTheme.colors.primary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (!isVerified) {
                    OutlinedTextField(
                        value = answer,
                        onValueChange = {
                            errorMessage = null
                            answer = it
                        },
                        label = { Text("Your Answer") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
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
                                if (appLockManager.verifySecurityRecovery(answer)) {
                                    isVerified = true
                                    errorMessage = null
                                } else {
                                    errorMessage = "Incorrect recovery answer."
                                }
                            },
                            enabled = answer.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.primary)
                        ) {
                            Text("Verify")
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = {
                            errorMessage = null
                            newPin = it
                        },
                        label = { Text("New 4-8 Digit PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (newPin.length >= 4) {
                                appLockManager.resetCredentialsWithRecovery(answer, newPin, LockMethod.PIN)
                                onSuccess()
                            } else {
                                errorMessage = "PIN must be at least 4 digits"
                            }
                        },
                        enabled = newPin.length >= 4,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.primary)
                    ) {
                        Text("Reset & Unlock")
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
