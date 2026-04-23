import SwiftUI

// MARK: - 主题环境对象

/// 主题模式
enum ThemeMode: String, CaseIterable, Identifiable {
    case system
    case light
    case dark

    var id: String { rawValue }
}

/// 全局主题状态
@Observable
class HMPTheme {
    var mode: ThemeMode = .system
    var useDynamicColor: Bool = false

    /// 当前是否为深色
    var isDark: Bool {
        switch mode {
        case .system:
            return ColorScheme.isDarkMode
        case .light:
            return false
        case .dark:
            return true
        }
    }

    // MARK: - 颜色快捷方式
    var primary: Color { isDark ? ColorTokens.darkPrimary : ColorTokens.lightPrimary }
    var onPrimary: Color { isDark ? ColorTokens.darkOnPrimary : ColorTokens.lightOnPrimary }
    var primaryContainer: Color { isDark ? ColorTokens.darkPrimaryContainer : ColorTokens.lightPrimaryContainer }
    var secondary: Color { isDark ? ColorTokens.darkSecondary : ColorTokens.lightSecondary }
    var background: Color { isDark ? ColorTokens.darkBackground : ColorTokens.lightBackground }
    var surface: Color { isDark ? ColorTokens.darkSurface : ColorTokens.lightSurface }
    var text: Color { isDark ? ColorTokens.textDark : ColorTokens.textLight }
    var secondaryText: Color { isDark ? ColorTokens.secondaryTextDark : ColorTokens.secondaryTextLight }
    var divider: Color { isDark ? ColorTokens.dividerDark : ColorTokens.dividerLight }
    var cardBackground: Color { isDark ? ColorTokens.cardDark : ColorTokens.cardLight }
    var tertiary: Color { isDark ? ColorTokens.darkSecondary : ColorTokens.lightPrimaryContainer }
}

// MARK: - ColorScheme 辅助
private extension ColorScheme {
    static var isDarkMode: Bool {
        UITraitCollection.current.userInterfaceStyle == .dark
    }
}

// MARK: - View 扩展
extension View {
    /// 应用 HMP 品牌主题
    func hmpTheme(_ theme: HMPTheme? = nil) -> some View {
        environment(theme ?? HMPTheme())
    }
}
