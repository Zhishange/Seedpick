# Requirements Document

## Introduction

积微（Jiwei）是一款 Android 端本地知识库应用，帮助用户随手记录碎片知识，通过标签分类、双向链接、全文搜索和卡片式浏览，将零散信息逐步构建为个人知识体系。

应用名取自《荀子》"积微者著"——积累细微，终成显赫。所有数据存储于本地，支持导出备份。

## Glossary

- **System / 系统**: 积微 Android 应用
- **Entry / 条目**: 一条知识记录，包含标题、正文、标签、附件、双向链接
- **Tag / 标签**: 用于分类知识条目的文本标识
- **Bidirectional Link / 双向链接**: 知识条目之间通过 `[[条目名称]]` 语法建立的相互引用关系
- **Card / 卡片**: 主页展示知识条目的视觉单元，显示标题、摘要、标签
- **Attachment / 附件**: 嵌入知识条目的图片或文件
- **Export / 导出**: 将知识库数据打包输出为可迁移的文件格式
- **Grid View / 网格视图**: 以卡片网格布局展示知识条目的浏览方式
- **Knowledge Graph / 知识图谱**: 以节点和连线可视化展示知识条目之间双向链接关系的交互式视图
- **Theme / 主题**: 应用的色彩方案，包含浅色模式和深色模式
- **Live Preview / 实时预览**: Obsidian 风格的 Markdown 编辑体验，在同一个编辑器中输入语法后光标离开即自动渲染，光标所在行保持源码状态
- **Tag Hierarchy / 标签层级**: 以 `/` 分隔符表示嵌套标签结构，例如"编程/Android/Kotlin"
- **Pinned Entry / 收藏置顶**: 标记为重要的知识条目，在主页卡片网格中优先展示

## Requirements

### Requirement 1: 知识条目管理

**User Story:** AS 用户, I want 创建和编辑知识条目, so that 我能记录和整理碎片知识

#### Acceptance Criteria

1. WHEN 用户点击"新建条目"按钮, THE 系统 SHALL 创建一个空白知识条目并进入编辑界面
2. WHEN 用户在编辑界面输入标题和正文后点击"保存", THE 系统 SHALL 将条目持久化到本地数据库并返回浏览界面
3. WHEN 用户在条目列表中长按某条目, THE 系统 SHALL 显示"编辑"和"删除"操作选项
4. IF 用户选择删除条目, THE 系统 SHALL 在删除前弹出确认对话框
5. THE 系统 SHALL 支持 Obsidian 风格的 Markdown 实时预览——输入语法后光标离开该行即自动渲染，光标所在行保持源码编辑状态
6. THE 系统 SHALL 在编辑界面工具栏提供 Markdown 语法快捷插入按钮（标题、加粗、列表、代码块、双向链接等）

### Requirement 2: 标签分类系统

**User Story:** AS 用户, I want 为知识条目添加标签, so that 我能按主题分类和筛选知识

#### Acceptance Criteria

1. WHEN 用户编辑条目时输入标签名称, THE 系统 SHALL 将标签与当前条目关联并持久化
2. WHEN 用户点击某个标签, THE 系统 SHALL 展示所有关联该标签及其子标签的知识条目
3. THE 系统 SHALL 在标签选择界面提供已有标签的自动补全建议
4. THE 系统 SHALL 支持一个条目关联多个标签
5. THE 系统 SHALL 支持以 `/` 分隔符创建层级标签（如"编程/Android"），并在标签管理界面以树形结构展示
6. THE 系统 SHALL 在标签树形界面中支持展开和折叠子标签

### Requirement 3: 全文搜索

**User Story:** AS 用户, I want 搜索知识条目内容, so that 我能快速定位目标知识

#### Acceptance Criteria

1. WHEN 用户在主界面搜索框输入关键词, THE 系统 SHALL 实时返回标题、正文或标签名中包含该关键词的条目列表
2. THE 系统 SHALL 在搜索结果中高亮匹配的关键词
3. THE 系统 SHALL 在搜索结果中按相关性排序
4. IF 搜索关键词无匹配结果, THE 系统 SHALL 显示"无搜索结果"的空状态提示

### Requirement 4: 图片与附件管理

**User Story:** AS 用户, I want 在知识条目中插入图片和文件, so that 我能丰富知识内容

#### Acceptance Criteria

