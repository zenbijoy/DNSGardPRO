package com.dnsguard.locker

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * Helper to check and request Device Administrator rights.
 * Standard Android OS feature — requires NO PC, NO USB, NO Dhizuku.
 * Once granted, Android natively blocks standard uninstallation.
 */
object DeviceAdminManager {

    fun getAdminComponent(context: Context): ComponentName =
        ComponentName(context, MyDeviceAdminReceiver::class.java)

    /** Returns true if DnsGuard is currently an active Device Administrator. */
    fun isAdminActive(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager ?: return false
        return dpm.isAdminActive(getAdminComponent(context))
    }

    /** Returns an Intent to launch the system prompt to grant Device Administrator. */
    fun getRequestAdminIntent(context: Context): Intent {
        return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, getAdminComponent(context))
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "DnsGuard requires Device Administrator to prevent accidental or impulsive uninstallation during your 1-year freedom journey."
            )
        }
    }
}
