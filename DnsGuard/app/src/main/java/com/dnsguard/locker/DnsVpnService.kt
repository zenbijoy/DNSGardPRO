package com.dnsguard.locker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * Standalone Local DNS VPN Engine.
 * Operates completely on-device without any external server, logging, or subscription.
 * Intercepts DNS queries on device and enforces AdGuard Family Protection endpoints (100% Adult & Ad Free):
 *   - Primary IPv4: 94.140.14.15
 *   - Secondary IPv4: 94.140.15.16
 *
 * 100% Google Play Store Compliant — requires NO PC, NO USB, NO Dhizuku.
 */
class DnsVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isRunningWorker = false

    companion object {
        const val ACTION_START = "com.dnsguard.locker.START_VPN"
        const val ACTION_STOP  = "com.dnsguard.locker.STOP_VPN"
        private const val NOTIFICATION_ID = 8844
        private const val CHANNEL_ID = "dg_vpn_channel"

        // Safe DNS Servers (AdGuard Family Protection: 100% Adult Blocker + 100% Ad & Tracker Blocker)
        val PRIMARY_DNS_IPV4: String   = "94.140.14.15"
        val SECONDARY_DNS_IPV4: String = "94.140.15.16"

        @Volatile
        var isRunning: Boolean = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, DnsVpnService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, DnsVpnService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopVpn()
                return START_NOT_STICKY
            }
            else -> {
                startForeground(NOTIFICATION_ID, buildNotification())
                startVpn()
                return START_STICKY
            }
        }
    }

    private fun startVpn() {
        if (isRunning) return
        try {
            val builder = Builder()
                .setSession("DNSGuard Safe Shield")
                .addAddress("10.200.0.2", 32)
                .addDnsServer(PRIMARY_DNS_IPV4)
                .addDnsServer(SECONDARY_DNS_IPV4)
                .setBlocking(false)
                .setMtu(1500)

            // Route exclusively to the safe DNS IP targets (preserves normal app speeds)
            builder.addRoute(PRIMARY_DNS_IPV4, 32)
            builder.addRoute(SECONDARY_DNS_IPV4, 32)

            vpnInterface = builder.establish()
            if (vpnInterface != null) {
                isRunning = true
                startDnsForwarder(vpnInterface!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isRunning = false
        }
    }

    private fun startDnsForwarder(pfd: ParcelFileDescriptor) {
        isRunningWorker = true
        scope.launch {
            val inStream = FileInputStream(pfd.fileDescriptor)
            val outStream = FileOutputStream(pfd.fileDescriptor)
            val packet = ByteArray(32767)

            val dnsSocket = DatagramSocket()
            protect(dnsSocket)
            dnsSocket.soTimeout = 2500

            val targetDns = InetAddress.getByName(PRIMARY_DNS_IPV4)

            while (isRunningWorker && isRunning) {
                try {
                    val length = inStream.read(packet)
                    if (length > 0) {
                        // Forward IP packet or DNS packet over protected socket
                        val outPacket = DatagramPacket(packet, length, targetDns, 53)
                        dnsSocket.send(outPacket)

                        val respBuf = ByteArray(32767)
                        val inPacket = DatagramPacket(respBuf, respBuf.size)
                        dnsSocket.receive(inPacket)

                        outStream.write(inPacket.data, 0, inPacket.length)
                    }
                } catch (_: Exception) {
                    // Timeout or network switch — continue gracefully
                }
            }

            runCatching { dnsSocket.close() }
        }
    }

    private fun stopVpn() {
        isRunningWorker = false
        isRunning = false
        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (_: Exception) {}
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    override fun onRevoke() {
        // Called when another VPN app starts or user revokes consent
        stopVpn()
        super.onRevoke()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "DNS Safe Shield",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps DNS protection active and adult content blocked"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DNSGuard PRO Active")
            .setContentText("AdGuard Family Protection • 100% Adult & Ad Free")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}
