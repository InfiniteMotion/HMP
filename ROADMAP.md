# Hearable Music Player 改进路线（可直接执行）

本文档为本项目（Hearable Music Player）的结构化改进路线，按阶段拆分为可直接执行的任务清单、步骤与验收标准。建议在小步快跑的节奏中滚动推进，每个阶段以合并到主干为目标，并保持回归测试通过。

目标
- 提升架构可维护性与可测试性
- 完善媒体播放体系与用户体验
- 优化数据可靠性、网络健壮性与性能
- 打通质量保障（测试、静态检查、CI/CD）与发布流程

范围
- 代码模块：`MainActivity`、`MusicApplication`、`MusicPlayService`、`MusicNotificationReceiver`、`DeepSeekAPI`、`MusicViewModel`、`PlayControlViewModel`、`MusicRepository`、`SettingsRepository`、`AppDatabase`、UI/Compose 组件等
- 技术栈：Kotlin、Jetpack Compose、AndroidX Media3、Room、DataStore、Retrofit+OkHttp、Gson/（或 Kotlin Serialization）、Jaudiotagger
- 权限与系统兼容：Android 12+ 前台服务、Android 13+ 通知与媒体读取权限

里程碑
- M1 架构增强与状态统一
- M2 Media3 服务完备与通知/外设控制
- M3 数据层迁移与健壮性
- M4 网络与序列化统一及缓存/重试
- M5 模块化重构
- M6 测试与质量保障
- M7 构建与发布优化
- M8 性能与稳定性
- M9 安全与隐私
- M10 UX、可访问性与国际化


阶段 1：基础架构增强（1–2 周）
方法细化
- 目标：通过依赖注入和统一状态通路提升可测试性与稳定性。
- 具体路径
  - 编辑 app/build.gradle.kts：应用 Hilt 插件，开启 kapt；保持与 libs.plugins 别名一致。
  - 标注 `MusicApplication` 为 @HiltAndroidApp；将数据库与网络初始化迁移到 Hilt Module。
  - 为 `MusicViewModel`、`PlayControlViewModel` 添加 @HiltViewModel 与 @Inject 构造；逐步移除 `ViewModelFactory`。
  - 提供 `AppDatabase`、DAO、`MusicRepository`、`SettingsRepository`、`DeepSeekAPI`、OkHttp/Retrofit、ExoPlayer 的 @Provides/@Singleton。
  - 在 `MusicPlayService` 注入 ExoPlayer 与 MediaSession。
- 操作要点
  - 保持与 libs.plugins 别名一致；启用 kapt 与 Hilt 插件。
  - 将重型初始化迁移至 Hilt Module，`MusicApplication` 轻量化。
  - `ViewModelFactory` 逐步淘汰，统一由 Hilt 构造。
  - 提供 Repository/Database/API/Player 的单例绑定，支持测试替换。
- 验收
  - 替换为 Fake 依赖后，`MusicViewModel` 测试可独立运行；应用启动与播放功能正常。
  - `MainActivity` 首次权限引导（媒体读取、通知）与设置项一致。
  - 版本兼容矩阵（Kotlin/Compose/AGP）记录并通过检查；初始化不阻塞冷启动。
1. 引入依赖注入（Hilt）
   - 任务
     - 在顶层与 `app/build.gradle.kts` 添加 Hilt 插件与依赖
     - 为 `MusicApplication` 添加 `@HiltAndroidApp`
     - 为 `ViewModel`（如 `MusicViewModel`、`PlayControlViewModel`）添加 `@HiltViewModel` 与构造 `@Inject`
     - 提供 `@Module`/`@InstallIn(SingletonComponent::class)` 的 Provider：`AppDatabase`、DAO、`MusicRepository`、`SettingsRepository`、`DeepSeekAPI`、`OkHttpClient`/`Retrofit`、`ExoPlayer`
     - 在 `MusicPlayService` 注入 `ExoPlayer` 与 `MediaSession`
   - 步骤
     - Gradle：应用 `com.google.dagger.hilt.android` 插件，添加 `kapt` 与 `hilt-android-compiler`
     - Application 注解并迁移初始化代码至 Hilt Module
   - 验收标准
     - 编译通过；应用可启动；关键依赖由 Hilt 管理且可在测试中替换

