# nONE FocusLauncher

 nONE FocusLauncher is a minimalist Android system launcher designed to enhance productivity by reducing distractions.

## Features

- **Default Launcher**: Replaces your standard home screen.
- **Focus Mode**: Grayscale filters for non-essential apps to reduce visual stimuli.
- **Intention Setting**: Prompts you to state your purpose before launching distracting apps.
- **Privacy First**: All data (intentions, settings) is stored locally. No intent access, no ads.

## Architecture

- **Language**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Architecture**: MVVM
- **Persistence**: Room Database (SQLite) & SharedPreferences

## Setup

1. Open the project in Android Studio Iguana or later.
2. Sync Gradle projects.
3. Build and Run on an Android Device or Emulator (API 28+).
4. **Important**: When prompted, select "nONE Launcher" as your Default Home App.

## Development

- **App Module**: `app/src/main/java/com/example/nONE launcher`
- **Home Screen**: `ui/home/HomeScreen.kt`
- **Data Layer**: `data/repository`

## Permissions

- `QUERY_ALL_PACKAGES`: Required to list installed applications.
- `SET_WALLPAPER`: Allows you to change the wallpaper from within the launcher.

## Changelog

### v1.3.0 - January 30, 2026

#### Features
- **Dual-Section Swipeable App Drawer**: Separated app grid and widgets into two distinct swipeable pages for a cleaner interface.
- **Dedicated Widget Grid**: Full-screen widget management page with dynamic fitting and reflowing.
- **Dynamic Widget Height**: Widgets now automatically adjust their height based on their requirements while maintaining a minimum touch target.
- **Clean App Grid**: Removed the widget carousel from the first page of the app drawer to focus purely on application access.
- **Improved Navigation**: Added page indicators and smoother horizontal scrolling between app and widget sections.

### v1.1.0 - January 21, 2026

#### Features
- **Black Glass Morphism UI**: Consistent glass morphism effects with backdrop blur on all modern UI components (navbar, 1 Things section, app grid, launcher settings, intention history, context menus)
- **Orange Active State Indicator**: Selected dock items (text and icons) glow in saturated orange (#FF4D00) for visual feedback
- **Dynamic Color System**: Replaced hardcoded dark brown colors with pure black for a cleaner, modern aesthetic
- **UI Refinements**: 
  - Adjusted transparency and opacity values for better glass effect visibility
  - Optimized card and dock width for improved spacing
  - Enhanced button styling with 56dp consistent height and proper text fitting
- **App Icon Adaptive Sizing**: Fixed app icons in focus app management carousel to properly fit squircle shapes (36dp icons with correct aspect ratio)
- **Default Wallpaper Option**: Orange (#FF4D00) wallpaper as default choice in launcher settings with custom wallpaper selection support
- **Mode Selector**: Implemented normal, focus, and blur mode selector with consistent orange brand theme
- **App & Widget Management**: Added comprehensive add/delete functionality for apps and widgets with black glass context menus

#### UI/UX Improvements
- Modern Intention Dialog: Restructured button layout with Cancel and Proceed buttons (56dp height, equal width)
- Dropdown Menus: Applied black glass morphism (0.7f alpha) with white text to all context menus
- App Icons: Added semi-transparent black background (0.3f alpha) for depth and visual consistency
- Widget Cards: Black glass styling for widget removal options
