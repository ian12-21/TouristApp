# Tourist App — Tablet Kiosk

Guest-facing tablet app for apartment tourist rentals. Displays apartment info, nearby places, and a map — all pulled from Firebase Firestore in real time.

## Tech Stack

- **Kotlin** + **Jetpack Compose** + **Material 3**
- **Firebase Firestore** (read-only)
- **Firebase Auth** (used only during owner setup)

## Setup

1. Open the project in **Android Studio** (Ladybug or newer)
2. Sync Gradle
3. Run on a tablet/emulator (API 26+)

On first launch the app shows a setup screen where the apartment owner logs in with their Firebase credentials, picks which apartment this tablet displays, and the app enters kiosk mode. Auth is discarded immediately after setup.

## Project Structure

```
app/src/main/java/com/touristapp/
├── data/
│   ├── local/AppPreferences.kt       — SharedPreferences wrapper
│   ├── model/Models.kt               — Firestore data classes
│   └── repository/TouristRepository.kt — Firestore read operations
├── ui/
│   ├── components/
│   │   ├── AdminLoginFlow.kt          — Reusable login + apartment picker
│   │   └── AdminDialog.kt             — Dialog wrapper for reconfiguration
│   ├── navigation/AppNavigation.kt    — Bottom nav + routing
│   ├── screens/
│   │   ├── setup/SetupScreen.kt       — First-launch setup
│   │   ├── home/HomeScreen.kt         — Guest home (+ hidden admin trigger)
│   │   ├── places/PlacesScreen.kt     — Nearby places (TODO)
│   │   └── map/MapScreen.kt           — Map view (TODO)
│   └── theme/Theme.kt                — Material 3 colors
└── MainActivity.kt                    — Entry point, setup vs kiosk decision
```

## Hidden Admin Access

Long-press the top of the home screen for 10 seconds to trigger the admin dialog. After logging in, an action menu lets the owner reconfigure the apartment, toggle kiosk mode on/off, or fully remove kiosk mode. 3 failed login attempts triggers a 60-second silent cooldown.

## Kiosk Mode Setup (one-time per tablet)

The app uses Android **Lock Task Mode** as **device owner** to lock guests into the app (no home, recents, or notification shade). This requires a one-time ADB provisioning step:

1. Factory reset the tablet and **skip Google account sign-in** during setup (device owner can only be set when no account exists).
2. Enable **Developer Options → USB Debugging**.
3. Install the app: `adb install app-release.apk`
4. Provision the app as device owner:
   `adb shell dpm set-device-owner com.touristapp/.admin.KioskAdminReceiver`
5. Launch the app and complete apartment setup.
6. Open the admin dialog (10s long-press the home icon → log in) → **Enable kiosk mode**.

Kiosk mode is **off by default** — provisioning device owner only makes it *possible*. It stays off until the owner explicitly enables it from the admin dialog, then stays on across reboots (and re-engages on every boot) until the owner turns it off again. To toggle or remove kiosk, open the admin dialog → "Exit kiosk mode" / "Enable kiosk mode", or "Remove kiosk completely" (the nuclear option — requires ADB re-provisioning to restore). A factory reset also clears device-owner status.

On a device that is **not** provisioned as device owner (e.g. a normal dev build), the app runs exactly as before — the kiosk calls are safely no-ops.
