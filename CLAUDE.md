# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

Hearable Music Player (HMP) — 一款跨平台本地音乐播放器，Android (Jetpack Compose) + iOS (SwiftUI)，共享业务层基于 Kotlin Multiplatform。当前版本 v5.10，正在进行 iOS 平台适配。

**产品边界**：纯本地，不做在线/云同步、不引入账号、不做社交；仅保留用户自填 API 的 AI 推荐。

## 常用命令

### Android 构建
```bash
# 构建全部
./gradlew build

# Android Debug 构建
./gradlew :android:app:assembleDebug

# Android Release 构建
./gradlew :android:app:assembleRelease

# 安装 Debug APK 到设备
./gradlew :android:app:installDebug

# Shared 模块 Android 编译
./gradlew :shared:compileAndroidMain
```

### iOS 构建
```bash
# 生成共享 Kotlin 框架
./gradlew :shared:generateDummyFramework

# 安装 CocoaPods 依赖
cd ios && pod install

# Shared 模块 iOS 编译 (需 macOS + Xcode)
./gradlew :shared:compileKotlinIosSimulatorArm64

# 编译全部 iOS 目标
./gradlew :shared:compileKotlinIosSimulatorArm64 :shared:compileKotlinIosArm64 :shared:compileKotlinIosX64
```

### 测试与检查
```bash
# 运行单元测试
./gradlew test

# 运行 Android 仪器测试
./gradlew connectedAndroidTest
```

### 运行单个测试
```bash
# 运行指定测试类
./gradlew :shared-ui:testDebugUnitTest --tests "com.hearablemusic.player.ui.common.navigation.RoutesTest"

# 运行指定测试方法
./gradlew :shared-ui:testDebugUnitTest --tests "com.hearablemusic.player.ui.common.navigation.RoutesTest.testRouteDefinition"
```

### 发布构建
```bash
# 构建所有平台 Release 产物（输出到 releases/）
./gradlew release

# 仅构建 Android Release (APK + AAB)
./gradlew releaseAndroid

# 仅构建 iOS Release Archive (需 macOS)
./gradlew releaseIos

# 仅构建 Storybook 离线包
./gradlew releaseStorybook
```

## 架构概览

### 目录结构
```
HMP/
├── shared/                    # KMP 共享模块 (domain + data 层)
│   └── src/
│       ├── commonMain/        # 跨平台共享代码
│       ├── androidMain/       # Android 特定实现
│       └── iosMain/           # iOS 特定实现
├── android/                   # Android 平台
│   ├── app/                   # 入口模块 (MainActivity, Application)
│   └── core-player/           # 播放核心 (Media3 服务, 播放控制)
├── shared-ui/                 # 共享 UI 模块 (KMP 化中: Compose 页面, ViewModel)
│   └── src/
│       ├── commonMain/        # 跨平台共享 UI（逐步迁移中）
│       └── androidMain/       # Android UI 现有代码
└── ios/                       # iOS 平台 (SwiftUI)
    └── HMP/                   # Xcode 项目
├── storybook/                 # 组件展示 (Kotlin/Wasm)
```

### 模块依赖关系
```
:shared-ui ──▶ :android:core-player
:shared-ui ──▶ :shared
:android:core-player ──▶ :shared
:ios ──▶ :shared (via CocoaPods)
```

### 架构分层
- **UI 层**: Android (Jetpack Compose + Navigation 3) / iOS (SwiftUI + NavigationStack)
- **ViewModel 层**: Koin 注入 (`koinViewModel()`)，StateFlow 状态管理
- **Domain 层**: Use Cases + 领域模型，位于 `shared/src/commonMain/kotlin/com/hmp/domain/`
- **Data 层**: Repository + Room Database + Ktor 网络，位于 `shared/src/commonMain/kotlin/com/hmp/data/`
- **播放引擎**: Android (Media3 ExoPlayer) / iOS (AVFoundation)

### 依赖注入
- **Shared 模块**: Koin (`io.insert-koin:koin-core`)
- **Android 端**: Koin + Koin Compose (已从 Hilt 迁移)
- iOS 端通过 `AppDelegate` 调用 `KoinKt.doInitKoin()` 初始化

