# 鸿蒙原生迁移任务列表

## 迁移顺序

### 1. 项目配置和基础设置

| 任务编号 | 任务描述 | 涉及文件/目录 | 完成状态 |
|---------|----------|---------------|----------|
| 1.1 | 配置应用基本信息（包名、版本号等） | AppScope/app.json5 | ✅ |
| 1.2 | 配置入口模块信息 | entry/src/main/module.json5 | ✅ |
| 1.3 | 配置应用权限 | entry/src/main/module.json5 | ✅ |
| 1.4 | 迁移字体资源 | origin/feature-ui/src/main/res/font/ → entry/src/main/resources/base/media/ | ⏭️ |
| 1.5 | 迁移图片资源 | origin/feature-ui/src/main/res/drawable/ → entry/src/main/resources/base/media/ | ✅ |

### 2. 数据模型迁移

| 任务编号 | 任务描述 | 涉及文件/目录 | 完成状态 |
|---------|----------|---------------|----------|
| 2.1 | 迁移领域模型 | origin/core-domain/src/main/java/com/example/hearablemusicplayer/domain/model/ → entry/src/main/ets/model/domain/ | ✅ |
| 2.2 | 迁移数据库模型 | origin/core-data/src/main/java/com/example/hearablemusicplayer/data/database/ → entry/src/main/ets/model/database/ | ✅ |
| 2.3 | 配置鸿蒙数据库 | entry/src/main/ets/data/ | ✅ |

### 3. 播放器核心迁移

| 任务编号 | 任务描述 | 涉及文件/目录 | 完成状态 |
|---------|----------|---------------|----------|
| 3.1 | 创建音乐播放ServiceAbility | entry/src/main/ets/serviceability/MusicPlayService.ets | ⬜ |
| 3.2 | 实现鸿蒙音频API集成 | entry/src/main/ets/serviceability/MusicPlayService.ets | ⬜ |
| 3.3 | 实现播放控制逻辑 | entry/src/main/ets/serviceability/MusicPlayService.ets | ⬜ |
| 3.4 | 实现音频焦点管理 | entry/src/main/ets/serviceability/MusicPlayService.ets | ⬜ |

### 4. 业务逻辑迁移

| 任务编号 | 任务描述 | 涉及文件/目录 | 完成状态 |
|---------|----------|---------------|----------|
| 4.1 | 迁移MusicViewModel | origin/feature-ui/src/main/java/com/example/hearablemusicplayer/ui/viewmodel/MusicViewModel.kt → entry/src/main/ets/viewmodel/MusicViewModel.ets | ⬜ |
| 4.2 | 迁移PlayControlViewModel | origin/feature-ui/src/main/java/com/example/hearablemusicplayer/ui/viewmodel/PlayControlViewModel.kt → entry/src/main/ets/viewmodel/PlayControlViewModel.ets | ⬜ |
| 4.3 | 迁移数据仓库逻辑 | origin/core-data/src/main/java/com/example/hearablemusicplayer/data/repository/ → entry/src/main/ets/repository/ | ⬜ |

### 5. UI组件迁移

| 任务编号 | 任务描述 | 涉及文件/目录 | 完成状态 |
|---------|----------|---------------|----------|
| 5.1 | 迁移主题配置 | origin/feature-ui/src/main/java/com/example/hearablemusicplayer/ui/theme/ → entry/src/main/ets/theme/ | ⬜ |
| 5.2 | 迁移基础UI组件 | origin/feature-ui/src/main/java/com/example/hearablemusicplayer/ui/components/ → entry/src/main/ets/components/ | ⬜ |
| 5.3 | 迁移页面组件 - PlayerScreen | origin/feature-ui/src/main/java/com/example/hearablemusicplayer/ui/pages/PlayerScreen.kt → entry/src/main/ets/pages/PlayerPage.ets | ⬜ |
| 5.4 | 迁移页面组件 - HomeScreen | origin/feature-ui/src/main/java/com/example/hearablemusicplayer/ui/pages/HomeScreen.kt → entry/src/main/ets/pages/HomePage.ets | ⬜ |
| 5.5 | 迁移页面组件 - ListScreen | origin/feature-ui/src/main/java/com/example/hearablemusicplayer/ui/pages/ListScreen.kt → entry/src/main/ets/pages/ListPage.ets | ⬜ |
| 5.6 | 迁移页面组件 - UserScreen | origin/feature-ui/src/main/java/com/example/hearablemusicplayer/ui/pages/UserScreen.kt → entry/src/main/ets/pages/UserPage.ets | ⬜ |
| 5.7 | 迁移其他页面组件 | origin/feature-ui/src/main/java/com/example/hearablemusicplayer/ui/pages/ → entry/src/main/ets/pages/ | ⬜ |

### 6. 应用入口和导航

| 任务编号 | 任务描述 | 涉及文件/目录 | 完成状态 |
|---------|----------|---------------|----------|
| 6.1 | 配置应用导航 | entry/src/main/ets/router/ | ⬜ |
| 6.2 | 更新EntryAbility | entry/src/main/ets/entryability/EntryAbility.ets | ⬜ |
| 6.3 | 实现应用启动逻辑 | entry/src/main/ets/entryability/EntryAbility.ets | ⬜ |
| 6.4 | 实现权限申请逻辑 | entry/src/main/ets/entryability/EntryAbility.ets | ⬜ |

### 7. 测试和优化

| 任务编号 | 任务描述 | 涉及文件/目录 | 完成状态 |
|---------|----------|---------------|----------|
| 7.1 | 功能测试 - 播放控制 |  | ⬜ |
| 7.2 | 功能测试 - 音乐列表 |  | ⬜ |
| 7.3 | 功能测试 - 播放模式切换 |  | ⬜ |
| 7.4 | 性能优化 - 内存占用 |  | ⬜ |
| 7.5 | 性能优化 - 启动速度 |  | ⬜ |
| 7.6 | UI适配测试 |  | ⬜ |
| 7.7 | 兼容性测试 |  | ⬜ |

## 迁移完成标准

1. 所有核心功能正常工作
2. 应用可以正常安装和运行
3. 保持原有用户体验
4. 符合鸿蒙应用开发规范
5. 通过鸿蒙应用市场审核要求

## 注意事项

1. 迁移过程中保持代码整洁和可维护性
2. 充分利用鸿蒙原生API的优势
3. 注意跨平台差异，特别是权限管理和后台服务
4. 迁移完成后进行全面测试
5. 遵循鸿蒙应用开发最佳实践