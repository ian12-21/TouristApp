package com.touristapp.kiosk

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.touristapp.admin.KioskAdminReceiver

/**
 * Thin wrapper around [DevicePolicyManager] for Lock Task ("kiosk") mode.
 *
 * The app must first be provisioned as device owner via ADB
 * (`adb shell dpm set-device-owner com.touristapp/.admin.KioskAdminReceiver`).
 * Every call is guarded and wrapped in try/catch so a non-provisioned device never crashes.
 */
class KioskManager(private val activity: Activity) {

    private val dpm: DevicePolicyManager = activity.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    private val adminComponent: ComponentName = ComponentName(activity, KioskAdminReceiver::class.java)

    /** True only if the app was provisioned as device owner via ADB. */
    fun isDeviceOwner(): Boolean = dpm.isDeviceOwnerApp(activity.packageName)

    /**
     * Whitelists this package for lock task and starts it. Safe to call repeatedly.
     * No-op (logs only) when the app is not device owner.
     */
    fun enterKioskMode() {
        if (!isDeviceOwner()) {
            Log.w(TAG, "enterKioskMode skipped: app is not device owner")
            return
        }
        try {
            dpm.setLockTaskPackages(adminComponent, arrayOf(activity.packageName))
            activity.startLockTask()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enter kiosk mode", e)
        }
    }

    /** Stops lock task. Safe to call even when not currently locked. */
    fun exitKioskMode() {
        try {
            activity.stopLockTask()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to exit kiosk mode", e)
        }
    }

    private companion object {
        const val TAG = "KioskManager"
    }
}
