package com.touristapp.admin

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.os.Build
import android.os.Bundle

/**
 * Answers Android's setup wizard during QR-code provisioning. From Android 12 on, the wizard
 * asks the app which mode it wants and then lets it finish its own setup; an app that doesn't
 * answer both makes provisioning fail. No UI: it replies and finishes at once.
 */
class ProvisioningActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Both actions exist only from API 29, so nothing calls this on older tablets.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            intent.action == DevicePolicyManager.ACTION_GET_PROVISIONING_MODE
        ) {
            setResult(
                RESULT_OK,
                Intent().putExtra(
                    DevicePolicyManager.EXTRA_PROVISIONING_MODE,
                    DevicePolicyManager.PROVISIONING_MODE_FULLY_MANAGED_DEVICE
                )
            )
        } else {
            // ACTION_ADMIN_POLICY_COMPLIANCE: kiosk mode is set up later, from the owner menu.
            setResult(RESULT_OK)
        }
        finish()
    }
}
