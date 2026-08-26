# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

Hearable Music Player (HMP) — 一款跨平台本地音乐播放器，Android (Jetpack Compose) / Desktop (Compose Multiplatform) / iOS (SwiftUI) 三端，共享业务层与 Android/Desktop 共享 UI 均基于 Kotlin Multiplatform。当前版本 v7.0.0。

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
# 生成聚合框架 + podspec（shared + shared-ui 单框架 sharedIos）
./gradlew :shared-ios:generateDummyFramework
./gradlew :shared-ios:podspec

# shared-ui / shared 模块 iOS 编译
./gradlew :shared-ui:compileKotlinIosSimulatorArm64
./gradlew :shared:compileKotlinIosSimulatorArm64

# 安装 CocoaPods 依赖（Podfile 已含 pod 'shared_ios'；Pod 脚本阶段自动 link + 同步产物）
cd ios && pod install

# 构建 iOS 模拟器 App（Apple Silicon；Xcode 26.6 需 iOS 26.5 模拟器运行时，
# 缺失时先 `xcodebuild -downloadPlatform iOS`；generic 目的地会自动装 26.5）
xcodebuild -workspace ios/HMP.xcworkspace -scheme HMP -configuration Debug \
  -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' \
  ARCHS=arm64 ONLY_ACTIVE_ARCH=YES build
```
说明：
- **聚合框架**：`shared-ios` 模块把 `:shared` + `:shared-ui` 链接为单一 static framework
  (baseName `sharedIos`)，Swift 统一 `import sharedIos`；避免双静态框架 duplicate symbol 与
  动态框架的 Koin 全局分裂。Podfile 含 Kotlin 2.3 适配（`syncFramework` 已更名 →
  `linkPod{Debug|Release}FrameworkIos{...}` + 产物同步到 podspec vendored 路径 +
  shared-ui composeResources 按 `<bundle>/compose-resources/composeResources/<Res包>/` 布局拷贝）。
- navigation3-ui 无 ios_x64 构件 → shared-ui/configure `shared-ios` 均不启用 iosX64。
- 试点入口：`simctl launch ... com.hmp.HMP -hmp-pilot`（AppDelegate 内模态呈现设置中心 Compose 试点）。

### Desktop 构建
```bash
# 运行 Desktop 应用（开发调试）
./gradlew :desktop:app:run

# 构建当前 OS 安装包 (macOS DMG / Windows MSI / Linux DEB+AppImage)
./gradlew :desktop:app:packageDistributionForCurrentOS
```

### 单端构建
通过环境变量 `HMP_BUILD_TARGET`（`android` / `desktop` / `all`，默认 `all`）只包含对应平台的模块（见 `settings.gradle.kts`），单端任务链不需要另一端的构建产物，可加快配置与构建。

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

# 仅构建 Desktop Release 分发包 (DMG/MSI/DEB/AppImage)
./gradlew releaseDesktop

# 仅构建 iOS Release Archive (需 macOS)
./gradlew releaseIos

# 仅构建 Storybook 离线包
./gradlew releaseStorybook
```

## 架构概览

### 目录结构
```
HMP/
├── shared/                    # KMP 共享业务模块 (domain + data 层)
│   └── src/
│       ├── commonMain/        # 跨平台共享代码
│       ├── androidMain/       # Android 特定实现
│       ├── desktopMain/       # Desktop (JVM) 特定实现
│       └── iosMain/           # iOS 特定实现
├── shared-ui/                 # 共享 UI 模块 (Compose 页面, ViewModel)
│   └── src/
│       ├── commonMain/        # Android/Desktop/iOS 三端共享 UI（v7.0 Android/Desktop；v7.1 iOS）
│       ├── androidMain/       # Android 桥接层
│       ├── desktopMain/       # Desktop 桥接层
│       └── iosMain/           # iOS 桥接层（PlaybackController 双桥 / PlatformServices / 触觉等，A3-A4）
├── android/                   # Android 平台
│   ├── app/                   # 入口模块 (MainActivity, Application)
│   └── core-player/           # 播放核心 (Media3 服务, 播放控制)
├── desktop/                   # Desktop 平台 (Compose Multiplatform)
│   ├── app/                   # 入口模块 (Main.kt, 无边框窗口, 托盘, 单实例)
│   └── core-player/           # 播放核心 (FFmpeg + JNA 音频引擎)
├── ios/                       # iOS 平台 (SwiftUI + Compose 试点)
│   └── HMP/                   # Xcode 项目（XcodeGen + CocoaPods）
├── shared-ios/                # iOS 聚合框架（shared + shared-ui → sharedIos，方向 A A1）
└── storybook/                 # 组件展示 (Kotlin/Wasm)
```

