package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AutomationResult
import com.example.data.NexaAiService
import com.example.data.NexaRepository
import com.example.data.SystemAutomationManager
import com.example.data.NetworkConnectivityObserver
import com.example.data.NexaOfflineEngine
import com.example.model.ActivityItem
import com.example.model.AppPlugin
import com.example.model.AppTab
import com.example.model.ChatMessage
import com.example.model.NexaLanguage
import com.example.model.OfflineNote
import com.example.model.WriteCategory
import com.example.speech.NexaSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ImageGenState(
    val prompt: String = "",
    val isLoading: Boolean = false,
    val resultImageUrl: String? = null,
    val resultBitmap: Bitmap? = null,
    val error: String? = null
)

data class WriteState(
    val topic: String = "",
    val category: WriteCategory = WriteCategory.CODE,
    val isGenerating: Boolean = false,
    val resultText: String? = null,
    val error: String? = null
)

data class ToastMessage(
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

class NexaViewModel(
    val repository: NexaRepository,
    val aiService: NexaAiService,
    val automationManager: SystemAutomationManager,
    val connectivityObserver: NetworkConnectivityObserver? = null
) : ViewModel() {

    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    val isOnline: StateFlow<Boolean> = connectivityObserver?.isOnline ?: MutableStateFlow(true).asStateFlow()
    val isOfflineForced: StateFlow<Boolean> = repository.offlineForceEnabledFlow
    val offlineNotes: StateFlow<List<OfflineNote>> = repository.offlineNotesFlow

    val isOfflineModeActive: StateFlow<Boolean> = combine(isOnline, isOfflineForced) { online, forced ->
        forced || !online
    }.stateIn(viewModelScope, SharingStarted.Eagerly, repository.isOfflineModeForced())

    fun toggleOfflineModeForced() {
        val next = !repository.isOfflineModeForced()
        repository.setOfflineModeForced(next)
        showToast(if (next) "⚡ Offline Mode ON (Zero data needed)" else "🌐 Live Online Mode restored")
    }

    fun addOfflineNote(title: String, content: String): OfflineNote {
        val note = repository.addOfflineNote(title, content)
        showToast("Note saved offline 📝")
        return note
    }

    fun deleteOfflineNote(id: String) {
        repository.deleteOfflineNote(id)
        showToast("Note deleted")
    }

    fun clearAllOfflineNotes() {
        repository.clearAllOfflineNotes()
        showToast("All offline notes cleared")
    }

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Hello! I am NEXA AI. Ask me anything in English, Bengali, or Hindi, or speak your voice commands!",
                isUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _imageState = MutableStateFlow(ImageGenState())
    val imageState: StateFlow<ImageGenState> = _imageState.asStateFlow()

    private val _writeState = MutableStateFlow(WriteState())
    val writeState: StateFlow<WriteState> = _writeState.asStateFlow()

    private val _toastFlow = MutableStateFlow<ToastMessage?>(null)
    val toastFlow: StateFlow<ToastMessage?> = _toastFlow.asStateFlow()

    private val _isQueryProcessing = MutableStateFlow(false)
    val isQueryProcessing: StateFlow<Boolean> = _isQueryProcessing.asStateFlow()

    var speechManager: NexaSpeechManager? = null

    val languageFlow: StateFlow<NexaLanguage> = repository.languageFlow
    val historyFlow: StateFlow<List<ActivityItem>> = repository.historyFlow
    val modeFlow: StateFlow<com.example.model.NexaMode> = repository.modeFlow
    val customModePrompt: StateFlow<String> = repository.customModePrompt
    val torchState: StateFlow<Boolean> = automationManager.torchState
    val pluginsFlow: StateFlow<List<AppPlugin>> = repository.pluginsFlow
    val selectedVoiceIdFlow: StateFlow<String> = repository.selectedVoiceIdFlow
    val elevenLabsApiKeyFlow: StateFlow<String> = repository.elevenLabsKeyFlow
    val backgroundServiceEnabledFlow: StateFlow<Boolean> = repository.backgroundServiceEnabledFlow
    val autoWakeOnScreenOnFlow: StateFlow<Boolean> = repository.autoWakeOnScreenOnFlow
    val autoLaunchOnScreenWakeFlow: StateFlow<Boolean> = repository.autoLaunchOnScreenWakeFlow
    val isBackgroundServiceRunning: StateFlow<Boolean> = com.example.service.NexaBackgroundService.isRunning
    val lastScreenWakeEvent: StateFlow<String> = com.example.service.NexaBackgroundService.lastScreenEvent
    val screenWakeCount: StateFlow<Int> = com.example.service.NexaBackgroundService.screenWakeCount

    val isNotificationListenerEnabled: Boolean
        get() = automationManager.isNotificationListenerEnabled()

    val lastCapturedMessage = com.example.service.NexaNotificationService.lastNotification

    fun setAutoWakeOnScreenOn(enabled: Boolean) {
        repository.setAutoWakeOnScreenOn(enabled)
        showToast(if (enabled) "Screen-On Auto Wake enabled ⚡" else "Screen-On Auto Wake disabled")
    }

    fun setAutoLaunchOnScreenWake(enabled: Boolean) {
        repository.setAutoLaunchOnScreenWake(enabled)
        showToast(if (enabled) "Auto-Launch NEXA on Screen Wake enabled" else "Auto-Launch on Screen Wake disabled")
    }

    fun toggleBackgroundService(context: Context) {
        val currentlyRunning = isBackgroundServiceRunning.value
        if (currentlyRunning) {
            com.example.service.NexaBackgroundService.stop(context)
            repository.setBackgroundServiceEnabled(false)
            showToast("NEXA Background Service stopped")
        } else {
            com.example.service.NexaBackgroundService.start(context)
            repository.setBackgroundServiceEnabled(true)
            showToast("⚡ NEXA Background Service active & ready on screen wake!")
        }
    }

    fun lockScreenVoiceAction() {
        val res = automationManager.lockDeviceScreen()
        if (res.success) {
            showToast(res.message)
            speakWithActiveVoice("Screen locked successfully")
        } else {
            showToast(res.message)
            speakWithActiveVoice("Please grant Accessibility or Device Admin permission to lock the screen")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                automationManager.openAccessibilitySettings()
            } else {
                automationManager.requestDeviceAdmin()
            }
        }
    }

    fun requestDeviceAdmin() {
        automationManager.requestDeviceAdmin()
    }

    fun openAccessibilitySettings() {
        automationManager.openAccessibilitySettings()
    }

    fun openOverlaySettings() {
        automationManager.openOverlaySettings()
    }

    fun openNotificationListenerSettings() {
        automationManager.openNotificationListenerSettings()
    }

    fun canDrawOverlays(): Boolean = automationManager.canDrawOverlays()

    fun isScreenLockCapable(): Boolean = automationManager.isScreenLockCapable()

    fun getDeviceLocation(): AutomationResult = automationManager.getDeviceLocation()

    fun callContactByName(name: String): AutomationResult = automationManager.callContactByName(name)

    fun setSelectedVoice(voiceId: String) {
        repository.setSelectedVoiceId(voiceId)
        val voice = com.example.model.NEXA_VOICES.find { it.voiceId.equals(voiceId, ignoreCase = true) }
        val name = voice?.name ?: "Selected Voice"
        showToast("Voice set to: $name ($voiceId)")
        repository.addHistory("Voice Selected", "$name ($voiceId)")
        voice?.let { previewVoice(it) }
    }

    fun previewVoice(voice: com.example.model.NexaVoice) {
        speechManager?.previewVoice(
            voice = voice,
            language = languageFlow.value,
            apiKey = repository.getElevenLabsApiKey()
        )
    }

    fun saveElevenLabsKey(key: String) {
        val trimmed = key.trim()
        if (trimmed.isNotBlank()) {
            repository.setElevenLabsApiKey(trimmed)
            showToast("ElevenLabs Key connected!")
        } else {
            repository.clearElevenLabsApiKey()
            showToast("ElevenLabs Key cleared!")
        }
    }

    fun clearElevenLabsKey() {
        repository.clearElevenLabsApiKey()
        showToast("ElevenLabs Key cleared!")
    }

    fun speakWithActiveVoice(text: String) {
        speechManager?.speak(
            text = text,
            language = languageFlow.value,
            voiceId = repository.getSelectedVoiceId(),
            elevenLabsApiKey = repository.getElevenLabsApiKey()
        )
    }

    fun setMode(mode: com.example.model.NexaMode) {
        repository.setMode(mode)
        repository.addHistory("Mode Changed", "${mode.title} activated")
        val greetingMsg = ChatMessage(
            text = "${mode.title} Mode Activated! ${mode.emoji}\n\n\"${mode.sampleGreeting}\"",
            isUser = false
        )
        _chatMessages.value = _chatMessages.value + greetingMsg
        _latestHomeResponse.value = greetingMsg
        speakWithActiveVoice(mode.sampleGreeting)
    }

    fun updateCustomModePrompt(prompt: String) {
        repository.setCustomModePrompt(prompt)
        showToast("Custom persona prompt saved!")
    }

    fun initSpeech(context: Context) {
        if (speechManager == null) {
            speechManager = NexaSpeechManager(
                context = context,
                onFinalSpeechResult = { recognizedText ->
                    processVoiceTranscript(recognizedText)
                },
                onSpeechError = { errorMsg ->
                    showToast(errorMsg)
                }
            )
        }
    }

    fun switchTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setLanguage(lang: NexaLanguage) {
        repository.setLanguage(lang)
        showToast("Voice Language set to ${lang.displayName} (${lang.shortLabel})")
    }

    fun showToast(msg: String) {
        _toastFlow.value = ToastMessage(msg)
    }

    fun dismissToast() {
        _toastFlow.value = null
    }

    fun toggleVoiceListening() {
        speechManager?.toggleListening(languageFlow.value)
    }

    private fun processVoiceTranscript(text: String) {
        if (text.isBlank()) return
        repository.addHistory("Voice Command", text)
        sendQuery(text, isFromVoice = true)
    }

    private val _latestHomeResponse = MutableStateFlow<ChatMessage>(
        ChatMessage(
            text = "Hello! I am NEXA AI. Ask me anything in English, Bengali, or Hindi, or speak your voice commands ♡",
            isUser = false
        )
    )
    val latestHomeResponse: StateFlow<ChatMessage> = _latestHomeResponse.asStateFlow()

    fun sendQuery(text: String, isFromVoice: Boolean = false) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        // Add user message
        val userMsg = ChatMessage(text = trimmed, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg
        if (!isFromVoice) {
            repository.addHistory("Chat Query", trimmed)
        }

        // Check if query is an automation or hardware action
        val automationHandled = handleAutomationCommand(trimmed, isFromVoice)
        if (automationHandled) {
            return
        }

        // If offline mode is active (either no internet or user forced offline), process locally via NexaOfflineEngine
        if (isOfflineModeActive.value) {
            _isQueryProcessing.value = true
            viewModelScope.launch {
                try {
                    val batteryInfo = automationManager.getBatteryInfo()
                    val storageInfo = automationManager.getAvailableStorage()
                    val offlineResult = NexaOfflineEngine.processOffline(
                        query = trimmed,
                        language = languageFlow.value,
                        mode = modeFlow.value,
                        batteryInfo = batteryInfo,
                        storageInfo = storageInfo
                    )
                    offlineResult.noteToSave?.let { (noteTitle, noteBody) ->
                        addOfflineNote(noteTitle, noteBody)
                    }
                    val botMsg = ChatMessage(text = offlineResult.replyText, isUser = false)
                    _chatMessages.value = _chatMessages.value + botMsg
                    _latestHomeResponse.value = botMsg
                    if (isFromVoice) {
                        speakWithActiveVoice(offlineResult.replyText)
                    }
                } finally {
                    _isQueryProcessing.value = false
                }
            }
            return
        }

        _isQueryProcessing.value = true

        viewModelScope.launch {
            try {
                val reply = aiService.processQuery(trimmed, languageFlow.value)
                val botMsg = ChatMessage(text = reply, isUser = false)
                _chatMessages.value = _chatMessages.value + botMsg
                _latestHomeResponse.value = botMsg

                if (isFromVoice) {
                    speakWithActiveVoice(reply)
                }
            } catch (e: Exception) {
                // If network connection failed, fall back cleanly to NexaOfflineEngine
                val batteryInfo = automationManager.getBatteryInfo()
                val storageInfo = automationManager.getAvailableStorage()
                val offlineResult = NexaOfflineEngine.processOffline(
                    query = trimmed,
                    language = languageFlow.value,
                    mode = modeFlow.value,
                    batteryInfo = batteryInfo,
                    storageInfo = storageInfo
                )
                offlineResult.noteToSave?.let { (noteTitle, noteBody) ->
                    addOfflineNote(noteTitle, noteBody)
                }
                val botMsg = ChatMessage(text = offlineResult.replyText, isUser = false)
                _chatMessages.value = _chatMessages.value + botMsg
                _latestHomeResponse.value = botMsg
                if (isFromVoice) {
                    speakWithActiveVoice(offlineResult.replyText)
                }
            } finally {
                _isQueryProcessing.value = false
            }
        }
    }

    /**
     * Intercepts natural speech & text commands for hardware/system automations
     */
    private fun handleAutomationCommand(query: String, isFromVoice: Boolean): Boolean {
        val lower = query.lowercase().trim()

        val result: AutomationResult? = when {
            // Offline Hub Switch
            lower.contains("offline mode") || lower.contains("অফলাইন মোড") || lower.contains("offline hub") -> {
                switchTab(AppTab.OFFLINE)
                AutomationResult(true, "⚡ Switched to Offline Mode Hub (Zero data needed)")
            }

            // Battery Telemetry
            lower.contains("battery level") || lower.contains("battery status") ||
            lower.contains("ব্যাটারি কত") || lower.contains("চার্জ কত") || lower.contains("charge koto") -> {
                val (pct, charging) = automationManager.getBatteryInfo()
                val chgText = if (charging) "Charging ⚡" else "Discharging"
                AutomationResult(true, "Battery level is $pct% ($chgText) 🔋")
            }

            // Volume Controls
            lower.contains("volume up") || lower.contains("sound up") || lower.contains("ভলিউম বাড়াও") || lower.contains("সাউন্ড বাড়াও") -> {
                automationManager.setDeviceVolume("up")
            }
            lower.contains("volume down") || lower.contains("sound down") || lower.contains("ভলিউম কমাও") || lower.contains("সাউন্ড কমাও") -> {
                automationManager.setDeviceVolume("down")
            }
            lower.contains("mute sound") || lower.contains("mute") || lower.contains("মিউট করো") || lower.contains("sound off") -> {
                automationManager.setDeviceVolume("mute")
            }
            lower.contains("max volume") || lower.contains("full volume") || lower.contains("ফুল সাউন্ড") -> {
                automationManager.setDeviceVolume("max")
            }

            // Common Device Tools
            lower.contains("open camera") || lower.contains("ক্যামেরা খোলো") || lower.contains("camera kholo") -> {
                automationManager.openSystemTool("camera")
            }
            lower.contains("open calculator") || lower.contains("ক্যালকুলেটর খোলো") || lower.contains("calculator kholo") -> {
                automationManager.openSystemTool("calculator")
            }
            lower.contains("open clock") || lower.contains("open alarm") || lower.contains("অ্যালার্ম") -> {
                automationManager.openSystemTool("clock")
            }

            // Screen Lock via Voice Command
            lower.contains("lock screen") || lower.contains("screen lock") ||
            lower.contains("lock phone") || lower.contains("lock the phone") ||
            lower.contains("lock device") || lower.contains("turn off screen") ||
            lower.contains("স্ক্রিন লক") || lower.contains("ফোন লক") ||
            lower.contains("মোবাইল লক") || lower.contains("লক করো") ||
            lower.contains("লক কর") || lower.contains("স্ক্রিন বন্ধ করো") ||
            lower.contains("স্ক্রিন অফ করো") || lower.contains("স্ক্রিন অফ") ||
            lower.contains("স্ক্রীন লক") || lower.contains("লক স্ক্রিন") ||
            lower.contains("स्क्रीन लॉक") || lower.contains("फोन लॉक") ||
            lower.contains("स्क्रीन बंद") || lower == "lock" -> {
                automationManager.lockDeviceScreen()
            }

            // Background Auto-Wake Toggles
            lower.contains("screen wake on") || lower.contains("background service on") ||
            lower.contains("ব্যাকগ্রাউন্ড সার্ভিস অন") || lower.contains("ব্যাকগ্রাউন্ড সার্ভিস চালু") -> {
                repository.setAutoWakeOnScreenOn(true)
                AutomationResult(true, "⚡ NEXA Auto-Wake on Screen-ON is now enabled!")
            }
            lower.contains("screen wake off") || lower.contains("background service off") ||
            lower.contains("ব্যাকগ্রাউন্ড সার্ভিস অফ") || lower.contains("ব্যাকগ্রাউন্ড সার্ভিস বন্ধ") -> {
                repository.setAutoWakeOnScreenOn(false)
                AutomationResult(true, "NEXA Auto-Wake on Screen-ON is now disabled.")
            }

            // Torch / Flashlight ON
            lower.contains("torch on") || lower.contains("light on") || lower.contains("flashlight on") ||
            lower.contains("টর্চ অন") || lower.contains("টর্চ জ্বালাও") || lower.contains("লাইটের সুইচ দাও") ||
            lower.contains("लाइट ऑन") || lower.contains("টর্চ চালু") -> {
                automationManager.setTorch(true)
            }

            // Torch / Flashlight OFF
            lower.contains("torch off") || lower.contains("light off") || lower.contains("flashlight off") ||
            lower.contains("টর্চ অফ") || lower.contains("টর্চ নিভাও") || lower.contains("টর্চ বন্ধ") ||
            lower.contains("लाइट बंद") -> {
                automationManager.setTorch(false)
            }

            // Wi-Fi
            lower.contains("wifi") || lower.contains("wi-fi") || lower.contains("ওয়াইফাই") -> {
                automationManager.openWifiSettings()
            }

            // Accessibility Settings
            lower.contains("accessibility setting") || lower.contains("অ্যাক্সেসিবিলিটি") -> {
                automationManager.openAccessibilitySettings()
            }

            // Autonomous Facebook Post
            lower.contains("facebook e post") || lower.contains("fb te post") || lower.contains("fb post") ||
            lower.contains("facebook post") || lower.contains("ফেসবুকে পোস্ট") || lower.contains("ফেসবুক পোস্ট") ||
            (lower.contains("facebook") && (lower.contains("post") || lower.contains("পোস্ট") || lower.contains("caption"))) -> {
                val extractedCaption = query.replace(Regex("(?i)(facebook e post koro|facebook e post|fb te post koro|fb post|ফেসবুকে পোস্ট করো|ফেসবুকে পোস্ট|facebook post|post to facebook|post on facebook)"), "")
                    .replace(Regex("(?i)^[:\\-\\s]+"), "").trim()
                val finalCaption = if (extractedCaption.isNotBlank()) {
                    extractedCaption
                } else {
                    "Excited to share this wonderful moment with everyone! Powered by NEXA AI ✨🚀 #NexaAI #Autonomous"
                }
                automationManager.postToFacebook(finalCaption)
            }

            // Autonomous Instagram Post
            lower.contains("instagram e post") || lower.contains("insta te post") || lower.contains("insta post") ||
            lower.contains("instagram post") || lower.contains("ইনস্টাগ্রামে পোস্ট") ||
            (lower.contains("instagram") && (lower.contains("post") || lower.contains("পোস্ট") || lower.contains("caption"))) -> {
                val extractedCaption = query.replace(Regex("(?i)(instagram e post koro|instagram e post|insta te post|insta post|ইনস্টাগ্রামে পোস্ট করো|ইনস্টাগ্রামে পোস্ট|instagram post|post on instagram|post to instagram)"), "")
                    .replace(Regex("(?i)^[:\\-\\s]+"), "").trim()
                val finalCaption = if (extractedCaption.isNotBlank()) {
                    extractedCaption
                } else {
                    "Vibes and futuristic moments captured through NEXA AI 💜✨ #Nexa #AI #Aesthetic #Futuristic"
                }
                automationManager.postToInstagram(finalCaption)
            }

            // Autonomous YouTube Post / Creator
            lower.contains("youtube e post") || lower.contains("youtube post") || lower.contains("ইউটিউবে পোস্ট") ||
            (lower.contains("youtube") && (lower.contains("post") || lower.contains("community"))) -> {
                val extractedContent = query.replace(Regex("(?i)(youtube e post koro|youtube e post|youtube post|ইউটিউবে পোস্ট করো|ইউটিউবে পোস্ট|post on youtube)"), "")
                    .replace(Regex("(?i)^[:\\-\\s]+"), "").trim()
                val finalContent = if (extractedContent.isNotBlank()) {
                    extractedContent
                } else {
                    "Welcome back to another exciting update! Don't forget to like, subscribe and share 🔔🎬"
                }
                automationManager.postToYouTube(finalContent)
            }

            // Screen Auto-Actions: Like
            lower.contains("auto like") || lower.contains("screen like") || lower.contains("লাইক দাও") ||
            lower.contains("like dao") || lower.contains("like koro") || lower.contains("লাইক করো") -> {
                automationManager.clickLike()
            }

            // Screen Auto-Actions: Follow
            lower.contains("auto follow") || lower.contains("screen follow") || lower.contains("ফলো করো") ||
            lower.contains("follow dao") || lower.contains("follow koro") || lower.contains("ফলো দাও") -> {
                automationManager.clickFollow()
            }

            // Screen Auto-Actions: Subscribe
            lower.contains("subscribe koro") || lower.contains("সাবস্ক্রাইব করো") || lower.contains("auto subscribe") -> {
                automationManager.clickSubscribe()
            }

            // Screen Auto-Actions: Comment
            lower.contains("comment koro") || lower.contains("কমেন্ট করো") || lower.contains("auto comment") -> {
                automationManager.clickComment()
            }

            // Screen Auto-Actions: Click Post / Publish Button
            lower.contains("post button") || lower.contains("publish post") || lower.contains("পোস্ট বাটনে ক্লিক") -> {
                automationManager.clickPostButton()
            }

            // Screen Global Gestures
            lower.contains("back jao") || lower.contains("go back") || lower.contains("পিছনে যাও") -> {
                automationManager.pressBack()
            }
            lower.contains("home jao") || lower.contains("go home") || lower.contains("হোমে যাও") -> {
                automationManager.pressHome()
            }
            lower.contains("notification dekhao") || lower.contains("notifications") || lower.contains("নোটিফিকেশন") -> {
                automationManager.openNotifications()
            }

            // YouTube Search
            (lower.startsWith("youtube search") || lower.startsWith("search youtube") ||
             lower.contains("ইউটিউবে সার্চ") || lower.contains("youtube e search")) -> {
                val searchTerm = query.replace(Regex("(?i)(youtube search|search youtube for|search youtube|ইউটিউবে সার্চ করো|ইউটিউবে সার্চ|youtube e search)"), "").trim()
                automationManager.searchYouTube(if (searchTerm.isNotBlank()) searchTerm else "Trending Music")
            }

            // Google Live Search
            (lower.startsWith("google search") || lower.startsWith("search google") ||
             lower.contains("গুগলে সার্চ") || lower.contains("google e khojo")) -> {
                val searchTerm = query.replace(Regex("(?i)(google search for|google search|search google for|search google|গুগলে সার্চ করো|গুগলে সার্চ|google e khojo)"), "").trim()
                automationManager.searchGoogleLive(if (searchTerm.isNotBlank()) searchTerm else "Latest Tech News")
            }

            // Open YouTube
            lower.contains("open youtube") || lower.contains("youtube kholo") || lower.contains("ইউটিউব খোলো") -> {
                automationManager.openSocialApp("youtube")
            }

            // Open Facebook
            lower.contains("open facebook") || lower.contains("facebook kholo") || lower.contains("ফেসবুক খোলো") -> {
                automationManager.openSocialApp("facebook")
            }

            // Open Instagram
            lower.contains("open instagram") || lower.contains("insta kholo") || lower.contains("ইনস্টাগ্রাম খোলো") -> {
                automationManager.openSocialApp("instagram")
            }

            // Open WhatsApp
            lower.contains("open whatsapp") || lower.contains("whatsapp kholo") || lower.contains("হোয়াটসঅ্যাপ খোলো") -> {
                automationManager.openSocialApp("whatsapp")
            }

            // Open Messenger
            lower.contains("open messenger") || lower.contains("messenger kholo") || lower.contains("মেসেঞ্জার খোলো") -> {
                automationManager.openMessenger()
            }

            // Media Play / Pause
            lower.contains("play music") || lower.contains("music play") || lower.contains("গান বাজাও") ||
            lower.contains("pause music") || lower.contains("video play") -> {
                automationManager.toggleMediaPlayPause()
            }

            // Google Docs
            lower.contains("new doc") || lower.contains("google doc") || lower.contains("open doc") || lower.contains("ডক খোলো") -> {
                automationManager.openNewGoogleDoc()
            }

            // Google Sheets
            lower.contains("new sheet") || lower.contains("google sheet") || lower.contains("open sheet") || lower.contains("শিট খোলো") -> {
                automationManager.openNewGoogleSheet()
            }

            // Microsoft Excel
            lower.contains("excel") || lower.contains("microsoft excel") || lower.contains("এক্সেল") -> {
                automationManager.openExcel()
            }

            // Google Slides
            lower.contains("new slide") || lower.contains("google slide") || lower.contains("open slide") || lower.contains("স্লাইড খোলো") -> {
                automationManager.openNewGoogleSlide()
            }

            // App Install via Play Store
            lower.contains("install app") || lower.contains("app install") || lower.contains("ইনস্টল করো") || lower.contains("ইন্সটল করো") -> {
                val appTarget = query.replace(Regex("(?i)(install app|app install|ইনস্টল করো|ইন্সটল করো)"), "").trim()
                automationManager.openPlayStore(if (appTarget.isNotBlank()) appTarget else "Top Apps")
            }

            // App Uninstall
            lower.contains("uninstall app") || lower.contains("app uninstall") || lower.contains("আনইনস্টল করো") -> {
                val pkgTarget = query.replace(Regex("(?i)(uninstall app|app uninstall|আনইনস্টল করো)"), "").trim()
                automationManager.openAppUninstall(if (pkgTarget.isNotBlank()) pkgTarget else "")
            }

            // Phone Call by Number
            (lower.startsWith("call ") || lower.contains("ফোন করো") || lower.contains("কল করো")) &&
            lower.any { it.isDigit() } -> {
                val digits = query.filter { it.isDigit() || it == '+' }
                if (digits.isNotBlank()) {
                    automationManager.makePhoneCall(digits)
                } else {
                    null
                }
            }

            // Contact Name Voice Calling (e.g. "Call Mummy", "Call Rahul", "আম্মুকে কল করো")
            lower.startsWith("call ") || lower.contains("ফোন করো") || lower.contains("কল করো") -> {
                val contactName = query.replace(Regex("(?i)(call|phone|ফোন করো|কল করো|কে কল করো|কে ফোন করো)"), "").trim()
                if (contactName.isNotBlank()) {
                    automationManager.callContactByName(contactName)
                } else {
                    automationManager.makePhoneCall("")
                }
            }

            // GPS & Location Detection
            lower.contains("my location") || lower.contains("current location") ||
            lower.contains("where am i") || lower.contains("আমার লোকেশন") ||
            lower.contains("লোকেশন") || lower.contains("আমি কোথায়") -> {
                automationManager.getDeviceLocation()
            }

            // Display Over Other Apps (Overlay)
            lower.contains("overlay permission") || lower.contains("display over app") ||
            lower.contains("ডিসপ্লে ওভার অ্যাপ") -> {
                automationManager.openOverlaySettings()
                AutomationResult(true, "Opening Display Over Other Apps settings 🪟")
            }

            // Screen Auto-Click simulation
            lower.contains("click screen") || lower.contains("screen e click") || lower.contains("স্ক্রিনে ক্লিক") -> {
                automationManager.simulateTap(540f, 960f)
            }

            // Like / Follow auto click on screen
            lower.contains("auto like") || lower.contains("screen like") -> {
                automationManager.clickOnScreenText("Like")
            }

            lower.contains("auto follow") || lower.contains("screen follow") -> {
                automationManager.clickOnScreenText("Follow")
            }

            // Read last WhatsApp / Messenger message
            lower.contains("read message") || lower.contains("last message") ||
            lower.contains("মেসেজ পড়ো") || lower.contains("কি মেসেজ এসেছে") ||
            lower.contains("মেসেজ চেক করো") || lower.contains("পড়ে শোনাও") ||
            lower.contains("whatsapp message check") -> {
                val msg = lastCapturedMessage.value
                if (msg != null) {
                    val reply = "Captured a message from ${msg.sender} on ${if(msg.pkg.contains("whatsapp")) "WhatsApp" else "Messenger"}: ${msg.content}"
                    AutomationResult(true, reply)
                } else {
                    if (!isNotificationListenerEnabled) {
                        AutomationResult(false, "Notification access is NOT enabled. Please grant permission to read messages.", detail = "NEED_NOTIFICATION_PERMISSION")
                    } else {
                        AutomationResult(false, "No new WhatsApp or Messenger messages captured yet.")
                    }
                }
            }

            // Reply to last message
            (lower.contains("reply") || lower.contains("রিপ্লাই দাও")) && lastCapturedMessage.value != null -> {
                val msg = lastCapturedMessage.value!!
                val replyText = query.replace(Regex("(?i)(reply|reply koro|রিপ্লাই দাও|উত্তর দাও)"), "").trim()
                val finalReply = if (replyText.isNotBlank()) replyText else "Okay, I'll reply with a default message."
                
                if (msg.pkg.contains("whatsapp")) {
                    automationManager.sendWhatsApp("", finalReply)
                } else {
                    automationManager.openMessenger()
                }
                AutomationResult(true, "Opening ${if(msg.pkg.contains("whatsapp")) "WhatsApp" else "Messenger"} to reply to ${msg.sender}...")
            }

            // Gemini Image Generation via Voice Command
            (lower.contains("generate image") || lower.contains("create image") ||
             lower.contains("draw an image") || lower.contains("make image") ||
             lower.contains("ছবি আঁকো") || lower.contains("ছবি বানাও") ||
             lower.contains("ছবি তৈরি করো") || lower.contains("चित्र बनाओ")) -> {
                val promptExtracted = query.replace(Regex("(?i)(generate image of|generate image|create image of|create image|draw an image of|make image of|ছবি আঁকো|ছবি বানাও|ছবি তৈরি করো|চিত্র बनाओ)"), "")
                    .replace(Regex("(?i)^[:\\-\\s]+"), "").trim()
                val finalPrompt = if (promptExtracted.isNotBlank()) promptExtracted else "A futuristic cybernetic city at night"
                switchTab(AppTab.CREATE_IMAGE)
                generateImage(finalPrompt)
                AutomationResult(true, "Synthesizing image with Gemini API: \"$finalPrompt\" 🎨")
            }

            else -> null
        }

        if (result != null) {
            repository.addHistory("System Automation", result.message)
            showToast(result.message)
            val botMsg = ChatMessage(
                text = "⚡ Automation Executed:\n${result.message}",
                isUser = false
            )
            _chatMessages.value = _chatMessages.value + botMsg
            _latestHomeResponse.value = botMsg
            if (isFromVoice) {
                speakWithActiveVoice(result.message)
            }
            if (result.detail == "NEED_LOCK_PERMISSION") {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    automationManager.openAccessibilitySettings()
                } else {
                    automationManager.requestDeviceAdmin()
                }
            } else if (result.detail == "NEED_NOTIFICATION_PERMISSION") {
                automationManager.openNotificationListenerSettings()
            }
            return true
        }

        return false
    }

    fun executeQuickAction(
        actionName: String,
        promptText: String,
        actionUrl: String? = null,
        context: Context
    ) {
        repository.addHistory(actionName, promptText)

        if (!actionUrl.isNullOrBlank()) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(actionUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                showToast("Cannot open link: ${e.localizedMessage}")
            }
        }

        switchTab(AppTab.CHAT)
        sendQuery(promptText, isFromVoice = true)
    }

    fun generateImage(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty()) {
            showToast("Please enter an image description!")
            return
        }
        repository.addHistory("Gemini Image Gen", trimmed)
        _imageState.value = ImageGenState(prompt = trimmed, isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val bitmap = aiService.generateImageWithGemini(trimmed)
                _imageState.value = ImageGenState(
                    prompt = trimmed,
                    isLoading = false,
                    resultBitmap = bitmap,
                    error = null
                )
                showToast("Image synthesized by Gemini API! 🎨")
            } catch (e: Exception) {
                val err = e.localizedMessage ?: "Image generation failed"
                _imageState.value = ImageGenState(
                    prompt = trimmed,
                    isLoading = false,
                    error = err
                )
                showToast("Gemini Error: $err")
            }
        }
    }

    fun generateWriting(topic: String, category: WriteCategory) {
        val trimmed = topic.trim()
        if (trimmed.isEmpty()) {
            showToast("Please enter a topic or prompt!")
            return
        }
        repository.addHistory("Write Content", "$category: $trimmed")
        _writeState.value = WriteState(
            topic = trimmed,
            category = category,
            isGenerating = true,
            resultText = "NEXA is formulating ${category.title}..."
        )

        viewModelScope.launch {
            try {
                val result = aiService.generateContentOrCode(trimmed, category.prefix, languageFlow.value)
                _writeState.value = WriteState(
                    topic = trimmed,
                    category = category,
                    isGenerating = false,
                    resultText = result
                )
                showToast("${category.title} generated!")
            } catch (e: Exception) {
                _writeState.value = WriteState(
                    topic = trimmed,
                    category = category,
                    isGenerating = false,
                    error = e.localizedMessage
                )
                showToast("Writing error: ${e.localizedMessage}")
            }
        }
    }

    // Direct helper for social post caption generator
    fun generateSocialCaption(topic: String, platform: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val prompt = "Create a catchy, trending $platform caption with emojis and hashtags for: $topic"
                val caption = aiService.processQuery(prompt, languageFlow.value)
                onResult(caption)
            } catch (e: Exception) {
                onResult("✨ $topic #trending #nexa #viral ✨")
            }
        }
    }

    // Direct helper for AI comment generator
    fun generateComment(contextText: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val prompt = "Generate a genuine, engaging, and supportive short social media comment for this post: $contextText"
                val comment = aiService.processQuery(prompt, languageFlow.value)
                onResult(comment)
            } catch (e: Exception) {
                onResult("Awesome post! Keep inspiring! 🔥✨")
            }
        }
    }

    fun saveGeminiKey(key: String) {
        val trimmed = key.trim()
        if (trimmed.isNotBlank()) {
            repository.setGeminiApiKey(trimmed)
            showToast("Gemini Key connected!")
        } else {
            repository.clearGeminiApiKey()
            showToast("Gemini Key removed!")
        }
    }

    fun clearGeminiKey() {
        repository.clearGeminiApiKey()
        showToast("Gemini Key cleared!")
    }

    fun saveGroqKey(key: String) {
        val trimmed = key.trim()
        if (trimmed.isNotBlank()) {
            repository.setGroqApiKey(trimmed)
            showToast("Groq Key connected!")
        } else {
            repository.clearGroqApiKey()
            showToast("Groq Key removed!")
        }
    }

    fun clearGroqKey() {
        repository.clearGroqApiKey()
        showToast("Groq Key cleared!")
    }

    fun togglePluginConnection(pluginId: String) {
        val connected = repository.togglePluginConnection(pluginId)
        val name = repository.pluginsFlow.value.find { it.id == pluginId }?.name ?: pluginId
        if (connected) {
            showToast("$name connected to NEXA AI ✓")
            repository.addHistory("Plugin Connected", "$name integration active")
        } else {
            showToast("$name disconnected from NEXA AI")
            repository.addHistory("Plugin Disconnected", "$name integration paused")
        }
    }

    fun addConnectedPlugin(plugin: AppPlugin) {
        repository.addPlugin(plugin)
        showToast("${plugin.name} connected to NEXA AI ✓")
        repository.addHistory("Plugin Added", "${plugin.name} (${if (plugin.accountEmail.isNotBlank()) plugin.accountEmail else plugin.accountName})")
    }

    fun removePlugin(pluginId: String) {
        val name = repository.pluginsFlow.value.find { it.id == pluginId }?.name ?: pluginId
        repository.removePlugin(pluginId)
        showToast("$name plugin removed")
        repository.addHistory("Plugin Removed", name)
    }

    fun updatePluginCredentials(pluginId: String, name: String, email: String, pass: String) {
        repository.updatePluginCredentials(pluginId, name, email, pass)
        showToast("Credentials updated!")
    }

    fun executePluginAction(plugin: AppPlugin, action: String, context: Context) {
        if (!plugin.isConnected) {
            showToast("${plugin.name} is disconnected! Enable plugin toggle first.")
            return
        }

        repository.addHistory("Plugin Action", "${plugin.name}: $action")
        showToast("Executing $action on ${plugin.name}...")

        when (plugin.id) {
            "facebook" -> {
                when {
                    action.contains("Post", ignoreCase = true) || action.contains("Status", ignoreCase = true) -> {
                        generateSocialCaption("futuristic AI updates and tech breakthroughs", "Facebook") { caption ->
                            automationManager.postToFacebook(caption)
                        }
                    }
                    action.contains("Like", ignoreCase = true) -> automationManager.clickLike()
                    action.contains("Comment", ignoreCase = true) -> automationManager.clickComment()
                    else -> automationManager.openApp(plugin.packageName)
                }
            }
            "instagram" -> {
                when {
                    action.contains("Post", ignoreCase = true) || action.contains("Caption", ignoreCase = true) -> {
                        generateSocialCaption("cyber aesthetic vibes and neon night life", "Instagram") { caption ->
                            automationManager.postToInstagram(caption)
                        }
                    }
                    action.contains("Like", ignoreCase = true) -> automationManager.clickLike()
                    action.contains("Follow", ignoreCase = true) -> automationManager.clickFollow()
                    else -> automationManager.openApp(plugin.packageName)
                }
            }
            "youtube" -> {
                when {
                    action.contains("Search", ignoreCase = true) -> automationManager.searchYouTube("cyberpunk futuristic synthwave music")
                    action.contains("Subscribe", ignoreCase = true) -> automationManager.clickSubscribe()
                    action.contains("Like", ignoreCase = true) -> automationManager.clickLike()
                    action.contains("Comment", ignoreCase = true) -> automationManager.clickComment()
                    else -> automationManager.openApp(plugin.packageName)
                }
            }
            "whatsapp" -> {
                automationManager.sendWhatsApp("", "Hi! Automated message sent via NEXA AI.")
            }
            "messenger" -> {
                automationManager.openMessenger()
            }
            "googledocs" -> {
                automationManager.openNewGoogleDoc()
            }
            "googlesheets" -> {
                automationManager.openNewGoogleSheet()
            }
            "googleslides" -> {
                automationManager.openNewGoogleSlide()
            }
            "spotify" -> {
                automationManager.openApp("com.spotify.music")
            }
            "chrome" -> {
                automationManager.searchGoogleLive("Latest AI and technology updates")
            }
            "phone" -> {
                automationManager.makePhoneCall("1234567890")
            }
            "system" -> {
                when {
                    action.contains("Torch", ignoreCase = true) -> automationManager.toggleTorch()
                    action.contains("Wi-Fi", ignoreCase = true) -> automationManager.openWifiSettings()
                    action.contains("Screen", ignoreCase = true) -> automationManager.simulateTap(540f, 1000f)
                    action.contains("Notification", ignoreCase = true) -> automationManager.openNotifications()
                    else -> automationManager.openAccessibilitySettings()
                }
            }
            else -> {
                automationManager.openApp(plugin.packageName)
            }
        }
    }

    fun clearHistory() {
        repository.clearHistory()
        showToast("History cleared!")
    }

    override fun onCleared() {
        super.onCleared()
        speechManager?.destroy()
    }
}
