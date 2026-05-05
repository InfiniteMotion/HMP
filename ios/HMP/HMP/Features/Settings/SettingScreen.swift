import SwiftUI
import shared

/// 设置中心 — 对应 Android SettingScreen.kt
struct SettingScreen: View {
    @Environment(HMPTheme.self) private var theme

    var body: some View {
        SubScreen(title: "设置") {
            List {
                Section {
                    NavigationLink(value: HMPRoute.profileSettings) {
                        Label("个人资料", systemImage: "person.circle")
                    }
                    NavigationLink(value: HMPRoute.backupSettings) {
                        Label("备份与恢复", systemImage: "arrow.triangle.2.circlepath")
                    }
                    NavigationLink(value: HMPRoute.librarySettings) {
                        Label("音乐库设置", systemImage: "opticaldisc")
                    }
                }
            }
            .listStyle(.insetGrouped)
        }
    }
}
