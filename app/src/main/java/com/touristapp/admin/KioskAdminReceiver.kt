package com.touristapp.admin

import android.app.admin.DeviceAdminReceiver

/**
 * Empty [DeviceAdminReceiver] subclass. Required only so the app can be provisioned as
 * device owner via `adb shell dpm set-device-owner com.touristapp/.admin.KioskAdminReceiver`.
 * No runtime logic lives here.
 */
class KioskAdminReceiver : DeviceAdminReceiver()
