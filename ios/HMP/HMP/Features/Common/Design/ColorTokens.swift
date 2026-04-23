import SwiftUI

/// 颜色令牌 - 与 Android ColorTokens.kt 保持对等
struct ColorTokens {
    // MARK: - 品牌颜色
    static let hdBlue = Color(red: 0.0, green: 0.184, blue: 0.655)  // #002FA7
    static let hdRed = Color(red: 0.788, green: 0.173, blue: 0.173) // #C92C2C

    // MARK: - 浅色主题
    static let lightPrimary = hdRed
    static let lightOnPrimary = Color.white
    static let lightPrimaryContainer = Color(red: 0.098, green: 0.463, blue: 0.824) // #1976D2
    static let lightOnPrimaryContainer = Color(red: 0.690, green: 0.0, blue: 0.125)  // #B00020
    static let lightSecondary = hdBlue
    static let lightBackground = Color.white
    static let lightSurface = Color.white
    static let lightError = lightPrimaryContainer

    // MARK: - 深色主题
    static let darkPrimary = Color(red: 0.565, green: 0.792, blue: 0.976)  // #90CAF9
    static let darkOnPrimary = Color.black
    static let darkPrimaryContainer = Color(red: 0.812, green: 0.4, blue: 0.475) // #CF6679
    static let darkOnPrimaryContainer = lightPrimaryContainer
    static let darkSecondary = Color(red: 0.957, green: 0.561, blue: 0.694)     // #F48FB1
    static let darkBackground = Color(red: 0.071, green: 0.071, blue: 0.071)    // #121212
    static let darkSurface = darkBackground
    static let darkError = darkPrimaryContainer

    // MARK: - 通用色
    static let cardLight = Color(red: 0.96, green: 0.96, blue: 0.97)
    static let cardDark = Color(red: 0.14, green: 0.14, blue: 0.15)
    static let dividerLight = Color(white: 0.88)
    static let dividerDark = Color(white: 0.20)
    static let textLight = Color(white: 0.12)
    static let textDark = Color(white: 0.95)
    static let secondaryTextLight = Color(white: 0.38)
    static let secondaryTextDark = Color(white: 0.68)
}
