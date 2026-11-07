package com.example.data

import android.accessibilityservice.AccessibilityService
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import com.example.receiver.NexaDeviceAdminReceiver
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import com.example.accessibility.NexaAccessibilityService
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.URLEncoder

data class AutomationResult(
    val success: Boolean,
    val message: String,
    val detail: String? = null
)

class SystemAutomationManager(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val _torchState = MutableStateFlow(false)
    val torchState: StateFlow<Boolean> = _torchState.asStateFlow()

    private var cameraIdWithFlash: String? = null

    init {
        findCameraWithFlash()
    }

    private fun findCameraWithFlash() {
        try {
            cameraManager?.let { mgr ->
                for (id in mgr.cameraIdList) {
                    val characteristics = mgr.getCameraCharacteristics(id)
                    val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                    val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                    if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        cameraIdWithFlash = id
                        break
                    }
                }
                if (cameraIdWithFlash == null && mgr.cameraIdList.isNotEmpty()) {
                    cameraIdWithFlash = mgr.cameraIdList[0]
                }
            }
        } catch (e: Exception) {
            Log.e("SystemAutomation", "Error finding camera flash: ${e.message}")
        }
    }

    // ==========================================
    // 1. FLASHLIGHT / TORCH
    // ==========================================
    fun setTorch(enabled: Boolean): AutomationResult {
        return try {
            val id = cameraIdWithFlash
            if (id == null) {
                AutomationResult(false, "Flashlight hardware not available on this device")
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    cameraManager?.setTorchMode(id, enabled)
                    _torchState.value = enabled
                    val stateText = if (enabled) "Torch turned ON 🔦" else "Torch turned OFF"
                    AutomationResult(true, stateText)
                } else {
                    AutomationResult(false, "Torch requires Android 6.0+")
                }
            }
        } catch (e: CameraAccessException) {
            AutomationResult(false, "Torch access error: ${e.localizedMessage}")
        } catch (e: Exception) {
            AutomationResult(false, "Flashlight error: ${e.localizedMessage}")
        }
    }

    fun toggleTorch(): AutomationResult {
        return setTorch(!_torchState.value)
    }

    // ==========================================
    // 2. WI-FI CONTROL
    // ==========================================
    fun openWifiSettings(): AutomationResult {
        return try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent(Settings.Panel.ACTION_WIFI)
            } else {
                Intent(Settings.ACTION_WIFI_SETTINGS)
            }.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AutomationResult(true, "Wi-Fi control panel opened")
        } catch (e: Exception) {
            // Fallback to general wireless settings
            try {
                val fallbackIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
                AutomationResult(true, "Settings opened")
            } catch (err: Exception) {
                AutomationResult(false, "Could not open Wi-Fi settings: ${e.localizedMessage}")
            }
        }
    }

    // ==========================================
    // 3. ACCESSIBILITY & SCREEN CLICKS
    // ==========================================
    fun isAccessibilityActive(): Boolean = NexaAccessibilityService.isServiceRunning()

    fun openAccessibilitySettings(): AutomationResult {
        return try {
            NexaAccessibilityService.openAccessibilitySettings(context)
            AutomationResult(true, "Opening Accessibility Settings. Please turn ON NEXA Service.")
        } catch (e: Exception) {
            AutomationResult(false, "Failed to open settings: ${e.localizedMessage}")
        }
    }

    fun simulateTap(x: Float, y: Float): AutomationResult {
        val service = NexaAccessibilityService.instance
        return if (service != null) {
            val success = service.simulateTap(x, y)
            if (success) {
                AutomationResult(true, "Screen tapped at ($x, $y)")
            } else {
                AutomationResult(false, "Gesture dispatch failed")
            }
        } else {
            AutomationResult(false, "Accessibility service is inactive. Tap 'Enable Accessibility' first.")
        }
    }

    fun clickOnScreenText(text: String): AutomationResult {
        val service = NexaAccessibilityService.instance
        return if (service != null) {
            val success = service.clickByText(text)
            if (success) {
                AutomationResult(true, "Clicked element containing \"$text\"")
            } else {
                AutomationResult(false, "Element with text \"$text\" not found on active screen")
            }
        } else {
            AutomationResult(false, "Accessibility Service is not enabled. Please enable it in Settings.")
        }
    }

    fun performGlobalAction(action: Int): AutomationResult {
        val service = NexaAccessibilityService.instance
        return if (service != null) {
            val success = service.triggerGlobalAction(action)
            val actionName = when (action) {
                AccessibilityService.GLOBAL_ACTION_HOME -> "Home"
                AccessibilityService.GLOBAL_ACTION_BACK -> "Back"
                AccessibilityService.GLOBAL_ACTION_RECENTS -> "Recents"
                AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS -> "Notifications"
                AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS -> "Quick Settings"
                else -> "Global Action $action"
            }
            if (success) AutomationResult(true, "$actionName triggered")
            else AutomationResult(false, "Failed to perform $actionName")
        } else {
            AutomationResult(false, "Accessibility Service not enabled")
        }
    }

    // ==========================================
    // SCREEN LOCK (VOICE & SYSTEM)
    // ==========================================
    fun isDeviceAdminActive(): Boolean {
        return try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            val adminComponent = ComponentName(context, NexaDeviceAdminReceiver::class.java)
            dpm?.isAdminActive(adminComponent) ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun isScreenLockCapable(): Boolean {
        val hasAccessibility = NexaAccessibilityService.isServiceRunning() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
        val hasAdmin = isDeviceAdminActive()
        return hasAccessibility || hasAdmin
    }

    fun lockDeviceScreen(): AutomationResult {
        // 1. First priority: Accessibility Service (available API 28+, no device admin prompt required)
        val service = NexaAccessibilityService.instance
        if (service != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val success = service.lockScreen()
            if (success) {
                return AutomationResult(true, "Screen locked successfully 🔒")
            }
        }

        // 2. Second priority: Device Policy Manager
        try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            val adminComponent = ComponentName(context, NexaDeviceAdminReceiver::class.java)
            if (dpm != null && dpm.isAdminActive(adminComponent)) {
                dpm.lockNow()
                return AutomationResult(true, "Screen locked via Device Admin 🔒")
            }
        } catch (e: Exception) {
            Log.e("SystemAutomation", "DPM lock error: ${e.message}")
        }

        // 3. Fallback: prompt user to grant permission
        return AutomationResult(
            false,
            "Screen lock requires Accessibility Service (Android 9+) or Device Admin. Tap to enable!",
            detail = "NEED_LOCK_PERMISSION"
        )
    }

    fun requestDeviceAdmin() {
        try {
            val adminComponent = ComponentName(context, NexaDeviceAdminReceiver::class.java)
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Enables NEXA AI voice screen lock command.")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("SystemAutomation", "Device admin request error: ${e.message}")
        }
    }

    // ==========================================
    // DISPLAY OVER OTHER APPS (OVERLAY)
    // ==========================================
    fun canDrawOverlays(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun openOverlaySettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        }
    }

    // ==========================================
    // NOTIFICATION LISTENER (READ MESSAGES)
    // ==========================================
    fun isNotificationListenerEnabled(): Boolean {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return flat?.contains(packageName) ?: false
    }

    fun openNotificationListenerSettings() {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("SystemAutomation", "Error opening notification settings: ${e.message}")
        }
    }

    // ==========================================
    // 4. APP LAUNCHER & SEARCH
    // ==========================================
    fun openApp(packageName: String): AutomationResult {
        return try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                AutomationResult(true, "Launching app: $packageName")
            } else {
                openPlayStore(packageName)
            }
        } catch (e: Exception) {
            AutomationResult(false, "Could not open app: ${e.localizedMessage}")
        }
    }

    fun launchKnownApp(appNameQuery: String): AutomationResult {
        val clean = appNameQuery.lowercase().trim()
        val pkg = when {
            clean.contains("youtube") -> "com.google.android.youtube"
            clean.contains("facebook") || clean.contains("fb") -> "com.facebook.katana"
            clean.contains("insta") -> "com.instagram.android"
            clean.contains("whatsapp") -> "com.whatsapp"
            clean.contains("messenger") -> "com.facebook.orca"
            clean.contains("sheet") -> "com.google.android.apps.docs.editors.sheets"
            clean.contains("doc") -> "com.google.android.apps.docs.editors.docs"
            clean.contains("slide") -> "com.google.android.apps.docs.editors.slides"
            clean.contains("excel") -> "com.microsoft.office.excel"
            clean.contains("chrome") -> "com.android.chrome"
            clean.contains("camera") -> "com.android.camera2"
            clean.contains("setting") -> "com.android.settings"
            else -> null
        }

        return if (pkg != null) {
            openApp(pkg)
        } else {
            // Search installed applications by app name label
            try {
                val pm = context.packageManager
                val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                val match = apps.firstOrNull {
                    pm.getApplicationLabel(it).toString().lowercase().contains(clean)
                }
                if (match != null) {
                    val intent = pm.getLaunchIntentForPackage(match.packageName)
                    if (intent != null) {
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        return AutomationResult(true, "Opened ${pm.getApplicationLabel(match)}")
                    }
                }
                // Fallback to Play Store search
                openPlayStore(clean)
            } catch (e: Exception) {
                openPlayStore(clean)
            }
        }
    }

    // ==========================================
    // 5. MEDIA CONTROLS & YOUTUBE SEARCH
    // ==========================================
    fun searchYouTube(query: String): AutomationResult {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val webUri = Uri.parse("https://www.youtube.com/results?search_query=$encoded")
            val appIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                setPackage("com.google.android.youtube")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(appIntent)
                AutomationResult(true, "Searching YouTube for: $query")
            } catch (e: Exception) {
                val browserIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
                AutomationResult(true, "Searching YouTube web for: $query")
            }
        } catch (e: Exception) {
            AutomationResult(false, "YouTube search failed: ${e.localizedMessage}")
        }
    }

    fun toggleMediaPlayPause(): AutomationResult {
        return try {
            val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            val upEvent = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            audioManager?.dispatchMediaKeyEvent(downEvent)
            audioManager?.dispatchMediaKeyEvent(upEvent)
            AutomationResult(true, "Media Play/Pause command sent 🎵")
        } catch (e: Exception) {
            AutomationResult(false, "Media control failed: ${e.localizedMessage}")
        }
    }

    // ==========================================
    // 6. GOOGLE LIVE SEARCH
    // ==========================================
    fun searchGoogleLive(query: String): AutomationResult {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val uri = Uri.parse("https://www.google.com/search?q=$encoded")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AutomationResult(true, "Opening live Google Search for: \"$query\"")
        } catch (e: Exception) {
            AutomationResult(false, "Google Search error: ${e.localizedMessage}")
        }
    }

    // ==========================================
    // 7. AUTONOMOUS SOCIAL POSTING & INTERACTIONS (Facebook, Instagram, YouTube)
    // ==========================================
    fun copyToClipboard(text: String, label: String = "NEXA AI Content") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard?.setPrimaryClip(clip)
        } catch (e: Exception) {
            Log.e("SystemAutomation", "Clipboard error: ${e.message}")
        }
    }

    /**
     * Autonomously generates and sends a post to Facebook.
     * Auto-copies caption to clipboard and launches Facebook composer directly.
     */
    fun postToFacebook(caption: String): AutomationResult {
        return try {
            copyToClipboard(caption, "Facebook Post")
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, caption)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val fbPackage = "com.facebook.katana"
            val fbLitePackage = "com.facebook.lite"
            val pm = context.packageManager
            val isInstalled = pm.getLaunchIntentForPackage(fbPackage) != null ||
                              pm.getLaunchIntentForPackage(fbLitePackage) != null

            if (isInstalled) {
                intent.setPackage(if (pm.getLaunchIntentForPackage(fbPackage) != null) fbPackage else fbLitePackage)
                try {
                    context.startActivity(intent)
                    return AutomationResult(
                        true,
                        "Facebook post launched! Caption copied to clipboard & injected 📱",
                        caption
                    )
                } catch (_: Exception) {
                    // fallback
                }
            }

            // Web fallback
            val webUrl = "https://m.facebook.com/composer/"
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
            AutomationResult(
                true,
                "Facebook opened with caption copied to clipboard! Ready to publish ✨",
                caption
            )
        } catch (e: Exception) {
            AutomationResult(false, "Facebook auto-post failed: ${e.localizedMessage}")
        }
    }

    /**
     * Autonomously launches Instagram with caption copied to clipboard and opens feed/story creator.
     */
    fun postToInstagram(caption: String): AutomationResult {
        return try {
            copyToClipboard(caption, "Instagram Post")
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, caption)
                setPackage("com.instagram.android")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val pm = context.packageManager
            if (pm.getLaunchIntentForPackage("com.instagram.android") != null) {
                try {
                    context.startActivity(intent)
                    return AutomationResult(
                        true,
                        "Instagram post initiated! Caption copied to clipboard for paste 📸",
                        caption
                    )
                } catch (_: Exception) {
                    // Fallback to app launch
                    val launchIntent = pm.getLaunchIntentForPackage("com.instagram.android")
                    launchIntent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    launchIntent?.let { context.startActivity(it) }
                    return AutomationResult(
                        true,
                        "Instagram opened! Caption copied to clipboard ready to paste.",
                        caption
                    )
                }
            }

            // Web fallback
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
            AutomationResult(
                true,
                "Instagram web opened! Caption copied to clipboard.",
                caption
            )
        } catch (e: Exception) {
            AutomationResult(false, "Instagram auto-post failed: ${e.localizedMessage}")
        }
    }

    /**
     * Autonomously launches YouTube create post / video upload with content.
     */
    fun postToYouTube(content: String): AutomationResult {
        return try {
            copyToClipboard(content, "YouTube Post")
            val youtubePkg = "com.google.android.youtube"
            val pm = context.packageManager

            if (pm.getLaunchIntentForPackage(youtubePkg) != null) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, content)
                    setPackage(youtubePkg)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(intent)
                    return AutomationResult(
                        true,
                        "YouTube creator launched! Content copied to clipboard 🎬",
                        content
                    )
                } catch (_: Exception) {
                    val launch = pm.getLaunchIntentForPackage(youtubePkg)
                    launch?.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    launch?.let { context.startActivity(it) }
                    return AutomationResult(
                        true,
                        "YouTube opened with content copied to clipboard.",
                        content
                    )
                }
            }

            // Studio / Web fallback
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://studio.youtube.com")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
            AutomationResult(true, "YouTube Studio opened with content copied to clipboard.", content)
        } catch (e: Exception) {
            AutomationResult(false, "YouTube post failed: ${e.localizedMessage}")
        }
    }

    fun shareSocialPost(platform: String, caption: String): AutomationResult {
        return when (platform.lowercase().trim()) {
            "facebook", "fb" -> postToFacebook(caption)
            "instagram", "insta" -> postToInstagram(caption)
            "youtube" -> postToYouTube(caption)
            else -> {
                copyToClipboard(caption, "Social Post")
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, caption)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                val chooser = Intent.createChooser(intent, "Share via NEXA").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(chooser)
                AutomationResult(true, "Shared to $platform with caption copied")
            }
        }
    }

    // Screen auto-actions for social media
    fun clickLike(): AutomationResult {
        val keywords = listOf("Like", "লাইক", "পছন্দ", "Thumbs up", "Thumps up")
        for (kw in keywords) {
            val res = clickOnScreenText(kw)
            if (res.success) return AutomationResult(true, "Tapped 'Like' on screen 👍")
        }
        return AutomationResult(false, "Could not find 'Like' button on current screen")
    }

    fun clickFollow(): AutomationResult {
        val keywords = listOf("Follow", "ফলো", "অনুসরণ")
        for (kw in keywords) {
            val res = clickOnScreenText(kw)
            if (res.success) return AutomationResult(true, "Tapped 'Follow' on screen 👤")
        }
        return AutomationResult(false, "Could not find 'Follow' button on current screen")
    }

    fun clickSubscribe(): AutomationResult {
        val keywords = listOf("Subscribe", "সাবস্ক্রাইব", "Subscribed")
        for (kw in keywords) {
            val res = clickOnScreenText(kw)
            if (res.success) return AutomationResult(true, "Tapped 'Subscribe' on screen 🔔")
        }
        return AutomationResult(false, "Could not find 'Subscribe' button on current screen")
    }

    fun clickComment(): AutomationResult {
        val keywords = listOf("Comment", "কমেন্ট", "মন্তব্য", "Add a comment")
        for (kw in keywords) {
            val res = clickOnScreenText(kw)
            if (res.success) return AutomationResult(true, "Tapped 'Comment' on screen 💬")
        }
        return AutomationResult(false, "Could not find 'Comment' button on current screen")
    }

    fun clickPostButton(): AutomationResult {
        val keywords = listOf("Post", "পোস্ট", "Share", "Publish", "Tweet")
        for (kw in keywords) {
            val res = clickOnScreenText(kw)
            if (res.success) return AutomationResult(true, "Tapped 'Post/Publish' button 🚀")
        }
        return AutomationResult(false, "Could not find 'Post' button on current screen")
    }

    fun pressBack(): AutomationResult = performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
    fun pressHome(): AutomationResult = performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
    fun openNotifications(): AutomationResult = performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
    fun lockScreen(): AutomationResult = lockDeviceScreen()

    fun openSocialApp(platform: String): AutomationResult {
        val (pkg, fallbackUrl) = when (platform.lowercase()) {
            "facebook", "fb" -> "com.facebook.katana" to "https://www.facebook.com"
            "instagram", "insta" -> "com.instagram.android" to "https://www.instagram.com"
            "youtube" -> "com.google.android.youtube" to "https://www.youtube.com"
            "whatsapp" -> "com.whatsapp" to "https://web.whatsapp.com"
            "messenger" -> "com.facebook.orca" to "https://www.messenger.com"
            else -> "" to "https://google.com"
        }

        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                AutomationResult(true, "Opening $platform app")
            } else {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                AutomationResult(true, "Opening $platform on web")
            }
        } catch (e: Exception) {
            AutomationResult(false, "Could not open $platform: ${e.localizedMessage}")
        }
    }

    // ==========================================
    // 8. CALLS, WHATSAPP & MESSAGING
    // ==========================================
    fun makePhoneCall(number: String): AutomationResult {
        return try {
            val clean = number.filter { it.isDigit() || it == '+' }
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AutomationResult(true, "Dialing $clean")
        } catch (e: Exception) {
            AutomationResult(false, "Dialer error: ${e.localizedMessage}")
        }
    }

    /**
     * Searches Contacts database by name and initiates a voice call
     */
    fun callContactByName(queryName: String): AutomationResult {
        return try {
            val resolver = context.contentResolver
            val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )
            val cursor = resolver.query(
                uri,
                projection,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
                arrayOf("%$queryName%"),
                null
            )
            var foundNumber: String? = null
            var matchedName: String? = null
            cursor?.use {
                if (it.moveToFirst()) {
                    matchedName = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    foundNumber = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                }
            }
            if (foundNumber != null) {
                makePhoneCall(foundNumber!!)
                AutomationResult(true, "Calling $matchedName ($foundNumber) 📞")
            } else {
                AutomationResult(false, "No contact found matching \"$queryName\" in phonebook")
            }
        } catch (e: SecurityException) {
            AutomationResult(false, "Contact permission needed to search and call contacts")
        } catch (e: Exception) {
            AutomationResult(false, "Contact search error: ${e.localizedMessage}")
        }
    }

    /**
     * Reads device GPS coordinates and location state
     */
    fun getDeviceLocation(): AutomationResult {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager == null) {
                return AutomationResult(false, "Location service not available")
            }
            val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            var bestLocation: Location? = null
            if (hasGps) {
                bestLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
            if (bestLocation == null && hasNetwork) {
                bestLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }

            if (bestLocation != null) {
                val lat = String.format("%.4f", bestLocation.latitude)
                val lon = String.format("%.4f", bestLocation.longitude)
                AutomationResult(true, "Current Location: Latitude $lat, Longitude $lon 📍")
            } else {
                AutomationResult(true, "GPS satellite search active. Location services enabled 📍")
            }
        } catch (e: SecurityException) {
            AutomationResult(false, "Location permission is required to detect GPS coordinates")
        } catch (e: Exception) {
            AutomationResult(false, "Location error: ${e.localizedMessage}")
        }
    }

    fun sendWhatsApp(number: String, message: String): AutomationResult {
        return try {
            val cleanNumber = number.filter { it.isDigit() }
            val encodedText = URLEncoder.encode(message, "UTF-8")
            val url = if (cleanNumber.isNotBlank()) {
                "https://api.whatsapp.com/send?phone=$cleanNumber&text=$encodedText"
            } else {
                "https://api.whatsapp.com/send?text=$encodedText"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage("com.whatsapp")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(intent)
                AutomationResult(true, "Opening WhatsApp to send message")
            } catch (e: Exception) {
                // If app is not installed, open web link in browser
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                AutomationResult(true, "Opening WhatsApp web")
            }
        } catch (e: Exception) {
            AutomationResult(false, "WhatsApp failed: ${e.localizedMessage}")
        }
    }

    fun openMessenger(recipientId: String? = null): AutomationResult {
        return try {
            val url = if (!recipientId.isNullOrBlank()) {
                "https://m.me/$recipientId"
            } else {
                "https://www.messenger.com"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage("com.facebook.orca")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(intent)
                AutomationResult(true, "Opening Messenger")
            } catch (e: Exception) {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                AutomationResult(true, "Opening Messenger web")
            }
        } catch (e: Exception) {
            AutomationResult(false, "Messenger error: ${e.localizedMessage}")
        }
    }

    fun sendVoiceSms(phoneNumber: String, text: String): AutomationResult {
        return try {
            val clean = phoneNumber.filter { it.isDigit() || it == '+' }
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$clean")
                putExtra("sms_body", text)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AutomationResult(true, "SMS composer prepared for $clean")
        } catch (e: Exception) {
            AutomationResult(false, "SMS error: ${e.localizedMessage}")
        }
    }

    // ==========================================
    // 9. APP INSTALL & UNINSTALL
    // ==========================================
    fun openPlayStore(queryOrPackage: String): AutomationResult {
        return try {
            val marketUri = Uri.parse("market://search?q=${URLEncoder.encode(queryOrPackage, "UTF-8")}")
            val intent = Intent(Intent.ACTION_VIEW, marketUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(intent)
                AutomationResult(true, "Searching Google Play Store for \"$queryOrPackage\"")
            } catch (e: Exception) {
                val webUri = Uri.parse("https://play.google.com/store/search?q=${URLEncoder.encode(queryOrPackage, "UTF-8")}")
                val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                AutomationResult(true, "Opening Play Store web for \"$queryOrPackage\"")
            }
        } catch (e: Exception) {
            AutomationResult(false, "Play Store error: ${e.localizedMessage}")
        }
    }

    fun openAppUninstall(packageName: String): AutomationResult {
        return try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AutomationResult(true, "Opening uninstaller for $packageName")
        } catch (e: Exception) {
            AutomationResult(false, "Uninstall error: ${e.localizedMessage}")
        }
    }

    // ==========================================
    // 10. GOOGLE DOCS, SHEETS, SLIDES & EXCEL
    // ==========================================
    fun openNewGoogleDoc(): AutomationResult = openWebShortcut("https://docs.new", "Google Docs")
    fun openNewGoogleSheet(): AutomationResult = openWebShortcut("https://sheets.new", "Google Sheets")
    fun openNewGoogleSlide(): AutomationResult = openWebShortcut("https://slides.new", "Google Slides")
    fun openExcel(): AutomationResult = openWebShortcut("https://office.live.com/start/Excel.aspx", "Microsoft Excel")

    private fun openWebShortcut(url: String, serviceName: String): AutomationResult {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AutomationResult(true, "Opening new $serviceName document ✨")
        } catch (e: Exception) {
            AutomationResult(false, "Failed to open $serviceName: ${e.localizedMessage}")
        }
    }

    // ==========================================
    // 11. OFFLINE TELEMETRY & HARDWARE CONTROLS
    // ==========================================
    fun getBatteryInfo(): Pair<Int, Boolean> {
        return try {
            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = context.registerReceiver(null, ifilter)
            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else 85
            val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            batteryPct to isCharging
        } catch (_: Exception) {
            85 to false
        }
    }

    fun getAvailableStorage(): String {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val bytesAvailable = stat.availableBlocksLong * stat.blockSizeLong
            val gb = bytesAvailable / (1024 * 1024 * 1024.0)
            String.format(java.util.Locale.US, "%.1f GB Free", gb)
        } catch (_: Exception) {
            "Storage Available"
        }
    }

    fun setDeviceVolume(command: String): AutomationResult {
        val am = audioManager ?: return AutomationResult(false, "Audio service unavailable")
        return try {
            when (command.lowercase().trim()) {
                "mute" -> {
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI)
                    AutomationResult(true, "Media sound muted 🔇")
                }
                "max" -> {
                    val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, max, AudioManager.FLAG_SHOW_UI)
                    AutomationResult(true, "Media volume set to maximum 🔊")
                }
                "up" -> {
                    am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                    AutomationResult(true, "Volume increased 🔊")
                }
                "down" -> {
                    am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                    AutomationResult(true, "Volume decreased 🔉")
                }
                else -> AutomationResult(false, "Unknown volume command")
            }
        } catch (e: Exception) {
            AutomationResult(false, "Volume error: ${e.localizedMessage}")
        }
    }

    fun openSystemTool(tool: String): AutomationResult {
        return try {
            val intent = when (tool.lowercase().trim()) {
                "calculator" -> {
                    val calcIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALCULATOR)
                    if (calcIntent.resolveActivity(context.packageManager) != null) calcIntent else Intent(Settings.ACTION_SETTINGS)
                }
                "clock", "alarm" -> {
                    Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS)
                }
                "camera" -> {
                    Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                }
                "bluetooth" -> {
                    Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                }
                "display" -> {
                    Intent(Settings.ACTION_DISPLAY_SETTINGS)
                }
                else -> Intent(Settings.ACTION_SETTINGS)
            }.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AutomationResult(true, "Opening ${tool.replaceFirstChar { it.uppercase() }} ⚡")
        } catch (e: Exception) {
            AutomationResult(false, "Could not open $tool: ${e.localizedMessage}")
        }
    }
}
