# ui/ — UI 层

UI 层负责 Jetpack Compose 界面的渲染和用户交互处理，包括所有 Screen、ViewModel 和主题定义。

## 结构

```
ui/
├── theme/
│   ├── Color.kt                    # 浅色/深色调色板
│   ├── Type.kt                     # Typography 定义
│   └── Theme.kt                    # Material 3 主题 (JiweiTheme)
├── home/
│   ├── HomeScreen.kt               # 主页卡片网格
│   └── HomeViewModel.kt            # 主页状态管理
├── entry/
│   ├── detail/
│   │   ├── EntryDetailScreen.kt    # 条目详情（阅读模式）
│   │   └── EntryDetailViewModel.kt
│   └── edit/
│       ├── EntryEditScreen.kt      # 条目编辑（实时预览）
│       └── EntryEditViewModel.kt
├── search/
│   ├── SearchScreen.kt             # 搜索页面
│   └── SearchViewModel.kt
├── tag/
│   ├── TagManageScreen.kt          # 标签管理（树形结构）
│   └── TagManageViewModel.kt
├── graph/
│   ├── GraphScreen.kt              # 知识图谱（Canvas 绘制）
│   └── GraphViewModel.kt
├── settings/
│   ├── SettingsScreen.kt           # 设置页面
│   └── SettingsViewModel.kt
└── components/                     # 可复用 Compose 组件
    ├── EntryCard.kt                # 知识条目卡片
    ├── TagChip.kt                  # 标签芯片
    ├── SearchHighlightText.kt      # 搜索高亮文本
    └── MarkdownRenderer.kt         # Markdown 渲染组件
```

## 关键文件

| 文件 | 目的 |
|------|------|
| `Theme.kt` | JiweiTheme Composable，支持 isSystemInDarkTheme 和手动覆盖 |
| `HomeScreen.kt` | 主页，LazyVerticalStaggeredGrid 卡片瀑布流 |
| `EntryEditScreen.kt` | 编辑器，BasicTextField + AnnotatedString 实时预览 |
| `GraphScreen.kt` | Canvas 自定义绘制力导向图谱 |
| `EntryCard.kt` | 卡片组件，展示标题、摘要、标签、收藏标识 |

## 依赖

**本模块依赖**:
- `domain/usecase/` — ViewModel 调用 UseCase
- `navigation/` — 路由导航
- `di/` — Hilt 注入
- Coil Compose（图片加载）
- Material 3（设计系统）

**依赖本模块的**:
- `MainActivity.kt` — 通过 JiweiNavGraph 间接使用

## 规范

### ViewModel

- 使用 `@HiltViewModel` 注解
- UI 状态通过 `StateFlow<UiState<T>>` 暴露
- 用户事件通过普通方法接收
- 使用 `viewModelScope.launch` 执行异步操作

### 主题使用

```kotlin
// 使用主题颜色
color = MaterialTheme.colorScheme.primary

// 使用主题排版
style = MaterialTheme.typography.titleMedium
```

### Markdown 实时预览

- 使用 `BasicTextField` + `AnnotatedString`
- 光标所在段落保持源码
- 其余段落即时渲染为富文本
- 增量解析仅更新变化的段落
