# 积微 (Jiwei) — 接口文档

## 概述

积微是一个纯本地 Android 应用，无网络 API。本文档描述应用内部的组件接口、数据模型、ViewModel 状态管理和 Room DAO 查询接口。

## 组件接口

### Navigation 路由

| 路由 | 参数 | 说明 |
|------|------|------|
| `home` | — | 主页，卡片网格浏览知识条目 |
| `entry/{entryId}` | entryId: String | 条目详情页（阅读模式） |
| `entry/edit/{entryId}` | entryId: String | 条目编辑页 |
| `search` | — | 全文搜索页 |
| `tags` | — | 标签管理页 |
| `graph` | — | 知识图谱页 |
| `settings` | — | 设置页（主题、导出导入） |

### ViewModel 状态定义

```kotlin
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

## 数据模型

### Room Entity 接口

#### EntryEntity
```kotlin
@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey val id: String,          // UUID v4
    val title: String,                   // 条目标题
    val content: String,                 // Markdown 正文
    val isPinned: Boolean = false,       // 是否收藏置顶
    val createdAt: Long,                 // 创建时间戳
    val updatedAt: Long                  // 更新时间戳
)
```

#### TagEntity
```kotlin
@Entity(
    tableName = "tags",
    indices = [Index("parentId")]
)
data class TagEntity(
    @PrimaryKey val id: String,          // UUID v4
    val name: String,                    // 标签名
    val parentId: String? = null         // 父标签 ID（层级标签）
)
```

#### EntryTagCrossRef
```kotlin
@Entity(
    tableName = "entry_tag_cross_ref",
    primaryKeys = ["entryId", "tagId"],
    indices = [Index("tagId")]
)
data class EntryTagCrossRef(
    val entryId: String,
    val tagId: String
)
```

#### EntryLinkEntity
```kotlin
@Entity(
    tableName = "entry_links",
    primaryKeys = ["sourceEntryId", "targetEntryTitle"],
    indices = [Index("targetEntryTitle")]
)
data class EntryLinkEntity(
    val sourceEntryId: String,           // 源条目 ID
    val targetEntryTitle: String         // 目标条目标题
)
```

#### AttachmentEntity
```kotlin
@Entity(
    tableName = "attachments",
    foreignKeys = [ForeignKey(
        entity = EntryEntity::class,
        parentColumns = ["id"],
        childColumns = ["entryId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class AttachmentEntity(
    @PrimaryKey val id: String,          // UUID v4
    val entryId: String,                 // 所属条目 ID
    val fileName: String,                // 文件名
    val filePath: String,                // 应用私有目录下的相对路径
    val mimeType: String,                // MIME 类型
    val fileSize: Long,                  // 文件大小（字节）
    val createdAt: Long                  // 创建时间戳
)
```

#### EntryFts (全文搜索)
```kotlin
@Fts4(contentEntity = EntryEntity::class)
@Entity(tableName = "entries_fts")
data class EntryFts(
    val title: String,                   // 条目名称
    val content: String,                 // 正文内容
    val tagNames: String                 // 关联标签名（逗号分隔）
)
```

### 关系型查询结果

```kotlin
data class EntryWithTags(
    @Embedded val entry: EntryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = EntryTagCrossRef::class,
            parentColumn = "entryId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)
```

### 知识图谱模型

```kotlin
data class GraphNode(
    val entryId: String,
    val title: String,
    val x: Float,
    val y: Float
)

data class GraphEdge(
    val sourceId: String,
    val targetId: String
)

data class GraphData(
    val nodes: List<GraphNode>,
    val edges: List<GraphEdge>
)
```

### 层级标签树模型

```kotlin
data class TreeNode(
    val tag: TagEntity,
    val children: List<TreeNode>
)
```

### 导出元数据模型

```kotlin
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
```

## Room DAO 查询接口

### EntryDao
```kotlin
@Dao
interface EntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: EntryEntity)

    @Update
    suspend fun update(entry: EntryEntity)

    @Query("DELETE FROM entries WHERE id = :entryId")
    suspend fun delete(entryId: String)

    @Query("SELECT * FROM entries WHERE id = :entryId")
    suspend fun getById(entryId: String): EntryEntity?

    @Query("SELECT * FROM entries ORDER BY updatedAt DESC")
    fun getAllFlow(): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE isPinned = 1 ORDER BY updatedAt DESC")
    fun getPinnedFlow(): Flow<List<EntryEntity>>

    @Query("""
        SELECT entries.* FROM entries
        JOIN entries_fts ON entries.id = entries_fts.rowid
        WHERE entries_fts MATCH :query
        ORDER BY rank
    """)
    fun searchByKeyword(query: String): Flow<List<EntryEntity>>
}
```

### TagDao
```kotlin
@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: TagEntity)

    @Query("DELETE FROM tags WHERE id = :tagId")
    suspend fun delete(tagId: String)

    @Query("SELECT * FROM tags ORDER BY name")
    suspend fun getAll(): List<TagEntity>

    @Query("SELECT * FROM tags WHERE parentId = :parentId")
    suspend fun getChildren(parentId: String): List<TagEntity>

    @Query("SELECT * FROM tags WHERE name LIKE '%' || :query || '%'")
    suspend fun searchByName(query: String): List<TagEntity>
}
```

### EntryTagDao
```kotlin
@Dao
interface EntryTagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: EntryTagCrossRef)

    @Query("DELETE FROM entry_tag_cross_ref WHERE entryId = :entryId")
    suspend fun deleteByEntryId(entryId: String)

    @Query("""
        SELECT tags.* FROM tags
        INNER JOIN entry_tag_cross_ref ON tags.id = entry_tag_cross_ref.tagId
        WHERE entry_tag_cross_ref.entryId = :entryId
    """)
    suspend fun getTagsForEntry(entryId: String): List<TagEntity>

    @Query("""
        SELECT entries.* FROM entries
        INNER JOIN entry_tag_cross_ref ON entries.id = entry_tag_cross_ref.entryId
        WHERE entry_tag_cross_ref.tagId = :tagId
        ORDER BY entries.updatedAt DESC
    """)
    suspend fun getEntriesForTag(tagId: String): List<EntryEntity>
}
```

### EntryLinkDao
```kotlin
@Dao
interface EntryLinkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(links: List<EntryLinkEntity>)

    @Query("DELETE FROM entry_links WHERE sourceEntryId = :entryId")
    suspend fun deleteBySourceEntryId(entryId: String)

    @Query("SELECT sourceEntryId FROM entry_links WHERE targetEntryTitle IN (:titles)")
    suspend fun getBacklinks(titles: List<String>): List<String>

    @Query("SELECT * FROM entry_links")
    suspend fun getAllLinks(): List<EntryLinkEntity>
}
```

### AttachmentDao
```kotlin
@Dao
interface AttachmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachment: AttachmentEntity)

    @Query("DELETE FROM attachments WHERE id = :attachmentId")
    suspend fun deleteById(attachmentId: String)

    @Query("SELECT * FROM attachments WHERE entryId = :entryId")
    suspend fun getByEntryId(entryId: String): List<AttachmentEntity>
}
```
