package com.jiwei.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jiwei.app.data.local.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachment: AttachmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attachments: List<AttachmentEntity>)

    @Query("DELETE FROM attachments WHERE id = :attachmentId")
    suspend fun deleteById(attachmentId: String)

    @Query("DELETE FROM attachments WHERE entryId = :entryId")
    suspend fun deleteByEntryId(entryId: String)

    @Query("SELECT * FROM attachments WHERE entryId = :entryId ORDER BY createdAt ASC")
    suspend fun getByEntryId(entryId: String): List<AttachmentEntity>

    @Query("SELECT * FROM attachments WHERE entryId = :entryId ORDER BY createdAt ASC")
    fun getByEntryIdFlow(entryId: String): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments")
    suspend fun getAll(): List<AttachmentEntity>
}
