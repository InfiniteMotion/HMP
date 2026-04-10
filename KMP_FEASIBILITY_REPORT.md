# Hearable Music Player KMP 适配可行性评估报告

## 1. 评估概述

本报告详细评估了将 Hearable Music Player 从纯 Android 项目适配为 Kotlin Multiplatform (KMP) 项目的可行性、技术挑战和实施建议。

**评估日期**: 2026-04-10  
**当前项目版本**: v5.7  
**目标平台**: iOS + Android

---

## 2. 项目现状分析

### 2.1 当前技术栈

| 技术类别 | 技术选型 | 版本 | KMP 适配难度 |
|---------|---------|------|-------------|
| 开发语言 | Kotlin | 2.2.21 | ✅ 完全兼容 |
| UI 框架 | Jetpack Compose | BOM 2025.11.00 | ⚠️ 需要 Compose Multiplatform |
| 依赖注入 | Hilt | 2.58 | ⚠️ 需要 Koin/Kodein |
| 数据库 | Room | 2.8.3 | ⚠️ 需要 SQLDelight 或 Room Multiplatform |
| 偏好设置 | DataStore | 1.1.7 | ✅ 支持 KMP |
| 媒体播放 | AndroidX Media3 | 1.8.0 | ❌ 需平台特定实现 |
| 网络请求 | Retrofit + OkHttp | 2.9.0 / 4.12.0 | ⚠️ 需 Ktor 或 OkHttp KMP |
| JSON 解析 | Gson | 2.10.1 | ⚠️ 建议 kotlinx.serialization |
| ID3 解析 | Jaudiotagger | 3.0.1 | ❌ 需平台特定实现 |
| 拼音排序 | pinyin4j | 2.5.1 | ❌ 需 KMP 替代方案 |

### 2.2 架构优势

当前项目的模块化架构为 KMP 适配提供了良好基础：

✅ **已实现 Clean Architecture** - 清晰的分层架构  
✅ **模块化设计** - 5 个独立模块，职责分离明确  
✅ **Kotlin 代码** - 100% Kotlin，无 Java 遗留代码  
✅ **MVVM 模式** - UI 与业务逻辑分离良好  

---

## 3. 模块适配可行性分析

### 3.1 core-domain 模块

**适配难度**: ⭐ (最容易)

**现状**:
- 纯 Kotlin 代码，无 Android 依赖
- 包含领域模型、枚举、Repository 接口
- 使用 Kotlin Coroutines 和 Flow

**适配策略**:
- 直接迁移为 KMP 共享模块
- 无需大改动

