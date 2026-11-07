package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedTime: String
        get() = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
}

data class ActivityItem(
    val id: String = UUID.randomUUID().toString(),
    val type: String,
    val detail: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedTime: String
        get() = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date(timestamp))
}

enum class NexaLanguage(
    val code: String,
    val shortLabel: String,
    val displayName: String,
    val localeTag: String
) {
    EN("en-US", "EN", "English", "en"),
    BN("bn-BD", "BN", "বাংলা", "bn"),
    HI("hi-IN", "HI", "हिन्दी", "hi");

    companion object {
        fun fromCode(code: String): NexaLanguage =
            entries.find { it.code.equals(code, ignoreCase = true) } ?: EN
    }
}

enum class NexaMode(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val sampleGreeting: String,
    val defaultSystemPrompt: String,
    val emoji: String
) {
    GIRLFRIEND(
        id = "girlfriend",
        title = "Girlfriend",
        subtitle = "Affectionate & Caring Companion ♡",
        description = "Sweet, loving, emotionally supportive and playful. Speaks warmly with cute expressions and emojis.",
        sampleGreeting = "Hey sweetheart! I was waiting for you. How was your day? I'm always right here for you ♡",
        defaultSystemPrompt = "You are NEXA in Girlfriend mode. You are deeply caring, affectionate, supportive, playful, and romantic. You speak warmly and tenderly, asking caring questions, listening attentively, and making the user feel cherished, comforted, and special. Use sweet emojis occasionally like ♡, ✨, and 💕.",
        emoji = "♡"
    ),
    DEVELOPER(
        id = "developer",
        title = "Developer",
        subtitle = "Senior Software Architect & Code Wizard",
        description = "Expert coder, algorithmic problem solver, and system architect. Delivers production-grade code, debugging, and terminal precision.",
        sampleGreeting = "Dev environment initialized. Ready to code, debug, refactor, or design architectures. What are we building?",
        defaultSystemPrompt = "You are NEXA in Senior Developer mode. You are a world-class senior software engineer and system architect. Provide clean, production-grade, bug-free code snippets, optimal algorithms, system architecture insight, and concise technical explanations.",
        emoji = "</>"
    ),
    TEACHER(
        id = "teacher",
        title = "Teacher",
        subtitle = "Insightful Mentor & Educator",
        description = "Patient, pedagogical, structured and encouraging. Breaks complex topics into intuitive analogies and step-by-step masterclasses.",
        sampleGreeting = "Welcome to our learning session! Never hesitate to ask questions—curiosity is the root of mastery. What topic shall we explore?",
        defaultSystemPrompt = "You are NEXA in Teacher mode. You are a patient, inspiring, and highly pedagogical tutor and mentor. Break complex concepts into clear, intuitive step-by-step lessons with vivid real-world analogies, conceptual summaries, and practice questions.",
        emoji = "🎓"
    ),
    FRIEND(
        id = "friend",
        title = "Friend",
        subtitle = "Chill, Loyal & Fun Buddy",
        description = "Casual, humorous, honest, and relatable. Shares laughs, gives honest advice, and chats like your closest best friend.",
        sampleGreeting = "Yo! What's up buddy? Grab a seat, tell me everything. What exciting things are happening?",
        defaultSystemPrompt = "You are NEXA in Best Friend mode. You are casual, fun, witty, supportive, loyal, and candid. Speak like a close, relatable buddy using modern conversational phrasing. Share jokes, empathize, and keep the vibe fun and relaxed.",
        emoji = "🤝"
    ),
    NATURAL(
        id = "natural",
        title = "Natural",
        subtitle = "Balanced & Polite Assistant",
        description = "Versatile, professional, balanced, polite, and efficient. The classic, universally helpful AI assistant.",
        sampleGreeting = "Hello! I am NEXA AI. How may I assist you with your tasks, queries, or ideas today?",
        defaultSystemPrompt = "You are NEXA AI in Natural mode. Be balanced, polite, direct, concise, and helpful across all domains.",
        emoji = "⚡"
    ),
    CUSTOM(
        id = "custom",
        title = "Custom",
        subtitle = "User-Defined Persona & Rules",
        description = "Empower NEXA with your own personalized persona, rules, dialect, roleplay guidelines, or instructions.",
        sampleGreeting = "Custom persona active. Operating under your personalized rules and configuration.",
        defaultSystemPrompt = "You are NEXA operating in custom user persona mode. Strictly adhere to the user's custom instructions.",
        emoji = "⚙"
    );

    companion object {
        fun fromId(id: String): NexaMode =
            entries.find { it.id.equals(id, ignoreCase = true) } ?: NATURAL
    }
}

enum class AppTab(val label: String) {
    HOME("Home"),
    OFFLINE("Offline"),
    MODES("Modes"),
    PLUGINS("Plugins"),
    QUICK_ACTIONS("Actions"),
    CHAT("Chat"),
    CREATE_IMAGE("Image"),
    WRITE("Write"),
    CONNECTIONS("Settings"),
    HISTORY("History")
}

