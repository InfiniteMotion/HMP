# Hearable Music Player 待办事项

本文档仅包含可执行的任务列表，帮助我规划和跟踪个人项目的进展。

## 📋 相关文档

- [docs/README](docs/README.md) — 项目文档索引与各文档职责
- [ROADMAP](ROADMAP.md) — 功能状态与版本历史
- [设计文档](docs/5_10/ios-adaptation-design.md) — iOS 适配技术设计
- [实施计划](docs/5_10/ios-adaptation-plan.md) — v5.10 详细实施步骤
- [UI 差异对照](docs/5_10/ios-android-ui-diff.md) — iOS vs Android UI 原生优势与简化策略

---

## v5.10 重点：iOS 平台适配与双平台架构

**现状**：Android 端已完成 KMP 迁移（Hilt → Koin，domain + data 层移入 shared），iOS 端 SwiftUI 界面已大规模实现（设计系统 ✅、通用组件部分 ✅、部分页面 ✅），待完成 P6 编译修复和 ViewModel 集成。

**技术路线**：KMP 共享核心层（domain + data），UI 和播放引擎保持平台原生，Monorepo 结构。

**实际进度**：
- P0-P5: ✅ 全部完成
- P6: 🔶 阶段一/二 ✅，阶段三待完成
- P7: 🔶 设计系统 ✅，部分页面完整实现，部分占位待完善

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

### P2: core-data 迁移 — Room ✅

- [x] **P2.1** 移动 Room 数据库文件（10 个）到 `shared/src/commonMain/kotlin/com/hmp/data/database/`
- [x] **P2.2** 改造 AppDatabase 为 KMP 模式 — `@ConstructedBy` + `expect object AppDatabaseConstructor`
- [x] **P2.3** 迁移 Room Migration — 简化迁移逻辑，使用 `fallbackToDestructiveMigration()`
- [x] **P2.4** 创建平台特定 Database Builder — androidMain（Context.getDatabasePath）+ iosMain（NSFileManager）
- [x] **P2.5** 检查 DAO 兼容性 — 移除 `PagingSource` 相关查询（KMP commonMain 不可用）
- [x] **P2.6** 验证 — `./gradlew :shared:compileAndroidMain` 通过（iOS 编译需要 macOS + Xcode 环境）

### P3: core-data 迁移 — 网络 ✅

- [x] **P3.1** 移动 `MultiProviderApiAdapter.kt` 到 `shared/src/commonMain/kotlin/com/hmp/data/network/`
- [x] **P3.2** 重写 MultiProviderApiAdapter — OkHttp → Ktor Client，Gson → kotlinx.serialization（DTO 添加 `@Serializable`）
- [x] **P3.3** 创建平台特定 HttpClient — androidMain（OkHttp engine）+ iosMain（Darwin engine）
- [x] **P3.4** 验证 — `./gradlew :shared:compileAndroidMain` 通过（iOS 编译需要 macOS + Xcode 环境）

### P4: core-data 迁移 — DI / 标签 / 存储 / 工具 ✅

- [x] **P4.1** 目录结构创建完成
- [x] **P4.2a** 设备音乐扫描 expect/actual — `DeviceMusicScanner` ✅
- [x] **P4.3** 音乐标签解析 expect/actual — `MusicTagParser` ✅
- [x] **P4.4** 安全存储 expect/actual — `SecureStorageHelper` ✅
- [x] **P4.5** 拼音排序 expect/actual — `stringToPinyinSortKey()` ✅
- [x] **P4.6** DataStore KMP 配置 — `DataStoreFactory` expect/actual ✅
- [x] **P4.1b** 移动 Repository 实现（4 个）和 Mapper（2 个）到 shared ✅
- [x] **P4.7** 配置 Koin DI 模块 ✅
- [x] **P4.8** 移动测试文件到 `shared/src/commonTest/` ✅（4 个测试文件，47 个用例）
- [x] **P4.9** 验证 — `./gradlew :shared:compileAndroidMain` 通过 ✅

### P5: Android 端适配 ✅

