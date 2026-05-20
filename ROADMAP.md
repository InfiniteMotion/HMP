# Hearable Music Player 项目演进路线

**文档说明**：本文件为**功能状态与版本历史的单一事实来源**。README 中的功能列表与本文件保持一致；完整变更日志仅在此维护。版本号格式与发版流程遵循 [docs/VERSIONING.md](docs/VERSIONING.md)。其他文档见 [docs/README.md](docs/README.md)。

## 📜 项目历史与版本演进

### v1.0 (2025-03-31)
- 项目初始化
- 创建基础项目结构
- 配置Git仓库

### v2.0 (2025-04-23)
- 实现播放列表功能
- 添加基础播放控制功能
- 完成第一个可运行版本

### v3.0 (2025-05-06)
- 更新README文档
- 优化MusicViewModel逻辑
- 完善播放控制功能

### v4.0 (2025-05-06)
- 重构代码结构
- 优化播放逻辑
- 提升应用稳定性

### v5.0 (2025-11-20)
- 模块化架构迁移完成
- 实现Hilt依赖注入
- 升级数据库版本支持分页查询
- 迁移到Media3 MediaSession
- 优化播放控制流程

### v5.2 (2025-12-03)
- 添加触觉反馈增强用户体验
- 优化播放页面UI及交互体验
- 更新状态沉浸并优化用户播放界面
- 统一调整UI组件颜色适配主题色彩体系
- 修复播放进度调整失败的漏洞
- 添加音频效果屏幕并重构用户界面
- 引入TabScreen模板
- 实现动态主题设置
- 添加按艺术家页面
- 添加自定义主题和AI功能页面
- 添加主题模式切换功能

### v5.3 (2025-12-30)
- 实现多 AI 服务商支持（DeepSeek、OpenAI、Claude、通义千问、文心一言）
- 添加 AI 服务商配置界面，支持自由切换
- 实现 API 密钥加密存储机制
- 添加 API 连接测试功能
- 实现每日推荐刷新策略系统：
  - 支持按时间刷新（自定义小时数）
  - 支持按启动次数刷新
  - 预留智能刷新接口
- 添加每日推荐持久化存储，重启后保持同一首歌
- 优化 AI 批量处理流程，支持暂停、继续、取消操作
- 添加处理进度显示和结果统计

### v5.3.1 (2026-01-08)
- 添加下一阶段架构优化方案文档
- 优化音乐列表布局和交互体验
- 重构引导页和主界面，添加动画和多步骤流程
- 统一页面路由常量管理，调整导航路由和界面排版
- 添加动画效果和优化组件交互

### v5.4 (2026-01-14)
- 实现 Hilt 导航组件集成与音乐控制器重构
- 重构项目模块依赖关系与模型迁移
- 重构并整合域模型
- 统一屏幕布局样式并优化导航逻辑
- 重构列表页面，新增多样化播放列表展示组件
- 优化用户界面和听歌时长热力图展示
- 优化多个页面的组件拆分和状态管理
- 修复音乐列表快速滑动时专辑封面/头像占位符导致的崩溃
- 更新文档

### v5.5 (2026-02-05)
- 实现 UI 字符串国际化并优化组件样式
- 添加动态背景风格选择与改进动画效果
- 统一签名配置，支持无缝安装 APK
- 优化界面组件和交互细节
- 替换智能歌词组件为高级歌词组件，支持歌词显示参数配置及管理
- 移除播放模式功能相关代码

### v5.6 (2026-02-24)
- 重构歌曲详情页面并新增详情视图模型
- 新增独立歌词页面及其设置面板
- 重构组件结构并移除旧音效组件
- 添加智能播放列表生成算法及 UI 支持
- 调整 domain 层包结构
- 优化底部导航和分页指示组件及按钮样式
- 优化音乐库界面及播放列表组件
- 优化播放器界面组件及空状态处理
- 重构主导航和用户界面布局

### v5.7 (2026-03-17)
- **播放列表管理补全**：用户自定义播放列表全流程支持
  - 数据/领域层：getAllPlaylists、重命名、按 ID 删除（仅自定义列表）、列表内单曲排序（reorderPlaylistItems）
  - UI：播放列表页「用户自定义播放列表」区块与入口、新建/重命名/删除（含确认）、列表内拖拽排序、从播放列表移除单曲

### v5.8 (2026-04-01)
- **架构升级与UI优化**：
  - 迁移到 Navigation 3 和 Gradle 9.0
  - 重构导航系统为集中式路由管理
  - 新增专辑页面及功能
  - 添加毛玻璃效果相关设置和优化弹窗功能
  - 优化音乐列表和消息提示的UI布局和交互
  - 添加对话框管理功能并优化UI交互
  - 简化应用名称与开启代码混淆
  - 修复 Navigation3 迁移相关问题，统一配置页面动画效果

