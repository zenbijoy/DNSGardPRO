package com.dnsguard.locker

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.File

/**
 * Feature 4 + 7: Timer storage with EncryptedSharedPreferences + file backup.
 *
 * Storage layers (all three are written on every save):
 *   1. EncryptedSharedPreferences  — AES-256-GCM, tamper-proof
 *   2. Plain SharedPreferences      — fallback if key store gets corrupted
 *   3. Binary file in filesDir      — survives some edge cases
 *
 * On read, takes the SMALLEST non-zero value across all sources
 * (earliest start time = most restrictive).
 *
 * Anti-clock-cheat: high-water mark tracks the highest network time ever seen.
 * Rolling back the device clock has zero effect.
 */
object TimerManager {

    private const val SECURE_PREFS_NAME = "dg_secure"      // EncryptedSharedPreferences
    private const val BACKUP_PREFS_NAME = "dg_backup"      // plain fallback prefs
    private const val BACKUP_FILE_NAME  = ".dg_ts"         // binary file backup

    private const val KEY_START_TIME   = "st"
    private const val KEY_HIGH_WATER   = "hw"

    const val ONE_YEAR_MS = 365L * 24L * 60L * 60L * 1_000L   // 365 days in ms

    // ── SharedPreferences helpers ────────────────────────────────────────────

    /** Primary: EncryptedSharedPreferences (AES-256-GCM). Falls back to plain if unavailable. */
    private fun securePrefs(context: Context): SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            SECURE_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (_: Exception) {
        // Key store corrupted / device reset — fall back gracefully
        context.getSharedPreferences(BACKUP_PREFS_NAME, Context.MODE_PRIVATE)
    }

    /** Secondary: plain SharedPreferences (different file name from secure) */
    private fun backupPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(BACKUP_PREFS_NAME, Context.MODE_PRIVATE)

    /** Tertiary: binary file in internal storage */
    private fun backupFile(context: Context): File = File(context.filesDir, BACKUP_FILE_NAME)

    // ── Write (always write to ALL three layers) ─────────────────────────────

    private fun writeLong(context: Context, key: String, value: Long) {
        securePrefs(context).edit().putLong(key, value).apply()
        backupPrefs(context).edit().putLong(key, value).apply()
        // File backup: key byte + 8-byte big-endian long
        try {
            val file = backupFile(context)
            val existing = readFileMap(file).toMutableMap()
            existing[key] = value
            writeFileMap(file, existing)
        } catch (_: Exception) {}
    }

    // ── File map read/write (simple custom format) ───────────────────────────

    private fun readFileMap(file: File): Map<String, Long> {
        if (!file.exists()) return emptyMap()
        return try {
            val bytes = file.readBytes()
            val map   = mutableMapOf<String, Long>()
            var i = 0
            while (i + 9 <= bytes.size) {
                val keyByte = bytes[i].toInt() and 0xFF
                val key     = if (keyByte == 0) KEY_START_TIME else KEY_HIGH_WATER
                var v       = 0L
                for (j in 1..8) v = (v shl 8) or (bytes[i + j].toLong() and 0xFF)
                map[key]   = v
                i += 9
            }
            map
        } catch (_: Exception) { emptyMap() }
    }

    private fun writeFileMap(file: File, map: Map<String, Long>) {
        val bytes = ByteArray(map.size * 9)
        var i = 0
        for ((k, v) in map) {
            bytes[i] = (if (k == KEY_START_TIME) 0 else 1).toByte()
            for (j in 7 downTo 0) bytes[i + 1 + (7 - j)] = ((v shr (j * 8)) and 0xFF).toByte()
            i += 9
        }
        file.writeBytes(bytes)
    }

    // ── Read (across all layers; picks most restrictive value) ───────────────

    private fun readLong(context: Context, key: String, pickMin: Boolean): Long {
        val fromSecure = try { securePrefs(context).getLong(key, 0L) } catch (_: Exception) { 0L }
        val fromBackup = backupPrefs(context).getLong(key, 0L)
        val fromFile   = try { readFileMap(backupFile(context))[key] ?: 0L } catch (_: Exception) { 0L }

        val candidates = listOf(fromSecure, fromBackup, fromFile).filter { it > 0L }
        if (candidates.isEmpty()) return 0L
        return if (pickMin) candidates.min() else candidates.max()
    }

    private fun readStartTime(context: Context) = readLong(context, KEY_START_TIME, pickMin = true)
    private fun readHighWater(context: Context)  = readLong(context, KEY_HIGH_WATER, pickMin = false)

    // ── Anti-clock-cheat ─────────────────────────────────────────────────────

    /** Call every time a network timestamp is fetched. Ratchets forward only. */
    fun updateHighWater(context: Context, networkNowMs: Long) {
        if (networkNowMs > readHighWater(context)) {
            writeLong(context, KEY_HIGH_WATER, networkNowMs)
        }
    }

    /** Trusted "now" = max(device clock, stored high-water mark). */
    private fun trustedNow(context: Context): Long =
        maxOf(System.currentTimeMillis(), readHighWater(context))

    // ── Timer lifecycle ──────────────────────────────────────────────────────

    /** Starts the 1-year timer. Idempotent — safe to call multiple times. */
    fun startTimer(context: Context) {
        if (readStartTime(context) == 0L) {
            writeLong(context, KEY_START_TIME, trustedNow(context))
        }
    }

    fun isStarted(context: Context): Boolean = readStartTime(context) != 0L

    // ── Countdown ────────────────────────────────────────────────────────────

    fun getRemainingTime(context: Context): Long {
        val startTime = readStartTime(context)
        if (startTime == 0L) return ONE_YEAR_MS
        val remaining = (startTime + ONE_YEAR_MS) - trustedNow(context)
        return if (remaining > 0L) remaining else 0L
    }

    fun isYearPassed(context: Context): Boolean = getRemainingTime(context) <= 0L

    /** 0.0 → 1.0 progress through the full 365 days. */
    fun progressFraction(context: Context): Float {
        if (!isStarted(context)) return 0f
        val elapsed = ONE_YEAR_MS - getRemainingTime(context)
        return (elapsed.toFloat() / ONE_YEAR_MS).coerceIn(0f, 1f)
    }

    // ── Display breakdown ────────────────────────────────────────────────────

    data class Breakdown(val days: Long, val hours: Long, val minutes: Long, val seconds: Long)

    fun breakdown(remainingMs: Long): Breakdown {
        var r = remainingMs
        val d = r / (1_000L * 60 * 60 * 24); r -= d * 1_000L * 60 * 60 * 24
        val h = r / (1_000L * 60 * 60);       r -= h * 1_000L * 60 * 60
        val m = r / (1_000L * 60);             r -= m * 1_000L * 60
        val s = r / 1_000L
        return Breakdown(d, h, m, s)
    }
}
