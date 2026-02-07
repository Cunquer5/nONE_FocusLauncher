# AGENTS.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

**nONE FocusLauncher** is a minimalist Android system launcher built with Kotlin and Jetpack Compose. It uses a productivity-focused design with features like grayscale focus mode, intention prompts, and a "1 Thing" task manager. The app targets Android API 28+ and uses modern Android architecture (MVVM).

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

### Dual UI System
The launcher supports **two distinct UI modes** that can be switched at runtime:

1. **LEGACY Mode** (`ui/home/HomeScreen.kt`):
   - Original simplified launcher interface
   - Basic app grid with focus mode toggles
   
2. **MODERN Mode** (`ui/modern/ModernHomeScreen.kt`):
   - Black glass morphism design with backdrop blur
   - Orange (#FF4D00) brand accent for active states
   - Integrated dock navigation (Apps, 1 Thing, Settings)
   - Widget support with AppWidgetHost
   - Advanced features: intention history, wallpaper selection, mode selector (Normal/Focus/Blur)

**UI Mode Switching**: Managed by `UiModeManager` using DataStore. The `LauncherScreen.kt` acts as a router that uses `Crossfade` animation to switch between modes based on `MainViewModel.uiMode` state.

### Data Flow Architecture

**State Management**:
- `HomeViewModel` aggregates multiple data sources into a single `HomeUiState` using `combine()` operator
- All user preferences flow through `UserPreferencesRepository` backed by SharedPreferences with StateFlow wrappers
- App list is cached in `AppRepository` to avoid repeated package manager queries
- Intention logging persists to Room database via `IntentionRepository`

**Key Data Sources**:
- `UserPreferencesRepository`: Focus mode, blur mode, grid columns, focus app packages, app order, one thing task, widget IDs
- `AppRepository`: Installed apps with cached icon bitmaps (adaptive icons scaled to fill circles)
- `IntentionRepository`: User intention logs stored in Room database
- `UiModeManager`: UI mode persistence via DataStore

### Dependency Injection
**Important**: This project does **NOT** use Hilt/Dagger despite dependencies being declared in `build.gradle.kts`. The codebase uses **manual dependency injection**:
- ViewModels instantiate repositories directly in constructors
- Application context is passed to repositories via AndroidViewModel
- No `@HiltAndroidApp`, `@AndroidEntryPoint`, or `@Inject` annotations are used

### Package Structure
```
com.example.nonelauncher/
├── data/
│   ├── local/           # Room database, DAOs, entities
│   ├── model/           # Data models (AppModel, UiMode, VersionInfo)
│   ├── repository/      # Repository pattern implementations
│   └── UiModeManager.kt # UI mode state management
├── ui/
│   ├── components/      # Reusable UI components (AppIcon, IntentionDialog)
│   ├── home/            # Legacy UI mode screen + ViewModel
│   ├── modern/          # Modern UI mode screen + glass morphism components
│   ├── modes/           # LauncherUiMode enum
│   ├── theme/           # Material 3 theming + glass morphism utilities
│   ├── LauncherScreen.kt  # Root launcher router
│   └── MainViewModel.kt   # Top-level UI mode state
└── MainActivity.kt      # Entry point with AppWidgetHost setup
```

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
- **Glass Morphism**: Black backgrounds with 0.7f alpha + backdrop blur (see `ui/theme/GlassMorphism.kt`, `AcrylicGlass.kt`)
- **Material 3**: Custom theme in `ui/theme/Theme.kt` with edge-to-edge support
- **Wallpaper**: Launcher shows device wallpaper via `FLAG_SHOW_WALLPAPER` in MainActivity

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
- **Current Version**: 1.3.3 (versionCode 7)
- **Target SDK**: 34 (Android 14)
- **Min SDK**: 28 (Android 9)
- **Java Version**: 17
- **Kotlin Version**: 2.2.10

## Common Workflows

### Adding a New Feature to Modern UI
1. Check if feature requires new preference → add to `UserPreferencesRepository`
2. Update `HomeViewModel.uiState` to include new state in `HomeUiState` data class
3. Add UI component to `ui/modern/components/` following glass morphism pattern
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
