# domain/ — 领域层

领域层包含纯 Kotlin 业务逻辑，定义 Repository 接口和 UseCase 实现，不依赖 Android 框架。

## 结构

```
domain/
├── model/                          # 领域模型
│   ├── TreeNode.kt                 # 标签树节点
│   ├── GraphData.kt                # 图谱数据模型
│   └── ExportMetadata.kt           # 导出元数据模型
└── usecase/                        # 用例
    ├── CreateEntry.kt              # 创建条目
    ├── UpdateEntry.kt              # 更新条目
    ├── ParseBidirectionalLinks.kt  # 解析 [[]] 语法
    ├── BuildTagTree.kt             # 构建标签树
    ├── SearchEntries.kt            # 搜索条目
    ├── BuildGraphData.kt           # 构建图谱数据
    ├── ExportKnowledgeBase.kt      # 导出知识库
    ├── ImportKnowledgeBase.kt      # 导入知识库
    └── TogglePin.kt                # 切换收藏状态
```

## 关键文件

| 文件 | 目的 |
|------|------|
| `CreateEntry.kt` | 验证输入、生成 UUID、设置时间戳、调用 Repository 插入 |
| `ParseBidirectionalLinks.kt` | 正则匹配 `[[]]` 提取链接目标列表 |
| `BuildTagTree.kt` | 将扁平 TagEntity 列表递归构建为 TreeNode 树 |
| `BuildGraphData.kt` | 将条目和链接转换为 GraphNode + GraphEdge 列表 |
| `ExportKnowledgeBase.kt` | 序列化条目为 Markdown 文件，打包为 ZIP |

## 依赖

**本模块依赖**:
- `data/repository/` — Repository 接口（通过构造函数注入）

**依赖本模块的**:
- `ui/` — ViewModel 调用 UseCase

## 规范

### 设计原则

- 每个 UseCase 类只做一件事
- UseCase 通过构造函数注入 Repository 接口
- UseCase 返回领域模型而非 Room Entity
- 不包含 Android 框架依赖

### 代码模式

```kotlin
class CreateEntry @Inject constructor(
    private val entryRepository: EntryRepository,
    private val tagRepository: TagRepository,
    private val linkParser: ParseBidirectionalLinks,
    private val linkRepository: LinkRepository
) {
    suspend operator fun invoke(
        title: String,
        content: String,
        tags: List<String>
    ): EntryEntity {
        // 业务逻辑
    }
}
```