- [x] **P5.1** 更新 Android 模块依赖 — 删除 `android/core-data` 和 `android/core-domain` 模块，所有消费者依赖 `:shared`
- [x] **P5.1b** 更新 core-player 依赖 — 改为依赖 `:shared`，将 `MusicController` 切换为 Koin 管理
- [x] **P5.2** 更新 feature-ui 依赖 — 改为依赖 `:shared`，添加 Koin Compose 依赖，移除 Hilt
- [x] **P5.3** 更新 ViewModel 注入方式 — 14 个 ViewModel 从 `@HiltViewModel` 切换为 `koinViewModel()`
- [x] **P5.4** 更新 Application 类 — `@HiltAndroidApp` → `startKoin { modules(sharedModule, androidPlatformModule) }`
- [x] **P5.5** 更新包名引用 — `com.example.hearablemusicplayer.domain/data` → `com.hmp.domain/data`
- [x] **P5.6** 移除 feature-ui 中的 Gson 依赖
- [x] **P5.7** 移除 feature-ui 中的 Pinyin4j 依赖
- [x] **P5.8** 验证 — `assembleDebug` + `assembleRelease` 通过 ✅

### P6: iOS 端基础 🔶

> **当前状态**：Kotlin iOS 编译 ✅，P6.6 ✅，P6.8 ✅（Koin 已启用），P6.9 ✅（Xcode 编译通过 + 模拟器运行），阶段三待完成。

#### 阶段一：Kotlin iOS 编译 ✅

- [x] **P6.1** SecureStorageHelper.ios.kt — 存根实现（encrypt/decrypt 返回原文），编译通过，功能待完善 → 见 P6.10
- [x] **P6.2** BackupFileRepositoryImpl.ios.kt — 存根实现（saveBackup 返回路径，loadBackup 抛异常），编译通过，功能待完善 → 见 P6.13
- [x] **P6.3** DeviceMusicScanner.ios.kt — 存根实现（isDirectoryAtPath 返回 false，元数据硬编码），编译通过，功能待完善 → 见 P6.11
- [x] **P6.4** SettingsRepositoryImpl.ios.kt — ✅ 已修复，`currentTimeMillis()` 正确使用
- [x] **P6.5** 验证 — `./gradlew :shared:compileKotlinIosSimulatorArm64` 编译通过（需 macOS 环境）✅

#### 阶段二：KMP 框架集成与 DI 打通 ✅

- [x] **P6.6** Podfile 构建脚本 — `post_install` 中 `[CP-User] Build shared` 阶段调用 Gradle 编译 `shared.framework`，`FRAMEWORK_SEARCH_PATHS` 正确指向 `shared/build/cocoapods/framework` ✅
- [x] **P6.8** 接入 Swift 层 Koin 初始化 — `AppDelegate.swift` 中 `KoinInitializer()` 已启用，`import shared` 已添加 ✅
- [x] **P6.9** 验证 — Xcode 编译通过 + 模拟器可运行 + Koin 初始化成功 ✅

#### 阶段三：iOS 核心功能实现

