package com.dnsguard.locker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Boot persistence receiver.
 * Triggered on BOOT_COMPLETED and LOCKED_BOOT_COMPLETED.
 *
 * Actions:
 *  1. Restores Local DNS VPN Shield (DnsVpnService)
 *  2. Re-applies Dhizuku DPM restrictions (if available)
 *  3. Starts LockMonitorService watchdog
 *  4. Schedules AlarmManager re-apply chain
 *  5. Schedules WorkManager NTP sync
 *  6. Refreshes countdown widget and morning/nightly notifications
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
                // 1. Always restore DNS VPN Shield
                DnsVpnService.start(context)

                // 2. Check and re-apply Dhizuku if available
                DhizukuHelper.init(context)
                val dpm = DhizukuHelper.getDpm()
                val admin = DhizukuHelper.getAdmin()
                DnsLocker.reVerifyLock(context, dpm, admin)

                // 3. Fetch fresh NTP time
                val networkTime = NtpClient.nowMs()
                TimerManager.updateHighWater(context, networkTime)

                // 4. Start foreground service watchdog
                LockMonitorService.start(context)

                // 5. Start AlarmManager chain
                AlarmScheduler.scheduleReApply(context)

                // 6. Start WorkManager NTP sync
                NtpSyncWorker.schedule(context)

                // 7. Refresh widget
                CountdownWidget.requestUpdate(context)

                // 8. Schedule focus notifications
                NightlyScheduler.scheduleNext(context)
                MorningScheduler.scheduleNext(context)

            } finally {
                pendingResult.finish()
            }
        }
    }
}
