package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Image as ImageIcon
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppTab
import com.example.ui.NexaViewModel
import com.example.ui.components.MicOrbButton
import com.example.ui.components.NexaCard
import com.example.ui.components.SoundwaveVisualizer
import com.example.ui.theme.NexaBg
import com.example.ui.theme.NexaCard
import com.example.ui.theme.NexaPink
import com.example.ui.theme.NexaPurple
import com.example.ui.theme.NexaTextPrimary
import com.example.ui.theme.NexaTextSecondary

@Composable
fun HomeScreen(
    viewModel: NexaViewModel,
    modifier: Modifier = Modifier
) {
    val speechManager = viewModel.speechManager
    val isListening by speechManager?.isListening?.collectAsState() ?: remember { mutableStateOf(false) }
    val isSpeaking by speechManager?.isSpeaking?.collectAsState() ?: remember { mutableStateOf(false) }
    val rmsLevel by speechManager?.rmsLevel?.collectAsState() ?: remember { mutableStateOf(0f) }
    val partialTranscript by speechManager?.partialTranscript?.collectAsState() ?: remember { mutableStateOf("") }
    val activeMode by viewModel.modeFlow.collectAsState()
    val latestResponse by viewModel.latestHomeResponse.collectAsState()
    val isProcessing by viewModel.isQueryProcessing.collectAsState()
    val isOfflineModeActive by viewModel.isOfflineModeActive.collectAsState()
    val selectedVoiceId by viewModel.selectedVoiceIdFlow.collectAsState()
    val activeVoice = remember(selectedVoiceId) {
        com.example.model.NEXA_VOICES.find { it.voiceId.equals(selectedVoiceId, ignoreCase = true) }
            ?: com.example.model.NEXA_VOICES[0]
    }

    var textInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val quickQuestions = listOf(
        "Screen lock koro 🔒",
        "Generate image of a cyber cat 🎨",
        "Kemon acho? ♡",
        "Background Service on ⚡",
        "Write Python code",
        "Tell me a joke"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero AI Avatar Card with Cyber Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(32.dp))
                .border(1.dp, NexaPurple.copy(alpha = 0.35f), RoundedCornerShape(32.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.nexa_avatar_1793092081901),
                contentDescription = "Nexa AI Character",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Cyber neon gradients
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                NexaBg.copy(alpha = 0.4f),
                                NexaBg.copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            // Neon glowing typography badge top-left
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 20.dp, top = 18.dp)
                    .rotate(-6f)
            ) {
                Text(
                    text = "Hi,",
                    color = NexaPink,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "I'm NEXA ♡",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Neon glowing badge top-right
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 20.dp, top = 18.dp)
                    .rotate(6f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Always",
                    color = NexaPurple,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Here For You ♡",
                    color = NexaPink,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Active Persona Mode Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF141226))
                .border(1.dp, NexaPink.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .clickable { viewModel.switchTab(AppTab.MODES) }
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .testTag("home_active_mode_pill")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = activeMode.emoji,
                    fontSize = 12.sp
                )
                Text(
                    text = "Mode: ${activeMode.title}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "• Change",
                    color = NexaPink,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Active Offline Mode Alert Banner (if offline or forced offline)
        if (isOfflineModeActive) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable { viewModel.switchTab(AppTab.OFFLINE) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "⚡", fontSize = 16.sp)
                        Column {
                            Text(
                                text = "OFFLINE MODE ACTIVE (ডেটা ছাড়া চালু)",
                                color = Color(0xFFFBBF24),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Torch, Lock, Calls, Math & Notes work 100% locally.",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.sp
                            )
                        }
                    }
                    Text(
                        text = "Open Hub →",
                        color = Color(0xFFFBBF24),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Central Glowing Mic Orb & Voice Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 0.dp)
        ) {
            MicOrbButton(
                isListening = isListening,
                onClick = { viewModel.toggleVoiceListening() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            val statusText = when {
                isListening && partialTranscript.isNotBlank() -> "\"$partialTranscript\""
                isListening -> "Listening to you..."
                isSpeaking -> "NEXA is speaking..."
                else -> "Tap microphone or enter query below"
            }

            Text(
                text = statusText,
                color = if (isListening) NexaPink else NexaTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Real-time Soundwave Equalizer (FEATURE 5)
            SoundwaveVisualizer(
                isActive = isListening || isSpeaking,
                rmsLevel = rmsLevel
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Active Voice Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(NexaPurple.copy(alpha = 0.15f))
                    .border(1.dp, NexaPurple.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .clickable { viewModel.switchTab(AppTab.CONNECTIONS) }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .testTag("home_active_voice_pill")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "🎙️", fontSize = 11.sp)
                    Text(
                        text = "${activeVoice.name} (${activeVoice.accent})",
                        color = Color(0xFFDDD6FE),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "VOICES ➔",
                        color = NexaPink,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // LIVE AI RESPONSE CARD ON HOME SCREEN
        NexaCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home_live_response_card"),
            borderColor = NexaPurple.copy(alpha = 0.5f),
            backgroundColor = Color(0xFF15132B)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(listOf(NexaPurple, NexaPink))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "N",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = "NEXA • ${activeMode.title} ${activeMode.emoji}",
                            color = NexaPink,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.speakWithActiveVoice(latestResponse.text)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Speak",
                                tint = NexaPurple,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.switchTab(AppTab.CHAT) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "View Chat",
                                tint = NexaTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                if (isProcessing) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            color = NexaPurple,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "NEXA is formulating response...",
                            color = NexaTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Text(
                        text = latestResponse.text,
                        color = NexaTextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Quick Query Suggestion Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickQuestions) { question ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(NexaPurple.copy(alpha = 0.15f))
                        .border(1.dp, NexaPurple.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .clickable {
                            viewModel.sendQuery(question, isFromVoice = true)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = question,
                        color = Color(0xFFDDD6FE),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Text Query Input Bar
        NexaCard(
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NexaPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Sparkle",
                        tint = NexaPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }

                TextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            text = "Ask me anything in Bengali, Hindi, or English...",
                            color = NexaTextSecondary,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_query_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (textInput.isNotBlank()) {
                                viewModel.sendQuery(textInput, isFromVoice = true)
                                textInput = ""
                            }
                        }
                    )
                )

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendQuery(textInput, isFromVoice = true)
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(NexaPurple, Color(0xFF4F46E5))
                            )
                        )
                        .testTag("home_query_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Bottom Quick Feature Cards (Chat, Modes, Create Image, Write)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Chat Card
                NexaCard(
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.switchTab(AppTab.CHAT) }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NexaPurple.copy(alpha = 0.2f))
                                .border(1.dp, NexaPurple.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Message,
                                contentDescription = "Chat",
                                tint = NexaPurple,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Chat",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Instant answers",
                            color = NexaTextSecondary,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Modes Card
                NexaCard(
                    modifier = Modifier.weight(1f),
                    borderColor = NexaPink.copy(alpha = 0.5f),
                    onClick = { viewModel.switchTab(AppTab.MODES) }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NexaPink.copy(alpha = 0.2f))
                                .border(1.dp, NexaPink.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "AI Modes",
                                tint = NexaPink,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "AI Modes",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "6 Personas",
                            color = NexaPink,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Create Image Card
                NexaCard(
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.switchTab(AppTab.CREATE_IMAGE) }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NexaPurple.copy(alpha = 0.2f))
                                .border(1.dp, NexaPurple.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ImageIcon,
                                contentDescription = "Create Image",
                                tint = NexaPurple,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Create Image",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gemini AI Art",
                            color = NexaTextSecondary,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Write Card
                NexaCard(
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.switchTab(AppTab.WRITE) }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NexaPurple.copy(alpha = 0.2f))
                                .border(1.dp, NexaPurple.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Write",
                                tint = NexaPurple,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Write",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Notes & Code",
                            color = NexaTextSecondary,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Offline Mode Hub Card
            NexaCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_offline_banner"),
                borderColor = Color(0xFFF59E0B).copy(alpha = 0.45f),
                onClick = { viewModel.switchTab(AppTab.OFFLINE) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "⚡", fontSize = 18.sp)
                        }
                        Column {
                            Text(
                                text = "Offline Mode Hub (অফলাইন মোড)",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Work without data: Torch, Lock, Calls, Math & Local Notes",
                                color = NexaTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF59E0B).copy(alpha = 0.25f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isOfflineModeActive) "ACTIVE" else "ZERO DATA",
                            color = Color(0xFFFBBF24),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // App Plugins & Automation Hub Banner
            NexaCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_plugins_banner"),
                borderColor = NexaPurple.copy(alpha = 0.45f),
                onClick = { viewModel.switchTab(AppTab.PLUGINS) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NexaPurple.copy(alpha = 0.2f))
                                .border(1.dp, NexaPurple.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Extension,
                                contentDescription = "App Plugins",
                                tint = NexaPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "App Plugins & Automation Hub",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Connect Facebook, Instagram, YouTube, WhatsApp & more",
                                color = NexaTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Text(
                        text = "OPEN ➔",
                        color = NexaPink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Autonomous Voice & Direct Actions Card
            NexaCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_autonomous_commands_card"),
                borderColor = NexaPink.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NexaPink.copy(alpha = 0.2f))
                                .border(1.dp, NexaPink.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Autonomous Actions",
                                tint = NexaPink,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Autonomous Voice & AI Actions",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Speak or tap to auto-post to Facebook/Instagram/YouTube, like & control device",
                                color = NexaTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val quickVoiceCommands = listOf(
                        "Facebook e post koro",
                        "Instagram e post koro",
                        "YouTube e post koro",
                        "Torch ON",
                        "Auto Like screen",
                        "Auto Follow",
                        "New Google Sheet",
                        "YouTube search"
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickVoiceCommands) { cmd ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(NexaPurple.copy(alpha = 0.15f))
                                    .border(1.dp, NexaPurple.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                    .clickable { viewModel.sendQuery(cmd, isFromVoice = false) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "⚡ $cmd",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
