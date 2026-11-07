package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppTab
import com.example.model.ChatMessage
import com.example.ui.NexaViewModel
import com.example.ui.components.NexaCard
import com.example.ui.theme.NexaCard
import com.example.ui.theme.NexaPink
import com.example.ui.theme.NexaPurple
import com.example.ui.theme.NexaTextPrimary
import com.example.ui.theme.NexaTextSecondary

@Composable
fun ChatScreen(
    viewModel: NexaViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.switchTab(AppTab.HOME)
    }

    val chatMessages by viewModel.chatMessages.collectAsState()
    val isProcessing by viewModel.isQueryProcessing.collectAsState()
    val activeMode by viewModel.modeFlow.collectAsState()
    val isOfflineModeActive by viewModel.isOfflineModeActive.collectAsState()
    val listState = rememberLazyListState()

    var inputMsg by remember { mutableStateOf("") }

    val quickChatSuggestions = remember(activeMode) {
        when (activeMode) {
            com.example.model.NexaMode.GIRLFRIEND -> listOf(
                "Kemon acho shona? ♡",
                "Tumi ki amake bhalobasho?",
                "Tell me something romantic",
                "Tomar kotha mone porche ♡",
                "Sing me a love song"
            )
            com.example.model.NexaMode.DEVELOPER -> listOf(
                "Explain Kotlin Coroutines",
                "How to optimize Compose recomposition?",
                "Write a Python FastAPI example",
                "Debug NullPointerException in Android",
                "Explain Clean Architecture"
            )
            com.example.model.NexaMode.TEACHER -> listOf(
                "Explain Quantum Physics simply",
                "Give me a study plan for exams",
                "Teach me Bengali grammar",
                "Explain photosynthesis with examples",
                "Quiz me on world history"
            )
            com.example.model.NexaMode.FRIEND -> listOf(
                "Bhai, ki obostha?",
                "Tell me a hilarious joke!",
                "Recommend a great sci-fi movie",
                "Give me life advice",
                "Cholo adda dei"
            )
            com.example.model.NexaMode.NATURAL -> listOf(
                "Who are you?",
                "What can you do?",
                "Tell me today's news summary",
                "Kemon acho NEXA?",
                "Help me write an email"
            )
            com.example.model.NexaMode.CUSTOM -> listOf(
                "Hello my custom AI!",
                "Act according to my custom prompt",
                "What is your role?",
                "Assist me today",
                "Kemon acho?"
            )
        }
    }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Offline Mode Notice Strip
        if (isOfflineModeActive) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .clickable { viewModel.switchTab(AppTab.OFFLINE) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⚡ Offline Mode: Operating without internet/data",
                        color = Color(0xFFFBBF24),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tools →",
                        color = Color(0xFFFBBF24),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Active Mode banner / switch pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NexaPurple.copy(alpha = 0.15f))
                .border(1.dp, NexaPurple.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = activeMode.emoji,
                    fontSize = 16.sp
                )
                Column {
                    Text(
                        text = "Mode: ${activeMode.title}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = activeMode.description,
                        color = NexaTextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(NexaPurple.copy(alpha = 0.8f), NexaPink.copy(alpha = 0.8f))
                        )
                    )
                    .border(1.dp, NexaPink.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .clickable { viewModel.switchTab(AppTab.MODES) }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .testTag("switch_mode_button")
            ) {
                Text(
                    text = "Change",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat messages scroll list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatMessages, key = { it.id }) { msg ->
                ChatMessageItem(
                    message = msg,
                    onSpeak = {
                        viewModel.speakWithActiveVoice(msg.text)
                    }
                )
            }

            if (isProcessing) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            color = NexaPurple,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "NEXA is formulating a response...",
                            color = NexaTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Suggestion Chips
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            items(quickChatSuggestions) { suggestion ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(NexaPurple.copy(alpha = 0.18f))
                        .border(1.dp, NexaPurple.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                        .clickable {
                            viewModel.sendQuery(suggestion, isFromVoice = true)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = suggestion,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Chat Input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Voice Mic shortcut button
            IconButton(
                onClick = { viewModel.toggleVoiceListening() },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(NexaPurple.copy(alpha = 0.2f))
                    .border(1.dp, NexaPurple.copy(alpha = 0.5f), CircleShape)
                    .testTag("chat_voice_mic_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Input",
                    tint = NexaPurple,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Input pill
            NexaCard(
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier.weight(1f)
            ) {
                TextField(
                    value = inputMsg,
                    onValueChange = { inputMsg = it },
                    placeholder = {
                        Text(
                            text = "Message NEXA (${activeMode.title})...",
                            color = NexaTextSecondary,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chat_text_input"),
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
                            if (inputMsg.isNotBlank()) {
                                viewModel.sendQuery(inputMsg, isFromVoice = true)
                                inputMsg = ""
                            }
                        }
                    )
                )
            }

            // Send button
            IconButton(
                onClick = {
                    if (inputMsg.isNotBlank()) {
                        viewModel.sendQuery(inputMsg, isFromVoice = true)
                        inputMsg = ""
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(NexaPurple, NexaPink)
                        )
                    )
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Message",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onSpeak: () -> Unit
) {
    if (message.isUser) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                NexaPurple.copy(alpha = 0.5f),
                                NexaPink.copy(alpha = 0.45f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        NexaPurple.copy(alpha = 0.6f),
                        RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text = message.text,
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = message.formattedTime,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            // NEXA avatar badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(NexaPurple, NexaPink)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "N",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.widthIn(max = 280.dp)) {
                NexaCard(
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
                    backgroundColor = NexaCard
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = message.text,
                            color = NexaTextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = message.formattedTime,
                                color = NexaTextSecondary,
                                fontSize = 9.sp
                            )
                            IconButton(
                                onClick = onSpeak,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Speak Aloud",
                                    tint = NexaPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
