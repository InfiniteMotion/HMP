import UIKit

/// 触觉反馈管理器 - 对应 Android HapticFeedback.kt
final class HapticManager {
    static let shared = HapticManager()
    private init() {}

    /// 轻触反馈 — 对应 performLightClick() / CLOCK_TICK
    func lightClick() {
        let generator = UIImpactFeedbackGenerator(style: .light)
        generator.prepare()
        generator.impactOccurred()
    }

    /// 标准点击 — 对应 performClick() / VIRTUAL_KEY
    func click() {
        let generator = UIImpactFeedbackGenerator(style: .medium)
        generator.prepare()
        generator.impactOccurred()
    }

    /// 确认反馈 — 对应 performConfirm() / CONFIRM
    func confirm() {
        let generator = UINotificationFeedbackGenerator()
        generator.prepare()
        generator.notificationOccurred(.success)
    }

    /// 拒绝/错误反馈 — 对应 performReject() / REJECT
    func reject() {
        let generator = UINotificationFeedbackGenerator()
        generator.prepare()
        generator.notificationOccurred(.error)
    }

    /// 长按反馈 — 对应 performLongPress()
    func longPress() {
        let generator = UIImpactFeedbackGenerator(style: .heavy)
        generator.prepare()
        generator.impactOccurred()
    }

    /// 拖拽开始 — 对应 performDragStart()
    func dragStart() {
        let generator = UIImpactFeedbackGenerator(style: .rigid)
        generator.prepare()
        generator.impactOccurred()
    }

    /// 上下文点击 — 对应 performContextClick()
    func contextClick() {
        let generator = UIImpactFeedbackGenerator(style: .soft)
        generator.prepare()
        generator.impactOccurred()
    }
}
