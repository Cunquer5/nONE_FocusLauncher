# AGENTS.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

**nONE FocusLauncher** is a minimalist Android system launcher built with Kotlin and Jetpack Compose. It uses a productivity-focused design with features like grayscale focus mode, intention prompts, and a "1 Thing" task manager. The app targets Android API 28+ and uses modern Android architecture (MVVM).

**Note**: As of v1.4.1, the launcher uses a single Modern UI design with a **Universal Search Bar** (Apps, Contacts, Files) using a swipe-down-to-search pattern. v1.4.2 adds first-launch welcome and optional feedback prompts (local timing only). v1.4.3 adds wallpaper-under-status-bar, bottom-anchored draggable sheets with 32dp top rounds, and glass dial seats.

## Build & Development Commands

### Building the Project
```powershell
# Build debug APK
.\gradlew assembleDebug

# Build release APK
.\gradlew assembleRelease

# Install debug build to connected device/emulator
.\gradlew installDebug
```

### Running Tests
```powershell
# Run all unit tests
.\gradlew test

# Run all instrumented tests (requires connected device/emulator)
.\gradlew connectedAndroidTest

# Run specific test class
.\gradlew test --tests HomeViewModelTest
```

### Code Quality
```powershell
# Clean build artifacts
.\gradlew clean

# Check for dependency updates
.\gradlew dependencyUpdates

# Lint check
.\gradlew lint
```

### Android Studio
- Open project in **Android Studio Iguana or later**
- Sync Gradle projects before first build
- Use **API 28+** for emulator/device testing
- Set **nONE Launcher** as Default Home App when prompted on first launch

## Architecture Overview

### MVVM Pattern with Repository Layer
The app follows a strict MVVM architecture:
- **UI Layer**: Jetpack Compose screens and components
- **ViewModel Layer**: AndroidViewModel with StateFlow for reactive state
- **Repository Layer**: Data access abstraction
- **Data Layer**: Room database + SharedPreferences for persistence

### Single UI System (v1.4.1+)
As of v1.4.1, the launcher uses a **single Modern UI** design with a **Universal Search Bar**.

