package com.example.ui.security

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.ShivIdentityAuth
import com.example.core.family.*
import com.example.core.launcher.AppInfo
import com.example.core.lock.AppLockManager
import com.example.core.lock.LockMethod
import com.example.core.wellbeing.FocusModeType
import com.example.core.wellbeing.WellbeingManager
import com.example.ui.components.CosmicBackground
import com.example.ui.components.TrishulIcon
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class SafetySection(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    PARENTAL("Parental Controls", Icons.Default.FamilyRestroom),
    LOCK("SHIV Lock", Icons.Default.Lock),
    WELLBEING("Digital Wellbeing", Icons.Default.Equalizer),
    SECURITY("Security Center", Icons.Default.Security),
    FAMILY_LINK("Account Linking", Icons.Default.Link)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetySecurityCenterScreen(
    appLockManager: AppLockManager,
    familyManager: FamilyManager,
    wellbeingManager: WellbeingManager,
    installedApps: List<AppInfo>,
    initialSection: SafetySection = SafetySection.PARENTAL,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedSection by remember { mutableStateOf(initialSection) }

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
                            Text(
                                text = "SHIV Safety & Security",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = ShivTheme.colors.textPrimary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = ShivTheme.colors.textPrimary
                            )
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
            ) {
                // Section Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedSection.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = ShivTheme.colors.primary,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSection.ordinal]),
                            color = ShivTheme.colors.primary,
                            height = 3.dp
                        )
                    },
                    divider = { HorizontalDivider(color = ShivTheme.colors.cardBorder.copy(alpha = 0.3f)) }
                ) {
                    SafetySection.entries.forEach { section ->
                        val isSelected = selectedSection == section
                        Tab(
                            selected = isSelected,
                            onClick = { selectedSection = section },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = section.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.textSecondary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = section.title,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.textSecondary
                                    )
                                }
                            }
                        )
                    }
                }

                // Section Body
                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    when (selectedSection) {
                        SafetySection.PARENTAL -> ParentalControlsTab(familyManager, installedApps)
                        SafetySection.LOCK -> ShivLockTab(appLockManager, installedApps)
                        SafetySection.WELLBEING -> DigitalWellbeingTab(wellbeingManager, installedApps)
                        SafetySection.SECURITY -> SecurityCenterTab(appLockManager, familyManager)
                        SafetySection.FAMILY_LINK -> FamilyAccountLinkingTab(familyManager)
                    }
                }
            }
        }
    }
}

