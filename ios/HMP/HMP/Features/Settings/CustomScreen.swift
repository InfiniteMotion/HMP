import SwiftUI

/// 自定义主题页 - 对应 Android CustomScreen.kt
/// 动态背景风格 + 主题预览
struct CustomScreen: View {
    @Environment(HMPTheme.self) private var theme

    @State private var selectedBackgroundStyle: Int = 0
    @State private var selectedTheme: Int = 0
    private let backgroundStyles = ["流体极光", "沉浸光斑", "复古模糊"]
    private let themes = ["浅色", "深色", "跟随系统"]

    var body: some View {
        TabScreen(title: "自定义") {
            List {
                Section("背景风格") {
                    Picker("", selection: $selectedBackgroundStyle) {
                        ForEach(0..<backgroundStyles.count, id: \.self) { i in
                            Text(backgroundStyles[i]).tag(i)
                        }
                    }
                    .pickerStyle(.segmented)
                }

                Section("主题模式") {
                    Picker("", selection: $selectedTheme) {
                        ForEach(0..<themes.count, id: \.self) { i in
                            Text(themes[i]).tag(i)
                        }
                    }
                    .pickerStyle(.segmented)
                }

                Section("预览") {
                    VStack(spacing: 16) {
                        HStack {
                            Text("主色")
                            Spacer()
                            Circle()
                                .fill(theme.primary)
                                .frame(width: 24, height: 24)
                        }
                        HStack {
                            Text("强调色")
                            Spacer()
                            Circle()
                                .fill(theme.secondary)
                                .frame(width: 24, height: 24)
                        }
                    }
                    .padding(.vertical, 8)
                }
            }
            .listStyle(.insetGrouped)
        }
    }
}