**关键文件**:
- [MusicModels.kt](file:///workspace/core-domain/src/main/java/com/example/hearablemusicplayer/domain/music/MusicModels.kt) - 领域模型可完全复用

---

### 3.2 core-data 模块

**适配难度**: ⭐⭐⭐ (中等)

**当前依赖**:
- Room (Android 专属)
- DataStore (有 KMP 版本)
- Retrofit + OkHttp
- Jaudiotagger (Android/Java 专属)
- Hilt (Android 专属)

**适配方案对比**:

| 组件 | 当前技术 | KMP 替代方案 | 迁移难度 |
|------|---------|-------------|---------|
| 数据库 | Room | SQLDelight / Room Multiplatform (Beta) | ⭐⭐⭐ |
| 偏好设置 | DataStore | DataStore Preferences (KMP) | ⭐ |
| 网络 | Retrofit | Ktor Client | ⭐⭐ |
| JSON | Gson | kotlinx.serialization | ⭐⭐ |
| DI | Hilt | Koin / Kodein | ⭐⭐ |
| ID3 解析 | Jaudiotagger | 平台特定实现 + expect/actual | ⭐⭐⭐⭐ |
| 拼音排序 | pinyin4j | KMP 拼音库 / 平台特定 | ⭐⭐⭐ |

**推荐方案**:
- 数据库: SQLDelight (成熟稳定)
- 网络: Ktor Client
- DI: Koin (轻量，KMP 友好)
- JSON: kotlinx.serialization
- ID3/拼音: expect/actual 模式

---

### 3.3 core-player 模块

**适配难度**: ⭐⭐⭐⭐ (困难)

**当前依赖**:
- AndroidX Media3 (ExoPlayer)
- Android Service
- MediaSession
- 音频效果 (Android AudioEffect API)

**问题分析**:
- Media3 是 Android 专属框架
- iOS 需要使用 AVFoundation
- 前台服务机制完全不同
- 通知栏控制实现差异大

**适配策略**:
```
┌─────────────────────────────────────────┐
│      共享 Player 接口层 (expect)        │
├─────────────────────────────────────────┤
│  actual (Android) │ actual (iOS)       │
│  Media3 + Service  │  AVFoundation      │
└────────────────────┴────────────────────┘
```

**关键差异**:

| 功能 | Android | iOS |
|------|---------|-----|
| 播放器 | ExoPlayer | AVPlayer |
| 后台播放 | Foreground Service | Background Modes + AVAudioSession |
| 通知控制 | MediaSession + Notification | NowPlayingInfoCenter + RemoteCommandCenter |
| 音频效果 | AudioEffect API | AVAudioEngine |
| 耳机监听 | BroadcastReceiver | AVAudioSessionInterruptionNotification |

---

### 3.4 feature-ui 模块

**适配难度**: ⭐⭐⭐ (中等)

**当前技术**: Jetpack Compose

**适配方案**: Compose Multiplatform

**UI 组件迁移**:

| 组件 | Android | Compose Multiplatform |
|------|---------|---------------------|
| Material3 | androidx.compose.material3 | androidx.compose.material3 (共享) |
| Navigation | androidx.navigation.compose | com.arkivanov.decompose 或 Voyager |
| 图片加载 | Coil | Coil3 (支持 KMP) |
| 调色板 | Palette KTX | 需要 KMP 替代 / 平台特定 |
| 系统主题 | isSystemInDarkTheme() | expect/actual |

**推荐导航方案**:
- **Voyager**: 简单易用，适合中小项目
- **Decompose**: 更强大，支持状态保存

---

### 3.5 app 模块

**适配难度**: ⭐⭐⭐⭐ (困难)

**内容**:
- MainActivity (Android)
- MusicApplication (Android)
- AndroidManifest.xml

**适配策略**:
- 创建 `androidApp` 和 `iosApp` 两个平台入口
- 共享应用初始化逻辑
- 平台特定的权限处理

---

## 4. 详细技术方案

### 4.1 项目结构重构

```
Hearable Music Player (KMP)/
├── shared/                          # 共享代码
│   ├── core-domain/                # 领域层 (完全共享)
│   ├── core-data/                  # 数据层 (大部分共享)
│   └── core-ui/                    # UI 层 (Compose Multiplatform)
├── androidApp/                      # Android 平台
│   ├── core-player-android/        # Android 播放实现
│   └── app/                        # Android 入口
├── iosApp/                          # iOS 平台
│   ├── core-player-ios/            # iOS 播放实现
│   └── ios/                        # iOS 入口 (Swift/SwiftUI)
└── buildSrc/                       # 构建配置
```

### 4.2 关键技术选型

| 功能 | 技术选型 | 理由 |
|------|---------|------|
| 共享 UI | Compose Multiplatform | 与现有 Jetpack Compose 最接近 |
| 导航 | Voyager | 简单，学习成本低 |
| DI | Koin | 轻量，KMP 原生支持 |
| 数据库 | SQLDelight | 类型安全，成熟稳定 |
| 网络 | Ktor Client | Kotlin 原生，KMP 支持 |
| JSON | kotlinx.serialization | Kotlin 原生，性能好 |
| 图片 | Coil 3.0+ | 支持 KMP，API 熟悉 |

### 4.3 依赖注入迁移 (Hilt → Koin)

**当前 (Hilt)**:
```kotlin
@HiltViewModel
class PlayControlViewModel @Inject constructor(
    private val musicController: MusicController
) : ViewModel()
```

**迁移后 (Koin)**:
```kotlin
class PlayControlViewModel(
    private val musicController: MusicController
) : ViewModel()

// Koin 模块
val viewModelModule = module {
    viewModelOf(::PlayControlViewModel)
}
```

### 4.4 数据库迁移 (Room → SQLDelight)

**Room 实体**:
```kotlin
@Entity
data class Music(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String
)
```

**SQLDelight**:
```sql
-- Music.sq
CREATE TABLE music (
    id INTEGER PRIMARY KEY NOT NULL,
    title TEXT NOT NULL,
    artist TEXT NOT NULL
);

insertMusic:
INSERT INTO music (id, title, artist)
VALUES (?, ?, ?);

getAllMusic:
SELECT * FROM music;
```

### 4.5 播放器 expect/actual 设计

```kotlin
// shared 模块 - expect 接口
expect class MusicPlayer {
    fun play(music: Music)
    fun pause()
    fun seekTo(position: Long)
    val isPlaying: StateFlow<Boolean>
    val currentPosition: StateFlow<Long>
}

// androidApp 模块 - actual 实现
actual class MusicPlayer {
    private val exoPlayer: ExoPlayer
    // Media3 实现
}

// iosApp 模块 - actual 实现
actual class MusicPlayer {
    private val avPlayer: AVPlayer
    // AVFoundation 实现
}
```

---

## 5. 实施路线图

### 阶段 1: 基础设施搭建 (2-3 周)

**目标**: 建立 KMP 项目结构

- [ ] 配置 Gradle KMP 插件
- [ ] 创建 shared 模块基础结构
- [ ] 配置 iOS 目标 (需要 macOS)
- [ ] 迁移 core-domain 为共享模块
- [ ] 搭建基础 CI/CD

### 阶段 2: 数据层迁移 (3-4 周)

**目标**: 完成 core-data 模块 KMP 化

- [ ] 引入 SQLDelight，迁移数据库 schema
- [ ] 迁移 Repository 实现
- [ ] 引入 Koin，替换 Hilt
- [ ] 迁移网络层到 Ktor
- [ ] 替换 Gson 为 kotlinx.serialization
- [ ] 实现 ID3 解析的 expect/actual
- [ ] 实现拼音排序的 KMP 方案

### 阶段 3: UI 层迁移 (4-5 周)

**目标**: 完成 feature-ui 模块迁移

- [ ] 引入 Compose Multiplatform
- [ ] 迁移主题系统
- [ ] 迁移基础 UI 组件
- [ ] 引入 Voyager 导航
- [ ] 迁移页面组件
- [ ] 集成 Coil 3.0
- [ ] 实现调色板的 KMP 方案

### 阶段 4: 播放器迁移 (5-6 周)

**目标**: 实现跨平台播放器

- [ ] 设计播放器 expect 接口
- [ ] 重构 Android 播放器实现
- [ ] 实现 iOS AVFoundation 播放器
- [ ] 实现后台播放 (两端)
- [ ] 实现通知控制 (两端)
- [ ] 实现音频效果 (两端)

### 阶段 5: 平台集成与测试 (3-4 周)

**目标**: 完成两端集成和测试

- [ ] Android 端完整集成测试
- [ ] iOS 端完整集成测试
- [ ] 性能优化
- [ ] Bug 修复
- [ ] 文档更新

**总预计时间**: 17-22 周 (约 4-5 个月)

---

## 6. 风险评估

### 6.1 技术风险

| 风险 | 概率 | 影响 | 缓解措施 |
|------|------|------|---------|
| iOS 播放器复杂度高 | 高 | 严重 | 提前招聘/培训 iOS 开发者，考虑使用第三方库 |
| Compose Multiplatform 成熟度 | 中 | 中等 | 密切关注版本更新，准备回退方案 |
| 性能问题 (iOS) | 中 | 中等 | 早期进行性能测试，必要时平台特定优化 |
| 第三方库缺失 | 中 | 中等 | 提前调研替代库，准备自行实现方案 |

### 6.2 资源风险

**人员需求**:
- 1 名 Kotlin/KMP 专家
- 1 名 iOS/Swift 开发者
- 1 名 Android 开发者 (可兼任)

**设备需求**:
- macOS 开发机 (必需)
- iOS 测试设备
- Android 测试设备

### 6.3 时间风险

- 学习曲线: 团队需要学习 KMP、Compose Multiplatform、Koin 等新技术
- 平台差异: iOS 平台的特殊性可能需要更多时间
- 建议: 预留 20-30% 的缓冲时间

---

## 7. 成本效益分析

### 7.1 开发成本

| 项目 | 纯 Android | KMP (Android+iOS) | 增量成本 |
|------|-----------|-------------------|---------|
| 初始开发 | 已完成 | - | - |
| KMP 重构 | - | 4-5 个月 | 高 |
| iOS 新功能 | - | 包含 | - |
| 后续维护 | 1x | 1.2-1.5x | 中 |

### 7.2 收益

✅ **代码复用**: 预计 60-70% 代码可共享  
✅ **双平台覆盖**: 同时拥有 Android 和 iOS 版本  
✅ **技术现代化**: 使用最新的 KMP 技术栈  
✅ **统一用户体验**: 两端 UI/UX 保持一致  
✅ **长期维护成本降低**: 功能只需实现一次  

### 7.3 替代方案对比

| 方案 | 优点 | 缺点 | 推荐度 |
|------|------|------|--------|
| **KMP + Compose Multiplatform** | 代码复用高，技术栈统一 | 初始成本高，需要 iOS  expertise | ⭐⭐⭐⭐ |
| **Flutter** | 成熟，生态好 | 需要重写所有代码，技术栈变化大 | ⭐⭐⭐ |
| **React Native** | 生态丰富 | 需要重写，性能稍差 | ⭐⭐ |
| **原生 iOS + 共享逻辑** | 性能最优，风险低 | 代码复用较少 | ⭐⭐⭐⭐ |
| **维持现状** | 无需改动 | 只有 Android 版本 | ⭐⭐ |

---

## 8. 推荐方案

### 8.1 总体建议

**✅ 推荐进行 KMP 适配**，但建议采用渐进式策略：

### 8.2 分阶段实施建议

#### 第一阶段: 最小可行性验证 (1-2 周)

1. 创建简化的 KMP 原型
2. 验证 core-domain 可迁移性
3. 验证 Compose Multiplatform 基础 UI
4. 做关键技术决策 (数据库、DI 等)

#### 第二阶段: 核心功能 KMP 化 (不包含播放器)

1. 迁移 core-domain 和 core-data (除播放器外)
2. 迁移 UI 层 (除播放器页面外)
3. Android 端先跑通
4. 这阶段可以不涉及 iOS

#### 第三阶段: iOS 版本开发

1. 实现 iOS 播放器
2. iOS 端集成
3. 双平台测试

### 8.3 关键技术决策建议

| 决策项 | 建议 | 理由 |
|--------|------|------|
| 数据库 | SQLDelight | 成熟稳定，类型安全 |
| 导航 | Voyager | 简单易用，适合项目规模 |
| DI | Koin | 轻量，KMP 原生 |
| 网络 | Ktor | Kotlin 原生 |
| UI 框架 | Compose Multiplatform | 与现有代码最接近 |

---

## 9. 结论

### 9.1 可行性总结

**总体评估: ✅ 技术可行，但需要充足资源**

**有利因素**:
- ✅ 项目已采用模块化架构
- ✅ 代码 100% Kotlin
- ✅ Clean Architecture 分层清晰
- ✅ Compose 可以平滑迁移到 Compose Multiplatform

**挑战因素**:
- ⚠️ 播放器需要完全重写 iOS 版本
- ⚠️ 多个 Android 专属库需要替换
- ⚠️ 需要 iOS 开发 expertise
- ⚠️ 初始投入成本较高

### 9.2 最终建议

1. **如果目标是快速上线 iOS 版本**:
   - 考虑先使用 **原生 iOS + 共享核心逻辑** 的方案
   - 或者评估 Flutter/React Native

2. **如果看重长期技术栈统一**:
   - **推荐 KMP 方案**，但需要:
     - 预留充足的时间 (4-5 个月)
     - 配备 iOS 开发人员
     - 采用渐进式迁移策略

3. **如果资源有限**:
   - 可以先进行 **部分 KMP 化** (core-domain + core-data)
   - 为将来的全 KMP 做准备

---

## 10. 参考资源

### KMP 官方资源
- [Kotlin Multiplatform 官方文档](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform 官方文档](https://www.jetbrains.com/lp/compose-multiplatform/)
- [SQLDelight 文档](https://cashapp.github.io/sqldelight/)
- [Koin 文档](https://insert-koin.io/)

### 示例项目
- [Now in Android (KMP 分支)](https://github.com/android/nowinandroid)
- [Tivi (KMP 版本)](https://github.com/chrisbanes/tivi)
- [People In Space](https://github.com/joreilly/PeopleInSpace)

---

**报告生成时间**: 2026-04-10  
**报告版本**: 1.0
