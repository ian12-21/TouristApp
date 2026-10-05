# Tourist App — Tablet Kiosk

Guest-facing tablet app for apartment tourist rentals. It shows one apartment's info
(Wi-Fi, rooms and appliances, house rules, checkout), nearby places, transport, emergency
contacts, weather and guest reviews — all pulled from Firebase Firestore.

The content is entered by the apartment owner in the companion web app, `tourist-admin`.
The document shapes both apps agree on are in [SCHEMA.md](SCHEMA.md).

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
2. Add the Firebase config for the flavor you want to build — see
   [Build flavors](#build-flavors). The build fails without it.
3. Optional: add `WEATHER_API_KEY=<OpenWeatherMap key>` to `local.properties`. Without it
   the app builds and runs, but shows no weather.
4. Pick a build variant (**Build → Select Build Variant**) and run on a tablet or emulator
   (API 26+).

On first launch the app shows a setup screen: the owner signs in with the same email and
password as in the web admin and picks which apartment this tablet displays. The tablet
stores the owner id and apartment id locally; the owner's session is not kept.

## Build flavors

One flavor per Firebase project, so switching environments is picking a build variant —
no file swapping.

| Flavor | Firebase project | `applicationId` | Config file |
|---|---|---|---|
| `dev` | `tourist-app-ifbb26` | `com.touristapp` | `app/src/dev/google-services.json` |
| `staging` | `tourist-app-staging` | `com.touristapp.staging` | `app/src/staging/google-services.json` |

The two have different application ids, so both can be installed on one tablet.

`google-services.json` is git-ignored for every flavor, so a fresh clone has none. To get one:

1. Firebase Console → open the project from the table → **Project settings → General →
   Your apps**.
2. Select the Android app whose package name matches the flavor's `applicationId` (register
   it first if it is not there — the package name must match exactly).
3. Download `google-services.json` and put it at the path from the table.

The variants are `devDebug`, `stagingDebug`, `devRelease` and `stagingRelease`. The Gradle
wrapper script is not committed, so build from Android Studio, or with a local Gradle
install: `gradle assembleStagingDebug` writes
`app/build/outputs/apk/staging/debug/app-staging-debug.apk`. Release builds have no signing
config yet (#8), so only debug APKs are installable today.

> The `dev` project still holds its data in the old top-level layout, while this branch
> reads `owners/{ownerId}/…` — a `dev` build pairs but shows an empty apartment list until
> #13 is done. Use `staging` for anything real.

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

Flavor-specific files (only `google-services.json`) live in `app/src/dev/` and
`app/src/staging/`. UI strings are in `app/src/main/res/values*/strings.xml`
(English, Croatian, Italian, German).

## Hidden Admin Access

Press and hold the **home icon in the top-left corner for 5 seconds**, then release, to
open the admin dialog. After signing in, the owner can **Reconfigure apartment** (pick a
different one) or turn kiosk mode on or off. Three failed sign-ins close the dialog and
silently ignore the gesture for 60 seconds.

The owner signs in on a separate Firebase session, so opening the dialog never disturbs the
tablet's anonymous identity — that identity is what lets the tablet edit the reviews it wrote.

## Kiosk Mode Setup (one-time per tablet)

The app uses Android **Lock Task Mode** as **device owner** to lock guests into the app (no
home, recents, or notification shade). This requires a one-time ADB provisioning step:

1. Factory reset the tablet and **skip Google account sign-in** during setup (device owner
   can only be set when no account exists).
2. Enable **Developer Options → USB Debugging**.
3. Install the app: `adb install <the .apk>`
4. Provision the app as device owner:
   - staging build: `adb shell dpm set-device-owner com.touristapp.staging/com.touristapp.admin.KioskAdminReceiver`
   - dev build: `adb shell dpm set-device-owner com.touristapp/.admin.KioskAdminReceiver`

   The short `.admin…` form resolves against the applicationId, which differs from the code
   package in staging — so the staging command needs the fully qualified receiver.
5. Launch the app and complete apartment setup.
6. Open the admin dialog (hold the home icon 5 seconds → sign in) → **Enable kiosk mode**.

Kiosk mode is **off by default** — provisioning device owner only makes it *possible*. It
stays off until the owner enables it from the admin dialog, then stays on across reboots
(the app becomes the home screen and re-engages on every boot) until the owner chooses
**Exit kiosk mode** in the same dialog. After exiting, the tablet behaves normally again.

Exiting kiosk mode leaves the app provisioned as device owner. There is no in-app way to
give that up; a factory reset clears it.

On a device that is **not** provisioned as device owner (e.g. a normal dev build), the
kiosk calls are safely no-ops and "Enable kiosk mode" does not lock anything.
