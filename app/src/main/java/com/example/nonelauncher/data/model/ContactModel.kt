package com.example.nonelauncher.data.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap

@Immutable
data class ContactModel(
    val id: String,
    val name: String,
    val phoneNumber: String?,
    val photo: ImageBitmap? = null
)
