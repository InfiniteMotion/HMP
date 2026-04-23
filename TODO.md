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

**现状**：Android 端已完成 KMP 迁移（Hilt → Koin，domain + data 层移入 shared），iOS 端基础设施部分就绪（8 个 expect/actual 平台实现中 6 个完整），待完成 iOS 端功能开发。

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

### P6: iOS 端基础

> **当前状态**：Xcode 项目骨架已创建，iosMain 存在编译错误和空桩实现，iOS 原生 Swift 层几乎为零。需分三阶段推进：修复编译 → 核心功能 → 验证。

#### 阶段一：修复 iosMain 编译错误

- [ ] **P6.1** 修复 SecureStorageHelper.ios.kt — 移除 `android.util.Base64` 和不存在的 `SecKeyCreateEncryptedData`，改用 Keychain Services（`SecItemAdd`/`SecItemCopyMatching`/`SecItemDelete`）存储 API Key
- [ ] **P6.2** 修复 BackupFileRepositoryImpl.ios.kt — 移除 `java.io.File`/`java.io.IOException`，改用 `NSFileManager` + 路径字符串；若 commonMain 接口使用 `File` 类型则需同步抽象为 `String`
- [ ] **P6.3** 修复 DeviceMusicScanner.ios.kt — 修正 `fileExistsAtPath` 的 `ObjCBool` 指针参数（`memScoped` + `alloc<ObjCBool>()`）；移除末尾 `AVFoundation` 变量，直接使用 `platform.AVFoundation.AVURLAsset`
- [ ] **P6.4** 修复 SettingsRepositoryImpl.ios.kt — 第 168 行 `System.currentTimeMillis()` 替换为 `currentTimeMillis()`（commonMain expect/actual）
- [ ] **P6.5** 验证 — `./gradlew :shared:compileKotlinIosSimulatorArm64` 编译通过（需 macOS 环境）

#### 阶段二：KMP 框架集成与 DI 打通

- [ ] **P6.6** 修复 Podfile 构建脚本 — 排查 `post_install` 中 lock file 权限问题，恢复 Gradle 构建脚本执行；确认 `pod install` 后 `shared.framework` 正确生成
- [ ] **P6.7** 修正 Xcode 部署目标 — pbxproj 中 `IPHONEOS_DEPLOYMENT_TARGET` 从 26.4 改为 16.0，与 Podfile 一致
- [ ] **P6.8** 接入 Swift 层 Koin 初始化 — 创建 `AppDelegate.swift`（`@UIApplicationDelegateAdaptor`），调用 `KoinKt.doInitKoin()`；修改 `HMPApp.swift` 引用 AppDelegate
- [ ] **P6.9** 验证 — Xcode 编译通过 + Koin 初始化成功（`startKoin { modules(sharedModule, iosPlatformModule) }` 无报错）

#### 阶段三：iOS 核心功能实现

- [ ] **P6.10** 完善 DeviceMusicScanner — 从 `AVAsset.commonMetadata` 提取 title/artist/album（当前硬编码 "Unknown"）；通过 `AVAssetTrack` 提取 bitRate/sampleRate；通过 `NSFileManager.attributesOfItemAtPath` 获取 fileSize
- [ ] **P6.11** 完善 MusicTagParser.ios.kt — 实现 `parseMetadata()` 的 title/artist/album/bitRate/sampleRate 提取；评估歌词解析方案（ID3 标签 `AVMetadataID3MetadataKeyUnsynchronisedLyric` 或 LRC 外挂文件）
- [ ] **P6.12** 实现 PinyinSortKey.ios.kt — 使用 `CFStringTransform`（`kCFStringTransformToLatin` + `kCFStringTransformStripDiacritics`）将中文转拼音排序键
- [ ] **P6.13** 实现 MusicRepositoryImpl — 将 Android 版本通用逻辑提取到 commonMain 共享基类，iOS 仅覆盖 `loadMusicFromDevice()`（调用 `DeviceMusicScanner.scanMusic()`），DAO/AI 调用复用 commonMain
- [ ] **P6.14** 实现 PlaylistRepositoryImpl — 同上策略，DAO 操作跨平台复用，iOS 仅提供平台特定差异
- [ ] **P6.15** 实现 BackupFileRepositoryImpl — 基于 `NSFileManager` 实现备份文件读写，补全 SettingsRepository 的 `backupSettings()`/`restoreSettings()`
- [ ] **P6.16** AVPlayer 封装 — `PlayerService.swift`（play/pause/seek/next/previous/播放列表管理）+ `AudioSessionManager.swift`（音频焦点/中断处理）+ `NowPlayingManager.swift`（锁屏控制/远程命令）
- [ ] **P6.17** 基础 SwiftUI 界面 — `MainTabView`（音乐库/播放/列表/设置 4 个 Tab）+ `LibraryView`（歌曲列表）+ `NowPlayingMiniView`（迷你播放栏）+ `SettingsView`（基础设置项）
- [ ] **P6.18** 验证 — Xcode 编译通过 + 模拟器可运行 + 能扫描并播放音乐 + 锁屏控制可用