### v5.9 (2026-04-17)
- **代码组织与播放器重构**：
  - 拆分 PlayControlViewModel 为多个专用 ViewModel（PlaybackViewModel、PlaylistQueueViewModel等）
  - 重构播放器界面结构，优化播放控制逻辑
  - 按功能模块重新组织包结构，提升代码可维护性
  - 统一空状态和加载状态的UI样式
  - 移除视图模型默认参数并统一依赖注入方式
  - 实现音乐详情弹窗的分享功能（支持分享音频文件）
  - 优化「下一首播放」逻辑：若歌曲已存在于播放列表，先移除再插入到下一首位置

### v5.10 (2026-05-09)
- **跨平台架构迁移**：
  - 实现Kotlin Multiplatform Mobile (KMM) 架构
  - 迁移core-domain和core-data层到shared模块
  - 配置CocoaPods集成，支持iOS平台
  - 创建iOS平台特定实现（11个expect/actual声明）
  - 完成iOS应用的构建和基本UI显示
- **iOS 核心功能**：
  - 播放引擎（AVPlayer + MusicPlayerController 单例 + AudioSessionManager）
  - 锁屏控制与远程命令（NowPlayingInfoManager + RemoteCommandManager）
  - Live Activity 与灵动岛（LiveActivityManager + HMPMediaSession 协调器）
  - 音乐扫描（DeviceMusicScanner + MusicTagParser Bridge）
  - 音乐仓库完整实现（MusicRepositoryImpl：扫描/增量同步/标签/AI/播放历史/分析）
  - 8个 ViewModel 实现完毕（Library/Search/SongDetail/Playlist/Settings/AudioEffect/Recommendation/Dialog）
  - SwiftUI 界面大规模实现：音乐库/播放器/播放列表/设置四大模块
- **发布流程重构**：
  - 版本号集中管理到 `gradle.properties`（`hmp.versionCode` / `hmp.versionName`）
  - 新增 Gradle release 任务（`releaseAndroid` / `releaseIos` / `releaseStorybook` / `release`）
  - Release 构建启用代码混淆和资源压缩
  - 图标加载改为 classpath 资源读取，不依赖 Android Context
- **CI/CD 自动发布**：
  - 新增 `.github/workflows/release.yml`，PR 合并到 master 时自动构建并发布 GitHub Release
  - 基于 git tag 自动生成 changelog
  - 自动构建并部署 Storybook 到 GitHub Pages
  - 移除旧的 `deploy-storybook.yml` 工作流

### v6.10 (2026-05-20)
- **桌面端平台** (Compose Multiplatform)：
  - 新模块 `desktop:app` / `desktop:core-player` / `desktop:feature-ui`
  - 自研音频引擎（FFmpeg 解码 + JNA 绑定）
  - 响应式布局系统（Compact/Expanded 模式、多面板导航）
  - NavigationRail + BottomFusionBar 自适应导航组件
  - 无边框窗口（标题栏、拖拽、圆角阴影、主题集成）
  - 键盘快捷键支持
  - 动态背景重构与主题系统优化
  - 冷启动优化（同步预加载、JNA 预热）
- **Android 增强**：
  - BottomFusionBar 组件（底部融合栏替代 MiniPlayerBar）
  - 动态背景取色算法重构
- **构建与 CI**：
  - `settings.gradle.kts` 条件化模块包含（`HMP_BUILD_TARGET` 环境变量）
  - CI 自动构建桌面三平台安装包（macOS DMG / Windows MSI / Linux DEB+AppImage）
  - GitHub Release 同发 Android + Desktop 六种产物

## 🛠️ 关键技术演进

### 架构演进
1. **单体架构** (v1.0-v4.0)
   - 传统Android项目结构
   - 所有代码集中在app模块

2. **模块化架构** (v5.0-v5.9)
   - 划分为core-data、core-domain、core-player、feature-ui模块
   - 降低模块耦合度
   - 提升编译速度

3. **跨平台架构** (v5.10+)
   - 实现Kotlin Multiplatform Mobile (KMM) 架构
   - 共享core-domain和core-data层
   - 平台特定UI实现（Android: Jetpack Compose, iOS: SwiftUI）
   - 平台特定播放引擎（Android: Media3, iOS: AVFoundation）
   - Monorepo结构，统一版本管理

4. **桌面端平台** (v6.10+)
   - Compose Multiplatform 桌面端（JVM）
   - 自研音频引擎替代 Media3（桌面环境适配）

### 技术栈升级
1. **播放引擎**
   - 从ExoPlayer迁移到Media3 (Android)
   - 实现MediaSession统一控制 (Android)
   - 集成AVFoundation (iOS)
   - 实现NowPlayingInfoCenter (iOS)

2. **依赖注入**
   - 从Hilt迁移到Koin（跨平台支持）
   - 重构ViewModel初始化流程

3. **数据库**
   - 升级Room数据库版本 (Android)
   - 支持分页查询
   - 优化数据访问性能
   - 迁移到 Room KMP（跨平台，Android + iOS 共享）

4. **网络**
   - 从Retrofit+OkHttp迁移到Ktor Client（跨平台支持）
   - 实现多平台HTTP客户端

