# Hearable Music Player 开发文档

本文档详细记录了 Hearable Music Player 的技术架构、实现细节、开发流程和关键决策，旨在帮助开发者快速理解项目并参与开发。项目文档索引与职责说明见 [docs/README.md](docs/README.md)。

## 🏗️ 技术架构

### 整体架构

项目采用MVVM（Model-View-ViewModel）架构模式，结合Jetpack Compose和AndroidX组件库，实现了清晰的职责分离和可维护性。

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│    UI Layer     │     │ ViewModel Layer │     │   Domain Layer  │
│  (Jetpack       │────▶│  (Hilt          │────▶│  (Use Cases,    │
│   Compose)      │     │   ViewModel)    │     │   Repository)   │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                              │                          │
                              ▼                          ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Data Layer     │     │  Service Layer  │     │  Network Layer  │
│  (Room,         │     │  (Media3,       │     │  (Retrofit,     │
│   DataStore)    │     │   MediaSession) │     │   OkHttp)       │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

### 模块划分

项目采用模块化架构，将不同功能划分为独立的模块，降低耦合度并提高可维护性。目前已经完成了模块化重构，划分为以下核心模块：

#### 核心模块

- **app**: 应用入口模块，包含MainActivity和Application类
- **core-data**: 数据层模块，包含 Room 数据库、DataStore、网络请求 (Retrofit) 和 AI 服务集成
- **core-domain**: 领域层模块，包含 Use Cases、领域模型和 Repository 接口定义
- **core-player**: 播放核心模块，包含Media3服务和播放控制逻辑
- **feature-ui**: UI功能模块，包含Compose页面和组件

### 模块间依赖关系

```
:feature-ui ──▶ :core-player
:feature-ui ──▶ :core-domain
:core-player ──▶ :core-domain
:core-data ──▶ :core-domain (implements)
```

### 模块化进展

- ✅ 已完成模块划分和依赖配置
- ✅ 已将Room/Repository移至core-data
- ✅ 已将Use Cases移至core-domain
- ✅ 已将Media3/Service移至core-player
- ✅ 已将Compose页面组件移至feature-ui
- ✅ app模块仅依赖feature-ui和核心模块，避免横向耦合

### 关键技术决策

#### 1. 依赖注入：Hilt

**选择理由**：Hilt与Jetpack组件深度集成，提供了简单易用的API，并且支持测试替换。作为个人项目，我希望通过使用Hilt来学习现代Android开发的最佳实践。

**实现细节**：
- MusicApplication标注为@HiltAndroidApp
- ViewModel使用@HiltViewModel和@Inject构造函数
- 提供@Module和@Provides方法来创建单例依赖

#### 2. 状态管理：Kotlin Flow/StateFlow

**选择理由**：Kotlin Flow和StateFlow提供了一种简洁的方式来管理UI状态，并且支持异步操作和线程切换。作为个人项目，我希望通过使用Flow来学习响应式编程的思想。

**实现细节**：
- ViewModel暴露StateFlow给UI层
- UI层使用collectAsState()订阅状态变化
- 所有状态更新都通过Flow进行，避免竞态条件

#### 3. 媒体播放：AndroidX Media3

**选择理由**：AndroidX Media3是Google推出的新一代媒体播放框架，提供了统一的API，支持多种媒体格式和播放场景。作为个人项目，我希望通过使用Media3来学习现代Android媒体播放的最佳实践。

**实现细节**：
- MusicPlayService管理ExoPlayer和MediaSession
- 实现音频焦点管理和通知控制
- 支持耳机插拔、蓝牙控制和来电打断处理

#### 4. 数据存储：Room + DataStore

**选择理由**：Room是Google推出的ORM框架，提供了简单易用的API，并且支持SQLite数据库的所有功能。DataStore是Google推出的新一代偏好设置存储框架，提供了一种安全、可靠的方式来存储应用偏好设置。作为个人项目，我希望通过使用Room和DataStore来学习现代Android数据存储的最佳实践。

**实现细节**：
- Room数据库版本化管理，支持迁移
- DataStore存储主题、音量等偏好设置
- Repository层封装数据访问逻辑

#### 5. 网络请求：Retrofit + OkHttp

**选择理由**：Retrofit是Square推出的网络请求框架，提供了简单易用的API，并且支持多种网络请求方式。OkHttp是Square推出的HTTP客户端，提供了高性能、可靠的网络请求能力。作为个人项目，我希望通过使用Retrofit和OkHttp来学习现代Android网络请求的最佳实践。

