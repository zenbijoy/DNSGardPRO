package com.dnsguard.locker

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.os.UserManager

/**
 * Encapsulates all DevicePolicyManager operations.
 * Uses NtpClient (Feature 6) for network time — no inline HTTP here.
 *
 * IMPORTANT: lockEverything() and unlockEverything() are blocking.
 * Always call from Dispatchers.IO / a background thread.
 */
object DnsLocker {

    private const val TARGET_DNS      = "high.kahfguard.com"
    private const val DHIZUKU_PKG     = "com.rosan.dhizuku"   // Dhizuku package name
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

    /** Returns true if Dhizuku is still installed on the device. */
    fun isDhizukuInstalled(context: Context): Boolean = try {
        context.packageManager.getPackageInfo(DHIZUKU_PKG, 0)
        true
    } catch (_: Exception) { false }

    // ── Core lock ────────────────────────────────────────────────────────────

    fun lockEverything(context: Context, dpm: DevicePolicyManager, admin: ComponentName): Boolean {
        return try {
            // 1. Set Private DNS host
            val dnsResult = dpm.setGlobalPrivateDnsModeSpecifiedHost(admin, TARGET_DNS)
            if (dnsResult != DevicePolicyManager.PRIVATE_DNS_SET_NO_ERROR) return false

            // 2. Block user from changing DNS in Settings
            dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_PRIVATE_DNS)

            // 3. Block DnsGuard uninstallation
            dpm.setUninstallBlocked(admin, context.packageName, true)

            // 4. Block Dhizuku uninstallation
            runCatching { dpm.setUninstallBlocked(admin, DHIZUKU_PKG, true) }

            // 5. *** KEY FIX: Hide Dhizuku's entire UI ***
            //    User can never open Dhizuku app → cannot see the Deactivate button
            runCatching { dpm.setApplicationHidden(admin, DHIZUKU_PKG, true) }

            // 6. Block Factory Reset
            dpm.addUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET)

            // 7. Block Safe Mode boot
            runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_SAFE_BOOT) }

            // 8. Block USB file transfer
            runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_USB_FILE_TRANSFER) }

            // 9. Block VPN configuration (prevents DNS bypass via VPN apps)
            runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_VPN) }

            // 10. Block adding new users / switching profiles
            runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_ADD_USER) }

            // 11. Block app controls (Clear Storage / Clear Data, Clear Cache, Force Stop)
            runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_APPS_CONTROL) }

            // 12. Ensure device-wide uninstallation is NOT blocked (allows normal apps to be uninstalled)
            runCatching { dpm.clearUserRestriction(admin, UserManager.DISALLOW_UNINSTALL_APPS) }

            saveLockState(context, true)
            true
        } catch (e: SecurityException) {
            false
        }
    }

    // ── Re-verify and re-apply ────────────────────────────────────────────────

    fun reVerifyLock(context: Context, dpm: DevicePolicyManager, admin: ComponentName) {
        if (!isLocked(context)) return
        try {
            runCatching { dpm.setGlobalPrivateDnsModeSpecifiedHost(admin, TARGET_DNS) }
            runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_PRIVATE_DNS) }
            runCatching { dpm.setUninstallBlocked(admin, context.packageName, true) }
            runCatching { dpm.setUninstallBlocked(admin, DHIZUKU_PKG, true) }
            runCatching { dpm.setApplicationHidden(admin, DHIZUKU_PKG, true) }  // keep Dhizuku hidden
            runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_APPS_CONTROL) }
            runCatching { dpm.clearUserRestriction(admin, UserManager.DISALLOW_UNINSTALL_APPS) }
            runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET) }
            runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_SAFE_BOOT) }
            runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_VPN) }
            runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_ADD_USER) }
            runCatching { dpm.addUserRestriction(admin, UserManager.DISALLOW_USB_FILE_TRANSFER) }
        } catch (_: Exception) {}
    }

    // ── Unlock ───────────────────────────────────────────────────────────────

    fun unlockEverything(context: Context, dpm: DevicePolicyManager, admin: ComponentName) {
        try {
            dpm.setGlobalPrivateDnsModeOpportunistic(admin)
            dpm.clearUserRestriction(admin, UserManager.DISALLOW_CONFIG_PRIVATE_DNS)
            dpm.setUninstallBlocked(admin, context.packageName, false)

            // Unhide Dhizuku so it can be managed again
            runCatching { dpm.setApplicationHidden(admin, DHIZUKU_PKG, false) }
            runCatching { dpm.setUninstallBlocked(admin, DHIZUKU_PKG, false) }

            // Remove all other restrictions
            runCatching { dpm.clearUserRestriction(admin, UserManager.DISALLOW_APPS_CONTROL) }
            runCatching { dpm.clearUserRestriction(admin, UserManager.DISALLOW_UNINSTALL_APPS) }
            dpm.clearUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET)
            runCatching { dpm.clearUserRestriction(admin, UserManager.DISALLOW_SAFE_BOOT) }
            runCatching { dpm.clearUserRestriction(admin, UserManager.DISALLOW_USB_FILE_TRANSFER) }
            runCatching { dpm.clearUserRestriction(admin, UserManager.DISALLOW_CONFIG_VPN) }
            runCatching { dpm.clearUserRestriction(admin, UserManager.DISALLOW_ADD_USER) }

            saveLockState(context, false)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // ── Network time (delegates to NtpClient — Feature 6) ───────────────────

    /**
     * Fetches real-world UTC time. Uses NtpClient which tries UDP NTP first,
     * then HTTP Date header fallback. Must be called from a background thread.
     */
    fun fetchNetworkTimeMs(): Long = NtpClient.nowMs()
}
