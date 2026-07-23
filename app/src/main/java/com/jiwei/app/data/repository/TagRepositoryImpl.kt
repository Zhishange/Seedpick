package com.jiwei.app.data.repository

import com.jiwei.app.data.local.dao.EntryTagDao
import com.jiwei.app.data.local.dao.TagDao
import com.jiwei.app.data.local.entity.EntryEntity
import com.jiwei.app.data.local.entity.EntryTagCrossRef
import com.jiwei.app.data.local.entity.TagEntity
import com.jiwei.app.domain.model.TreeNode
import com.jiwei.app.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao,
    private val entryTagDao: EntryTagDao
) : TagRepository {

    override suspend fun findOrCreateTag(name: String, parentPath: String?): TagEntity {
        val existing = tagDao.searchByName(name)
        if (parentPath != null) {
            val parentTag = existing.firstOrNull { it.parentId != null }
            if (parentTag != null) return parentTag
        } else {
            val rootTag = existing.firstOrNull { it.parentId == null }
            if (rootTag != null) return rootTag
        }
        val tag = TagEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            parentId = parentPath?.let { findOrCreateTag(it).id }
        )
        tagDao.insert(tag)
        return tag
    }

    override suspend fun createHierarchy(fullPath: String): TagEntity {
        val parts = fullPath.split("/")
        return parts.fold<String?, TagEntity>(null) { parentId, name ->
            val tag = TagEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                parentId = parentId
            )
            tagDao.insert(tag)
            tag
        }
    }

    override suspend fun deleteTag(tagId: String) {
        tagDao.delete(tagId)
    }

    override suspend fun getAllTags(): List<TagEntity> = tagDao.getAll()

    override suspend fun searchTags(query: String): List<TagEntity> {
        if (query.isBlank()) return getAllTags()
        return tagDao.searchByName(query)
    }

    override suspend fun buildTagTree(): List<TreeNode> {
        val allTags = tagDao.getAll()
        val rootTags = allTags.filter { it.parentId == null }
        return rootTags.map { buildTreeNode(it, allTags) }
    }

    private fun buildTreeNode(tag: TagEntity, allTags: List<TagEntity>): TreeNode {
        val children = allTags.filter { it.parentId == tag.id }
        return TreeNode(
            tagId = tag.id,
            tagName = tag.name,
            parentId = tag.parentId,
            children = children.map { buildTreeNode(it, allTags) }
        )
    }

    override suspend fun getEntriesForTag(tagId: String): List<EntryEntity> {
        return entryTagDao.getEntriesForTag(tagId)
    }

    override suspend fun getEntriesForTagRecursive(tagId: String): List<EntryEntity> {
        val childIds = getChildTagIds(tagId)
        val allTagIds = listOf(tagId) + childIds
        if (allTagIds.isEmpty()) return emptyList()
        val entries = entryTagDao.getEntriesForTags(allTagIds)
        return entries.distinctBy { it.id }
    }

    override fun getEntriesForTagsFlow(tagIds: List<String>): Flow<List<EntryEntity>> {
        return entryTagDao.getEntriesForTagsFlow(tagIds)
    }

    override suspend fun getTagsForEntry(entryId: String): List<TagEntity> {
        return entryTagDao.getTagsForEntry(entryId)
    }

    override suspend fun updateEntryTags(entryId: String, tagNames: List<String>) {
        entryTagDao.deleteByEntryId(entryId)
        if (tagNames.isEmpty()) return

        val crossRefs = tagNames.mapNotNull { tagName ->
            val tag = findOrCreateTag(tagName)
            EntryTagCrossRef(entryId = entryId, tagId = tag.id)
        }
        if (crossRefs.isNotEmpty()) {
            entryTagDao.insertCrossRefs(crossRefs)
        }
    }

    override suspend fun getChildTagIds(tagId: String): List<String> {
        val result = mutableListOf<String>()
        val children = tagDao.getChildren(tagId)
        for (child in children) {
            result.add(child.id)
            result.addAll(getChildTagIds(child.id))
        }
        return result
    }
}