### 模块依赖关系
```
:shared-ui ──▶ :shared
:shared-ui ──▶ :android:core-player   (androidMain 桥接)
:shared-ui ──▶ :desktop:core-player   (desktopMain 桥接)
:android:app ──▶ :shared + :shared-ui
:desktop:app ──▶ :shared + :shared-ui + :desktop:core-player
:android:core-player ──▶ :shared
:desktop:core-player ──▶ :shared
:ios ──▶ :shared-ios (via CocoaPods；聚合 :shared + :shared-ui)
```

### 架构分层
- **UI 层**: Android + Desktop 共享 `shared-ui` commonMain（Compose，平台差异收口到 androidMain/desktopMain 桥接层：`PlaybackController` / `AlbumArtPixelsLoader` / `PlatformServices` 接口，位于 `ui/platform/`）/ iOS (SwiftUI + NavigationStack)
- **ViewModel 层**: 位于 shared-ui，Koin 注入 (`koinViewModel()`)，StateFlow 状态管理
- **Domain 层**: Use Cases + 领域模型，位于 `shared/src/commonMain/kotlin/com/hmp/domain/`
- **Data 层**: Repository + Room Database + Ktor 网络，位于 `shared/src/commonMain/kotlin/com/hmp/data/`
- **播放引擎**: Android (Media3 ExoPlayer) / Desktop (FFmpeg + JNA 自研引擎) / iOS (AVFoundation)

### 依赖注入
- **Shared 模块**: Koin (`io.insert-koin:koin-core`)
- **Android 端**: Koin + Koin Compose (已从 Hilt 迁移)
- iOS 端通过 `AppDelegate` 调用 `KoinKt.doInitKoin()` 初始化

### 跨平台机制 (expect/actual)
以下接口通过 `expect` 在 commonMain 声明，`actual` 在 androidMain/desktopMain/iosMain 分别实现：
- `DeviceMusicScanner` — 设备音乐扫描
- `MusicTagParser` — 音乐标签解析
- `SecureStorageHelper` — 安全存储 (API Key 加密)
- `stringToPinyinSortKey()` — 拼音排序键
- `getRoomDatabase()` — Room 数据库构建
- `AppDatabaseConstructor` — Room 数据库构造器
- `DataStoreFactory` — DataStore 构建
- `createHttpClient()` / `createJson()` — Ktor HTTP 客户端 + JSON 配置 (Android: OkHttp engine / iOS: Darwin engine)
- `currentTimeMillis()` — 平台时间戳

图标资源已迁移至 `shared-ui` 的 composeResources 编译期资源（exhaustive when 映射，不再运行时动态加载）

## 关键技术栈

| 领域 | Android | Desktop | Shared (KMP) | iOS |
|------|---------|---------|--------------|-----|
| UI | shared-ui Compose + Material3 + Haze 毛玻璃 | shared-ui Compose (响应式 Compact/Expanded) | — | SwiftUI + Liquid Glass |
| 导航 | shared-ui 共享导航 (Navigation 3 类型安全) | 与 Android 共用 shared-ui | — | NavigationStack |
| 播放 | Media3 ExoPlayer | FFmpeg + JNA 自研引擎 | — | AVFoundation |
| 数据库 | Room KMP | Room KMP | Room KMP | Room KMP |
| 偏好存储 | DataStore | DataStore KMP | DataStore KMP | UserDefaults |
| 网络 | — | — | Ktor Client | Ktor (Darwin engine) |
| 序列化 | kotlinx.serialization | kotlinx.serialization | kotlinx.serialization | — |
| DI | Koin | Koin | Koin | Koin |
| 图片加载 | Coil | — | — | AsyncImage |
| 标签解析 | Jaudiotagger + pinyin4j | — | 平台特定 | AVAsset 元数据 |

