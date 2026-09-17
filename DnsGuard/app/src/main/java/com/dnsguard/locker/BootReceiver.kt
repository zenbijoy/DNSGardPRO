package com.dnsguard.locker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Feature 1: Boot persistence.
 * Triggered on BOOT_COMPLETED and LOCKED_BOOT_COMPLETED.
 *
 * On every boot:
 *  1. Re-applies all DPM restrictions
 *  2. Starts LockMonitorService (ContentObserver real-time watchdog)
 *  3. Schedules AlarmManager chain (every 15 min)
 *  4. Schedules WorkManager NTP sync (every 6 hrs)
 *  5. Updates NTP high-water mark
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != "android.intent.action.LOCKED_BOOT_COMPLETED") return

        if (!DnsLocker.isLocked(context)) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                DhizukuHelper.init(context)
                if (!DhizukuHelper.isPermissionGranted()) return@launch

                val dpm   = DhizukuHelper.getDpm()  ?: return@launch
                val admin = DhizukuHelper.getAdmin() ?: return@launch

                // 1. Re-apply all restrictions
                DnsLocker.lockEverything(context, dpm, admin)

                // 2. Fetch fresh NTP time
                val networkTime = NtpClient.nowMs()
                TimerManager.updateHighWater(context, networkTime)

                // 3. Start foreground service (ContentObserver real-time watchdog)
                LockMonitorService.start(context)

                // 4. Start AlarmManager chain (every 15 minutes)
                AlarmScheduler.scheduleReApply(context)

                // 5. Start WorkManager NTP sync (every 6 hours)
                NtpSyncWorker.schedule(context)

                // 6. Refresh widget
                CountdownWidget.requestUpdate(context)

                // 7. Schedule nightly encouragement notification (9:30 PM)
                NightlyScheduler.scheduleNext(context)

                // 8. Schedule morning focus directive notification (8:00 AM)
                MorningScheduler.scheduleNext(context)

            } finally {
                pendingResult.finish()
            }
        }
    }
}
