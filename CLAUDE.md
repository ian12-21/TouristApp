# CLAUDE.md — Kotlin + Jetpack Compose Project Guide

## Project Overview

**Tourist App** is the guest-facing kiosk tablet app for apartment rentals, built with
**Kotlin** and **Jetpack Compose**. It reads one apartment's content from **Firebase
Firestore** (under `owners/{ownerId}/…`) and writes only guest reviews. The content is
entered in the companion Angular app, `tourist-admin`.

- `README.md` — setup, build flavors (`dev` / `staging`), kiosk mode.
- `SCHEMA.md` — the Firestore contract shared with `tourist-admin`. **Change it first, then
  both repos**, and keep the file identical in both.

Follow the conventions below for all code generation, refactoring, and reviews.

---

## Architecture

**MVVM** with the layers as top-level packages under `com.touristapp` — not one
`data/domain/presentation` triple per feature:

```
data/      # Firestore/weather models, repository implementations, AppPreferences
domain/    # Repository interfaces only
feature/   # One package per screen area: composables + its ViewModel
core/      # DI, i18n, theme, shared composables, utilities
```

- **Feature → Domain ← Data.** ViewModels depend on the repository interfaces in
  `domain/repository/`; the implementations in `data/repository/` are bound in
  `core/di/RepositoryModule.kt`.
- There is no use-case layer and no separate domain model: the classes in `data/model/`
  are used all the way up to the UI. Don't add either for a single call site.
- ViewModels expose UI state via `StateFlow`. Never expose `MutableStateFlow` publicly.
- `MainViewModel` owns the app-wide state (apartment, stay, places, language, overlay);
  feature ViewModels own what is local to their screen.

---

## Kotlin Conventions

- **Language level:** Kotlin 2.0+ idioms. Use `data class`, `sealed interface`, `value class` where appropriate.
- **Nullability:** Avoid `!!`. Use `?.let`, `?:`, or require non-null at the boundary.
- **Coroutines:** Use structured concurrency. Launch coroutines in `viewModelScope`. Never use `GlobalScope`.
- **Immutability first:** Prefer `val` over `var`, `List` over `MutableList` in public APIs.
- **Named arguments:** Use them when calling functions with 3+ parameters or when meaning isn't obvious.
- **Extension functions:** Use for utility logic. Keep them focused — don't dump unrelated extensions in one file.
- **Naming:**
    - Classes/Interfaces: `PascalCase`
    - Functions/Properties: `camelCase`
    - Constants: `SCREAMING_SNAKE_CASE`
    - Packages: `lowercase`, no underscores
- **No magic numbers/strings.** Extract to constants or resource files.

---

## Jetpack Compose Rules

### State Management
- Hoist state out of composables. Composables should receive state and emit events.
- Use `remember` and `rememberSaveable` correctly — `rememberSaveable` for anything that must survive config changes.
- Model UI state as a single `data class` per screen:
  ```kotlin
  data class ProfileUiState(
      val isLoading: Boolean = false,
      val user: User? = null,
      val error: String? = null
  )
  ```
- Use `sealed interface` for one-shot events (navigation, snackbars):
  ```kotlin
  sealed interface ProfileEvent {
      data class NavigateToEdit(val userId: String) : ProfileEvent
      data class ShowError(val message: String) : ProfileEvent
  }
  ```

### Composable Best Practices
- Keep composables **small and focused**. Extract reusable pieces early.
- Stateless composables are preferred. Pass data down, push events up.
- Use `Modifier` as the **first optional parameter** in every public composable.
- Provide sensible defaults. A composable should render something useful with zero config.
- Use `@Preview` with sample data for every public composable.
- Avoid side effects inside composable functions. Use `LaunchedEffect`, `SideEffect`, or `DisposableEffect` when needed.
- Never call ViewModel functions directly from deeply nested composables — pass lambdas down.

### Compose Naming
- Screen-level composables: `ProfileScreen`, `HomeScreen`
- Reusable components: descriptive noun — `UserCard`, `SearchBar`
- Preview functions: `PreviewProfileScreen`

### Navigation
- There is no `NavHost`. `feature/main/AppNavigation.kt` is a `HorizontalPager` of four
  slides (home, places, reviews, transport) with full-screen overlays on top.
- The open overlay is the `OverlayScreen` sealed interface in `MainViewModel`'s UI state.
  Add a screen by adding a case there and rendering it in `AppNavigation`.
- Screens navigate by calling lambdas that end in a `MainViewModel` function — never by
  holding navigation state themselves.

### Theming
- Use Material 3 (`MaterialTheme`) for colors, typography, and shapes.
- Access theme values via `MaterialTheme.colorScheme`, `MaterialTheme.typography`, etc. Never hardcode colors or text styles.
- Support dynamic color where it makes sense.
- Define custom theme properties through composition locals only when Material tokens aren't sufficient.