### 跨平台机制 (expect/actual)
以下接口通过 `expect` 在 commonMain 声明，`actual` 在 androidMain/iosMain 分别实现：
- `DeviceMusicScanner` — 设备音乐扫描
- `MusicTagParser` — 音乐标签解析
- `SecureStorageHelper` — 安全存储 (API Key 加密)
- `stringToPinyinSortKey()` — 拼音排序键
- `getRoomDatabase()` — Room 数据库构建
- `AppDatabaseConstructor` — Room 数据库构造器
- `DataStoreFactory` — DataStore 构建
- `createHttpClient()` / `createJson()` — Ktor HTTP 客户端 + JSON 配置 (Android: OkHttp engine / iOS: Darwin engine)
- `currentTimeMillis()` — 平台时间戳
- `SharedIconLoader` — 共享图标资源加载

## 关键技术栈

| 领域 | Android | Shared (KMP) | iOS |
|------|---------|--------------|-----|
| UI | Jetpack Compose + Material3 + Haze 毛玻璃 | — | SwiftUI + Liquid Glass |
| 导航 | Navigation 3 (类型安全) | — | NavigationStack |
| 播放 | Media3 ExoPlayer | — | AVFoundation |
| 数据库 | Room KMP | Room KMP | Room KMP |
| 偏好存储 | DataStore | DataStore KMP | UserDefaults |
| 网络 | — | Ktor Client | Ktor (Darwin engine) |
| 序列化 | kotlinx.serialization | kotlinx.serialization | — |
| DI | Koin | Koin | Koin |
| 图片加载 | Coil | — | AsyncImage |
| 标签解析 | Jaudiotagger + pinyin4j | 平台特定 | AVAsset 元数据 |

## 开发注意事项

### 版本信息
- Kotlin: 2.2.21
- AGP: 9.0.0
- Gradle: 9.x
- Android SDK: compileSdk 36, minSdk 33, targetSdk 36
- Koin: 4.0.4
- Ktor: 3.1.1
- Room: 2.8.3
- iOS 部署目标: 26.3 (应用目标), 16.0 (shared 模块 CocoaPods)

### 包名
- Android: `com.hearablemusic.player`
- Shared (KMP): `com.hmp`

### 分支策略
- `master`: 已发布版本
- `develop-android` / `develop-ios` / `develop-desktop` / `develop-shared`: 各平台独立开发分支
- `release/X.Y`: 发版集成分支，各 develop 合入后 PR 到 master
- `feature/*`: 功能分支
- `fix/*`: 修复分支

### 版本号管理
版本号集中维护在 `gradle.properties` 中 (`hmp.versionCode` / `hmp.versionName`)，各模块通过 `project.findProperty()` 引用。

### 发布与 CI/CD
- 本地构建：`./gradlew release`（输出到 `releases/`，含 Android APK+AAB / iOS Archive / Storybook）
- 自动发布：`release/*` 分支 PR 合并到 `master` 时，`.github/workflows/release.yml` 自动构建并发布 GitHub Release + 部署 Storybook 到 GitHub Pages
- 发版流程：各 develop 分支合入 `release/X.Y` → 验证通过 → PR 到 master 触发发布
- 详见 [docs/VERSIONING.md](docs/VERSIONING.md)

### 已知待完成任务 (TODO.md)
- P6: iOS 端编译修复与核心功能实现 ✅ 已完成
- P7: iOS SwiftUI 界面迁移 ✅ 已完成
- P8-P10: v6 阶段 iOS 功能补全与双平台对齐
- T3: Repository 通用逻辑提取到 commonMain 共享基类

### 文档索引
- [README.md](README.md) — 项目概览
- [DEVELOP.md](DEVELOP.md) — 技术架构与开发流程
- [ROADMAP.md](ROADMAP.md) — 版本历史与功能状态 (单一事实来源)
- [TODO.md](TODO.md) — 可执行任务列表
- [docs/VERSIONING.md](docs/VERSIONING.md) — 版本号规范
- [docs/ROOM_KMP_SETUP.md](docs/ROOM_KMP_SETUP.md) — Room KMP 跨平台数据库配置指南
- [docs/5_10/ios-adaptation-design.md](docs/5_10/ios-adaptation-design.md) — iOS 适配设计
- [docs/5_10/ios-adaptation-plan.md](docs/5_10/ios-adaptation-plan.md) — iOS 适配实施计划
