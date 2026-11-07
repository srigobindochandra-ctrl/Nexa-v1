package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.NexaAiService
import com.example.data.NexaRepository
import com.example.data.SystemAutomationManager
import com.example.model.AppTab
import com.example.ui.NexaViewModel
import com.example.ui.components.CosmicOrbButton
import com.example.ui.components.NexaToastAlert
import com.example.ui.components.NexaTopBar
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ConnectionsScreen
import com.example.ui.screens.CreateImageScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ModesScreen
import androidx.compose.material.icons.filled.Extension
import com.example.ui.screens.OfflineHubScreen
import com.example.ui.screens.PluginsScreen
import com.example.ui.screens.QuickActionsScreen
import com.example.ui.screens.WriteScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NexaBg
import com.example.ui.theme.NexaPink
import com.example.ui.theme.NexaPurple
import com.example.ui.theme.NexaTextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                NexaMainApp()
            }
        }
    }
}

@Composable
fun NexaMainApp() {
    val context = LocalContext.current

    // Initialize dependencies
    val repository = remember { NexaRepository(context) }
    val aiService = remember { NexaAiService(repository) }
    val automationManager = remember { SystemAutomationManager(context) }
    val connectivityObserver = remember { com.example.data.NetworkConnectivityObserver(context) }
    val viewModel = remember { NexaViewModel(repository, aiService, automationManager, connectivityObserver) }

    // Unified Permission Request Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (audioGranted) {
            viewModel.initSpeech(context)
        }
        val grantedCount = permissions.values.count { it }
        viewModel.showToast("$grantedCount permissions verified! ⚡")
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissionsToRequest.toTypedArray())

        val audioCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        )
        if (audioCheck == PackageManager.PERMISSION_GRANTED) {
            viewModel.initSpeech(context)
        }

        // Auto-enable background service if enabled in preferences
        if (repository.isBackgroundServiceEnabled()) {
            com.example.service.NexaBackgroundService.start(context)
        }
    }

    val currentTab by viewModel.currentTab.collectAsState()
    val currentLang by viewModel.languageFlow.collectAsState()
    val activeMode by viewModel.modeFlow.collectAsState()
    val toastMessage by viewModel.toastFlow.collectAsState()
    val isOfflineModeActive by viewModel.isOfflineModeActive.collectAsState()

    val speechManager = viewModel.speechManager
    val isListening by speechManager?.isListening?.collectAsState() ?: remember {
        androidx.compose.runtime.mutableStateOf(false)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(NexaBg),
        containerColor = NexaBg,
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                NexaTopBar(
                    currentLanguage = currentLang,
                    onLanguageChange = { viewModel.setLanguage(it) },
                    activeMode = activeMode,
                    onOpenModes = { viewModel.switchTab(AppTab.MODES) },
                    onOpenQuickActions = { viewModel.switchTab(AppTab.QUICK_ACTIONS) },
                    onOpenSettings = { viewModel.switchTab(AppTab.CONNECTIONS) },
                    onLogoClick = { viewModel.switchTab(AppTab.HOME) },
                    isOfflineActive = isOfflineModeActive,
                    onOpenOfflineHub = { viewModel.switchTab(AppTab.OFFLINE) }
                )
                NexaToastAlert(
                    toast = toastMessage,
                    onDismiss = { viewModel.dismissToast() }
                )
            }
        },
        bottomBar = {
            NexaBottomNavigationBar(
                currentTab = currentTab,
                isListening = isListening,
                onTabSelected = { viewModel.switchTab(it) },
                onCosmicOrbClick = {
                    val permissionCheck = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    )
                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                        viewModel.switchTab(AppTab.HOME)
                        viewModel.toggleVoiceListening()
                    } else {
                        permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.HOME -> HomeScreen(viewModel = viewModel)
                AppTab.OFFLINE -> OfflineHubScreen(viewModel = viewModel)
                AppTab.MODES -> ModesScreen(viewModel = viewModel)
                AppTab.PLUGINS -> PluginsScreen(viewModel = viewModel)
                AppTab.QUICK_ACTIONS -> QuickActionsScreen(viewModel = viewModel)
                AppTab.CHAT -> ChatScreen(viewModel = viewModel)
                AppTab.CREATE_IMAGE -> CreateImageScreen(viewModel = viewModel)
                AppTab.WRITE -> WriteScreen(viewModel = viewModel)
                AppTab.CONNECTIONS -> ConnectionsScreen(viewModel = viewModel)
                AppTab.HISTORY -> HistoryScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun NexaBottomNavigationBar(
    currentTab: AppTab,
    isListening: Boolean,
    onTabSelected: (AppTab) -> Unit,
    onCosmicOrbClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = NexaBg.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, NexaPurple.copy(alpha = 0.25f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                NavTabItem(
                    label = "Home",
                    icon = Icons.Default.Home,
                    isSelected = currentTab == AppTab.HOME,
                    onClick = { onTabSelected(AppTab.HOME) },
                    testTag = "nav_tab_home"
                )

                // Modes
                NavTabItem(
                    label = "Modes",
                    icon = Icons.Default.Psychology,
                    isSelected = currentTab == AppTab.MODES,
                    onClick = { onTabSelected(AppTab.MODES) },
                    testTag = "nav_tab_modes"
                )

                // Spacer for floating center orb
                Spacer(modifier = Modifier.size(54.dp))

                // Plugins
                NavTabItem(
                    label = "Plugins",
                    icon = Icons.Default.Extension,
                    isSelected = currentTab == AppTab.PLUGINS,
                    onClick = { onTabSelected(AppTab.PLUGINS) },
                    testTag = "nav_tab_plugins"
                )

                // Chat
                NavTabItem(
                    label = "Chat",
                    icon = Icons.AutoMirrored.Filled.Message,
                    isSelected = currentTab == AppTab.CHAT,
                    onClick = { onTabSelected(AppTab.CHAT) },
                    testTag = "nav_tab_chat"
                )
            }

            // Floating Center Cosmic Orb
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-14).dp)
            ) {
                CosmicOrbButton(
                    isListening = isListening,
                    onClick = onCosmicOrbClick
                )
            }
        }
    }
}

@Composable
fun NavTabItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) NexaPurple else NexaTextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = if (isSelected) NexaPurple else NexaTextSecondary,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
