import SwiftUI

/// 子页面容器 — 对应 Android SubScreen.kt
/// 使用系统导航栏，避免与 NavigationStack 的返回按钮重复
struct SubScreen<Content: View>: View {
    @Environment(HMPTheme.self) private var theme
    @Environment(\.dismiss) private var dismiss

    let title: String?
    let largeTitle: Bool
    let trailingContent: (() -> AnyView)?
    let onBack: (() -> Void)?
    let content: Content

    init(
        title: String? = nil,
        largeTitle: Bool = false,
        onBack: (() -> Void)? = nil,
        trailing: (() -> AnyView)? = nil,
        @ViewBuilder content: () -> Content
    ) {
        self.title = title
        self.largeTitle = largeTitle
        self.onBack = onBack
        self.trailingContent = trailing
        self.content = content()
    }

    var body: some View {
        content
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .navigationTitle(title ?? "")
            .navigationBarTitleDisplayMode(largeTitle ? .large : .inline)
            .toolbar {
                if let trailing = trailingContent {
                    ToolbarItem(placement: .topBarTrailing) { trailing() }
                }
            }
    }
}
