package com.jiwei.app.data.repository

import com.jiwei.app.data.local.dao.AttachmentDao
import com.jiwei.app.data.local.dao.EntryDao
import com.jiwei.app.data.local.dao.EntryLinkDao
import com.jiwei.app.data.local.dao.EntryTagDao
import com.jiwei.app.data.local.dao.TagDao
import com.jiwei.app.data.local.entity.EntryEntity
import com.jiwei.app.domain.model.EntryWithTags
import com.jiwei.app.domain.repository.EntryRepository
import com.jiwei.app.domain.repository.LinkRepository
import com.jiwei.app.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EntryRepositoryImpl @Inject constructor(
    private val entryDao: EntryDao,
    private val tagDao: TagDao,
    private val entryTagDao: EntryTagDao,
    private val entryLinkDao: EntryLinkDao,
    private val attachmentDao: AttachmentDao,
    private val tagRepository: TagRepository,
    private val linkRepository: LinkRepository
) : EntryRepository {

    override suspend fun createEntry(title: String, content: String): EntryEntity {
        val now = System.currentTimeMillis()
        val entry = EntryEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            content = content,
            isPinned = false,
            createdAt = now,
            updatedAt = now
        )
        entryDao.insert(entry)
        return entry
    }

    override suspend fun updateEntry(
        entryId: String,
        title: String,
        content: String,
        tags: List<String>,
        isPinned: Boolean
    ): EntryEntity {
        val existing = entryDao.getById(entryId)
            ?: throw IllegalStateException("Entry not found: $entryId")
        val updated = existing.copy(
            title = title,
            content = content,
            isPinned = isPinned,
            updatedAt = System.currentTimeMillis()
        )
        entryDao.update(updated)
        tagRepository.updateEntryTags(entryId, tags)
        linkRepository.extractAndSaveLinks(entryId, content)
        return updated
    }

    override suspend fun deleteEntry(entryId: String) {
        entryDao.delete(entryId)
    }

    override suspend fun getEntryById(entryId: String): EntryEntity? {
        return entryDao.getById(entryId)
    }

    override suspend fun getEntryWithDetails(entryId: String): EntryWithTags? {
        val entry = entryDao.getById(entryId) ?: return null
        val tags = entryTagDao.getTagsForEntry(entryId)
        val attachments = attachmentDao.getByEntryId(entryId)
        val outgoingLinks = entryLinkDao.getLinksForEntry(entryId)
        val backlinkIds = entryLinkDao.getBacklinkSourceIds(listOf(entry.title))
        val backlinks = if (backlinkIds.isNotEmpty()) entryDao.getByIds(backlinkIds) else emptyList()
        return EntryWithTags(
            entry = entry,
            tags = tags,
            attachments = attachments,
            outgoingLinks = outgoingLinks,
            backlinks = backlinks
        )
    }

    override fun getAllEntries(): Flow<List<EntryEntity>> = entryDao.getAllFlow()

    override fun getPinnedEntries(): Flow<List<EntryEntity>> = entryDao.getPinnedFlow()

    override fun getUnpinnedEntries(): Flow<List<EntryEntity>> = entryDao.getUnpinnedFlow()

    override fun searchEntries(query: String): Flow<List<EntryEntity>> {
        val safeQuery = query.trim().replace("\"", "")
        return if (safeQuery.isBlank()) {
            entryDao.getAllFlow()
        } else {
            val ftsQuery = safeQuery.split("\\s+".toRegex())
                .joinToString(" AND ") { "$it*" }
            entryDao.searchByKeyword(ftsQuery)
        }
    }

    override suspend fun togglePin(entryId: String): EntryEntity {
        val entry = entryDao.getById(entryId)
            ?: throw IllegalStateException("Entry not found: $entryId")
        val updated = entry.copy(isPinned = !entry.isPinned)
        entryDao.update(updated)
        return updated
    }
}
