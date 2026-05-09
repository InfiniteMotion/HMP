package com.hmp.storybook.navigation

/**
 * Storybook 路由定义
 */
sealed interface StorybookRoute {
    val path: String

    // 设计系统
    data object ColorPalette : StorybookRoute {
        override val path = "design/colors"
    }

    data object Typography : StorybookRoute {
        override val path = "design/typography"
    }

    data object Animation : StorybookRoute {
        override val path = "design/animation"
    }

    // 组件库
    data object MiniPlayerBar : StorybookRoute {
        override val path = "components/mini-player"
    }

    data object MusicRow : StorybookRoute {
        override val path = "components/music-row"
    }

    data object DailyHeroCard : StorybookRoute {
        override val path = "components/daily-hero"
    }

    data object AlbumCover : StorybookRoute {
        override val path = "components/album-cover"
    }

    data object CapsuleButton : StorybookRoute {
        override val path = "components/capsule-button"
    }

    data object Avatar : StorybookRoute {
        override val path = "components/avatar"
    }

    // 页面预览
    data object HomeScreen : StorybookRoute {
        override val path = "screens/home"
    }

    data object PlayerScreen : StorybookRoute {
        override val path = "screens/player"
    }

    data object PlaylistScreen : StorybookRoute {
        override val path = "screens/playlist"
    }

    data object SettingsScreen : StorybookRoute {
        override val path = "screens/settings"
    }

    // 技术架构
    data object Architecture : StorybookRoute {
        override val path = "architecture"
    }

    // 产品
    data object ProductJourney : StorybookRoute {
        override val path = "product/journey"
    }

    data object DesignPhilosophy : StorybookRoute {
        override val path = "product/design-philosophy"
    }

    // 首页
    data object Home : StorybookRoute {
        override val path = ""
    }
}

/**
 * 侧边栏导航分组
 */
data class NavGroup(
    val titleKey: (com.hmp.storybook.theme.AppLanguage) -> String,
    val routes: List<Pair<StorybookRoute, (com.hmp.storybook.theme.AppLanguage) -> String>>,
)

val navGroups = listOf(
    // ---- 产品概览 ----
    NavGroup(
        titleKey = { if (it == com.hmp.storybook.theme.AppLanguage.ZH) "产品概览" else "Product Overview" },
        routes = listOf(
            StorybookRoute.ProductJourney to { com.hmp.storybook.i18n.Strings.productJourney(it) },
            StorybookRoute.DesignPhilosophy to { com.hmp.storybook.i18n.Strings.designPhilosophy(it) },
        ),
    ),
    // ---- 技术文档 ----
    NavGroup(
        titleKey = { com.hmp.storybook.i18n.Strings.designSystem(it) },
        routes = listOf(
            StorybookRoute.ColorPalette to { com.hmp.storybook.i18n.Strings.colorPalette(it) },
            StorybookRoute.Typography to { com.hmp.storybook.i18n.Strings.typography(it) },
            StorybookRoute.Animation to { com.hmp.storybook.i18n.Strings.animation(it) },
        ),
    ),
    NavGroup(
        titleKey = { com.hmp.storybook.i18n.Strings.components(it) },
        routes = listOf(
            StorybookRoute.MiniPlayerBar to { com.hmp.storybook.i18n.Strings.miniPlayerBar(it) },
            StorybookRoute.MusicRow to { com.hmp.storybook.i18n.Strings.musicRow(it) },
            StorybookRoute.DailyHeroCard to { com.hmp.storybook.i18n.Strings.dailyHeroCard(it) },
            StorybookRoute.AlbumCover to { com.hmp.storybook.i18n.Strings.albumCover(it) },
            StorybookRoute.CapsuleButton to { com.hmp.storybook.i18n.Strings.capsuleButton(it) },
            StorybookRoute.Avatar to { com.hmp.storybook.i18n.Strings.avatar(it) },
        ),
    ),
    NavGroup(
        titleKey = { com.hmp.storybook.i18n.Strings.screens(it) },
        routes = listOf(
            StorybookRoute.HomeScreen to { com.hmp.storybook.i18n.Strings.homeScreen(it) },
            StorybookRoute.PlayerScreen to { com.hmp.storybook.i18n.Strings.playerScreen(it) },
            StorybookRoute.PlaylistScreen to { com.hmp.storybook.i18n.Strings.playlistScreen(it) },
            StorybookRoute.SettingsScreen to { com.hmp.storybook.i18n.Strings.settingsScreen(it) },
        ),
    ),
    NavGroup(
        titleKey = { com.hmp.storybook.i18n.Strings.architecture(it) },
        routes = listOf(
            StorybookRoute.Architecture to { com.hmp.storybook.i18n.Strings.architecture(it) },
        ),
    ),
)
