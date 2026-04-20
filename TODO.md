# Hearable Music Player 待办事项

本文档仅包含可执行的任务列表，帮助我规划和跟踪个人项目的进展。

## 📋 相关文档

- [docs/README](docs/README.md) — 项目文档索引与各文档职责
- [ROADMAP](ROADMAP.md) — 功能状态与版本历史
- [设计文档](docs/5_10/ios-adaptation-design.md) — iOS 适配技术设计
- [实施计划](docs/5_10/ios-adaptation-plan.md) — v5.10 详细实施步骤

---

## v5.10 重点：iOS 平台适配与双平台架构

**现状**：Android 单平台架构（Kotlin + Compose + MVVM），需适配 iOS 并建立长期双平台维护工作流。

**技术路线**：KMP 共享核心层（domain + data），UI 和播放引擎保持平台原生，Monorepo 结构。

---

### P0: Monorepo 项目骨架 ✅

- [x] **P0.1** 调整目录结构 — 现有模块移入 `android/`，创建 `shared/` 和 `ios/` 空目录
- [x] **P0.2** 更新根构建配置 — `settings.gradle.kts`、`build.gradle.kts`、`libs.versions.toml` 新增 KMP 依赖
- [x] **P0.3** 创建 shared KMP 模块 — `build.gradle.kts`（androidTarget + iosX64/Arm64/SimulatorArm64 + CocoaPods）+ 空目录结构
- [x] **P0.4** 更新 Android 模块路径 — 所有 `project(":core-data")` 等引用改为 `project(":android:core-data")`
- [x] **P0.5** 验证 — `./gradlew :android:app:assembleDebug` 通过 (126 tasks BUILD SUCCESSFUL)

### P1: core-domain 迁移 ✅

- [x] **P1.1** 移动 core-domain 源文件（36 个 .kt 文件）到 `shared/src/commonMain/kotlin/com/hmp/domain/`
- [x] **P1.2** 移除 Android 依赖 — `GetDailyMusicRecommendationUseCase.kt` 中 `android.util.Log` 替换为 `println`；移除 `javax.inject.Inject` 注解
- [x] **P1.3** 更新 shared 模块依赖 — 确认 commonMain 依赖完整
- [x] **P1.4** 更新 Android 端引用 — core-data/feature-ui/core-player 的依赖和 import 路径
- [x] **P1.5** 验证 — `./gradlew :shared:compileAndroidMain` 通过 + `./gradlew :android:app:assembleDebug` 通过 (117 tasks BUILD SUCCESSFUL in 26s)
- [x] **P1.6** 清理空模块 — 移除 `settings.gradle.kts` 中的 `include(":android:core-domain")`，删除目录

### P2: core-data 迁移 — Room

- [ ] **P2.1** 移动 Room 数据库文件（10 个）到 `shared/src/commonMain/kotlin/com/hmp/data/database/`
- [ ] **P2.2** 改造 AppDatabase 为 KMP 模式 — `@ConstructedBy` + `expect object AppDatabaseConstructor`
- [ ] **P2.3** 迁移 Room Migration — 4 个 Migration 从 `SupportSQLiteDatabase` 改为 `SQLiteConnection`
- [ ] **P2.4** 创建平台特定 Database Builder — androidMain（Context.getDatabasePath）+ iosMain（NSFileManager）
- [ ] **P2.5** 检查 DAO 兼容性 — `@RawQuery` 改为 `RoomRawQuery`，确认 `PagingSource` KMP 兼容性
- [ ] **P2.6** 验证 — `./gradlew :shared:build` 通过

### P3: core-data 迁移 — 网络

- [ ] **P3.1** 移动 `MultiProviderApiAdapter.kt` 到 `shared/src/commonMain/kotlin/com/hmp/data/network/`
- [ ] **P3.2** 重写 MultiProviderApiAdapter — OkHttp → Ktor Client，Gson → kotlinx.serialization（DTO 添加 `@Serializable`）
- [ ] **P3.3** 创建平台特定 HttpClient — androidMain（OkHttp engine）+ iosMain（Darwin engine）
- [ ] **P3.4** 验证 — `./gradlew :shared:build` 通过

### P4: core-data 迁移 — DI / 标签 / 存储 / 工具

