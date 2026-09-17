package com.dnsguard.locker

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar

/**
 * Manages the multi-condition emergency recovery gate.
 *
 * Strict conditions enforced:
 *  1. Allowed time window: 10:00 AM – 6:00 PM (night/early morning access strictly blocked)
 *  2. Mandatory 24-Hour Cooling-Off period (anti-impulse buffer)
 *  3. Cognitive math challenge
 *  4. Manual typed statement (no clipboard paste)
 */
object RecoveryManager {

    private const val PREFS_NAME = "dg_recovery"
    private const val KEY_COOL_OFF_START = "cool_off_start"
    const val COOL_OFF_DURATION_MS = 24L * 60L * 60L * 1000L // 24 hours

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Returns true if current local time is between 10:00 AM (10) and 6:00 PM (18). */
    fun isWithinAllowedHours(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour in 10..17 // 10:00 to 17:59
    }

    /** Initiates the 24-hour cooling-off countdown. */
    fun initiateCoolOff(context: Context, nowMs: Long = System.currentTimeMillis()) {
        if (getCoolOffStartTime(context) == 0L) {
            prefs(context).edit().putLong(KEY_COOL_OFF_START, nowMs).apply()
        }
    }

    fun getCoolOffStartTime(context: Context): Long =
        prefs(context).getLong(KEY_COOL_OFF_START, 0L)

    fun isCoolOffInitiated(context: Context): Boolean =
        getCoolOffStartTime(context) > 0L

    /** Returns remaining ms of the 24-hour cooling-off period. */
    fun getCoolOffRemainingMs(context: Context): Long {
        val start = getCoolOffStartTime(context)
        if (start == 0L) return COOL_OFF_DURATION_MS
        val elapsed = System.currentTimeMillis() - start
        val remaining = COOL_OFF_DURATION_MS - elapsed
        return if (remaining > 0L) remaining else 0L
    }

    fun isCoolOffComplete(context: Context): Boolean =
        isCoolOffInitiated(context) && getCoolOffRemainingMs(context) <= 0L

    /** Cancels the emergency request. */
    fun cancelCoolOff(context: Context) {
        prefs(context).edit().remove(KEY_COOL_OFF_START).apply()
    }
}
