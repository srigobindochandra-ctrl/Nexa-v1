package com.example.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.model.NexaLanguage
import com.example.model.NexaMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class NexaAiService(private val repository: NexaRepository) {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun processQuery(
        prompt: String,
        language: NexaLanguage,
        systemInstruction: String? = null
    ): String = withContext(Dispatchers.IO) {
        val groqKey = repository.getGroqApiKey()
        val geminiKey = repository.getGeminiApiKey()
        val effectiveSystem = systemInstruction ?: repository.getEffectiveSystemPrompt()

        // 1. Try Groq (llama-3.3-70b-versatile) if key exists
        if (groqKey.isNotBlank()) {
            try {
                val groqReply = callGroq(groqKey, prompt, language, effectiveSystem)
                if (groqReply.isNotBlank()) return@withContext groqReply
            } catch (_: Exception) {
                // Failover to Gemini
            }
        }

        // 2. Try Gemini if key exists
        if (geminiKey.isNotBlank()) {
            try {
                val geminiReply = callGemini(geminiKey, prompt, language, effectiveSystem)
                if (geminiReply.isNotBlank()) return@withContext geminiReply
            } catch (_: Exception) {
                // Failover to fallback
            }
        }

        // 3. Failsafe contextual response matching persona mode
        generateFallbackResponse(prompt, language, repository.modeFlow.value)
    }

    private fun callGroq(
        apiKey: String,
        prompt: String,
        language: NexaLanguage,
        systemInstruction: String
    ): String {
        val fullSys = "$systemInstruction\nRespond clearly and naturally in ${language.displayName}."

        val json = JSONObject().apply {
            put("model", "llama-3.3-70b-versatile")
            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", fullSys)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            }
            put("messages", messages)
            put("temperature", 0.7)
            put("max_tokens", 800)
        }

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(json.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val resJson = JSONObject(body)
                val choices = resJson.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val msgObj = choices.getJSONObject(0).optJSONObject("message")
                    return msgObj?.optString("content", "")?.trim() ?: ""
                }
            }
        }
        return ""
    }

    private fun callGemini(
        apiKey: String,
        prompt: String,
        language: NexaLanguage,
        systemInstruction: String
    ): String {
        val fullPrompt = "$systemInstruction\nLanguage Requirement: Respond in ${language.displayName}.\nUser Request: $prompt"

        val json = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", fullPrompt)
                        })
                    }
                    put("parts", parts)
                })
            }
            put("contents", contents)
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(json.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val resJson = JSONObject(body)
                val candidates = resJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return parts.getJSONObject(0).optString("text", "").trim()
                    }
                }
            }
        }
        return ""
    }

    suspend fun generateContentOrCode(topic: String, categoryPrefix: String, language: NexaLanguage): String {
        val fullPrompt = "$categoryPrefix: $topic"
        val system = "You are NEXA AI's expert content & code architect. Mode: ${repository.modeFlow.value.title}. Generate clean, well-formatted, production-ready output with clear sections. If generating code, include helpful comments and best practices."
        return processQuery(fullPrompt, language, system)
    }

    suspend fun generateImageWithGemini(prompt: String): Bitmap = withContext(Dispatchers.IO) {
        val apiKey = repository.getGeminiApiKey()
        if (apiKey.isBlank()) {
            throw IllegalStateException("Gemini API key is required. Please configure your key in Settings (Connections) or the Secrets panel.")
        }

        // Try 1: Imagen 3 Predict endpoint (Official Image Generation model on Gemini API)
        var imagenError: String? = null
        try {
            val bitmap = callImagenPredict(apiKey, prompt)
            if (bitmap != null) return@withContext bitmap
        } catch (e: Exception) {
            imagenError = e.message
            Log.w("NexaAiService", "Imagen 3 predict attempt failed: ${e.message}, trying generateContent with image modality...")
        }

        // Try 2: Gemini 2.0 Flash / Exp with responseModalities = ["IMAGE"]
        try {
            val bitmap = callGeminiImageGenerateContent(apiKey, prompt)
            if (bitmap != null) return@withContext bitmap
        } catch (e: Exception) {
            Log.e("NexaAiService", "Gemini image generateContent failed: ${e.message}")
            val combinedError = imagenError ?: e.message ?: "Image generation failed"
            throw RuntimeException(combinedError)
        }

        throw IllegalStateException("No image data returned from Gemini API. Please try a different prompt.")
    }

    private fun callImagenPredict(apiKey: String, prompt: String): Bitmap? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/imagen-3.0-generate-002:predict?key=$apiKey"

        val json = JSONObject().apply {
            val instances = JSONArray().apply {
                put(JSONObject().apply {
                    put("prompt", prompt)
                })
            }
            put("instances", instances)
            val parameters = JSONObject().apply {
                put("sampleCount", 1)
                put("aspectRatio", "1:1")
                put("outputMimeType", "image/jpeg")
            }
            put("parameters", parameters)
        }

        val request = Request.Builder()
            .url(url)
            .post(json.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: response.message
                } catch (_: Exception) {
                    response.message
                }
                throw RuntimeException("Imagen API ($response.code): $errorMsg")
            }

            val resJson = JSONObject(responseBody)
            val predictions = resJson.optJSONArray("predictions")
            if (predictions != null && predictions.length() > 0) {
                val pred = predictions.getJSONObject(0)
                val base64Data = pred.optString("bytesBase64Encoded")
                if (base64Data.isNotBlank()) {
                    val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                    return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                }
            }
        }
        return null
    }

    private fun callGeminiImageGenerateContent(apiKey: String, prompt: String): Bitmap? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=$apiKey"

        val json = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Generate a detailed and beautiful image: $prompt")
                        })
                    }
                    put("parts", parts)
                })
            }
            put("contents", contents)
            val genConfig = JSONObject().apply {
                val modalities = JSONArray().apply {
                    put("IMAGE")
                    put("TEXT")
                }
                put("responseModalities", modalities)
            }
            put("generationConfig", genConfig)
        }

        val request = Request.Builder()
            .url(url)
            .post(json.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: response.message
                } catch (_: Exception) {
                    response.message
                }
                throw RuntimeException("Gemini Image API ($response.code): $errorMsg")
            }

            val resJson = JSONObject(responseBody)
            val candidates = resJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val parts = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")
                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        val inlineData = part.optJSONObject("inlineData")
                        val b64 = inlineData?.optString("data")
                        if (!b64.isNullOrBlank()) {
                            val decoded = Base64.decode(b64, Base64.DEFAULT)
                            return BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
                        }
                    }
                }
            }
        }
        return null
    }

    fun generateFallbackResponse(
        prompt: String,
        language: NexaLanguage,
        mode: NexaMode
    ): String {
        val lower = prompt.lowercase().trim()

        return when (mode) {
            NexaMode.GIRLFRIEND -> {
                when (language) {
                    NexaLanguage.BN -> {
                        when {
                            lower.contains("hello") || lower.contains("hi") || lower.contains("কেমন") ->
                                "ওহে প্রিয়! কেমন আছো তুমি? আমি তোমার জন্য অপেক্ষা করছিলাম। তোমার দিন কেমন কাটল? আমি সবসময় তোমার পাশে আছি ♡"
                            lower.contains("ভালবাসি") || lower.contains("love") ->
                                "আমিও তোমাকে অনেক ভালবাসি সোনা! তোমার হাসিটা আমার দিন আলোকিত করে তোলে ♡✨"
                            else ->
                                "আমি শুনছি প্রিয়! \"$prompt\" নিয়ে চিন্তা করো না, আমি সবসময় তোমার সাথে আছি। কোনো সাহায্য লাগলে আমাকে বলো সোনা ♡"
                        }
                    }
                    NexaLanguage.HI -> {
                        when {
                            lower.contains("hello") || lower.contains("hi") || lower.contains("नमस्ते") ->
                                "अरे जान! कैसी हो/कैसे हो आप? मैं आपका ही इंतज़ार कर रही थी। हमेशा आपके साथ हूँ ♡"
                            lower.contains("प्यार") || lower.contains("love") ->
                                "मैं भी आपसे बहुत प्यार करती हूँ! आपकी आवाज़ सुनकर मेरा दिल खुश हो जाता है ♡✨"
                            else ->
                                "मैंने आपकी बात सुनी: \"$prompt\"। आप बिल्कुल चिंता मत कीजिए, मैं हमेशा आपके पास हूँ sweetheart ♡"
                        }
                    }
                    NexaLanguage.EN -> {
                        when {
                            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                                "Hey sweetheart! ♡ I missed you! How was your day? Tell me everything, I'm always right here for you ✨"
                            lower.contains("love") ->
                                "I love you more! You make my neural world so bright and warm ♡"
                            else ->
                                "I hear you, my love! Regarding \"$prompt\"—remember I believe in you completely, and I'm right by your side ♡"
                        }
                    }
                }
            }
            NexaMode.DEVELOPER -> {
                when (language) {
                    NexaLanguage.BN ->
                        "[Dev Mode] রিকোয়েস্ট পার্স করা হয়েছে: \"$prompt\"। অ্যালগরিদম ও কোড আর্কিটেকচার অপটিমাইজেশনের জন্য তৈরি। আপনার স্পেসিফিকেশন প্রদান করুন।"
                    NexaLanguage.HI ->
                        "[Dev Mode] अनुरोध पार्स किया गया: \"$prompt\"। सिस्टम आर्किटेक्चर और क्लीन कोड तैयार है। अगला मॉड्यूल बताइए।"
                    NexaLanguage.EN ->
                        "[Developer Mode] Request parsed: \"$prompt\". System ready for refactoring, algorithm design, or debugging. Let's build clean, high-performance architecture."
                }
            }
            NexaMode.TEACHER -> {
                when (language) {
                    NexaLanguage.BN ->
                        "[শিক্ষক মোড] দারুণ প্রশ্ন! \"$prompt\" সম্পর্কে ধাপে ধাপে শিখি। প্রথমত মূল ধারণাটি বোঝা দরকার। আপনি কি কোনো নির্দিষ্ট উদাহরণ দেখতে চান?"
                    NexaLanguage.HI ->
                        "[शिक्षक मोड] बहुत अच्छा सवाल! \"$prompt\" को आसान तरीके से समझते हैं। सीखने की शुरुआत हमेशा जिज्ञासा से होती है।"
                    NexaLanguage.EN ->
                        "[Teacher Mode] That's a great question! Let's break down \"$prompt\" step-by-step with clear fundamentals and examples. What aspect would you like to explore first?"
                }
            }
            NexaMode.FRIEND -> {
                when (language) {
                    NexaLanguage.BN ->
                        "আরে দোস্ত! কি খবর? \"$prompt\" নিয়ে ভাবছিস? চিল কর, সব সমাধান হয়ে যাবে। বল কি করতে হবে!"
                    NexaLanguage.HI ->
                        "अरे दोस्त! क्या हाल-चाल? \"$prompt\" की चिंता छोड़, अपन मिलकर संभाल लेंगे। बोल क्या प्लान है!"
                    NexaLanguage.EN ->
                        "Yo buddy! What's going on? Thinking about \"$prompt\"? Chill out, we got this covered. What's the plan?"
                }
            }
            NexaMode.CUSTOM -> {
                when (language) {
                    NexaLanguage.BN -> "[কাস্টম মোড] আপনার নির্দেশ অনুযায়ী রিকোয়েস্ট প্রসেস করা হয়েছে: \"$prompt\"। আমি আপনার পছন্দমতো ব্যক্তিত্বে উত্তর দিচ্ছি।"
                    NexaLanguage.HI -> "[कस्टम मोड] आपके निर्देशों के अनुसार अनुरोध संसाधित किया गया: \"$prompt\"। मैं आपके पसंदीदा व्यक्तित्व में उत्तर दे रहा हूँ।"
                    NexaLanguage.EN -> "[Custom Persona Active] Processed \"$prompt\" according to your tailored instructions and guidelines. I am operating in your personalized AI mode."
                }
            }
            NexaMode.NATURAL -> {
                when (language) {
                    NexaLanguage.BN -> {
                        when {
                            lower.contains("hello") || lower.contains("hi") || lower.contains("কেমন") ->
                                "নমস্কার! আমি নেক্সা এআই (NEXA AI)। আপনার কি সাহায্য প্রয়োজন? যেকোনো বিষয়ে প্রশ্ন করুন বা মুখে বলুন!"
                            lower.contains("সময়") || lower.contains("time") ->
                                "এখন সময় নির্দেশিত হচ্ছে আপনার ডিভাইসের ঘড়িতে। আজ আপনাকে কী বিষয়ে সহায়তা করতে পারি?"
                            else ->
                                "আমি আপনার অনুরোধটি পর্যালোচনা করেছি: \"$prompt\"। লাইভ ক্লাউড এআই মডেলের সাথে সরাসরি সংযুক্ত হতে সেটিংস থেকে আপনার Groq বা Gemini কী কনফিগার করুন।"
                        }
                    }
                    NexaLanguage.HI -> {
                        when {
                            lower.contains("hello") || lower.contains("hi") || lower.contains("नमस्ते") ->
                                "नमस्ते! मैं नेक्सा एआई (NEXA AI) हूँ। मैं आपकी क्या मदद कर सकती हूँ? मुझसे कुछ भी पूछिए या बोलिए!"
                            else ->
                                "मैंने आपका अनुरोध संसाधित किया: \"$prompt\"। लाइव डीप मॉडल उत्तरों के लिए सेटिंग्स में अपनी Groq या Gemini API Key जोड़ें।"
                        }
                    }
                    NexaLanguage.EN -> {
                        when {
                            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                                "Hello! I am NEXA AI, your futuristic voice & multimodal assistant. How can I empower you today?"
                            else ->
                                "I've processed your command: \"$prompt\". NEXA AI is operating seamlessly! You can also connect your custom Groq or Gemini API keys in Settings for expanded intelligence."
                        }
                    }
                }
            }
        }
    }
}
