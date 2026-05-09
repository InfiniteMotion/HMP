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
        let content = ZStack {
            if let imageUri, !imageUri.isEmpty {
                // TODO: 加载本地图片
                fallbackView
            } else {
                fallbackView
            }
        }
        .frame(width: size, height: size)

        switch style {
        case .circle:
            content.clipShape(Circle())
        case .roundedRectangle:
            content.clipShape(RoundedRectangle(cornerRadius: size * 0.15))
        }
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
}
