package com.jiwei.app.domain.model

data class ExportMetadata(
    val version: Int = 1,
    val exportedAt: String,
    val entries: List<ExportEntryInfo>,
    val links: List<ExportLinkInfo>
)

data class ExportEntryInfo(
    val id: String,
    val title: String,
    val file: String,
    val tags: List<String>,
    val isPinned: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

data class ExportLinkInfo(
    val sourceEntryId: String,
    val targetEntryTitle: String
)

enum class ImportStrategy {
    OVERWRITE,
    SKIP
}

data class ImportResult(
    val totalEntries: Int,
    val importedCount: Int,
    val skippedCount: Int,
    val errors: List<String> = emptyList()
)
