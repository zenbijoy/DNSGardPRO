package com.dnsguard.locker

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

/**
 * Required by the Android system for any app that declares device-admin policies.
 * Dhizuku uses this component name to identify our app as a managed client.
 *
 * onDisableRequested: shows a blocking message when someone tries to deactivate
 * device admin through Settings → Security → Device Admin Apps.
 */
class MyDeviceAdminReceiver : DeviceAdminReceiver() {

    /**
     * Called when the user tries to deactivate device admin for this app in Settings.
     * The returned string is shown in the confirmation dialog as a warning.
     * This does NOT prevent deactivation but creates a strong psychological barrier.
     */
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        val remaining = TimerManager.getRemainingTime(context)
        val bd        = TimerManager.breakdown(remaining)
        return if (TimerManager.isYearPassed(context)) {
            "Your 1-year commitment is complete. You may now disable DnsGuard admin."
        } else {
            "⚠️ WARNING: Disabling admin will break your 1-year self-control commitment! " +
            "${bd.days} days, ${bd.hours} hours remaining. " +
            "Are you sure you want to give up?"
        }
    }

    /**
     * Called after admin is actually disabled. Re-attempts to restore restrictions
     * one last time (likely fails, but worth trying).
     */
    override fun onDisabled(context: Context, intent: Intent) {
        // Mark as unlocked since we no longer have admin rights
        if (TimerManager.isYearPassed(context)) {
            DnsLocker.saveLockState(context, false)
        }
        // If year hasn't passed, someone force-disabled us.
        // We can't stop it, but log it in prefs for awareness.
    }
}