**实现细节**：
- Retrofit接口定义API请求
- OkHttp配置连接、读取和写入超时
- 实现失败重试和指数退避策略

#### 6. AI服务集成：多服务商支持

**选择理由**：为了提供更灵活的 AI 推荐服务，项目支持多个 AI 服务商（DeepSeek、OpenAI、Claude、通义千问、文心一言）。用户可以根据自己的需求选择不同的服务商。

**实现细节**：
- 统一的 API 适配器层，封装不同服务商的 API 调用
- API 密钥加密存储，保障安全性
- 支持 API 连接测试功能
- 用户可在配置界面自由切换服务商

#### 7. 导航系统：Navigation 3

**选择理由**：Navigation 3 提供了类型安全的导航方式，支持编译时路由检查和参数验证。迁移到 Navigation 3 后，消除了字符串路由的潜在错误，提升了导航的可靠性。

**实现细节**：
- 使用 @Serializable 注解定义路由
- 集中式路由管理，统一处理导航逻辑
- 支持类型安全的参数传递
- 迁移过程中保持向后兼容

#### 8. 视觉效果：毛玻璃效果

**选择理由**：毛玻璃效果（Haze）可以提升UI的视觉层次感和现代感，与动态背景结合使用效果更佳。

**实现细节**：
- 使用 Haze 库实现毛玻璃效果
- 支持动态背景风格选择
- 可配置的模糊强度和颜色叠加
- 应用于弹窗、底部栏等组件

## 📦 项目结构

```
Hearable Music Player/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/hearablemusicplayer/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   └── MusicApplication.kt
│   │   │   └── res/                 # Resources
│   │   └── test/                    # Unit tests
│   └── build.gradle.kts             # Module build configuration
├── core-data/
│   ├── schemas/                     # Room database schemas
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/hearablemusicplayer/
│   │   │   │   ├── data/
│   │   │   │   │   ├── database/    # AppDatabase and entities
│   │   │   │   │   ├── di/          # Database/Network/Repository modules
│   │   │   │   │   ├── network/     # DeepSeekAPI and network utilities
│   │   │   │   │   ├── repository/  # Repository implementations
│   │   │   │   │   └── util/        # SecureStorage and other utilities
│   │   │   └── AndroidManifest.xml
│   │   └── test/                    # Data layer tests
│   └── build.gradle.kts
├── core-domain/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/hearablemusicplayer/
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/       # Domain models
│   │   │   │   │   ├── repository/  # Repository interfaces
│   │   │   │   │   └── usecase/     # Use Cases
│   │   │   └── AndroidManifest.xml
│   │   └── test/                    # Domain layer tests
│   └── build.gradle.kts
├── core-player/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/hearablemusicplayer/
│   │   │   │   ├── player/
│   │   │   │   │   ├── controller/  # MusicController (播放控制)
│   │   │   │   │   ├── di/          # Player modules
│   │   │   │   │   └── service/     # MusicPlayService and receiver
│   │   │   └── res/                 # Player resources
│   │   └── test/                    # Player layer tests
│   └── build.gradle.kts
├── feature-ui/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/hearablemusicplayer/
│   │   │   │   ├── ui/
│   │   │   │   │   ├── common/      # 通用组件和基础设施
│   │   │   │   │   │   ├── components/      # 通用UI组件
│   │   │   │   │   │   │   └── base/        # 基础组件 (Empty, Loading, Error)
│   │   │   │   │   │   ├── design/          # 设计系统
│   │   │   │   │   │   │   ├── animation/   # 动画令牌
│   │   │   │   │   │   │   ├── colors/      # 颜色令牌
│   │   │   │   │   │   │   ├── core/        # 设计系统核心
│   │   │   │   │   │   │   ├── theme/       # 主题管理
│   │   │   │   │   │   │   └── typography/  # 排版令牌
│   │   │   │   │   │   ├── dialogs/         # 弹窗组件
│   │   │   │   │   │   │   ├── base/        # 基础弹窗组件
│   │   │   │   │   │   │   ├── controller/  # DialogManager
│   │   │   │   │   │   │   └── viewmodel/   # DialogViewModel, DialogEvent
│   │   │   │   │   │   ├── navigation/      # Navigation 3 路由
│   │   │   │   │   │   ├── pages/           # 通用页面
│   │   │   │   │   │   │   └── base/        # TabScreen, SubScreen
│   │   │   │   │   │   ├── util/            # UI工具类
│   │   │   │   │   │   └── viewmodel/       # ThemeViewModel
│   │   │   │   │   ├── library/     # 音乐库模块
│   │   │   │   │   │   ├── pages/           # Home, Gallery, Search, Artist, Album, SongDetail
│   │   │   │   │   │   │   └── components/  # 音乐列表组件
│   │   │   │   │   │   │       └── musiclist/   # MusicList及相关组件
│   │   │   │   │   │   └── viewmodel/       # LibraryViewModel, SearchViewModel, SongDetailViewModel
│   │   │   │   │   ├── player/      # 播放器模块
│   │   │   │   │   │   ├── components/      # 播放器组件
│   │   │   │   │   │   ├── pages/           # PlayerScreen, LyricsScreen, PlaylistArea
│   │   │   │   │   │   └── viewmodel/       # PlaybackViewModel, PlaylistQueueViewModel, PlayControlViewModel
│   │   │   │   │   ├── playlist/    # 播放列表模块
│   │   │   │   │   │   ├── pages/           # PlaylistScreen, PlaylistManageScreen
│   │   │   │   │   │   └── viewmodel/       # PlaylistViewModel
│   │   │   │   │   └── settings/    # 设置模块
│   │   │   │   │       ├── pages/           # SettingScreen, AIScreen, UserScreen等
│   │   │   │   │       ├── components/      # 设置页面组件
│   │   │   │   │       └── viewmodel/       # SettingsViewModel, AudioEffectViewModel等
│   │   │   └── AndroidManifest.xml
│   │   └── test/                    # UI tests
│   └── build.gradle.kts
├── gradle/
│   └── wrapper/
├── .gitignore
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── settings.gradle.kts
```

