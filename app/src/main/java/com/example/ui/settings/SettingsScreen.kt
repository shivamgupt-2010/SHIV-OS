package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.permissions.AIReadinessState
import com.example.core.permissions.PermissionHealth
import com.example.core.permissions.PermissionOrchestrator
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    permissionOrchestrator: PermissionOrchestrator,
    shivAIPreferences: com.example.ai.shivai.config.ShivAIPreferences? = null,
    shivAIClient: com.example.ai.shivai.client.ShivAIClient? = null,
    onNavigateBack: () -> Unit,
    onNavigateDiagnostics: () -> Unit,
    onRequestUsageStats: () -> Unit,
    onRequestOverlay: () -> Unit,
    onRequestAccessibility: () -> Unit,
    onRequestNotification: () -> Unit,
    onRequestBasicPermissions: () -> Unit
) {
    val readinessState by permissionOrchestrator.readinessState.collectAsState()
    val health by permissionOrchestrator.permissionHealth.collectAsState()

    LaunchedEffect(Unit) {
        permissionOrchestrator.refreshPermissions()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI & Cloud Control Center", fontWeight = FontWeight.Bold) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ReadinessCard(readinessState)

            if (shivAIPreferences != null && shivAIClient != null) {
                ShivAICloudConfigCard(preferences = shivAIPreferences, client = shivAIClient)
            }

            ShivIdentityOAuthCard()

            Text("Transparency & Control", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("These permissions power the AI engine. You have full control. Disabling permissions will fall back to Safe-Mode operation.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            PermissionItem(
                title = "Contextual Screen Intelligence",
                description = "Required to know which apps are on screen for contextual help. (Usage Access)",
                isGranted = health.hasUsageStats,
                onClick = onRequestUsageStats
            )

            PermissionItem(
                title = "Floating Assistant Interface",
                description = "Lets ShivAI draw the fluid contextual assistant overlay.",
                isGranted = health.hasOverlay,
                onClick = onRequestOverlay
            )

            PermissionItem(
                title = "Semantic Screen Reader",
                description = "Crucial for reading visible text using accessibility. Allows deep study material context.",
                isGranted = health.hasAccessibility,
                onClick = onRequestAccessibility
            )

            PermissionItem(
                title = "Smart Notification Suppressor",
                description = "Allows ShivAI to block spam and summarize important messages intelligently.",
                isGranted = health.hasNotificationAccess,
                onClick = onRequestNotification
            )

            PermissionItem(
                title = "Basic AI Sensors",
                description = "Microphone (voice), Contacts, Calendar, and Storage (encrypted memory vault).",
                isGranted = health.hasRecordAudio && health.hasContacts && health.hasCalendar && health.hasExternalStorage,
                onClick = onRequestBasicPermissions
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Testing & Trust Operations", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            
            Button(
                onClick = onNavigateDiagnostics,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Usability Testing Framework & Status")
            }
        }
    }
}

@Composable
fun ReadinessCard(readiness: AIReadinessState) {
    val backgroundColor = when (readiness) {
        AIReadinessState.FULL_AI_MODE -> MaterialTheme.colorScheme.primaryContainer
        AIReadinessState.LIMITED_AI_MODE -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.errorContainer
    }

    val contentColor = when (readiness) {
        AIReadinessState.FULL_AI_MODE -> MaterialTheme.colorScheme.onPrimaryContainer
        AIReadinessState.LIMITED_AI_MODE -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "System Readiness Phase",
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                readiness.name.replace("_", " "),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
fun PermissionItem(
    title: String,
    description: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = { if (!isGranted) onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!isGranted) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onClick, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                    Text("Grant")
                }
            }
        }
    }
}

