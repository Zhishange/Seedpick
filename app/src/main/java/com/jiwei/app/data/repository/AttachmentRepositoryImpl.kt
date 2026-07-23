package com.jiwei.app.data.repository

import android.content.Context
import android.net.Uri
import com.jiwei.app.data.local.dao.AttachmentDao
import com.jiwei.app.data.local.entity.AttachmentEntity
import com.jiwei.app.domain.repository.AttachmentRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttachmentRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val attachmentDao: AttachmentDao
) : AttachmentRepository {

    private val attachmentsDir: File
        get() = File(context.filesDir, "attachments").also { it.mkdirs() }

    override suspend fun saveImage(entryId: String, imageUri: Uri): AttachmentEntity {
        val fileName = "img_${UUID.randomUUID().toString().take(8)}.jpg"
        return saveFileFromUri(entryId, imageUri, fileName, "image/jpeg")
    }

    override suspend fun saveFile(entryId: String, fileUri: Uri, fileName: String, mimeType: String): AttachmentEntity {
        return saveFileFromUri(entryId, fileUri, fileName, mimeType)
    }

    private fun saveFileFromUri(entryId: String, uri: Uri, fileName: String, mimeType: String): AttachmentEntity {
        val uniqueName = "${UUID.randomUUID().toString().take(8)}_$fileName"
        val destFile = File(attachmentsDir, uniqueName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Cannot open input stream for $uri")

        val attachment = AttachmentEntity(
            id = UUID.randomUUID().toString(),
            entryId = entryId,
            fileName = fileName,
            filePath = destFile.absolutePath,
            mimeType = mimeType,
            fileSize = destFile.length(),
            createdAt = System.currentTimeMillis()
        )
        attachmentDao.insert(attachment)
        return attachment
    }

    override suspend fun deleteAttachment(attachmentId: String) {
        val attachment = attachmentDao.getByEntryId(attachmentId).let { atts ->
            atts.firstOrNull { it.id == attachmentId }
        } ?: run {
            attachmentDao.deleteById(attachmentId)
            return
        }
        attachmentDao.deleteById(attachmentId)
        File(attachment.filePath).delete()
    }

    override suspend fun getAttachmentsForEntry(entryId: String): List<AttachmentEntity> {
        return attachmentDao.getByEntryId(entryId)
    }

    override fun getAttachmentsForEntryFlow(entryId: String): Flow<List<AttachmentEntity>> {
        return attachmentDao.getByEntryIdFlow(entryId)
    }

    override suspend fun getAllAttachments(): List<AttachmentEntity> {
        return attachmentDao.getAll()
    }
}
