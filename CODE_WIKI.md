# Hearable Music Player Code Wiki

## 1. 项目概述

**Hearable Music Player** 是一款现代化的本地音乐播放器，基于 Jetpack Compose 构建，提供流畅的用户体验和丰富的音乐播放功能。该项目采用模块化架构设计，集成了 AI 驱动的音乐推荐功能，支持多语言国际化。

### 1.1 核心特性
- 本地音乐扫描与管理
- 完整的音乐播放控制
- 播放列表管理
- AI 驱动的音乐推荐（支持 DeepSeek、OpenAI、Claude、通义千问、文心一言）
- 歌词显示
- 音频效果调节
- 睡眠定时
- 用户听歌统计
- 暗色/亮色主题切换

### 1.2 系统要求
- Android 12.0 (API 31) 及以上
- 存储空间权限
- 网络权限（用于推荐功能）

---

## 2. 项目架构

### 2.1 架构模式
项目采用 **MVVM (Model-View-ViewModel)** 架构模式，结合了 Clean Architecture 的思想，将代码分为数据层、领域层和表现层。

### 2.2 模块化架构
项目采用模块化设计，共分为 5 个核心模块：

```
Hearable Music Player/
├── app/                    # 应用入口模块
├── core-data/              # 数据层模块
├── core-domain/            # 领域层模块
├── core-player/            # 播放核心模块
└── feature-ui/             # UI功能模块
```

### 2.3 模块依赖关系图
```
app
├── core-data
├── core-domain
├── core-player
└── feature-ui

core-data
└── core-domain

core-player
├── core-data
└── core-domain

feature-ui
├── core-data
├── core-domain
└── core-player
```

---

## 3. 模块职责

### 3.1 app 模块
应用入口模块，负责应用的初始化和启动。

