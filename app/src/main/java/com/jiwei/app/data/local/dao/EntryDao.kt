package com.jiwei.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jiwei.app.data.local.entity.EntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: EntryEntity)

    @Update
    suspend fun update(entry: EntryEntity)

    @Query("DELETE FROM entries WHERE id = :entryId")
    suspend fun delete(entryId: String)

    @Query("SELECT * FROM entries WHERE id = :entryId")
    suspend fun getById(entryId: String): EntryEntity?

    @Query("SELECT * FROM entries WHERE title = :title LIMIT 1")
    suspend fun getByTitle(title: String): EntryEntity?

    @Query("SELECT * FROM entries ORDER BY updatedAt DESC")
    fun getAllFlow(): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE isPinned = 1 ORDER BY updatedAt DESC")
    fun getPinnedFlow(): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE isPinned = 0 ORDER BY updatedAt DESC")
    fun getUnpinnedFlow(): Flow<List<EntryEntity>>

    @Query("SELECT COUNT(*) FROM entries")
    suspend fun getCount(): Int

    @Query("""
        SELECT entries.* FROM entries
        JOIN entries_fts ON entries.id = entries_fts.rowid
        WHERE entries_fts MATCH :query
        ORDER BY rank
    """)
    fun searchByKeyword(query: String): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE id IN (:entryIds) ORDER BY updatedAt DESC")
    suspend fun getByIds(entryIds: List<String>): List<EntryEntity>
}
