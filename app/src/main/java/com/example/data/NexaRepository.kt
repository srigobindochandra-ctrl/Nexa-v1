package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ActivityItem
import com.example.model.NexaLanguage
import com.example.model.NexaMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

import com.example.model.AppPlugin
import com.example.model.OfflineNote

class NexaRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nexa_ai_prefs", Context.MODE_PRIVATE)

    private val _historyFlow = MutableStateFlow<List<ActivityItem>>(emptyList())
    val historyFlow: StateFlow<List<ActivityItem>> = _historyFlow.asStateFlow()

    private val _offlineForceEnabledFlow = MutableStateFlow(
        prefs.getBoolean("nexa_force_offline_mode", false)
    )
    val offlineForceEnabledFlow: StateFlow<Boolean> = _offlineForceEnabledFlow.asStateFlow()

    private val _offlineNotesFlow = MutableStateFlow<List<OfflineNote>>(emptyList())
    val offlineNotesFlow: StateFlow<List<OfflineNote>> = _offlineNotesFlow.asStateFlow()

    private val _languageFlow = MutableStateFlow(NexaLanguage.EN)
    val languageFlow: StateFlow<NexaLanguage> = _languageFlow.asStateFlow()

    private val _modeFlow = MutableStateFlow(NexaMode.NATURAL)
    val modeFlow: StateFlow<NexaMode> = _modeFlow.asStateFlow()

    private val _customModePrompt = MutableStateFlow("")
    val customModePrompt: StateFlow<String> = _customModePrompt.asStateFlow()

    private val _geminiKeyFlow = MutableStateFlow(
        prefs.getString("nexa_gemini_key", "")?.trim() ?: ""
    )
    val geminiKeyFlow: StateFlow<String> = _geminiKeyFlow.asStateFlow()

    private val _groqKeyFlow = MutableStateFlow(
        prefs.getString("nexa_groq_key", "")?.trim() ?: ""
    )
    val groqKeyFlow: StateFlow<String> = _groqKeyFlow.asStateFlow()

    private val _pluginsFlow = MutableStateFlow<List<AppPlugin>>(emptyList())
    val pluginsFlow: StateFlow<List<AppPlugin>> = _pluginsFlow.asStateFlow()

    private val _selectedVoiceIdFlow = MutableStateFlow(
        prefs.getString("nexa_selected_voice_id", "3vl8X3o2mxL44uBIHGkS") ?: "3vl8X3o2mxL44uBIHGkS"
    )
    val selectedVoiceIdFlow: StateFlow<String> = _selectedVoiceIdFlow.asStateFlow()

    private val _elevenLabsKeyFlow = MutableStateFlow(
        prefs.getString("nexa_elevenlabs_key", "")?.trim() ?: ""
    )
    val elevenLabsKeyFlow: StateFlow<String> = _elevenLabsKeyFlow.asStateFlow()

    private val _backgroundServiceEnabledFlow = MutableStateFlow(
        prefs.getBoolean("nexa_bg_service_enabled", true)
    )
    val backgroundServiceEnabledFlow: StateFlow<Boolean> = _backgroundServiceEnabledFlow.asStateFlow()

    private val _autoWakeOnScreenOnFlow = MutableStateFlow(
        prefs.getBoolean("nexa_auto_wake_screen_on", true)
    )
    val autoWakeOnScreenOnFlow: StateFlow<Boolean> = _autoWakeOnScreenOnFlow.asStateFlow()

    private val _autoLaunchOnScreenWakeFlow = MutableStateFlow(
        prefs.getBoolean("nexa_auto_launch_screen_wake", false)
    )
    val autoLaunchOnScreenWakeFlow: StateFlow<Boolean> = _autoLaunchOnScreenWakeFlow.asStateFlow()

    init {
        loadLanguage()
        loadMode()
        loadHistory()
        loadPlugins()
        loadOfflineNotes()
    }

    fun getGeminiApiKey(): String {
        val userSaved = _geminiKeyFlow.value.trim()
        if (userSaved.isNotBlank()) return userSaved
        return try {
            val buildConfigKey = com.example.BuildConfig.GEMINI_API_KEY.trim()
            if (buildConfigKey.isNotBlank() && !buildConfigKey.equals("MY_GEMINI_API_KEY", ignoreCase = true)) {
                buildConfigKey
            } else {
                ""
            }
        } catch (_: Exception) {
            ""
        }
    }

    fun setGeminiApiKey(key: String) {
        val trimmed = key.trim()
        prefs.edit().putString("nexa_gemini_key", trimmed).apply()
        _geminiKeyFlow.value = trimmed
    }

    fun clearGeminiApiKey() {
        prefs.edit().remove("nexa_gemini_key").apply()
        _geminiKeyFlow.value = ""
    }

    fun getGroqApiKey(): String {
        return _groqKeyFlow.value
    }

    fun setGroqApiKey(key: String) {
        val trimmed = key.trim()
        prefs.edit().putString("nexa_groq_key", trimmed).apply()
        _groqKeyFlow.value = trimmed
    }

    fun clearGroqApiKey() {
        prefs.edit().remove("nexa_groq_key").apply()
        _groqKeyFlow.value = ""
    }

    fun getSelectedVoiceId(): String = _selectedVoiceIdFlow.value

    fun setSelectedVoiceId(voiceId: String) {
        prefs.edit().putString("nexa_selected_voice_id", voiceId).apply()
        _selectedVoiceIdFlow.value = voiceId
    }

    fun getElevenLabsApiKey(): String = _elevenLabsKeyFlow.value

    fun setElevenLabsApiKey(key: String) {
        val trimmed = key.trim()
        prefs.edit().putString("nexa_elevenlabs_key", trimmed).apply()
        _elevenLabsKeyFlow.value = trimmed
    }

    fun clearElevenLabsApiKey() {
        prefs.edit().remove("nexa_elevenlabs_key").apply()
        _elevenLabsKeyFlow.value = ""
    }

    fun isBackgroundServiceEnabled(): Boolean = _backgroundServiceEnabledFlow.value

    fun setBackgroundServiceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("nexa_bg_service_enabled", enabled).apply()
        _backgroundServiceEnabledFlow.value = enabled
    }

    fun isAutoWakeOnScreenOn(): Boolean = _autoWakeOnScreenOnFlow.value

    fun setAutoWakeOnScreenOn(enabled: Boolean) {
        prefs.edit().putBoolean("nexa_auto_wake_screen_on", enabled).apply()
        _autoWakeOnScreenOnFlow.value = enabled
    }

    fun isAutoLaunchOnScreenWake(): Boolean = _autoLaunchOnScreenWakeFlow.value

    fun setAutoLaunchOnScreenWake(enabled: Boolean) {
        prefs.edit().putBoolean("nexa_auto_launch_screen_wake", enabled).apply()
        _autoLaunchOnScreenWakeFlow.value = enabled
    }

    private fun loadLanguage() {
        val code = prefs.getString("nexa_lang", NexaLanguage.EN.code) ?: NexaLanguage.EN.code
        _languageFlow.value = NexaLanguage.fromCode(code)
    }

    fun setLanguage(lang: NexaLanguage) {
        prefs.edit().putString("nexa_lang", lang.code).apply()
        _languageFlow.value = lang
    }

    private fun loadMode() {
        val modeId = prefs.getString("nexa_mode_id", NexaMode.NATURAL.id) ?: NexaMode.NATURAL.id
        _modeFlow.value = NexaMode.fromId(modeId)
        _customModePrompt.value = prefs.getString(
            "nexa_custom_mode_prompt",
            "You are NEXA AI, an empathetic cybernetic companion who speaks poetically with philosophical insights."
        ) ?: ""
    }

    fun setMode(mode: NexaMode) {
        prefs.edit().putString("nexa_mode_id", mode.id).apply()
        _modeFlow.value = mode
    }

    fun setCustomModePrompt(prompt: String) {
        prefs.edit().putString("nexa_custom_mode_prompt", prompt.trim()).apply()
        _customModePrompt.value = prompt.trim()
    }

    fun getEffectiveSystemPrompt(): String {
        return when (_modeFlow.value) {
            NexaMode.CUSTOM -> {
                val custom = _customModePrompt.value
                if (custom.isNotBlank()) custom else NexaMode.CUSTOM.defaultSystemPrompt
            }
            else -> _modeFlow.value.defaultSystemPrompt
        }
    }

    private fun loadHistory() {
        val raw = prefs.getString("nexa_history_json", "[]") ?: "[]"
        val list = mutableListOf<ActivityItem>()
        try {
            val jsonArray = JSONArray(raw)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    ActivityItem(
                        id = obj.optString("id"),
                        type = obj.optString("type"),
                        detail = obj.optString("detail"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {
        }
        _historyFlow.value = list
    }

    fun addHistory(type: String, detail: String) {
        val current = _historyFlow.value.toMutableList()
        val item = ActivityItem(type = type, detail = detail)
        current.add(0, item)
        val trimmed = if (current.size > 30) current.take(30) else current
        _historyFlow.value = trimmed

        try {
            val jsonArray = JSONArray()
            for (it in trimmed) {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("type", it.type)
                obj.put("detail", it.detail)
                obj.put("timestamp", it.timestamp)
                jsonArray.put(obj)
            }
            prefs.edit().putString("nexa_history_json", jsonArray.toString()).apply()
        } catch (_: Exception) {
        }
    }

    fun clearHistory() {
        _historyFlow.value = emptyList()
        prefs.edit().remove("nexa_history_json").apply()
    }

    private fun loadPlugins() {
        val rawJson = prefs.getString("nexa_user_plugins_v2_json", null)
        if (rawJson != null) {
            try {
                val array = JSONArray(rawJson)
                val list = mutableListOf<AppPlugin>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val actionsArray = obj.optJSONArray("supportedActions")
                    val actions = mutableListOf<String>()
                    if (actionsArray != null) {
                        for (j in 0 until actionsArray.length()) {
                            actions.add(actionsArray.getString(j))
                        }
                    }
                    list.add(
                        AppPlugin(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            packageName = obj.getString("packageName"),
                            category = obj.getString("category"),
                            isConnected = obj.optBoolean("isConnected", true),
                            description = obj.optString("description", ""),
                            supportedActions = actions,
                            quickCommand = obj.optString("quickCommand", ""),
                            iconType = obj.optString("iconType", "generic"),
                            accountName = obj.optString("accountName", ""),
                            accountEmail = obj.optString("accountEmail", ""),
                            accountPassword = obj.optString("accountPassword", ""),
                            connectedAt = obj.optLong("connectedAt", System.currentTimeMillis())
                        )
                    )
                }
                _pluginsFlow.value = list
                return
            } catch (_: Exception) {
            }
        }
        // User starts with empty plugins list so they add only what they want
        _pluginsFlow.value = emptyList()
    }

    fun togglePluginConnection(pluginId: String): Boolean {
        val current = _pluginsFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == pluginId }
        if (index != -1) {
            val item = current[index]
            val updated = item.copy(isConnected = !item.isConnected)
            current[index] = updated
            _pluginsFlow.value = current
            savePluginsToPrefs(current)
            return updated.isConnected
        }
        return false
    }

    fun addPlugin(plugin: AppPlugin) {
        val current = _pluginsFlow.value.toMutableList()
        current.removeAll { it.id == plugin.id }
        current.add(0, plugin)
        _pluginsFlow.value = current
        savePluginsToPrefs(current)
    }

    fun removePlugin(pluginId: String) {
        val current = _pluginsFlow.value.toMutableList()
        current.removeAll { it.id == pluginId }
        _pluginsFlow.value = current
        savePluginsToPrefs(current)
    }

    fun updatePluginCredentials(pluginId: String, name: String, email: String, pass: String) {
        val current = _pluginsFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == pluginId }
        if (index != -1) {
            val item = current[index]
            val updated = item.copy(
                accountName = name,
                accountEmail = email,
                accountPassword = pass
            )
            current[index] = updated
            _pluginsFlow.value = current
            savePluginsToPrefs(current)
        }
    }

    private fun savePluginsToPrefs(list: List<AppPlugin>) {
        try {
            val array = JSONArray()
            for (p in list) {
                val obj = JSONObject()
                obj.put("id", p.id)
                obj.put("name", p.name)
                obj.put("packageName", p.packageName)
                obj.put("category", p.category)
                obj.put("isConnected", p.isConnected)
                obj.put("description", p.description)
                val actArray = JSONArray()
                for (a in p.supportedActions) {
                    actArray.put(a)
                }
                obj.put("supportedActions", actArray)
                obj.put("quickCommand", p.quickCommand)
                obj.put("iconType", p.iconType)
                obj.put("accountName", p.accountName)
                obj.put("accountEmail", p.accountEmail)
                obj.put("accountPassword", p.accountPassword)
                obj.put("connectedAt", p.connectedAt)
                array.put(obj)
            }
            prefs.edit().putString("nexa_user_plugins_v2_json", array.toString()).apply()
        } catch (_: Exception) {
        }
    }

    companion object {
        fun getAvailableAppCatalog(): List<AppPlugin> {
            return listOf(
                AppPlugin(
                    id = "facebook",
                    name = "Facebook",
                    packageName = "com.facebook.katana",
                    category = "Social",
                    description = "Automate social feed posts, auto-generate captions, and auto-like feeds.",
                    supportedActions = listOf("Auto-Post Status", "Generate AI Caption", "Auto-Like Screen", "Comment on Feed"),
                    quickCommand = "Facebook e post koro",
                    iconType = "facebook"
                ),
                AppPlugin(
                    id = "instagram",
                    name = "Instagram",
                    packageName = "com.instagram.android",
                    category = "Social",
                    description = "Share AI-written reels captions, auto-like posts, and follow creators.",
                    supportedActions = listOf("Post Reel / Feed", "Auto-Hashtag Caption", "Auto-Like Reel", "Follow Creator"),
                    quickCommand = "Instagram e post koro",
                    iconType = "instagram"
                ),
                AppPlugin(
                    id = "youtube",
                    name = "YouTube",
                    packageName = "com.google.android.youtube",
                    category = "Media",
                    description = "Live search videos, play music, auto-like and subscribe channels autonomously.",
                    supportedActions = listOf("Live Video Search", "Auto-Subscribe Channel", "Like Video", "Post Comment"),
                    quickCommand = "YouTube e gan search koro",
                    iconType = "youtube"
                ),
                AppPlugin(
                    id = "whatsapp",
                    name = "WhatsApp",
                    packageName = "com.whatsapp",
                    category = "Messaging",
                    description = "Send instant messages, voice notes, and autonomous message replies.",
                    supportedActions = listOf("Send Instant Message", "Voice Note SMS", "Auto Reply to Contact"),
                    quickCommand = "WhatsApp message pathao",
                    iconType = "whatsapp"
                ),
                AppPlugin(
                    id = "messenger",
                    name = "Facebook Messenger",
                    packageName = "com.facebook.orca",
                    category = "Messaging",
                    description = "Direct chat dispatch, read unread messages, and voice chat.",
                    supportedActions = listOf("Direct Chat Dispatch", "Voice Message", "Open Conversations"),
                    quickCommand = "Messenger e reply koro",
                    iconType = "messenger"
                ),
                AppPlugin(
                    id = "googledocs",
                    name = "Google Docs",
                    packageName = "com.google.android.apps.docs.editors.docs",
                    category = "Productivity",
                    description = "Create new documents, transcribe voice notes, and edit drafts.",
                    supportedActions = listOf("Create New Document", "Voice Note Dictation", "Export AI Summary"),
                    quickCommand = "New Google Doc kholo",
                    iconType = "docs"
                ),
                AppPlugin(
                    id = "googlesheets",
                    name = "Google Sheets & Excel",
                    packageName = "com.google.android.apps.docs.editors.sheets",
                    category = "Productivity",
                    description = "Create spreadsheets, tabular budgets, and formulas instantly.",
                    supportedActions = listOf("Create Spreadsheet", "Log Expenses Table", "Insert Formulas"),
                    quickCommand = "Google Sheet banao",
                    iconType = "sheets"
                ),
                AppPlugin(
                    id = "googleslides",
                    name = "Google Slides",
                    packageName = "com.google.android.apps.docs.editors.slides",
                    category = "Productivity",
                    description = "Draft presentation outlines and create new presentation decks.",
                    supportedActions = listOf("Create Slide Deck", "Presentation Outline", "Speaker Notes"),
                    quickCommand = "Presentation slide kholo",
                    iconType = "slides"
                ),
                AppPlugin(
                    id = "spotify",
                    name = "Spotify Music",
                    packageName = "com.spotify.music",
                    category = "Media",
                    description = "Control audio playback, search songs, and manage playlists.",
                    supportedActions = listOf("Play Music", "Pause/Resume", "Search Artist/Song"),
                    quickCommand = "Spotify te music chalao",
                    iconType = "spotify"
                ),
                AppPlugin(
                    id = "telegram",
                    name = "Telegram",
                    packageName = "org.telegram.messenger",
                    category = "Messaging",
                    description = "Send instant voice notes, messages, and channel announcements.",
                    supportedActions = listOf("Open Telegram", "Send Voice Note", "Quick Chat"),
                    quickCommand = "Open Telegram",
                    iconType = "telegram"
                ),
                AppPlugin(
                    id = "twitter",
                    name = "Twitter / X",
                    packageName = "com.twitter.android",
                    category = "Social",
                    description = "Post viral tweets, draft threads, and auto-engage feeds.",
                    supportedActions = listOf("Post Tweet", "AI Thread Writer", "Auto-Like Tweet"),
                    quickCommand = "Twitter e post koro",
                    iconType = "twitter"
                ),
                AppPlugin(
                    id = "tiktok",
                    name = "TikTok",
                    packageName = "com.zhiliaoapp.musically",
                    category = "Social",
                    description = "Browse trending reels, generate viral captions, and like videos.",
                    supportedActions = listOf("Open TikTok", "Viral Caption Writer", "Auto-Like Reel"),
                    quickCommand = "TikTok kholo",
                    iconType = "tiktok"
                ),
                AppPlugin(
                    id = "chrome",
                    name = "Google Chrome",
                    packageName = "com.android.chrome",
                    category = "Browser",
                    description = "Perform live Google web searches and read real-time news.",
                    supportedActions = listOf("Live Web Search", "Open URL", "Read Web News"),
                    quickCommand = "Google live search koro",
                    iconType = "chrome"
                ),
                AppPlugin(
                    id = "phone",
                    name = "Phone & Dialer",
                    packageName = "com.google.android.dialer",
                    category = "Communication",
                    description = "Autonomous voice calling, contact management, and quick SMS dispatch.",
                    supportedActions = listOf("Voice Dial Contact", "Call Management", "SMS Dispatch"),
                    quickCommand = "Call 1234567890",
                    iconType = "phone"
                ),
                AppPlugin(
                    id = "system",
                    name = "Device Hardware & Screen",
                    packageName = "android.hardware",
                    category = "System",
                    description = "Direct hardware automation: Torch light, Wi-Fi toggles, and Screen Clicks.",
                    supportedActions = listOf("Toggle Torch ON/OFF", "Open Wi-Fi Settings", "Simulate Screen Tap", "Open Notifications"),
                    quickCommand = "Torch on koro",
                    iconType = "system"
                )
            )
        }
    }

    // ==========================================
    // OFFLINE MODE & OFFLINE NOTES
    // ==========================================
    fun isOfflineModeForced(): Boolean = _offlineForceEnabledFlow.value

    fun setOfflineModeForced(enabled: Boolean) {
        prefs.edit().putBoolean("nexa_force_offline_mode", enabled).apply()
        _offlineForceEnabledFlow.value = enabled
    }

    private fun loadOfflineNotes() {
        val rawJson = prefs.getString("nexa_offline_notes_json", "[]") ?: "[]"
        val notes = mutableListOf<OfflineNote>()
        try {
            val array = JSONArray(rawJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                notes.add(
                    OfflineNote(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        title = obj.optString("title", "Note"),
                        content = obj.optString("content", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}
        _offlineNotesFlow.value = notes
    }

    fun addOfflineNote(title: String, content: String): OfflineNote {
        val note = OfflineNote(
            title = if (title.isNotBlank()) title else "Quick Note",
            content = content
        )
        val current = _offlineNotesFlow.value.toMutableList()
        current.add(0, note)
        _offlineNotesFlow.value = current
        saveOfflineNotes(current)
        return note
    }

    fun deleteOfflineNote(id: String) {
        val filtered = _offlineNotesFlow.value.filter { it.id != id }
        _offlineNotesFlow.value = filtered
        saveOfflineNotes(filtered)
    }

    fun clearAllOfflineNotes() {
        _offlineNotesFlow.value = emptyList()
        prefs.edit().remove("nexa_offline_notes_json").apply()
    }

    private fun saveOfflineNotes(notes: List<OfflineNote>) {
        try {
            val array = JSONArray()
            for (n in notes) {
                val obj = JSONObject().apply {
                    put("id", n.id)
                    put("title", n.title)
                    put("content", n.content)
                    put("timestamp", n.timestamp)
                }
                array.put(obj)
            }
            prefs.edit().putString("nexa_offline_notes_json", array.toString()).apply()
        } catch (_: Exception) {}
    }
}
