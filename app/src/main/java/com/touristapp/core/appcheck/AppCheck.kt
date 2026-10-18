package com.touristapp.core.appcheck

import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck

/**
 * Attaches App Check to this [FirebaseApp], so its Firestore and Auth requests carry a
 * token proving they come from this APK and not from a script holding the API key.
 *
 * App Check is per app instance: the default app and the owner's `@AdminScope` app each
 * need this call before their first Firestore request. The provider comes from the build
 * type — Play Integrity in release, the debug provider in debug (see
 * [appCheckProviderFactory]).
 */
fun FirebaseApp.installAppCheck() {
    FirebaseAppCheck.getInstance(this).installAppCheckProviderFactory(appCheckProviderFactory)
}
