package com.example.nonelauncher.data.model

data class VersionInfo(
    val versionName: String,
    val versionCode: Int,
    val releaseDate: String,
    val features: List<String>
)

data class SubTask(
    val id: String = java.util.UUID.randomUUID().toString(), // Unique identifier
    val text: String,
    val isCompleted: Boolean = false
)

data class OneThingTask(
    val mainTask: String,
    val subTasks: List<SubTask>
)

fun serializeOneThingTask(task: OneThingTask): String {
    return "${task.mainTask}|${task.subTasks.joinToString(";") { "${it.id},${it.text.replace("|", "\\|").replace(";", "\\;").replace(",", "\\,")},${it.isCompleted}" }}"
}

fun parseOneThingTaskWithState(serialized: String?): OneThingTask? {
    if (serialized.isNullOrEmpty()) return null
    val parts = serialized.split("|")
    if (parts.size < 2) return null
    
    val mainTask = parts[0]
    val subTasks = parts[1].split(";").filter { it.isNotEmpty() }.mapNotNull { subStr ->
        val subParts = subStr.split(",")
        if (subParts.size >= 3) {
            SubTask(
                id = subParts[0],
                text = subParts[1].replace("\\|", "|").replace("\\;", ";").replace("\\,", ","),
                isCompleted = subParts[2].toBoolean()
            )
        } else null
    }
    
    return OneThingTask(mainTask, subTasks)
}

val Changelog = listOf(
    VersionInfo(
        versionName = "1.3.2",
        versionCode = 6,
        releaseDate = "2026-02-03",
        features = listOf(
            "persistent subtask state for 1 Thing todo list",
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
