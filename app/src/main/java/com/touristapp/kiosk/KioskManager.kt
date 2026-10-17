package com.touristapp.kiosk

import android.app.Activity
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.UserManager
import android.provider.Settings
import android.util.Log
import com.touristapp.admin.KioskAdminReceiver

/**
 * Thin wrapper around [DevicePolicyManager] for Lock Task ("kiosk") mode.
 *
 * The app must first be provisioned as device owner via ADB
 * (`adb shell dpm set-device-owner com.touristapp.staging/com.touristapp.admin.KioskAdminReceiver`).
 * Every call is guarded and wrapped in try/catch so a non-provisioned device never crashes.
 */
class KioskManager(private val activity: Activity) {

    private val dpm: DevicePolicyManager = activity.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    private val adminComponent: ComponentName = ComponentName(activity, KioskAdminReceiver::class.java)

    /** The HOME activity-alias, disabled in the manifest and toggled with kiosk state. */
    private val homeAlias: ComponentName = ComponentName(activity, HOME_ALIAS)

    /** True only if the app was provisioned as device owner via ADB. */
    fun isDeviceOwner(): Boolean = dpm.isDeviceOwnerApp(activity.packageName)

    /**
     * True if the tablet has a PIN, pattern or password. Kiosk mode can't switch that off,
     * so after a restart the tablet waits at the lock screen instead of opening the app.
     */
    fun hasScreenLock(): Boolean =
        (activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isDeviceSecure

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
            applyLockdown()
            setAsPersistentHome()
            activity.startLockTask()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enter kiosk mode", e)
        }
    }

    /**
     * Stops lock task and releases the home binding. The app stays in the foreground,
     * now unlocked, so admin can keep working; since the HOME alias is disabled, pressing
     * Home leaves to the stock launcher normally.
     */
    fun exitKioskMode() {
        try {
            clearLockdown()
            clearPersistentHome()
            activity.stopLockTask()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to exit kiosk mode", e)
        }
    }

    /**
     * Closes the ways out that lock task alone leaves open. Device owner only.
     * - No power menu, so no Restart, Power off, or long-press on Power off for safe mode.
     *   By default lock task keeps that menu.
     * - No safe mode at all, which would boot with this app turned off. This also covers
     *   holding a volume key while the tablet starts.
     * - No factory reset from Settings.
     * - No swipe lock screen after a restart, so the tablet goes straight into the app.
     *   A PIN can't be removed this way; see [hasScreenLock].
     * - The screen stays on while the tablet is charging.
     */
    private fun applyLockdown() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            dpm.setLockTaskFeatures(adminComponent, DevicePolicyManager.LOCK_TASK_FEATURE_NONE)
        }
        LOCKDOWN_RESTRICTIONS.forEach { dpm.addUserRestriction(adminComponent, it) }
        dpm.setKeyguardDisabled(adminComponent, true)
        dpm.setGlobalSetting(adminComponent, Settings.Global.STAY_ON_WHILE_PLUGGED_IN, STAY_ON_ANY_CHARGER)
    }

    /** Undoes [applyLockdown], back to Android's defaults. */
    private fun clearLockdown() {
        if (!isDeviceOwner()) return
        LOCKDOWN_RESTRICTIONS.forEach { dpm.clearUserRestriction(adminComponent, it) }
        dpm.setKeyguardDisabled(adminComponent, false)
        dpm.setGlobalSetting(adminComponent, Settings.Global.STAY_ON_WHILE_PLUGGED_IN, STAY_ON_NEVER)
    }

    /**
     * Enables the HOME alias and pins it as the device's preferred home so the app
     * relaunches on every boot (battery die, manual shutdown). Without this, lock task
     * is lost on reboot and the tablet boots to the stock launcher instead of the kiosk.
     * Device owner only.
     */
    private fun setAsPersistentHome() {
        setHomeAliasEnabled(true)
        val filter = IntentFilter(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        dpm.addPersistentPreferredActivity(adminComponent, filter, homeAlias)
    }

    /**
     * Releases the HOME binding and disables the HOME alias, so the app stops being a
     * home candidate entirely and the tablet behaves like normal — pressing Home goes
     * straight to the stock launcher.
     */
    private fun clearPersistentHome() {
        if (!isDeviceOwner()) return
        dpm.clearPackagePersistentPreferredActivities(adminComponent, activity.packageName)
        setHomeAliasEnabled(false)
    }

    /** Flips the manifest-disabled HOME alias on/off without killing the running app. */
    private fun setHomeAliasEnabled(enabled: Boolean) {
        val state = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        activity.packageManager.setComponentEnabledSetting(
            homeAlias,
            state,
            PackageManager.DONT_KILL_APP
        )
    }

    /**
     * NUCLEAR: relinquishes device-owner status. After this, ADB re-provisioning is
     * required to re-enable kiosk mode.
     */
    fun clearDeviceOwner() {
        try {
            dpm.clearDeviceOwnerApp(activity.packageName)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear device owner", e)
        }
    }

    private companion object {
        const val TAG = "KioskManager"
        const val HOME_ALIAS = "com.touristapp.MainActivityHome"

        val LOCKDOWN_RESTRICTIONS = listOf(
            UserManager.DISALLOW_SAFE_BOOT,
            UserManager.DISALLOW_FACTORY_RESET
        )

        val STAY_ON_ANY_CHARGER = (
            BatteryManager.BATTERY_PLUGGED_AC or
                BatteryManager.BATTERY_PLUGGED_USB or
                BatteryManager.BATTERY_PLUGGED_WIRELESS
            ).toString()
        const val STAY_ON_NEVER = "0"
    }
}