- [ ] **P6.10** 完善 SecureStorageHelper + DeviceMusicScanner — 实现 Keychain 加密存储 + 完整元数据提取（title/artist/album/bitRate/sampleRate/fileSize）
- [ ] **P6.11** 完善 MusicTagParser.ios.kt — 实现 `parseMetadata()` 的 title/artist/album/bitRate/sampleRate 提取；歌词解析（ID3 或 LRC）
- [ ] **P6.12** 实现 PinyinSortKey.ios.kt — 使用 `CFStringTransform`（`kCFStringTransformToLatin` + `kCFStringTransformStripDiacritics`）将中文转拼音排序键（当前：直接返回原字符串）
- [ ] **P6.13** 实现 MusicRepositoryImpl — 将 Android 版本通用逻辑提取到 commonMain 共享基类，iOS 仅覆盖 `loadMusicFromDevice()`（调用 `DeviceMusicScanner.scanMusic()`），DAO/AI 调用复用 commonMain（当前：所有方法返回空值）
- [ ] **P6.14** 实现 PlaylistRepositoryImpl — 同上策略，DAO 操作跨平台复用，iOS 仅提供平台特定差异（当前：所有方法返回空值）
- [ ] **P6.15** 实现 BackupFileRepositoryImpl — 基于 `NSFileManager` 实现备份文件读写，补全 SettingsRepository 的 `backupSettings()`/`restoreSettings()`（当前：saveBackup 仅返回路径，loadBackup 抛异常）
- [ ] **P6.16** AVPlayer 封装 — `PlayerService.swift`（play/pause/seek/next/previous/播放列表管理）+ `AudioSessionManager.swift`（音频焦点/中断处理）+ `NowPlayingManager.swift`（锁屏控制/远程命令）
- [ ] **P6.17** 基础 SwiftUI 界面 — `MainTabView`（音乐库/播放/列表/设置 4 个 Tab）+ `LibraryView`（歌曲列表）+ `NowPlayingMiniView`（迷你播放栏）+ `SettingsView`（基础设置项）
- [ ] **P6.18** 验证 — Xcode 编译通过 + 模拟器可运行 + 能扫描并播放音乐 + 锁屏控制可用

### P7: iOS SwiftUI 界面迁移

> **对照 Android feature-ui 模块逐一映射**。Android 端使用 Navigation3 + HorizontalPager + Material3 + Haze 毛玻璃；iOS 端使用 NavigationStack + TabView(.page) + SwiftUI 原生 + Liquid Glass（iOS 26+ 液态玻璃，回退 .regularMaterial）。
>
> **迁移顺序**：设计系统基础 → 通用组件 → 导航框架 → 各模块页面 → 验证
>
> **实际进度**：设计系统 (P7-A) ✅、通用组件 (P7-B) 🔶、导航框架 (P7-C) ✅、部分页面完整/部分占位 🔶

#### P7-A: 设计系统与基础 ✅

- [x] **P7.1** ColorTokens.swift — 品牌色 `HDBlue(#002FA7)` / `HDRed(#C92C2C)` + 浅色/深色主题完整色板，对应 Android `ColorTokens.kt`
- [x] **P7.2** TypographyTokens.swift — HarmonyOS Sans 字体族（6 字重）+ Material3 Typography 完整定义（displayLarge 40pt ~ labelSmall 11pt），sp→pt 1:1 映射，对应 Android `TypographyTokens.kt`
- [x] **P7.3** AnimationTokens.swift — 持续时间（MICRO 200ms / TRANSITION 400ms / COMPLEX 650ms / BACKGROUND 3000ms）+ 缓动函数（EASE_IN_OUT / EASE_OUT / EASE_IN 对应 UnitCurve）+ Spring 配置，对应 Android `AnimationTokens.kt`
- [x] **P7.4** HMPTheme.swift — 主题入口，`@Environment` 注入 ColorTokens + TypographyTokens，支持浅色/深色/动态取色切换，对应 Android `HearableMusicPlayerTheme.kt` + `DesignSystem.kt`

#### P7-B: 通用组件 🔶

- [x] **P7.5** UiState 泛型体系 — `enum UiState<T>` (idle/loading/success/error/empty) + `@ViewBuilder` 条件视图（iOS 无需独立 UiStateContent 组件，SwiftUI 天然支持），对应 Android `UiState.kt` + `UiStateContent.kt`
- [x] **P7.6** 状态占位组件 — SwiftUI 原生 `ProgressView` / `ContentUnavailableView` 支持，无需独立实现
- [x] **P7.7** SegmentedControl — **改用原生 `SegmentedControl` / `Picker(.segmented)`**，自带滑块动画+无障碍+动态类型，无需手写
- [ ] **P7.8** TabPageIndicator.swift — 顶部胶囊圆点页面指示器（带颜色动画），对应 Android `TabPageIndicator.kt`
- [x] **P7.9** 弹窗基础 — `MusicDetailDialog` / `CreatePlaylistDialog` / `TimerDialog` 已实现；`ConfirmDialog`/`InputDialog` 改用原生 `.alert()`
- [x] **P7.10** 业务弹窗 — `MusicDetailDialog` / `CreatePlaylistDialog` / `TimerDialog` ✅，`HMPDialogs` 汇总
- [x] **P7.11** 页面模板 — `TabScreen` + `SubScreen` 已实现；`DynamicBackground` 待实现（FluidBackgroundView 占位）
- [x] **P7.12** 小组件 — `Avatar` / `Capsule` / `TitleWidget` / `AlbumCover` / `MiniPlayerBar` 全部实现
- [x] **P7.13** 触觉反馈 — `HapticManager` (lightClick/click/confirm/longPress/dragStart/contextClick)，对应 Android `HapticFeedback.kt`

