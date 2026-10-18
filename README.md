# Tourist App — Tablet Kiosk

Guest-facing tablet app for apartment tourist rentals. It shows one apartment's info
(Wi-Fi, rooms and appliances, house rules, checkout), nearby places, transport, emergency
contacts, weather and guest reviews — all pulled from Firebase Firestore.

The content is entered by the apartment owner in the companion web app, `tourist-admin`.
The document shapes both apps agree on are in [SCHEMA.md](SCHEMA.md), which must stay
identical in both repos — a CI check in `tourist-admin` (`schema-sync.yml`) fails when
they differ.

**Handing a tablet to a tester?** Give them the [tester guide](docs/tester-guide.md)
([hrvatski](docs/tester-guide.hr.md)). Before sending it, check that what it promises is
true: the admin is deployed at the URL in section 1 (#1), the tester's account exists (#2),
there is an installable staging APK to link (#8), and at least one emergency contact group
exists in staging (#4).

## Tech Stack

- **Kotlin** + **Jetpack Compose** + **Material 3**, **Hilt** for DI
- **Firebase Firestore** — reads everything under `owners/{ownerId}/…`; the only thing the
  tablet writes is guest **reviews**
- **Firebase Auth** — the tablet runs as an anonymous user; the owner's email/password
  sign-in is used only while pairing and is signed out right after
- **Ktor** + kotlinx.serialization for the weather lookup (OpenWeatherMap), **Coil** for images

## Setup

1. Open the project in **Android Studio** (Ladybug or newer) and sync Gradle.
2. Add the Firebase config — see [Build flavors](#build-flavors). The build fails
   without it.
3. Optional: add `WEATHER_API_KEY=<OpenWeatherMap key>` to `local.properties`. Without it
   the app builds and runs, but shows no weather.
4. Run the `stagingDebug` variant on a tablet or emulator (API 26+).

On first launch the app shows a setup screen: the owner signs in with the same email and
password as in the web admin and picks which apartment this tablet displays. The tablet
stores the owner id and apartment id locally; the owner's session is not kept.

## Build flavors

One flavor per Firebase project. There is a single project, so a single flavor:

| Flavor | Firebase project | `applicationId` | Config file |
|---|---|---|---|
| `staging` | `tourist-app-staging` | `com.touristapp.staging` | `app/src/staging/google-services.json` |

`google-services.json` is git-ignored, so a fresh clone has none. To get one:

1. Firebase Console → open the project from the table → **Project settings → General →
   Your apps**.
2. Select the Android app whose package name matches the `applicationId` above (register
   it first if it is not there — the package name must match exactly).
3. Download `google-services.json` and put it at the path from the table.

The variants are `stagingDebug` and `stagingRelease`. Build from
Android Studio, or from the command line with the Gradle wrapper:
`./gradlew assembleStagingDebug` writes
`app/build/outputs/apk/staging/debug/app-staging-debug.apk`, and
`./gradlew installStagingDebug` installs it on the connected device or emulator. The
wrapper runs Gradle 8.11.1, which needs `JAVA_HOME` pointing at JDK 17 or 21 — it does not
start on newer JDKs.

### Release builds

`./gradlew assembleStagingRelease` writes
`app/build/outputs/apk/staging/release/app-staging-release.apk`, signed with the release key
when `local.properties` names it, and unsigned (not installable) otherwise. That APK is the
one owners download, and the one QR-code kiosk setup installs.

The version comes from the number of commits on the checked-out branch: `versionCode` is
that count and `versionName` is `1.0.<count>-staging`, shown at the bottom of the owner
dialog. Commit before building, and build owners' APKs from `staging`, so each one has a
higher `versionCode` than the last and installs as an update.

1. Create the key once:
   `keytool -genkeypair -v -keystore ~/keystores/tourist-app-release.jks -alias tourist-app -keyalg RSA -keysize 4096 -validity 10000`
2. Add it to `local.properties`:

   ```
   RELEASE_STORE_FILE=/home/<you>/keystores/tourist-app-release.jks
   RELEASE_STORE_PASSWORD=…
   RELEASE_KEY_ALIAS=tourist-app
   RELEASE_KEY_PASSWORD=…
   ```

3. **Back the keystore and its passwords up somewhere safe.** Android only installs an
   update signed with the same key, and a device-owner app can't be uninstalled: without
   the key, every kiosk tablet needs a factory reset to take a new version.
4. Upload each new APK in the Firebase console (Storage) as `tablet/tourist-app.apk`,
   replacing the old one. The admin's Download button and the kiosk-setup QR code both
   point there, and `storage.rules` in `tourist-admin` makes only that file public.

The QR code also carries the key's fingerprint, `appSettings.tabletSigningChecksum` in
`tourist-admin`. It only changes with the key. To compute it:

```
apksigner verify --print-certs app/build/outputs/apk/staging/release/app-staging-release.apk \
  | grep -m1 'SHA-256 digest' | awk '{print $NF}' | xxd -r -p | base64 | tr '+/' '-_' | tr -d '='
```

(`apksigner` is in `$ANDROID_HOME/build-tools/<version>/`.)

### App Check

Both Firebase sessions (guest and owner) attach an App Check token to their requests
(`core/appcheck`). Release builds attest with **Play Integrity**; the app is registered in
App Check with the release key's SHA-256. Debug builds use the **debug provider**, which
only `src/debug` depends on, so it never reaches a release APK.

A debug build logs a debug secret per Firebase app on first start — two in all, one for
the default app and one for `admin`:

```
adb logcat | grep -i "debug secret"
```

Add both under Firebase Console → App Check → Apps → the Android app → **Manage debug
tokens**. Until enforcement is on, unregistered builds still work and only show up as
unverified requests in the App Check metrics. A secret changes when the app's data is
cleared or it is reinstalled.

## Project Structure

```
app/src/main/java/com/touristapp/
├── MainActivity.kt          — entry point: setup screen vs. guest UI, kiosk on/off
├── TouristApp.kt            — @HiltAndroidApp Application class
├── admin/                   — KioskAdminReceiver (device-admin receiver)
├── kiosk/KioskManager.kt    — Lock Task mode wrapper
├── core/
│   ├── di/                  — Hilt modules; AdminScope = the owner's separate Firebase session
│   ├── i18n/                — localized-field resolution, per-app language
│   ├── ui/                  — theme and shared composables (dialogs, doodle canvas, …)
│   └── util/                — Resource<T>, serializers, doodle encoding
├── data/
│   ├── local/AppPreferences.kt  — SharedPreferences: owner id, apartment id, language, kiosk flag
│   ├── model/               — Firestore and weather models
│   └── repository/          — TouristRepositoryImpl, AdminRepositoryImpl, WeatherRepositoryImpl
├── domain/repository/       — repository interfaces
└── feature/
    ├── setup/               — first-launch pairing screen
    ├── admin/               — owner login + apartment picker + kiosk menu (hidden dialog)
    ├── main/                — AppNavigation (pager + overlays), MainViewModel, language picker
    ├── home/                — home slide
    ├── apartment/           — overview, rooms, house rules, checkout, transport
    ├── places/              — explore, category listing, place detail
    └── reviews/             — review list and the create/edit sheet
```

Flavor-specific files (only `google-services.json`) live in `app/src/staging/`. UI strings are in `app/src/main/res/values*/strings.xml`
(English, Croatian, Italian, German).

## Hidden Admin Access

Press and hold the **home icon in the top-left corner for 5 seconds**, then release, to
open the admin dialog. After signing in, the owner can **Reconfigure apartment** (pick a
different one) or turn kiosk mode on or off. Three failed sign-ins close the dialog and
silently ignore the gesture for 60 seconds.

Any owner account can sign in here, not only the one the tablet is paired to. Picking an
apartment — the same one again, or one of another owner's — pairs the tablet with it and
reloads everything from scratch, so nothing of the previous apartment stays on screen.
Closing the dialog with **✕** changes nothing: the tablet keeps its pairing and data, and
the owner is signed out.

The owner signs in on a separate Firebase session, so opening the dialog never disturbs the
tablet's anonymous identity — that identity is what lets the tablet edit the reviews it wrote.

## Kiosk Mode Setup (one-time per tablet)

The app uses Android **Lock Task Mode** as **device owner** to lock guests into the app (no
home, recents, or notification shade). Android only makes an app device owner while a
tablet is being set up from a factory reset, so this erases the tablet.

**With a QR code (no computer).** The owner follows the **Tablet setup** page of the web
admin:

1. Factory reset the tablet.
2. On the first welcome screen, tap the same spot 6 times. A QR code scanner opens (some
   tablets ask for Wi-Fi first).
3. Scan the code on the Tablet setup page and connect to Wi-Fi. Android downloads the APK
   from the code's link, checks its signing key against the code's checksum, installs it
   and makes it device owner. On Android 12+ the setup wizard also asks the app for its
   provisioning mode; `admin/ProvisioningActivity` answers that.
4. Finish the setup **without a Google account and without a screen lock**.
5. Open the app and complete apartment setup.
6. Open the admin dialog (hold the home icon 5 seconds → sign in) → **Enable kiosk mode**.

The QR code is built from `appSettings` in `tourist-admin` (the APK link, the receiver
below and the signing checksum, see [Release builds](#release-builds)). It shows only
once the checksum is filled in.

**With ADB**, for development or a tablet whose setup wizard has no QR scanner:

1. Factory reset the tablet and **skip Google account sign-in** during setup (device owner
   can only be set when no account exists). Don't set a screen lock either.
2. Enable **Developer Options → USB Debugging**.
3. Install the app: `adb install <the .apk>`
4. Provision the app as device owner:
   `adb shell dpm set-device-owner com.touristapp.staging/com.touristapp.admin.KioskAdminReceiver`

   The short `.admin…` form would resolve against the applicationId, which differs from the
   code package — so the receiver has to be fully qualified.
5. Launch the app and complete apartment setup.
6. Open the admin dialog (hold the home icon 5 seconds → sign in) → **Enable kiosk mode**.

Kiosk mode is **off by default** — provisioning device owner only makes it *possible*. It
stays off until the owner enables it from the admin dialog, then stays on across reboots
(the app becomes the home screen and re-engages on every boot) until the owner chooses
**Exit kiosk mode** in the same dialog. After exiting, the tablet behaves normally again.

While kiosk mode is on, the app also:
- hides the power menu, so a guest can't choose Restart or Power off, or long-press
  Power off to reach safe mode;
- blocks safe mode completely, which would start the tablet with the app turned off;
- blocks factory reset from Settings;
- removes the swipe lock screen, so the tablet goes straight into the app after a restart;
- keeps the screen on while the tablet is charging.

Exiting kiosk mode undoes all of this. Two things are outside the app's control:
- **A PIN, pattern or password** can't be removed by the app. A tablet that has one waits
  at the lock screen after every restart, and the owner menu warns about it. Set the
  screen lock to **None** before you enable kiosk mode.
- **Recovery mode** (the hardware-button factory reset) can't be blocked. It only wipes
  the tablet; it doesn't open it.

Exiting kiosk mode leaves the app provisioned as device owner. There is no in-app way to
give that up; a factory reset clears it.

On a device that is **not** provisioned as device owner (e.g. an emulator), the
kiosk calls are safely no-ops, and the owner menu shows a note in place of "Enable kiosk mode".