**MODERN Mode** (`ui/modern/ModernHomeScreen.kt`):
   - Liquid glass over the device's own wallpaper, which is drawn inside the composition
     (`ui/theme/WallpaperBackground.kt`) so Haze has a real source to blur
   - Orange (#FF4D00) brand accent for active states
   - Integrated dock navigation (Apps, 1 Thing, Settings)
   - Widget support with AppWidgetHost
   - Swipe-down-to-search app discovery pattern (replaces traditional app grid)
   - Advanced features: intention history, wallpaper selection, mode selector (Normal/Focus/Blur)

**Glass**: `ui/theme/LiquidGlass.kt` is the only glass material — `LiquidGlassStyle` (Thin / Regular /
Heavy / Control / Seat) applied with `Modifier.liquidGlass(hazeState, shape, style)`. The shared
`HazeState` is created in `LauncherScreen`, attached to the wallpaper and provided as
`LocalHazeState`; never create a second one. Modal overlays (intention, wallpaper, welcome, feedback)
are composed in the home window, not as `Dialog` windows, so their glass can sample that source.
Surfaces resting *on* glass pass `sampling = false` (tint, sheen and rim without a second backdrop
pass). Dial seats sample the wallpaper like every other disc: their tint is thinner than a sheet's
because a 68–80dp disc must show the ground, with a 22dp blur keeping the glyphs legible.

### Data Flow Architecture

**State Management**:
- `HomeViewModel` aggregates multiple data sources into a single `HomeUiState` using `combine()` operator
- All user preferences flow through `UserPreferencesRepository` backed by SharedPreferences with StateFlow wrappers
- App list is cached in `AppRepository` to avoid repeated package manager queries
- Intention logging persists to Room database via `IntentionRepository`

**Key Data Sources**:
- `UserPreferencesRepository`: Focus mode, blur mode, grid columns, focus app packages, app order, one thing task, widget IDs, first-launch welcome and feedback prompt flags
- `AppRepository`: Installed apps with cached icon bitmaps (adaptive icons scaled to fill circles)
- `IntentionRepository`: User intention logs stored in Room database
- `UiModeManager`: UI mode persistence via DataStore

### Dependency Injection
This project uses **manual dependency injection**:
- ViewModels instantiate repositories directly in constructors
- Application context is passed to repositories via AndroidViewModel
- No Hilt, Dagger, or other DI frameworks are used

### Package Structure
```
com.example.nonelauncher/
├── data/
│   ├── local/           # Room database, DAOs, entities
│   ├── model/           # Data models (AppModel, VersionInfo)
│   └── repository/      # Repository pattern implementations
├── ui/
│   ├── components/      # Reusable UI components (AppIcon, IntentionDialog, FeedbackPromptDialog, WelcomeFirstLaunchDialog, GlassCard)
│   ├── home/            # HomeViewModel (Legacy mode placeholder - not used)
│   ├── modern/          # Modern UI mode screen + glass morphism components
│   ├── theme/           # Material 3 theming + glass morphism utilities
│   ├── LauncherScreen.kt  # Root launcher router (single Modern UI)
│   └── MainViewModel.kt   # Top-level UI state
└── MainActivity.kt      # Entry point with AppWidgetHost setup
```

**Note**: Legacy UI mode was removed in v1.4.0. The launcher now uses a single Modern UI with swipe-down-to-search pattern.

### Critical Implementation Details

**App Icon Rendering**:
- Icons are pre-processed in `AppRepository.drawableToBitmap()` with 1.3x scale for adaptive icons
- This moves bitmap conversion off UI thread to improve scroll performance
- Icons are cached in memory after first load

**Widget Support**:
- `AppWidgetHost` is initialized in `MainActivity` with ID `1024`
- Widget lifecycle managed manually: `startListening()` in onCreate, `stopListening()` in onDestroy
- Widget IDs stored in `UserPreferencesRepository` as comma-separated string

**Focus Mode**:
- Apps are categorized as "essential" (banking, contacts, messaging) vs. distracting apps
- Focus apps marked in `focusAppPackages` set show normally; others are grayscaled
- Essential app detection uses package name patterns and label keywords

**Default Launcher Detection**:
- `LauncherScreen.kt` checks if app is default launcher on every resume
- Shows setup prompt if not default, with RoleManager API (Android Q+) or fallback Settings intent
- `AndroidManifest.xml` declares `CATEGORY_HOME` intent filter

## Development Notes

### Theme & Styling
- **Brand Color**: Orange `#FF4D00` (`OrangePrimary`) used for active states, wallpaper, checkmarks
- **Liquid Glass**: One material in `ui/theme/LiquidGlass.kt` — Haze blur of the wallpaper, `#0A0A0B`
  tint, radial sheen, 1dp specular rim, optional shadow. No other glass utilities.
- **Material 3**: Custom theme in `ui/theme/Theme.kt` with edge-to-edge support
- **Wallpaper**: Drawn by `ui/theme/WallpaperBackground.kt` inside the composition and used as the Haze
  source. Reading the static wallpaper needs photo access (`READ_MEDIA_IMAGES` on 13/14, all-files on
  newer), asked once from the welcome dialog and passed to `WallpaperBackground` as `refreshKey` on grant;
  refreshes on `ACTION_WALLPAPER_CHANGED` (registered `RECEIVER_EXPORTED`, since the system sends it) and
  on resume. Until the bitmap is readable the window stays transparent with `FLAG_SHOW_WALLPAPER`, so the
  system's own wallpaper shows behind the tree — no painted fallback ground.

### Testing
- **Unit Tests**: JUnit 4, Mockito Kotlin, Coroutines Test
- **Instrumented Tests**: AndroidX Test, Espresso, Compose UI Test
- **Note**: No existing test files in codebase currently

### Permissions
Required permissions in AndroidManifest.xml:
- `QUERY_ALL_PACKAGES`: List installed applications
- `SET_WALLPAPER`: Change wallpaper from launcher settings
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`: Background widget updates
- `REQUEST_DELETE_PACKAGES`: App uninstall support

### Version Info
- **Current Version**: 1.4.3 (versionCode 11)
- **Target SDK**: 35 (Android 15)
- **Min SDK**: 28 (Android 9)
- **Java Version**: 17
- **Kotlin Version**: 2.2.10

## Common Workflows

### Onboarding & Feedback Prompts (v1.4.2+)
- **Welcome**: `WelcomeFirstLaunchDialog` in `ui/components/`; shown once via `first_launch_welcome_shown` in `UserPreferencesRepository`.
- **Feedback**: `FeedbackPromptDialog`; timing in `UserPreferencesRepository.onAppResume()` (5 launches / 7 days, 30-day snooze, permanent opt-out). URL: `FEEDBACK_WEBSITE_URL`.
- Wired in `LauncherScreen.kt`; welcome takes priority over feedback on the same resume.
- Set `FEEDBACK_TEST_TRIGGER_ON_ONE_THING = false` before release (when `true`, feedback shows on 1 Thing dock tap for QA).

### Adding a New Feature to Modern UI
1. Check if feature requires new preference → add to `UserPreferencesRepository`
2. Update `HomeViewModel.uiState` to include new state in `HomeUiState` data class
3. Add UI component to `ui/modern/components/` using `LocalHazeState` / `liquidGlass` (or `LiquidGlassPanel`); pass `sampling = false` for anything resting on existing glass
4. Wire up component in `ModernHomeScreen.kt` with appropriate state observers

### Modifying App List Behavior
1. Update `AppRepository.getInstalledApps()` for icon/metadata changes
2. Modify `HomeViewModel` state transformations for filtering/sorting logic
3. Update `AppModel` if new app-level properties needed

### Adding a New UI Mode
1. Add enum value to `LauncherUiMode.kt`
2. Create new screen composable in `ui/` package
3. Update `LauncherScreen.kt` Crossfade when-expression to handle new mode
4. Add UI mode switcher in settings sheet

### Database Schema Changes
1. Update entity classes in `data/local/entity/`
2. Increment `@Database` version number in `AppDatabase.kt`
3. Add Room migration or use `fallbackToDestructiveMigration()` for development
4. Update DAO queries if needed