### P7: iOS SwiftUI 界面迁移

> **对照 Android feature-ui 模块逐一映射**。Android 端使用 Navigation3 + HorizontalPager + Material3 + Haze 毛玻璃；iOS 端使用 NavigationStack + TabView(.page) + SwiftUI 原生 + Liquid Glass（iOS 26+ 液态玻璃，回退 .regularMaterial）。
>
> **迁移顺序**：设计系统基础 → 通用组件 → 导航框架 → 各模块页面 → 验证

#### P7-A: 设计系统与基础

- [ ] **P7.1** ColorTokens.swift — 品牌色 `HDBlue(#002FA7)` / `HDRed(#C92C2C)` + 浅色/深色主题完整色板，对应 Android `ColorTokens.kt`
- [ ] **P7.2** TypographyTokens.swift — HarmonyOS Sans 字体族（6 字重）+ Material3 Typography 完整定义（displayLarge 40pt ~ labelSmall 11pt），sp→pt 1:1 映射，对应 Android `TypographyTokens.kt`
- [ ] **P7.3** AnimationTokens.swift — 持续时间（MICRO 200ms / TRANSITION 400ms / COMPLEX 650ms / BACKGROUND 3000ms）+ 缓动函数（EASE_IN_OUT / EASE_OUT / EASE_IN 对应 UnitCurve）+ Spring 配置，对应 Android `AnimationTokens.kt`
- [ ] **P7.4** HMPTheme.swift — 主题入口，`@Environment` 注入 ColorTokens + TypographyTokens，支持浅色/深色/动态取色切换，对应 Android `HearableMusicPlayerTheme.kt` + `DesignSystem.kt`

#### P7-B: 通用组件

- [ ] **P7.5** UiState 泛型体系 — `enum UiState<T>` (idle/loading/success/error/empty) + `@ViewBuilder` 条件视图（iOS 无需独立 UiStateContent 组件，SwiftUI 天然支持），对应 Android `UiState.kt` + `UiStateContent.kt`
- [ ] **P7.6** 状态占位组件 — `DefaultLoadingView` (原生 `ProgressView` + 文字) / `DefaultErrorView` (图标+文字+重试按钮) / `DefaultEmptyView` (图标+文字)，对应 Android `DefaultLoading/DefaultError/DefaultEmpty.kt`
- [ ] **P7.7** SegmentedControl — **改用原生 `SegmentedControl` / `Picker(.segmented)`**，自带滑块动画+无障碍+动态类型，无需手写（Android 需~170行自定义 Surface+Row），对应 Android `SegmentedControl.kt`
- [ ] **P7.8** TabPageIndicator.swift — 顶部胶囊圆点页面指示器（带颜色动画），对应 Android `TabPageIndicator.kt`
- [ ] **P7.9** 弹窗基础 — `ConfirmDialog`（原生 `.alert()`） / `InputDialog`（`.alert` + TextField） / `ScrimDialog` (全屏遮罩 `.fullScreenCover`) / `MessageToast` (滑入滑出+Liquid Glass)，对应 Android `dialogs/base/` 下 4 个文件
- [ ] **P7.10** 业务弹窗 — `MusicDetailDialog` / `CreatePlaylistDialog` / `MusicPickerDialog` / `PlaylistPickerDialog` / `MusicScanDialog` / `TimerDialog` / `AddSongToPlaylistDialog`，对应 Android `dialogs/` 下 7 个文件
- [ ] **P7.11** 页面模板 — `TabScreen` (Tab 页通用：标题+搜索+内容) / `SubScreen` (子页面通用：`toolbar` + `NavigationStack` 内建返回) / `DynamicBackground` (专辑封面取色动态背景，iOS 需 CoreImage 自实现 Palette)，对应 Android `pages/base/` 下 3 个文件
- [ ] **P7.12** 小组件 — `Avatar` (圆形头像+AsyncImage) / `Capsule` (胶囊标签) / `TitleWidget` (渐变竖线标题卡片) / `AlbumCover` (专辑封面)，对应 Android `Avatar/Capsule/TitleWidget/AlbumCover.kt`
- [ ] **P7.13** 触觉反馈 — `HapticManager` (lightClick/click/confirm 映射 UIImpactFeedbackGenerator)，对应 Android `HapticFeedback.kt`

