package com.dnsguard.locker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Foreground Service — the core of the self-healing system.
 *
 * Registers a ContentObserver on the Android Private DNS settings.
 * The moment DNS mode or DNS host is changed (by ANY method including ADB),
 * the observer fires and re-applies the restriction within < 1 second.
 *
 * Also re-schedules AlarmManager as a belt-and-suspenders fallback.
 *
 * START_STICKY: Android automatically restarts this service if it's killed.
 */
class LockMonitorService : Service() {

    private val scope       = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var dnsObserver: ContentObserver? = null

    companion object {
        private const val NOTIFICATION_ID = 7623
        private const val CHANNEL_ID      = "dg_monitor"

        fun start(context: Context) {
            if (!DnsLocker.isLocked(context)) return
            val intent = Intent(context, LockMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, LockMonitorService::class.java))
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        if (!DnsLocker.isLocked(this)) {
            stopSelf()
            return
        }

        createChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, buildNotification())
        }

        registerDnsObserver()
        AlarmScheduler.scheduleReApply(this)   // also start alarm chain
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!DnsLocker.isLocked(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        dnsObserver?.let { runCatching { contentResolver.unregisterContentObserver(it) } }
        // Re-schedule alarm so the chain continues even if service dies
        if (DnsLocker.isLocked(this)) {
            AlarmScheduler.scheduleReApply(this)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── ContentObserver: reacts to DNS changes instantly ─────────────────────

    private fun registerDnsObserver() {
        dnsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                // DNS settings changed — fight back immediately
                scope.launch { reApplyNow() }
            }
        }

        // Watch the actual system settings that control Private DNS
        val cr = contentResolver
        runCatching {
            cr.registerContentObserver(
                Settings.Global.getUriFor("private_dns_mode"),
                false, dnsObserver!!
            )
            cr.registerContentObserver(
                Settings.Global.getUriFor("private_dns_specifier"),
                false, dnsObserver!!
            )
        }
    }

    private fun reApplyNow() {
        if (!DnsLocker.isLocked(this)) return
        try {
            DhizukuHelper.init(this)
            val dpm   = DhizukuHelper.getDpm()  ?: return
            val admin = DhizukuHelper.getAdmin() ?: return
            DnsLocker.reVerifyLock(this, dpm, admin)
        } catch (_: Exception) { /* Dhizuku lost authority — nothing we can do */ }
    }

    // ── Notification (required by Android for foreground services) ───────────

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID,
                "DNS Guard",
                NotificationManager.IMPORTANCE_MIN      // no sound, minimal presence
            ).apply {
                description = "Keeps DNS protection active"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DNS Protected")
            .setContentText("high.kahfguard.com • ${TimerManager.breakdown(TimerManager.getRemainingTime(this)).days}d remaining")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)       // cannot be swiped away while foreground service runs
            .setSilent(true)
            .build()
}
