# nONE FocusLauncher

 nONE FocusLauncher is a minimalist Android system launcher designed to enhance productivity by reducing distractions.

## Features

- **Default Launcher**: Replaces your standard home screen.
- **Focus Mode**: Grayscale filters for non-essential apps to reduce visual stimuli.
- **Intention Setting**: Prompts you to state your purpose before launching distracting apps.
- **Privacy First**: All data (intentions, settings) is stored locally. No intent access, no ads.
- **First-Launch Welcome**: One-time onboarding dialog when you install and open the app.
- **Feedback Prompts**: Optional, locally timed nudge to share feedback on the nONE dashboard—no analytics.

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
- `READ_CONTACTS`: Required for searching and displaying system contacts in Universal Search.
- `READ_EXTERNAL_STORAGE` / `READ_MEDIA_*`: Required for searching and accessing local files.

## Changelog

### v1.4.2 - May 19, 2026

#### Features
- **First-Launch Welcome**: One-time dialog on first open after install, with guidance to set nONE as default Home and an overview of swipe-down search.
- **Feedback Prompt**: Respectful, optional prompt to visit the [feedback dashboard](https://none-launcher.vercel.app/dashboard).
  - **Visit Website** opens the dashboard in the browser.
  - **Later** snoozes the prompt for 30 days.
  - **No Thanks** permanently disables feedback prompts.
- Shown after **5** default-home resumes **or** **7 days** of use—not on first launch.

#### Privacy
- Prompt timing uses local `SharedPreferences` only (launch count, first-use date, last prompt time, opt-out flag).
- No analytics SDKs and no tracking of whether the user visits the feedback site.

### v1.4.1 - May 11, 2026

#### Features
- **Universal Search Bar**: Replaced the standard app drawer with a comprehensive search experience.
  - **App Search**: Instantly find and launch installed applications.
  - **Contact Search**: Search system contacts and view details or call directly from the search bar.
  - **File Search**: Search for images, videos, audio, and documents across device storage.
- **Categorized Results**: Search results are now grouped by type (Apps, Contacts, Files) with distinct icons and metadata.
- **Dynamic Permission Handling**: Implemented proactive runtime permission requests for Contacts and Files when searching.

#### Technical Improvements
- **Optimized Data Fetching**: Added 300ms debounce and parallel coroutine execution for search queries to ensure zero-lag typing.
- **Resilient Architecture**: Implemented robust error handling and `try-catch` safety in repositories to prevent crashes from system data access issues.
- **Modern UI Integration**: Fully integrated the new search overlay with the existing glass morphism theme and backdrop blur effects.
- **Gradle Cleanup**: Resolved plugin conflicts and removed deprecated build properties in preparation for AGP 10.0.

### v1.3.3 - February 7, 2026

#### Features
- **Enhanced Visual Design**: Added white borders to app drawer and launcher settings cards for better visual definition
- **Improved UI Navigation**: Implemented mutual exclusivity between intention history and launcher settings
- **Better User Flow**: Closing intention history now automatically opens launcher settings
- **Visual Refinements**: Increased border opacity from 0.1f to 0.25f for better visibility
- **Transparency Adjustments**: Reduced settings card background opacity from 0.6f to 0.46f for improved transparency
- **Button Enhancement**: Set focus task button now has 100% transparency with saturated orange color

### v1.3.2 - February 3, 2026

#### Features
- **Persistent Subtask State**: Subtask completion status now survives app restarts and closing.
- **Reliable Task Tracking**: Individual subtasks maintain their checked/unchecked state consistently.

### v1.3.1 - January 30, 2026

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