## 🚀 开发流程

### 开发环境

- Android Studio Ladybug | 2024.2.1
- Kotlin 2.2.21
- Gradle 9.1.0
- Android SDK 35
- AGP (Android Gradle Plugin) 9.0.0

### 构建项目

```bash
# 克隆项目
https://github.com/yourusername/hearable-music-player.git

# 进入项目目录
cd hearable-music-player

# 构建项目
./gradlew build

# 运行应用
./gradlew installDebug
```

### 开发进展

#### 已完成

- ✅ 模块化架构设计与实现
- ✅ 核心音乐播放功能（Media3集成）
- ✅ 本地音乐扫描与Room数据库存储
- ✅ 多 AI 服务商集成与管理
- ✅ Jetpack Compose UI界面搭建
- ✅ Hilt依赖注入配置
- ✅ 基本的播放控制功能
- ✅ 触觉反馈增强用户体验
- ✅ 动态主题设置与切换
- ✅ 音频效果调节功能
- ✅ 按艺术家分类浏览
- ✅ 自定义主题设置
- ✅ 优化播放页面UI及交互体验
- ✅ 更新状态沉浸并优化用户播放界面
- ✅ 统一调整UI组件颜色适配主题色彩体系
- ✅ 修复播放进度调整失败的漏洞
- ✅ 引入TabScreen模板
- ✅ 实现每日推荐刷新策略系统
- ✅ API 密钥加密存储机制
- ✅ Navigation 3 迁移与类型安全导航
- ✅ Gradle 9.0 升级
- ✅ 代码混淆与包体积优化
- ✅ ViewModel 职责拆分与代码组织优化
- ✅ 专辑页面功能
- ✅ 音乐文件分享功能
- ✅ 毛玻璃视觉效果

#### 进行中

- 🔄 单元测试覆盖
- 🔄 性能优化与内存泄漏修复
- 🔄 CI/CD流水线配置

#### 待完成

- 📝 完善文档
- 📝 代码注释与格式化
- 📝 错误处理与异常捕获

### 代码风格

项目遵循Kotlin官方代码风格指南，使用ktlint进行代码检查。

**配置文件**：
- `.editorconfig`: 编辑器配置
- `ktlint.gradle`: ktlint配置

### 测试流程

项目采用分层测试策略，确保代码质量和功能正确性。

#### 单元测试

- 使用JUnit 5和MockK进行单元测试
- 测试Repository、Use Cases和ViewModel
- 运行命令：`./gradlew test`

#### 仪器测试

- 使用Espresso和Compose Test进行仪器测试
- 测试UI交互和服务功能
- 运行命令：`./gradlew connectedAndroidTest`

#### 静态检查

- 使用ktlint进行代码风格检查
- 使用detekt进行代码质量分析
- 运行命令：`./gradlew ktlintCheck detekt`

### 版本控制

项目使用Git进行版本控制，采用Git Flow工作流。

