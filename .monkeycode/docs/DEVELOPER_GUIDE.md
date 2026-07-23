# 积微 (Jiwei) — 开发者指南

## 项目目的

积微是一款 Android 本地知识库应用。核心职责：
- 提供 Markdown 知识条目的创建、编辑、搜索和浏览
- 通过层级标签和双向链接构建知识网络
- 以知识图谱可视化展示知识关联
- 支持数据导出备份为标准格式

## 环境搭建

### 前置条件

- Android Studio Hedgehog (2023.1.1) 或更新版本
- JDK 17
- Android SDK (API 34)
- Gradle 8.7

### 安装

```bash
git clone [repo-url]
cd [repo-name]
```

用 Android Studio 打开项目根目录，等待 Gradle 同步完成。

### 运行

在 Android Studio 中：
1. 选择运行配置为 `app`
2. 选择目标设备（模拟器或真机，minSdk 26）
3. 点击 Run 按钮

或使用命令行：

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 开发工作流

### 代码质量

| 工具 | 说明 |
|------|------|
| Kotlin | 官方代码风格 (`.editorconfig`) |
| KSP | Room 和 Hilt 的注解处理器 |
| Compose | 声明式 UI，使用 Compose 预览 |

### 架构模式

项目采用 MVVM + Clean Architecture 三层架构：

```
UI (Compose) → ViewModel (StateFlow) → UseCase → Repository → Room DAO
```

- **UI 层**：仅负责界面渲染和用户交互，通过 `collectAsState()` 观察 ViewModel 的 StateFlow
- **ViewModel 层**：管理 UI 状态，处理用户事件，调用 UseCase
- **领域层**：纯 Kotlin 业务逻辑，不依赖 Android 框架
- **数据层**：封装数据源（Room、文件系统），返回 Flow 或挂起函数

### 提交前检查

1. 确保代码可编译：`./gradlew assembleDebug`
2. 运行测试：`./gradlew test`
3. 确认无新增 lint 警告

## 常见任务

### 添加新页面

**需修改的文件**:
1. `navigation/Screen.kt` — 添加路由定义
2. `navigation/JiweiNavGraph.kt` — 在 NavHost 中添加 composable
3. `ui/[screen]/` — 创建 Screen 和 ViewModel

**步骤**:
1. 在 `Screen` 密封类中添加新路由
2. 创建 `[Name]ViewModel`（使用 `@HiltViewModel` 注解）
3. 创建 `[Name]Screen` Composable 函数
4. 在 `JiweiNavGraph` 中注册路由

### 添加新数据库表

**需修改的文件**:
1. `data/local/entity/` — 新建 Entity 类
2. `data/local/dao/` — 新建 DAO 接口
3. `data/local/db/JiweiDatabase.kt` — 注册 Entity 和 DAO
4. 增加数据库版本号，编写 Migration

**步骤**:
1. 创建 Entity 类，添加 Room 注解
2. 创建对应的 DAO 接口
3. 在 `JiweiDatabase` 的 `entities` 列表中添加新 Entity
4. 在 `AppModule` 中提供新的 DAO（如果需要）

### 修复 Bug

**流程**:
1. 定位相关代码位置（通过需求文档和设计文档）
2. 编写复现 bug 的测试
3. 最小改动修复
4. 验证测试通过
5. 检查相关代码是否受影响

## 编码规范

### 文件组织

- 按架构层分包：`data/`、`domain/`、`ui/`、`di/`、`navigation/`
- 每个功能模块在 `ui/` 下有独立子包
- ViewModel 与 Screen 放在同一包内

### 命名

| 类型 | 约定 | 示例 |
|------|------|------|
| 包名 | lowercase | `com.jiwei.app.data.local.entity` |
| 类/接口 | PascalCase | `EntryEntity`, `EntryDao` |
| 函数/属性 | camelCase | `getById`, `isPinned` |
| 常量 | UPPER_SNAKE | `MAX_TITLE_LENGTH` |
| Composable | PascalCase | `HomeScreen`, `EntryCard` |

### 依赖注入

- 所有 `@HiltViewModel` 的构造函数使用 `@Inject` 注解
- Repository 通过 Hilt Module 以 `@Singleton` 作用域提供
- DAO 从 `JiweiDatabase` 获取，不直接注入

### 异步处理

- Repository 的查询方法返回 `Flow` 或 `suspend` 函数
- ViewModel 中使用 `viewModelScope.launch` 执行挂起操作
- UI 层使用 `collectAsStateWithLifecycle()` 收集 Flow

### 错误处理

- 数据层异常在 Repository 中捕获并转换为领域异常
- ViewModel 通过 `UiState.Error(message)` 向 UI 层传递错误
- UI 层使用 Snackbar 展示错误信息

### 主题

- 始终使用 `MaterialTheme.colorScheme` 中的颜色，不硬编码颜色值
- 文本样式使用 `MaterialTheme.typography`
- 深色模式通过 `JiweiTheme` 自动适配