#### P7-C: 导航框架 🔶

- [x] **P7.14** Route 枚举 — `HMPRoute: Hashable` + `TabItem: CaseIterable`，包含所有路由 + 底部 Tab
- [ ] **P7.15** HMPApp.swift 改造 — `AppDelegate` 已创建，`@UIApplicationDelegateAdaptor` 已添加，`KoinKt.doInitKoin()` 待启用（需 P6 编译通过）
- [x] **P7.16** MainTabView — `TabView(.page)` + `MiniPlayerBar` overlay，含 `LibraryView`/`PlayerView`/`PlaylistView`/`SettingsView` 占位

#### P7-D: 音乐库模块（对照 Android `ui/library/`）🔶

- [x] **P7.17** MusicList 组件族 — `MusicList` / `MusicRow` / `DailyHeroCard` 已实现（内置 HomeScreen），`MusicListIndexStrip` 待实现
- [x] **P7.18** HomeScreen — 每日推荐卡片 + 心动歌单入口，集成 `TabScreen` + `TitleWidget` + `MusicList`
- [x] **P7.19** GalleryScreen — 画廊/浏览，完整网格布局 + 空状态，占位专辑/歌手数据待接入 ViewModel
- [ ] **P7.20** ListScreen — 音乐列表页，Tab 分段选择器已实现，5 个内容子视图（歌曲/歌手/专辑/文件夹/标签）均为"待实现"占位
- [x] **P7.21** SearchScreen — **改用原生 `.searchable(text:)` modifier**，自带搜索栏动画+取消按钮+结果切换
- [ ] **P7.22** SongDetailScreen — 歌曲详情页（参数：musicId），标签/播放历史/操作，对应 Android `SongDetailScreen.kt`
- [ ] **P7.23** ArtistScreen — 艺术家页（参数：name），对应 Android `ArtistScreen.kt`
- [ ] **P7.24** AlbumScreen — 专辑页（参数：name），对应 Android `AlbumScreen.kt`
- [x] **P7.25** CustomScreen — 自定义主题/界面配置页
- [ ] **P7.26** LibraryViewModel — `allMusic` / `orderBy` / `scanState` / `hiddenFolders` 状态管理，对应 Android `LibraryViewModel.kt`
- [ ] **P7.27** SearchViewModel + SongDetailViewModel — 搜索状态 + 歌曲详情+标签+播放历史，对应 Android `SearchViewModel.kt` + `SongDetailViewModel.kt`

#### P7-E: 播放器模块（对照 Android `ui/player/`）🔶

- [x] **P7.28** PlayerScreen — 播放主界面，专辑封面 + 进度条 + 播放控制，`FluidBackgroundView` 占位（待实现 CoreImage 取色）
- [x] **P7.29** LyricsScreen — 歌词显示页，支持 `.sheet` + `.presentationDetents`
- [ ] **P7.30** PlaylistArea + TechnicalInfoCard — 播放队列区域 + 技术信息卡片(比特率/采样率/格式)，对应 Android `PlaylistArea.kt` + `TechnicalInfoCard.kt`
- [x] **P7.31** MiniPlayerBar — 全局悬浮迷你播放器（播放/暂停/进度/点击展开），`.ultraThinMaterial` 毛玻璃
- [ ] **P7.32** PlaybackViewModel — `isPlaying` / `currentPosition` / `duration` / `playbackMode` / `currentPlayingMusic` / `timerRemaining`，对应 Android `PlaybackViewModel.kt`
- [ ] **P7.33** PlaylistQueueViewModel — `currentPlaylist` / `likeStatus` / `currentMusicLabels` / `currentMusicLyrics` / 心动模式 / 智能列表生成，对应 Android `PlaylistQueueViewModel.kt`

