package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppTab
import com.example.model.NexaMode
import com.example.ui.NexaViewModel
import com.example.ui.components.NexaCard
import com.example.ui.theme.NexaBg
import com.example.ui.theme.NexaCard
import com.example.ui.theme.NexaPink
import com.example.ui.theme.NexaPurple
import com.example.ui.theme.NexaSuccess
import com.example.ui.theme.NexaTextPrimary
import com.example.ui.theme.NexaTextSecondary

@Composable
fun ModesScreen(
    viewModel: NexaViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.switchTab(AppTab.HOME)
    }

    val activeMode by viewModel.modeFlow.collectAsState()
    val customPrompt by viewModel.customModePrompt.collectAsState()
    var editingCustomPrompt by remember(customPrompt) { mutableStateOf(customPrompt) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Back Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .clickable { viewModel.switchTab(AppTab.HOME) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = NexaTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "AI Personas & Modes",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Choose how NEXA speaks, thinks, and relates to you",
                    color = NexaTextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // List of all 6 modes
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(NexaMode.entries) { mode ->
                val isActive = mode == activeMode
                val themeColor = getModeColor(mode)
                val icon = getModeIcon(mode)

                NexaCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (isActive) themeColor else themeColor.copy(alpha = 0.25f),
                    backgroundColor = if (isActive) Color(0xFF181530) else NexaCard,
                    onClick = { viewModel.setMode(mode) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header with icon, title, subtitle & active badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(themeColor.copy(alpha = 0.2f))
                                        .border(1.dp, themeColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = mode.title,
                                        tint = themeColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = mode.title,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = mode.emoji,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Text(
                                        text = mode.subtitle,
                                        color = NexaTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Active / Select pill
                            if (isActive) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(themeColor.copy(alpha = 0.25f))
                                        .border(1.dp, themeColor, RoundedCornerShape(20.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Active",
                                            tint = themeColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "ACTIVE",
                                            color = themeColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White.copy(alpha = 0.06f))
                                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Select",
                                        color = NexaTextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Description
                        Text(
                            text = mode.description,
                            color = NexaTextPrimary.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        // Sample Greeting Quote
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(NexaBg.copy(alpha = 0.7f))
                                .border(1.dp, themeColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "💬 \"${mode.sampleGreeting}\"",
                                color = themeColor.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        // Custom Mode prompt editor
                        if (mode == NexaMode.CUSTOM) {
                            AnimatedVisibility(visible = true) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Your Custom Persona Instructions:",
                                        color = NexaTextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    OutlinedTextField(
                                        value = editingCustomPrompt,
                                        onValueChange = { editingCustomPrompt = it },
                                        placeholder = {
                                            Text(
                                                "e.g. Speak like a wise cyberpunk philosopher from 2099...",
                                                color = NexaTextSecondary,
                                                fontSize = 11.sp
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(84.dp)
                                            .testTag("custom_mode_prompt_input"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = NexaBg,
                                            unfocusedContainerColor = NexaBg,
                                            focusedBorderColor = NexaPurple,
                                            unfocusedBorderColor = NexaPurple.copy(alpha = 0.4f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                    Button(
                                        onClick = {
                                            viewModel.updateCustomModePrompt(editingCustomPrompt)
                                            if (activeMode != NexaMode.CUSTOM) {
                                                viewModel.setMode(NexaMode.CUSTOM)
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(38.dp)
                                            .testTag("save_custom_mode_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = NexaPurple),
                                        shape = RoundedCornerShape(18.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Save,
                                            contentDescription = "Save Prompt",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Save & Apply Custom Persona",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

private fun getModeColor(mode: NexaMode): Color {
    return when (mode) {
        NexaMode.GIRLFRIEND -> NexaPink
        NexaMode.DEVELOPER -> Color(0xFF38BDF8) // Cyan / Dev Blue
        NexaMode.TEACHER -> Color(0xFFFBBF24) // Amber / Teacher Gold
        NexaMode.FRIEND -> Color(0xFF34D399) // Emerald / Buddy Green
        NexaMode.NATURAL -> NexaPurple // Neon Purple
        NexaMode.CUSTOM -> Color(0xFFA855F7) // Glowing Violet
    }
}

private fun getModeIcon(mode: NexaMode): ImageVector {
    return when (mode) {
        NexaMode.GIRLFRIEND -> Icons.Default.Favorite
        NexaMode.DEVELOPER -> Icons.Default.Code
        NexaMode.TEACHER -> Icons.Default.School
        NexaMode.FRIEND -> Icons.Default.People
        NexaMode.NATURAL -> Icons.Default.Psychology
        NexaMode.CUSTOM -> Icons.Default.Tune
    }
}
