package com.example.nonelauncher.data.repository

import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.provider.ContactsContract
import androidx.compose.ui.graphics.asImageBitmap
import com.example.nonelauncher.data.model.ContactModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class ContactRepository(private val context: Context) {

    suspend fun searchContacts(query: String): List<ContactModel> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        
        // Check permission before querying
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) 
            != PackageManager.PERMISSION_GRANTED) {
            return@withContext emptyList()
        }

        val contacts = mutableListOf<ContactModel>()
        val contentResolver: ContentResolver = context.contentResolver
        
        try {
            val uri = ContactsContract.Contacts.CONTENT_URI
            val projection = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                ContactsContract.Contacts.HAS_PHONE_NUMBER
            )
            
            val selection = "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE ?"
            val selectionArgs = arrayOf("%$query%")
            val sortOrder = "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"

            contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                val photoUriIndex = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)
                val hasPhoneIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                if (idIndex == -1 || nameIndex == -1) return@use

                while (cursor.moveToNext()) {
                    val id = cursor.getString(idIndex)
                    val name = cursor.getString(nameIndex) ?: "Unknown"
                    val photoUri = if (photoUriIndex != -1) cursor.getString(photoUriIndex) else null
                    val hasPhone = if (hasPhoneIndex != -1) cursor.getInt(hasPhoneIndex) > 0 else false

                    var phoneNumber: String? = null
                    if (hasPhone) {
                        phoneNumber = getPhoneNumber(id, contentResolver)
                    }

                    val photo = if (photoUri != null) {
                        try {
                            val inputStream: InputStream? = contentResolver.openInputStream(android.net.Uri.parse(photoUri))
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            bitmap?.asImageBitmap()
                        } catch (e: Exception) {
                            null
                        }
                    } else null

                    contacts.add(ContactModel(id, name, phoneNumber, photo))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        contacts
    }

    private fun getPhoneNumber(contactId: String, contentResolver: ContentResolver): String? {
        val phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val selection = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?"
        val selectionArgs = arrayOf(contactId)

        contentResolver.query(phoneUri, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            }
        }
        return null
    }
}
