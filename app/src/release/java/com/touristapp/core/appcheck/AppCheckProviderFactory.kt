package com.touristapp.core.appcheck

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

/** Release builds attest with Play Integrity, checked against the release signing key. */
internal val appCheckProviderFactory: AppCheckProviderFactory =
    PlayIntegrityAppCheckProviderFactory.getInstance()
