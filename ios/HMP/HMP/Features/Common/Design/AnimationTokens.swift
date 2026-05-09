import SwiftUI

/// 动画令牌 - 与 Android AnimationTokens.kt 保持对等
struct AnimationTokens {
    // MARK: - 持续时间 (秒)
    static let microInteraction: Double = 0.2     // 微交互动画
    static let transition: Double = 0.4           // 过渡动画
    static let complex: Double = 0.65             // 复杂动画
    static let background: Double = 3.0           // 背景动画

    // MARK: - 缓动 (UnitCurve 对应 CubicBezierEasing)
    static let easeInOut = UnitCurve.easeInOut
    static let easeOut = UnitCurve.easeOut
    static let easeIn = UnitCurve.easeIn

    // MARK: - 弹簧动画
    static func springMedium() -> Animation {
        .interactiveSpring(response: 0.3, dampingFraction: 0.7, blendDuration: 0.3)
    }

    static func springBouncy() -> Animation {
        .interactiveSpring(response: 0.4, dampingFraction: 0.5, blendDuration: 0.3)
    }

    static func springGentle() -> Animation {
        .interactiveSpring(response: 0.5, dampingFraction: 0.6, blendDuration: 0.3)
    }
}
