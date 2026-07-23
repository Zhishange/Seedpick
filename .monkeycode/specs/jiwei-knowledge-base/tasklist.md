# 需求实施计划

- [x] 1. 创建 Android 项目基础结构
  - 创建 Gradle 构建文件（项目级和模块级 build.gradle.kts），配置 Kotlin、Compose、Room、Hilt、Navigation 等依赖
  - 创建 AndroidManifest.xml，声明应用权限（INTERNET、存储读写）
  - 创建 Application 类并添加 @HiltAndroidApp 注解
  - 创建 Compose 主题系统基础框架（Theme.kt、Color.kt、Type.kt），定义浅色和深色调色板 (Req 9)
  - 创建 Hilt DI 模块（AppModule 提供 Database、DataStore；RepositoryModule 提供各 Repository）
  - 创建 Navigation 骨架（JiweiNavGraph），定义 6 条路由：home、entry/{id}、entry/edit/{id?}、search、tags、graph
  - 创建 MainActivity，设置 Compose 内容并挂载 NavHost
  - * 1.1 为 Gradle 依赖版本编写 Catalog 验证脚本
  - * 1.2 为 Hilt 模块编写编译时验证测试

- [x] 2. 检查点 #1 — 确保项目结构可编译，如有疑问请询问用户

- [x] 3. 实现数据层 — Room 数据库
  - 创建 EntryEntity、TagEntity、EntryTagCrossRef、EntryLinkEntity、AttachmentEntity 五个实体类 (Req 1, 2, 4, 5)
  - 创建 EntryFts FTS4 虚拟表，包含 title、content、tagNames 列 (Req 3)
  - 创建 EntryDao（insert、update、delete、getById、getAllFlow、getPinnedFlow、searchByKeyword），搜索方法使用 FTS MATCH 查询 (Req 1.2, 3.1, 6.4)
  - 创建 TagDao（insert、upsert、delete、getAll、getChildren、searchByName），支持按 parentId 查询子标签和关键词模糊匹配 (Req 2.5, 2.3)
  - 创建 EntryTagDao（insertCrossRef、deleteCrossRef、getTagsForEntry、getEntriesForTag、getEntriesForTagRecursive），递归查询需包含所有子孙标签的条目 (Req 2.2)
  - 创建 EntryLinkDao（insertAll、deleteBySourceEntryId、getBacklinks、getAllLinks），用于双向链接和知识图谱查询 (Req 5.3, 8.1)
  - 创建 AttachmentDao（insert、deleteById、getByEntryId） (Req 4.3)
  - 创建 JiweiDatabase 抽象类，定义版本号、迁移策略和各 DAO 抽象方法
  - 创建 TypeConverter 处理 Long/Date 转换和 List<String> 到 JSON 的转换
  - * 3.1 为每个 DAO 编写 Room 内存数据库插桩测试

- [ ] 4. 实现领域层 — Repository 接口与实现
  - 创建 domain 包结构，定义 Repository 接口（EntryRepository、TagRepository、LinkRepository、AttachmentRepository、ExportRepository、ThemeRepository）
  - 实现 EntryRepositoryImpl：条目的 CRUD 操作、Flow 查询、收藏状态管理、FTS 搜索调用，保存条目时同步更新 FTS 索引和双向链接 (Req 1.2, 3.1, 6.4, 10.1)
  - 实现 TagRepositoryImpl：标签 CRUD、按名称查询自动补全、层级树构建（递归 parentId 查询）、获取标签及其所有子孙标签的条目 (Req 2.3, 2.5, 2.6)
  - 实现 LinkRepositoryImpl：保存条目时解析 `[[ ]]` 语法提取链接目标、全量更新 EntryLinkEntity 表、查询反向链接、查询所有链接用于图谱 (Req 5.1, 5.3, 8.1)
  - 实现 AttachmentRepositoryImpl：文件复制到应用私有目录、按条目 ID 查询附件列表、删除附件时同步清理文件 (Req 4.3, 4.4)
  - 实现 ThemeRepositoryImpl：基于 DataStore 读写主题偏好（LIGHT/DARK/SYSTEM） (Req 9.3)
  - * 4.1 为每个 Repository 编写单元测试（MockK + JUnit）
  - * 4.2 为链接解析编写属性测试（验证 `[[ ]]` 语法解析的幂等性和边界情况）

- [ ] 5. 检查点 #2 — 确保数据层和领域层可编译且测试通过，如有疑问请询问用户

