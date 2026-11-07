package com.example.ui.screens

import android.content.Context
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppTab
import com.example.ui.NexaViewModel
import com.example.ui.components.NexaCard
import com.example.ui.theme.NexaPink
import com.example.ui.theme.NexaPurple
import com.example.ui.theme.NexaTextPrimary
import com.example.ui.theme.NexaTextSecondary

import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lock

data class QuickActionItem(
    val title: String,
    val subtitle: String,
    val promptText: String,
    val intentUrl: String? = null,
    val icon: ImageVector,
    val customTab: AppTab? = null
)

@Composable
fun QuickActionsScreen(
    viewModel: NexaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler {
        viewModel.switchTab(AppTab.HOME)
    }

    val items = listOf(
        QuickActionItem(
            title = "Offline Mode Hub ⚡",
            subtitle = "Zero-Data Automation & Notes",
            promptText = "Offline mode hub",
            icon = Icons.Default.Bolt,
            customTab = AppTab.OFFLINE
        ),
        QuickActionItem(
            title = "App Plugins & Hub",
            subtitle = "Connect & Automate Apps",
            promptText = "Open Plugins Page",
            icon = Icons.Default.Extension,
            customTab = AppTab.PLUGINS
        ),
        QuickActionItem(
            title = "AI Modes & Personas",
            subtitle = "Girlfriend, Dev, Teacher...",
            promptText = "Switch AI persona",
            icon = Icons.Default.Psychology,
            customTab = AppTab.MODES
        ),
        QuickActionItem(
            title = "Voice Screen Lock",
            subtitle = "\"Screen lock koro 🔒\"",
            promptText = "Screen lock koro",
            icon = Icons.Default.Lock
        ),
        QuickActionItem(
            title = "Screen-On Auto Wake",
            subtitle = "Active in Background ⚡",
            promptText = "Background service on",
            icon = Icons.Default.Bolt,
            customTab = AppTab.CONNECTIONS
        ),
        QuickActionItem(
            title = "Torch Light Toggle",
            subtitle = "\"Torch on / off\"",
            promptText = "Torch on",
            icon = Icons.Default.FlashlightOn
        ),
        QuickActionItem(
            title = "Open YouTube / App",
            subtitle = "\"YouTube kholo\"",
            promptText = "Open YouTube to play trending videos",
            intentUrl = "https://youtube.com",
            icon = Icons.Default.PlayArrow
        ),
        QuickActionItem(
            title = "Call someone",
            subtitle = "\"Mummy ko call karo\"",
            promptText = "Call my contact",
            intentUrl = "tel:1234567890",
            icon = Icons.Default.Call
        ),
        QuickActionItem(
            title = "Send a Message",
            subtitle = "\"WhatsApp message send karo\"",
            promptText = "Send a quick message via messaging app",
            intentUrl = "sms:?body=Hi! Sent from NEXA AI",
            icon = Icons.AutoMirrored.Filled.Chat
        ),
        QuickActionItem(
            title = "Build a website",
            subtitle = "\"ek cafe ki website banao\"",
            promptText = "Generate HTML and CSS code for a futuristic cyber cafe website",
            icon = Icons.Default.Language,
            customTab = AppTab.WRITE
        ),
        QuickActionItem(
            title = "Sketch / Art",
            subtitle = "\"Drawing banao\"",
            promptText = "A futuristic cybernetic anime character with neon wings",
            icon = Icons.Default.Brush,
            customTab = AppTab.CREATE_IMAGE
        ),
        QuickActionItem(
            title = "Talk to NEXA",
            subtitle = "Start Voice Chat",
            promptText = "Hello Nexa, what's new today?",
            icon = Icons.Default.Mic
        )
    )

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
            Spacer(modifier = Modifier.size(12.dp))
            Column {
                Text(
                    text = "Quick Actions (Voice & Text)",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap any action or say it directly in Bangla, Hindi or English.",
                    color = NexaTextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items) { item ->
                NexaCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (item.customTab == AppTab.MODES) NexaPink.copy(alpha = 0.5f) else NexaPurple.copy(alpha = 0.25f),
                    onClick = {
                        if (item.customTab != null) {
                            viewModel.switchTab(item.customTab)
                        } else if (item.icon == Icons.Default.Mic) {
                            viewModel.switchTab(AppTab.HOME)
                            viewModel.toggleVoiceListening()
                        } else {
                            viewModel.executeQuickAction(
                                actionName = item.title,
                                promptText = item.promptText,
                                actionUrl = item.intentUrl,
                                context = context
                            )
                        }
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (item.customTab == AppTab.MODES) NexaPink.copy(alpha = 0.25f)
                                    else NexaPurple.copy(alpha = 0.25f)
                                )
                                .border(
                                    1.dp,
                                    if (item.customTab == AppTab.MODES) NexaPink.copy(alpha = 0.6f)
                                    else NexaPurple.copy(alpha = 0.45f),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (item.customTab == AppTab.MODES) NexaPink else NexaPurple,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.subtitle,
                                color = NexaTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
