package com.touristapp.core.di

import javax.inject.Qualifier

/**
 * Marks the Firebase instances backing the **owner** session on the tablet.
 *
 * The tablet runs two independent Firebase sessions at once:
 *
 * - the default one, permanently signed in anonymously, used for everything the
 *   guest sees, and
 * - this one, used only while an owner is logged into the admin dialog.
 *
 * They must stay separate. A single [com.google.firebase.auth.FirebaseAuth] holds
 * exactly one user, so signing an owner in on the default instance would evict the
 * anonymous session — and signing out afterwards orphans that anonymous account
 * permanently, since an anonymous user can never be signed back into. The tablet
 * would come back with a brand-new uid and lose the ability to edit any review it
 * had written (see `authorUid` in firestore.rules).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AdminScope
