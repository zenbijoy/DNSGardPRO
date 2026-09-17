package com.dnsguard.locker

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * Feature 5: WorkManager periodic worker that fetches real network time
 * every 6 hours in the background — even when the app is closed.
 *
 * This keeps the anti-clock-cheat high-water mark advancing continuously,
 * making it impossible to cheat by rolling the clock back while offline.
 */
class NtpSyncWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        return try {
            val networkTimeMs = NtpClient.nowMs()
            TimerManager.updateHighWater(applicationContext, networkTimeMs)

            // Re-verify all DPM restrictions — detects & fixes ADB-based bypass attempts
            if (DnsLocker.isLocked(applicationContext) && DhizukuHelper.isPermissionGranted()) {
                val dpm   = DhizukuHelper.getDpm()
                val admin = DhizukuHelper.getAdmin()
                if (dpm != null && admin != null) {
                    DhizukuHelper.init(applicationContext)
                    DnsLocker.reVerifyLock(applicationContext, dpm, admin)
                }
            }

            // Refresh widget
            CountdownWidget.requestUpdate(applicationContext)

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "DnsGuard_NtpSync"

        /**
         * Schedules a periodic NTP sync every 6 hours.
         * Uses KEEP policy — calling this multiple times is safe.
         * Only runs when network is connected.
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<NtpSyncWorker>(6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,   // don't reset if already scheduled
                request
            )
        }

        /** Cancel the periodic sync (called on unlock) */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
