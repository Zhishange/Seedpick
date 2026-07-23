package com.jiwei.app.domain.repository

import com.jiwei.app.data.local.entity.EntryEntity
import com.jiwei.app.data.local.entity.TagEntity
import com.jiwei.app.domain.model.TreeNode
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    suspend fun findOrCreateTag(name: String, parentPath: String? = null): TagEntity

    suspend fun createHierarchy(fullPath: String): TagEntity

    suspend fun deleteTag(tagId: String)

    suspend fun getAllTags(): List<TagEntity>

    suspend fun searchTags(query: String): List<TagEntity>

    suspend fun buildTagTree(): List<TreeNode>

    suspend fun getEntriesForTag(tagId: String): List<EntryEntity>

    suspend fun getEntriesForTagRecursive(tagId: String): List<EntryEntity>

    fun getEntriesForTagsFlow(tagIds: List<String>): Flow<List<EntryEntity>>

    suspend fun getTagsForEntry(entryId: String): List<TagEntity>

    suspend fun updateEntryTags(entryId: String, tagNames: List<String>)

    suspend fun getChildTagIds(tagId: String): List<String>
}
