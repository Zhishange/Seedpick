package com.jiwei.app.domain.model

import com.jiwei.app.data.local.entity.AttachmentEntity
import com.jiwei.app.data.local.entity.EntryEntity
import com.jiwei.app.data.local.entity.EntryLinkEntity
import com.jiwei.app.data.local.entity.TagEntity

data class EntryWithTags(
    val entry: EntryEntity,
    val tags: List<TagEntity>,
    val attachments: List<AttachmentEntity> = emptyList(),
    val outgoingLinks: List<EntryLinkEntity> = emptyList(),
    val backlinks: List<EntryEntity> = emptyList()
)