2. 统一状态管理（Kotlin Flow/StateFlow）
   - 任务
     - 统一 `MusicViewModel`、`PlayControlViewModel` 的 UI 状态为 `StateFlow`
     - 播放状态（进度、队列、模式、歌词行）统一经 Flow 暴露；UI 以 `collectAsState()` 订阅
   - 验收标准
     - UI 状态无竞态更新；服务端到 UI 的事件链路一致

3. 错误与结果规范
   - 任务
     - Repository 层使用 `Result<T>`/自定义错误类型统一异常处理；网络、DB、I/O 错误映射为前端可识别状态
   - 验收标准
     - 异常路径覆盖单元测试；UI 能显示友好错误与重试入口


阶段 2：Media3 播放服务完备（1–2 周）
方法细化
- 目标：完善后台播放能力与系统/外设交互。
- 具体路径
  - 在 `MusicPlayService` 构建 MediaSession 并绑定 ExoPlayer；调用 setMediaSession。
  - 配置 AudioAttributes 与音频焦点；处理耳机拔插、蓝牙、来电暂停。
  - 启动前台服务，使用通知渠道；通知支持播放/暂停/上一首/下一首。
  - 与 `PlayControlViewModel` 建立双向队列与状态同步（Flow）。
- 操作要点
  - 建立 `PlayControlViewModel` 与服务的双向同步（Flow/StateFlow）唯一真源。
  - 配置 AudioAttributes，处理音频焦点、来电、耳机拔插、蓝牙。
  - 前台通知动作映射到 `MusicNotificationReceiver` → `MusicPlayService`，统一命令模型。
- 验收
  - 蓝牙/耳机控制、系统打断场景表现正确；通知交互稳定。
  - Android 13+ 通知权限与 Android 12+ 前台服务限制下行为正确。
  - 播放事件写入 `PlaybackHistory`、`ListeningDuration`、`DailyMusicInfo`，具节流与批处理策略。
  - 歌词组件与播放进度联动对齐，缺失歌词有占位与提示。
1. MediaSession 与音频焦点
   - 任务
     - 在 `MusicPlayService` 创建并管理 `MediaSession`
     - 处理音频焦点、音量、耳机插拔、蓝牙控制与来电打断
   - 验收标准
     - 外设/系统事件下播放行为符合预期；无音频资源泄漏

2. 前台服务与通知
   - 任务
     - 启动前台服务并展示播放通知；支持播放/暂停/下一首/上一首、进度显示
   - 验收标准
     - Android 12+ 前台服务限制下稳定运行；通知交互正常

3. 队列与模式
   - 任务
     - 在 `PlayControlViewModel` 与 `MusicPlayService` 间明确队列同步协议；模式变更（顺序/随机/单曲）一致
   - 验收标准
     - 队列状态跨页面/重启保持一致；随机播放无重复/偏差问题


阶段 3：数据层与迁移（1 周）
方法细化
- 目标：安全升级数据库并提升大列表体验。
- 具体路径
  - 在 `AppDatabase` 增加 version 并实现 Migration；使用 Room.databaseBuilder(...).addMigrations(MIGRATION_1_2...)。
  - 列表页采用 Paging 3；本地扫描与 Jaudiotagger 运行在 Dispatchers.IO。
- 操作要点
  - 明确 Room version 与 Migration 脚本；测试升级与回滚。
  - 列表采用 Paging 3；扫描和标签解析运行在 Dispatchers.IO。
  - 重启后的会话恢复策略与数据一致性校验。
- 验收
  - 升级无数据丢失；大列表滚动流畅；迁移失败有降级/重建策略。
  - `SettingsRepository`（DataStore）具备备份/恢复策略，与 `res/xml/backup_rules.xml`、`data_extraction_rules.xml` 一致。
  - 扫描/批量写入采用分批提交与 I/O 调度，异常回滚与重试策略明确。
