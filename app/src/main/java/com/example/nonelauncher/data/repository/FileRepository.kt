package com.example.nonelauncher.data.repository

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.nonelauncher.data.model.FileModel
import com.example.nonelauncher.data.model.FileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class FileRepository(private val context: Context) {

    suspend fun searchFiles(query: String): List<FileModel> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        // Check permissions based on Android version
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }

        if (!hasPermission) return@withContext emptyList()

        val files = mutableListOf<FileModel>()
        val contentResolver = context.contentResolver

        val collections = mutableListOf(
            MediaStore.Files.getContentUri("external")
        )
        
        // On newer Android versions, MediaStore.Files might not return everything, 
        // so we might need specific collections, but for now we'll stick to external.
        // Also avoiding redundant queries.

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        val selection = "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$query%")
        val sortOrder = "${MediaStore.Files.FileColumns.DISPLAY_NAME} ASC"

        try {
            collections.forEach { uri ->
                contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                    val idIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
                    val nameIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                    val mimeIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)

                    while (cursor.moveToNext()) {
                        if (idIndex == -1 || nameIndex == -1) continue
                        
                        val id = cursor.getLong(idIndex)
                        val name = cursor.getString(nameIndex) ?: "Unknown"
                        val size = if (sizeIndex != -1) cursor.getLong(sizeIndex) else 0L
                        val mimeType = if (mimeIndex != -1) cursor.getString(mimeIndex) else null

                        val fileType = when {
                            mimeType?.startsWith("image/") == true -> FileType.IMAGE
                            mimeType?.startsWith("video/") == true -> FileType.VIDEO
                            mimeType?.startsWith("audio/") == true -> FileType.AUDIO
                            mimeType?.startsWith("application/pdf") == true || 
                            mimeType?.startsWith("text/") == true -> FileType.DOCUMENT
                            else -> FileType.OTHER
                        }

                        val fileUri = Uri.withAppendedPath(uri, id.toString())
                        
                        // Using URI as key instead of path since DATA is deprecated
                        if (files.none { it.uri == fileUri }) {
                            files.add(FileModel(id, name, "", fileUri, size, fileType))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        files.take(20)
    }
}
