package com.dnsguard.locker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * Catches secret codes typed into the phone dialer (e.g. *#*#1234#*#* and *#*#7777#*#*).
 *
 * Modern Android dialers (including Google Phone) strip the asterisks/hashes and
 * broadcast `android.provider.Telephony.SECRET_CODE` with data URI `android_secret_code://<code>`.
 *
 * This receiver launches the target Activity directly, with a high-priority heads-up
 * notification fallback in case the OS enforces background-activity-start restrictions.
 */
class SecretCodeReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "dg_secret_access"
        private const val NOTIF_ID_ACCESS = 8999
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val code = when (action) {
            "android.provider.Telephony.SECRET_CODE" -> {
                intent.data?.host ?: intent.data?.schemeSpecificPart?.trimStart('/') ?: return
            }
            "com.dnsguard.locker.TRIGGER_SECRET_CODE" -> {
                intent.getStringExtra("code") ?: return
            }
            else -> return
        }

        val (targetClass, title) = when (code) {
            "1234" -> Pair(MainActivity::class.java, "DnsGuard Dashboard")
            "7777" -> Pair(EmergencyRecoveryActivity::class.java, "Emergency Recovery")
            else   -> return
        }

        val launchIntent = Intent(context, targetClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        // 1. Attempt direct launch
        val directStarted = runCatching {
            context.startActivity(launchIntent)
            true
        }.getOrDefault(false)

        // 2. High-priority notification fallback (heads-up / fullScreenIntent)
        // Ensures the user can immediately enter even if background launch is throttled
        postAccessNotification(context, launchIntent, title, directStarted)
    }

    private fun postAccessNotification(
        context: Context,
        targetIntent: Intent,
        title: String,
        directStarted: Boolean
    ) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Dialer Code Access",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Quick entry notification when dialer code is entered"
            }
            nm.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            title.hashCode(),
            targetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("🔓 $title")
            .setContentText("Tap to open $title")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setTimeoutAfter(30_000)

        // If direct start did not run, use fullScreenIntent to force heads-up popup
        if (!directStarted) {
            builder.setFullScreenIntent(pendingIntent, true)
        }

        nm.notify(NOTIF_ID_ACCESS, builder.build())
    }
}
