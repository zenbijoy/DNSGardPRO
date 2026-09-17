package com.dnsguard.locker

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Last line of defense — an AccessibilityService that guards device configuration.
 *
 * Responsibilities:
 *  1. Blocking Dhizuku app (which has a Deactivate button)
 *  2. Blocking Private DNS configuration in Settings
 *  3. Blocking Device Admin management / deactivation screens
 *  4. Blocking tampering with System Service in Accessibility or App Storage
 *  5. Blocking uninstallation of System Service or Dhizuku ONLY (allows all other apps)
 *  6. Fallback real-time ContentObserver watchdog on DNS settings
 */
class GuardAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var dnsObserver: ContentObserver? = null

    companion object {
        // Packages that must never be allowed to open while locked
        private val BLOCKED_PACKAGES = setOf(
            "com.rosan.dhizuku"                    // Dhizuku — has deactivate button
        )

        // System package installers
        private val INSTALLER_PACKAGES = setOf(
            "com.android.packageinstaller",
            "com.google.android.packageinstaller",
            "com.samsung.android.packageinstaller"
        )

        // Keywords that uniquely identify protected components during uninstallation
        private val PROTECTED_APP_KEYWORDS = listOf(
            "system service",
            "dnsguard",
            "dhizuku",
            "com.dnsguard.locker",
            "com.rosan.dhizuku"
        )

        // Settings activity classes to block
        private val BLOCKED_ACTIVITY_CLASSES = setOf(
            "com.android.settings.network.PrivateDnsSettings",
            "com.android.settings.network.telephony.PrivateDnsModeDialogPreference",
            "com.android.settings.wifi.dpp.WifiDppQrCodeScannerFragment",
            "com.android.settings.DeviceAdminSettings",
            "com.android.settings.DeviceAdminAdd"
        )

        // Settings activity substrings to block
        private val BLOCKED_CLASS_SUBSTRINGS = listOf(
            "privatedns",
            "deviceadmin",
            "masterclear",
            "resetdashboard"
        )

        // Keywords that, if seen in Settings, indicate tampering with DNS, Admin, Storage, Accessibility, or Reset
        private val BLOCKED_SETTINGS_KEYWORDS = listOf(
            "private dns",
            "dhizuku",
            "device owner",
            "system service",           // app & accessibility service label
            "dnsguard",
            "com.dnsguard.locker",
            "guardaccessibilityservice",
            "com.rosan.dhizuku",
            "stop system service",
            "turn off system service",
            "disable system service",
            "factory reset",
            "erase all data",
            "reset options",
            "reset phone"
        )

        @Volatile
        var isRunning: Boolean = false
            private set

        @Volatile
        var lastHeartbeatMs: Long = 0L
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        lastHeartbeatMs = System.currentTimeMillis()

        // Dismiss any persistent accessibility warning notification immediately
        runCatching {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.cancel(LockReApplyReceiver.NOTIF_ID_ACC)
        }

        // Configure event handling: ONLY listen to window state changes (avoids flood & IPC choke)
        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = info.flags or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        info.notificationTimeout = 100L
        serviceInfo = info

        // Fallback ContentObserver to guard DNS even if LockMonitorService is disrupted
        registerDnsFallbackObserver()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isRunning = false
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        isRunning = false
        dnsObserver?.let { runCatching { contentResolver.unregisterContentObserver(it) } }
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        try {
            lastHeartbeatMs = System.currentTimeMillis()

            // Only guard when actually locked
            if (!DnsLocker.isLocked(this)) return

            val pkg = event.packageName?.toString() ?: return
            val clazz = event.className?.toString() ?: ""

            val isDhizuku = pkg in BLOCKED_PACKAGES
            val isInstaller = pkg in INSTALLER_PACKAGES
            val isSettings = pkg == "com.android.settings" || pkg.endsWith(".settings")

            // FAST EXIT: Do zero work for unrelated apps (browsers, games, chat, launcher)
            if (!isDhizuku && !isInstaller && !isSettings) return

            // ── Block 1: Dhizuku app directly ────────────────────────────────────
            if (isDhizuku) {
                goHome()
                return
            }

            // ── Block 2: Package uninstaller dialogs targeting System Service or Dhizuku ──
            if (isInstaller) {
                if (isTargetingProtectedPackage(event)) {
                    goHome()
                    return
                }
                // Allow uninstallation of ALL other apps!
                return
            }

            // ── Block 3: Settings screens (DNS, Accessibility, App Storage, Device Admin) ──
            if (isSettings) {
                // 1. In-memory check on activity class
                val clazzLower = clazz.lowercase()
                if (clazz in BLOCKED_ACTIVITY_CLASSES || BLOCKED_CLASS_SUBSTRINGS.any { clazzLower.contains(it) }) {
                    goHome()
                    return
                }

                // 2. In-memory check on event text
                if (eventTextMatches(event, BLOCKED_SETTINGS_KEYWORDS)) {
                    goHome()
                    return
                }

                // 3. Hierarchy check with safe node recycling
                if (hasBlockedKeywordSafe()) {
                    goHome()
                    return
                }
            }
        } catch (_: Exception) {
            // Prevent unhandled view hierarchy or IPC exceptions from crashing the service binder
        }
    }

    private fun isTargetingProtectedPackage(event: AccessibilityEvent): Boolean {
        // In-memory event text match first
        if (eventTextMatches(event, PROTECTED_APP_KEYWORDS)) return true

        // Node inspection specifically for protected app keywords with recycling
        val root = runCatching { rootInActiveWindow ?: event.source }.getOrNull() ?: return false
        return try {
            for (keyword in PROTECTED_APP_KEYWORDS) {
                val matches = root.findAccessibilityNodeInfosByText(keyword)
                if (!matches.isNullOrEmpty()) {
                    matches.forEach { runCatching { it.recycle() } }
                    return true
                }
            }
            false
        } finally {
            runCatching { root.recycle() }
        }
    }

    private fun eventTextMatches(event: AccessibilityEvent, keywords: List<String>): Boolean {
        return runCatching {
            val fullText = event.text.joinToString(" ").lowercase()
            keywords.any { fullText.contains(it) }
        }.getOrDefault(false)
    }

    private fun hasBlockedKeywordSafe(): Boolean {
        val root = runCatching { rootInActiveWindow }.getOrNull() ?: return false
        return try {
            for (keyword in BLOCKED_SETTINGS_KEYWORDS) {
                val matches = root.findAccessibilityNodeInfosByText(keyword)
                if (!matches.isNullOrEmpty()) {
                    matches.forEach { runCatching { it.recycle() } }
                    return true
                }
            }
            false
        } finally {
            runCatching { root.recycle() }
        }
    }

    private fun registerDnsFallbackObserver() {
        if (dnsObserver != null) return
        dnsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                if (DnsLocker.isLocked(this@GuardAccessibilityService)) {
                    scope.launch {
                        try {
                            DhizukuHelper.init(this@GuardAccessibilityService)
                            val dpm = DhizukuHelper.getDpm() ?: return@launch
                            val admin = DhizukuHelper.getAdmin() ?: return@launch
                            DnsLocker.reVerifyLock(this@GuardAccessibilityService, dpm, admin)
                        } catch (_: Exception) {}
                    }
                }
            }
        }

        runCatching {
            val cr = contentResolver
            cr.registerContentObserver(Settings.Global.getUriFor("private_dns_mode"), false, dnsObserver!!)
            cr.registerContentObserver(Settings.Global.getUriFor("private_dns_specifier"), false, dnsObserver!!)
        }
    }

    private fun goHome() {
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    override fun onInterrupt() {}
}
