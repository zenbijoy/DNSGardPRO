package com.dnsguard.locker

import android.content.Context
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

/**
 * Diagnostic helper that checks DNS configuration and live DNS resolution.
 */
object DnsDiagnosticHelper {

    private const val TARGET_HOST = "high.kahfguard.com"

    data class DiagnosticResult(
        val isConfigured: Boolean,
        val isResolving: Boolean,
        val latencyMs: Long,
        val dnsMode: String,
        val dnsHost: String
    )

    suspend fun runDiagnostic(context: Context): DiagnosticResult = withContext(Dispatchers.IO) {
        val cr = context.contentResolver
        val currentMode = Settings.Global.getString(cr, "private_dns_mode") ?: "unknown"
        val currentHost = Settings.Global.getString(cr, "private_dns_specifier") ?: "none"

        val isConfigured = currentMode == "hostname" && currentHost == TARGET_HOST

        var isResolving = false
        var latencyMs = -1L

        try {
            val start = System.currentTimeMillis()
            val address = InetAddress.getByName(TARGET_HOST)
            val end = System.currentTimeMillis()
            if (address.hostAddress != null) {
                isResolving = true
                latencyMs = end - start
            }
        } catch (_: Exception) {
            isResolving = false
            latencyMs = -1L
        }

        DiagnosticResult(
            isConfigured = isConfigured,
            isResolving = isResolving,
            latencyMs = latencyMs,
            dnsMode = currentMode,
            dnsHost = currentHost
        )
    }
}
