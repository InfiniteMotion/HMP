import SwiftUI

/// 专辑封面组件 - 对应 Android AlbumCover.kt
struct AlbumCover: View {
    @Environment(HMPTheme.self) private var theme

    let uri: String?
    let size: CGFloat
    let cornerRadius: CGFloat

    init(
        uri: String?,
        size: CGFloat = 200,
        cornerRadius: CGFloat = 12
    ) {
        self.uri = uri
        self.size = size
        self.cornerRadius = cornerRadius
    }

    var body: some View {
        ZStack {
            if let uri, !uri.isEmpty {
                // TODO: 加载本地图片 URL (需从 shared 层暴露的 KMP 框架转换)
                // Image(uiImage: ...)
                placeholderView
            } else {
                placeholderView
            }
        }
        .frame(width: size, height: size)
        .cornerRadius(cornerRadius)
        .clipped()
    }

    private var placeholderView: some View {
        Rectangle()
            .fill(theme.primaryContainer)
            .overlay(
                Image(systemName: "music.note")
                    .font(.system(size: size * 0.35))
                    .foregroundColor(theme.onPrimary)
            )
    }
}
