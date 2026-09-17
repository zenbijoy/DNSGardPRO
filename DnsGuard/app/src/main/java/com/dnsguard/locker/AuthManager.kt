package com.dnsguard.locker

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

/**
 * Manages user authentication (Master Security PIN) using EncryptedSharedPreferences (AES-256).
 * Ensures only the authentic user can access the app dashboard, change settings,
 * or seal the 1-year commitment.
 */
object AuthManager {

    private const val PREFS_NAME = "dg_auth_secure"
    private const val KEY_PIN_HASH = "pin_hash"
    private const val KEY_SALT = "pin_salt"
    private const val KEY_PLEDGE_SIGNED = "pledge_signed"

    @Volatile
    var isAuthenticatedSession: Boolean = false

    private fun getSecurePrefs(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Exception) {
            context.getSharedPreferences("dg_auth_fallback", Context.MODE_PRIVATE)
        }
    }

    private const val KEY_PIN_LENGTH = "pin_len"

    /** Returns true if a Master PIN has been created by the user. */
    fun isPinSet(context: Context): Boolean {
        val prefs = getSecurePrefs(context)
        val hash = prefs.getString(KEY_PIN_HASH, null)
        return !hash.isNullOrEmpty()
    }

    fun getPinLength(context: Context): Int {
        return getSecurePrefs(context).getInt(KEY_PIN_LENGTH, 4)
    }

    /**
     * Sets or updates the Master PIN with SHA-256 hashing.
     */
    fun setMasterPin(context: Context, pin: String) {
        val prefs = getSecurePrefs(context)
        val salt = System.currentTimeMillis().toString()
        val hash = hashPin(pin, salt)
        prefs.edit()
            .putString(KEY_PIN_HASH, hash)
            .putString(KEY_SALT, salt)
            .putInt(KEY_PIN_LENGTH, pin.length)
            .apply()
        isAuthenticatedSession = true
    }

    /**
     * Verifies the provided PIN against the stored hash.
     */
    fun verifyPin(context: Context, pin: String): Boolean {
        val prefs = getSecurePrefs(context)
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val salt = prefs.getString(KEY_SALT, "") ?: ""
        val inputHash = hashPin(pin, salt)
        val matches = storedHash == inputHash
        if (matches) {
            isAuthenticatedSession = true
        }
        return matches
    }

    /** Records that the user signed the 1-year strict pledge. */
    fun recordPledgeSigned(context: Context) {
        getSecurePrefs(context).edit().putBoolean(KEY_PLEDGE_SIGNED, true).apply()
    }

    fun isPledgeSigned(context: Context): Boolean {
        return getSecurePrefs(context).getBoolean(KEY_PLEDGE_SIGNED, false)
    }

    private fun hashPin(pin: String, salt: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest("$pin:$salt:dnsguard_ironclad".toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
