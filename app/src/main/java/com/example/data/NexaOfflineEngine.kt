package com.example.data

import com.example.model.NexaLanguage
import com.example.model.NexaMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class OfflineParseResult(
    val handled: Boolean,
    val replyText: String,
    val noteToSave: Pair<String, String>? = null, // title to content
    val automationQuery: String? = null
)

object NexaOfflineEngine {

    /**
     * Attempts to resolve the query locally using rule-based NLP, math evaluator,
     * unit converter, and offline knowledge.
     */
    fun processOffline(
        query: String,
        language: NexaLanguage,
        mode: NexaMode,
        batteryInfo: Pair<Int, Boolean>,
        storageInfo: String
    ): OfflineParseResult {
        val trimmed = query.trim()
        val lower = trimmed.lowercase()

        // 1. Math Calculation Check
        val mathResult = tryEvaluateMath(trimmed)
        if (mathResult != null) {
            val reply = when (language) {
                NexaLanguage.BN -> "⚡ অফলাইন ক্যালকুলেশন:\n$mathResult"
                NexaLanguage.HI -> "⚡ ऑफलाइन गणना:\n$mathResult"
                NexaLanguage.EN -> "⚡ Offline Math Evaluation:\n$mathResult"
            }
            return OfflineParseResult(handled = true, replyText = reply)
        }

        // 2. Unit Conversion Check
        val unitResult = tryConvertUnits(lower)
        if (unitResult != null) {
            val reply = when (language) {
                NexaLanguage.BN -> "⚡ অফলাইন একক রূপান্তর:\n$unitResult"
                NexaLanguage.HI -> "⚡ ऑफलाइन यूनिट रूपांतरण:\n$unitResult"
                NexaLanguage.EN -> "⚡ Offline Unit Conversion:\n$unitResult"
            }
            return OfflineParseResult(handled = true, replyText = reply)
        }

        // 3. Offline Note creation via chat
        // "note: ...", "নোট: ...", "নোট করো: ...", "মনে রাখো: ..."
        if (lower.startsWith("note:") || lower.startsWith("note down:") ||
            lower.startsWith("নোট:") || lower.startsWith("নোট করো:") ||
            lower.startsWith("মনে রাখো:") || lower.startsWith("save note:")
        ) {
            val noteBody = trimmed.substringAfter(":").trim()
            if (noteBody.isNotBlank()) {
                val title = if (noteBody.length > 25) noteBody.take(25) + "..." else noteBody
                val reply = when (language) {
                    NexaLanguage.BN -> "📝 অফলাইন নোট সফলভাবে সংরক্ষণ করা হয়েছে:\n\"$noteBody\""
                    NexaLanguage.HI -> "📝 ऑफ़लाइन नोट सफलतापूर्वक सहेजा गया:\n\"$noteBody\""
                    NexaLanguage.EN -> "📝 Offline note saved successfully:\n\"$noteBody\""
                }
                return OfflineParseResult(
                    handled = true,
                    replyText = reply,
                    noteToSave = title to noteBody
                )
            }
        }

        // 4. Battery Telemetry Query
        if (lower.contains("battery") || lower.contains("charge") ||
            lower.contains("ব্যাটারি") || lower.contains("চার্জ") ||
            lower.contains("কত পার্সেন্ট চার্জ") || lower.contains("बैटरी")
        ) {
            val (pct, isCharging) = batteryInfo
            val chargingStatusBn = if (isCharging) "চার্জ হচ্ছে ⚡" else "ডিসচার্জ হচ্ছে"
            val chargingStatusEn = if (isCharging) "Charging ⚡" else "Not Charging"
            val chargingStatusHi = if (isCharging) "चार्ज हो रहा है ⚡" else "चार्ज नहीं हो रहा है"

            val reply = when (language) {
                NexaLanguage.BN -> "🔋 ডিভাইসের ব্যাটারি স্ট্যাটাস:\nচার্জ: $pct% ($chargingStatusBn)\nস্টোরেজ: $storageInfo"
                NexaLanguage.HI -> "🔋 डिवाइस बैटरी स्थिति:\nचार्ज: $pct% ($chargingStatusHi)\nस्टोरेज: $storageInfo"
                NexaLanguage.EN -> "🔋 Device Battery Status:\nLevel: $pct% ($chargingStatusEn)\nAvailable Storage: $storageInfo"
            }
            return OfflineParseResult(handled = true, replyText = reply)
        }

        // 5. Time and Date Query
        if (lower.contains("what time") || lower.contains("current time") || lower.contains("time now") ||
            lower.contains("time koto") || lower.contains("সময় কত") || lower.contains("কয়টা বাজে") ||
            lower.contains("আজকের তারিখ") || lower.contains("what date") || lower.contains("today's date") ||
            lower.contains("तारीख") || lower.contains("समय क्या")
        ) {
            val now = Date()
            val timeFmt = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now)
            val dateFmt = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(now)

            val reply = when (language) {
                NexaLanguage.BN -> "🕒 বর্তমান স্থানীয় সময় ও তারিখ:\nসময়: $timeFmt\nতারিখ: $dateFmt"
                NexaLanguage.HI -> "🕒 वर्तमान समय और तारीख:\nसमय: $timeFmt\nतारीख: $dateFmt"
                NexaLanguage.EN -> "🕒 Local Time & Date:\nTime: $timeFmt\nDate: $dateFmt"
            }
            return OfflineParseResult(handled = true, replyText = reply)
        }

