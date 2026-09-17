package com.dnsguard.locker

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.app.PendingIntent

/**
 * Feature 10: Home screen countdown widget.
 *
 * Shows days remaining, hours/min/sec, lock status, and a progress bar.
 * Updates automatically every 30 minutes (Android minimum interval).
 * Also updated manually from BootReceiver, NtpSyncWorker, and MainActivity.
 */
class CountdownWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateSingleWidget(context, appWidgetManager, id)
        }
    }

    companion object {
        /**
         * Called from other places (BootReceiver, NtpSyncWorker, MainActivity)
         * to force-refresh all active widget instances.
         */
        fun requestUpdate(context: Context) {
            val manager    = AppWidgetManager.getInstance(context)
            val ids        = manager.getAppWidgetIds(
                ComponentName(context, CountdownWidget::class.java)
            )
            if (ids.isEmpty()) return
            for (id in ids) updateSingleWidget(context, manager, id)
        }

        fun updateSingleWidget(context: Context, mgr: AppWidgetManager, widgetId: Int) {
            val remainingMs    = TimerManager.getRemainingTime(context)
            val bd             = TimerManager.breakdown(remainingMs)
            val isLocked       = DnsLocker.isLocked(context)
            val started        = TimerManager.isStarted(context)
            val yearPassed     = TimerManager.isYearPassed(context)

            val progressPct = if (started) {
                ((TimerManager.ONE_YEAR_MS - remainingMs).toFloat()
                    / TimerManager.ONE_YEAR_MS * 100).toInt().coerceIn(0, 100)
            } else 0

            val views = RemoteViews(context.packageName, R.layout.widget_countdown)

            // Status line
            views.setTextViewText(
                R.id.widget_status,
                if (isLocked) "🔒 LOCKED" else "UNLOCKED"
            )

            // Main day counter
            when {
                started && !yearPassed -> {
                    views.setTextViewText(R.id.widget_days,  bd.days.toString())
                    views.setTextViewText(R.id.widget_label, "DAYS REMAINING")
                    views.setTextViewText(
                        R.id.widget_time,
                        "%02dh %02dm %02ds".format(bd.hours, bd.minutes, bd.seconds)
                    )
                }
                yearPassed -> {
                    views.setTextViewText(R.id.widget_days,  "✓")
                    views.setTextViewText(R.id.widget_label, "YEAR COMPLETE")
                    views.setTextViewText(R.id.widget_time,  "Unlock ready in app")
                }
                else -> {
                    views.setTextViewText(R.id.widget_days,  "365")
                    views.setTextViewText(R.id.widget_label, "DAYS")
                    views.setTextViewText(R.id.widget_time,  "Not started")
                }
            }

            // Progress bar
            views.setProgressBar(R.id.widget_progress, 100, progressPct, false)

            // Tap widget → open MainActivity
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            mgr.updateAppWidget(widgetId, views)
        }
    }
}
