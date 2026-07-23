package com.jiwei.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jiwei.app.data.local.entity.EntryLinkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryLinkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(links: List<EntryLinkEntity>)

    @Query("DELETE FROM entry_links WHERE sourceEntryId = :entryId")
    suspend fun deleteBySourceEntryId(entryId: String)

    @Query("""
        SELECT sourceEntryId FROM entry_links
        WHERE targetEntryTitle IN (:titles)
    """)
    suspend fun getBacklinkSourceIds(titles: List<String>): List<String>

    @Query("SELECT * FROM entry_links")
    suspend fun getAllLinks(): List<EntryLinkEntity>

    @Query("SELECT * FROM entry_links")
    fun getAllLinksFlow(): Flow<List<EntryLinkEntity>>

    @Query("SELECT * FROM entry_links WHERE sourceEntryId = :entryId")
    suspend fun getLinksForEntry(entryId: String): List<EntryLinkEntity>
}