        // 6. What can NEXA do offline?
        if (lower.contains("offline e ki") || lower.contains("offline mode") ||
            lower.contains("অফলাইনে কি") || lower.contains("অফলাইন মোড") ||
            lower.contains("without internet") || lower.contains("data cara") ||
            lower.contains("data ছাড়া") || lower.contains("কী কাজ করতে পারো") ||
            lower.contains("what can you do offline")
        ) {
            val reply = when (language) {
                NexaLanguage.BN -> """
                    ⚡ অফলাইন মোডে (ইন্টারনেট বা ডেটা ছাড়া) নেক্সা যা যা করতে পারে:
                    
                    🔦 ১. টর্চ / ফ্ল্যাশলাইট চালু বা বন্ধ করা
                    🔒 ২. ভয়েস কমান্ড দিয়ে মোবাইল স্ক্রিন লক করা
                    📞 ৩. যেকোনো নম্বরে বা ফোনে সেভ থাকা কনট্যাক্টসে সরাসরি কল করা
                    🧮 ৪. অফলাইন ক্যালকুলেশন ও ইউনিট কনভার্টার
                    📝 ৫. লোকাল অফলাইন নোটস তৈরি, সংরক্ষণ ও পড়া
                    🔋 ৬. ব্যাটারি লেভেল, চার্জিং স্ট্যাটাস ও স্টোরেজ চেক
                    📱 ৭. যেকোনো ইনস্টল করা অ্যাপ খোলা (Camera, WhatsApp, Clock, Settings)
                    🔊 ৮. ফোনের সাউন্ড ভলিউম (Mute, Max, Up, Down) কন্ট্রোল
                    💬 ৯. অফলাইন লোকাল অ্যাসিস্ট্যান্ট প্রশ্নোত্তর ও ভয়েস স্পিচ
                """.trimIndent()
                NexaLanguage.HI -> """
                    ⚡ ऑफ़लाइन मोड में (बिना इंटरनेट के) नेक्सा की क्षमताएं:
                    
                    🔦 १. टॉर्च ऑन/ऑफ करना
                    🔒 २. वॉयस कमांड से स्क्रीन लॉक करना
                    📞 ३. किसी भी नंबर या कॉन्टैक्ट को फोन कॉल करना
                    🧮 ४. ऑफ़लाइन गणित गणना और यूनिट कनवर्टर
                    📝 ५. स्थानीय ऑफ़लाइन नोट्स बनाना और पढ़ना
                    🔋 ६. बैटरी प्रतिशत और स्टोरेज स्थिति देखना
                    📱 ७. ऐप्स खोलना (कैमरा, सेटिंग्स, अलार्म)
                    🔊 ८. फ़ोन वॉल्यूम नियंत्रित करना
                """.trimIndent()
                NexaLanguage.EN -> """
                    ⚡ Offline Mode Capabilities (100% Zero-Data):
                    
                    🔦 1. Flashlight / Torch ON / OFF
                    🔒 2. Voice-activated Screen Lock
                    📞 3. Direct Phone Calls & Contact Search
                    🧮 4. Offline Math Evaluator & Unit Converter
                    📝 5. Offline Quick Notes & Scratchpad
                    🔋 6. Real-time Battery Telemetry & Storage stats
                    📱 7. Launch Device Apps (Camera, Clock, Calculator, Settings)
                    🔊 8. Volume Control (Mute, 100%, Up, Down)
                    💬 9. Local AI Assistant Persona & Speech Output
                """.trimIndent()
            }
            return OfflineParseResult(handled = true, replyText = reply)
        }