- [ ] **P4.1** 移动 Repository 实现（4 个）和 Mapper（2 个）到 shared
- [ ] **P4.2** 处理 Repository 平台依赖 — MusicRepositoryImpl 重构（移除 Context/Gson，注入 DeviceMusicScanner/MusicTagParser/SecureStorageHelper）；SettingsRepositoryImpl DataStore 初始化；BackupFileRepositoryImpl 序列化替换
- [ ] **P4.2a** 设备音乐扫描 expect/actual — `DeviceMusicScanner`（Android: MediaStore，iOS: FileManager + AVAsset）
- [ ] **P4.3** 音乐标签解析 expect/actual — `MusicTagParser`（Android: MediaMetadataRetriever + Jaudiotagger，iOS: AVAsset + AVAssetReader）
- [ ] **P4.4** 安全存储 expect/actual — `SecureStorageHelper`（Android: KeyStore + AES-GCM，iOS: Keychain）
- [ ] **P4.5** 拼音排序 expect/actual — `stringToPinyinSortKey()`（Android: Pinyin4j，iOS: CFStringTransform）
- [ ] **P4.6** DataStore KMP 配置 — `createDataStore()` expect/actual（Android: Context.filesDir，iOS: NSDocumentDirectory）
- [ ] **P4.7** 配置 Koin DI 模块 — sharedModule（Database/Repositories/UseCases）+ androidPlatformModule（Context）
- [ ] **P4.8** 移动测试文件到 `shared/src/commonTest/`
- [ ] **P4.9** 验证 — `./gradlew :shared:build` + `./gradlew :shared:allTests` 通过

### P5: Android 端适配

- [ ] **P5.1** 更新 Android 模块依赖 — 删除 `android/core-data` 和 `android/core-domain` 模块，所有消费者依赖 `:shared`
- [ ] **P5.1b** 更新 core-player 依赖 — 改为依赖 `:shared`，将 `MusicController` 切换为 Koin 管理
- [ ] **P5.2** 更新 feature-ui 依赖 — 改为依赖 `:shared`，添加 Koin Compose 依赖，移除 Hilt
- [ ] **P5.3** 更新 ViewModel 注入方式 — 14 个 ViewModel 从 `@HiltViewModel` 切换为 `koinViewModel()`
- [ ] **P5.4** 更新 Application 类 — `@HiltAndroidApp` → `startKoin { modules(sharedModule, androidPlatformModule) }`
- [ ] **P5.5** 更新包名引用 — `com.example.hearablemusicplayer.domain/data` → `com.hmp.domain/data`
- [ ] **P5.6** 移除 feature-ui 中的 Gson 依赖
- [ ] **P5.7** 移除 feature-ui 中的 Pinyin4j 依赖
- [ ] **P5.8** 验证 — `assembleDebug` + `assembleRelease` 通过 + 手动功能回归测试（扫描/播放/列表/AI/搜索/歌词/设置/统计/备份/分享）

### P6: iOS 端基础

- [ ] **P6.1** 创建 Xcode 项目 — SwiftUI + iOS 16.0
- [ ] **P6.2** 集成 KMP shared 框架 — CocoaPods（Podfile + pod install）
- [ ] **P6.3** Koin iOS 初始化 — AppDelegate 中 `KoinKt.doInitKoin()`
- [ ] **P6.4** AVPlayer 封装 — PlayerService（play/pause/seek/next/previous）+ AudioSessionManager + NowPlayingManager
- [ ] **P6.5** 基础 SwiftUI 界面 — HMPApp + MainTabView（音乐库/播放/列表/设置）
- [ ] **P6.6** 音乐扫描（iOS 端）— FileManager 扫描 + shared Repository 存储
- [ ] **P6.7** 验证 — Xcode 编译通过 + 模拟器可运行 + 能播放音乐 + 锁屏控制可用

### P7: iOS 端功能

- [ ] **P7.1** 音乐库模块 — LibraryView / GalleryView / SearchView / ArtistView / AlbumView / SongDetailView
- [ ] **P7.2** 播放器模块 — NowPlayingView / LyricsView / QueueView
- [ ] **P7.3** 播放列表模块 — PlaylistListView / PlaylistDetailView
- [ ] **P7.4** 设置模块 — SettingsView / AISettingsView / AudioEffectView / UserView
- [ ] **P7.5** 通用组件 — 设计系统 / 主题管理 / 空状态加载状态 / 对话框
- [ ] **P7.6** 验证 — 逐模块功能对比测试（音乐库/播放器/播放列表/设置/通用）

---

© 2026 Hearable Music Player | Developed by WLYB
