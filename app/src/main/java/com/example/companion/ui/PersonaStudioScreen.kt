package com.example.companion.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.companion.avatar.AvatarRenderContext
import com.example.companion.avatar.AvatarState
import com.example.companion.avatar.AvatarType
import com.example.companion.avatar.PremiumAvatar3DScene
import com.example.companion.avatar.SeasonalSkin

/**
 * A non-destructive studio. Selecting an avatar prepares the scene for the
 * command deck; it does not unexpectedly start the system overlay service.
 */
@Composable
fun PersonaStudioScreen(
    currentAvatar: AvatarType,
    onAvatarSelected: (AvatarType) -> Unit,
    onBack: () -> Unit
) {
    var selectedVoice by remember { mutableStateOf("Madhur") }
    var wakeWordSensitivity by remember { mutableFloatStateOf(0.78f) }
    var selectedSkin by remember { mutableStateOf(SeasonalSkin.DEFAULT) }

    CompanionBackground {
        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(12.dp)) }
            item {
                CompanionTopBar(
                    title = "Persona Studio",
                    subtitle = "Shape your companion's presence",
                    onBack = onBack
                )
            }
            item { CompanionSectionLabel("Active companion") }
            item {
                AvatarHeroPreview(currentAvatar = currentAvatar, skin = selectedSkin)
            }
            item { CompanionSectionLabel("Choose a persona") }
            item {
                AvatarType.entries.forEach { avatar ->
                    PersonaChoice(
                        avatar = avatar,
                        selected = avatar == currentAvatar,
                        onClick = { onAvatarSelected(avatar) }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
            item { CompanionSectionLabel("Voice and wake word") }
            item {
                WakeWordControl(
                    sensitivity = wakeWordSensitivity,
                    onSensitivityChanged = { wakeWordSensitivity = it }
                )
            }
            item {
                VoicePicker(
                    selectedVoice = selectedVoice,
                    onVoiceSelected = { selectedVoice = it }
                )
            }
            item { CompanionSectionLabel("Scene atmosphere") }
            item {
                SkinPicker(selectedSkin = selectedSkin, onSkinSelected = { selectedSkin = it })
            }
            item { Spacer(Modifier.height(28.dp)) }
        }
    }
}

@Composable
private fun AvatarHeroPreview(currentAvatar: AvatarType, skin: SeasonalSkin) {
    CompanionSurface(
        modifier = Modifier.fillMaxWidth(),
        accent = CompanionPalette.Violet,
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(248.dp), contentAlignment = Alignment.Center) {
            PremiumAvatar3DScene(
                renderContext = AvatarRenderContext(
                    avatarType = currentAvatar,
                    state = AvatarState.IDLE,
                    seasonalSkin = skin
                ),
                modifier = Modifier.size(206.dp)
            )
            CompanionStatusPill(
                label = "Selected",
                color = CompanionPalette.Violet,
                modifier = Modifier.align(Alignment.TopEnd).padding(14.dp),
                leading = Icons.Default.Check
            )
            Text(
                text = currentAvatar.title,
                color = CompanionPalette.Text,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.BottomStart).padding(18.dp)
            )
        }
    }
}

@Composable
private fun PersonaChoice(avatar: AvatarType, selected: Boolean, onClick: () -> Unit) {
    CompanionSurface(
        modifier = Modifier.fillMaxWidth(),
        accent = if (selected) CompanionPalette.Cyan else CompanionPalette.Line,
        contentPadding = PaddingValues(12.dp),
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(62.dp), contentAlignment = Alignment.Center) {
                PremiumAvatar3DScene(
                    renderContext = AvatarRenderContext(
                        avatarType = avatar,
                        state = if (selected) AvatarState.IDLE else AvatarState.DROWSY
                    ),
                    modifier = Modifier.size(56.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(avatar.title, color = CompanionPalette.Text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(avatar.subtitle, color = CompanionPalette.Muted, fontSize = 11.sp, lineHeight = 14.sp)
            }
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = "Selected", tint = CompanionPalette.Cyan)
            }
        }
    }
}

@Composable
private fun WakeWordControl(sensitivity: Float, onSensitivityChanged: (Float) -> Unit) {
    CompanionSurface(accent = CompanionPalette.Cyan) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Mic, contentDescription = null, tint = CompanionPalette.Cyan)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Om Namas Shivaya", color = CompanionPalette.Text, fontWeight = FontWeight.SemiBold)
                Text("Offline wake word, calibrated locally", color = CompanionPalette.Muted, fontSize = 12.sp)
            }
            CompanionStatusPill("On device", CompanionPalette.Green)
        }
        Spacer(Modifier.height(18.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Sensitivity", color = CompanionPalette.Muted, fontSize = 12.sp)
            Text("${(sensitivity * 100).toInt()}%", color = CompanionPalette.Cyan, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
        Slider(
            value = sensitivity,
            onValueChange = onSensitivityChanged,
            colors = SliderDefaults.colors(
                thumbColor = CompanionPalette.Cyan,
                activeTrackColor = CompanionPalette.Cyan,
                inactiveTrackColor = CompanionPalette.Line
            )
        )
    }
}

@Composable
private fun VoicePicker(selectedVoice: String, onVoiceSelected: (String) -> Unit) {
    val choices = listOf(
        "Madhur" to "Warm Hindi and bilingual guidance",
        "Prabhat" to "Clear Indian English companion",
        "Aarav" to "Calm, deliberate coaching voice"
    )
    CompanionSurface {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = CompanionPalette.Violet)
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Voice character", color = CompanionPalette.Text, fontWeight = FontWeight.SemiBold)
                Text("Select the speaking profile", color = CompanionPalette.Muted, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        choices.forEachIndexed { index, (name, detail) ->
            val selected = name == selectedVoice
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onVoiceSelected(name) }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompanionStatusPill(if (selected) "Active" else "", if (selected) CompanionPalette.Violet else CompanionPalette.Line)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, color = if (selected) CompanionPalette.Text else CompanionPalette.Muted, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    Text(detail, color = CompanionPalette.Quiet, fontSize = 11.sp)
                }
                if (selected) Icon(Icons.Default.GraphicEq, contentDescription = null, tint = CompanionPalette.Violet, modifier = Modifier.size(18.dp))
            }
            if (index < choices.lastIndex) MeterBar(1f, CompanionPalette.Line)
        }
    }
}

@Composable
private fun SkinPicker(selectedSkin: SeasonalSkin, onSkinSelected: (SeasonalSkin) -> Unit) {
    CompanionSurface {
        SeasonalSkin.entries.forEachIndexed { index, skin ->
            val selected = skin == selectedSkin
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSkinSelected(skin) }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompanionStatusPill(
                    label = if (selected) "Active" else "",
                    color = if (selected) CompanionPalette.Amber else CompanionPalette.Line
                )
                Spacer(Modifier.width(10.dp))
                Text(skin.title, color = if (selected) CompanionPalette.Text else CompanionPalette.Muted, modifier = Modifier.weight(1f), fontSize = 13.sp)
                if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = CompanionPalette.Amber, modifier = Modifier.size(18.dp))
            }
            if (index < SeasonalSkin.entries.lastIndex) MeterBar(1f, CompanionPalette.Line)
        }
    }
}