**分支策略**（与版本规范一致，详见 [docs/VERSIONING.md](docs/VERSIONING.md) 分支与发版）：
- `main`: 已发布版本；MINOR/MAJOR 通过从 develop 合并更新，PATCH 可在 main 上直接改并打 tag
- `develop`: 下一版本的集成分支，MINOR 功能开发在此进行
- `feature/*`: 功能分支，从 develop 拉出，开发完毕后合并回 develop
- `bugfix/*`: 修复分支，合并回 develop 或（若仅 PATCH 热修）合并回 main

### 构建与发布

#### 构建类型

- `debug`: 调试版本，包含调试信息
- `release`: 发布版本，经过混淆和优化

#### 发布流程

版本号与发布步骤详见 **[docs/VERSIONING.md](docs/VERSIONING.md)**，摘要如下：

1. **确定版本类型**：按变更内容决定升级 MAJOR / MINOR / PATCH，得到新版本号（如 5.7.0）
2. **更新构建配置**：在 `app/build.gradle.kts` 中更新 `versionCode` 和 `versionName`
3. **更新 ROADMAP**：在 [ROADMAP.md](ROADMAP.md) 中新增该版本条目与「当前版本」
4. 构建发布包：`./gradlew assembleRelease`
5. 签名 APK 并上传或分发

## 🎯 关键实现细节

### 音乐扫描与解析

**流程**：
1. 申请存储权限
2. 扫描设备中的音乐文件
3. 使用Jaudiotagger解析ID3标签
4. 将音乐信息存储到Room数据库
5. 通过Repository暴露给UI层

**关键代码**：
- `MusicScanner.kt`: 音乐扫描逻辑
- `ID3Parser.kt`: ID3标签解析
- `MusicRepository.kt`: 数据访问封装

### 播放控制

**流程**：
1. 用户选择歌曲
2. ViewModel更新播放队列
3. Service层启动ExoPlayer
4. 媒体会话同步播放状态
5. 通知栏显示播放控制

**关键代码**：
- `PlayControlViewModel.kt`: 播放控制逻辑
- `MusicPlayService.kt`: 播放服务实现
- `MediaSessionManager.kt`: 媒体会话管理

### AI推荐功能

**流程**：
1. 用户选择 AI 服务商并配置 API 密钥
2. 系统自动或手动触发推荐（根据刷新策略）
3. ViewModel调用Use Case
4. Repository请求当前选中的 AI 服务商 API
5. 解析推荐结果并生成标签
6. 展示推荐歌曲和 AI 生成的扩展信息

**关键代码**：
- `MultiProviderApiAdapter.kt`: 多服务商 API 适配器
- `GetDailyMusicRecommendationUseCase.kt`: 推荐用例
- `SettingsRepository.kt`: API 密钥加密存储
- `AIScreen.kt`: AI 服务商配置界面

### 每日推荐刷新策略

**功能**：
- 按时间刷新：用户可设置间隔小时数（默认24小时）
- 按启动次数刷新：用户可设置启动次数（默认3次）
- 智能刷新：预留接口，后续可根据听歌习惯智能判断
- 持久化存储：重启后保持同一首每日推荐

**关键代码**：
- `UserSettingsUseCase.kt`: 刷新策略判断逻辑
- `SettingsRepository.kt`: 刷新配置存储
- `MusicViewModel.kt`: 刷新控制逻辑
- `SettingScreen.kt`: 刷新策略配置界面

## 📚 学习资源

### 官方文档

- [Kotlin官方文档](https://kotlinlang.org/docs/home.html)
- [Jetpack Compose官方文档](https://developer.android.com/jetpack/compose)
- [AndroidX Media3官方文档](https://developer.android.com/jetpack/androidx/releases/media3)
- [Hilt官方文档](https://developer.android.com/training/dependency-injection/hilt-android)

### 推荐教程

- [Jetpack Compose Tutorial](https://developer.android.com/codelabs/jetpack-compose-basics)
- [Android MVVM Architecture](https://developer.android.com/topic/architecture)
- [Media3 Playback Tutorial](https://developer.android.com/codelabs/media3-getting-started)

## 🤝 贡献指南

作为个人项目，我欢迎任何形式的贡献和反馈。如果您有任何建议或问题，请随时联系我。

### 贡献方式

1. 提交Issue报告bug或提出功能建议
2. 提交Pull Request修复bug或添加新功能
3. 提供使用反馈和改进建议

### 代码规范

- 遵循Kotlin官方代码风格指南
- 使用Jetpack Compose的最佳实践
- 保持代码简洁、可读性强
- 添加必要的注释和文档

## 📄 许可证

该项目使用MIT许可证 - 详情请查看LICENSE文件

---

© 2026 Hearable Music Player | Developed by WLYB