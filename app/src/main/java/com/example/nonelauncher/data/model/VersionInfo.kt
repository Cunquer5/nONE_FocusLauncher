package com.example.nonelauncher.data.model

data class VersionInfo(
    val versionName: String,
    val versionCode: Int,
    val releaseDate: String,
    val features: List<String>
)

val Changelog = listOf(
    VersionInfo(
        versionName = "1.5.0",
        versionCode = 11,
        releaseDate = "2026-10-27",
        features = listOf(
            "Departure Board redesign: matte split-flap console floating over the wallpaper",
            "home board with the NONE flap wordmark, a live clock, and the 1 Thing white paint band",
            "structure as No./Step/Status rows — tap any row to set the current step",
            "current row carries the orange beam; past rows strike through and dim in place",
            "board-material dock strip and square spindle handle replace the glass dock and side artifacts",
            "search, Structure, 1 Thing, settings, history, wallpaper and intention gate rebuilt on the board material",
            "welcome copy updated to the current gestures: handle left for Structure, right for search, up for the dock",
            "board stays composed behind a scrim when a sheet opens; tap the scrim to close",
            "1 Thing completion persists as Done today with a New task action — the 5-second auto-clear is gone",
            "explicit check targets, resting handle labels, AA metadata, locale-aware clock and 12/24-hour support",
            "focus mode switch, real Settings/History empty states, and status-bar/navigation-bar safe sheets",
            "app grid, contact and file rows, long-press menus and the default-home gate use board wells and hairlines — the last glass surfaces are gone"
        )
    ),
    VersionInfo(
        versionName = "1.4.2",
        versionCode = 10,
        releaseDate = "2026-05-19",
        features = listOf(
            "first-launch welcome dialog for new installs with contextual default-home guidance",
            "feedback prompt linking to none-launcher.vercel.app/dashboard (Visit Website, Later, No Thanks)",
            "respectful feedback timing: after 5 home resumes or 7 days of use, 30-day snooze on dismiss",
            "privacy-first prompts: local SharedPreferences only—no analytics SDK or visit tracking",
            "welcome and feedback prompt coordination so only one dialog shows per resume"
        )
    ),
    VersionInfo(
        versionName = "1.4.1",
        versionCode = 9,
        releaseDate = "2026-05-11",
        features = listOf(
            "Universal Search Bar: Search for apps, contacts, and files in one place",
            "Categorized search results with distinct icons and metadata",
            "Dynamic runtime permission handling for contacts and storage",
            "Optimized search performance with debounce and parallel fetching",
            "Gradle build system cleanup and AGP 10.0 readiness"
        )
    ),
    VersionInfo(
        versionName = "1.4.0",
        versionCode = 8,
        releaseDate = "2026-05-11",
        features = listOf(
            "Legacy UI mode removed - now single Modern UI only",
            "App grid replaced: swipe down from home screen to activate app search",
            "Simplified home screen with dock navigation only",
            "Removed separate widgets section - integrated into search",
            "Cleaner, minimal interface focused on productivity"
        )
    ),
    VersionInfo(
        versionName = "1.3.3",
        versionCode = 7,
        releaseDate = "2026-02-07",
        features = listOf(
            "added white borders to app drawer and launcher settings cards for better visual definition",
            "implemented mutual exclusivity between intention history and launcher settings",
            "improved UI navigation flow: closing history automatically opens settings",
            "increased border opacity from 0.1f to 0.25f for better visibility",
            "reduced settings card background opacity from 0.6f to 0.46f for improved transparency",
            "set focus task button now has 100% transparency with saturated orange color"
        )
    ),
    VersionInfo(
        versionName = "1.3.2",
        versionCode = 6,
        releaseDate = "2026-02-03",
        features = listOf(
            "persistent subtask state for 1 thing todo list",
            "subtask completion status now survives app restarts",
            "proper serialization and deserialization of subtask states"
        )
    ),
    VersionInfo(
        versionName = "1.3.1",
        versionCode = 5,
        releaseDate = "2026-02-03",
        features = listOf(
            "fixed getting stuck at homescreen",
            "fixed getting stuck at recent apps section"
        )
    ),
    VersionInfo(
        versionName = "1.3.0",
        versionCode = 4,
        releaseDate = "2026-01-30",
        features = listOf(
            "Dual-section swipeable Modern App Drawer (Apps vs Widgets)",
            "Dedicated Full-screen Widget Grid with dynamic fitting and dynamic height",
            "Clean Page 1 App Grid with widget carousel removed",
            "Improved horizontal swipe navigation between sections",
            "Enhanced widget management with long-press context menus",
            "one thing todo with optional steps breakdown-dynamic task input box, vanishes when incomplete task present and comesback when task checked off",
            "emphasis on 1 thing in dock" 
        )
    ),
    VersionInfo(
        versionName = "1.2.0",
        versionCode = 3,
        releaseDate = "2026-01-21",
        features = listOf(
            "consistent brand theme black and orange, with black glass morphism",
            "app image fit app icon in focus management carousal section",
            "deafult orange wallpaper option given with, wallaper changes visible in modern ui mode",
            "add/delete app icon and widgets in app grid in modern ui mode",
            "card alignment properly set",
            "dock item glow in orange colour when selected"
        )
    ),
    VersionInfo(
        versionName = "1.1.0",
        versionCode = 2,
        releaseDate = "2026-01-16",
        features = listOf(
            "Updated to Kotlin 2.1.0 and latest Compose compiler.",
            "Improved performance: smooth scrolling and fast load times (<500ms).",
            "Fixed app congestion issues in 5 and 6 column grid layouts.",
            "Resolved color naming conflicts and internal refactoring.",
            "Finalized nONE launcher rebranding and README updates."
        )
    ),
    VersionInfo(
        versionName = "1.0.0",
        versionCode = 1,
        releaseDate = "2024-05-20",
        features = listOf(
            "Initial Release: nONE Launcher rebranding.",
            "nONE Mode: Grayscale and Blur effects for non-essential apps.",
            "Intention Logging: Prompt before opening distracting apps.",
            "App Grid Customization: Choose 4, 5, or 6 columns.",
            "Pull-down to Search: Quick access to find apps.",
            "Adaptive Icons: Unified circular design for all apps.",
            "Battery Optimization: Direct link to set unrestricted background usage."
        )
    )
)