1. Room Schema 版本化与迁移
   - 任务
     - 在 `AppDatabase` 明确 `version` 与 `Migration`；制作从旧版本到新版本的迁移脚本
   - 验收标准
     - 升级路径可回归；无数据丢失；迁移失败有降级/重建策略

2. 大数据与分页
   - 任务
     - 音乐列表/历史记录采用 Paging 3；长耗时 I/O（如 Jaudiotagger 扫描）在 `Dispatchers.IO` 并批量提交
   - 验收标准
     - 列表滚动流畅；数据库压力可控；不阻塞主线程


阶段 4：网络与序列化统一（1 周）
方法细化
- 目标：统一序列化与网络策略，提升弱网体验。
- 具体路径
  - 保留 Gson（或切换至 Kotlin Serialization），项目内统一一种实现并移除冗余依赖。
  - OkHttp 设置超时、重试与缓存；为 `DeepSeekAPI` 增加失败重试与速率限制。
- 操作要点
  - 保留并统一一种序列化方案（Gson 或 Kotlin Serialization），移除冗余依赖。
  - OkHttp 设置超时、重试与缓存；必要拦截器支持 ETag/Cache-Control；弱网与离线策略明确。
  - `DeepSeekAPI` 增加失败分类、速率限制与本地降级方案；缓存过期策略。
- 验收
  - API 调用一致；离线/弱网场景体验提升；错误路径对 UI 友好且可重试。
  - 统一序列化栈后混淆白名单安全（反射组件如 Media3/Gson 校验）。
  - `DeepSeekAPI` 降级与缓存策略生效，避免高频限流。
1. 序列化栈统一
   - 任务
     - 在 Gson 与 Kotlin Serialization 二选一并全局统一（建议保留 Gson 或切换至 Kotlin Serialization，根据现有使用）
   - 验收标准
     - 所有 API 调用均使用同一序列化方案；移除冗余依赖

2. OkHttp 策略
   - 任务
     - 配置连接/读取/写入超时，添加重试与指数退避；启用缓存与网络拦截器（如 ETag/Cache-Control）
   - 验收标准
     - 异常与超时处理一致；离线/弱网场景体验提升

3. `DeepSeekAPI` 健壮性
   - 任务
     - 增加失败重试、速率限制、错误分类与降级策略；必要时增加结果缓存
   - 验收标准
     - 高频调用不触发限流；失败路径对 UI 友好且可重试


阶段 5：模块化重构（2–3 周）
方法细化
- 目标：降低耦合与编译时间，明确边界。
- 具体路径
  - 在 settings.gradle.kts 添加 include(":core-data", ":core-domain", ":core-player", ":feature-ui", ":app")。
  - 将 Room/Repository 移至 core-data；Use Cases 至 core-domain；Media3/Service 至 core-player；Compose 页面组件至 feature-ui。
  - `app` 仅依赖 feature-ui 与核心模块，避免横向耦合。
- 验收
  - 依赖清晰；编译时间下降；模块职责边界明确。
1. 模块划分
   - 任务
     - 新建模块：`core-data`（Room、Repository）、`core-domain`（Use Cases）、`core-player`（Media3、Service）、`feature-ui`（Compose 页面与组件）、`app`（入口）
   - 验收标准
     - 模块边界清晰；公共依赖通过 `core-*` 暴露；编译时间下降

2. 领域用例
   - 任务
     - 在 `core-domain` 增加用例（如 获取推荐、更新播放队列、写入历史）以隔离业务规则
   - 验收标准
     - `ViewModel` 仅编排用例与状态；业务逻辑集中于 domain 层


阶段 6：测试与质量保障（持续进行）
方法细化
- 目标：建立质量保障闭环。
- 具体路径
  - 单元测试：使用 JUnit + Turbine，为 `MusicRepository`、`SettingsRepository`、`MusicViewModel` 与 domain 用例编写测试；使用 CoroutineTestRule 控制调度器。
  - 仪器测试：覆盖 `MusicPlayService` 的通知交互与播放控制；Compose UI 测试使用 createAndroidComposeRule。
  - 静态检查：集成 detekt/ktlint，在本地与 CI 执行，阻断不合规 PR。
