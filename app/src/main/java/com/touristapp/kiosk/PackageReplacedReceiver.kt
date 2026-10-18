package com.touristapp.kiosk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Reopens the app after it has been updated while kiosk mode was on. Installing the update
 * stops the app, which also ends lock task, and nothing starts it again: the tablet is left
 * unlocked on the stock launcher until someone presses Home.
 *
 * The HOME alias is enabled only while kiosk mode is on (see [KioskManager]), so resolving
 * this app's HOME activity tells whether to reopen it. MainActivity then locks the tablet
 * again. A device-owner app may start an activity from the background, which this needs.
 */
class PackageReplacedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val home = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_HOME)
            .setPackage(context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (home.resolveActivity(context.packageManager) == null) return
        context.startActivity(home)
    }
}
