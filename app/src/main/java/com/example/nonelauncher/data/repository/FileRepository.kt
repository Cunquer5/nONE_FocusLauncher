package com.example.nonelauncher.data.repository

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.example.nonelauncher.data.model.FileModel
import com.example.nonelauncher.data.model.FileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class FileRepository(private val context: Context) {

    private val projection = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.Files.FileColumns.DISPLAY_NAME,
        MediaStore.Files.FileColumns.SIZE,
        MediaStore.Files.FileColumns.MIME_TYPE
    )

    suspend fun searchFiles(query: String): List<FileModel> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        if (!canReadFiles(context)) return@withContext emptyList()

        val files = mutableListOf<FileModel>()
        val contentResolver = context.contentResolver

        val collections = mutableListOf(
            MediaStore.Files.getContentUri("external")
        )
        
        // On newer Android versions, MediaStore.Files might not return everything, 
        // so we might need specific collections, but for now we'll stick to external.
        // Also avoiding redundant queries.

        val selection = "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$query%")
        val sortOrder = "${MediaStore.Files.FileColumns.DISPLAY_NAME} ASC"

        try {
            collections.forEach { uri ->
                contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                    while (cursor.moveToNext()) {
                        // Using URI as key instead of path since DATA is deprecated
                        val file = cursorToFileModel(uri, cursor) ?: continue
                        if (files.none { it.uri == file.uri }) files.add(file)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        files.take(20)
    }

    /**
     * Resolves the Essentials shelf's pinned uris. Reading them back from MediaStore is what drops
     * a deleted file from the shelf on its own, with no bookkeeping of ours.
     */
    suspend fun getFilesByUris(uris: Set<String>): List<FileModel> = withContext(Dispatchers.IO) {
        if (!canReadFiles(context)) return@withContext emptyList()

        // Only the numeric id taken from a content uri reaches the selection, so it cannot carry input.
        val ids = uris.mapNotNull { Uri.parse(it).lastPathSegment?.toLongOrNull() }
        if (ids.isEmpty()) return@withContext emptyList()

        val collection = MediaStore.Files.getContentUri("external")
        val selection = "${MediaStore.Files.FileColumns._ID} IN (${ids.joinToString(",")})"
        val files = mutableListOf<FileModel>()
        try {
            context.contentResolver.query(collection, projection, selection, null, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    cursorToFileModel(collection, cursor)?.let { files.add(it) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        files
    }

    private fun cursorToFileModel(
        collection: Uri,
        cursor: android.database.Cursor,
    ): FileModel? {
        val idIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
        val nameIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
        if (idIndex == -1 || nameIndex == -1) return null

        val id = cursor.getLong(idIndex)
        val name = cursor.getString(nameIndex) ?: "Unknown"
        val sizeIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
        val size = if (sizeIndex != -1) cursor.getLong(sizeIndex) else 0L
        val mimeIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)
        val mimeType = if (mimeIndex != -1) cursor.getString(mimeIndex) else null

        val fileType = when {
            mimeType?.startsWith("image/") == true -> FileType.IMAGE
            mimeType?.startsWith("video/") == true -> FileType.VIDEO
            mimeType?.startsWith("audio/") == true -> FileType.AUDIO
            mimeType?.startsWith("application/pdf") == true ||
            mimeType?.startsWith("text/") == true -> FileType.DOCUMENT
            else -> FileType.OTHER
        }

        return FileModel(id, name, "", Uri.withAppendedPath(collection, id.toString()), size, fileType)
    }

    companion object {
        /**
         * Whether file search can actually read rows for the current query.
         * Documents/ebooks/archives are only visible through MediaStore.Files to a UID that holds
         * All files access (Android 11+), so that is the primary route; the media permissions keep
         * images/video/audio searchable when it is not granted (no regression for media-only users).
         */
        fun canReadFiles(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                return true
            }
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }
        }
    }
}
