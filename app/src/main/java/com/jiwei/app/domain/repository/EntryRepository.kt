package com.jiwei.app.domain.repository

import com.jiwei.app.data.local.entity.EntryEntity
import com.jiwei.app.domain.model.EntryWithTags
import kotlinx.coroutines.flow.Flow

interface EntryRepository {
    suspend fun createEntry(title: String, content: String): EntryEntity

    suspend fun updateEntry(
        entryId: String,
        title: String,
        content: String,
        tags: List<String>,
        isPinned: Boolean
    ): EntryEntity

    suspend fun deleteEntry(entryId: String)

    suspend fun getEntryById(entryId: String): EntryEntity?

    suspend fun findEntryByTitle(title: String): EntryEntity?

    suspend fun getEntryWithDetails(entryId: String): EntryWithTags?

    fun getAllEntries(): Flow<List<EntryEntity>>

    fun getPinnedEntries(): Flow<List<EntryEntity>>

    fun getUnpinnedEntries(): Flow<List<EntryEntity>>

    fun searchEntries(query: String): Flow<List<EntryEntity>>

    suspend fun togglePin(entryId: String): EntryEntity
}
