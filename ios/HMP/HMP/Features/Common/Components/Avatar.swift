import SwiftUI

/// 头像组件 - 对应 Android Avatar.kt
/// 用于艺术家/专辑头像，支持文字首字母 fallback
struct Avatar: View {
    @Environment(HMPTheme.self) private var theme

    let text: String
    let size: CGFloat
    let imageUri: String?
    let style: AvatarStyle

    enum AvatarStyle {
        case circle
        case roundedRectangle
    }

    init(
        text: String = "",
        size: CGFloat = 40,
        imageUri: String? = nil,
        style: AvatarStyle = .circle
    ) {
        self.text = text
        self.size = size
        self.imageUri = imageUri
        self.style = style
    }

    var body: some View {
        ZStack {
            if let imageUri, !imageUri.isEmpty {
                // TODO: 加载本地图片
                fallbackView
            } else {
                fallbackView
            }
        }
        .frame(width: size, height: size)
        .modifier(avatarShape)
        .clipped()
    }

    private var fallbackView: some View {
        ZStack {
            Circle().fill(theme.primaryContainer)
            let initial = text.first.map { String($0).uppercased() } ?? "?"
            Text(initial)
                .font(.system(size: size * 0.4, weight: .bold))
                .foregroundColor(theme.onPrimary)
        }
    }

    private var avatarShape: some ViewModifier {
        switch style {
        case .circle:
            AnyModifier(_CircleShape())
        case .roundedRectangle:
            AnyModifier(_RoundedShape(radius: size * 0.15))
        }
    }
}

// Shape modifiers
private struct _CircleShape: ViewModifier {
    func body(content: Content) -> some View {
        content.clipShape(Circle())
    }
}

private struct _RoundedShape: ViewModifier {
    let radius: CGFloat
    func body(content: Content) -> some View {
        content.clipShape(RoundedRectangle(cornerRadius: radius))
    }
}

struct AnyModifier: ViewModifier {
    private let _body: (Content) -> AnyView
    init<M: ViewModifier>(_ modifier: M) {
        _body = { AnyView(modifier.body(from: $0)) }
    }
    func body(content: Content) -> some View {
        _body(content)
    }
}
