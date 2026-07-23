# data/ — 数据层

数据层负责所有本地持久化操作，包括 Room 数据库管理、实体定义、DAO 查询接口和 Repository 实现。

## 结构

```
data/
├── local/
│   ├── db/
│   │   └── JiweiDatabase.kt       # Room 数据库抽象类
│   ├── dao/
│   │   ├── EntryDao.kt            # 条目数据访问
│   │   ├── TagDao.kt              # 标签数据访问
│   │   ├── EntryTagDao.kt         # 条目-标签交叉引用
│   │   ├── EntryLinkDao.kt        # 双向链接数据访问
│   │   └── AttachmentDao.kt       # 附件数据访问
│   ├── entity/
│   │   ├── EntryEntity.kt         # 条目实体
│   │   ├── EntryFts.kt            # FTS4 全文搜索虚拟表
│   │   ├── TagEntity.kt           # 标签实体
│   │   ├── EntryTagCrossRef.kt    # 条目-标签多对多关联
│   │   ├── EntryLinkEntity.kt     # 双向链接实体
│   │   └── AttachmentEntity.kt    # 附件实体
│   └── converter/                 # Room TypeConverter
└── repository/                    # Repository 实现类
    ├── EntryRepositoryImpl.kt
    ├── TagRepositoryImpl.kt
    ├── LinkRepositoryImpl.kt
    ├── AttachmentRepositoryImpl.kt
    ├── ExportRepositoryImpl.kt
    └── ThemeRepositoryImpl.kt
```

## 关键文件

| 文件 | 目的 |
|------|------|
| `JiweiDatabase.kt` | Room 数据库入口，声明所有 Entity 和 DAO |
| `EntryDao.kt` | 条目的 CRUD、FTS 搜索、排序查询 |
| `TagDao.kt` | 标签的 CRUD、层级查询、名称搜索 |
| `EntryFts.kt` | FTS4 虚拟表，提供标题、正文、标签名的全文索引 |

## 依赖

**本模块依赖**:
- Room Runtime / KTX
- Kotlin Coroutines

**依赖本模块的**:
- `domain/repository/` — Repository 接口的实现
- `di/AppModule.kt` — 通过 Hilt 提供 Database 实例

## 规范

### 命名

- Entity: `[Name]Entity.kt`
- DAO: `[Name]Dao.kt`
- Repository 实现: `[Name]RepositoryImpl.kt`
- 交叉表: `[Name1][Name2]CrossRef.kt`

### 代码模式

**查询返回 Flow**:
```kotlin
@Query("SELECT * FROM entries ORDER BY updatedAt DESC")
fun getAllFlow(): Flow<List<EntryEntity>>
```

**事务操作**:
```kotlin
@Transaction
suspend fun insertEntryWithTags(entry: EntryEntity, tags: List<TagEntity>) {
    insert(entry)
    tags.forEach { tag ->
        insert(tag)
        insertCrossRef(EntryTagCrossRef(entry.id, tag.id))
    }
}
```
