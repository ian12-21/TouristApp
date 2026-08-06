# Demo readiness checklist

Goal: run the kiosk on **one tablet, in one apartment, with real guests**, as a
supervised prototype. Not multi-property, not "a product".

Verdict: the architecture and the Firestore rules are ready. What is missing is
operational polish — the things that only hurt once a stranger is holding the
device and you are not in the room.

Covers both repos:

- `TouristApp` — Kotlin / Compose tablet kiosk
- `tourist-admin` — Angular admin web app

---

## Blockers — do these before a guest touches the tablet

### 1. The release build is not a release build

**Where:** `app/build.gradle.kts`

```kotlin
buildTypes {
    release {
        isMinifyEnabled = false   // ← no shrinking, no obfuscation
        // ← no signingConfig at all
    }
}
```

With no `signingConfig`, `assembleRelease` produces an unsigned APK; anything you
actually install ends up debug-signed. A debug-signed build also cannot be
attested by App Check later (item 4), because Play Integrity keys off the release
signing certificate.

**Do:**

- Generate a keystore, keep it out of git (`.gitignore` already covers `*.jks`? — verify)
- Read the keystore path/password from `local.properties`, the same pattern already
  used for `WEATHER_API_KEY`
- Set `isMinifyEnabled = true` and check the app still runs — Firestore data classes
  need `@Keep` or ProGuard rules, since deserialization is reflective and R8 will
  happily rename the fields out from under it

**Cost:** ~1 hour, most of it verifying minify didn't break Firestore mapping.

---

### 2. Default Android app icon

**Where:** `app/src/main/AndroidManifest.xml`

```xml
android:icon="@android:drawable/sym_def_app_icon"
```

That is the generic grey Android placeholder. On a device handed to a paying
guest it reads as unfinished.

**Do:** add an adaptive icon (`mipmap-anydpi-v26/ic_launcher.xml` + foreground/
background drawables) and point the manifest at it. Also set `android:roundIcon`.

**Cost:** ~30 min. Highest visual return per minute of any item here.

---

### 3. No crash reporting

**Where:** nowhere — there is no Crashlytics dependency in `app/build.gradle.kts`.

This is the one I would do first. The entire point of a live demo is finding out
what breaks, and right now a crash at 11pm in an apartment you are not in is
completely invisible to you. You would learn about it from a bad review.

**Do:**

- Add `firebase-crashlytics` + the Gradle plugin (free tier is fine)
- Log non-fatals at the repository boundary too — every `catch (e: Exception)` in
  `TouristRepositoryImpl` currently swallows the cause into a `Resource.Error`
  string. Send those to Crashlytics as non-fatals so you can see *why* a screen
  showed an error, not just that it did.

**Cost:** ~1 hour. Pays for itself the first time something goes wrong.

---

### 4. `allowBackup="true"` on a kiosk

**Where:** `app/src/main/AndroidManifest.xml`

Android auto-backup will ship `AppPreferences` (apartment ID, kiosk flag, cached
weather) to the guest's Google account if one is ever added. On a device that is
deliberately account-less and locked into one app, this is pure downside.

**Do:** `android:allowBackup="false"` and add `android:dataExtractionRules` /
`android:fullBackupContent` as belt-and-braces.

**Cost:** 2 minutes.

---

### 5. The screen will go to sleep

**Where:** `MainActivity.kt` — no `FLAG_KEEP_SCREEN_ON`, no wake policy.

Lock Task Mode stops the guest leaving the app; it does not stop the display
timing out. A black tablet reads as a broken tablet, and the guest cannot always
tell the difference.

**Do:** in `MainActivity.onCreate`, `window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)`.
Consider gating it on `isKioskEnabled` so dev builds don't burn your laptop
emulator. Optionally lower brightness on idle rather than sleeping.

**Cost:** 15 minutes. Also check the tablet's own display-timeout setting — a
device-owner app can set it via `DevicePolicyManager` if you want it enforced.

---

### 6. App Check is not enabled

**Where:** documented as a known gap in `../tourist-admin/SECURITY.md` §4, which
already calls it "the single highest-value item here".

