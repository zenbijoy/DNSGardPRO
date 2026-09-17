package com.dnsguard.locker

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

/**
 * Feature 6: Robust NTP time client.
 *
 * Priority order:
 *  1. UDP NTP (most accurate — direct socket to port 123)
 *  2. HTTP Date header fallback (if UDP is blocked by firewall/ISP)
 *  3. System.currentTimeMillis() last resort
 *
 * ALL methods are blocking — always call from Dispatchers.IO.
 */
object NtpClient {

    private const val NTP_PACKET_SIZE  = 48
    private const val NTP_PORT         = 123
    private const val NTP_EPOCH_OFFSET = 2_208_988_800L  // seconds: 1900-01-01 → 1970-01-01
    private const val TIMEOUT_MS       = 5_000

    /** Tier 1: NTP UDP servers (in priority order) */
    private val NTP_SERVERS = listOf(
        "time.google.com",
        "time.cloudflare.com",
        "pool.ntp.org",
        "time.apple.com",
        "time.windows.com"
    )

    /** Tier 2: HTTP fallback endpoints */
    private val HTTP_ENDPOINTS = listOf(
        "https://www.google.com",
        "https://www.cloudflare.com",
        "https://www.apple.com"
    )

    /**
     * Returns current UTC time in milliseconds.
     * Tries NTP UDP first, then HTTP Date header, then device clock.
     */
    fun nowMs(): Long {
        // — Tier 1: NTP UDP —
        for (server in NTP_SERVERS) {
            try {
                val t = queryNtp(server)
                if (t > 1_000_000_000_000L) return t   // sanity: after year 2001
            } catch (_: Exception) { /* try next */ }
        }

        // — Tier 2: HTTP Date header —
        for (url in HTTP_ENDPOINTS) {
            try {
                val t = queryHttp(url)
                if (t > 1_000_000_000_000L) return t
            } catch (_: Exception) { /* try next */ }
        }

        // — Last resort: device clock —
        return System.currentTimeMillis()
    }

    // ── Private: NTP UDP query ────────────────────────────────────────────────

    private fun queryNtp(host: String): Long {
        val socket = DatagramSocket()
        socket.soTimeout = TIMEOUT_MS
        return try {
            // Build NTP request packet: LI=0, Version=3, Mode=3 (client)
            val sendBuffer = ByteArray(NTP_PACKET_SIZE)
            sendBuffer[0] = 0x1B.toByte()

            val address = InetAddress.getByName(host)
            socket.send(DatagramPacket(sendBuffer, sendBuffer.size, address, NTP_PORT))

            // Receive response
            val recvBuffer = ByteArray(NTP_PACKET_SIZE)
            socket.receive(DatagramPacket(recvBuffer, recvBuffer.size))

            // Transmit Timestamp (T4) is at bytes 40–43 (seconds since 1900)
            var seconds = 0L
            for (i in 40..43) {
                seconds = (seconds shl 8) or (recvBuffer[i].toLong() and 0xFF)
            }

            (seconds - NTP_EPOCH_OFFSET) * 1_000L      // convert to Unix ms
        } finally {
            runCatching { socket.close() }
        }
    }

    // ── Private: HTTP Date header query ──────────────────────────────────────

    private fun queryHttp(url: String): Long {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod  = "HEAD"
        conn.connectTimeout = 4_000
        conn.readTimeout    = 4_000
        return try {
            conn.connect()
            val dateHeader = conn.getHeaderField("Date") ?: return -1L
            @Suppress("DEPRECATION")
            java.util.Date(dateHeader).time
        } finally {
            runCatching { conn.disconnect() }
        }
    }
}
