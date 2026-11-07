package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NexaLanguage
import com.example.ui.ToastMessage
import com.example.ui.theme.NexaBg
import com.example.ui.theme.NexaBorder
import com.example.ui.theme.NexaBorderGlowing
import com.example.ui.theme.NexaCard
import com.example.ui.theme.NexaGlow
import com.example.ui.theme.NexaPink
import com.example.ui.theme.NexaPurple
import com.example.ui.theme.NexaTextPrimary
import com.example.ui.theme.NexaTextSecondary
import kotlinx.coroutines.delay

@Composable
fun NexaCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    borderColor: Color = NexaBorderGlowing,
    backgroundColor: Color = NexaCard,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val clickableMod = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Surface(
        modifier = clickableMod
            .clip(shape)
            .border(BorderStroke(1.dp, borderColor), shape),
        shape = shape,
        color = backgroundColor.copy(alpha = 0.85f),
        tonalElevation = 4.dp
    ) {
        Box(
            modifier = Modifier.background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF16162C).copy(alpha = 0.7f),
                        Color(0xFF0F0F1E).copy(alpha = 0.85f)
                    )
                )
            )
        ) {
            content()
        }
    }
}

@Composable
fun SoundwaveVisualizer(
    isActive: Boolean,
    rmsLevel: Float = 0f,
    modifier: Modifier = Modifier
) {
    val barCount = 7
    val infiniteTransition = rememberInfiniteTransition(label = "eq")

    val anim1 by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = 24f,
        animationSpec = infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bar1"
    )
    val anim2 by infiniteTransition.animateFloat(
        initialValue = 6f, targetValue = 28f,
        animationSpec = infiniteRepeatable(tween(550, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bar2"
    )
    val anim3 by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = 20f,
        animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bar3"
    )
    val anim4 by infiniteTransition.animateFloat(
        initialValue = 8f, targetValue = 30f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bar4"
    )

    Row(
        modifier = modifier.height(32.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val heights = if (isActive) {
            val boost = (rmsLevel * 20f)
            listOf(
                (anim1 + boost).coerceIn(4f, 32f),
                (anim2 + boost).coerceIn(6f, 32f),
                (anim4 + boost).coerceIn(8f, 32f),
                (anim3 + boost).coerceIn(4f, 32f),
                (anim2 + boost).coerceIn(6f, 32f),
                (anim1 + boost).coerceIn(4f, 32f),
                (anim3 + boost).coerceIn(4f, 32f)
            )
        } else {
            List(barCount) { 4f }
        }

        heights.forEach { h ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(h.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(NexaPink, NexaPurple)
                        )
                    )
            )
        }
    }
}

@Composable
fun MicOrbButton(
    isListening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_orb")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier
            .size(110.dp)
            .clickable(onClick = onClick)
            .testTag("main_mic_orb_button"),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing ring
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(105.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                NexaPink.copy(alpha = glowAlpha),
                                NexaPurple.copy(alpha = glowAlpha * 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Inner glowing sphere
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .border(
                    BorderStroke(
                        2.dp,
                        if (isListening) NexaPink else NexaPurple.copy(alpha = 0.6f)
                    ),
                    CircleShape
                )
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF2A164D),
                            Color(0xFF120B24),
                            Color(0xFF0A0614)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                contentDescription = "Microphone",
                tint = if (isListening) NexaPink else NexaPurple,
                modifier = Modifier.size(38.dp)
            )
        }
    }
}

@Composable
fun CosmicOrbButton(
    isListening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cosmic_orb")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    Box(
        modifier = modifier
            .size(62.dp)
            .clip(CircleShape)
            .border(
                BorderStroke(
                    2.dp,
                    if (isListening) NexaPink else NexaPurple.copy(alpha = 0.7f)
                ),
                CircleShape
            )
            .clickable(onClick = onClick)
            .testTag("floating_cosmic_orb_button"),
        contentAlignment = Alignment.Center
    ) {
        // Rotating cosmic aura
        Box(
            modifier = Modifier
                .size(46.dp)
                .rotate(rotation)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            NexaPurple,
                            NexaPink,
                            NexaGlow,
                            NexaPurple
                        )
                    )
                )
        )

        // Center mic core
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F0B1E)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Cosmic Voice Orb",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun NexaTopBar(
    currentLanguage: NexaLanguage,
    onLanguageChange: (NexaLanguage) -> Unit,
    activeMode: com.example.model.NexaMode? = null,
    onOpenModes: (() -> Unit)? = null,
    onOpenQuickActions: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogoClick: () -> Unit,
    isOfflineActive: Boolean = false,
    onOpenOfflineHub: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var langMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(NexaBg.copy(alpha = 0.85f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left menu button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                .clickable(onClick = onOpenQuickActions),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Quick Actions",
                tint = NexaTextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        // Center Brand Logo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(onClick = onLogoClick)
        ) {
            Text(
                text = "NEXA AI",
                color = NexaTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                text = "YOUR AI ASSISTANT",
                color = NexaTextSecondary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Right: Mode Button + Language Dropdown + Settings
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Offline mode badge
            if (isOfflineActive) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(8.dp))
                        .clickable { onOpenOfflineHub?.invoke() }
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "⚡ OFFLINE",
                        color = Color(0xFFFBBF24),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Mode shortcut badge
            if (activeMode != null && onOpenModes != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NexaCard)
                        .border(1.dp, NexaPink.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpenModes)
                        .padding(horizontal = 7.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = activeMode.emoji,
                        fontSize = 11.sp
                    )
                }
            }

            Box {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NexaCard)
                        .border(1.dp, NexaPurple.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable { langMenuExpanded = true }
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = currentLanguage.shortLabel,
                        color = NexaPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                DropdownMenu(
                    expanded = langMenuExpanded,
                    onDismissRequest = { langMenuExpanded = false },
                    modifier = Modifier.background(NexaCard)
                ) {
                    NexaLanguage.entries.forEach { lang ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${lang.shortLabel} - ${lang.displayName}",
                                    color = if (lang == currentLanguage) NexaPurple else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = if (lang == currentLanguage) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                langMenuExpanded = false
                                onLanguageChange(lang)
                            }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenSettings),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = NexaTextPrimary,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
fun NexaToastAlert(
    toast: ToastMessage?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = toast != null,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier
    ) {
        if (toast != null) {
            LaunchedEffect(toast.timestamp) {
                delay(3200)
                onDismiss()
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = NexaPurple.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, NexaPurple.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = toast.message,
                        color = Color(0xFFDDD6FE),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Status",
                        tint = NexaPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