#### P7-F: 播放列表模块（对照 Android `ui/playlist/`）🔶

- [x] **P7.34** PlaylistScreen — 播放列表详情页（参数：name 或 playlistId），歌曲列表 + 排序 + 编辑模式
- [x] **P7.35** PlaylistManageScreen — 用户歌单管理页，创建/重命名/删除/排序/置顶
- [ ] **P7.36** PlaylistViewModel — `genrePlaylistName` / `moodPlaylistName` / `userCustomPlaylistsState` / 歌单 CRUD + 歌曲增删排序，对应 Android `PlaylistViewModel.kt`

#### P7-G: 设置模块（对照 Android `ui/settings/`）🔶

- [ ] **P7.37** SettingScreen — **改用原生 `Form` / `List(.insetGrouped)` 分组样式**，主题/背景/Haze/AI/音效/备份/关于；UserScreen.swift 已建框架，子页面均为占位"待实现"
- [ ] **P7.38** ProfileSettingsScreen — 个人资料设置（用户名/头像），对应 Android `ProfileSettingsScreen.kt`，当前：占位"待实现"
- [x] **P7.39** UserScreen — 用户页（Tab 内），每日推荐 + 使用统计 + AI 配置入口
- [ ] **P7.40** AIScreen — AI 配置页，Provider 切换 + API Key + 模型选择 + 连接测试 + 批量处理，当前：占位"待实现"
- [x] **P7.41** AudioEffectsScreen — 音效调节页，均衡器 + 低音增强 + 环绕声 + 混响
- [ ] **P7.42** BackupSettingsScreen — 备份/还原，导出 + 导入 + 本地备份列表，对应 Android `BackupSettingsScreen.kt`，当前：占位"待实现"
- [ ] **P7.43** LibrarySettingsScreen — 音乐库设置，扫描 + 隐藏文件夹，对应 Android `LibrarySettingsScreen.kt`，当前：占位"待实现"
- [ ] **P7.44** UserUsageDataScreen — 使用数据统计页，播放次数/收听时长/标签分布/图表，对应 Android `UserUsageDataScreen.kt`，当前：占位"待实现"
- [ ] **P7.45** SettingsViewModel + RecommendationViewModel + AudioEffectViewModel + UserUsageDataViewModel — 设置/AI推荐/音效/使用数据 4 个 ViewModel

#### P7-H: 验证

- [ ] **P7.46** 音乐库模块验证 — 扫描→列表→搜索→详情→艺术家→专辑，功能对比 Android
- [ ] **P7.47** 播放器模块验证 — 播放→暂停→上下曲→进度→歌词→队列→心动模式
- [ ] **P7.48** 播放列表模块验证 — 创建→编辑→排序→添加歌曲→删除→歌单管理
- [ ] **P7.49** 设置模块验证 — 主题切换→AI 配置→音效→备份还原→使用数据

### 技术债务清理

- [x] **T1** 清理 Hilt 残留 — 删除 `libs.versions.toml` 中的 Hilt 版本和库定义（6 处）
- [x] **T2** 清理过时注释 — 更新 `MusicPlayService.kt` 和 `NavigationGraph.kt` 中的 Hilt 相关注释
- [ ] **T3** 评估 Repository 架构重构 — 将 `MusicRepositoryImpl`/`PlaylistRepositoryImpl` 的通用业务逻辑从平台 actual 提取到 commonMain 共享基类，减少重复实现（关联 P6.13/P6.14）

---

© 2026 Hearable Music Player | Developed by WLYB