#### P7-C: 导航框架

- [ ] **P7.14** Route 枚举 — 对照 Android `Routes.kt`，定义 `HMPRoute: Hashable`，包含 Main(.tabs/.home/.gallery/.list/.user) / Player(.player/.lyrics/.audioEffects) / Library(.search/.songDetail/.artist/.album) / Playlist(.playlist/.customPlaylist/.userPlaylistManage) / Settings(.setting/.profile/.backup/.library) / AI / Custom / UserData 所有路由
- [ ] **P7.15** HMPApp.swift 改造 — `@UIApplicationDelegateAdaptor` + Koin 初始化 + `NavigationStack(path:)` + `MainTabView` 嵌入
- [ ] **P7.16** MainTabView — **iOS 26+ 可用原生 `TabView` + `.tabViewStyle(.tabBar)` Liquid Glass Tab 栏**；保留 HorizontalPager 滑动交互时用 `TabView(.page)` + 自定义 `TabPageIndicator` + `MiniPlayerBar` overlay，对应 Android `TabsHost.kt`

#### P7-D: 音乐库模块（对照 Android `ui/library/`）

- [ ] **P7.17** MusicList 组件族 — `MusicList` + `MusicListItem` + `MusicListHeader` + **`MusicListIndexStrip`（改用 `List` + `sectionIndexTitles` 原生字母索引，Android 需~340行自定义拖拽索引）** + `MusicListEditToolbar` + `MusicListScrollbar` + `ListBanner`，对应 Android `library/pages/components/musiclist/` 下 10 个文件
- [ ] **P7.18** HomeScreen — 首页/推荐，包含每日推荐卡片 + 心动歌单入口 + 快捷操作，对应 Android `HomeScreen.kt`
- [ ] **P7.19** GalleryScreen — 画廊/浏览，按专辑/艺术家分组展示，对应 Android `GalleryScreen.kt`
- [ ] **P7.20** ListScreen — 音乐列表页，集成 MusicList 组件 + 排序/筛选，对应 Android `ListScreen.kt`
- [ ] **P7.21** SearchScreen — **改用原生 `.searchable(text:)` modifier**，自带搜索栏动画+取消按钮+结果切换（Android 需手写 TextField+UiState+空状态~60行），对应 Android `SearchScreen.kt`
- [ ] **P7.22** SongDetailScreen — 歌曲详情页（参数：musicId），标签/播放历史/操作，对应 Android `SongDetailScreen.kt`
- [ ] **P7.23** ArtistScreen — 艺术家页（参数：name），对应 Android `ArtistScreen.kt`
- [ ] **P7.24** AlbumScreen — 专辑页（参数：name），对应 Android `AlbumScreen.kt`
- [ ] **P7.25** CustomScreen — 自定义主题/界面配置页，对应 Android `CustomScreen.kt`
- [ ] **P7.26** LibraryViewModel — `allMusic` / `orderBy` / `scanState` / `hiddenFolders` 状态管理，对应 Android `LibraryViewModel.kt`
- [ ] **P7.27** SearchViewModel + SongDetailViewModel — 搜索状态 + 歌曲详情+标签+播放历史，对应 Android `SearchViewModel.kt` + `SongDetailViewModel.kt`

#### P7-E: 播放器模块（对照 Android `ui/player/`）

