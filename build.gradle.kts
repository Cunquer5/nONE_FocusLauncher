// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.7.2" apply false // Updated from 8.2.2
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false // Use a stable Kotlin 2.x version
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false // Match KSP to Kotlin version
}
