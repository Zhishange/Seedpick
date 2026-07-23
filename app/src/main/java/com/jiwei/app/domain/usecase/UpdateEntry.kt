package com.jiwei.app.domain.usecase

import com.jiwei.app.data.local.entity.EntryEntity
import com.jiwei.app.domain.repository.EntryRepository
import javax.inject.Inject

class UpdateEntry @Inject constructor(
    private val entryRepository: EntryRepository
) {
    suspend operator fun invoke(
        entryId: String,
        title: String,
        content: String,
        tags: List<String>,
        isPinned: Boolean
    ): EntryEntity {
        return entryRepository.updateEntry(entryId, title, content, tags, isPinned)
    }
}
