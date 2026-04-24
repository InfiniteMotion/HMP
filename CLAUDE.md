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

# 代码风格检查
./gradlew ktlintCheck detekt
```

### 运行单个测试
```bash
# 运行指定测试类
./gradlew :shared:test --tests "com.hmp.data.repository.MusicRepositoryTest"

# 运行指定测试方法
./gradlew :shared:test --tests "com.hmp.data.repository.MusicRepositoryTest.testScanMusic"
```

## 架构概览

### 目录结构
```
HMP/
├── shared/                    # KMP 共享模块 (domain + data 层)
│   └── src/
│       ├── commonMain/        # 跨平台共享代码
│       ├── androidMain/       # Android 特定实现
│       ├── iosMain/           # iOS 特定实现
│       └── commonTest/        # 共享测试 (47 个用例)
├── android/                   # Android 平台
│   ├── app/                   # 入口模块 (MainActivity, Application)
│   ├── core-player/           # 播放核心 (Media3 服务, 播放控制)
│   └── feature-ui/            # UI 模块 (Compose 页面, ViewModel)
└── ios/                       # iOS 平台 (SwiftUI)
    └── HMP/                   # Xcode 项目
```

### 模块依赖关系
```
:android:feature-ui ──▶ :android:core-player
:android:feature-ui ──▶ :shared
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
- `PinyinSortKey` — 拼音排序键
- `DatabaseBuilder` — Room 数据库构建器
- `DataStoreFactory` — DataStore 构建
- `HttpClient` — Ktor HTTP 客户端 (Android: OkHttp engine / iOS: Darwin engine)
- `BackupFileRepository` — 备份文件读写

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
- iOS 部署目标: 16.0

### 包名
- Android: `com.example.hearablemusicplayer`
- Shared (KMP): `com.hmp`

### 分支策略
- `main`: 已发布版本
- `develop`: 下一版本集成分支 (当前分支)
- `feature/*`: 功能分支
- `bugfix/*`: 修复分支

### 版本号管理
版本号在 `android/app/build.gradle.kts` 中维护 (`versionCode` / `versionName`)。

### 已知待完成任务 (TODO.md)
- P6: iOS 端编译修复与核心功能实现 ✅ 已完成 (所有 expect/actual 已修复)
- P7: iOS SwiftUI 界面迁移 (~50 个组件/ViewModel)
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
