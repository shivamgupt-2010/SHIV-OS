package com.example.ui.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TrishulIcon
import com.example.ui.theme.*

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import java.io.File
import java.io.FileOutputStream

@Composable
fun SidebarDrawer(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onClose: () -> Unit,
    userProfileManager: com.example.core.identity.UserProfileManager? = null
) {
    val profile = userProfileManager?.profile?.collectAsState()?.value 
        ?: com.example.core.identity.UserProfile()
    var showEditProfileDialog by remember { mutableStateOf(false) }
    ModalDrawerSheet(
        modifier = Modifier.width(310.dp),
        drawerContainerColor = ShivTheme.colors.surface,
        drawerContentColor = ShivTheme.colors.textPrimary,
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TrishulIcon(size = 28.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ShivAI",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = ShivTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = ShivTheme.colors.primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "v2.0",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = ShivTheme.colors.primary
                        )
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = ShivTheme.colors.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dynamic User Profile Card with Photo Setup
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(ShivTheme.cardRadius))
                    .clickable { showEditProfileDialog = true },
                shape = RoundedCornerShape(ShivTheme.cardRadius),
                color = ShivTheme.colors.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, ShivTheme.colors.cardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val photoBitmap = remember(profile.photoUri) {
                        if (!profile.photoUri.isNullOrBlank()) {
                            try {
                                BitmapFactory.decodeFile(profile.photoUri)
                            } catch (e: Exception) {
                                null
                            }
                        } else null
                    }

                    if (photoBitmap != null) {
                        Image(
                            bitmap = photoBitmap.asImageBitmap(),
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(ShivTheme.colors.primary, ShivTheme.colors.accent)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.displayName.firstOrNull()?.uppercase() ?: "S",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = ShivTheme.colors.textPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = profile.bio,
                            style = MaterialTheme.typography.bodySmall,
                            color = ShivTheme.colors.textSecondary,
                            maxLines = 1
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = ShivTheme.colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (showEditProfileDialog) {
                EditProfileDialog(
                    initialName = profile.displayName,
                    initialBio = profile.bio,
                    initialPhotoUri = profile.photoUri,
                    onSave = { name, bio, photoUri ->
                        userProfileManager?.updateProfile(name, bio, photoUri)
                        showEditProfileDialog = false
                    },
                    onDismiss = { showEditProfileDialog = false }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Menu Navigation
            DrawerNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = currentRoute == "launcher",
                onClick = { onNavigate("launcher"); onClose() }
            )
            DrawerNavItem(
                icon = Icons.Default.Apps,
                label = "All Apps",
                isSelected = currentRoute == "all_apps",
                onClick = { onNavigate("all_apps"); onClose() }
            )
            DrawerNavItem(
                icon = Icons.Default.Palette,
                label = "SHIV Studio",
                isSelected = currentRoute == "studio",
                onClick = { onNavigate("studio"); onClose() }
            )
            DrawerNavItem(
                icon = Icons.Default.Shield,
                label = "Safety & Security",
                isSelected = currentRoute == "safety_security",
                onClick = { onNavigate("safety_security"); onClose() }
            )
            DrawerNavItem(
                icon = Icons.Default.Lock,
                label = "SHIV Lock",
                isSelected = currentRoute == "lock",
                onClick = { onNavigate("lock"); onClose() }
            )
            DrawerNavItem(
                icon = Icons.Default.Equalizer,
                label = "Wellbeing",
                isSelected = currentRoute == "wellbeing",
                onClick = { onNavigate("wellbeing"); onClose() }
            )
            DrawerNavItem(
                icon = Icons.Default.FamilyRestroom,
                label = "Family",
                isSelected = currentRoute == "family",
                onClick = { onNavigate("family"); onClose() }
            )
            DrawerNavItem(
                icon = Icons.Default.ChatBubble,
                label = "Chat",
                isSelected = currentRoute == "chat",
                onClick = { onNavigate("chat"); onClose() }
            )
            DrawerNavItem(
                icon = Icons.Default.SmartToy,
                label = "Agents",
                isSelected = currentRoute == "agents",
                onClick = { onNavigate("agents"); onClose() }
            )
            DrawerNavItem(
                icon = Icons.Default.AccountTree,
                label = "Automations & Tasks",
                isSelected = currentRoute == "automations",
                onClick = { onNavigate("automations"); onClose() }
            )
            DrawerNavItem(
                icon = Icons.Default.Build,
                label = "Tools",
                isSelected = currentRoute == "tools",
                onClick = { onNavigate("tools"); onClose() }
            )
            DrawerNavItem(
                icon = Icons.Default.Description,
                label = "Notes",
                isSelected = currentRoute == "notes",
                onClick = { onNavigate("notes"); onClose() }
            )
            DrawerNavItem(
                icon = Icons.Default.Save,
                label = "Memory",
                isSelected = currentRoute == "memory",
                onClick = { onNavigate("memory"); onClose() }
            )
            DrawerNavItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = currentRoute == "settings",
                onClick = { onNavigate("settings"); onClose() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Ecosystem Section
            Text(
                text = "Ecosystem",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = ShivTheme.colors.textSecondary,
                modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
            )

            EcosystemItem(icon = Icons.Default.Code, label = "ShivAI SDK", onClick = { onNavigate("chat"); onClose() })
            EcosystemItem(icon = Icons.Default.Badge, label = "Identity", onClick = { onNavigate("settings"); onClose() })
            EcosystemItem(icon = Icons.Default.BarChart, label = "Analytics", onClick = { onNavigate("settings"); onClose() })

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(20.dp))

            // Footer
            Text(
                text = "Built with passion in India 🇮🇳",
                style = MaterialTheme.typography.bodySmall,
                color = ShivTheme.colors.textSecondary,
                modifier = Modifier.padding(start = 12.dp, bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun DrawerNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(ShivTheme.cardRadius))
            .clickable(onClick = onClick),
        color = if (isSelected) ShivTheme.colors.primary.copy(alpha = 0.2f) else Color.Transparent,
        shape = RoundedCornerShape(ShivTheme.cardRadius)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = if (isSelected) ShivTheme.colors.textPrimary else ShivTheme.colors.textSecondary
            )
        }
    }
}

@Composable
private fun EcosystemItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = ShivTheme.colors.textSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = ShivTheme.colors.textSecondary
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    initialName: String,
    initialBio: String,
    initialPhotoUri: String?,
    onSave: (String, String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var bio by remember { mutableStateOf(initialBio) }
    var photoUri by remember { mutableStateOf(initialPhotoUri) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val profilesDir = File(context.filesDir, "profiles").apply { mkdirs() }
                val photoFile = File(profilesDir, "avatar_${System.currentTimeMillis()}.jpg")
                FileOutputStream(photoFile).use { out ->
                    inputStream?.copyTo(out)
                }
                inputStream?.close()
                photoUri = photoFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ShivTheme.colors.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = ShivTheme.colors.textPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Photo Avatar Picker
                val photoBitmap = remember(photoUri) {
                    if (!photoUri.isNullOrBlank()) {
                        try {
                            BitmapFactory.decodeFile(photoUri)
                        } catch (e: Exception) {
                            null
                        }
                    } else null
                }

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(ShivTheme.colors.surfaceVariant)
                        .clickable { galleryLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (photoBitmap != null) {
                        Image(
                            bitmap = photoBitmap.asImageBitmap(),
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.AddAPhoto,
                            contentDescription = "Add Photo",
                            tint = ShivTheme.colors.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    TextButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Text("Choose Photo", color = ShivTheme.colors.primary)
                    }
                    if (photoUri != null) {
                        TextButton(onClick = { photoUri = null }) {
                            Text("Remove", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio / Status") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = ShivTheme.colors.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(name, bio, photoUri) },
                        colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.primary)
                    ) {
                        Text("Save Profile", color = Color.White)
                    }
                }
            }
        }
    }
}