Your own analysis is right, and it is worth restating why: the API key is
extractable from the APK in about a minute, and `signInAnonymously()` hands a
valid identity to anyone holding it. The rules are strong, but they can only
distinguish *owner* from *anonymous* — they cannot distinguish "your tablet" from
"a script".

For one tablet in one apartment the realistic exposure is low, so this is the
softest of the six. **But** note the rollout warning you already wrote: turning
enforcement on later breaks any APK already in a guest's hands, hard — every read
fails, it does not degrade. So either do it now, or accept that the demo APK is
disposable.

**Do:** follow `SECURITY.md` §4 exactly. Depends on item 1 (real release signing).

**Cost:** ~2 hours including the metrics-watching period.

---

## Accept for the demo — revisit before apartment #2

These are real, and they are also fine to knowingly ship for a supervised
single-property prototype. Listed so the decision is explicit rather than
forgotten.

### 7. The tablet cannot prove which apartment it is

`SECURITY.md` §5.1. `AdminViewModel.selectApartment` signs out right after
pairing, so the `apartmentId` lives only in local prefs; to Firestore every
tablet is an anonymous stranger. A client that knows a stay ID can `get` it.

Fine when you own the only tablet. The fix is real device pairing
(`devices/{uid}` holding the `apartmentId`, rules scoped through it) — a
data-model change across both apps, so not a demo-week task.

### 8. Storage bucket is world-readable

`storage.rules` uses `allow read: if true` for everything. Intended, since
tourist photos are public. The trap is future you uploading something private
into the same open bucket. Scope the rule to a `public/` path prefix *before*
that happens — cheap now, awkward later.

### 9. Zero tests in either repo

54 Kotlin files, 59 TypeScript files, 0 test files. Not a demo blocker — nobody
ships tests to unblock a prototype — but the review flow is the one place where
a silent bug costs you real guest data. If you write tests for exactly one thing,
make it `MainViewModel` state transitions and the review create/update path.

### 10. GDPR

Real names, emails and phone numbers in `guests` make you a data controller under
EU/Croatian law the moment a real guest is involved. Practical minimum for a
demo:

- collect only what you actually display or need
- delete stays once they are no longer useful
- be able to answer "what do you hold about me?"

Not legal advice. Worth a proper look before this becomes a product.

### 11. Admin: `environment.ts` is committed

`src/environments/environment.ts` is tracked in git with the Firebase web config.
This is *correct* — the key is public by design, as your own SECURITY.md
explains — but only as long as every future secret goes somewhere else. Worth a
comment in the file saying so, otherwise someone (you, in six months) adds an API
secret next to it.

---

## Suggested order

| # | Item | Cost | Why this order |
|---|------|------|----------------|
| 3 | Crashlytics | 1h | Instrument before you go live, not after |
| 2 | App icon | 30m | Cheapest perception win |
| 4 | `allowBackup=false` | 2m | Trivial |
| 5 | Keep screen on | 15m | Trivial, prevents "it's broken" |
| 1 | Signing + minify | 1h | Prerequisite for App Check |
| — | **Run it live** | — | Collect real crash + usage data |
| 6 | App Check | 2h | Do once you can read the metrics |

Items 2, 4 and 5 are one sitting. Item 3 is the one that changes what you learn
from the demo.

---

## Definition of done for the demo

- [ ] Signed release APK, minify on, Firestore deserialization verified
- [ ] Real app icon
- [ ] Crashlytics reporting fatals **and** repository non-fatals
- [ ] `allowBackup="false"`
- [ ] Screen stays on in kiosk mode
- [ ] Owner allowlist doc exists at `/owners/{uid}` (SECURITY.md §2 — **before** deploying rules)
- [ ] Rules deployed: `firebase deploy --only firestore:rules,storage`
- [ ] Tablet provisioned as device owner, kiosk toggled on, survives a reboot
- [ ] Tested: airplane mode → app degrades readably, does not crash or show a blank screen
- [ ] Tested: full guest journey end to end, including submitting a review
- [ ] You have a way to get the tablet back to a working state remotely-ish
      (i.e. you know the admin long-press + exit-kiosk sequence by heart)