- 操作要点
  - 单元测试使用 JUnit + Turbine，控制协程调度器（CoroutineTestRule）。
  - 仪器测试覆盖 `MusicPlayService` 通知交互与播放控制；Compose UI 测试使用 `createAndroidComposeRule`。
  - 集成 detekt/ktlint，在本地与 CI 执行，阻断不合规 PR。
- 验收
  - 关键路径覆盖率 ≥ 70%；核心交互稳定；格式与规则检查通过。
  - 端到端用例覆盖：权限拒绝、弱网、通知交互、服务重启、扫描大数据、长时播放、迁移升级。
  - 验证播放事件→数据回写闭环；歌词与进度联动一致。
1. 单元测试
   - 任务
     - 为 `MusicRepository`、`SettingsRepository`、`MusicViewModel`、用例编写测试（使用 `CoroutineTestRule`、`Turbine`）
   - 验收标准
     - 关键路径覆盖率 ≥ 70%；失败路径与边界场景有测试

2. 仪器测试
   - 任务
     - 对 `MusicPlayService` 的播放/通知行为做仪器测试；对 Compose 屏幕进行 UI 测试（`createAndroidComposeRule`）
   - 验收标准
     - 核心交互稳定；回归用例可复现关键场景

3. 静态检查
   - 任务
     - 集成 `detekt`/`ktlint`；Gradle 任务在本地与 CI 执行
   - 验收标准
     - PR 必须通过格式与规则检查；违规阻断合并


阶段 7：构建与发布优化（1 周）
方法细化
- 目标：提升构建速度与发布质量。
- 具体路径
  - 启用 R8/ProGuard 与资源压缩；校验混淆白名单对 Gson/Media3 反射安全。
  - 开启 Gradle Build Cache、配置缓存与并行；审查 kapt/ksp 的使用并减少注解处理开销。
  - 建立 CI 流水线：编译 → 测试 → 静态检查 → 打包；对主仓分支启用 PR 检查。
- 验收
  - 构建时间与包体明显下降；产物稳定并可自动化生成。
  - Compose 编译器/Kotlin/AGP 版本兼容矩阵检查通过。
  - 混淆白名单专项校验（Media3/Gson/反射）通过。
1. 包体与混淆
   - 任务
     - 启用 R8/ProGuard；开启资源压缩与移除未使用资源；按需启用 ABI 分包
   - 验收标准
     - 发布包体缩减；无运行时反射/序列化被误混淆问题

2. 构建性能
   - 任务
     - 启用 Gradle Build Cache、配置缓存、并行；合理化 `ksp/kapt` 使用
   - 验收标准
     - 本地与 CI 构建时间明显下降

3. CI/CD
   - 任务
     - 配置至少 1 条构建流水线：编译→测试→静态检查→打包；支持分支 PR 检查
   - 验收标准
     - 每次提交自动出报告与构建工件；主干稳定


阶段 8：性能与稳定性（并行/按需）
1. 启动优化
   - 任务
     - 按需惰性初始化（移出 `MusicApplication` 重型逻辑到按需 Module）；跟踪启动耗时
   - 验收标准
     - 冷启动耗时下降；首屏可交互更快

2. I/O 与扫描
   - 任务
     - 优化本地音乐扫描队列与批处理；避免主线程阻塞；对 Jaudiotagger 异常进行健壮处理
   - 验收标准
     - 首次/增量扫描策略明确；重复与冲突处理；异常重试与黑名单机制。
     - 无卡顿与 ANR；I/O 调度与资源占用受控。

3. 崩溃与异常监控（可选）
   - 任务
     - 引入崩溃与性能监控方案（如 Firebase Crashlytics/Performance）
   - 验收标准
     - 关键崩溃有告警与堆栈；线上问题可定位


