# HMP 产品网页 — 文案稿

---

## Hero 区

**顶部标签**
HMP v6.10.0 · 活跃开发中

**标题**
Hearable Music Player

**副标题**
听见你的音乐

**按钮**
[下载] → 跳转到平台下载区
[GitHub] → 外链仓库

---

## 功能亮点

### 功能 1：沉浸氛围

**标签** 视觉体验

**标题** 不只听见，也感受到

**描述**
不只是播放页——整个应用都笼罩在随音乐实时变化的氛围光影中。HMP 从专辑封面提取色彩，生成持续流动的动态背景。底层叠加大半径模糊的慢速旋转封面，上层覆盖渐变遮罩确保文字可读。三种风格可选：流体极光、沉浸光斑、复古模糊。亮色与暗色模式下各有不同的氛围表达。

**技术标签** 色彩提取 · 流体极光 · 全应用沉浸 · 亮暗双模

---

### 功能 2：通透质感

**标签** UI 设计

**标题** 毛玻璃，不只是好看

**描述**
底部导航栏、迷你播放器、对话框——HMP 的核心交互区全部采用大圆角毛玻璃卡片，悬浮于动态背景之上。模糊半径、噪点纹理、透明度均可调节。Android 和桌面端使用 Haze 库，iOS 端使用原生 ultraThinMaterial。以极细边框代替阴影，玻璃本身已是空间层次的表达。

**技术标签** Haze · ultraThinMaterial · 可调节参数

---

### 功能 3：智能推荐

**标签** AI · 推荐

**标题** 大模型打标，标签驱动推荐

**描述**
HMP 通过大语言模型为每首歌自动生成标签——流派、情绪、场景、语言、年代。基于这些标签构建推荐系统，每日为你推荐值得一听的曲目。已集成 DeepSeek、OpenAI、Claude、通义千问、文心一言。API Key 由你提供，加密存储于设备本地，不收集任何数据。

**技术标签** DeepSeek · OpenAI · Claude · Ktor · 端侧加密

---

### 功能 4：自适应布局

**标签** 平台

**标题** 一套代码，适配所有屏幕

**描述**
业务逻辑和数据层通过 Kotlin Multiplatform 在三平台共享。Android 和 iOS 采用底部胶囊导航，桌面端在宽屏下自动切换为侧边 NavigationRail，窗口缩窄时退化为底部布局。UI 层各自遵循原生规范——Jetpack Compose、SwiftUI、Compose Desktop——不是 WebView 套壳。

**技术标签** Kotlin Multiplatform · Compose · SwiftUI · 响应式布局

---

## 平台 & 下载

### Android

**截图区域** [待补充截图]

**平台特点**
- Jetpack Compose + Material3
- Media3 ExoPlayer 播放引擎
- Haze 毛玻璃效果
- Android 13+ (API 33+)

**下载按钮**
下载 APK → GitHub Release 最新 APK 直链

---

### iOS

**截图区域** [待补充截图]

**平台特点**
- SwiftUI + Liquid Glass
- AVFoundation 播放引擎
- 原生 ultraThinMaterial
- 最低 iOS 16.0，需 Xcode 17.0+

**操作按钮**
查看构建指南 → 开发者文档链接
（iOS 无直链分发，需自行构建）

---

### Desktop

**截图区域** [待补充截图]

**平台特点**
- Compose Desktop
- FFmpeg 音频引擎
- 响应式窗口布局
- 系统托盘 · 全局快捷键

**下载按钮**
下载 Windows → GitHub Release 直链
下载 macOS → GitHub Release 直链

---

## 页脚

**链接**
首页 · 功能 · 平台 · GitHub · 文档

**版权**
HMP — Hearable Music Player · 纯本地 · 跨平台 · 开源

---

## 页面结构

两个页面，通过 Hero 按钮和页脚相互链接，不设固定导航栏。

- **首页** — Hero + 四大功能（同页下滑），Hero 按钮跳转下载页。
- **下载/预览页** — 产品预览 + 三平台下载卡片。
