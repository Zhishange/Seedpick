package com.jiwei.app.domain.repository

import android.net.Uri
import com.jiwei.app.data.local.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

interface AttachmentRepository {
    suspend fun saveImage(entryId: String, imageUri: Uri): AttachmentEntity

    suspend fun saveFile(entryId: String, fileUri: Uri, fileName: String, mimeType: String): AttachmentEntity

    suspend fun deleteAttachment(attachmentId: String)

    suspend fun getAttachmentsForEntry(entryId: String): List<AttachmentEntity>

    fun getAttachmentsForEntryFlow(entryId: String): Flow<List<AttachmentEntity>>

    suspend fun getAllAttachments(): List<AttachmentEntity>
}
