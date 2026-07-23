package com.jiwei.app.data.repository

import com.jiwei.app.data.local.dao.EntryDao
import com.jiwei.app.data.local.dao.EntryLinkDao
import com.jiwei.app.data.local.entity.EntryEntity
import com.jiwei.app.data.local.entity.EntryLinkEntity
import com.jiwei.app.domain.repository.LinkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinkRepositoryImpl @Inject constructor(
    private val entryLinkDao: EntryLinkDao,
    private val entryDao: EntryDao
) : LinkRepository {

    override suspend fun extractAndSaveLinks(entryId: String, content: String) {
        entryLinkDao.deleteBySourceEntryId(entryId)
        val linkTargets = parseBidirectionalLinks(content)
        if (linkTargets.isNotEmpty()) {
            val links = linkTargets.map { targetTitle ->
                EntryLinkEntity(
                    sourceEntryId = entryId,
                    targetEntryTitle = targetTitle
                )
            }
            entryLinkDao.insertAll(links)
        }
    }

    override suspend fun getOutgoingLinks(entryId: String): List<EntryLinkEntity> {
        return entryLinkDao.getLinksForEntry(entryId)
    }

    override suspend fun getBacklinks(entryTitle: String): List<EntryEntity> {
        val sourceIds = entryLinkDao.getBacklinkSourceIds(listOf(entryTitle))
        if (sourceIds.isEmpty()) return emptyList()
        return entryDao.getByIds(sourceIds)
    }

    override suspend fun getAllLinks(): List<EntryLinkEntity> {
        return entryLinkDao.getAllLinks()
    }

    override fun getAllLinksFlow(): Flow<List<EntryLinkEntity>> {
        return entryLinkDao.getAllLinksFlow()
    }

    private fun parseBidirectionalLinks(content: String): List<String> {
        val regex = Regex("\\[\\[([^\\]]+)\\]\\]")
        return regex.findAll(content).map { it.groupValues[1].trim() }
            .filter { it.isNotEmpty() }
            .toList()
    }
}
