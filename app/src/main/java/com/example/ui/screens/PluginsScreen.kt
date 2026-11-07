package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NexaRepository
import com.example.model.AppPlugin
import com.example.model.AppTab
import com.example.ui.NexaViewModel
import com.example.ui.components.NexaCard
import com.example.ui.theme.NexaBg
import com.example.ui.theme.NexaCard
import com.example.ui.theme.NexaPink
import com.example.ui.theme.NexaPurple
import com.example.ui.theme.NexaTextPrimary
import com.example.ui.theme.NexaTextSecondary

@Composable
fun PluginsScreen(
    viewModel: NexaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler {
        viewModel.switchTab(AppTab.HOME)
    }

    val plugins by viewModel.pluginsFlow.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var pluginToEdit by remember { mutableStateOf<AppPlugin?>(null) }
    var pluginToDelete by remember { mutableStateOf<AppPlugin?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "App Plugins & Hub",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Connect your apps with login credentials to automate tasks",
                        color = NexaTextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Add Plugin Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(NexaPurple, NexaPink)
                            )
                        )
                        .clickable { showAddDialog = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("btn_open_add_plugin"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Plugin",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Add Plugin",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Status Card / Summary
        item {
            NexaCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NexaPurple.copy(alpha = 0.4f)
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
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NexaPurple.copy(alpha = 0.2f))
                                .border(1.dp, NexaPurple.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Extension,
                                contentDescription = "Plugins",
                                tint = NexaPurple,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Connected App Integrations",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (plugins.isEmpty()) "No apps connected yet" else "${plugins.size} apps connected to NEXA AI",
                                color = NexaTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (plugins.isNotEmpty()) Color(0xFF10B981).copy(alpha = 0.15f)
                                else Color.White.copy(alpha = 0.05f)
                            )
                            .border(
                                1.dp,
                                if (plugins.isNotEmpty()) Color(0xFF10B981).copy(alpha = 0.5f)
                                else Color.White.copy(alpha = 0.12f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (plugins.isNotEmpty()) "${plugins.count { it.isConnected }} Active" else "Ready",
                            color = if (plugins.isNotEmpty()) Color(0xFF10B981) else NexaTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Empty State: Prompt user to add their apps
        if (plugins.isEmpty()) {
            item {
                NexaCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = NexaPink.copy(alpha = 0.35f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(NexaPurple.copy(alpha = 0.15f))
                                .border(1.dp, NexaPurple.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Extension,
                                contentDescription = "No Plugins",
                                tint = NexaPink,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = "No App Plugins Connected",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Search and connect your apps (Facebook, YouTube, Instagram, WhatsApp, Google Docs, etc.) along with your account login details (Name, Email/Username, Password). NEXA AI will use them to automate your posts, comments, calls, messages, and document tasks!",
                            color = NexaTextSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NexaPurple
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("btn_empty_add_plugin")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Search & Connect App",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            // List of User-Connected Plugins
            items(plugins, key = { it.id }) { plugin ->
                ConnectedPluginCard(
                    plugin = plugin,
                    context = context,
                    onToggle = { viewModel.togglePluginConnection(plugin.id) },
                    onEdit = { pluginToEdit = plugin },
                    onDelete = { pluginToDelete = plugin },
                    onExecuteAction = { action ->
                        viewModel.executePluginAction(plugin, action, context)
                    },
                    onQuickCommand = { cmd ->
                        viewModel.sendQuery(cmd, isFromVoice = false)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Add Plugin Dialog with Search Bar & Login Credentials Form
    if (showAddDialog) {
        AddPluginWithCredentialsDialog(
            onDismiss = { showAddDialog = false },
            onConnect = { newPlugin ->
                viewModel.addConnectedPlugin(newPlugin)
                showAddDialog = false
            }
        )
    }

    // Edit Credentials Dialog
    pluginToEdit?.let { plugin ->
        EditCredentialsDialog(
            plugin = plugin,
            onDismiss = { pluginToEdit = null },
            onSave = { name, email, pass ->
                viewModel.updatePluginCredentials(plugin.id, name, email, pass)
                pluginToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    pluginToDelete?.let { plugin ->
        AlertDialog(
            onDismissRequest = { pluginToDelete = null },
            containerColor = Color(0xFF141428),
            title = {
                Text(
                    text = "Disconnect ${plugin.name}?",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove ${plugin.name} from NEXA AI plugins? Saved credentials will be removed.",
                    color = NexaTextSecondary,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removePlugin(plugin.id)
                        pluginToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Disconnect & Remove", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { pluginToDelete = null }) {
                    Text("Cancel", color = NexaTextSecondary)
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConnectedPluginCard(
    plugin: AppPlugin,
    context: Context,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExecuteAction: (String) -> Unit,
    onQuickCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val borderColor = if (plugin.isConnected) NexaPurple.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f)

    NexaCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("plugin_card_${plugin.id}"),
        borderColor = borderColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Icon + Name + Category + Status + Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(getPluginColor(plugin.iconType).copy(alpha = 0.18f))
                            .border(1.dp, getPluginColor(plugin.iconType).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getPluginIcon(plugin.iconType),
                            contentDescription = plugin.name,
                            tint = getPluginColor(plugin.iconType),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = plugin.name,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = plugin.category,
                                    color = NexaTextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Text(
                            text = plugin.packageName,
                            color = NexaTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                // Switch Toggle & Delete icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = plugin.isConnected,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NexaPurple,
                            uncheckedThumbColor = NexaTextSecondary,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("toggle_${plugin.id}")
                    )

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove Plugin",
                            tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Account Credentials Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0C0C18))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "LOGIN ACCOUNT DETAILS",
                            color = NexaPurple,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        // Edit Credentials Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onEdit() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Credentials",
                                tint = NexaPink,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Edit",
                                color = NexaPink,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Account Name
                    if (plugin.accountName.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Account Name",
                                tint = NexaTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Name: ",
                                color = NexaTextSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = plugin.accountName,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Email / Username
                    if (plugin.accountEmail.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = NexaTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Email / ID: ",
                                    color = NexaTextSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = plugin.accountEmail,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Copy Email button
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    clipboard?.setPrimaryClip(ClipData.newPlainText("Account Email", plugin.accountEmail))
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Email",
                                    tint = NexaTextSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }

                    // Password
                    if (plugin.accountPassword.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Password",
                                    tint = NexaTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Password: ",
                                    color = NexaTextSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = if (passwordVisible) plugin.accountPassword else "••••••••••••",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { passwordVisible = !passwordVisible },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Password",
                                        tint = NexaTextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        clipboard?.setPrimaryClip(ClipData.newPlainText("Account Password", plugin.accountPassword))
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Password",
                                        tint = NexaTextSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Supported Actions
            if (plugin.supportedActions.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "AUTONOMOUS ACTIONS:",
                        color = NexaTextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        plugin.supportedActions.forEach { action ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (plugin.isConnected) NexaPurple.copy(alpha = 0.15f)
                                        else Color.White.copy(alpha = 0.04f)
                                    )
                                    .border(
                                        1.dp,
                                        if (plugin.isConnected) NexaPurple.copy(alpha = 0.35f)
                                        else Color.White.copy(alpha = 0.08f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable(enabled = plugin.isConnected) {
                                        onExecuteAction(action)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = action,
                                        color = if (plugin.isConnected) Color(0xFFDDD6FE) else NexaTextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (plugin.isConnected) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Run",
                                            tint = NexaPink,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Voice Command Runner
            if (plugin.quickCommand.isNotBlank() && plugin.isConnected) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                        .clickable { onQuickCommand(plugin.quickCommand) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "⚡ Voice Trigger:",
                            color = NexaPink,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "\"${plugin.quickCommand}\"",
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = "RUN ➔",
                        color = NexaPurple,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AddPluginWithCredentialsDialog(
    onDismiss: () -> Unit,
    onConnect: (AppPlugin) -> Unit
) {
    val catalog = remember { NexaRepository.getAvailableAppCatalog() }
    var searchAppQuery by remember { mutableStateOf("") }

    var selectedApp by remember { mutableStateOf<AppPlugin?>(null) }
    var customAppName by remember { mutableStateOf("") }
    var customPackageName by remember { mutableStateOf("") }
    var customCategory by remember { mutableStateOf("Social") }

    var accountName by remember { mutableStateOf("") }
    var accountEmail by remember { mutableStateOf("") }
    var accountPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val filteredCatalog = remember(searchAppQuery) {
        if (searchAppQuery.isBlank()) {
            catalog
        } else {
            catalog.filter {
                it.name.contains(searchAppQuery, ignoreCase = true) ||
                it.category.contains(searchAppQuery, ignoreCase = true) ||
                it.packageName.contains(searchAppQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF141428),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Extension,
                    contentDescription = null,
                    tint = NexaPurple,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add & Connect App Plugin",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search Bar for App Selection
                item {
                    Text(
                        text = "1. SEARCH & SELECT APP",
                        color = NexaPurple,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = searchAppQuery,
                        onValueChange = {
                            searchAppQuery = it
                            if (selectedApp != null && !selectedApp!!.name.equals(it, ignoreCase = true)) {
                                selectedApp = null
                            }
                            customAppName = it
                        },
                        placeholder = { Text("Search Facebook, YouTube, WhatsApp...", color = NexaTextSecondary, fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = NexaPurple,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        singleLine = true,
                        colors = customDialogTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_search_app")
                    )
                }

                // Quick Selectable App Pills
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filteredCatalog) { app ->
                            val isSelected = selectedApp?.id == app.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) NexaPurple.copy(alpha = 0.35f)
                                        else Color.White.copy(alpha = 0.05f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) NexaPurple else Color.White.copy(alpha = 0.12f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        selectedApp = app
                                        customAppName = app.name
                                        customPackageName = app.packageName
                                        customCategory = app.category
                                    }
                                    .padding(horizontal = 9.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = getPluginIcon(app.iconType),
                                        contentDescription = app.name,
                                        tint = getPluginColor(app.iconType),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = app.name,
                                        color = if (isSelected) Color.White else NexaTextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // Selected App Confirmation Badge
                item {
                    val appDisplayName = selectedApp?.name ?: customAppName
                    if (appDisplayName.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(NexaPurple.copy(alpha = 0.12f))
                                .border(1.dp, NexaPurple.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Selected: $appDisplayName (${selectedApp?.category ?: customCategory})",
                                color = Color(0xFFDDD6FE),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Account Credentials Section
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "2. ACCOUNT LOGIN DETAILS",
                        color = NexaPink,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Name Field
                item {
                    OutlinedTextField(
                        value = accountName,
                        onValueChange = { accountName = it },
                        label = { Text("Account Name / Profile Name") },
                        placeholder = { Text("e.g. John Doe", color = NexaTextSecondary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Name",
                                tint = NexaPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        singleLine = true,
                        colors = customDialogTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_account_name")
                    )
                }

                // Email / Username / Phone Field
                item {
                    OutlinedTextField(
                        value = accountEmail,
                        onValueChange = { accountEmail = it },
                        label = { Text("Login Email / Username / Phone") },
                        placeholder = { Text("e.g. user@example.com", color = NexaTextSecondary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = NexaPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        singleLine = true,
                        colors = customDialogTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_account_email")
                    )
                }

                // Password Field
                item {
                    OutlinedTextField(
                        value = accountPassword,
                        onValueChange = { accountPassword = it },
                        label = { Text("Account Password") },
                        placeholder = { Text("Enter account password", color = NexaTextSecondary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "Password",
                                tint = NexaPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    tint = NexaTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        colors = customDialogTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_account_password")
                    )
                }

                // Custom Package Name (if custom app entered)
                if (selectedApp == null && customAppName.isNotBlank()) {
                    item {
                        OutlinedTextField(
                            value = customPackageName,
                            onValueChange = { customPackageName = it },
                            label = { Text("Package Name (optional)") },
                            placeholder = { Text("e.g. com.example.app", color = NexaTextSecondary) },
                            singleLine = true,
                            colors = customDialogTextFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Privacy notice
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secured",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Stored privately on-device for NEXA AI autonomous automation.",
                            color = Color(0xFF10B981),
                            fontSize = 9.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            val appFinalName = selectedApp?.name ?: customAppName.trim()
            val canConnect = appFinalName.isNotBlank() && (accountName.isNotBlank() || accountEmail.isNotBlank())

            Button(
                onClick = {
                    if (canConnect) {
                        val base = selectedApp ?: AppPlugin(
                            id = appFinalName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' },
                            name = appFinalName,
                            packageName = customPackageName.ifBlank { "com." + appFinalName.lowercase().filter { it.isLetter() } },
                            category = customCategory,
                            description = "User connected integration for $appFinalName",
                            supportedActions = listOf("Open $appFinalName", "Automate Screen", "Voice Trigger"),
                            quickCommand = "Open $appFinalName",
                            iconType = appFinalName.lowercase()
                        )

                        val finalPlugin = base.copy(
                            accountName = accountName.trim(),
                            accountEmail = accountEmail.trim(),
                            accountPassword = accountPassword.trim(),
                            isConnected = true,
                            connectedAt = System.currentTimeMillis()
                        )
                        onConnect(finalPlugin)
                    }
                },
                enabled = canConnect,
                colors = ButtonDefaults.buttonColors(containerColor = NexaPurple),
                modifier = Modifier.testTag("btn_confirm_add_plugin")
            ) {
                Text("Connect App", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = NexaTextSecondary)
            }
        }
    )
}

@Composable
fun EditCredentialsDialog(
    plugin: AppPlugin,
    onDismiss: () -> Unit,
    onSave: (name: String, email: String, pass: String) -> Unit
) {
    var name by remember { mutableStateOf(plugin.accountName) }
    var email by remember { mutableStateOf(plugin.accountEmail) }
    var pass by remember { mutableStateOf(plugin.accountPassword) }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF141428),
        title = {
            Text(
                text = "Edit ${plugin.name} Credentials",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name") },
                    singleLine = true,
                    colors = customDialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email / Username") },
                    singleLine = true,
                    colors = customDialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = { Text("Password") },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility",
                                tint = NexaTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    colors = customDialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name.trim(), email.trim(), pass.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = NexaPurple)
            ) {
                Text("Save Changes", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = NexaTextSecondary)
            }
        }
    )
}

@Composable
private fun customDialogTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NexaPurple,
    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = NexaPurple,
    unfocusedLabelColor = NexaTextSecondary
)

fun getPluginIcon(iconType: String): ImageVector {
    return when (iconType.lowercase()) {
        "facebook" -> Icons.Default.Share
        "instagram" -> Icons.Default.Public
        "youtube" -> Icons.Default.Videocam
        "whatsapp" -> Icons.AutoMirrored.Filled.Chat
        "messenger" -> Icons.AutoMirrored.Filled.Chat
        "docs" -> Icons.Default.Description
        "sheets" -> Icons.Default.TableChart
        "slides" -> Icons.Default.Description
        "spotify" -> Icons.Default.MusicNote
        "chrome" -> Icons.Default.Language
        "phone" -> Icons.Default.Call
        "system" -> Icons.Default.FlashlightOn
        else -> Icons.Default.Extension
    }
}

fun getPluginColor(iconType: String): Color {
    return when (iconType.lowercase()) {
        "facebook" -> Color(0xFF1877F2)
        "instagram" -> Color(0xFFE1306C)
        "youtube" -> Color(0xFFFF0000)
        "whatsapp" -> Color(0xFF25D366)
        "messenger" -> Color(0xFF00B2FF)
        "docs" -> Color(0xFF4285F4)
        "sheets" -> Color(0xFF0F9D58)
        "slides" -> Color(0xFFF4B400)
        "spotify" -> Color(0xFF1DB954)
        "chrome" -> Color(0xFF34A853)
        "phone" -> Color(0xFF6366F1)
        "system" -> NexaPurple
        else -> NexaPink
    }
}
