package com.weyr_associates.animaltrakkerfarmmobile.app.core

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

data class DocumentMetadata(
    val displayName: String,
    val sizeInBytes: Int
)
object Content {
    @JvmStatic
    fun queryDocumentMetadata(context: Context, uri: Uri): DocumentMetadata? {
        return context.contentResolver.query(uri, null, null, null)?.use { cursor ->
            with (cursor) {
                val displayNameIdx = getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                val sizeInBytesIdx = getColumnIndexOrThrow(OpenableColumns.SIZE)
                moveToFirst()
                DocumentMetadata(getString(displayNameIdx), getInt(sizeInBytesIdx))
            }
        }
    }
}
