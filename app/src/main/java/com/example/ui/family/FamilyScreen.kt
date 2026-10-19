package com.example.ui.family

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.family.FamilyManager
import com.example.core.launcher.AppInfo
import com.example.ui.components.CosmicBackground
import com.example.ui.components.TrishulIcon
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen(
    familyManager: FamilyManager,
    installedApps: List<AppInfo>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val policy by familyManager.policy.collectAsState()
    val isCurfewActive = remember(policy) { familyManager.isCurfewActive() }

    var isParentAuthenticated by remember { mutableStateOf(!policy.isSupervised) }
    var enteredPin by remember { mutableStateOf("") }
    var showPinDialog by remember { mutableStateOf(!isParentAuthenticated) }
    var newPinText by remember { mutableStateOf("") }
    var showSetPinDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        CosmicBackground(showSilhouette = false)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TrishulIcon(size = 22.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("SHIV Family", fontWeight = FontWeight.Bold)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Status Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(alpha = 0.8f)),
                    border = BorderStroke(1.dp, if (policy.isSupervised) ShivaCyan else CosmicCardBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Device Supervision Status", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Surface(
                                color = if (policy.isSupervised) ShivaCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (policy.isSupervised) "SUPERVISED (CHILD MODE)" else "STANDARD (PARENT)",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (policy.isSupervised) ShivaCyan else TextSecondary
                                )
                            }
                        }
                        Text(
                            "When supervised mode is enabled, only approved applications and study tools are accessible. Entertainment apps are blocked during scheduled bedtime curfews.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // Curfew Active Alert Banner
                if (isCurfewActive) {
                    Surface(
                        color = ShivaOrange.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, ShivaOrange.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bedtime, contentDescription = null, tint = ShivaOrange)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Bedtime Curfew Active", fontWeight = FontWeight.Bold, color = ShivaOrange)
                                Text("Non-essential apps are currently locked for rest time.", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            }
                        }
                    }
                }

                // Controls: Supervision Switch
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CosmicSurface.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, CosmicCardBorder.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enable Child Supervision Mode", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Enforces allowlist and curfew restrictions", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Switch(
                            checked = policy.isSupervised,
                            onCheckedChange = { enable ->
                                if (enable && policy.parentPinHash.isEmpty()) {
                                    showSetPinDialog = true
                                } else {
                                    showPinDialog = true
                                }
                            }
                        )
                    }
                }

                // Curfew Schedule Editor
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = ShivaCyan)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Bedtime Curfew", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            }
                            Switch(
                                checked = policy.curfewEnabled,
                                onCheckedChange = { familyManager.updateCurfew(it, 21, 0, 7, 0) }
                            )
                        }
                        Text("Active daily: 9:00 PM – 7:00 AM (Emergency phone calls always permitted)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }

                // Temporary Extension Actions
                if (policy.isSupervised) {
                    Text("Temporary Parental Extensions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                if (familyManager.grantTemporaryExtension(30, enteredPin)) {
                                    Toast.makeText(context, "+30 Minutes granted!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Parent PIN required", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+30 Mins")
                        }
                        FilledTonalButton(
                            onClick = {
                                if (familyManager.grantTemporaryExtension(60, enteredPin)) {
                                    Toast.makeText(context, "+1 Hour granted!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Parent PIN required", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+1 Hour")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Parent PIN Dialog
        if (showPinDialog && policy.parentPinHash.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { showPinDialog = false },
                title = { Text("Parent PIN Verification") },
                text = {
                    Column {
                        Text("Enter your 4-digit Parent PIN to configure family policies:", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = enteredPin,
                            onValueChange = { if (it.length <= 6) enteredPin = it },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (familyManager.verifyParentPin(enteredPin)) {
                            isParentAuthenticated = true
                            showPinDialog = false
                            familyManager.toggleSupervision(!policy.isSupervised, enteredPin)
                        } else {
                            Toast.makeText(context, "Incorrect Parent PIN", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Verify PIN")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPinDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Set New PIN Dialog
        if (showSetPinDialog) {
            AlertDialog(
                onDismissRequest = { showSetPinDialog = false },
                title = { Text("Set Parent Security PIN") },
                text = {
                    Column {
                        Text("Create a 4-digit Parent PIN to protect family settings:", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = newPinText,
                            onValueChange = { if (it.length <= 6) newPinText = it },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (newPinText.length >= 4) {
                            familyManager.setParentPin(newPinText)
                            enteredPin = newPinText
                            familyManager.toggleSupervision(true, newPinText)
                            showSetPinDialog = false
                            Toast.makeText(context, "Parent PIN set successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Set PIN & Enable")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSetPinDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