## 开发注意事项

### 版本信息
- 应用版本: 7.0.0 (versionCode 70000)
- JDK 工具链: 21（Desktop jpackage 要求 Gradle Daemon 运行于 JDK 21，配置说明见 `gradle.properties` 注释）
- Kotlin: 2.3.21
- AGP: 9.0.0
- Gradle: 9.x
- Android SDK: compileSdk 36, minSdk 33, targetSdk 36
- Koin: 4.2.2（4.2.2 起 iOS 端与 lifecycle 2.10 稳定 ID 对齐，修复 Koin 反射探测 SavedStateHandle 的 IrLinkageError）
- Ktor: 3.1.1
- Room: 2.8.3
- iOS 部署目标: 26.3 (应用目标), 16.0 (shared 模块 CocoaPods)

### 包名
- Android: `com.hearablemusic.player`
- Shared (KMP): `com.hmp`
- iOS: `com.hearablemusic.HMP`（2026-08-23 真机调试修改：原 com.hmp.HMP 不满足免费团队唯一标识注册，project.yml bundleIdPrefix 同时改为 com.hearablemusic）

### 分支策略
- `master`: 已发布版本
- `release/X.Y`: 发版集成分支，feature 分支合入后 PR 到 master 触发自动发布（合并后远程 release 分支删除）
- `feature/*`: 功能分支
- `fix/*`: 修复分支

> 无长期存活的 develop-* 分支；历史文档中提到的 develop 系列分支已不再使用。

### 版本号管理
版本号集中维护在 `gradle.properties` 中 (`hmp.versionCode` / `hmp.versionName`)，各模块通过 `project.findProperty()` 引用。

### 发布与 CI/CD
- 本地构建：`./gradlew release`（输出到 `releases/`，含 Android APK+AAB / Desktop DMG+MSI+DEB+AppImage / Storybook；iOS Archive 仅 macOS）
- 自动发布：`release/*` 分支 PR 合并到 `master` 时，`.github/workflows/release.yml` 自动构建并发布 GitHub Release（Android + Desktop 产物 + SHA256 校验）+ 部署 Storybook 到 GitHub Pages
- 发版流程：feature/* 合入 `release/X.Y` → 验证通过（单测 + 版本号重复检测）→ PR 到 master 触发发布
- 详见 [docs/VERSIONING.md](docs/VERSIONING.md)

### 已知待完成任务 (TODO.md)
- v7.x 三大方向：A) KMP 重写 iOS UI / B) AI 功能 Agent 化 / C) 播放功能增强补齐（编排 7.1-7.4，方向论证见 ROADMAP「未来发展方向」，任务分解见 TODO.md「v7.x 阶段」）
- v6 遗留：P8.1 iOS 安全存储真加密（现 XOR 伪加密 → CryptoKit AES-GCM + Keychain）；P9 已由方向 A 取代冻结
- T3: Repository 通用逻辑提取到 commonMain 共享基类

### 文档索引
- [README.md](README.md) — 项目概览
- [docs/README.md](docs/README.md) — 项目文档索引与各文档职责
- [DEVELOP.md](DEVELOP.md) — 技术架构与开发流程
- [ROADMAP.md](ROADMAP.md) — 版本历史与功能状态 (单一事实来源)
- [TODO.md](TODO.md) — 可执行任务列表
- [docs/VERSIONING.md](docs/VERSIONING.md) — 版本号规范
- [docs/ROOM_KMP_SETUP.md](docs/ROOM_KMP_SETUP.md) — Room KMP 跨平台数据库配置指南
- [docs/5_10/ios-adaptation-design.md](docs/5_10/ios-adaptation-design.md) — iOS 适配设计
- [docs/5_10/ios-adaptation-plan.md](docs/5_10/ios-adaptation-plan.md) — iOS 适配实施计划
