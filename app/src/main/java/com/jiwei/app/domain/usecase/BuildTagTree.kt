package com.jiwei.app.domain.usecase

import com.jiwei.app.data.local.entity.TagEntity
import com.jiwei.app.domain.model.TreeNode
import javax.inject.Inject

class BuildTagTree @Inject constructor() {

    operator fun invoke(tags: List<TagEntity>): List<TreeNode> {
        val rootTags = tags.filter { it.parentId == null }
        return rootTags.map { buildNode(it, tags) }
    }

    private fun buildNode(tag: TagEntity, allTags: List<TagEntity>): TreeNode {
        val children = allTags.filter { it.parentId == tag.id }
        return TreeNode(
            tagId = tag.id,
            tagName = tag.name,
            parentId = tag.parentId,
            children = children.map { buildNode(it, allTags) }
        )
    }
}
