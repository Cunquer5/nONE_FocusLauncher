package com.example.nonelauncher.data.model

import androidx.compose.runtime.Immutable
import android.net.Uri

@Immutable
data class FileModel(
    val id: Long,
    val name: String,
    val path: String,
    val uri: Uri,
    val size: Long,
    val type: FileType
)

enum class FileType {
    IMAGE, VIDEO, AUDIO, DOCUMENT, OTHER
}
