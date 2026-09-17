package com.dnsguard.locker

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager

/**
 * Represents the live state of GuardAccessibilityService:
 * - ACTIVE: Enabled in Settings AND currently bound/alive in memory.
 * - ZOMBIE: Enabled in Settings, but binder died (e.g. killed by battery manager).
 * - DISABLED: Not enabled in Settings.
 */
enum class AccessibilityState {
    ACTIVE,
    ZOMBIE,
    DISABLED
}

/**
 * Robust helper to check the state of GuardAccessibilityService across all Android OEM ROMs.
 */
object AccessibilityHelper {

    /**
     * Checks if GuardAccessibilityService is enabled in Android Settings.
     * Checks both the official AccessibilityManager API and Settings.Secure directly,
     * supporting all ComponentName formats (short, long, and partial).
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        // 1. Primary check: AccessibilityManager enabled services list
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        val enabledInAm = runCatching {
            am?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
                ?.any { info ->
                    val si = info.resolveInfo?.serviceInfo
                    si != null &&
                            si.packageName == context.packageName &&
                            (si.name == GuardAccessibilityService::class.java.name ||
                             si.name.endsWith("GuardAccessibilityService"))
                }
        }.getOrNull() ?: false

        if (enabledInAm) return true

        // 2. Secondary check: Settings.Secure ENABLED_ACCESSIBILITY_SERVICES string
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val targetPkg = context.packageName
        val targetClass = GuardAccessibilityService::class.java.name
        val simpleName = GuardAccessibilityService::class.java.simpleName

        return enabledServices.split(":").any { rawEntry ->
            val entry = rawEntry.trim()
            if (entry.isEmpty()) return@any false

            // Try parsing as ComponentName
            val cn = ComponentName.unflattenFromString(entry)
            if (cn != null && cn.packageName.equals(targetPkg, ignoreCase = true)) {
                cn.className.equals(targetClass, ignoreCase = true) ||
                        cn.className.endsWith(simpleName, ignoreCase = true) ||
                        cn.shortClassName.endsWith(simpleName, ignoreCase = true)
            } else {
                // Raw substring fallback for vendor-modified formats
                entry.contains(targetPkg, ignoreCase = true) &&
                        entry.contains(simpleName, ignoreCase = true)
            }
        }
    }

    /**
     * Evaluates whether the service is ACTIVE (enabled and running),
     * ZOMBIE (enabled in settings, but binder dead), or DISABLED.
     */
    fun getAccessibilityState(context: Context): AccessibilityState {
        val enabled = isAccessibilityServiceEnabled(context)
        if (!enabled) return AccessibilityState.DISABLED
        return if (GuardAccessibilityService.isRunning) {
            AccessibilityState.ACTIVE
        } else {
            AccessibilityState.ZOMBIE
        }
    }
}
