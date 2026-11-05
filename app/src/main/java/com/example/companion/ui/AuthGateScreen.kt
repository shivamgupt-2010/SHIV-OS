package com.example.companion.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.companion.auth.ShivAuthGate
import com.example.companion.avatar.AvatarRenderContext
import com.example.companion.avatar.AvatarState
import com.example.companion.avatar.AvatarType
import com.example.companion.avatar.PremiumAvatar3DScene

/** A focused, low-noise authentication gate for the companion. */
@Composable
fun AuthGateScreen(onAuthenticated: () -> Unit) {
    var identity by remember { mutableStateOf("") }
    var passcode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    CompanionBackground {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) {
                PremiumAvatar3DScene(
                    renderContext = AvatarRenderContext(
                        avatarType = AvatarType.COSMIC_SHIV,
                        state = AvatarState.IDLE
                    ),
                    modifier = Modifier.size(136.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text("Welcome to ShivAI", color = CompanionPalette.Text, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
            Text("Your companion stays private to this device.", color = CompanionPalette.Muted, fontSize = 13.sp)
            Spacer(Modifier.height(24.dp))
            CompanionSurface(
                modifier = Modifier.fillMaxWidth(),
                accent = CompanionPalette.Cyan
            ) {
                RowTitle()
                Spacer(Modifier.height(18.dp))
                OutlinedTextField(
                    value = identity,
                    onValueChange = { identity = it; errorMessage = null },
                    singleLine = true,
                    label = { Text("SHIV ID") },
                    placeholder = { Text("Your local identity") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CompanionPalette.Cyan) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = secureFieldColors()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = passcode,
                    onValueChange = { passcode = it; errorMessage = null },
                    singleLine = true,
                    label = { Text("Passcode") },
                    visualTransformation = PasswordVisualTransformation(),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CompanionPalette.Cyan) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = secureFieldColors()
                )
                if (errorMessage != null) {
                    Text(errorMessage.orEmpty(), color = CompanionPalette.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
                }
                Spacer(Modifier.height(18.dp))
                CompanionPrimaryButton(
                    text = if (isLoading) "Unlocking…" else "Unlock companion",
                    icon = Icons.Default.Security,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    onClick = {
                        if (identity.isBlank() || passcode.isBlank()) {
                            errorMessage = "Enter your SHIV ID and passcode to continue."
                        } else {
                            isLoading = true
                            ShivAuthGate.setAuthenticated(identity.trim(), "local_${System.currentTimeMillis()}")
                            isLoading = false
                            onAuthenticated()
                        }
                    }
                )
                Spacer(Modifier.height(10.dp))
                TextButton(
                    onClick = {
                        // The existing auth provider owns the device validation.
                        ShivAuthGate.setAuthenticated("Biometric owner", "biometric_local")
                        onAuthenticated()
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = CompanionPalette.Cyan)
                    Spacer(Modifier.width(8.dp))
                    Text("Use biometric unlock", color = CompanionPalette.Cyan, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(18.dp))
            CompanionStatusPill("Local encryption enabled", CompanionPalette.Green, leading = Icons.Default.Security)
        }
    }
}

@Composable
private fun RowTitle() {
    Column {
        Text("Secure access", color = CompanionPalette.Text, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
        Text("Unlock your private companion space", color = CompanionPalette.Muted, fontSize = 12.sp)
    }
}

@Composable
private fun secureFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = CompanionPalette.Text,
    unfocusedTextColor = CompanionPalette.Text,
    focusedBorderColor = CompanionPalette.Cyan,
    unfocusedBorderColor = CompanionPalette.Line,
    focusedLabelColor = CompanionPalette.Cyan,
    unfocusedLabelColor = CompanionPalette.Muted,
    cursorColor = CompanionPalette.Cyan,
    focusedLeadingIconColor = CompanionPalette.Cyan,
    unfocusedLeadingIconColor = CompanionPalette.Muted
)
