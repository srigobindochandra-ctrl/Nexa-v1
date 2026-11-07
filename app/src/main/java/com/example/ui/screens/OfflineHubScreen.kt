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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NexaOfflineEngine
import com.example.model.AppTab
import com.example.ui.NexaViewModel
import com.example.ui.components.NexaCard
import com.example.ui.theme.NexaBg
import com.example.ui.theme.NexaCard
import com.example.ui.theme.NexaGlow
import com.example.ui.theme.NexaPink
import com.example.ui.theme.NexaPurple
import com.example.ui.theme.NexaTextPrimary
import com.example.ui.theme.NexaTextSecondary

@Composable
fun OfflineHubScreen(
    viewModel: NexaViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.switchTab(AppTab.HOME)
    }

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val isOnline by viewModel.isOnline.collectAsState()
    val isForcedOffline by viewModel.isOfflineForced.collectAsState()
    val isOfflineModeActive by viewModel.isOfflineModeActive.collectAsState()
    val torchState by viewModel.torchState.collectAsState()
    val offlineNotes by viewModel.offlineNotes.collectAsState()
    val activeLang by viewModel.languageFlow.collectAsState()
    val activeMode by viewModel.modeFlow.collectAsState()

    val batteryInfo = remember { viewModel.automationManager.getBatteryInfo() }
    val storageInfo = remember { viewModel.automationManager.getAvailableStorage() }

    var mathInput by remember { mutableStateOf("") }
    var mathResultText by remember { mutableStateOf<String?>(null) }

    var newNoteTitle by remember { mutableStateOf("") }
    var newNoteContent by remember { mutableStateOf("") }
    var isCreatingNote by remember { mutableStateOf(false) }

    val offlineVoiceCommands = listOf(
        "টর্চ জ্বালাও 🔦",
        "স্ক্রিন লক করো 🔒",
        "ব্যাটারি কত পার্সেন্ট 🔋",
        "হিসাব করো ৫০০ * ২৫ 🧮",
        "সময় কত 🕒",
        "ক্যামেরা খোলো 📸",
        "মিউট করো 🔇",
        "উক্তি শোনাও ✨"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NexaBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { viewModel.switchTab(AppTab.HOME) },
                    modifier = Modifier.testTag("offline_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NexaTextPrimary
                    )
                }
                Column {
                    Text(
                        text = "OFFLINE MODE",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Zero-Data Automation & Tools",
                        color = NexaTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Mode Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isOfflineModeActive) Color(0xFFF59E0B).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f))
                    .border(1.dp, if (isOfflineModeActive) Color(0xFFF59E0B) else Color(0xFF10B981), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isOfflineModeActive) "⚡ OFFLINE" else "🟢 ONLINE",
                    color = if (isOfflineModeActive) Color(0xFFFBBF24) else Color(0xFF34D399),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Live Status & Forced Toggle Card
        NexaCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = if (isOfflineModeActive) NexaPink.copy(alpha = 0.6f) else NexaPurple.copy(alpha = 0.4f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isForcedOffline) "Force Offline Active (অফলাইন মোড)" else if (!isOnline) "Network Disconnected (ডেটা ছাড়া কাজ চলছে)" else "Auto Network Mode",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isOfflineModeActive) "NEXA handles all voice, torch, lock, calls, notes & math locally without internet." else "Connected to internet. Toggle switch below to force zero-data offline mode.",
                            color = NexaTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = isForcedOffline,
                        onCheckedChange = { viewModel.toggleOfflineModeForced() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NexaPink,
                            checkedTrackColor = NexaPurple.copy(alpha = 0.5f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier.testTag("force_offline_switch")
                    )
                }

                // Telemetry info chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Battery
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔋 ${batteryInfo.first}% ${if (batteryInfo.second) "⚡" else ""}",
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Storage
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💾 $storageInfo",
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Native TTS Engine
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🗣️ On-Device TTS",
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Quick Suggestion Chips
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "TRY OFFLINE COMMANDS (মুখে বা ট্যাপ করে বলুন)",
                color = NexaTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(offlineVoiceCommands) { cmd ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(NexaPurple.copy(alpha = 0.15f))
                            .border(1.dp, NexaPurple.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.sendQuery(cmd, isFromVoice = true)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cmd,
                            color = Color(0xFFDDD6FE),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Hardware Controls Grid
        Text(
            text = "HARDWARE & DEVICE AUTOMATION",
            color = NexaTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Flashlight
            NexaCard(
                modifier = Modifier.weight(1f),
                borderColor = if (torchState) NexaPink else NexaPurple.copy(alpha = 0.35f),
                onClick = { viewModel.automationManager.toggleTorch() }
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (torchState) NexaPink.copy(alpha = 0.3f) else NexaPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (torchState) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                            contentDescription = "Flashlight",
                            tint = if (torchState) NexaPink else NexaPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (torchState) "Torch ON" else "Torch OFF",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "100% Offline",
                        color = NexaTextSecondary,
                        fontSize = 9.sp
                    )
                }
            }

            // Screen Lock
            NexaCard(
                modifier = Modifier.weight(1f),
                onClick = { viewModel.lockScreenVoiceAction() }
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NexaPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Screen",
                            tint = NexaPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Lock Screen",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Voice Action",
                        color = NexaTextSecondary,
                        fontSize = 9.sp
                    )
                }
            }

            // Phone Dialer / Call
            NexaCard(
                modifier = Modifier.weight(1f),
                onClick = { viewModel.automationManager.makePhoneCall("") }
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Dialer",
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Phone Call",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Direct SOS",
                        color = NexaTextSecondary,
                        fontSize = 9.sp
                    )
                }
            }
        }

        // Secondary Hardware Action Row (Volume & Camera)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Volume Controls Bar
            NexaCard(
                modifier = Modifier.weight(1.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.automationManager.setDeviceVolume("mute") },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeMute,
                            contentDescription = "Mute",
                            tint = NexaPink,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.automationManager.setDeviceVolume("down") },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeDown,
                            contentDescription = "Volume Down",
                            tint = NexaPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.automationManager.setDeviceVolume("up") },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Volume Up",
                            tint = NexaPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.automationManager.setDeviceVolume("max") },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Text(
                            text = "MAX",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Quick App Launchers
            NexaCard(
                modifier = Modifier.weight(1f),
                onClick = { viewModel.automationManager.openSystemTool("camera") }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        tint = NexaGlow,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Camera",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Offline",
                            color = NexaTextSecondary,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        // On-Device Math Evaluator Section
        NexaCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "Calculator",
                        tint = NexaPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Instant Offline Math & Unit Calculator",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = mathInput,
                        onValueChange = {
                            mathInput = it
                            if (it.isNotBlank()) {
                                val res = NexaOfflineEngine.processOffline(
                                    query = it,
                                    language = activeLang,
                                    mode = activeMode,
                                    batteryInfo = batteryInfo,
                                    storageInfo = storageInfo
                                )
                                mathResultText = res.replyText
                            } else {
                                mathResultText = null
                            }
                        },
                        placeholder = {
                            Text(
                                "e.g. 500 * 25, 20% of 1500, 10 km to miles",
                                color = NexaTextSecondary,
                                fontSize = 11.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("offline_math_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NexaPurple,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }

                if (mathResultText != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(NexaPurple.copy(alpha = 0.2f))
                            .border(1.dp, NexaPurple.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = mathResultText!!,
                            color = Color(0xFFE0E7FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Offline Quick Notes & Scratchpad
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OFFLINE NOTES & SCRATCHPAD (${offlineNotes.size})",
                    color = NexaTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Button(
                    onClick = { isCreatingNote = !isCreatingNote },
                    colors = ButtonDefaults.buttonColors(containerColor = NexaPurple.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(
                        imageVector = if (isCreatingNote) Icons.Default.Delete else Icons.Default.Add,
                        contentDescription = "New Note",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCreatingNote) "Cancel" else "Add Note",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }

            if (isCreatingNote) {
                NexaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newNoteTitle,
                            onValueChange = { newNoteTitle = it },
                            placeholder = { Text("Note Title (e.g. Grocery List, Idea)", color = NexaTextSecondary, fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NexaPink,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = newNoteContent,
                            onValueChange = { newNoteContent = it },
                            placeholder = { Text("Write your thoughts or reminders here. Persisted on device locally without data...", color = NexaTextSecondary, fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NexaPink,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Button(
                            onClick = {
                                if (newNoteContent.isNotBlank() || newNoteTitle.isNotBlank()) {
                                    viewModel.addOfflineNote(newNoteTitle, newNoteContent)
                                    newNoteTitle = ""
                                    newNoteContent = ""
                                    isCreatingNote = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NexaPink),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Save Locally 💾", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            if (offlineNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.NoteAdd,
                            contentDescription = "Empty Notes",
                            tint = NexaTextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No offline notes yet.",
                            color = NexaTextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Create quick notes above or say: \"নোট: ...\" in voice chat!",
                            color = NexaPurple,
                            fontSize = 10.sp
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    offlineNotes.forEach { note ->
                        NexaCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = Color.White.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = note.title,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = note.content,
                                        color = NexaTextPrimary,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = note.formattedDate,
                                        color = NexaTextSecondary,
                                        fontSize = 9.sp
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.deleteOfflineNote(note.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete note",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
