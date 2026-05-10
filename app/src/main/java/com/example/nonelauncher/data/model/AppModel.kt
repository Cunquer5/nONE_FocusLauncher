package com.example.nonelauncher.data.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap

@Immutable
data class AppModel(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap,
    val isEssential: Boolean = false
)
