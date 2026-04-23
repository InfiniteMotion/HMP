import SwiftUI

/// 字体令牌 - 与 Android TypographyTokens.kt 保持对等
/// sp -> pt 1:1 映射，字体族使用系统 SF Pro（HarmonyOS Sans 在 iOS 无对应，使用系统字体）
struct TypographyTokens {
    // MARK: - Display

    static let displayLarge = Font.system(size: 40, weight: .bold, design: .default)
    static let displayMedium = Font.system(size: 24, weight: .bold, design: .default)
    static let displaySmall = Font.system(size: 20, weight: .bold, design: .default)

    // MARK: - Headline

    static let headlineLarge = Font.system(size: 24, weight: .bold, design: .default)
    static let headlineMedium = Font.system(size: 18, weight: .bold, design: .default)
    static let headlineSmall = Font.system(size: 14, weight: .bold, design: .default)

    // MARK: - Title

    static let titleLarge = Font.system(size: 18, weight: .bold, design: .default)
    static let titleMedium = Font.system(size: 16, weight: .medium, design: .default)
    static let titleSmall = Font.system(size: 14, weight: .medium, design: .default)

    // MARK: - Body

    static let bodyLarge = Font.system(size: 16, weight: .regular, design: .default)
    static let bodyMedium = Font.system(size: 14, weight: .regular, design: .default)
    static let bodySmall = Font.system(size: 12, weight: .regular, design: .default)

    // MARK: - Label

    static let labelLarge = Font.system(size: 14, weight: .medium, design: .default)
    static let labelMedium = Font.system(size: 12, weight: .medium, design: .default)
    static let labelSmall = Font.system(size: 11, weight: .medium, design: .default)

    // MARK: - 行高间距常量 (pt)

    static let displayLargeLineHeight: CGFloat = 40
    static let displayMediumLineHeight: CGFloat = 32
    static let displaySmallLineHeight: CGFloat = 28
    static let headlineLargeLineHeight: CGFloat = 32
    static let headlineMediumLineHeight: CGFloat = 28
    static let headlineSmallLineHeight: CGFloat = 24
    static let titleLargeLineHeight: CGFloat = 28
    static let titleMediumLineHeight: CGFloat = 24
    static let titleSmallLineHeight: CGFloat = 20
    static let bodyLargeLineHeight: CGFloat = 24
    static let bodyMediumLineHeight: CGFloat = 20
    static let bodySmallLineHeight: CGFloat = 16
    static let labelLargeLineHeight: CGFloat = 20
    static let labelMediumLineHeight: CGFloat = 16
    static let labelSmallLineHeight: CGFloat = 16
}