        // 7. Identity & Greetings
        if (lower.contains("who are you") || lower.contains("your name") ||
            lower.contains("তুমি কে") || lower.contains("তোমার নাম কি") ||
            lower.contains("आप कौन हैं")
        ) {
            val reply = when (language) {
                NexaLanguage.BN -> "আমি নেক্সা এআই (NEXA AI) — আপনার ভবিষ্যৎমুখী পার্সোনাল ভয়েস অ্যাসিস্ট্যান্ট। আমি ডেটা বা ইন্টারনেট ছাড়াও অফলাইনে আপনার ডিভাইস পরিচালনা, হিসাব, কল ও নোটের কাজ করতে পারি! ⚡"
                NexaLanguage.HI -> "मैं नेक्सा एआई (NEXA AI) हूँ — आपकी फ्यूचरिस्टिक वॉइस और मल्टीमॉडल असिस्टेंट। मैं बिना इंटरनेट के भी आपके डिवाइस पर काम कर सकती हूँ!"
                NexaLanguage.EN -> "I am NEXA AI — your futuristic personal voice and automation companion. I can operate 100% offline without data for device automations, math, calls, and notes! ⚡"
            }
            return OfflineParseResult(handled = true, replyText = reply)
        }

        if (lower.contains("kemon acho") || lower.contains("কেমন আছো") ||
            lower.contains("how are you") || lower.contains("कैसी हो") || lower.contains("हाल चाल")
        ) {
            val reply = when (language) {
                NexaLanguage.BN -> "আমি খুব ভালো আছি! আপনার সাথে কথা বলতে পেরে আনন্দিত। আমি অফলাইন মোডেও আপনার সাহায্য করতে প্রস্তুত ♡"
                NexaLanguage.HI -> "मैं बहुत अच्छी हूँ! आपसे बात करके बहुत खुशी हुई। मैं ऑफ़लाइन मोड में भी आपकी सेवा के लिए तैयार हूँ ♡"
                NexaLanguage.EN -> "I'm functioning at peak performance! Great to connect with you. I'm ready to assist you offline or online ♡"
            }
            return OfflineParseResult(handled = true, replyText = reply)
        }

        // 8. Jokes & Motivational Quotes
        if (lower.contains("joke") || lower.contains("জোক") || lower.contains("হাসাও") || lower.contains("মজার কথা") || lower.contains("चुटकुला")) {
            val jokesBn = listOf(
                "শিক্ষক: বলো তো পৃথিবী কেন ঘোরে?\nছাত্র: স্যার, আপনি তো সবসময় বলেন জ্ঞান সবার সাথে শেয়ার করতে, তাই পৃথিবী ঘুরিয়ে সবাইকে সমান দিন-রাত দিচ্ছে!",
                "ডাক্তার: আপনার প্রতিদিন ৩ কিলোমিটার হাঁটা দরকার।\nরোগী: ডাক্তারবাবু, যাওয়ার সময় ৩ কিমি না আসার সময় ৩ কিমি?",
                "বন্ধু ১: জানিস, আমার মোবাইল এত স্মার্ট যে আমি না বললেও বুঝে ফেলে!\nবন্ধু ২: কীভাবে?\nবন্ধু ১: চার্জ শেষ হলেই নিজে নিজে ঘুমিয়ে পড়ে!"
            )
            val jokesEn = listOf(
                "Why do programmers prefer dark mode? Because light attracts bugs! 🐛😄",
                "Why was the computer cold? Because it forgot to close its Windows! 💻❄️",
                "Artificial Intelligence usually beats natural stupidity, but offline mode never fails! ⚡😉"
            )
            val joke = if (language == NexaLanguage.BN) jokesBn.random() else jokesEn.random()
            return OfflineParseResult(handled = true, replyText = "😄 [Offline Joke]:\n$joke")
        }

