package com.jiwei.app.domain.usecase

import com.jiwei.app.data.local.entity.EntryEntity
import com.jiwei.app.domain.repository.EntryRepository
import javax.inject.Inject

class CreateEntry @Inject constructor(
    private val entryRepository: EntryRepository
) {
    suspend operator fun invoke(title: String, content: String): EntryEntity {
        return entryRepository.createEntry(title, content)
    }
}
