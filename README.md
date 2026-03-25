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

Long-press the top of the home screen for 10 seconds to trigger the admin reconfiguration dialog. 3 failed login attempts triggers a 60-second silent cooldown.
