package com.dnsguard.locker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * BroadcastReceiver triggered every morning at 8:00 AM.
 * Posts an actionable morning focus directive and chains the next morning.
 */
class MorningNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "dg_morning_focus"
        private const val NOTIFICATION_ID = 8866
    }

    override fun onReceive(context: Context, intent: Intent) {
        val directive = MorningMotivationManager.getTodayDirective()
        postNotification(context, directive)

        // Reschedule for tomorrow morning
        MorningScheduler.scheduleNext(context)
    }

    private fun postNotification(context: Context, directive: MorningMotivationManager.MorningDirective) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Morning Focus & Strategy",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily morning directives and mental models for peak focus"
            }
            nm.createNotificationChannel(channel)
        }

        val expandedContent = "${directive.prompt}\n\n🎯 Today's Action: ${directive.action}"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Morning Focus • ${directive.title}")
            .setContentText(directive.action)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedContent))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        nm.notify(NOTIFICATION_ID, notification)
    }
}