1. WHEN 用户在编辑条目时选择"插入图片", THE 系统 SHALL 调用系统相册或相机获取图片并嵌入正文
2. WHEN 用户在编辑条目时选择"添加附件", THE 系统 SHALL 调用系统文件选择器选取文件并关联到条目
3. THE 系统 SHALL 将图片和附件的副本存储于应用私有目录
4. IF 用户删除包含附件的条目, THE 系统 SHALL 同时删除关联的附件文件

### Requirement 5: 双向链接

**User Story:** AS 用户, I want 通过 `[[条目名称]]` 语法建立条目间的引用关系, so that 我能构建知识网络

#### Acceptance Criteria

1. WHEN 用户在正文中输入 `[[条目名称]]` 语法, THE 系统 SHALL 将其解析为指向目标条目的超链接
2. WHEN 用户点击双向链接, THE 系统 SHALL 跳转到目标条目详情页
3. THE 系统 SHALL 在目标条目详情页底部展示"被以下条目引用"的反向链接列表
4. IF 引用的目标条目不存在, THE 系统 SHALL 提示用户是否创建该条目

### Requirement 6: 卡片浏览

**User Story:** AS 用户, I want 以卡片网格方式浏览知识条目, so that 我能直观概览知识库

#### Acceptance Criteria

1. THE 系统 SHALL 在主页以卡片网格布局展示知识条目
2. THE 系统 SHALL 在每张卡片上展示条目标题、正文摘要、标签列表
3. WHEN 用户点击某张卡片, THE 系统 SHALL 跳转到该条目的详情页
4. THE 系统 SHALL 支持按最近修改时间排序卡片
5. THE 系统 SHALL 支持按标签筛选卡片展示范围

### Requirement 7: 数据导出与备份

**User Story:** AS 用户, I want 导出知识库数据, so that 我能备份或迁移数据

#### Acceptance Criteria

1. WHEN 用户选择"导出数据", THE 系统 SHALL 将全部知识条目、标签、附件打包为单个 ZIP 文件
2. THE 系统 SHALL 在导出数据中包含 Markdown 格式的条目正文和 JSON 格式的元数据（标签、链接关系）
3. WHEN 导出完成后, THE 系统 SHALL 调用系统分享面板供用户选择保存位置或发送方式
4. THE 系统 SHALL 支持从 ZIP 备份文件导入恢复数据
5. IF 导入数据与现有数据存在冲突, THE 系统 SHALL 提示用户选择覆盖或跳过策略

### Requirement 8: 知识图谱可视化

**User Story:** AS 用户, I want 以可视化图谱方式浏览条目间的关联, so that 我能直观感知知识网络结构

#### Acceptance Criteria

1. THE 系统 SHALL 在知识图谱视图中以节点表示知识条目、以连线表示双向链接关系
2. WHEN 用户点击图谱中的某个节点, THE 系统 SHALL 高亮该节点及其直接关联的相邻节点
3. WHEN 用户点击某个节点, THE 系统 SHALL 展示该条目的标题和摘要信息浮层
4. WHEN 用户双击某个节点, THE 系统 SHALL 跳转到该条目的详情页
5. THE 系统 SHALL 支持在图谱视图中拖拽平移和双指缩放操作

### Requirement 9: 深色模式

**User Story:** AS 用户, I want 在深色主题下使用应用, so that 在弱光环境中获得舒适的阅读体验

#### Acceptance Criteria

1. THE 系统 SHALL 提供浅色和深色两种主题
2. WHEN 用户切换主题, THE 系统 SHALL 即时更新全部界面的色彩方案
3. THE 系统 SHALL 默认跟随系统深色模式设置
4. THE 系统 SHALL 在设置界面提供手动切换主题的开关

### Requirement 10: 收藏置顶

**User Story:** AS 用户, I want 收藏和置顶重要知识条目, so that 我能快速访问高频使用的知识

#### Acceptance Criteria

1. WHEN 用户在条目详情页点击收藏按钮, THE 系统 SHALL 将该条目标记为已收藏并在主页卡片中优先排列
2. THE 系统 SHALL 在已收藏条目的卡片上显示醒目的收藏标识
3. WHEN 用户在条目详情页再次点击收藏按钮, THE 系统 SHALL 取消该条目的收藏状态
4. THE 系统 SHALL 在主页提供按收藏状态筛选卡片的选项
