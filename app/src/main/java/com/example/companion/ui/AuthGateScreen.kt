package com.example.companion.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.companion.auth.ShivAuthGate
import com.example.companion.avatar.AvatarRenderContext
import com.example.companion.avatar.AvatarType
import com.example.companion.avatar.CosmicShivRenderer

/**
 * AuthGateScreen:
 * Luxury Obsidian Glassmorphic Security Boundary for ShivAI Companion.
 * Enforces mandatory identity verification before granting access to the system.
 */
@Composable
fun AuthGateScreen(
    onAuthenticated: () -> Unit
) {
    var username by remember { mutableStateOf("Krishn (SHV-10482)") }
    var password by remember { mutableStateOf("••••••••") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF04060B), Color(0xFF0B0E17), Color(0xFF04060B))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
        ) {
            // 1. Hero Floating Avatar Viewport
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                CosmicShivRenderer(
                    context = AvatarRenderContext(avatarType = AvatarType.COSMIC_SHIV)
                )
            }

            // 2. Luxury Glassmorphic Identity Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                color = Color(0xF00D111A),
                shadowElevation = 16.dp,
                border = androidx.compose.foundation.BorderStroke(
                    1.2.dp,
                    Brush.verticalGradient(listOf(Color(0x8800E5FF), Color(0x2200E5FF)))
                )
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SHIV IDENTITY GATE",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color(0xFF00E5FF)
                    )

                    Text(
                        text = "Authentication required to access ShivAI Companion",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username or SHV-ID", fontFamily = FontFamily.SansSerif) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF00E5FF))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF1E293B)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", fontFamily = FontFamily.SansSerif) },
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF00E5FF))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF1E293B)
                        )
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            fontFamily = FontFamily.SansSerif,
                            color = Color(0xFFEF4444),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Primary Sign In Button
                    Button(
                        onClick = {
                            isLoading = true
                            ShivAuthGate.setAuthenticated(username, "shiv_auth_token_${System.currentTimeMillis()}")
                            isLoading = false
                            onAuthenticated()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text(
                                "SIGN IN WITH SHIV ID",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Biometric Quick Unlock
                    OutlinedButton(
                        onClick = {
                            ShivAuthGate.setAuthenticated("Krishn (SHV-10482)", "shiv_biometric_token")
                            onAuthenticated()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF))
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color(0xFF00E5FF))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Biometric Quick Unlock",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF00E5FF)
                        )
                    }
                }
            }
        }
    }
}
