import SwiftUI

/// Tab 屏幕模板 - 对应 Android TabScreen.kt
/// 用作 Tab 页内容容器，带可选标题、搜索按钮、尾部内容
struct TabScreen<Content: View>: View {
    @Environment(HMPTheme.self) private var theme

    let title: String?
    let hasSearchButton: Bool
    let trailingContent: (() -> AnyView)?
    let content: Content

    init(
        title: String? = nil,
        hasSearchButton: Bool = false,
        trailing: (() -> AnyView)? = nil,
        @ViewBuilder content: () -> Content
    ) {
        self.title = title
        self.hasSearchButton = hasSearchButton
        self.trailingContent = trailing
        self.content = content()
    }

    var body: some View {
        VStack(spacing: 0) {
            if let title {
                HStack(alignment: .center) {
                    Text(title)
                        .font(TypographyTokens.displayLarge)
                        .foregroundColor(theme.text)

                    Spacer()

                    if let trailing = trailingContent {
                        trailing()
                    }

                    if hasSearchButton {
                        NavigationLink(value: HMPRoute.search) {
                            Image(systemName: "magnifyingglass")
                                .font(.system(size: 18))
                                .foregroundColor(theme.text)
                        }
                    }
                }
                .padding(.horizontal, 32)
                .padding(.top, 16)
                .padding(.bottom, 16)
            }

            content
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .ignoresSafeArea(edges: .bottom)
    }
}

#Preview {
    TabScreen(
        title: "音乐库",
        hasSearchButton: true
    ) {
        Text("Content here")
    }
}
