package com.jiwei.app.domain.model

data class TreeNode(
    val tagId: String,
    val tagName: String,
    val parentId: String?,
    val children: List<TreeNode> = emptyList()
)
