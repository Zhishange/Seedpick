# 积微 (Jiwei) 文档

积微是一款 Android 本地知识库应用的完整项目文档。本文档面向开发者，涵盖系统架构、组件接口、开发指南和核心概念。

**快速链接**: [架构](./ARCHITECTURE.md) | [接口](./INTERFACES.md) | [开发者指南](./DEVELOPER_GUIDE.md)

---

## 核心文档

### [架构](./ARCHITECTURE.md)
系统设计、技术栈、项目结构和数据流程。从这里开始了解系统如何运作。

### [接口](./INTERFACES.md)
Room Entity 定义、DAO 查询接口、ViewModel 状态管理和 Navigation 路由定义。

### [开发者指南](./DEVELOPER_GUIDE.md)
环境搭建、开发工作流、编码规范和常见任务。贡献者必读。

---

## 模块

| 模块 | 描述 | 文档 |
|------|------|------|
| `data/` | Room 数据库、Entity、DAO、Repository 实现 | [README](./模块/data.md) |
| `domain/` | 业务逻辑层，UseCase 和 Repository 接口 | [README](./模块/domain.md) |
| `ui/` | Jetpack Compose 用户界面和主题 | [README](./模块/ui.md) |
| `di/` | Hilt 依赖注入模块 | [README](./模块/di.md) |
| `navigation/` | 路由定义和导航图 | [README](./模块/navigation.md) |

---

## 核心概念

理解以下领域概念有助于导航代码库：

| 概念 | 描述 |
|------|------|
| [知识条目 (Entry)](./专有概念/Entry.md) | 系统核心实体，一条 Markdown 知识记录 |
| [标签 (Tag)](./专有概念/Tag.md) | 层级分类体系，以 `/` 分隔的嵌套标签 |
| [双向链接 (Bidirectional Link)](./专有概念/BidirectionalLink.md) | `[[]]` 语法建立的条目关联 |
| [知识图谱 (Knowledge Graph)](./专有概念/KnowledgeGraph.md) | 条目关联的可视化网络 |

---

## 入门指南

### 项目新人？

按此路径学习：
1. **[架构](./ARCHITECTURE.md)** — 了解全局
2. **[核心概念](#核心概念)** — 学习领域术语
3. **[开发者指南](./DEVELOPER_GUIDE.md)** — 搭建环境
4. **[接口](./INTERFACES.md)** — 探索内部接口

### 首次贡献？

1. **[开发者指南](./DEVELOPER_GUIDE.md)** — 搭建和工作流
2. **[添加新页面](./DEVELOPER_GUIDE.md#添加新页面)** — 低风险起步任务
3. **[常见任务](./DEVELOPER_GUIDE.md#常见任务)** — 分步指南

---

## 快速参考

### Gradle 命令

```bash
./gradlew assembleDebug    # Debug 构建
./gradlew test             # 运行测试
./gradlew lint             # 代码检查
```

### 关键文件

| 文件 | 目的 |
|------|------|
| `app/build.gradle.kts` | 模块构建配置和依赖 |
| `gradle/libs.versions.toml` | 依赖版本目录 |
| `app/src/main/AndroidManifest.xml` | 应用清单 |
| `.monkeycode/specs/jiwei-knowledge-base/requirements.md` | 功能需求规格 |
| `.monkeycode/specs/jiwei-knowledge-base/design.md` | 技术设计文档 |
| `.monkeycode/specs/jiwei-knowledge-base/tasklist.md` | 实施任务列表 |
