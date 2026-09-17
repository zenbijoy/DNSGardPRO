package com.dnsguard.locker

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages optional strictness preferences for DNSGuard PRO.
 * Keeps developer mode and USB debugging optional so that normal charging,
 * USB file transfer, and developer workflows are not blocked by default.
 */
object SecurityPreferences {
    private const val PREFS_NAME = "dnsguard_security_prefs"
    private const val KEY_BLOCK_DEV_OPTIONS = "block_developer_options_and_usb"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Whether Developer Options & USB Debugging blocking is active.
     * Default: FALSE (optional), ensuring USB charging, OTG, and developer features work freely.
     */
    fun isBlockDeveloperOptionsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_BLOCK_DEV_OPTIONS, false)
    }

    /**
     * Updates the preference for Developer Options & USB Debugging blocking.
     */
    fun setBlockDeveloperOptionsEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BLOCK_DEV_OPTIONS, enabled).apply()
    }
}
