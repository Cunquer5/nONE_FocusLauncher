package com.example.nonelauncher.data.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap

@Immutable
data class AppModel(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap,
    val isEssential: Boolean = false,
    // Membership in the user's "Essentials" shelf, not the auto-detected isEssential above
    val isPinned: Boolean = false,
    val isUninstallable: Boolean = false
)
