import SwiftUI

/// 标题栏组件 - 对应 Android TitleWidget.kt
/// 左侧标题 + 右侧可选查看更多按钮
struct TitleWidget: View {
    @Environment(HMPTheme.self) private var theme

    let title: String
    let showMoreButton: Bool
    let onMore: (() -> Void)?

    init(
        title: String,
        showMoreButton: Bool = false,
        onMore: (() -> Void)? = nil
    ) {
        self.title = title
        self.showMoreButton = showMoreButton
        self.onMore = onMore
    }

    var body: some View {
        HStack {
            Text(title)
                .font(TypographyTokens.titleLarge)
                .foregroundColor(theme.text)

            Spacer()

            if showMoreButton {
                Button {
                    HapticManager.shared.click()
                    onMore?()
                } label: {
                    HStack(spacing: 4) {
                        Text("更多")
                            .font(TypographyTokens.labelMedium)
                        Image(systemName: "chevron.right")
                            .font(.system(size: 10, weight: .medium))
                    }
                    .foregroundColor(theme.primary)
                }
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
    }
}