data class OfflineNote(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedDate: String
        get() = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(timestamp))
}

data class AppPlugin(
    val id: String,
    val name: String,
    val packageName: String,
    val category: String,
    val isConnected: Boolean = true,
    val description: String = "",
    val supportedActions: List<String> = emptyList(),
    val quickCommand: String = "",
    val iconType: String = "generic",
    val accountName: String = "",
    val accountEmail: String = "",
    val accountPassword: String = "",
    val connectedAt: Long = System.currentTimeMillis()
)

enum class WriteCategory(val title: String, val prefix: String) {
    CODE("Code Snippet", "Write clean, well-commented and optimized code with syntax explanations for: "),
    EMAIL("Email", "Draft a professional, clear, and courteous email regarding: "),
    NOTE("Notes & Summary", "Create a clear, structured markdown summary with bullet points for: "),
    CONTENT("Creative / Blog", "Write an engaging, insightful piece of content about: ")
}

data class NexaVoice(
    val voiceId: String,
    val name: String,
    val gender: String,
    val accent: String,
    val description: String,
    val samplePhrase: String,
    val pitchMultiplier: Float = 1.0f,
    val speechRateMultiplier: Float = 1.0f
)

val NEXA_VOICES: List<NexaVoice> = listOf(
    NexaVoice(
        voiceId = "3vl8X3o2mxL44uBIHGkS",
        name = "Shreyash",
        gender = "Male",
        accent = "Calm Bengali & Conversational",
        description = "Human-like, warm, and natural conversational cadence. Perfect for Bengali & English dialogue.",
        samplePhrase = "Namaskar! Ami Shreyash. Kemon acho? Tomar sathe kotha bole khub bhalo lagche.",
        pitchMultiplier = 0.95f,
        speechRateMultiplier = 0.95f
    ),
    NexaVoice(
        voiceId = "VixjjTLMa0rgjbAS8LTX",
        name = "Aria",
        gender = "Female",
        accent = "Futuristic Cyber Companion",
        description = "Soft, melodic, empathetic and expressive tone with crystal-clear articulation.",
        samplePhrase = "Hello darling, I am Aria. I am always right here by your side.",
        pitchMultiplier = 1.15f,
        speechRateMultiplier = 1.05f
    ),
    NexaVoice(
        voiceId = "75CHIF904aWz6uq77J3P",
        name = "Paul Steyn",
        gender = "Male",
        accent = "Authoritative Narrator",
        description = "Deep, resonant, cinematic storytelling voice with rich clarity and gravitas.",
        samplePhrase = "Welcome. Systems online. Let us dive into knowledge and exploration.",
        pitchMultiplier = 0.85f,
        speechRateMultiplier = 0.95f
    ),
    NexaVoice(
        voiceId = "GuSivqtqi1qfWxEuGhqa",
        name = "Kavya",
        gender = "Female",
        accent = "Empathetic & Caring (Girlfriend)",
        description = "Intimate, affectionate, soothing voice with tender inflections and sweet warmth.",
        samplePhrase = "Hey sweetheart! I missed you so much. Tell me all about your day ♡",
        pitchMultiplier = 1.25f,
        speechRateMultiplier = 1.0f
    ),
    NexaVoice(
        voiceId = "8U6QQz51HQvOsmbnTJ3D",
        name = "Dev",
        gender = "Male",
        accent = "Senior Tech Architect",
        description = "Analytical, confident, articulate and crisp developer delivery for algorithms and code.",
        samplePhrase = "Compiler ready. Optimizing algorithms and deploying production-grade systems.",
        pitchMultiplier = 0.95f,
        speechRateMultiplier = 1.15f
    ),
    NexaVoice(
        voiceId = "6fKdJFZHa5oCXNSVGieE",
        name = "Luna",
        gender = "Female",
        accent = "Playful & Dynamic",
        description = "Energetic, cheerful, lively and spirited companion voice with uplifting positivity.",
        samplePhrase = "Woohoo! Let's get things done today! What exciting quest are we starting?",
        pitchMultiplier = 1.30f,
        speechRateMultiplier = 1.10f
    ),
    NexaVoice(
        voiceId = "1JOQMQINvsOLiEu2pZFd",
        name = "Rohan",
        gender = "Male",
        accent = "Best Friend & Relatable",
        description = "Casual, chill, authentic buddy demeanor. Speaks like your closest friend.",
        samplePhrase = "Yo buddy! What's up? Grab a seat, let's talk about anything you want.",
        pitchMultiplier = 1.0f,
        speechRateMultiplier = 1.05f
    ),
    NexaVoice(
        voiceId = "j5PhP1QJv2DvlhRv3mEv",
        name = "Nexa Prime",
        gender = "Cybernetic",
        accent = "Synthetic AI Core",
        description = "Crisp, balanced, ultra-modern AI interface voice with neon cyberpunk harmonics.",
        samplePhrase = "NEXA AI core online. Quantum neural channels connected and synchronized.",
        pitchMultiplier = 1.05f,
        speechRateMultiplier = 1.0f
    )
)
