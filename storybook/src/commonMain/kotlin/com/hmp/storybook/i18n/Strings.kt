package com.hmp.storybook.i18n

import com.hmp.storybook.theme.AppLanguage

/**
 * 中英双语文案
 */
object Strings {
    fun appTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "HMP Storybook"
        AppLanguage.EN -> "HMP Storybook"
    }

    fun designSystem(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "设计系统"
        AppLanguage.EN -> "Design System"
    }

    fun colorPalette(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "颜色面板"
        AppLanguage.EN -> "Color Palette"
    }

    fun typography(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "字体排版"
        AppLanguage.EN -> "Typography"
    }

    fun animation(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "动效系统"
        AppLanguage.EN -> "Motion"
    }

    fun components(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "组件库"
        AppLanguage.EN -> "Components"
    }

    fun screens(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "页面预览"
        AppLanguage.EN -> "Screens"
    }

    fun architecture(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "技术架构"
        AppLanguage.EN -> "Architecture"
    }

    fun miniPlayerBar(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "迷你播放器"
        AppLanguage.EN -> "Mini Player Bar"
    }

    fun musicRow(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "歌曲行"
        AppLanguage.EN -> "Music Row"
    }

    fun dailyHeroCard(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "每日推荐卡片"
        AppLanguage.EN -> "Daily Hero Card"
    }

    fun albumCover(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "专辑封面"
        AppLanguage.EN -> "Album Cover"
    }

    fun capsuleButton(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "胶囊按钮"
        AppLanguage.EN -> "Capsule Button"
    }

    fun avatar(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "头像"
        AppLanguage.EN -> "Avatar"
    }

    fun homeScreen(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "首页"
        AppLanguage.EN -> "Home Screen"
    }

    fun playerScreen(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "播放器页面"
        AppLanguage.EN -> "Player Screen"
    }

    fun playlistScreen(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "播放列表页"
        AppLanguage.EN -> "Playlist Screen"
    }

    fun settingsScreen(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "设置页面"
        AppLanguage.EN -> "Settings Screen"
    }

    fun description(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "描述"
        AppLanguage.EN -> "Description"
    }

    fun preview(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "预览"
        AppLanguage.EN -> "Preview"
    }

    fun sourceCode(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "源码"
        AppLanguage.EN -> "Source"
    }

    fun lightTheme(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "浅色主题"
        AppLanguage.EN -> "Light Theme"
    }

    fun darkTheme(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "深色主题"
        AppLanguage.EN -> "Dark Theme"
    }

    fun brandColors(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "品牌色"
        AppLanguage.EN -> "Brand Colors"
    }

    fun semanticColors(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "语义色"
        AppLanguage.EN -> "Semantic Colors"
    }

    fun fontScale(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "字体缩放"
        AppLanguage.EN -> "Font Scale"
    }

    fun duration(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "持续时间"
        AppLanguage.EN -> "Duration"
    }

    fun easing(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "缓动函数"
        AppLanguage.EN -> "Easing"
    }

    fun springConfig(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "弹簧配置"
        AppLanguage.EN -> "Spring Config"
    }

    fun overview(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "概览"
        AppLanguage.EN -> "Overview"
    }

    fun moduleStructure(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "模块结构"
        AppLanguage.EN -> "Module Structure"
    }

    fun techStack(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "技术栈"
        AppLanguage.EN -> "Tech Stack"
    }

    fun kmpPlatform(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "KMP 跨平台机制"
        AppLanguage.EN -> "KMP Cross-Platform"
    }

    fun hearableMusicPlayer(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "Hearable Music Player"
        AppLanguage.EN -> "Hearable Music Player"
    }

    fun subtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "一款现代化的跨平台本地音乐播放器"
        AppLanguage.EN -> "A modern cross-platform local music player"
    }

    fun switchLanguage(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "EN"
        AppLanguage.EN -> "中文"
    }

    fun colorPaletteDescription(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "HMP 的颜色系统基于 Material Design 3 色彩规范，以品牌蓝 (#002FA7) 和品牌红 (#C92C2C) 为核心，构建了完整的浅色/深色主题色板。"
        AppLanguage.EN -> "HMP's color system is based on Material Design 3 color specifications, built around brand blue (#002FA7) and brand red (#C92C2C), creating a complete light/dark theme palette."
    }

    fun typographyDescription(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "HMP 使用 HarmonyOS Sans 字体族，搭配 Material3 Typography 规范，定义了从 displayLarge (40pt) 到 labelSmall (11pt) 的完整排版体系。"
        AppLanguage.EN -> "HMP uses the HarmonyOS Sans font family with Material3 Typography specs, defining a complete type scale from displayLarge (40pt) to labelSmall (11pt)."
    }

    fun animationDescription(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "HMP 定义了四档动画持续时间（MICRO 200ms ~ BACKGROUND 3000ms）和三组缓动函数（EASE_IN_OUT / EASE_OUT / EASE_IN），以及 Spring 弹簧动画配置。"
        AppLanguage.EN -> "HMP defines four animation duration tiers (MICRO 200ms ~ BACKGROUND 3000ms), three easing curves (EASE_IN_OUT / EASE_OUT / EASE_IN), and Spring animation configs."
    }

    fun architectureDescription(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "HMP 采用 Kotlin Multiplatform Mobile (KMP) 架构，共享 domain + data 层，UI 和播放引擎保持平台原生实现。"
        AppLanguage.EN -> "HMP uses Kotlin Multiplatform Mobile (KMP) architecture, sharing domain + data layers, with platform-native UI and playback engines."
    }

    // ========== 新增组件变体名称 ==========

    fun tabScreen(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "TabScreen 标签页容器"
        AppLanguage.EN -> "TabScreen Container"
    }

    fun tabPageIndicator(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "TabPageIndicator 页面指示器"
        AppLanguage.EN -> "TabPageIndicator"
    }

    fun fixedMusicList(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "FixedMusicList 固定列表"
        AppLanguage.EN -> "FixedMusicList"
    }

    fun itemVariantFull(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "ItemVariant.Full 完整变体"
        AppLanguage.EN -> "ItemVariant.Full"
    }

    fun filledIconButton(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "FilledIconButton 填充图标按钮"
        AppLanguage.EN -> "FilledIconButton"
    }

    fun playerHeader(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "PlayerHeader 播放器头部"
        AppLanguage.EN -> "PlayerHeader"
    }

    fun musicInfo(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "MusicInfo 音乐信息"
        AppLanguage.EN -> "MusicInfo"
    }

    fun seekBar(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "SeekBar 进度条"
        AppLanguage.EN -> "SeekBar"
    }

    fun playbackControlsButtons(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "PlaybackControlsButtons 播放控制"
        AppLanguage.EN -> "PlaybackControlsButtons"
    }

    fun dotPager(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "DotPager 点状翻页器"
        AppLanguage.EN -> "DotPager"
    }

    fun subScreen(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "SubScreen 子页面包装"
        AppLanguage.EN -> "SubScreen Wrapper"
    }

    fun playlistHeader(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "PlaylistHeader 歌单头部"
        AppLanguage.EN -> "PlaylistHeader"
    }

    fun extendedFloatingActionButton(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "ExtendedFloatingActionButton 扩展悬浮按钮"
        AppLanguage.EN -> "ExtendedFloatingActionButton"
    }

    fun settingItem(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "SettingItem 设置项"
        AppLanguage.EN -> "SettingItem"
    }

    fun userScreen(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "用户页面"
        AppLanguage.EN -> "User Screen"
    }

    fun userCard(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "用户卡片"
        AppLanguage.EN -> "User Card"
    }

    fun listeningChart(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "收听图表"
        AppLanguage.EN -> "Listening Chart"
    }

    fun featureGrid(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "功能网格"
        AppLanguage.EN -> "Feature Grid"
    }

    // ========== 新增设置项名称 ==========

    fun darkMode(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "深色模式"
        AppLanguage.EN -> "Dark Mode"
    }

    fun themeColor(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "主题色"
        AppLanguage.EN -> "Theme Color"
    }

    fun blurEffect(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "毛玻璃效果"
        AppLanguage.EN -> "Blur Effect"
    }

    fun backgroundStyle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "背景风格"
        AppLanguage.EN -> "Background Style"
    }

    fun audioEffects(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "音频效果"
        AppLanguage.EN -> "Audio Effects"
    }

    fun equalizer(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "均衡器"
        AppLanguage.EN -> "Equalizer"
    }

    fun aiRecommendation(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "AI 推荐"
        AppLanguage.EN -> "AI Recommendation"
    }

    fun providerConfig(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "服务商配置"
        AppLanguage.EN -> "Provider Config"
    }

    fun backupRestore(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "备份与还原"
        AppLanguage.EN -> "Backup & Restore"
    }

    fun librarySettings(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "音乐库设置"
        AppLanguage.EN -> "Library Settings"
    }

    fun versionInfo(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "版本信息"
        AppLanguage.EN -> "Version Info"
    }

    // ========== 新增架构相关文案 ==========

    fun versionInfoTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "版本信息"
        AppLanguage.EN -> "Version Info"
    }

    fun databaseEntities(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "数据库实体"
        AppLanguage.EN -> "Database Entities"
    }

    fun useCaseList(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "UseCase 列表"
        AppLanguage.EN -> "UseCase List"
    }

    fun fiveLayerArchitecture(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "5 层架构"
        AppLanguage.EN -> "5-Layer Architecture"
    }

    fun uiLayer(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "UI 层 (平台原生)"
        AppLanguage.EN -> "UI Layer (Platform Native)"
    }

    fun viewModelLayer(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "ViewModel 层 (Koin + StateFlow)"
        AppLanguage.EN -> "ViewModel Layer (Koin + StateFlow)"
    }

    fun domainLayer(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "Domain 层 (shared)"
        AppLanguage.EN -> "Domain Layer (shared)"
    }

    fun dataLayer(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "Data 层 (shared)"
        AppLanguage.EN -> "Data Layer (shared)"
    }

    fun playbackEngine(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "播放引擎 (平台原生)"
        AppLanguage.EN -> "Playback Engine (Platform Native)"
    }

    fun appearance(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "外观"
        AppLanguage.EN -> "Appearance"
    }

    fun playback(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "播放"
        AppLanguage.EN -> "Playback"
    }

    fun data(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "数据"
        AppLanguage.EN -> "Data"
    }

    fun about(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "关于"
        AppLanguage.EN -> "About"
    }

    fun shuffle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "随机播放"
        AppLanguage.EN -> "Shuffle"
    }

    fun sequential(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "顺序播放"
        AppLanguage.EN -> "Sequential"
    }

    fun playAll(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "播放全部"
        AppLanguage.EN -> "Play All"
    }

    fun favorites(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "心动歌单"
        AppLanguage.EN -> "Favorites"
    }

    fun dailyPick(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "每日推荐"
        AppLanguage.EN -> "Daily Pick"
    }

    fun techInfo(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "技术信息"
        AppLanguage.EN -> "Tech Info"
    }

    fun lyrics(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "歌词"
        AppLanguage.EN -> "Lyrics"
    }

    fun playMode(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "播放模式"
        AppLanguage.EN -> "Play Mode"
    }

    fun favorite(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "收藏"
        AppLanguage.EN -> "Favorite"
    }

    fun like(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "心动"
        AppLanguage.EN -> "Like"
    }

    fun sleepTimer(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "定时"
        AppLanguage.EN -> "Sleep Timer"
    }

    fun queue(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "列表"
        AppLanguage.EN -> "Queue"
    }

    fun listeningStats(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "收听统计"
        AppLanguage.EN -> "Listening Stats"
    }

    fun themeCustomize(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "主题定制"
        AppLanguage.EN -> "Theme"
    }

    fun audioFx(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "音效"
        AppLanguage.EN -> "Audio FX"
    }

    fun aiService(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "AI 服务"
        AppLanguage.EN -> "AI Service"
    }

    fun settings(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "设置"
        AppLanguage.EN -> "Settings"
    }

    // ========== 产品页面文案 ==========

    fun productJourney(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "产品历程"
        AppLanguage.EN -> "Product Journey"
    }

    fun designPhilosophy(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "设计理念"
        AppLanguage.EN -> "Design Philosophy"
    }

    fun productPositioning(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "产品定位"
        AppLanguage.EN -> "Product Positioning"
    }

    fun productBoundary(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "产品边界"
        AppLanguage.EN -> "Product Boundary"
    }

    fun productJourneyDescription(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "从 v1.0 单体架构到 v5.10 KMP 跨平台，13 个月的渐进式演进历程"
        AppLanguage.EN -> "From v1.0 monolith to v5.10 KMP cross-platform, 13 months of progressive evolution"
    }

    fun designPhilosophyDescription(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "纯本地优先、平台原生 UI、渐进式演进、先设计后编码"
        AppLanguage.EN -> "Local-first, native UI, progressive evolution, design-before-code"
    }

    // 产品历程
    fun product(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "产品历程"
        AppLanguage.EN -> "Product Journey"
    }

    fun coreData(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "核心数据"
        AppLanguage.EN -> "Key Metrics"
    }

    fun versionTimeline(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "版本时间线"
        AppLanguage.EN -> "Version Timeline"
    }

    // 产品历程页
    fun architectureEvolution(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "架构演进"
        AppLanguage.EN -> "Architecture Evolution"
    }

    fun majorDecisions(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "重大技术决策"
        AppLanguage.EN -> "Major Technical Decisions"
    }

    fun kmpMigration(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "KMP 跨平台迁移"
        AppLanguage.EN -> "KMP Cross-Platform Migration"
    }

    fun keyNumbers(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "关键数字"
        AppLanguage.EN -> "Key Numbers"
    }

    fun futureRoadmap(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "未来规划"
        AppLanguage.EN -> "Future Roadmap"
    }

    // 设计理念页
    fun localFirst(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "纯本地优先"
        AppLanguage.EN -> "Local-First Philosophy"
    }

    fun nativeUI(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "平台原生 UI"
        AppLanguage.EN -> "Platform-Native UI"
    }

    fun progressiveEvolution(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "渐进式演进"
        AppLanguage.EN -> "Progressive Evolution"
    }

    fun designFirst(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "先设计后编码"
        AppLanguage.EN -> "Design Before Code"
    }

    fun aiRecommendationStrategy(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "AI 推荐策略"
        AppLanguage.EN -> "AI Recommendation Strategy"
    }

    fun dynamicTheme(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "动态主题系统"
        AppLanguage.EN -> "Dynamic Theme System"
    }

    fun visualLanguage(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "视觉语言"
        AppLanguage.EN -> "Visual Language"
    }
}
