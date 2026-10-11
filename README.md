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


##Website:https://none-launcher.vercel.app/
