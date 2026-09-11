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

@Composable
fun SidebarDrawer(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onClose: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(310.dp),
        drawerContainerColor = CosmicSurface,
        drawerContentColor = TextPrimary,
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
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = ShivaIndigo.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "v1.0",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = ShivaCyan
                        )
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // User Profile Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CosmicSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(ShivaIndigo, ShivaPurple)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Shivam Gupta",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Creator • Builder • Dreamer",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
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
                icon = Icons.Default.Folder,
                label = "Files",
                isSelected = currentRoute == "files",
                onClick = { onNavigate("files"); onClose() }
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
                color = TextTertiary,
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
                color = TextTertiary,
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
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) ShivaIndigo.copy(alpha = 0.2f) else Color.Transparent,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) ShivaCyan else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = if (isSelected) TextPrimary else TextSecondary
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
                tint = TextTertiary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
