package com.example.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var currentStep by remember { mutableStateOf(0) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (currentStep) {
                0 -> {
                    Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Welcome to ShivAI", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Your intelligent, context-aware operating environment designed for real-world usefulness and privacy.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = { currentStep++ }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                        Text("Next")
                    }
                }
                1 -> {
                    Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Built on Trust", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("All your data is encrypted locally. The 'Memory Inspection' screen shows you exactly what ShivAI remembers. You have full control over what it sees and learns.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = { currentStep++ }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                        Text("Next")
                    }
                }
                2 -> {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Permissions Matter", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("ShivAI needs advanced permissions like Accessibility and Screen Overlays to assist you contextually. We will walk you through setting them up gently. You can revoke them at any time in the 'Diagnostic & Settings' dashboard.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = onComplete, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                        Text("Get Started")
                    }
                }
            }
        }
    }
}
