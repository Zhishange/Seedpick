package com.jiwei.app.domain.repository

import com.jiwei.app.data.local.entity.EntryEntity
import com.jiwei.app.data.local.entity.EntryLinkEntity
import kotlinx.coroutines.flow.Flow

interface LinkRepository {
    suspend fun extractAndSaveLinks(entryId: String, content: String)

    suspend fun getOutgoingLinks(entryId: String): List<EntryLinkEntity>

    suspend fun getBacklinks(entryTitle: String): List<EntryEntity>

    suspend fun getAllLinks(): List<EntryLinkEntity>

    fun getAllLinksFlow(): Flow<List<EntryLinkEntity>>
}
