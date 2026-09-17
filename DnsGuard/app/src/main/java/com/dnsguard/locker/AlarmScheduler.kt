package com.dnsguard.locker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock

/**
 * Schedules a repeating AlarmManager-based restriction re-apply.
 * AlarmManager fires even in Doze mode (setExactAndAllowWhileIdle).
 * This is independent of WorkManager — two separate timers.
 */
object AlarmScheduler {

    private const val INTERVAL_MS  = 15L * 60 * 1_000   // every 15 minutes
    private const val REQUEST_CODE = 0xDEF

    fun scheduleReApply(context: Context) {
        val intent = buildIntent(context)
        val triggerAt = SystemClock.elapsedRealtime() + INTERVAL_MS
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            // Fires even when device is in deep Doze/idle mode
            am.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, intent
            )
        } catch (_: SecurityException) {
            // Android 12+ may require SCHEDULE_EXACT_ALARM — fallback to inexact
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, intent)
        }
    }

    fun cancel(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(buildIntent(context))
    }

    private fun buildIntent(context: Context) = PendingIntent.getBroadcast(
        context, REQUEST_CODE,
        Intent(context, LockReApplyReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