**核心文件：**
- [MainActivity.kt](file:///workspace/app/src/main/java/com/example/hearablemusicplayer/MainActivity.kt) - 主活动，处理权限请求和 UI 渲染
- [MusicApplication.kt](file:///workspace/app/src/main/java/com/example/hearablemusicplayer/MusicApplication.kt) - 应用类，初始化 Hilt 依赖注入

### 3.2 core-data 模块
数据层模块，负责数据的持久化和获取。

**主要包结构：**
- `database/` - Room 数据库相关
- `di/` - 依赖注入模块
- `mapper/` - 数据模型转换器
- `network/` - 网络请求相关
- `repository/` - 仓库实现
- `util/` - 工具类

**核心文件：**
- [AppDatabase.kt](file:///workspace/core-data/src/main/java/com/example/hearablemusicplayer/data/database/AppDatabase.kt) - Room 数据库定义
- [MusicRepositoryImpl.kt](file:///workspace/core-data/src/main/java/com/example/hearablemusicplayer/data/repository/MusicRepositoryImpl.kt) - 音乐仓库实现

### 3.3 core-domain 模块
领域层模块，定义业务逻辑和数据模型。

**主要包结构：**
- `backup/` - 备份相关
- `config/` - 配置相关
- `enum/` - 枚举类型
- `music/` - 音乐相关模型和接口
- `playlist/` - 播放列表相关
- `setting/` - 设置相关

**核心文件：**
- [MusicModels.kt](file:///workspace/core-domain/src/main/java/com/example/hearablemusicplayer/domain/music/MusicModels.kt) - 音乐领域模型

### 3.4 core-player 模块
播放核心模块，封装 Media3 播放器和播放控制逻辑。

**主要包结构：**
- `controller/` - 音乐控制器
- `di/` - 依赖注入
- `service/` - 播放服务

**核心文件：**
- [MusicPlayService.kt](file:///workspace/core-player/src/main/java/com/example/hearablemusicplayer/player/service/MusicPlayService.kt) - 音乐播放服务

### 3.5 feature-ui 模块
UI 功能模块，包含所有 Compose UI 组件和页面。

**主要包结构：**
- `components/` - 可复用 UI 组件
- `dialogs/` - 对话框组件
- `pages/` - 页面组件
- `theme/` - 主题配置
- `util/` - UI 工具类
- `viewmodel/` - ViewModel 类

**核心文件：**
- [PlayControlViewModel.kt](file:///workspace/feature-ui/src/main/java/com/example/hearablemusicplayer/ui/viewmodel/PlayControlViewModel.kt) - 播放控制 ViewModel

---

## 4. 主要类与函数说明

### 4.1 应用入口类

#### MainActivity
主活动类，负责应用的初始化、权限管理和 UI 渲染。

**主要功能：**
- 处理存储和通知权限请求
- 初始化音乐控制器
- 启动时获取每日推荐
- 主题切换管理

**关键方法：**
- `onCreate()` - 初始化 UI 和权限检查
- `onStart()` - 绑定音乐服务
- `onDestroy()` - 释放资源

#### MusicApplication
应用类，初始化 Hilt 依赖注入框架。

---

### 4.2 数据库类

#### AppDatabase
Room 数据库抽象类，定义数据库结构和迁移。

**实体类：**
- `Music` - 音乐基本信息
- `MusicExtra` - 音乐额外信息
- `UserInfo` - 用户对音乐的操作信息
- `MusicLabel` - 音乐标签
- `Playlist` - 播放列表
- `PlaylistItem` - 播放列表项
- `PlaybackHistory` - 播放历史
- `ListeningDuration` - 收听时长统计

**数据库迁移：**
- `MIGRATION_1_2` - 字段类型调整
- `MIGRATION_2_3` - 添加软删除标记
- `MIGRATION_3_4` - 播放列表增强
- `MIGRATION_4_5` - 音乐额外信息表重构

---

### 4.3 播放服务类

#### MusicPlayService
前台服务，负责音乐播放控制和通知管理。

**核心接口：PlayControl**
定义了播放控制的核心方法：
- `play()`, `pause()` - 播放/暂停
- `playSingleMusic(music)` - 播放指定音乐
- `seekTo(position)` - 跳转位置
- `getCurrentPosition()`, `getDuration()` - 获取播放进度和时长
- 音效控制方法（均衡器、低音增强等）

**关键功能：**
- 基于 Media3 ExoPlayer 的播放引擎
- 前台通知显示和控制
- 耳机拔插和蓝牙断开自动暂停
- 音频效果管理（均衡器、低音增强、环绕音、混响）
- MediaSession 集成

---

### 4.4 ViewModel 类

#### PlayControlViewModel
播放控制 ViewModel，处理播放相关的 UI 状态和逻辑。

**主要状态流：**
- `isPlaying` - 播放状态
- `currentPlaylist` - 当前播放列表
- `currentIndex` - 当前播放索引
- `currentPlayingMusic` - 当前播放音乐
- `likeStatus` - 喜欢状态
- `playbackMode` - 播放模式
- `currentPosition`, `duration` - 播放进度
- `paletteColors` - 专辑封面调色板颜色
- `audioEffectSettings` - 音效设置

**主要方法：**
- `playOrResume()`, `pauseMusic()` - 播放控制
- `playNext()`, `playPrevious()` - 切换歌曲
- `seekTo(position)` - 跳转进度
- `togglePlaybackModeByOrder()` - 切换播放模式
- `updateMusicLikedStatus()` - 更新喜欢状态
- `generatePlaylist()` - 生成智能播放列表
- `extractPaletteColors()` - 提取专辑封面调色板

---

### 4.5 仓库实现类

#### MusicRepositoryImpl
音乐仓库实现，处理音乐数据的获取和操作。

**主要功能：**
- 本地音乐扫描（全量和增量）
- 音乐信息查询和排序
- 音乐标签管理
- 播放历史记录
- 用户统计分析
- AI 音乐信息获取
- 数据备份和恢复

**关键方法：**
- `loadMusicFromDevice()` - 全量扫描设备音乐
- `syncMusicFromDeviceIncremental()` - 增量同步音乐
- `getAllMusicInfoAsList()` - 获取所有音乐信息
- `fetchMusicExtraInfoWithProvider()` - 通过 AI 获取音乐额外信息
- `getSimilarSongsByWeightedLabels()` - 基于标签获取相似歌曲
- `getUserUsageAnalytics()` - 获取用户使用统计
- `exportMusicUserStateSnapshot()` - 导出用户数据快照

---

## 5. 数据模型

### 5.1 音乐模型

#### Music (领域层)
```kotlin
data class Music(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val albumArtUri: String,
)
```

#### MusicExtra (领域层)
```kotlin
data class MusicExtra(
    val id: Long,
    val lyrics: String? = null,
    val bitRate: Int? = null,
    val sampleRate: Int? = null,
    val fileSize: Long? = null,
    val format: String? = null,
    val language: String? = null,
    val date: Long? = null,
    val recommendationIds: String? = null,
    val isGetExtraInfo: Boolean,
    val rewards: String? = null,
    val popLyric: String? = null,
    val singerIntroduce: String? = null,
    val backgroundIntroduce: String? = null,
    val description: String? = null,
    val relevantMusic: String? = null
)
```

#### MusicInfo (领域层)
组合音乐基本信息、额外信息和用户信息的完整模型。
```kotlin
data class MusicInfo(
    val music: Music,
    val extra: MusicExtra?,
    val userInfo: UserInfo?
)
```

### 5.2 枚举类型

#### LabelCategory
标签分类枚举：
- `GENRE` - 流派
- `MOOD` - 情绪
- `SCENARIO` - 场景
- `LANGUAGE` - 语言
- `ERA` - 年代

#### LabelName
标签名称枚举，包含：
- 流派：ROCK, POP, JAZZ, CLASSICAL, HIPHOP, ELECTRONIC, FOLK, RNB, METAL, COUNTRY, BLUES, REGGAE, PUNK, FUNK, SOUL, INDIE
- 情绪：HAPPY, SAD, ENERGETIC, CALM, ROMANTIC, ANGRY, LONELY, UPLIFTING, MYSTERIOUS, DARK, MELANCHOLY, HOPEFUL
- 场景：WORKOUT, SLEEP, PARTY, DRIVING, STUDY, RELAX, DINNER, MEDITATION, FOCUS, TRAVEL, MORNING, NIGHT
- 语言：ENGLISH, CHINESE, JAPANESE, KOREAN, OTHERS
- 年代：SIXTIES, SEVENTIES, EIGHTIES, NINETIES, TWO_THOUSANDS, TWENTY_TENS, TWENTY_TWENTIES

#### PlaybackMode
播放模式枚举：
- `SEQUENTIAL` - 顺序播放
- `REPEAT_ALL` - 列表循环
- `REPEAT_ONE` - 单曲循环
- `SHUFFLE` - 随机播放

---

## 6. 依赖关系

### 6.1 主要技术栈

| 技术/库 | 版本 | 用途 |
|---------|------|------|
| Kotlin | 2.2.21 | 开发语言 |
| Jetpack Compose | BOM 2025.11.00 | UI 框架 |
| Hilt | 2.58 | 依赖注入 |
| Room | 2.8.3 | 本地数据库 |
| DataStore | 1.1.7 | 偏好设置存储 |
| Media3 | 1.8.0 | 媒体播放 |
| Retrofit | 2.9.0 | 网络请求 |
| OkHttp | 4.12.0 | HTTP 客户端 |
| Gson | 2.10.1 | JSON 解析 |
| Jaudiotagger | 3.0.1 | 音乐标签解析 |
| Coil | 2.7.0 | 图片加载 |
| Palette KTX | 1.0.0 | 调色板提取 |

### 6.2 模块化依赖配置

项目使用 Gradle 版本目录（Version Catalog）管理依赖，配置文件位于 [gradle/libs.versions.toml](file:///workspace/gradle/libs.versions.toml)。

---

## 7. 项目运行方式

### 7.1 环境要求
- Android Studio Hedgehog | 2023.1.1 或更高版本
- Kotlin 1.9.0 或更高版本
- Gradle 8.0 或更高版本
- JDK 17 或更高版本

### 7.2 构建步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/InfiniteMotion/HMP.git
   ```

2. **打开项目**
   使用 Android Studio 打开项目目录。

3. **同步 Gradle**
   等待 Android Studio 自动同步 Gradle 依赖。

4. **配置 AI 提供商（可选）**
   如需使用 AI 推荐功能，需要在应用内配置相应的 API 密钥。

5. **构建并运行**
   连接 Android 设备或启动模拟器，点击运行按钮。

### 7.3 首次使用
1. 应用启动后会请求存储权限，用于扫描本地音乐文件
2. 首次启动会显示引导界面
3. 扫描完成后即可浏览和播放音乐

---

## 8. 关键业务流程

### 8.1 音乐扫描流程

```
用户触发扫描
    ↓
检查权限
    ↓
查询 MediaStore
    ↓
遍历音乐文件
    ↓
提取 ID3 标签
    ↓
提取专辑封面
    ↓
提取歌词（如果有）
    ↓
批量插入数据库
    ↓
完成
```

### 8.2 音乐播放流程

```
用户点击歌曲
    ↓
PlayControlViewModel.playWith()
    ↓
MusicController.playWith()
    ↓
MusicPlayService.playSingleMusic()
    ↓
ExoPlayer 准备并播放
    ↓
更新通知
    ↓
更新 UI 状态
```

### 8.3 AI 音乐信息获取流程

```
用户请求音乐额外信息
    ↓
MusicRepositoryImpl.fetchMusicExtraInfoWithProvider()
    ↓
构建提示词
    ↓
MultiProviderApiAdapter.callChatApi()
    ↓
调用相应 AI 服务商 API
    ↓
解析 JSON 响应
    ↓
保存到数据库
    ↓
更新 UI
```

---

## 9. 开发规范

### 9.1 代码规范
- 遵循 Kotlin 官方代码风格指南
- 使用 Jetpack Compose 最佳实践
- 保持代码简洁、可读性强
- 添加必要的注释和文档

### 9.2 架构原则
- 依赖注入使用 Hilt
- 数据流向遵循单向数据流
- ViewModel 持有 UI 状态
- Repository 负责数据处理
- 避免在 UI 层直接处理业务逻辑

### 9.3 Git 工作流
- 主分支：main
- 开发分支：develop
- 功能分支：feature/feature-name
- 修复分支：fix/bug-name

---

## 10. 附录

### 10.1 相关文档
- [README.md](file:///workspace/README.md) - 项目说明
- [ROADMAP.md](file:///workspace/ROADMAP.md) - 版本路线图
- [DEVELOP.md](file:///workspace/DEVELOP.md) - 开发指南
- [TODO.md](file:///workspace/TODO.md) - 任务列表

### 10.2 联系方式
- 开发者：WLYB
- GitHub：https://github.com/InfiniteMotion/HMP

---

**文档版本：** 1.0  
**最后更新：** 2026-04-10  
**项目版本：** v5.6