---

## Dependency Injection

- Use **Hilt** for DI.
- Annotate ViewModels with `@HiltViewModel` + `@Inject constructor`.
- Provide repository implementations via `@Binds` in `core/di/RepositoryModule.kt`; the Firebase instances and the Ktor client come from `core/di/AppModule.kt`.
- Use `@Singleton` for app-wide dependencies, `@ViewModelScoped` only when truly needed.

---

## Networking & Data

- **Firestore** is the backend. All reads and the review writes go through
  `TouristRepositoryImpl`; paths are built from the owner id saved in `AppPreferences`.
- **Two Firebase sessions.** The guest session is anonymous (`ensureAnonymousAuth()`); the
  owner signs in on a separate `FirebaseApp` (`@AdminScope`, `AdminRepositoryImpl`) so that
  pairing never replaces the tablet's anonymous uid — that uid is what lets it edit its reviews.
- **Localized fields** are Firestore maps (`{ en, hr, it, de }`). Model properties for them
  are `@get:Exclude` and resolved by hand in the repository — see `SCHEMA.md` §1.2.
- **Ktor** + kotlinx.serialization is used only for the weather API (`WeatherRepositoryImpl`).
- Local persistence is `AppPreferences` (SharedPreferences) plus Firestore's offline cache.
  There is no Room database and no Retrofit.
- Repositories return `Resource<T>` (`core/util/Resource.kt`) for success/failure.

---

## Error Handling

- Catch exceptions at the **repository boundary**. Domain and presentation layers should work with sealed results, not raw exceptions.
- Show user-friendly messages. Log technical details for debugging.
- Use `runCatching` sparingly — only when you want to catch all exceptions. Prefer specific catches.

---

## Testing

There are no tests in this repo yet. When adding them:

- **Unit tests:** ViewModels with JUnit + Turbine (for Flow testing), using fakes of the
  repository interfaces rather than mocks.
- **UI tests:** Compose Testing (`createComposeRule`) for screen-level tests.
- Test naming: `should [expected] when [condition]` — e.g., `should show error when login fails`.
- Don't test implementation details. Test behavior and outcomes.
- The Firestore security rules are tested in `tourist-admin` (`npm run test:rules`).

---

## Project Structure

```
app/src/main/java/com/touristapp/
├── MainActivity.kt      # Setup screen vs. guest UI, kiosk on/off
├── TouristApp.kt        # @HiltAndroidApp Application class
├── admin/               # KioskAdminReceiver
├── kiosk/               # KioskManager (Lock Task mode)
├── core/
│   ├── di/              # AppModule, RepositoryModule, AdminScope
│   ├── i18n/            # Localized-field resolution, per-app language
│   ├── ui/
│   │   ├── theme/       # Theme
│   │   └── components/  # Shared composables (dialogs, DoodleCanvas, …)
│   └── util/            # Resource, serializers, DoodleEncoder
├── data/
│   ├── local/           # AppPreferences
│   ├── model/           # Models.kt, WeatherModels.kt
│   └── repository/      # *RepositoryImpl
├── domain/
│   └── repository/      # Repository interfaces
└── feature/
    ├── admin/           # Owner login, apartment picker, kiosk menu
    ├── apartment/
    ├── home/
    ├── main/            # AppNavigation, MainViewModel
    ├── places/
    ├── reviews/
    └── setup/
```

Per-flavor Firebase config lives in `app/src/dev/` and `app/src/staging/`
(`google-services.json`, git-ignored).

---

## Code Review Checklist

When reviewing or generating code, verify:
- [ ] No business logic in composables or Activities
- [ ] State flows from ViewModel → UI, events flow from UI → ViewModel
- [ ] No hardcoded strings — use `strings.xml` for user-facing text
- [ ] Modifiers passed correctly and applied in the right order
- [ ] Coroutines use appropriate dispatchers (don't do IO on Main)
- [ ] No memory leaks — no Activity/Context references in ViewModels
- [ ] Preview annotations present on public composables
- [ ] Error states are handled and visible to the user

---

## Common Pitfalls to Avoid

- **Don't** use `mutableStateOf` in the ViewModel — use `MutableStateFlow` + `.stateIn()`.
- **Don't** create god ViewModels with 15 functions. Split by responsibility.
- **Don't** nest composables 10 levels deep. Extract and name components.
- **Don't** ignore recomposition costs. Use `key()`, `derivedStateOf`, and stable types.
- **Don't** put `suspend` functions in composables. Use `LaunchedEffect`.
- **Don't** mix UI logic with business logic. A ViewModel should never reference `Color`, `Dp`, or any Compose type.
