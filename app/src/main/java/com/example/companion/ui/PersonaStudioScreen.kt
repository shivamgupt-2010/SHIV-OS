package com.example.companion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.companion.avatar.*
import com.example.companion.voice.WakeWordConfig

/**
 * PersonaStudioScreen:
 * Customization Studio for Avatar Personas, Neural Voices,
 * Wake Word Sensitivity ("Om Namas Shivaya"), and Seasonal Skins.
 */
@Composable
fun PersonaStudioScreen(
    currentAvatar: AvatarType,
    onAvatarSelected: (AvatarType) -> Unit,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedVoice by remember { mutableStateOf("hi-IN-MadhurNeural") }
    var wakeWordSensitivity by remember { mutableFloatStateOf(0.85f) }
    var selectedSkin by remember { mutableStateOf(SeasonalSkin.DEFAULT) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF020617), Color(0xFF0F172A), Color(0xFF020617))
                )
            )
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("PERSONA & VOICE STUDIO", color = Color(0xFF00F0FF), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Avatar Persona Selection (The 3 Core Personas)
        Text("SELECT ACTIVE AVATAR", color = Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        AvatarType.values().forEach { avatar ->
            val isSelected = avatar == currentAvatar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        onAvatarSelected(avatar)
                        com.example.companion.overlay.FloatingAvatarService.start(context, avatar)
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.2.dp,
                    if (isSelected) Color(0xFF00F0FF) else Color(0xFF1E293B)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(54.dp)) {
                        val ctx = AvatarRenderContext(avatarType = avatar)
                        when (avatar) {
                            AvatarType.INDIE_CACOONAUT -> IndieCacoonautRenderer(ctx)
                            AvatarType.PHANTOM_NEKO -> PhantomNekoRenderer(ctx)
                            AvatarType.COSMIC_SHIV -> CosmicShivRenderer(ctx)
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(avatar.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(avatar.subtitle, color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00F0FF))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Default Wake Word Settings
        Text("WAKE WORD CONFIGURATION", color = Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Default Sacred Wake Word:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Text(
                    text = "\"Om Namas Shivaya\" (ॐ नमः शिवाय)",
                    color = Color(0xFF38BDF8),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Wake Word Sensitivity (${(wakeWordSensitivity * 100).toInt()}%)", color = Color(0xFFE2E8F0), fontSize = 12.sp)
                Slider(
                    value = wakeWordSensitivity,
                    onValueChange = { wakeWordSensitivity = it },
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF00F0FF), activeTrackColor = Color(0xFF0284C7))
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Neural Voice Selection
        Text("NEURAL VOICE MODEL", color = Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        listOf(
            "hi-IN-MadhurNeural" to "Madhur (Warm Natural Hindi)",
            "en-IN-PrabhatNeural" to "Prabhat (Natural Indian English)",
            "en-GB-RyanNeural" to "Jarvis (British Butler AI)"
        ).forEach { (voiceId, label) ->
            val isSelected = selectedVoice == voiceId
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clickable { selectedVoice = voiceId },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E293B)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, color = if (isSelected) Color.White else Color(0xFF94A3B8), fontSize = 12.sp)
                    if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Seasonal Skin Selector
        Text("SEASONAL & ATMOSPHERIC SKINS", color = Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        SeasonalSkin.values().forEach { skin ->
            val isSelected = selectedSkin == skin
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clickable { selectedSkin = skin },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) Color(0xFFF59E0B) else Color(0xFF1E293B)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(skin.title, color = if (isSelected) Color(0xFFFBBF24) else Color(0xFF94A3B8), fontSize = 12.sp)
                    if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
