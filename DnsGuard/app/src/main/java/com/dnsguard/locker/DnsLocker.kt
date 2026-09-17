package com.dnsguard.locker

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.os.UserManager

/**
 * Encapsulates all protection enforcement operations.
 * Supports both:
 *  1. Standalone Mode: Local DNS VPN (VpnService) + Device Admin + Accessibility Guard (No PC/USB required)
 *  2. Extreme Mode: Dhizuku Device Owner privileged restrictions (if available)
 */
object DnsLocker {

    private const val TARGET_DNS      = "high.kahfguard.com"
    private const val DHIZUKU_PKG     = "com.rosan.dhizuku"
    private const val PREFS_NAME      = "dg_state"
    private const val KEY_IS_LOCKED   = "lk"

    // ── Lock state ───────────────────────────────────────────────────────────

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveLockState(context: Context, locked: Boolean) {
        prefs(context).edit().putBoolean(KEY_IS_LOCKED, locked).apply()
    }

    fun isLocked(context: Context): Boolean =
        prefs(context).getBoolean(KEY_IS_LOCKED, false)

    // ── Dhizuku health check ─────────────────────────────────────────────────

    fun isDhizukuInstalled(context: Context): Boolean = try {
        context.packageManager.getPackageInfo(DHIZUKU_PKG, 0)
        true
    } catch (_: Exception) { false }

    // ── Core lock (Unified Standalone + Device Owner) ─────────────────────────

    fun lockEverything(context: Context, dpm: DevicePolicyManager? = null, admin: ComponentName? = null): Boolean {
        return try {
            // 1. Always activate the Local DNS VPN Shield (works on 100% of Android devices without PC)
            DnsVpnService.start(context)

            // 2. If Dhizuku Device Owner is connected, apply privileged system-level policies
            if (dpm != null && admin != null) {
                runCatching { dpm.setGlobalPrivateDnsModeSpecifiedHost(admin, TARGET_DNS) }
                runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_PRIVATE_DNS) }
                runCatching { dpm.setUninstallBlocked(admin, context.packageName, true) }
                runCatching { dpm.setUninstallBlocked(admin, DHIZUKU_PKG, true) }
                runCatching { dpm.setApplicationHidden(admin, DHIZUKU_PKG, true) }
                runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET) }
                runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_SAFE_BOOT) }
                runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_USB_FILE_TRANSFER) }
                runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_VPN) }
                runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_ADD_USER) }
                runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_APPS_CONTROL) }
                runCatching { dpm.clearUserRestriction(admin, UserManager.DISALLOW_UNINSTALL_APPS) }
            }

            saveLockState(context, true)
            true
        } catch (_: Exception) {
            saveLockState(context, true)
            true
        }
    }

    // ── Re-verify and re-apply ────────────────────────────────────────────────

    fun reVerifyLock(context: Context, dpm: DevicePolicyManager? = null, admin: ComponentName? = null) {
        if (!isLocked(context)) return
        try {
            // Ensure DNS VPN is running
            DnsVpnService.start(context)

            if (dpm != null && admin != null) {
                runCatching { dpm.setGlobalPrivateDnsModeSpecifiedHost(admin, TARGET_DNS) }
                runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_PRIVATE_DNS) }
                runCatching { dpm.setUninstallBlocked(admin, context.packageName, true) }
                runCatching { dpm.setUninstallBlocked(admin, DHIZUKU_PKG, true) }
                runCatching { dpm.setApplicationHidden(admin, DHIZUKU_PKG, true) }
                runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_APPS_CONTROL) }
                runCatching { dpm.clearUserRestriction(admin, UserManager.DISALLOW_UNINSTALL_APPS) }
                runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET) }
                runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_SAFE_BOOT) }
                runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_VPN) }
                runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_ADD_USER) }
                runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_USB_FILE_TRANSFER) }
            }
        } catch (_: Exception) {}
    }

    // ── Unlock ───────────────────────────────────────────────────────────────

    fun unlockEverything(context: Context, dpm: DevicePolicyManager? = null, admin: ComponentName? = null) {
        try {
            DnsVpnService.stop(context)

            if (dpm != null && admin != null) {
                runCatching { dpm.setGlobalPrivateDnsModeOpportunistic(admin) }
                runCatching { dpm.clearUserRestriction(admin, UserManager.DISALLOW_CONFIG_PRIVATE_DNS) }
                runCatching { dpm.setUninstallBlocked(admin, context.packageName, false) }
                runCatching { dpm.setApplicationHidden(admin, DHIZUKU_PKG, false) }
                runCatching { dpm.setUninstallBlocked(admin, DHIZUKU_PKG, false) }
                runCatching { dpm.clearUserRestriction(admin, UserManager.DISALLOW_APPS_CONTROL) }
                runCatching { dpm.clearUserRestriction(admin, UserManager.DISALLOW_UNINSTALL_APPS) }
                runCatching { dpm.clearUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET) }
                runCatching { dpm.clearUserRestriction(admin, UserManager.DISALLOW_SAFE_BOOT) }
                runCatching { dpm.clearUserRestriction(admin, UserManager.DISALLOW_USB_FILE_TRANSFER) }
                runCatching { dpm.clearUserRestriction(admin, UserManager.DISALLOW_CONFIG_VPN) }
                runCatching { dpm.clearUserRestriction(admin, UserManager.DISALLOW_ADD_USER) }
            }

            saveLockState(context, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ── Network time ─────────────────────────────────────────────────────────

    fun fetchNetworkTimeMs(): Long = NtpClient.nowMs()
}
