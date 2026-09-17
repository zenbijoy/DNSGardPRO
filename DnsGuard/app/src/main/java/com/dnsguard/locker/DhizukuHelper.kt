package com.dnsguard.locker

import android.content.ComponentName
import android.content.Context
import android.app.admin.DevicePolicyManager
import android.os.IBinder
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.DhizukuBinderWrapper
import com.rosan.dhizuku.api.DhizukuRequestPermissionListener
import org.lsposed.hiddenapibypass.HiddenApiBypass

/**
 * Utility wrapper around the Dhizuku API.
 *
 * Dhizuku is a third-party app that acts as a Device/Profile Owner bridge.
 * It lets non-system apps invoke privileged DevicePolicyManager APIs without
 * the device being rooted — the user activates Dhizuku once via ADB, and then
 * any app it grants permission to can call DPM APIs through it.
 */
object DhizukuHelper {

    private var appContext: Context? = null

    /**
     * Must be called once before any other Dhizuku call.
     * Call this in MainActivity.onCreate() or service/receiver onReceive.
     */
    fun init(context: Context) {
        appContext = context.applicationContext
        runCatching {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                HiddenApiBypass.setHiddenApiExemptions("")
            }
        }
        try {
            Dhizuku.init(context)
        } catch (_: Throwable) {
            // Dhizuku might not be installed or binder unready
        }
    }

    /**
     * Returns true if the user has already granted Dhizuku permission to this app.
     */
    fun isPermissionGranted(): Boolean {
        return try {
            Dhizuku.isPermissionGranted()
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Shows the Dhizuku permission dialog to the user.
     * [onResult] receives true = granted, false = denied.
     */
    fun requestPermission(context: Context, onResult: (Boolean) -> Unit) {
        init(context)
        try {
            Dhizuku.requestPermission(object : DhizukuRequestPermissionListener() {
                override fun onRequestPermission(grantResult: Int) {
                    onResult(grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED)
                }
            })
        } catch (_: Throwable) {
            onResult(false)
        }
    }

    /**
     * Returns a DevicePolicyManager whose calls are automatically proxied through
     * Dhizuku's Device/Profile Owner — i.e., they run with elevated privileges.
     * Returns null if Dhizuku is not installed or permission is not granted.
     */
    fun getDpm(context: Context? = null): DevicePolicyManager? {
        val ctx = context?.applicationContext ?: appContext ?: return null
        val dpm = ctx.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager ?: return null
        return try {
            val field = DevicePolicyManager::class.java.getDeclaredField("mService")
            field.isAccessible = true
            val service = field.get(dpm)
            if (service != null && service !is DhizukuBinderWrapper) {
                val asBinderMethod = service.javaClass.getMethod("asBinder")
                val binder = asBinderMethod.invoke(service) as IBinder
                val wrapped = Dhizuku.binderWrapper(binder)
                val stubClass = Class.forName("android.app.admin.IDevicePolicyManager\$Stub")
                val asInterface = stubClass.getMethod("asInterface", IBinder::class.java)
                val proxied = asInterface.invoke(null, wrapped)
                field.set(dpm, proxied)
            }
            dpm
        } catch (_: Throwable) {
            dpm
        }
    }

    /**
     * Returns the ComponentName that Dhizuku registered as the active admin.
     * Catches Throwable to prevent java.lang.AssertionError from killing the process.
     */
    fun getAdmin(): ComponentName? {
        return try {
            Dhizuku.getOwnerComponent()
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Quick sanity check: permission is granted AND we can retrieve a valid DPM + admin.
     */
    fun isFullyReady(): Boolean {
        return isPermissionGranted() && getDpm() != null && getAdmin() != null
    }
}