- [ ] **P7.28** PlayerScreen — 播放主界面，专辑封面 + 进度条 + 播放控制 + 播放模式切换，**下滑关闭改用 `.presentationDetents` + `.interactiveDismiss`（Android 需自定义 nestedScroll+Animatable~30行）**，背景模糊用 Liquid Glass 替代 Haze 库，对应 Android `PlayerScreen.kt` + `PlayContent.kt` + `PlayerHeader.kt`
- [ ] **P7.29** LyricsScreen — 歌词显示页，支持原词/译文/时间轴/对齐配置，对应 Android `LyricsScreen.kt` + `AdvancedLyrics.kt`
- [ ] **P7.30** PlaylistArea + TechnicalInfoCard — 播放队列区域 + 技术信息卡片(比特率/采样率/格式)，对应 Android `PlaylistArea.kt` + `TechnicalInfoCard.kt`
- [ ] **P7.31** MiniPlayerBar — 全局悬浮迷你播放器（播放/暂停/上下曲/进度/点击展开），对应 Android `MiniPlayerBar.kt`
- [ ] **P7.32** PlaybackViewModel — `isPlaying` / `currentPosition` / `duration` / `playbackMode` / `currentPlayingMusic` / `timerRemaining`，对应 Android `PlaybackViewModel.kt`
- [ ] **P7.33** PlaylistQueueViewModel — `currentPlaylist` / `likeStatus` / `currentMusicLabels` / `currentMusicLyrics` / 心动模式 / 智能列表生成，对应 Android `PlaylistQueueViewModel.kt`

#### P7-F: 播放列表模块（对照 Android `ui/playlist/`）

- [ ] **P7.34** PlaylistScreen — 播放列表详情页（参数：name 或 playlistId），歌曲列表 + 排序 + 编辑模式，对应 Android `PlaylistScreen.kt`
- [ ] **P7.35** PlaylistManageScreen — 用户歌单管理页，创建/重命名/删除/排序/置顶，对应 Android `PlaylistManageScreen.kt`
- [ ] **P7.36** PlaylistViewModel — `genrePlaylistName` / `moodPlaylistName` / `userCustomPlaylistsState` / 歌单 CRUD + 歌曲增删排序，对应 Android `PlaylistViewModel.kt`

#### P7-G: 设置模块（对照 Android `ui/settings/`）

- [ ] **P7.37** SettingScreen — 设置主页，**改用原生 `Form` / `List(.insetGrouped)` 分组样式**（Android 需手写 Card+Row 模拟设置项），主题/背景/Haze/AI/音效/备份/关于，对应 Android `SettingScreen.kt`
- [ ] **P7.38** ProfileSettingsScreen — 个人资料设置（用户名/头像），对应 Android `ProfileSettingsScreen.kt`
- [ ] **P7.39** UserScreen — 用户页（Tab 内），每日推荐 + 使用统计 + AI 配置入口，对应 Android `UserScreen.kt`
- [ ] **P7.40** AIScreen — AI 配置页，Provider 切换 + API Key + 模型选择 + 连接测试 + 批量处理，对应 Android `AIScreen.kt`
- [ ] **P7.41** AudioEffectsScreen — 音效调节页，均衡器 + 低音增强 + 环绕声 + 混响，**均衡器预设选择器用原生 `SegmentedControl`/`Picker` 简化**（Android 手写选择器UI），垂直滑块仍需自定义，对应 Android `AudioEffectsScreen.kt`
- [ ] **P7.42** BackupSettingsScreen — 备份/还原，导出 + 导入 + 本地备份列表，对应 Android `BackupSettingsScreen.kt`
- [ ] **P7.43** LibrarySettingsScreen — 音乐库设置，扫描 + 隐藏文件夹，对应 Android `LibrarySettingsScreen.kt`
- [ ] **P7.44** UserUsageDataScreen — 使用数据统计页，播放次数/收听时长/标签分布/图表，对应 Android `UserUsageDataScreen.kt`
- [ ] **P7.45** SettingsViewModel + RecommendationViewModel + AudioEffectViewModel + UserUsageDataViewModel — 设置/AI推荐/音效/使用数据 4 个 ViewModel，对应 Android `settings/viewmodel/` 下 4 个文件

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
