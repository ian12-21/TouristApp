# CLAUDE.md — Kotlin + Jetpack Compose Project Guide

## Project Overview

This is an Android project built with **Kotlin** and **Jetpack Compose**. Follow the conventions below for all code generation, refactoring, and reviews.

---

## Architecture

Use **MVVM + Clean Architecture** with a clear layer separation:

```
feature/
├── data/          # Repositories, data sources, DTOs, mappers
├── domain/        # Use cases, domain models, repository interfaces
└── presentation/  # ViewModels, UI state, Compose screens & components
```

- **Presentation → Domain → Data.** Dependencies point inward. Domain never depends on Data or Presentation.
- Each feature is a self-contained package. Shared code lives in `core/`.
- ViewModels expose UI state via `StateFlow`. Never expose `MutableStateFlow` publicly.
- Use cases contain **single business operations** — one public `operator fun invoke()` per use case.
- Repository interfaces live in `domain/`. Implementations live in `data/`.

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
- Use Compose Navigation with type-safe routes.
- Define routes as `sealed interface` or `@Serializable` data classes.
- Keep NavHost in a single `AppNavigation` composable at the app level.
- ViewModels are scoped to navigation destinations — never share a ViewModel across unrelated screens by hacking scopes.

### Theming
- Use Material 3 (`MaterialTheme`) for colors, typography, and shapes.
- Access theme values via `MaterialTheme.colorScheme`, `MaterialTheme.typography`, etc. Never hardcode colors or text styles.
- Support dynamic color where it makes sense.
- Define custom theme properties through composition locals only when Material tokens aren't sufficient.

---

## Dependency Injection

- Use **Hilt** for DI.
- Annotate ViewModels with `@HiltViewModel` + `@Inject constructor`.
- Provide repository implementations and data sources via `@Module` / `@Provides` or `@Binds`.
- Use `@Singleton` for app-wide dependencies, `@ViewModelScoped` only when truly needed.
- Keep modules organized per feature or layer — don't put everything in one god module.

---

## Networking & Data

- Use **Retrofit** + **Kotlin Serialization** (or Moshi) for API calls.
- DTOs stay in `data/` and get mapped to domain models at the repository level. Never leak DTOs into the domain or presentation layers.
- Use `Result` or a custom `sealed interface` (e.g., `Resource<T>`) for representing success/failure from repositories.
- Room for local persistence. DAOs return `Flow<List<T>>` for observable queries.

---

## Error Handling

- Catch exceptions at the **repository boundary**. Domain and presentation layers should work with sealed results, not raw exceptions.
- Show user-friendly messages. Log technical details for debugging.
- Use `runCatching` sparingly — only when you want to catch all exceptions. Prefer specific catches.

---

## Testing

- **Unit tests:** Test ViewModels and use cases with JUnit 5 + Turbine (for Flow testing) + MockK.
- **UI tests:** Use Compose Testing (`createComposeRule`) for screen-level tests.
- Test naming: `should [expected] when [condition]` — e.g., `should show error when login fails`.
- Don't test implementation details. Test behavior and outcomes.
- Fakes over mocks when practical — especially for repositories in ViewModel tests.

---

## Project Structure

```
app/src/main/java/com/example/app/
├── core/
│   ├── di/              # App-level Hilt modules
│   ├── network/         # API client setup, interceptors
│   ├── database/        # Room database, type converters
│   ├── ui/
│   │   ├── theme/       # Color, Type, Theme, Shape
│   │   └── components/  # Shared composables (buttons, cards, etc.)
│   └── util/            # Extensions, constants, helpers
├── feature/
│   ├── auth/
│   │   ├── data/
│   │   ├── domain/
│   │   └── presentation/
│   ├── home/
│   │   ├── data/
│   │   ├── domain/
│   │   └── presentation/
│   └── ...
└── App.kt              # Application class
```

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

- **Don't** pass `NavController` into ViewModels. Handle navigation via events.
- **Don't** use `mutableStateOf` in the ViewModel — use `MutableStateFlow` + `.stateIn()`.
- **Don't** create god ViewModels with 15 functions. Split by responsibility.
- **Don't** nest composables 10 levels deep. Extract and name components.
- **Don't** ignore recomposition costs. Use `key()`, `derivedStateOf`, and stable types.
- **Don't** put `suspend` functions in composables. Use `LaunchedEffect`.
- **Don't** mix UI logic with business logic. A ViewModel should never reference `Color`, `Dp`, or any Compose type.