@Composable
fun ShivAICloudConfigCard(
    preferences: com.example.ai.shivai.config.ShivAIPreferences,
    client: com.example.ai.shivai.client.ShivAIClient
) {
    val coroutineScope = rememberCoroutineScope()
    var baseUrl by remember { mutableStateOf(preferences.getBaseUrl()) }
    var userId by remember { mutableStateOf(preferences.getUserId()) }
    var apiKey by remember { mutableStateOf(preferences.getApiKey()) }

    var testStatus by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "ShivAI Cloud Engine",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Connect your mobile launcher directly to your 24/7 cloud AI backend instance on Render or custom server.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it; isSaved = false },
                label = { Text("Cloud Base URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it; isSaved = false },
                label = { Text("User ID (e.g. shivam)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it; isSaved = false },
                label = { Text("Render API Key (Optional / Header Token)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        preferences.setBaseUrl(baseUrl)
                        preferences.setUserId(userId)
                        preferences.setApiKey(apiKey)
                        isSaved = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isSaved) "Saved!" else "Save Config")
                }

                FilledTonalButton(
                    onClick = {
                        isTesting = true
                        testStatus = null
                        coroutineScope.launch {
                            preferences.setBaseUrl(baseUrl)
                            preferences.setUserId(userId)
                            preferences.setApiKey(apiKey)
                            when (val result = client.checkHealth()) {
                                is com.example.core.utils.Result.Success -> {
                                    val (health, latency) = result.data
                                    testStatus = "ONLINE: ${health.service ?: "ShivAI"} ${health.version ?: ""} (${latency}ms)"
                                }
                                is com.example.core.utils.Result.Error -> {
                                    testStatus = "ERROR: ${result.message}"
                                }
                                else -> {}
                            }
                            isTesting = false
                        }
                    },
                    enabled = !isTesting,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isTesting) "Pinging..." else "Test Cloud")
                }
            }

            testStatus?.let { status ->
                Surface(
                    color = if (status.startsWith("ONLINE")) Color(0xFF2E7D32).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (status.startsWith("ONLINE")) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
fun ShivIdentityOAuthCard() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isLoggedIn by remember { mutableStateOf(com.example.auth.ShivIdentityAuth.isLoggedIn(context)) }
    var userProfileText by remember { mutableStateOf<String?>(null) }
    var isTestingService by remember { mutableStateOf(false) }
    var serviceTestStatus by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (isLoggedIn) {
            val token = com.example.auth.ShivIdentityAuth.getAccessToken(context)
            if (token != null) {
                com.example.auth.ShivIdentityAuth.fetchUserInfo(token) { profile ->
                    if (profile != null) {
                        userProfileText = profile.optString("name", profile.optString("preferred_username", "Logged In"))
                    }
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "SHIV Identity (OAuth PKCE)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    color = if (isLoggedIn) Color(0xFF2E7D32).copy(alpha = 0.2f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isLoggedIn) "ACTIVE" else "PUBLIC CLIENT",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isLoggedIn) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                    )
                }
            }

            Text(
                "Authorization Code Flow with PKCE (RFC 7636). Redirects securely via custom Android deep link shivlauncher://oauth/callback.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Client ID: shiv_client_1d07df81d86aa737330cd3e9", style = MaterialTheme.typography.bodySmall, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    Text("Redirect URI: shivlauncher://oauth/callback", style = MaterialTheme.typography.bodySmall, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    Text("Issuer: https://shiv-database.onrender.com", style = MaterialTheme.typography.bodySmall, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    if (isLoggedIn && userProfileText != null) {
                        Text("Active User: $userProfileText", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isLoggedIn) {
                    Button(
                        onClick = {
                            com.example.auth.ShivIdentityAuth.startSignIn(context)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Sign In with SHIV")
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            com.example.auth.ShivIdentityAuth.signOut(context)
                            isLoggedIn = false
                            userProfileText = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Sign Out")
                    }
                }

                FilledTonalButton(
                    onClick = {
                        isTestingService = true
                        serviceTestStatus = null
                        val okhttp = okhttp3.OkHttpClient()
                        val req = okhttp3.Request.Builder()
                            .url("https://shiv-database.onrender.com/health")
                            .get()
                            .build()
                        okhttp.newCall(req).enqueue(object : okhttp3.Callback {
                            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                                isTestingService = false
                                serviceTestStatus = "Service unreachable: ${e.message}"
                            }
                            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                                isTestingService = false
                                serviceTestStatus = if (response.isSuccessful) "Service Online: 200 OK (PKCE Ready)" else "Status: ${response.code}"
                            }
                        })
                    },
                    enabled = !isTestingService,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isTestingService) "Testing..." else "Test Service")
                }
            }

            serviceTestStatus?.let { status ->
                Surface(
                    color = if (status.contains("Online") || status.contains("200")) Color(0xFF2E7D32).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (status.contains("Online") || status.contains("200")) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}