5. **UI框架**
   - 全面采用Jetpack Compose (Android)
   - 实现SwiftUI (iOS)
   - 实现动态主题切换
   - 优化无障碍支持

6. **跨平台开发**
   - 实现Kotlin Multiplatform Mobile (KMM)
   - 配置CocoaPods集成
   - 实现expect/actual平台特定代码
   - 共享核心业务逻辑

## 🎯 核心功能演进

### 已完成功能
- ✅ 本地音乐扫描与管理
- ✅ 完整播放控制功能
- ✅ 播放列表管理（含用户自定义列表的创建/重命名/删除/排序/从列表移除）
- ✅ 智能播放列表生成算法及 UI
- ✅ 音乐搜索功能
- ✅ 歌词显示和滚动
- ✅ 独立歌词页面及设置面板
- ✅ 睡眠定时功能
- ✅ AI音乐推荐（多服务商支持）
- ✅ 个性化用户主页
- ✅ 暗色/亮色主题自动切换
- ✅ 触觉反馈
- ✅ 音频效果调节
- ✅ 按艺术家分类浏览
- ✅ 自定义主题设置
- ✅ 每日推荐刷新策略配置
- ✅ AI 服务商管理和切换
- ✅ UI 字符串国际化
- ✅ 歌曲详情页与动态背景风格
- ✅ 专辑页面浏览
- ✅ 音乐详情弹窗分享功能（支持分享音频文件）
- ✅ Navigation 3 导航系统
- ✅ 代码混淆与包体积优化
- ✅ Kotlin Multiplatform Mobile (KMM) 架构
- ✅ iOS平台基础支持
- ✅ CocoaPods集成
- ✅ 跨平台数据层和业务逻辑
- ✅ iOS端音乐扫描功能
- ✅ iOS端播放控制功能
- ✅ iOS锁屏控制与Live Activity
- ✅ CI/CD自动发布（GitHub Actions）

### 计划中功能
- 🔄 桌面小组件
- 🔄 音乐标签编辑
- 🔄 性能优化
- 🔄 单元测试覆盖

## 📊 开发里程碑

### 阶段1：基础架构搭建 (2025-03 ~ 2025-04)
- 项目初始化
- 基础播放功能实现
- 数据库设计

### 阶段2：核心功能完善 (2025-04 ~ 2025-05)
- 播放列表管理
- 搜索功能
- 歌词显示

### 阶段3：架构升级 (2025-11 ~ 2025-12)
- 模块化重构
- Media3迁移
- Hilt依赖注入

### 阶段4：功能增强 (2025-12 ~ 2026-01)
- AI推荐功能
- 主题切换
- 用户体验优化

### 阶段5：质量保障 (2026-01 ~ 2026-02)
- 单元测试
- 性能优化
- 发布准备

### 阶段6：架构优化与体验提升 (2026-03 ~ 2026-04)
- Navigation 3 迁移与导航系统重构
- Gradle 9.0 升级
- ViewModel 职责拆分与代码组织优化
- 专辑页面功能
- 音乐分享功能
- UI 细节优化（毛玻璃效果、空状态、加载状态）

### 阶段7：跨平台架构实现 (2026-04 ~ 2026-05)
- Kotlin Multiplatform Mobile (KMM) 架构搭建
- 核心业务逻辑迁移到shared模块
- iOS平台基础功能实现
- CocoaPods集成与Xcode项目配置
- 跨平台数据层和业务逻辑验证
- iOS应用构建与运行测试
- iOS播放引擎与锁屏控制
- iOS Live Activity 与灵动岛
- iOS SwiftUI 界面大规模实现
- 发布流程重构与 CI/CD 自动发布

### 阶段8：iOS 功能补全与双平台对齐 (v6.x)
- iOS 存根实现替换（SecureStorageHelper / PinyinSortKey / BackupFileRepository）
- iOS 设置页面后端模拟→真实实现（AI/备份/音乐库/使用数据）
- iOS 真机验证（锁屏控制 + Live Activity）
- Repository 通用逻辑提取到 commonMain 共享基类
- 双平台功能对齐验证

## 🚀 未来发展方向

**产品边界**：坚持纯本地，不做在线/云同步、不引入账号、不做社交；仅保留用户自填 API 的 AI 推荐。

### v6 阶段：iOS 功能补全与双平台对齐
1. iOS 存根替换为真实实现（SecureStorageHelper / PinyinSortKey / BackupFileRepository）
2. iOS 设置页面后端模拟→真实 API（AI 配置/备份还原/音乐库管理/使用数据）
3. iOS 真机验证（锁屏控制 + Live Activity + 后台播放）
4. Repository 通用逻辑提取到 commonMain 共享基类
5. 双平台功能完整对齐验证

### 中期目标
1. 实现桌面小组件
2. 添加音乐标签编辑功能
3. 优化电池续航
4. 完善单元测试覆盖

### 长期目标
1. 发布到应用商店（可选）
2. 车载/穿戴、播客等扩展（可选）

---

**最后更新时间**: 2026-05-20
**当前版本**: v6.10.0

---

© 2026 Hearable Music Player | Developed by WLYB