import SwiftUI

// MARK: - 主题模式

enum ThemeMode: String, CaseIterable, Identifiable {
    case system
    case light
    case dark

    var id: String { rawValue }
}

// MARK: - 全局主题状态

@Observable
class HMPTheme {
    var mode: ThemeMode = .system {
        didSet { if oldValue != mode { persistMode() } }
    }

    /// 系统当前的 colorScheme，由 ContentView 注入
    var systemColorScheme: ColorScheme = .light

    /// 当前是否为深色
    var isDark: Bool {
        switch mode {
        case .system:
            return systemColorScheme == .dark
        case .light:
            return false
        case .dark:
            return true
        }
    }

    init() {
        loadMode()
    }

    private func loadMode() {
        let saved = UserDefaults.standard.string(forKey: "theme_mode")
        if let saved, let parsed = ThemeMode(rawValue: saved) {
            self.mode = parsed
        }
    }

    private func persistMode() {
        UserDefaults.standard.set(mode.rawValue, forKey: "theme_mode")
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

// MARK: - View 扩展

extension View {
    func hmpTheme(_ theme: HMPTheme? = nil) -> some View {
        environment(theme ?? HMPTheme())
    }
}
