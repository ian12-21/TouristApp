package com.touristapp.core.appcheck

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/**
 * Debug builds can't pass Play Integrity (emulators, unsigned APKs), so they use the debug
 * provider. On first start it logs a debug secret per Firebase app — look for "debug secret"
 * in logcat — and each one must be added under App Check → Apps →
 * Manage debug tokens before the build is let through once enforcement is on.
 *
 * Only this source set depends on `firebase-appcheck-debug`, so it never reaches a release
 * APK, where it would let anyone with a registered token past App Check.
 */
internal val appCheckProviderFactory: AppCheckProviderFactory =
    DebugAppCheckProviderFactory.getInstance()
