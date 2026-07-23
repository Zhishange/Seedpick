package com.jiwei.app.data.repository

import android.content.Context
import android.net.Uri
import com.jiwei.app.data.local.dao.AttachmentDao
import com.jiwei.app.data.local.dao.EntryDao
import com.jiwei.app.data.local.dao.EntryLinkDao
import com.jiwei.app.data.local.dao.EntryTagDao
import com.jiwei.app.data.local.dao.TagDao
import com.jiwei.app.data.local.entity.EntryEntity
import com.jiwei.app.data.local.entity.TagEntity
import com.jiwei.app.domain.model.ExportEntryInfo
import com.jiwei.app.domain.model.ExportLinkInfo
import com.jiwei.app.domain.model.ExportMetadata
import com.jiwei.app.domain.model.ImportResult
import com.jiwei.app.domain.model.ImportStrategy
import com.jiwei.app.domain.repository.ExportRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val entryDao: EntryDao,
    private val tagDao: TagDao,
    private val entryTagDao: EntryTagDao,
    private val attachmentDao: AttachmentDao,
    private val entryLinkDao: EntryLinkDao
) : ExportRepository {

    override suspend fun exportToZip(): Uri = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, "export_${System.currentTimeMillis()}")
        exportDir.mkdirs()
        val entriesDir = File(exportDir, "entries").also { it.mkdirs() }
        val attachmentsDir = File(exportDir, "attachments").also { it.mkdirs() }

        val allEntries = entryDao.getAllFlow().let { flow ->
            kotlinx.coroutines.flow.first(flow)
        }
        val allTags = tagDao.getAll()
        val allLinks = entryLinkDao.getAllLinks()
        val allAttachments = attachmentDao.getAll()

        val exportEntries = allEntries.map { entry ->
            val entryTags = entryTagDao.getTagsForEntry(entry.id)
            val tagNames = entryTags.map { buildTagPath(it, allTags) }

            val mdFile = File(entriesDir, "${entry.id}.md")
            mdFile.writeText(entry.content)
            if (entry.title.isNotBlank()) {
                val fullContent = "# ${entry.title}\n\n${entry.content}"
                mdFile.writeText(fullContent)
            }

            ExportEntryInfo(
                id = entry.id,
                title = entry.title,
                file = "entries/${entry.id}.md",
                tags = tagNames,
                isPinned = entry.isPinned,
                createdAt = entry.createdAt,
                updatedAt = entry.updatedAt
            )
        }

        for (attachment in allAttachments) {
            val src = File(attachment.filePath)
            if (src.exists()) {
                src.copyTo(File(attachmentsDir, attachment.fileName), overwrite = true)
            }
        }

        val exportLinks = allLinks.map {
            ExportLinkInfo(sourceEntryId = it.sourceEntryId, targetEntryTitle = it.targetEntryTitle)
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val metadata = ExportMetadata(
            version = 1,
            exportedAt = dateFormat.format(Date()),
            entries = exportEntries,
            links = exportLinks
        )
        val metadataJson = JSONObject().apply {
            put("version", metadata.version)
            put("exportedAt", metadata.exportedAt)
            put("entries", JSONArray().apply {
                metadata.entries.forEach { entry ->
                    put(JSONObject().apply {
                        put("id", entry.id)
                        put("title", entry.title)
                        put("file", entry.file)
                        put("tags", JSONArray(entry.tags))
                        put("isPinned", entry.isPinned)
                        put("createdAt", entry.createdAt)
                        put("updatedAt", entry.updatedAt)
                    })
                }
            })
            put("links", JSONArray().apply {
                metadata.links.forEach { link ->
                    put(JSONObject().apply {
                        put("sourceEntryId", link.sourceEntryId)
                        put("targetEntryTitle", link.targetEntryTitle)
                    })
                }
            })
        }.toString(2)
        File(exportDir, "metadata.json").writeText(metadataJson)

        val zipFile = File(context.cacheDir, "jiwei_export_${System.currentTimeMillis()}.zip")
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            exportDir.walkTopDown().filter { it.isFile }.forEach { file ->
                val relativePath = file.relativeTo(exportDir).path
                zos.putNextEntry(ZipEntry(relativePath))
                file.inputStream().use { it.copyTo(zos) }
                zos.closeEntry()
            }
        }

        exportDir.deleteRecursively()
        Uri.fromFile(zipFile)
    }

    override suspend fun importFromZip(zipUri: Uri, strategy: ImportStrategy): ImportResult =
        withContext(Dispatchers.IO) {
            val importDir = File(context.cacheDir, "import_${System.currentTimeMillis()}")
            importDir.mkdirs()

            var totalEntries = 0
            var importedCount = 0
            var skippedCount = 0
            val errors = mutableListOf<String>()

            try {
                context.contentResolver.openInputStream(zipUri)?.use { inputStream ->
                    ZipInputStream(inputStream).use { zis ->
                        var entry = zis.nextEntry
                        while (entry != null) {
                            val file = File(importDir, entry.name)
                            if (!entry.isDirectory) {
                                file.parentFile?.mkdirs()
                                FileOutputStream(file).use { zis.copyTo(it) }
                            }
                            entry = zis.nextEntry
                        }
                    }
                } ?: throw IllegalStateException("Cannot open ZIP file")

                val metadataFile = File(importDir, "metadata.json")
                if (!metadataFile.exists()) {
                    return@withContext ImportResult(0, 0, 0, listOf("Invalid ZIP: metadata.json not found"))
                }

                val metadataJson = JSONObject(metadataFile.readText())
                val entriesArray = metadataJson.getJSONArray("entries")
                val linksArray = metadataJson.optJSONArray("links")
                val entriesDir = File(importDir, "entries")
                val attachmentsDir = File(importDir, "attachments")

                val now = System.currentTimeMillis()
                for (i in 0 until entriesArray.length()) {
                    val entryJson = entriesArray.getJSONObject(i)
                    val entryId = entryJson.getString("id")
                    totalEntries++

                    val existing = entryDao.getById(entryId)
                    if (existing != null) {
                        when (strategy) {
                            ImportStrategy.SKIP -> {
                                skippedCount++
                                continue
                            }
                            ImportStrategy.OVERWRITE -> {
                                entryDao.delete(entryId)
                            }
                        }
                    }

                    val mdFile = File(entriesDir, "${entryId}.md")
                    val content = if (mdFile.exists()) mdFile.readText() else ""
                    val title = entryJson.optString("title", "Untitled")
                    val isPinned = entryJson.optBoolean("isPinned", false)
                    val createdAt = entryJson.optLong("createdAt", now)
                    val updatedAt = entryJson.optLong("updatedAt", now)

                    val entryEntity = EntryEntity(
                        id = entryId,
                        title = title,
                        content = if (content.startsWith("# ")) {
                            content.substringAfter("\n").trimStart()
                        } else content,
                        isPinned = isPinned,
                        createdAt = createdAt,
                        updatedAt = updatedAt
                    )
                    entryDao.insert(entryEntity)

                    val tags = entryJson.optJSONArray("tags")
                    if (tags != null && tags.length() > 0) {
                        val tagNames = (0 until tags.length()).map { tags.getString(it) }
                        tagNames.forEach { tagName ->
                            val parts = tagName.split("/")
                            var parentId: String? = null
                            parts.forEach { part ->
                                val existingTag = tagDao.getAll().firstOrNull {
                                    it.name == part && it.parentId == parentId
                                }
                                if (existingTag != null) {
                                    parentId = existingTag.id
                                } else {
                                    val newTag = TagEntity(
                                        id = UUID.randomUUID().toString(),
                                        name = part,
                                        parentId = parentId
                                    )
                                    tagDao.insert(newTag)
                                    parentId = newTag.id
                                }
                            }
                            parentId?.let { pid ->
                                val crossRef = com.jiwei.app.data.local.entity.EntryTagCrossRef(
                                    entryId = entryId, tagId = pid
                                )
                                entryTagDao.insertCrossRef(crossRef)
                            }
                        }
                    }

                    importedCount++
                }

                if (linksArray != null && linksArray.length() > 0) {
                    for (i in 0 until linksArray.length()) {
                        val linkJson = linksArray.getJSONObject(i)
                        val sourceEntryId = linkJson.getString("sourceEntryId")
                        val targetEntryTitle = linkJson.getString("targetEntryTitle")
                        val linkEntity = com.jiwei.app.data.local.entity.EntryLinkEntity(
                            sourceEntryId = sourceEntryId,
                            targetEntryTitle = targetEntryTitle
                        )
                        kotlin.runCatching { entryLinkDao.insertAll(listOf(linkEntity)) }
                    }
                }
            } catch (e: Exception) {
                errors.add(e.message ?: "Unknown error")
            } finally {
                importDir.deleteRecursively()
            }

            ImportResult(
                totalEntries = totalEntries,
                importedCount = importedCount,
                skippedCount = skippedCount,
                errors = errors
            )
        }

    private fun buildTagPath(tag: TagEntity, allTags: List<TagEntity>): String {
        val parts = mutableListOf<String>()
        var current: TagEntity? = tag
        while (current != null) {
            parts.add(0, current.name)
            current = if (current.parentId != null) {
                allTags.firstOrNull { it.id == current.parentId }
            } else null
        }
        return parts.joinToString("/")
    }
}