阶段 9：安全与隐私
1. 凭据与存储
   - 任务
     - 使用 `SecureStorage` 管理敏感信息（API Key、Token）；按生命周期定期轮换与失效控制
   - 验收标准
     - 敏感数据不落地明文；非必要不持久化。
     - API Key 生命周期与轮换策略明确；`SecureStorage` 管理与访问控制生效。

2. 网络安全
   - 任务
     - 启用 TLS；必要时配置证书固定与域名校验；清理不安全的网络请求
   - 验收标准
     - 无明文传输与中间人风险；网络错误可恢复。
     - 启用 TLS；必要时证书固定与域名校验通过专项检查。


阶段 10：UX、可访问性与国际化
1. Compose 可访问性
   - 任务
     - 为重要组件添加 `contentDescription`、语义标签；支持 TalkBack
   - 验收标准
     - 基本无障碍检查通过；关键交互对读屏友好

2. 主题与适配
   - 任务
     - 深浅色一致性；横竖屏与多尺寸布局优化
   - 验收标准
     - 常见设备适配良好；无布局溢出

3. 文案与多语言（按需）
   - 任务
     - 使用 `strings.xml` 管理文案；抽离硬编码
   - 验收标准
     - 语言切换正常；文案统一


执行节奏与角色
- 每阶段包含：设计评审 → 开发 → 自测 → 代码评审 → 合并 → 回归测试
- 角色：Android 开发（架构/实现/测试）、QA（用例/回归）、维护者（版本与发布）

度量与验收总则
- 覆盖率提升、构建时间下降、崩溃率下降、ANR 降低、包体缩减、网络错误率下降、用户交互稳定
- 每阶段结束必须产出：变更说明、风险与回滚策略、回归测试结果

附：关键改动参考点
- Application 层：`MusicApplication`（Hilt 初始化、懒加载）
- 服务层：`MusicPlayService`（`MediaSession`、通知、焦点）
- 视图层：`MainActivity`、`MusicViewModel`、`PlayControlViewModel`（状态统一、用例编排）
- 数据层：`AppDatabase`、`MusicRepository`、`SettingsRepository`（迁移与错误映射）
- 网络层：`DeepSeekAPI`（序列化与重试/缓存）
- UI 组件：`ui/components`（无障碍与适配）

全链路覆盖验收清单
- 音乐扫描：扫描→ID3 解析→`AppDatabase` 入库→`MusicRepository`→`MusicList` 展示；覆盖首次/增量、重复/冲突、异常重试与黑名单。
- 播放服务：选曲→`PlayControlViewModel` 队列→`MusicPlayService` 播放→`MediaSession` 通知→`MusicNotificationReceiver` 动作→状态回传 UI；同时写入 `PlaybackHistory`/`ListeningDuration`/`DailyMusicInfo`，具节流与批处理。
- 设置偏好：`SettingsRepository`（DataStore）→`MusicViewModel` 状态→UI；备份/恢复与备份规则一致；权限变化影响播放策略可追踪。
- AI 推荐：`DeepSeekAPI` 调用→缓存与降级→`MusicRepository`→UI；弱网与失败场景用户体验稳定。
- 安全合规：`SecureStorage` 管理凭据；TLS/证书固定/域名校验；无明文持久化。
- 构建发布：R8/资源压缩/ABI；CI 流水线提供工件与报告；版本兼容矩阵稳定。
- 质量保障：单元/仪器/UI/静态检查通过；端到端场景（权限拒绝、弱网、通知交互、服务重启、扫描大数据、长时播放、迁移升级）可回归。

建议工作清单（可勾选）
- [x] 阶段 1 完成：Hilt 引入与状态统一
- [x] 阶段 2 完成：MediaSession、前台服务与通知
- [x] 阶段 3 完成：Room 迁移与 Paging
- [x] 阶段 4 完成：序列化统一与网络策略
- [ ] 阶段 5 完成：模块化与领域用例
- [ ] 阶段 6 完成：测试与静态检查
- [ ] 阶段 7 完成：构建与发布优化
- [ ] 阶段 8 完成：性能与稳定性
- [ ] 阶段 9 完成：安全与隐私
- [ ] 阶段 10 完成：UX、无障碍与国际化
