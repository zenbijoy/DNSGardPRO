package com.dnsguard.locker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * BroadcastReceiver triggered every night at 9:30 PM.
 * Posts an uplifting short story & focus principle, then chains the next night.
 */
class NightlyNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "dg_nightly_wisdom"
        private const val NOTIFICATION_ID = 8844
    }

    override fun onReceive(context: Context, intent: Intent) {
        val story = NightlyMotivationManager.getTodayStory()
        postNotification(context, story)

        // Reschedule for tomorrow night
        NightlyScheduler.scheduleNext(context)
    }

    private fun postNotification(context: Context, story: NightlyMotivationManager.Story) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Evening Focus & Wisdom",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily evening stories, insights, and principles for focus and growth"
            }
            nm.createNotificationChannel(channel)
        }

        val expandedContent = "${story.categoryEmoji} ${story.category.uppercase()}\n\n${story.body}\n\n✨ MORAL: ${story.moral}"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("${story.emoji} ${story.title} • ${story.categoryEmoji}")
            .setContentText(story.moral)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedContent))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        nm.notify(NOTIFICATION_ID, notification)
    }
}