- [ ] 6. 实现条目管理模块 (Req 1)
  - 创建 EntryEditViewModel：管理 title、content、tags 状态，处理新建和编辑两种模式，调用 CreateEntry/UpdateEntry UseCase，保存后通过双向链接解析更新 EntryLinkEntity (Req 1.1, 1.2, 5.1)
  - 创建 Markdown 实时预览解析器（MarkdownParser）：基于 AnnotatedString，支持 H1-H6、加粗、斜体、行内代码、代码块、无序列表、引用块、`[[]]` 双向链接的语法解析和样式渲染 (Req 1.5)
  - 实现增量解析策略：仅重新解析光标所在段落，其余段落缓存渲染结果 (Req 1.5)
  - 创建 EntryEditScreen：使用 BasicTextField + AnnotatedString 实现编辑区域，标题输入框，底部 Markdown 快捷工具栏（标题、加粗、列表、代码块、图片、链接、`[[]]` 按钮） (Req 1.6)
  - 创建 EntryDetailViewModel：加载条目详情、渲染 Markdown 为可点击的 AnnotatedString（`[[ ]]` 链接可点击跳转）、加载反向链接列表 (Req 5.2, 5.3, 5.4)
  - 创建 EntryDetailScreen：顶部标题栏含收藏和编辑按钮，正文区渲染 Markdown，底部展示"被以下条目引用"的反向链接面板 (Req 10.1, 5.3)
  - * 6.1 为 MarkdownParser 编写单元测试（覆盖所有语法类型的渲染正确性）
  - * 6.2 为 EntryEditViewModel 编写 Turbine Flow 测试

- [ ] 7. 实现标签系统 (Req 2)
  - 创建 BuildTagTree UseCase：将扁平 TagEntity 列表按 parentId 构建为 TreeNode 树形结构 (Req 2.5)
  - 创建 TagManageViewModel：管理标签树状态、展开/折叠节点、选中标签后展示关联条目列表 (Req 2.6)
  - 创建 TagManageScreen：树形标签列表（递归 Compose 组件，缩进 + 展开/折叠图标），选中标签后底部展示条目卡片列表 (Req 2.2, 2.6)
  - 创建标签选择组件（TagSelector）：在编辑界面使用，输入时自动补全已有标签，支持输入 `/` 创建层级标签 (Req 2.1, 2.3, 2.4, 2.5)
  - * 7.1 为 BuildTagTree UseCase 编写单元测试（验证空列表、单层、多层嵌套的树构建）
  - * 7.2 为 TagManageViewModel 编写 Flow 测试

- [ ] 8. 检查点 #3 — 确保条目管理和标签功能可编译且测试通过，如有疑问请询问用户

- [ ] 9. 实现全文搜索 (Req 3)
  - 创建 SearchViewModel：管理搜索 query 状态，通过 EntryDao.searchByKeyword 执行 FTS MATCH 查询返回 Flow，支持防抖（300ms），按相关度排序 (Req 3.1, 3.3)
  - 创建 SearchScreen：顶部搜索输入框，搜索结果列表（每项显示标题、高亮匹配关键词、标签列表），空状态提示 (Req 3.2, 3.4)
  - 实现搜索结果关键词高亮组件（SearchHighlightText）：将匹配文本用 AnnotatedString 的 SpanStyle 高亮标记 (Req 3.2)
  - * 9.1 为 SearchViewModel 编写 Turbine Flow 测试（验证防抖和排序逻辑）
  - * 9.2 为 SearchHighlightText 编写 Compose UI 测试

- [ ] 10. 实现附件管理 (Req 4)
  - 创建 AttachmentManager 工具类：处理从相册/相机获取图片（ActivityResultContracts），从文件选择器获取文件（OPEN_DOCUMENT），将文件复制到应用私有目录（context.filesDir/attachments/） (Req 4.1, 4.2, 4.3)
  - 在 EntryEditScreen 的 Markdown 工具栏中集成图片插入和文件添加按钮，插入图片后自动生成 `![描述](attachment://uuid)` 语法 (Req 4.1, 4.2)
  - 在 EntryDetailScreen 中渲染附件链接为可点击/可预览的元素，图片使用 Coil 加载显示 (Req 4.1)
  - * 10.1 为 AttachmentManager 编写单元测试（模拟文件复制操作）

- [ ] 11. 实现双向链接 (Req 5)
  - 创建 ParseBidirectionalLinks UseCase：使用正则 `\[\[([^\]]+)\]\]` 解析正文中所有双向链接引用 (Req 5.1)
  - 在 MarkdownParser 中将 `[[条目名称]]` 渲染为蓝色可点击的 AnnotatedString pushStringAnnotation (Req 5.1, 5.2)
  - 在 EntryDetailScreen 中处理 Annotation 点击事件：查找目标条目，存在则导航到详情页，不存在则弹窗询问是否创建 (Req 5.2, 5.4)
  - 在 EntryDetailScreen 底部实现反向链接面板：展示所有引用当前条目的条目卡片列表 (Req 5.3)
  - * 11.1 为 ParseBidirectionalLinks 编写单元测试（验证各种 [[ ]] 嵌套和边界情况）

