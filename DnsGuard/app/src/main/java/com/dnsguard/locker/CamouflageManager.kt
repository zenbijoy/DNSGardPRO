package com.dnsguard.locker

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

/**
 * Manages dynamic app icon and label camouflage using Android activity-aliases.
 * 100% compliant with Google Play Store policies and functional across all Android versions (10–14+).
 */
object CamouflageManager {

    enum class CamouflageMode(
        val id: String,
        val aliasClassName: String,
        val displayName: String,
        val subtitle: String,
        val iconRes: Int
    ) {
        DEFAULT(
            id = "default",
            aliasClassName = "com.dnsguard.locker.MainActivityDefault",
            displayName = "DNSGuard PRO",
            subtitle = "Official Brand Shield & Globe",
            iconRes = R.drawable.ic_brand_logo
        ),
        HABIT(
            id = "habit",
            aliasClassName = "com.dnsguard.locker.MainActivityHabit",
            displayName = "Habit Tracker",
            subtitle = "Routine progress ring & checkmark",
            iconRes = R.drawable.ic_camo_habit
        ),
        STUDY(
            id = "study",
            aliasClassName = "com.dnsguard.locker.MainActivityStudy",
            displayName = "Study Focus",
            subtitle = "Academic open notebook & focus star",
            iconRes = R.drawable.ic_camo_study
        ),
        HEALTH(
            id = "health",
            aliasClassName = "com.dnsguard.locker.MainActivityHealth",
            displayName = "Health Pulse",
            subtitle = "Wellness ECG pulse & health monitor",
            iconRes = R.drawable.ic_camo_health
        ),
        CALC(
            id = "calc",
            aliasClassName = "com.dnsguard.locker.MainActivityCalc",
            displayName = "Math Tools",
            subtitle = "Minimalist arithmetic calculator utility",
            iconRes = R.drawable.ic_camo_calc
        );

        companion object {
            fun fromId(id: String): CamouflageMode {
                return entries.firstOrNull { it.id == id } ?: DEFAULT
            }
        }
    }

    private const val PREFS_NAME = "dnsguard_camouflage_prefs"
    private const val KEY_ACTIVE_CAMO = "active_camouflage_mode"

    fun getActiveMode(context: Context): CamouflageMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY_ACTIVE_CAMO, CamouflageMode.DEFAULT.id) ?: CamouflageMode.DEFAULT.id
        return CamouflageMode.fromId(id)
    }

    /**
     * Safely switches the launcher icon and label.
     * Crucial: Always ENABLES the new alias BEFORE disabling others to guarantee 0-icon glitch never occurs.
     */
    fun setCamouflageMode(context: Context, mode: CamouflageMode): Boolean {
        return try {
            val pm = context.packageManager
            val targetComponent = ComponentName(context, mode.aliasClassName)

            // 1. Enable target component first
            pm.setComponentEnabledSetting(
                targetComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            // 2. Disable all other aliases
            for (other in CamouflageMode.entries) {
                if (other != mode) {
                    val otherComponent = ComponentName(context, other.aliasClassName)
                    pm.setComponentEnabledSetting(
                        otherComponent,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
            }

            // 3. Persist selection
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_ACTIVE_CAMO, mode.id)
                .apply()

            true
        } catch (_: Exception) {
            false
        }
    }
}
