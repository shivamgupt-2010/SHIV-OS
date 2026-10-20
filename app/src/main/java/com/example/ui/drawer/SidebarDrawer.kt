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

            // User Profile Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(ShivTheme.cardRadius),
                color = ShivTheme.colors.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, ShivTheme.colors.cardBorder)
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
                                    listOf(ShivTheme.colors.primary, ShivTheme.colors.accent)
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
                            color = ShivTheme.colors.textPrimary
                        )
                        Text(
                            text = "Creator • Builder • Dreamer",
                            style = MaterialTheme.typography.bodySmall,
                            color = ShivTheme.colors.textSecondary
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
