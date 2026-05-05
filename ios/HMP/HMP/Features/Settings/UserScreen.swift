import SwiftUI
import shared

/// 我的页 - 对应 Android UserScreen.kt + SettingScreen.kt
struct UserScreen: View {
    @Environment(HMPTheme.self) private var theme

    var body: some View {
        TabScreen(title: "我的") {
            List {
                Section("用户资料") {
                    NavigationLink(value: HMPRoute.profileSettings) {
                        Text("个人资料")
                    }
                    NavigationLink(value: HMPRoute.userUsageData) {
                        Text("使用数据")
                    }
                }

                Section("音乐库") {
                    NavigationLink(value: HMPRoute.librarySettings) {
                        Text("音乐库设置")
                    }
                    NavigationLink(value: HMPRoute.audioEffects) {
                        Text("音效设置")
                    }
                }

                Section("AI 推荐") {
                    NavigationLink(value: HMPRoute.ai) {
                        Text("AI 设置")
                    }
                }

                Section("数据管理") {
                    NavigationLink(value: HMPRoute.backupSettings) {
                        Text("备份与恢复")
                    }
                }

                Section("个性化") {
                    NavigationLink(value: HMPRoute.custom) {
                        Text("主题与背景")
                    }
                }

                Section("关于") {
                    HStack {
                        Text("版本")
                        Spacer()
                        Text("v5.10")
                            .foregroundColor(theme.text.opacity(0.4))
                    }
                }
            }
            .listStyle(.insetGrouped)
        }
    }
}
