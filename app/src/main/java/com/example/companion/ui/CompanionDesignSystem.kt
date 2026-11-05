package com.example.companion.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawCircle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A deliberately restrained companion visual system.  Ambient depth is static and
 * interaction feedback only animates after a user action, so this UI can safely
 * live above other applications without an always-on render loop.
 */
internal object CompanionPalette {
    val Ink = Color(0xFF060914)
    val InkRaised = Color(0xFF0B1020)
    val Panel = Color(0xF0121930)
    val PanelStrong = Color(0xFF18223A)
    val Line = Color(0xFF273552)
    val Cyan = Color(0xFF6DE8FF)
    val CyanDim = Color(0xFF278AA7)
    val Violet = Color(0xFFB794FF)
    val Amber = Color(0xFFFFC86B)
    val Green = Color(0xFF72E4AC)
    val Red = Color(0xFFFF8A9A)
    val Text = Color(0xFFF3F7FF)
    val Muted = Color(0xFFA9B6CC)
    val Quiet = Color(0xFF71809B)
}

@Composable
internal fun CompanionBackground(
    modifier: Modifier = Modifier,
    content: @Composable Box.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(CompanionPalette.Ink, Color(0xFF0A1122), CompanionPalette.Ink)
                )
            )
            .drawBehind {
                // Static atmospheric light.  This is drawn only during layout or
                // invalidation, unlike an animated shader or particle loop.
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CompanionPalette.Cyan.copy(alpha = 0.09f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.86f, size.height * 0.08f),
                        radius = size.width * 0.72f
                    ),
                    radius = size.width * 0.72f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.86f, size.height * 0.08f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CompanionPalette.Violet.copy(alpha = 0.08f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.08f, size.height * 0.68f),
                        radius = size.width * 0.84f
                    ),
                    radius = size.width * 0.84f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.08f, size.height * 0.68f)
                )
            },
        content = content
    )
}

@Composable
internal fun CompanionTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.size(42.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CompanionPalette.Text)
                }
                Spacer(Modifier.width(4.dp))
            }
            Column {
                Text(
                    text = title,
                    color = CompanionPalette.Text,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        color = CompanionPalette.Muted,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        action?.invoke()
    }
}

@Composable
internal fun CompanionSectionLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = CompanionPalette.Quiet,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.1.sp
    )
}

@Composable
internal fun CompanionSurface(
    modifier: Modifier = Modifier,
    accent: Color = CompanionPalette.Line,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable Column.() -> Unit
) {
    val interactiveModifier = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)
    Surface(
        modifier = modifier.then(interactiveModifier),
        shape = RoundedCornerShape(24.dp),
        color = CompanionPalette.Panel,
        border = BorderStroke(1.dp, accent.copy(alpha = if (accent == CompanionPalette.Line) 0.8f else 0.56f)),
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Composable
internal fun CompanionStatusPill(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    leading: ImageVector? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.36f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leading != null) {
                Icon(leading, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(5.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
internal fun CompanionPrimaryButton(
    text: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier.height(50.dp),
        onClick = onClick,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 18.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CompanionPalette.Cyan,
            contentColor = CompanionPalette.Ink,
            disabledContainerColor = CompanionPalette.PanelStrong,
            disabledContentColor = CompanionPalette.Quiet
        )
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.Bold, letterSpacing = 0.2.sp)
    }
}

@Composable
internal fun CompanionActionTile(
    title: String,
    description: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val pressedColor by animateColorAsState(
        targetValue = CompanionPalette.Panel,
        animationSpec = tween(160),
        label = "tile_color"
    )
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = pressedColor,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.38f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Surface(shape = RoundedCornerShape(12.dp), color = accent.copy(alpha = 0.15f)) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.padding(9.dp).size(20.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(title, color = CompanionPalette.Text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Spacer(Modifier.height(3.dp))
            Text(description, color = CompanionPalette.Muted, fontSize = 10.sp, lineHeight = 13.sp, maxLines = 2)
        }
    }
}

@Composable
internal fun MeterBar(
    value: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val safeValue = value.coerceIn(0f, 1f)
    Surface(
        modifier = modifier.height(7.dp),
        shape = RoundedCornerShape(50),
        color = CompanionPalette.Line
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(safeValue)
                    .fillMaxSize()
                    .background(color)
            )
        }
    }
}
