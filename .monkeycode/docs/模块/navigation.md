# navigation/ — 导航

导航模块定义应用内所有路由和页面跳转逻辑。

## 结构

```
navigation/
├── Screen.kt                       # 路由密封类定义
└── JiweiNavGraph.kt                # NavHost 导航图
```

## 关键文件

| 文件 | 目的 |
|------|------|
| `Screen.kt` | 定义所有路由名称和参数 |
| `JiweiNavGraph.kt` | NavHost 组装，路由到 Composable 的映射 |

## 路由定义

```kotlin
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Tags : Screen("tags")
    data object Graph : Screen("graph")
    data object Settings : Screen("settings")
    data object EntryDetail : Screen("entry/{entryId}") {
        fun createRoute(entryId: String) = "entry/$entryId"
    }
    data object EntryEdit : Screen("entry/edit/{entryId}") {
        fun createRoute(entryId: String) = "entry/edit/$entryId"
    }
}
```

## 依赖

**本模块依赖**:
- Navigation Compose
- `ui/` — 各 Screen Composable

**依赖本模块的**:
- `MainActivity.kt` — 创建 NavController 并挂载 JiweiNavGraph

## 规范

- 使用类型安全的 `sealed class` 定义路由
- 带参数的路由提供 `createRoute()` 工厂方法
- 新页面添加到 `Screen` 密封类和 `JiweiNavGraph` 的 `NavHost` 中
