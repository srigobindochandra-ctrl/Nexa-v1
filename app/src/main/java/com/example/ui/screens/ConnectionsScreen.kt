package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppTab
import com.example.model.NEXA_VOICES
import com.example.model.NexaVoice
import com.example.ui.NexaViewModel
import com.example.ui.components.NexaCard
import com.example.ui.theme.NexaBg
import com.example.ui.theme.NexaPink
import com.example.ui.theme.NexaPurple
import com.example.ui.theme.NexaSuccess
import com.example.ui.theme.NexaTextPrimary
import com.example.ui.theme.NexaTextSecondary

@Composable
fun ConnectionsScreen(
    viewModel: NexaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    BackHandler {
        viewModel.switchTab(AppTab.HOME)
    }

    val savedGroqKey by viewModel.repository.groqKeyFlow.collectAsState()
    val savedGeminiKey by viewModel.repository.geminiKeyFlow.collectAsState()
    val savedElevenLabsKey by viewModel.elevenLabsApiKeyFlow.collectAsState()
    val selectedVoiceId by viewModel.selectedVoiceIdFlow.collectAsState()
    val isBgRunning by viewModel.isBackgroundServiceRunning.collectAsState()
    val autoWakeOnScreenOn by viewModel.autoWakeOnScreenOnFlow.collectAsState()
    val autoLaunchOnWake by viewModel.autoLaunchOnScreenWakeFlow.collectAsState()
    val screenWakeCount by viewModel.screenWakeCount.collectAsState()
    val lastWakeEvent by viewModel.lastScreenWakeEvent.collectAsState()
    val isForcedOffline by viewModel.isOfflineForced.collectAsState()
    val isOfflineModeActive by viewModel.isOfflineModeActive.collectAsState()

    var groqInput by remember { mutableStateOf(savedGroqKey) }
    var geminiInput by remember { mutableStateOf(savedGeminiKey) }
    var elevenLabsInput by remember { mutableStateOf(savedElevenLabsKey) }

    var showGroqKey by remember { mutableStateOf(false) }
    var showGeminiKey by remember { mutableStateOf(false) }
    var showElevenLabsKey by remember { mutableStateOf(false) }

    val hasGroq = savedGroqKey.isNotBlank()
    val hasGemini = savedGeminiKey.isNotBlank()
    val hasElevenLabs = savedElevenLabsKey.isNotBlank()

    // 7 System Permissions State Checks
    var permissionRefreshTrigger by remember { mutableStateOf(0) }
    val isMicGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    val isCameraGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    val isCallGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
    val isContactsGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    val isLocationGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val isOverlayGranted = viewModel.canDrawOverlays()
    val isAccessibilityGranted = com.example.accessibility.NexaAccessibilityService.isServiceRunning()
    val isNotificationGranted = viewModel.isNotificationListenerEnabled

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        permissionRefreshTrigger++
        viewModel.showToast("Permission status refreshed! 🛡️")
    }

    // Sync state if saved keys change
    LaunchedEffect(savedGroqKey) {
        groqInput = savedGroqKey
    }
    LaunchedEffect(savedGeminiKey) {
        geminiInput = savedGeminiKey
    }
    LaunchedEffect(savedElevenLabsKey) {
        elevenLabsInput = savedElevenLabsKey
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
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
            Spacer(modifier = Modifier.size(12.dp))
            Column {
                Text(
                    text = "API Keys & Connections",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Add your own Groq & Gemini API keys below",
                    color = NexaTextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Info Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NexaPurple.copy(alpha = 0.12f))
                .border(1.dp, NexaPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "Paste your personal API keys into the boxes below and tap Save. Both keys are optional—NEXA also has a built-in multi-mode conversational engine that always responds!",
                color = NexaTextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }

        // Offline Mode Settings Card
        NexaCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = Color(0xFFF59E0B).copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚡", fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = "Offline Mode (ডেটা ছাড়া অফলাইন মোড)",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isOfflineModeActive) "Active: Torch, lock, calls, math & notes run with 0% data." else "Run automations, calculations & local notes without data.",
                            color = NexaTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Switch(
                    checked = isForcedOffline,
                    onCheckedChange = { viewModel.toggleOfflineModeForced() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NexaPink,
                        checkedTrackColor = NexaPurple.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("settings_offline_switch")
                )
            }
        }

        // 1. Groq API Key Box
        NexaCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Groq",
                            tint = Color(0xFFF97316),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Groq API Key (Ultra-Fast Chat)",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (hasGroq) NexaSuccess.copy(alpha = 0.2f)
                                else Color.White.copy(alpha = 0.1f)
                            )
                            .border(
                                1.dp,
                                if (hasGroq) NexaSuccess.copy(alpha = 0.5f)
                                else Color.White.copy(alpha = 0.15f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (hasGroq) "Connected ✓" else "Not Connected",
                            color = if (hasGroq) NexaSuccess else NexaTextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Link to get key
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://console.groq.com/keys"))
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        text = "Get free Groq key: console.groq.com/keys",
                        color = Color(0xFFFB923C),
                        fontSize = 11.sp
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open Link",
                        tint = Color(0xFFFB923C),
                        modifier = Modifier.size(13.dp)
                    )
                }

                // Text field with Paste & Show/Hide actions
                OutlinedTextField(
                    value = groqInput,
                    onValueChange = { groqInput = it },
                    placeholder = {
                        Text("Paste Groq key (gsk_...)", color = NexaTextSecondary, fontSize = 12.sp)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("groq_api_key_input"),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (showGroqKey) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Quick Paste Button
                            IconButton(
                                onClick = {
                                    val clip = clipboardManager.getText()?.text ?: ""
                                    if (clip.isNotBlank()) groqInput = clip.trim()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Paste from Clipboard",
                                    tint = Color(0xFFFB923C),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            // Show/Hide toggle
                            IconButton(
                                onClick = { showGroqKey = !showGroqKey },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (showGroqKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle visibility",
                                    tint = NexaTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NexaBg,
                        unfocusedContainerColor = NexaBg,
                        focusedBorderColor = Color(0xFFF97316),
                        unfocusedBorderColor = Color(0xFFF97316).copy(alpha = 0.4f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.saveGroqKey(groqInput) },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("save_groq_key_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Save Groq Key", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            groqInput = ""
                            viewModel.clearGroqKey()
                        },
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("clear_groq_key_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Clear", color = Color(0xFFFCA5A5), fontSize = 12.sp)
                    }
                }
            }
        }

        // 2. Gemini API Key Box (Completely user-provided, starts empty)
        NexaCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini",
                            tint = NexaPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Gemini API Key (Voice & Image)",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Status Badge (Connected vs Not Connected)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (hasGemini) NexaSuccess.copy(alpha = 0.2f)
                                else Color.White.copy(alpha = 0.1f)
                            )
                            .border(
                                1.dp,
                                if (hasGemini) NexaSuccess.copy(alpha = 0.5f)
                                else Color.White.copy(alpha = 0.15f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (hasGemini) "Connected ✓" else "Not Connected",
                            color = if (hasGemini) NexaSuccess else NexaTextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Link to get key
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/app/apikey"))
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        text = "Get free Gemini key: aistudio.google.com",
                        color = NexaPurple,
                        fontSize = 11.sp
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open Link",
                        tint = NexaPurple,
                        modifier = Modifier.size(13.dp)
                    )
                }

                // Text field with Paste & Show/Hide actions
                OutlinedTextField(
                    value = geminiInput,
                    onValueChange = { geminiInput = it },
                    placeholder = {
                        Text("Paste Gemini key (AIzaSy...)", color = NexaTextSecondary, fontSize = 12.sp)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gemini_api_key_input"),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (showGeminiKey) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Quick Paste Button
                            IconButton(
                                onClick = {
                                    val clip = clipboardManager.getText()?.text ?: ""
                                    if (clip.isNotBlank()) geminiInput = clip.trim()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Paste from Clipboard",
                                    tint = NexaPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            // Show/Hide toggle
                            IconButton(
                                onClick = { showGeminiKey = !showGeminiKey },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (showGeminiKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle visibility",
                                    tint = NexaTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NexaBg,
                        unfocusedContainerColor = NexaBg,
                        focusedBorderColor = NexaPurple,
                        unfocusedBorderColor = NexaPurple.copy(alpha = 0.4f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.saveGeminiKey(geminiInput) },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("save_gemini_key_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NexaPurple),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Save Gemini Key", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            geminiInput = ""
                            viewModel.clearGeminiKey()
                        },
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("clear_gemini_key_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Clear", color = Color(0xFFFCA5A5), fontSize = 12.sp)
                    }
                }
            }
        }

        // ==========================================
        // 3. ELEVENLABS API KEY & STATUS
        // ==========================================
        NexaCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = if (hasElevenLabs) NexaPurple.copy(alpha = 0.6f) else NexaPurple.copy(alpha = 0.3f)
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "ElevenLabs",
                                tint = NexaPink,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "ElevenLabs API Key",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "High-fidelity AI voice synthesis",
                                color = NexaTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (hasElevenLabs) Color(0xFF10B981).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.06f))
                            .border(
                                1.dp,
                                if (hasElevenLabs) Color(0xFF10B981).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.15f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (hasElevenLabs) "Connected ✓" else "Native TTS Active",
                            color = if (hasElevenLabs) Color(0xFF10B981) else NexaTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Enter your ElevenLabs API key to generate hyper-realistic speech using the 8 custom voice IDs below. If left empty, NEXA uses the built-in neural TTS engine with customized pitch and pace.",
                    color = NexaTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://elevenlabs.io"))
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Get ElevenLabs API Key: https://elevenlabs.io",
                        color = NexaPink,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open ElevenLabs",
                        tint = NexaPink,
                        modifier = Modifier.size(12.dp)
                    )
                }

                OutlinedTextField(
                    value = elevenLabsInput,
                    onValueChange = { elevenLabsInput = it },
                    placeholder = {
                        Text("Paste ElevenLabs Key (xi-...)", color = NexaTextSecondary, fontSize = 12.sp)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("elevenlabs_api_key_input"),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (showElevenLabsKey) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val clip = clipboardManager.getText()?.text ?: ""
                                    if (clip.isNotBlank()) elevenLabsInput = clip.trim()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Paste from Clipboard",
                                    tint = NexaPink,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { showElevenLabsKey = !showElevenLabsKey },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (showElevenLabsKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle visibility",
                                    tint = NexaTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NexaBg,
                        unfocusedContainerColor = NexaBg,
                        focusedBorderColor = NexaPink,
                        unfocusedBorderColor = NexaPink.copy(alpha = 0.4f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.saveElevenLabsKey(elevenLabsInput) },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("save_elevenlabs_key_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NexaPink),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Save ElevenLabs Key", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = {
                            elevenLabsInput = ""
                            viewModel.clearElevenLabsKey()
                        },
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("clear_elevenlabs_key_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Clear", color = Color(0xFFFCA5A5), fontSize = 12.sp)
                    }
                }
            }
        }

        // ==========================================
        // 4. AI VOICES & NEURAL SPEECH ENGINE (8 VOICES)
        // ==========================================
        NexaCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = NexaPurple.copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NexaPurple.copy(alpha = 0.2f))
                                .border(1.dp, NexaPurple.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "Voices",
                                tint = NexaPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "AI Neural Voices (8 Voices)",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Select your active voice for speech output",
                                color = NexaTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(NexaPurple.copy(alpha = 0.15f))
                            .border(1.dp, NexaPurple.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "8 Voices Ready",
                            color = NexaPurple,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Tap any voice to select it as NEXA's speech synthesis persona. Tap '▶ Preview' to listen to each voice.",
                    color = NexaTextSecondary,
                    fontSize = 11.sp
                )

                // Render All 8 Voices
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    NEXA_VOICES.forEachIndexed { index, voice ->
                        val isSelected = selectedVoiceId.equals(voice.voiceId, ignoreCase = true)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) NexaPurple.copy(alpha = 0.18f)
                                    else Color(0xFF0F0F1E).copy(alpha = 0.7f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) NexaPurple else Color.White.copy(alpha = 0.08f),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    viewModel.setSelectedVoice(voice.voiceId)
                                }
                                .padding(12.dp)
                                .testTag("voice_item_${voice.voiceId}")
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) NexaPurple else Color.White.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = voice.name,
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color.White.copy(alpha = 0.08f))
                                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = voice.gender,
                                                        color = NexaTextSecondary,
                                                        fontSize = 9.sp
                                                    )
                                                }
                                            }
                                            Text(
                                                text = voice.accent,
                                                color = NexaPink,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    // Active badge or Preview Button
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = "ACTIVE ✓",
                                                    color = Color(0xFF10B981),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        // Preview Voice Button
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(NexaPurple.copy(alpha = 0.25f))
                                                .border(1.dp, NexaPurple.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                .clickable {
                                                    viewModel.previewVoice(voice)
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                .testTag("preview_voice_${voice.voiceId}")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Preview",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = "Preview",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Text(
                                    text = voice.description,
                                    color = NexaTextSecondary,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )

                                // Monospaced Voice ID with Copy Button
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF080812))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "ID: ${voice.voiceId}",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(voice.voiceId))
                                            viewModel.showToast("Voice ID copied: ${voice.voiceId}")
                                        },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Voice ID",
                                            tint = NexaPurple,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. SCREEN-ON AUTO WAKE & VOICE SCREEN LOCK SYSTEM
        // ==========================================
        NexaCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = Color(0xFF10B981).copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
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
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Screen Wake",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Screen-On Wake & Voice Lock",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Auto background trigger & screen locking",
                                color = NexaTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Background Service Active Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isBgRunning) Color(0xFF10B981).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.06f))
                            .border(
                                1.dp,
                                if (isBgRunning) Color(0xFF10B981).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.15f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isBgRunning) "Active in BG ✓" else "Standby",
                            color = if (isBgRunning) Color(0xFF10B981) else NexaTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Description
                Text(
                    text = "When the device screen turns ON, NEXA automatically enables in the background immediately and readies voice input. You can also lock your phone instantly by voice command.",
                    color = NexaTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                // Feature 1: Auto-Wake On Screen ON Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Enable NEXA on Screen-ON",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Screen wake automatically activates NEXA background engine",
                            color = NexaTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = autoWakeOnScreenOn,
                        onCheckedChange = { viewModel.setAutoWakeOnScreenOn(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }

                // Feature 2: Auto-Launch UI on Screen Wake Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Launch App on Screen Wake",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Brings NEXA to front immediately when screen is unlocked",
                            color = NexaTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = autoLaunchOnWake,
                        onCheckedChange = { viewModel.setAutoLaunchOnScreenWake(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NexaPurple,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }

                // Live Telemetry Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0A0A18))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Screen Wakes Detected: $screenWakeCount",
                        color = Color(0xFF86EFAC),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Last: $lastWakeEvent",
                        color = NexaTextSecondary,
                        fontSize = 10.sp
                    )
                }

                // Voice Screen Lock Controls
                Text(
                    text = "VOICE SCREEN LOCK",
                    color = NexaPurple,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                // Voice lock command chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("\"Lock screen\"", "\"স্ক্রিন লক করো\"", "\"फोन लॉक करो\"").forEach { cmd ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NexaPurple.copy(alpha = 0.2f))
                                .border(1.dp, NexaPurple.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = cmd,
                                color = Color(0xFFDDD6FE),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.lockScreenVoiceAction() },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("test_voice_lock_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Lock Screen Now 🔒", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Button(
                        onClick = { viewModel.toggleBackgroundService(context) },
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("toggle_bg_service_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBgRunning) Color.White.copy(alpha = 0.12f) else Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = if (isBgRunning) "Stop Service" else "Start Service",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Permission Setup Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.openAccessibilitySettings() },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("open_accessibility_settings_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NexaPurple.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text("Accessibility Settings", fontSize = 10.sp, color = Color.White)
                    }

                    Button(
                        onClick = { viewModel.requestDeviceAdmin() },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("request_device_admin_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NexaPink.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text("Device Admin Lock", fontSize = 10.sp, color = Color.White)
                    }
                }
            }
        }

        // ==========================================
        // 6. PERMISSIONS & SYSTEM ACCESS CENTER (7 PERMISSIONS)
        // ==========================================
        NexaCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = NexaPurple.copy(alpha = 0.6f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
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
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NexaPurple.copy(alpha = 0.2f))
                                .border(1.dp, NexaPurple.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Permissions",
                                tint = NexaPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "System Permissions Center",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "7 Core Hardware & System Privileges",
                                color = NexaTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    val totalGranted = listOf(
                        isMicGranted, isCameraGranted, isCallGranted,
                        isContactsGranted, isLocationGranted, isOverlayGranted,
                        isAccessibilityGranted, isNotificationGranted
                    ).count { it }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (totalGranted >= 5) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f))
                            .border(
                                1.dp,
                                if (totalGranted >= 5) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFFF59E0B).copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$totalGranted / 8 Active",
                            color = if (totalGranted >= 5) Color(0xFF10B981) else Color(0xFFF59E0B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "NEXA AI requires these 8 permissions to operate autonomous voice, hardware, message reading, screen wake, and device intelligence:",
                    color = NexaTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                // 1-Tap Grant All Runtime Permissions Button
                Button(
                    onClick = {
                        requestPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.CAMERA,
                                Manifest.permission.CALL_PHONE,
                                Manifest.permission.READ_CONTACTS,
                                Manifest.permission.WRITE_CONTACTS,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("grant_all_permissions_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NexaPurple),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Grant Standard Permissions ⚡", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // 1. Microphone
                PermissionRowItem(
                    name = "1. Microphone",
                    detail = "Voice commands, speech recognition & live equalizer",
                    icon = Icons.Default.Mic,
                    isGranted = isMicGranted,
                    onGrantClick = {
                        requestPermissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                    }
                )

                // 2. Display Over Other Apps (Overlay)
                PermissionRowItem(
                    name = "2. Display Over Apps",
                    detail = "Floating cosmic orb & popup wake over other apps",
                    icon = Icons.Default.Layers,
                    isGranted = isOverlayGranted,
                    actionLabel = "Open Settings",
                    onGrantClick = {
                        viewModel.openOverlaySettings()
                    }
                )

                // 3. Accessibility Service
                PermissionRowItem(
                    name = "3. Accessibility",
                    detail = "Screen auto-clicks, like/follow/post & voice screen lock",
                    icon = Icons.Default.Accessibility,
                    isGranted = isAccessibilityGranted,
                    actionLabel = "Enable Service",
                    onGrantClick = {
                        viewModel.openAccessibilitySettings()
                    }
                )

                // 4. Camera
                PermissionRowItem(
                    name = "4. Camera",
                    detail = "Torch / flashlight control & image scanning",
                    icon = Icons.Default.CameraAlt,
                    isGranted = isCameraGranted,
                    onGrantClick = {
                        requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                    }
                )

                // 5. Phone Call
                PermissionRowItem(
                    name = "5. Phone Call",
                    detail = "Autonomous phone dialing & voice-directed calls",
                    icon = Icons.Default.Call,
                    isGranted = isCallGranted,
                    onGrantClick = {
                        requestPermissionLauncher.launch(arrayOf(Manifest.permission.CALL_PHONE))
                    }
                )

                // 6. Contacts
                PermissionRowItem(
                    name = "6. Contacts",
                    detail = "Search phonebook to call contacts by name (\"Call Mummy\")",
                    icon = Icons.Default.Contacts,
                    isGranted = isContactsGranted,
                    onGrantClick = {
                        requestPermissionLauncher.launch(
                            arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
                        )
                    }
                )

                // 7. Location
                PermissionRowItem(
                    name = "7. Location",
                    detail = "GPS coordinates, local live search & city detection",
                    icon = Icons.Default.LocationOn,
                    isGranted = isLocationGranted,
                    onGrantClick = {
                        requestPermissionLauncher.launch(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                    }
                )

                // 8. Notification Access
                PermissionRowItem(
                    name = "8. Notification Access",
                    detail = "Read and reply to WhatsApp & Messenger messages autonomously",
                    icon = Icons.Default.Security,
                    isGranted = isNotificationGranted,
                    actionLabel = "Enable Access",
                    onGrantClick = {
                        viewModel.openNotificationListenerSettings()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun PermissionRowItem(
    name: String,
    detail: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isGranted: Boolean,
    actionLabel: String = "Grant",
    onGrantClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, if (isGranted) Color(0xFF10B981).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (isGranted) Color(0xFF10B981).copy(alpha = 0.2f) else NexaPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    tint = if (isGranted) Color(0xFF10B981) else NexaPurple,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = detail,
                    color = NexaTextSecondary,
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isGranted) Color(0xFF10B981).copy(alpha = 0.2f) else NexaPurple.copy(alpha = 0.25f))
                .border(
                    1.dp,
                    if (isGranted) Color(0xFF10B981).copy(alpha = 0.5f) else NexaPurple.copy(alpha = 0.5f),
                    RoundedCornerShape(8.dp)
                )
                .clickable { onGrantClick() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isGranted) "Granted ✓" else actionLabel,
                color = if (isGranted) Color(0xFF10B981) else Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
