package com.dnsguard.locker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Triggered by AlarmManager every 15 minutes.
 *
 * Actions:
 *  1. Re-applies ALL DPM restrictions (includes re-hiding Dhizuku)
 *  2. Restarts LockMonitorService if it was killed
 *  3. Reschedules next alarm (self-sustaining chain)
 *  4. Updates NTP high-water mark
 *  5. If Accessibility Guard is OFF → fires a dismissible warning notification
 */
class LockReApplyReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID   = "dg_warn"
        const val NOTIF_ID_ACC = 7001

        fun cancelAccessibilityWarning(context: Context) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(NOTIF_ID_ACC)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (!DnsLocker.isLocked(context)) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                DhizukuHelper.init(context)
                if (!DhizukuHelper.isPermissionGranted()) return@launch

                val dpm   = DhizukuHelper.getDpm()  ?: return@launch
                val admin = DhizukuHelper.getAdmin() ?: return@launch

                // 1. Re-apply DPM restrictions (keeps Dhizuku hidden, clears blanket app uninstall lock)
                DnsLocker.reVerifyLock(context, dpm, admin)

                // 2. Update NTP anti-cheat high-water mark
                val networkTime = NtpClient.nowMs()
                TimerManager.updateHighWater(context, networkTime)

                // 3. Restart foreground service if it was killed
                LockMonitorService.start(context)

                // 4. Refresh widget
                CountdownWidget.requestUpdate(context)

                // 5. Check if Accessibility Guard is disabled or in Zombie (frozen) state
                val state = AccessibilityHelper.getAccessibilityState(context)
                if (state != AccessibilityState.ACTIVE) {
                    showAccessibilityWarning(context, state)
                } else {
                    cancelAccessibilityWarning(context)
                }

            } finally {
                // Always reschedule — keeps the alarm chain alive until unlocked
                AlarmScheduler.scheduleReApply(context)
                pendingResult.finish()
            }
        }
    }

    // ── Accessibility warning notification ────────────────────────────────────

    private fun showAccessibilityWarning(context: Context, status: AccessibilityState) {
        createWarningChannel(context)

        // Tapping notification opens Accessibility settings
        val openIntent = PendingIntent.getActivity(
            context, 0,
            Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, shortText, bigText) = when (status) {
            AccessibilityState.ZOMBIE -> Triple(
                "⚠️ Accessibility Guard Frozen",
                "Service disconnected. Tap to toggle OFF then ON.",
                "The Accessibility Guard was disconnected by Android power management. " +
                "In Settings it appears 'ON', but it is currently frozen.\n\n" +
                "Tap 'Fix Now' to turn it OFF and back ON to restore defense."
            )
            else -> Triple(
                "⚠️ DnsGuard: Protection Weakened",
                "Accessibility Guard is OFF. Tap to re-enable and restore full protection.",
                "The Accessibility Guard is disabled. Without it, someone could open Dhizuku " +
                "and click Deactivate.\n\nNote: DNS is still locked via DPM (Dhizuku is hidden). " +
                "Re-enabling Accessibility Guard adds an extra safety layer."
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(shortText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(false)            // dismissible / swipable by user
            .setAutoCancel(true)          // auto clears when clicked
            .setContentIntent(openIntent)
            .addAction(
                android.R.drawable.ic_lock_lock,
                "Fix Now",
                openIntent
            )
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID_ACC, notification)
    }

    private fun createWarningChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID,
                "DnsGuard Warnings",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Security warnings from DnsGuard" }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(ch)
        }
    }
}
