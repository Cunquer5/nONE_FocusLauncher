package com.example.nonelauncher.data.model

data class VersionInfo(
    val versionName: String,
    val versionCode: Int,
    val releaseDate: String,
    val features: List<String>
)

val Changelog = listOf(
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
