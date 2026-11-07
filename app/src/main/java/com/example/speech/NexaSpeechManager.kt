package com.example.speech

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.model.NEXA_VOICES
import com.example.model.NexaLanguage
import com.example.model.NexaVoice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

class NexaSpeechManager(
    private val context: Context,
    private val onFinalSpeechResult: (String) -> Unit,
    private val onSpeechError: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false
    private var mediaPlayer: MediaPlayer? = null

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript.asStateFlow()

    init {
        initTts()
        initSpeechRecognizer()
    }

    private fun initTts() {
        textToSpeech = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsInitialized = true
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
            }
        }
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {
                        _isListening.value = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _rmsLevel.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _rmsLevel.value = 0f
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _rmsLevel.value = 0f
                        val errorMsg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please try again."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timed out."
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                            else -> "Voice recognition error ($error)"
                        }
                        onSpeechError(errorMsg)
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _rmsLevel.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim() ?: ""
                        if (text.isNotBlank()) {
                            _partialTranscript.value = text
                            onFinalSpeechResult(text)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim() ?: ""
                        if (text.isNotBlank()) {
                            _partialTranscript.value = text
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    fun startListening(language: NexaLanguage) {
        stopSpeaking()
        _partialTranscript.value = ""

        if (speechRecognizer == null) {
            initSpeechRecognizer()
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.code)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language.code)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }

        try {
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            _isListening.value = false
            onSpeechError("Could not start speech recognition: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        _isListening.value = false
        _rmsLevel.value = 0f
    }

    fun toggleListening(language: NexaLanguage) {
        if (_isListening.value) {
            stopListening()
        } else {
            startListening(language)
        }
    }

    fun speak(
        text: String,
        language: NexaLanguage,
        voiceId: String? = null,
        elevenLabsApiKey: String? = null
    ) {
        stopListening()
        stopSpeaking()

        val clean = text.replace(Regex("[*#`_~>]"), "").trim()
        if (clean.isBlank()) return

        val activeVoiceId = voiceId ?: "3vl8X3o2mxL44uBIHGkS"
        val voice = NEXA_VOICES.find { it.voiceId.equals(activeVoiceId.trim(), ignoreCase = true) } ?: NEXA_VOICES[0]

        val apiKey = elevenLabsApiKey?.trim() ?: ""
        if (apiKey.isNotBlank()) {
            scope.launch {
                val success = synthesizeWithElevenLabs(clean, activeVoiceId, apiKey)
                if (!success) {
                    speakWithLocalTts(clean, language, voice)
                }
            }
        } else {
            speakWithLocalTts(clean, language, voice)
        }
    }

    private suspend fun synthesizeWithElevenLabs(
        text: String,
        voiceId: String,
        apiKey: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("text", text)
                put("model_id", "eleven_multilingual_v2")
                val voiceSettings = JSONObject().apply {
                    put("stability", 0.5)
                    put("similarity_boost", 0.8)
                }
                put("voice_settings", voiceSettings)
            }

            val request = Request.Builder()
                .url("https://api.elevenlabs.io/v1/text-to-speech/${voiceId.trim()}")
                .addHeader("xi-api-key", apiKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "audio/mpeg")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val audioBytes = response.body?.bytes()
                    if (audioBytes != null && audioBytes.isNotEmpty()) {
                        val tempFile = File(context.cacheDir, "nexa_elevenlabs_${System.currentTimeMillis()}.mp3")
                        FileOutputStream(tempFile).use { it.write(audioBytes) }

                        withContext(Dispatchers.Main) {
                            playAudioFile(tempFile)
                        }
                        return@withContext true
                    }
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun playAudioFile(file: File) {
        try {
            stopSpeaking()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnPreparedListener {
                    _isSpeaking.value = true
                    start()
                }
                setOnCompletionListener {
                    _isSpeaking.value = false
                    try {
                        file.delete()
                    } catch (_: Exception) {}
                }
                setOnErrorListener { _, _, _ ->
                    _isSpeaking.value = false
                    try {
                        file.delete()
                    } catch (_: Exception) {}
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            _isSpeaking.value = false
        }
    }

    private fun speakWithLocalTts(clean: String, language: NexaLanguage, voice: NexaVoice) {
        if (!isTtsInitialized || textToSpeech == null) return

        try {
            textToSpeech?.setPitch(voice.pitchMultiplier)
            textToSpeech?.setSpeechRate(voice.speechRateMultiplier)

            val locale = when (language) {
                NexaLanguage.BN -> Locale.forLanguageTag("bn-BD")
                NexaLanguage.HI -> Locale.forLanguageTag("hi-IN")
                NexaLanguage.EN -> Locale.US
            }

            val available = textToSpeech?.isLanguageAvailable(locale)
            if (available == TextToSpeech.LANG_AVAILABLE || available == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                textToSpeech?.language = locale
            } else {
                textToSpeech?.language = Locale.US
            }

            val utteranceId = UUID.randomUUID().toString()
            _isSpeaking.value = true
            textToSpeech?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            _isSpeaking.value = false
        }
    }

    fun previewVoice(voice: NexaVoice, language: NexaLanguage, apiKey: String? = null) {
        speak(
            text = voice.samplePhrase,
            language = language,
            voiceId = voice.voiceId,
            elevenLabsApiKey = apiKey
        )
    }

    fun stopSpeaking() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.reset()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception) {}

        try {
            textToSpeech?.stop()
        } catch (_: Exception) {}
        _isSpeaking.value = false
    }

    fun destroy() {
        try {
            stopSpeaking()
            speechRecognizer?.destroy()
            textToSpeech?.shutdown()
        } catch (_: Exception) {}
    }
}
