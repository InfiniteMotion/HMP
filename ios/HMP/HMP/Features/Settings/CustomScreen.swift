import SwiftUI
import shared

/// 自定义主题页 - 对应 Android CustomScreen.kt
struct CustomScreen: View {
    @Environment(HMPTheme.self) private var theme
    @State private var settingsVM = SettingsViewModel()

    private let backgroundStyles = ["FLUID", "IMMERSIVE", "BLUR"]
    private let backgroundStyleNames = ["流体极光", "沉浸光斑", "复古模糊"]
    private let themes = ["default", "light", "dark"]
    private let themeNames = ["跟随系统", "浅色", "深色"]

    var body: some View {
        SubScreen(title: "主题与背景") {
            List {
                Section("背景风格") {
                    Picker("", selection: Binding(
                        get: { backgroundStyles.firstIndex(of: settingsVM.backgroundStyle) ?? 0 },
                        set: { settingsVM.saveBackgroundStyle(backgroundStyles[$0]) }
                    )) {
                        ForEach(0..<backgroundStyleNames.count, id: \.self) { Text(backgroundStyleNames[$0]).tag($0) }
                    }
                    .pickerStyle(.segmented)
                }

                Section("主题模式") {
                    Picker("", selection: Binding(
                        get: { themes.firstIndex(of: settingsVM.customMode) ?? 0 },
                        set: { settingsVM.saveCustomMode(themes[$0]) }
                    )) {
                        ForEach(0..<themeNames.count, id: \.self) { Text(themeNames[$0]).tag($0) }
                    }
                    .pickerStyle(.segmented)
                }

                Section("预览") {
                    VStack(spacing: 16) {
                        HStack {
                            Text("主色")
                            Spacer()
                            Circle().fill(theme.primary).frame(width: 24, height: 24)
                        }
                        HStack {
                            Text("强调色")
                            Spacer()
                            Circle().fill(theme.secondary).frame(width: 24, height: 24)
                        }
                    }
                    .padding(.vertical, 8)
                }
            }
            .listStyle(.insetGrouped)
        }
    }
}