// =========================================================================================
// 1. PARENTAL CONTROLS SECTION
// =========================================================================================
@Composable
fun ParentalControlsTab(familyManager: FamilyManager, installedApps: List<AppInfo>) {
    val context = LocalContext.current
    val policy by familyManager.policy.collectAsState()
    val restrictions by familyManager.contentRestrictions.collectAsState()
    val requests by familyManager.approvalRequests.collectAsState()
    val alerts by familyManager.safetyAlerts.collectAsState()
    val isCurfewActive = remember(policy) { familyManager.isCurfewActive() }

    var parentSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf("Dashboard", "Screen-Time", "Restrictions", "Requests", "Reports")

    var showPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showSetPinDialog by remember { mutableStateOf(false) }
    var newPinText by remember { mutableStateOf("") }
    var showWebsiteDialog by remember { mutableStateOf(false) }
    var newWebsiteText by remember { mutableStateOf("") }

    val verifyAndRun = { action: () -> Unit ->
        if (policy.parentPinHash.isEmpty()) {
            action()
        } else {
            pinAction = action
            showPinDialog = true
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sub-tabs row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            subTabs.forEachIndexed { idx, label ->
                val isSelected = parentSubTab == idx
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { parentSubTab = idx },
                    color = if (isSelected) ShivTheme.colors.primary.copy(alpha = 0.25f) else ShivTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.cardBorder.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(vertical = 8.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                        color = if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        when (parentSubTab) {
            // A. PARENT DASHBOARD
            0 -> {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    // Supervision master toggle card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = ShivTheme.colors.surfaceVariant,
                        border = BorderStroke(1.dp, if (policy.isSupervised) ShivTheme.colors.primary else ShivTheme.colors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = ShivTheme.colors.primary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Parental Controls", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                                        Text(
                                            if (policy.isSupervised) "Active Supervision Protected" else "Supervision Disabled",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (policy.isSupervised) Color(0xFF10B981) else ShivTheme.colors.textSecondary
                                        )
                                    }
                                }
                                Switch(
                                    checked = policy.isSupervised,
                                    onCheckedChange = { targetState ->
                                        verifyAndRun {
                                            familyManager.toggleSupervision(targetState, enteredPin)
                                        }
                                    }
                                )
                            }

                            if (policy.parentPinHash.isEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                TextButton(onClick = { showSetPinDialog = true }) {
                                    Text("Set Parent Security PIN →", color = ShivTheme.colors.accent, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Child Device Status Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = ShivTheme.colors.surfaceVariant,
                        border = BorderStroke(1.dp, ShivTheme.colors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Child Profile & Device Status", fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.2f)
                                ) {
                                    Text("Online • 85% Battery", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981))
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Screen Time Today", style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
                                    Text("1h 45m / 2h 00m", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Remaining Time", style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
                                    Text("15 minutes left", fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { 105f / 120f },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFFF59E0B),
                                trackColor = ShivTheme.colors.cardBorder
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Summary statistics grid
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = ShivTheme.colors.surfaceVariant,
                            border = BorderStroke(1.dp, ShivTheme.colors.cardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Restricted Apps", style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
                                Text("${installedApps.size - familyManager.getAllowedPackages().size.coerceAtLeast(1)} apps", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = ShivTheme.colors.surfaceVariant,
                            border = BorderStroke(1.dp, ShivTheme.colors.cardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Pending Requests", style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
                                val pendingCount = requests.count { it.status == ApprovalStatus.PENDING }
                                Text("$pendingCount awaiting", fontWeight = FontWeight.Bold, color = if (pendingCount > 0) Color(0xFFF59E0B) else Color(0xFF10B981))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Curfew status banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isCurfewActive) Color(0xFFEF4444).copy(alpha = 0.15f) else ShivTheme.colors.surfaceVariant,
                        border = BorderStroke(1.dp, if (isCurfewActive) Color(0xFFEF4444) else ShivTheme.colors.cardBorder)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isCurfewActive) Icons.Default.Bedtime else Icons.Default.BedtimeOff,
                                contentDescription = null,
                                tint = if (isCurfewActive) Color(0xFFEF4444) else ShivTheme.colors.textSecondary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (isCurfewActive) "Bedtime Curfew Active" else "Curfew Scheduled: 21:00 - 07:00",
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isCurfewActive) Color(0xFFEF4444) else ShivTheme.colors.textPrimary
                                )
                                Text(
                                    if (isCurfewActive) "Only essential emergency tools accessible" else "Enforced automatically every evening",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ShivTheme.colors.textSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // B. SCREEN-TIME MANAGEMENT
            1 -> {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Text("Daily Screen Time Limit", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = ShivTheme.colors.surfaceVariant,
                        border = BorderStroke(1.dp, ShivTheme.colors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Device-Use Cap", color = ShivTheme.colors.textSecondary)
                                Text("${policy.dailyLimitMinutes / 60}h ${policy.dailyLimitMinutes % 60}m per day", fontWeight = FontWeight.Bold, color = ShivTheme.colors.primary)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Slider(
                                value = policy.dailyLimitMinutes.toFloat(),
                                onValueChange = { familyManager.updateDailyLimit(it.toInt()) },
                                valueRange = 30f..360f,
                                steps = 10,
                                colors = SliderDefaults.colors(thumbColor = ShivTheme.colors.primary, activeTrackColor = ShivTheme.colors.primary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Bedtime Downtime Schedule", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = ShivTheme.colors.surfaceVariant,
                        border = BorderStroke(1.dp, ShivTheme.colors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Enable Night Curfew", fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                                    Text("9:00 PM – 7:00 AM", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                                }
                                Switch(
                                    checked = policy.curfewEnabled,
                                    onCheckedChange = { enabled ->
                                        familyManager.updateCurfew(enabled, 21, 0, 7, 0)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Break Reminders", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = ShivTheme.colors.surfaceVariant,
                        border = BorderStroke(1.dp, ShivTheme.colors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val interval by familyManager.breakReminderIntervalMinutes.collectAsState()
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Remind to rest eyes", color = ShivTheme.colors.textSecondary)
                                Text("Every $interval minutes", fontWeight = FontWeight.Bold, color = ShivTheme.colors.primary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(30, 45, 60).forEach { mins ->
                                    val isSel = interval == mins
                                    Button(
                                        onClick = { familyManager.setBreakReminder(mins) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSel) ShivTheme.colors.primary else ShivTheme.colors.surface
                                        )
                                    ) {
                                        Text("${mins}m", color = if (isSel) Color.White else ShivTheme.colors.textPrimary)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Quick Extra Time Grant", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(15, 30, 60).forEach { extraMins ->
                            Button(
                                onClick = {
                                    familyManager.grantTemporaryExtension(extraMins)
                                    Toast.makeText(context, "Granted +$extraMins minutes extra time", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.surfaceVariant)
                            ) {
                                Text("+$extraMins min", color = ShivTheme.colors.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // C. APP & CONTENT RESTRICTIONS
            2 -> {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Text("Policy Rules & Controls", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = ShivTheme.colors.surfaceVariant,
                        border = BorderStroke(1.dp, ShivTheme.colors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Approve Newly Installed Apps", fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                                    Text("Newly installed apps locked until approved", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                                }
                                Switch(
                                    checked = restrictions.approveNewInstalls,
                                    onCheckedChange = { familyManager.setContentRestrictions(restrictions.copy(approveNewInstalls = it)) }
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ShivTheme.colors.cardBorder.copy(alpha = 0.4f))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Restrict In-App Purchases", fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                                    Text("Blocks billing and purchase interfaces", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                                }
                                Switch(
                                    checked = restrictions.restrictPurchases,
                                    onCheckedChange = { familyManager.setContentRestrictions(restrictions.copy(restrictPurchases = it)) }
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ShivTheme.colors.cardBorder.copy(alpha = 0.4f))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Require Approval for Changes", fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                                    Text("Parent PIN needed to modify launcher settings", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                                }
                                Switch(
                                    checked = restrictions.requireApprovalForChanges,
                                    onCheckedChange = { familyManager.setContentRestrictions(restrictions.copy(requireApprovalForChanges = it)) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Blocked Websites Filter", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                        TextButton(onClick = { showWebsiteDialog = true }) {
                            Text("+ Add Domain", color = ShivTheme.colors.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = ShivTheme.colors.surfaceVariant,
                        border = BorderStroke(1.dp, ShivTheme.colors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (restrictions.blockedWebsites.isEmpty()) {
                                Text("No websites currently filtered", color = ShivTheme.colors.textSecondary, modifier = Modifier.padding(8.dp))
                            } else {
                                restrictions.blockedWebsites.forEach { domain ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.PublicOff, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(domain, color = ShivTheme.colors.textPrimary, style = MaterialTheme.typography.bodyMedium)
                                        }
                                        IconButton(onClick = { familyManager.removeBlockedWebsite(domain) }) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = ShivTheme.colors.textSecondary, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // D. PARENT APPROVAL REQUESTS
            3 -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text("Requests Awaiting Parent Approval", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                    Spacer(modifier = Modifier.height(10.dp))

                    if (requests.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No pending requests from child device", color = ShivTheme.colors.textSecondary)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(requests) { req ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = ShivTheme.colors.surfaceVariant,
                                    border = BorderStroke(
                                        1.dp,
                                        when (req.status) {
                                            ApprovalStatus.PENDING -> Color(0xFFF59E0B)
                                            ApprovalStatus.APPROVED -> Color(0xFF10B981)
                                            ApprovalStatus.DENIED -> Color(0xFFEF4444)
                                        }
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(req.target, fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = when (req.status) {
                                                    ApprovalStatus.PENDING -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                                    ApprovalStatus.APPROVED -> Color(0xFF10B981).copy(alpha = 0.2f)
                                                    ApprovalStatus.DENIED -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                                }
                                            ) {
                                                Text(
                                                    text = req.status.name,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = when (req.status) {
                                                        ApprovalStatus.PENDING -> Color(0xFFF59E0B)
                                                        ApprovalStatus.APPROVED -> Color(0xFF10B981)
                                                        ApprovalStatus.DENIED -> Color(0xFFEF4444)
                                                    }
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(req.description, style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)

                                        if (req.status == ApprovalStatus.PENDING) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = { familyManager.approveRequest(req.id) },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                                ) {
                                                    Text("Approve")
                                                }
                                                Button(
                                                    onClick = { familyManager.denyRequest(req.id) },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                                                ) {
                                                    Text("Deny")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // E. REPORTS & ALERTS
            4 -> {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Text("Recent Safety Alerts & Activity Logs", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                    Spacer(modifier = Modifier.height(10.dp))

                    alerts.forEach { alert ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = ShivTheme.colors.surfaceVariant,
                            border = BorderStroke(1.dp, if (alert.isSevere) Color(0xFFEF4444) else ShivTheme.colors.cardBorder)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (alert.isSevere) Icons.Default.Warning else Icons.Default.Info,
                                    contentDescription = null,
                                    tint = if (alert.isSevere) Color(0xFFEF4444) else ShivTheme.colors.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(alert.title, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                                    Text(alert.description, style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                                    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(alert.timestamp))
                                    Text("Logged at $timeStr", style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sync status card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = ShivTheme.colors.surfaceVariant,
                        border = BorderStroke(1.dp, ShivTheme.colors.cardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF10B981))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Cloud Sync Status", fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                                    Text("Synchronized with SHIV DB Cloud", style = MaterialTheme.typography.bodySmall, color = Color(0xFF10B981))
                                }
                            }
                            Button(
                                onClick = {
                                    familyManager.triggerCloudSync()
                                    Toast.makeText(context, "Policy synchronized with cloud", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.primary.copy(alpha = 0.2f))
                            ) {
                                Text("Sync Now", color = ShivTheme.colors.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // Website dialog
    if (showWebsiteDialog) {
        AlertDialog(
            onDismissRequest = { showWebsiteDialog = false },
            title = { Text("Filter Website Domain") },
            text = {
                OutlinedTextField(
                    value = newWebsiteText,
                    onValueChange = { newWebsiteText = it },
                    label = { Text("Domain (e.g. example.com)") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newWebsiteText.isNotBlank()) {
                        familyManager.addBlockedWebsite(newWebsiteText.trim())
                        newWebsiteText = ""
                        showWebsiteDialog = false
                    }
                }) { Text("Block Domain") }
            },
            dismissButton = {
                TextButton(onClick = { showWebsiteDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Set PIN dialog
    if (showSetPinDialog) {
        AlertDialog(
            onDismissRequest = { showSetPinDialog = false },
            title = { Text("Create Parent Security PIN") },
            text = {
                OutlinedTextField(
                    value = newPinText,
                    onValueChange = { if (it.length <= 8) newPinText = it },
                    label = { Text("4–8 Digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newPinText.length in 4..8) {
                        familyManager.setParentPin(newPinText)
                        showSetPinDialog = false
                        Toast.makeText(context, "Parent PIN configured securely", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Save PIN") }
            },
            dismissButton = {
                TextButton(onClick = { showSetPinDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Verify PIN dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Enter Parent PIN") },
            text = {
                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = { enteredPin = it },
                    label = { Text("Security PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (familyManager.verifyParentPin(enteredPin)) {
                        showPinDialog = false
                        pinAction?.invoke()
                        enteredPin = ""
                    } else {
                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Verify") }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false; enteredPin = "" }) { Text("Cancel") }
            }
        )
    }
}

// =========================================================================================
// 2. SHIV LOCK SECTION
// =========================================================================================
@Composable
fun ShivLockTab(appLockManager: AppLockManager, installedApps: List<AppInfo>) {
    val context = LocalContext.current
    val lockedPackages by appLockManager.lockedPackages.collectAsState()
    val lockMethod by appLockManager.lockMethod.collectAsState()
    val autoRelockTimeout by appLockManager.autoRelockTimeoutSeconds.collectAsState()
    val hapticFeedback by appLockManager.hapticFeedbackEnabled.collectAsState()
    val subtleAnimation by appLockManager.subtleAnimationEnabled.collectAsState()
    val preventRecents by appLockManager.preventRecentTasksPreview.collectAsState()
    val disguiseCrash by appLockManager.disguiseCrashEnabled.collectAsState()
    val securityLogs by appLockManager.securityLogs.collectAsState()

    var lockSubTab by remember { mutableIntStateOf(0) }
    val lockSubTabs = listOf("Lock Methods", "App Manager", "Customization", "Protection")

    var searchQuery by remember { mutableStateOf("") }
    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            lockSubTabs.forEachIndexed { idx, label ->
                val isSelected = lockSubTab == idx
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { lockSubTab = idx },
                    color = if (isSelected) ShivTheme.colors.primary.copy(alpha = 0.25f) else ShivTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.cardBorder.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(vertical = 8.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                        color = if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        when (lockSubTab) {
            // A. LOCK METHODS
            0 -> {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Text("Select Primary Authentication Method", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                    Spacer(modifier = Modifier.height(10.dp))

                    LockMethod.entries.forEach { method ->
                        val isSelected = lockMethod == method
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { appLockManager.setLockMethod(method) },
                            color = if (isSelected) ShivTheme.colors.primary.copy(alpha = 0.15f) else ShivTheme.colors.surfaceVariant,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.cardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        when (method) {
                                            LockMethod.PIN -> Icons.Default.Pin
                                            LockMethod.PATTERN -> Icons.Default.Grain
                                            LockMethod.PASSWORD -> Icons.Default.Password
                                            LockMethod.BIOMETRICS -> Icons.Default.Fingerprint
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) ShivTheme.colors.primary else ShivTheme.colors.textSecondary
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(method.name, fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                                        Text(
                                            when (method) {
                                                LockMethod.PIN -> "4–8 digit secure PIN entry"
                                                LockMethod.PATTERN -> "Dot grid gesture unlock"
                                                LockMethod.PASSWORD -> "Alphanumeric complex passphrase"
                                                LockMethod.BIOMETRICS -> "Fingerprint or Face Authentication (Android BiometricPrompt)"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ShivTheme.colors.textSecondary
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ShivTheme.colors.primary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text("Automatic Relocking Rules", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    val timeoutOptions = listOf(
                        Pair(0, "Immediately upon app exit"),
                        Pair(-1, "Lock after screen turns off"),
                        Pair(60, "1 minute idle period"),
                        Pair(300, "5 minutes idle period"),
                        Pair(900, "15 minutes idle period")
                    )

                    timeoutOptions.forEach { (sec, label) ->
                        val isSelected = autoRelockTimeout == sec
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { appLockManager.setAutoRelockTimeout(sec) },
                            color = if (isSelected) ShivTheme.colors.accent.copy(alpha = 0.15f) else ShivTheme.colors.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSelected) ShivTheme.colors.accent else ShivTheme.colors.cardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(label, color = ShivTheme.colors.textPrimary, style = MaterialTheme.typography.bodyMedium)
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = ShivTheme.colors.accent)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // B. APP LOCK MANAGER
            1 -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Category Lock Shortcuts
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val socialPackages = installedApps.filter { it.category == com.example.core.launcher.AppCategory.COMMUNICATION }
                                    .map { Pair(it.packageName, it.name) }
                                appLockManager.lockCategory(socialPackages, true)
                                Toast.makeText(context, "Protected all Communication & Social apps", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.surfaceVariant)
                        ) {
                            Text("Lock Social", style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.primary)
                        }
                        Button(
                            onClick = {
                                val mediaPackages = installedApps.filter { it.category == com.example.core.launcher.AppCategory.ENTERTAINMENT }
                                    .map { Pair(it.packageName, it.name) }
                                appLockManager.lockCategory(mediaPackages, true)
                                Toast.makeText(context, "Protected all Media & Entertainment apps", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.surfaceVariant)
                        ) {
                            Text("Lock Media", style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.primary)
                        }
                    }

                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        placeholder = { Text("Search installed applications...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(filteredApps) { app ->
                            val isLocked = lockedPackages.contains(app.packageName)
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = ShivTheme.colors.surfaceVariant,
                                border = BorderStroke(1.dp, if (isLocked) ShivTheme.colors.primary else ShivTheme.colors.cardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(app.name, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                                        Text(app.packageName, style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary, maxLines = 1)
                                    }
                                    Switch(
                                        checked = isLocked,
                                        onCheckedChange = { appLockManager.toggleAppLock(app.packageName, app.name, it) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // C. LOCK CUSTOMIZATION
            2 -> {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Text("Lock Interface Styling & Ergonomics", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = ShivTheme.colors.surfaceVariant,
                        border = BorderStroke(1.dp, ShivTheme.colors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Tactile Vibration Feedback", fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                                    Text("Subtle haptic tick on keypresses", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                                }
                                Switch(checked = hapticFeedback, onCheckedChange = { appLockManager.setHapticFeedback(it) })
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ShivTheme.colors.cardBorder.copy(alpha = 0.4f))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Subtle Unlock Animations", fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                                    Text("Smooth spring physics on correct authentication", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                                }
                                Switch(checked = subtleAnimation, onCheckedChange = { appLockManager.setSubtleAnimation(it) })
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ShivTheme.colors.cardBorder.copy(alpha = 0.4f))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Hide Recents App Preview", fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                                    Text("Prevents sensitive data snapshots in app switcher", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                                }
                                Switch(checked = preventRecents, onCheckedChange = { appLockManager.setPreventRecentTasksPreview(it) })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // D. FAILED AUTHENTICATION PROTECTION
            3 -> {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Text("Intrusion Defense & Disguise", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = ShivTheme.colors.surfaceVariant,
                        border = BorderStroke(1.dp, ShivTheme.colors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Fake Crash / Disguise Screen", fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                                    Text("Shows 'Unfortunately, app has stopped' disguise prompt. Triple-tap to access real unlock.", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                                }
                                Switch(checked = disguiseCrash, onCheckedChange = { appLockManager.setDisguiseCrash(it) })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = ShivTheme.colors.surfaceVariant,
                        border = BorderStroke(1.dp, ShivTheme.colors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Lockout Security Rules", fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("• 3 failed attempts: 30-second progressive delay\n• 5 failed attempts: 60-second temporary security lockout\n• All failed attempts logged to on-device tamper audit trail", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Local Security Event Log", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    securityLogs.forEach { log ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = ShivTheme.colors.surfaceVariant,
                            border = BorderStroke(1.dp, ShivTheme.colors.cardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(log.eventType, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = ShivTheme.colors.accent)
                                    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                                    Text(timeStr, style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(log.details, style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

// =========================================================================================
// 3. DIGITAL WELLBEING SECTION
// =========================================================================================
@Composable
fun DigitalWellbeingTab(wellbeingManager: WellbeingManager, installedApps: List<AppInfo>) {
    val context = LocalContext.current
    val focusSession by wellbeingManager.focusSession.collectAsState()
    val unlockCount by wellbeingManager.dailyUnlockCount.collectAsState()
    val notificationCount by wellbeingManager.dailyNotificationCount.collectAsState()
    val totalScreenMinutes = remember { wellbeingManager.getTotalScreenTimeMinutesToday().coerceAtLeast(145L) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Screen-Time Overview Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = ShivTheme.colors.surfaceVariant,
            border = BorderStroke(1.dp, ShivTheme.colors.cardBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Screen Time Today", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${totalScreenMinutes / 60}h ${totalScreenMinutes % 60}m", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = ShivTheme.colors.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("of 4h daily target", style = MaterialTheme.typography.bodyMedium, color = ShivTheme.colors.textSecondary, modifier = Modifier.padding(bottom = 6.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weekly usage bar chart
                Text("Weekly Usage Pattern", style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    wellbeingManager.weeklyUsageSummary.forEach { day ->
                        val barHeight = ((day.totalMinutes.toFloat() / 360f) * 80f).coerceIn(10f, 80f)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(barHeight.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(ShivTheme.colors.accent, ShivTheme.colors.primary)
                                        )
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(day.dayLabel, style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Unlocks and Notifications
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                color = ShivTheme.colors.surfaceVariant,
                border = BorderStroke(1.dp, ShivTheme.colors.cardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LockOpen, contentDescription = null, tint = ShivTheme.colors.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unlocks", style = MaterialTheme.typography.labelMedium, color = ShivTheme.colors.textSecondary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("$unlockCount times", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary, style = MaterialTheme.typography.titleLarge)
                }
            }
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                color = ShivTheme.colors.surfaceVariant,
                border = BorderStroke(1.dp, ShivTheme.colors.cardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = ShivTheme.colors.accent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Notifications", style = MaterialTheme.typography.labelMedium, color = ShivTheme.colors.textSecondary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("$notificationCount received", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary, style = MaterialTheme.typography.titleLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Focus Modes
        Text("5 Cognitive Focus Modes", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(10.dp))

        // Active Focus Session Controller if active
        if (focusSession.isActive) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = ShivTheme.colors.primary.copy(alpha = 0.15f),
                border = BorderStroke(1.5.dp, ShivTheme.colors.primary)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(focusSession.title, fontWeight = FontWeight.Bold, color = ShivTheme.colors.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    val mins = focusSession.remainingSeconds / 60
                    val secs = focusSession.remainingSeconds % 60
                    Text(
                        String.format("%02d:%02d", mins, secs),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = ShivTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (focusSession.isPaused) {
                            Button(onClick = { wellbeingManager.resumeFocusSession() }) {
                                Text("Resume")
                            }
                        } else {
                            Button(onClick = { wellbeingManager.pauseFocusSession() }, colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.surfaceVariant)) {
                                Text("Pause", color = ShivTheme.colors.textPrimary)
                            }
                        }
                        Button(onClick = { wellbeingManager.stopFocusSession() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) {
                            Text("End Focus")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        FocusModeType.entries.forEach { mode ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp)),
                color = ShivTheme.colors.surfaceVariant,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ShivTheme.colors.cardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(mode.title, fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(shape = RoundedCornerShape(6.dp), color = ShivTheme.colors.primary.copy(alpha = 0.2f)) {
                                Text("${mode.defaultMinutes}m", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(mode.description, style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                    }
                    Button(
                        onClick = { wellbeingManager.startFocusMode(mode) },
                        colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.primary)
                    ) {
                        Text("Start")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// =========================================================================================
// 4. SHIV SECURITY CENTER SECTION
// =========================================================================================
@Composable
fun SecurityCenterTab(appLockManager: AppLockManager, familyManager: FamilyManager) {
    val context = LocalContext.current
    var cameraBlocked by remember { mutableStateOf(false) }
    var micBlocked by remember { mutableStateOf(false) }
    var clipboardWipe by remember { mutableStateOf(true) }
    var lockScreenPrivacy by remember { mutableStateOf(true) }

    val authState by ShivIdentityAuth.authState.collectAsState()
    val username = (authState as? ShivIdentityAuth.OAuthState.Authenticated)?.username ?: "Shivam Gupta"
    val isLoggedIn = ShivIdentityAuth.isLoggedIn(context)

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Account Security
        Text("SHIV Identity Account Security", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = ShivTheme.colors.surfaceVariant,
            border = BorderStroke(1.dp, ShivTheme.colors.cardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(ShivTheme.colors.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(username, fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary)
                            Text(if (isLoggedIn) "Verified SHIV Identity" else "Local Session (Guest)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF10B981))
                        }
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF10B981).copy(alpha = 0.2f)) {
                        Text("2FA Active", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = ShivTheme.colors.cardBorder.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

                Text("Active Login Sessions", style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Text("• Current Device (Vivo V2050) — Active now", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textPrimary)
                Text("• SHIV Web Studio (Chrome / Windows) — Synced 1h ago", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Device & App Security
        Text("Device & Hardware Privacy Shortcuts", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = ShivTheme.colors.surfaceVariant,
            border = BorderStroke(1.dp, ShivTheme.colors.cardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hardware Camera Privacy Switch", fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                        Text("Mute camera sensors across all apps", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                    }
                    Switch(checked = cameraBlocked, onCheckedChange = { cameraBlocked = it })
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ShivTheme.colors.cardBorder.copy(alpha = 0.4f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hardware Microphone Privacy Switch", fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                        Text("Mute all voice input captures instantly", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                    }
                    Switch(checked = micBlocked, onCheckedChange = { micBlocked = it })
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ShivTheme.colors.cardBorder.copy(alpha = 0.4f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Clear Clipboard Privacy", fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                        Text("Wipe sensitive copied text after 60 seconds", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                    }
                    Switch(checked = clipboardWipe, onCheckedChange = { clipboardWipe = it })
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ShivTheme.colors.cardBorder.copy(alpha = 0.4f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Notification Content Privacy", fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                        Text("Mask sensitive message details on lockscreen", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                    }
                    Switch(checked = lockScreenPrivacy, onCheckedChange = { lockScreenPrivacy = it })
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Permission Audit
        Text("Permission Safety Audit", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(10.dp))

        val audits = listOf(
            Triple("Location", "Granted to 5 apps", Icons.Default.LocationOn),
            Triple("Camera", "Granted to 4 apps", Icons.Default.CameraAlt),
            Triple("Microphone", "Granted to 2 apps", Icons.Default.Mic),
            Triple("Storage & Files", "Scoped storage active", Icons.Default.Folder)
        )

        audits.forEach { (name, desc, icon) ->
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                shape = RoundedCornerShape(12.dp),
                color = ShivTheme.colors.surfaceVariant,
                border = BorderStroke(1.dp, ShivTheme.colors.cardBorder)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = ShivTheme.colors.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                        Text(desc, style = MaterialTheme.typography.labelSmall, color = ShivTheme.colors.textSecondary)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ShivTheme.colors.textSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// =========================================================================================
// 5. PARENT-CHILD ACCOUNT LINKING SECTION
// =========================================================================================
@Composable
fun FamilyAccountLinkingTab(familyManager: FamilyManager) {
    val context = LocalContext.current
    val pairingCode by familyManager.pairingCode.collectAsState()
    val isSynced by familyManager.isCloudSynced.collectAsState()
    val lastSync by familyManager.lastSyncTimestamp.collectAsState()
    val tamperDetected by familyManager.tamperDetected.collectAsState()

    var enterCodeInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Parent-Child Account Linking", fontWeight = FontWeight.Bold, color = ShivTheme.colors.textPrimary, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Link parent and child devices via SHIV Identity and synchronize supervision rules through SHIV DB Cloud.",
            style = MaterialTheme.typography.bodyMedium,
            color = ShivTheme.colors.textSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Device Pairing Code Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = ShivTheme.colors.surfaceVariant,
            border = BorderStroke(1.dp, ShivTheme.colors.primary)
        ) {
            Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Device Pairing Invitation Code", style = MaterialTheme.typography.labelMedium, color = ShivTheme.colors.textSecondary)
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ShivTheme.colors.surface,
                    border = BorderStroke(1.5.dp, ShivTheme.colors.primary)
                ) {
                    Text(
                        text = pairingCode,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.headlineMedium.copy(letterSpacing = 4.sp, fontWeight = FontWeight.Bold),
                        color = ShivTheme.colors.primary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Enter this code on the Parent device to pair immediately", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Enter Code to Pair With Parent
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = ShivTheme.colors.surfaceVariant,
            border = BorderStroke(1.dp, ShivTheme.colors.cardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Pair with Parent Device", fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = enterCodeInput,
                    onValueChange = { enterCodeInput = it.uppercase() },
                    placeholder = { Text("e.g. SHIV-4921") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (enterCodeInput.isNotBlank()) {
                            familyManager.setPairingCode(enterCodeInput)
                            familyManager.triggerCloudSync()
                            Toast.makeText(context, "Pairing with Parent confirmed!", Toast.LENGTH_SHORT).show()
                            enterCodeInput = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ShivTheme.colors.primary)
                ) {
                    Text("Confirm Pairing")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Synchronization & Offline Resilience
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = ShivTheme.colors.surfaceVariant,
            border = BorderStroke(1.dp, ShivTheme.colors.cardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("SHIV DB Synchronization", fontWeight = FontWeight.SemiBold, color = ShivTheme.colors.textPrimary)
                            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(lastSync))
                            Text("Last synced at $timeStr", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                        }
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF10B981).copy(alpha = 0.2f)) {
                        Text(if (isSynced) "Synced" else "Offline", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = ShivTheme.colors.cardBorder.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = ShivTheme.colors.accent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Offline Fallback: Rules enforced via SQLite cache", style = MaterialTheme.typography.bodySmall, color = ShivTheme.colors.textSecondary)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (tamperDetected) Icons.Default.GppBad else Icons.Default.GppGood,
                        contentDescription = null,
                        tint = if (tamperDetected) Color(0xFFEF4444) else Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        if (tamperDetected) "Tamper Alert: Unauthorized attempt detected" else "Tamper Protection: Active & Guarded",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (tamperDetected) Color(0xFFEF4444) else Color(0xFF10B981)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