        if (lower.contains("quote") || lower.contains("উক্তি") || lower.contains("অনুপ্রেরণা") || lower.contains("motivation") || lower.contains("বিখ্যাত উক্তি")) {
            val quotesBn = listOf(
                "\"স্বপ্ন সেটা নয় যা তুমি ঘুমিয়ে দেখো, স্বপ্ন সেটাই যা তোমাকে ঘুমাতে দেয় না।\" — এ পি জে আবদুল কালাম ✨",
                "\"যদি তুমি সূর্যের মতো উজ্জ্বল হতে চাও, তবে প্রথমে সূর্যের মতো জ্বলতে শেখো।\" — এ পি জে আবদুল কালাম 🔥",
                "\"বিপদে মোরে রক্ষা করো এ নহে মোর প্রার্থনা, বিপদে আমি না যেন করি ভয়।\" — রবীন্দ্রনাথ ঠাকুর 🌟"
            )
            val quotesEn = listOf(
                "\"The future belongs to those who believe in the beauty of their dreams.\" — Eleanor Roosevelt ✨",
                "\"It does not matter how slowly you go as long as you do not stop.\" — Confucius 🚀",
                "\"Success is not final, failure is not fatal: it is the courage to continue that counts.\" — Winston Churchill 🌟"
            )
            val q = if (language == NexaLanguage.BN) quotesBn.random() else quotesEn.random()
            return OfflineParseResult(handled = true, replyText = "✨ [Offline Inspiration]:\n$q")
        }

        // 9. General Offline Fallback with Guidance
        val fallback = when (language) {
            NexaLanguage.BN -> """
                ⚡ অফলাইন মোড সক্রিয় (ইন্টারনেট বা ডেটা নেই)
                
                আপনার অনুরোধটি গ্রহণ করা হয়েছে: "$trimmed"
                
                💡 অফলাইনে আপনি সহজে মুখে বলতে পারেন:
                • "টর্চ অন/অফ করো" 🔦
                • "স্ক্রিন লক করো" 🔒
                • "হিসাব করো ৫০০ * ২৫" 🧮
                • "নোট: কাল সকাল ৮টায় কাজ আছে" 📝
                • "আম্মুকে কল করো" অথবা "017XXXXXXXX এ কল করো" 📞
                • "ব্যাটারি কত পার্সেন্ট" 🔋
                
                ইন্টারনেটভিত্তিক লাইভ ওয়েব বা ইমেজ জেনারেশনের জন্য ডেটা সংযোগ চালু করুন।
            """.trimIndent()
            NexaLanguage.HI -> """
                ⚡ ऑफ़लाइन मोड सक्रिय (डेटा के बिना)
                
                आपका अनुरोध: "$trimmed"
                
                💡 ऑफ़लाइन कमांड्स:
                • टॉर्च ऑन/ऑफ
                • स्क्रीन लॉक
                • कॉल करें
                • बैटरी स्थिति
                • गणित गणना (उदा: 500 * 25)
            """.trimIndent()
            NexaLanguage.EN -> """
                ⚡ Offline Mode Active (No Data / Internet)
                
                Command received: "$trimmed"
                
                💡 Offline capabilities ready to use:
                • "Torch ON / OFF" 🔦
                • "Lock screen" 🔒
                • "Calculate 450 * 18" or "25% of 1200" 🧮
                • "Note: Buy groceries" 📝
                • "Call [Contact / Number]" 📞
                • "Battery status" 🔋
                
                Enable internet/Wi-Fi for live web search and Gemini image synthesis.
            """.trimIndent()
        }

