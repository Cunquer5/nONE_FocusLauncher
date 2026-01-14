package com.example.focuslauncher.data.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap

@Immutable
data class AppModel(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap,
    val isFocusApp: Boolean = false,
    val isEssential: Boolean = false,
    // Transient property for UI state, not persisted
    val isFocusMode: Boolean = false
)
