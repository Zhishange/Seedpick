package com.jiwei.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jiwei.app.data.local.entity.EntryEntity
import com.jiwei.app.data.local.entity.EntryTagCrossRef
import com.jiwei.app.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryTagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: EntryTagCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(crossRefs: List<EntryTagCrossRef>)

    @Query("DELETE FROM entry_tag_cross_ref WHERE entryId = :entryId")
    suspend fun deleteByEntryId(entryId: String)

    @Query("DELETE FROM entry_tag_cross_ref WHERE tagId = :tagId")
    suspend fun deleteByTagId(tagId: String)

    @Query("""
        SELECT tags.* FROM tags
        INNER JOIN entry_tag_cross_ref ON tags.id = entry_tag_cross_ref.tagId
        WHERE entry_tag_cross_ref.entryId = :entryId
        ORDER BY tags.name
    """)
    suspend fun getTagsForEntry(entryId: String): List<TagEntity>

    @Query("""
        SELECT entries.* FROM entries
        INNER JOIN entry_tag_cross_ref ON entries.id = entry_tag_cross_ref.entryId
        WHERE entry_tag_cross_ref.tagId = :tagId
        ORDER BY entries.updatedAt DESC
    """)
    suspend fun getEntriesForTag(tagId: String): List<EntryEntity>

    @Query("""
        SELECT entries.* FROM entries
        INNER JOIN entry_tag_cross_ref ON entries.id = entry_tag_cross_ref.entryId
        WHERE entry_tag_cross_ref.tagId IN (:tagIds)
        ORDER BY entries.updatedAt DESC
    """)
    suspend fun getEntriesForTags(tagIds: List<String>): List<EntryEntity>

    @Query("""
        SELECT entries.* FROM entries
        INNER JOIN entry_tag_cross_ref ON entries.id = entry_tag_cross_ref.entryId
        WHERE entry_tag_cross_ref.tagId IN (:tagIds)
        ORDER BY entries.updatedAt DESC
    """)
    fun getEntriesForTagsFlow(tagIds: List<String>): Flow<List<EntryEntity>>
}