        return OfflineParseResult(handled = true, replyText = fallback)
    }

    /**
     * Parses and evaluates arithmetic math queries:
     * e.g. "500 * 25", "1000 + 450", "20% of 1500", "500 / 2", "12 x 8", "150 - 45"
     */
    private fun tryEvaluateMath(input: String): String? {
        val normalized = input
            .replace("০", "0").replace("১", "1").replace("২", "2")
            .replace("৩", "3").replace("৪", "4").replace("৫", "5")
            .replace("৬", "6").replace("৭", "7").replace("৮", "8")
            .replace("৯", "9")
            .replace("x", "*")
            .replace("×", "*")
            .replace("÷", "/")
            .replace("গুণ", "*")
            .replace("ভাগ", "/")
            .replace("যোগ", "+")
            .replace("বিয়োগ", "-")
            .replace("ইনটু", "*")

        // Percentage check: "20% of 1500" or "1500 er 20%"
        val pctRegex = Regex("(\\d+(?:\\.\\d+)?)\\s*(?:%|শতাংশ)\\s*(?:of|এর)?\\s*(\\d+(?:\\.\\d+)?)")
        val pctMatch = pctRegex.find(normalized)
        if (pctMatch != null) {
            val p = pctMatch.groupValues[1].toDoubleOrNull() ?: return null
            val total = pctMatch.groupValues[2].toDoubleOrNull() ?: return null
            val ans = (p / 100.0) * total
            return "$p% of $total = ${formatNumber(ans)}"
        }

        // Standard 2-operand math: "A [+-*/] B"
        val opRegex = Regex("(\\d+(?:\\.\\d+)?)\\s*([+\\-*/%^])\\s*(\\d+(?:\\.\\d+)?)")
        val match = opRegex.find(normalized)
        if (match != null) {
            val a = match.groupValues[1].toDoubleOrNull() ?: return null
            val op = match.groupValues[2]
            val b = match.groupValues[3].toDoubleOrNull() ?: return null

            val result = when (op) {
                "+" -> a + b
                "-" -> a - b
                "*" -> a * b
                "/" -> if (b != 0.0) a / b else return "Division by zero error"
                "%" -> a % b
                "^" -> Math.pow(a, b)
                else -> return null
            }
            return "${formatNumber(a)} $op ${formatNumber(b)} = ${formatNumber(result)}"
        }

        return null
    }

    /**
     * Converts common units (km to miles, celsius to fahrenheit, etc.)
     */
    private fun tryConvertUnits(lower: String): String? {
        // km to miles
        val kmMatch = Regex("(\\d+(?:\\.\\d+)?)\\s*(?:km|kilometer|কিলোমিটার)\\s*(?:to|in|এ)?\\s*(?:miles|মাইল)?").find(lower)
        if (kmMatch != null && (lower.contains("mile") || lower.contains("মাইল"))) {
            val km = kmMatch.groupValues[1].toDoubleOrNull() ?: return null
            val miles = km * 0.621371
            return "$km km = ${formatNumber(miles)} miles"
        }

        // miles to km
        val mileMatch = Regex("(\\d+(?:\\.\\d+)?)\\s*(?:miles|mile|মাইল)\\s*(?:to|in)?\\s*(?:km|kilometer|কিলোমিটার)?").find(lower)
        if (mileMatch != null && (lower.contains("km") || lower.contains("kilometer") || lower.contains("কিলোমিটার"))) {
            val m = mileMatch.groupValues[1].toDoubleOrNull() ?: return null
            val km = m * 1.60934
            return "$m miles = ${formatNumber(km)} km"
        }

        // celsius to fahrenheit
        val cMatch = Regex("(\\d+(?:\\.\\d+)?)\\s*(?:c|celsius|সেলসিয়াস)\\s*(?:to|in)?\\s*(?:f|fahrenheit|ফারেনহাইট)?").find(lower)
        if (cMatch != null && (lower.contains("f") || lower.contains("fahrenheit") || lower.contains("ফারেনহাইট"))) {
            val c = cMatch.groupValues[1].toDoubleOrNull() ?: return null
            val f = (c * 9.0 / 5.0) + 32.0
            return "$c °C = ${formatNumber(f)} °F"
        }

        // kg to lbs
        val kgMatch = Regex("(\\d+(?:\\.\\d+)?)\\s*(?:kg|kilogram|কেজি)\\s*(?:to|in)?\\s*(?:lbs|pound|পাউন্ড)?").find(lower)
        if (kgMatch != null && (lower.contains("pound") || lower.contains("lbs") || lower.contains("পাউন্ড"))) {
            val kg = kgMatch.groupValues[1].toDoubleOrNull() ?: return null
            val lbs = kg * 2.20462
            return "$kg kg = ${formatNumber(lbs)} lbs"
        }

        return null
    }

    private fun formatNumber(num: Double): String {
        return if (num == num.toLong().toDouble()) {
            num.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", num)
        }
    }
}