- [ ] 12. 检查点 #4 — 确保搜索、附件和双向链接功能可编译且测试通过，如有疑问请询问用户

- [ ] 13. 实现卡片浏览主页 (Req 6, Req 10)
  - 创建 HomeViewModel：管理 entries Flow、收藏筛选状态、标签筛选状态、排序模式，调用 EntryDao.getPinnedFlow 置顶收藏条目 (Req 6.4, 6.5, 10.1, 10.4)
  - 创建 EntryCard 组件：卡片式布局展示标题、正文摘要（截取前 80 字）、标签列表（Chip 样式），收藏条目显示星标标识 (Req 6.2, 10.2)
  - 创建 HomeScreen：LazyVerticalStaggeredGrid 瀑布流卡片布局，顶部标签筛选栏（水平滚动 Chip 列表），排序切换按钮（时间/标题），FAB 新建按钮 (Req 6.1, 6.3, 6.4, 6.5, 10.4)
  - * 13.1 为 HomeViewModel 编写 Flow 测试（验证筛选和排序逻辑）
  - * 13.2 为 EntryCard 编写 Compose 快照测试（浅色/深色主题、收藏/非收藏状态）

- [ ] 14. 实现知识图谱 (Req 8)
  - 创建 BuildGraphData UseCase：从 EntryLinkDao.getAllLinks 获取所有链接关系，结合 EntryDao 条目列表，构建 ForceDirectedGraph 数据模型（nodes: List<GraphNode>, edges: List<GraphEdge>） (Req 8.1)
  - 实现力导向布局算法（ForceDirectedLayout）：基于库仑斥力（节点间）和胡克引力（连线节点间），迭代计算节点位置直到收敛 (Req 8.1)
  - 创建 GraphViewModel：管理图谱数据、选中节点状态、缩放偏移状态，调用 BuildGraphData 并运行力导向布局 (Req 8.2, 8.3, 8.5)
  - 创建 GraphScreen：Compose Canvas 自定义绘制节点（圆形 + 标题文字）、连线，支持双指缩放（transformable）和拖拽平移，点击节点高亮并显示信息浮层，双击跳转详情 (Req 8.2, 8.3, 8.4, 8.5)
  - * 14.1 为力导向布局算法编写单元测试（验证节点不重叠、迭代收敛）
  - * 14.2 为 BuildGraphData 编写单元测试

- [ ] 15. 实现数据导出导入 (Req 7)
  - 创建 ExportKnowledgeBase UseCase：查询所有条目、标签、链接、附件，生成 metadata.json，将 Markdown 文件和附件打包为 ZIP (Req 7.1, 7.2)
  - 创建 ImportKnowledgeBase UseCase：解压 ZIP，解析 metadata.json，根据 UUID 判断条目是否存在决定覆盖或跳过，批量插入条目/标签/链接，复制附件文件 (Req 7.4, 7.5)
  - 创建 SettingsViewModel：管理导出/导入进度状态，调用 Export/Import UseCase
  - 创建设置界面（在 SettingsScreen 中添加导出/导入区域）：点击导出后调用系统分享面板发送 ZIP 文件，点击导入后调用文件选择器选取 ZIP 文件并解析 (Req 7.3)
  - * 15.1 为 ExportKnowledgeBase UseCase 编写单元测试（验证生成文件格式和内容）
  - * 15.2 为 ImportKnowledgeBase UseCase 编写单元测试（验证冲突处理策略）

- [ ] 16. 检查点 #5 — 确保知识图谱和导出导入功能可编译且测试通过，如有疑问请询问用户

- [ ] 17. 完善设置与深色模式 (Req 9)
  - 完善 SettingsScreen：添加主题切换区域（跟随系统 / 浅色 / 深色三个选项的 RadioButton 组），添加导出、导入操作入口 (Req 9.1, 9.3, 9.4)
  - 在 JiweiTheme 中实现 isSystemInDarkTheme 检测和手动主题覆盖逻辑，通过 CompositionLocal 向下传递主题状态 (Req 9.2, 9.3)
  - 确保所有已实现的 Screen 和组件在深色模式下配色正确（背景、文字、卡片、分割线颜色适配） (Req 9.2)

- [ ] 18. 系统级完善
  - 为各 ViewModel 添加错误处理：捕获 Room 异常和 IO 异常，通过 UiState 密封类传递给 UI 层展示 Snackbar 提示
  - 添加条目删除确认对话框组件（AlertDialog），附带"同时删除附件"提示 (Req 1.4)
  - 确保 MainActivity 设置正确的 WindowInsets 处理和 Edge-to-Edge 显示
  - 创建应用图标资源（mipmap 各密度）

- [ ] 19. 检查点 #6 — 最终检查，确保全部功能可编译且测试通过，如有疑问请询问用户
