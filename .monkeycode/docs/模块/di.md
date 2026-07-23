# di/ — 依赖注入

依赖注入模块通过 Hilt 管理全应用范围的对象创建和生命周期。

## 结构

```
di/
├── AppModule.kt                    # 提供 Database、DataStore 等基础组件
└── RepositoryModule.kt             # 提供各 Repository 实现
```

## 关键文件

| 文件 | 目的 |
|------|------|
| `AppModule.kt` | 单例提供 JiweiDatabase、DataStore 实例 |
| `RepositoryModule.kt` | 单例提供各 Repository 接口的具体实现 |

## 依赖

**本模块依赖**:
- Hilt
- Room Database
- DataStore Preferences

**依赖本模块的**:
- 所有 `@HiltViewModel`
- `@AndroidEntryPoint` Activity 和 `@HiltAndroidApp` Application

## 规范

### AppModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): JiweiDatabase {
        return Room.databaseBuilder(context, JiweiDatabase::class.java, "jiwei.db").build()
    }

    @Provides
    fun provideEntryDao(db: JiweiDatabase): EntryDao = db.entryDao()
}
```

### RepositoryModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides @Singleton
    fun provideEntryRepository(impl: EntryRepositoryImpl): EntryRepository = impl
}
```

### 作用域

- `@Singleton` — Database、DataStore、Repository 实现
- 无注解 — DAO（每次从 Database 获取，不单独保持单例）
