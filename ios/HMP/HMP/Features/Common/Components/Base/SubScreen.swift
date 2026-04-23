import SwiftUI

/// 子屏幕模板 - 对应 Android SubScreen.kt
/// 用作二级页面容器，带返回按钮、可选标题、尾部操作
struct SubScreen<Content: View>: View {
    @Environment(HMPTheme.self) private var theme
    @Environment(\.dismiss) private var dismiss

    let title: String?
    let trailingContent: (() -> AnyView)?
    let onBack: (() -> Void)?
    let content: Content

    init(
        title: String? = nil,
        onBack: (() -> Void)? = nil,
        @ViewBuilder trailing: (() -> AnyView)? = nil,
        @ViewBuilder content: () -> Content
    ) {
        self.title = title
        self.onBack = onBack
        self.trailingContent = trailing
        self.content = content()
    }

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack(alignment: .center) {
                // Back button
                Button {
                    HapticManager.shared.click()
                    if let onBack {
                        onBack()
                    } else {
                        dismiss()
                    }
                } label: {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 18, weight: .medium))
                        .foregroundColor(theme.text)
                }

                Spacer()

                // Title
                if let title {
                    Text(title)
                        .font(TypographyTokens.headlineLarge)
                        .foregroundColor(theme.text)
                        .lineLimit(1)
                        .truncationMode(.tail)
                        .frame(maxWidth: 280)
                }

                Spacer()

                // Trailing content
                if let trailing = trailingContent {
                    trailing()
                } else {
                    Spacer()
                        .frame(width: 32)
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 16)
            .padding(.bottom, 8)

            // Main content
            content
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

#Preview {
    SubScreen(
        title: "歌曲详情"
    ) {
        Text("Detail content")
    }
}
