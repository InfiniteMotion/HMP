import SwiftUI
import shared

/// 我的页 - 对应 Android UserScreen.kt + SettingScreen.kt
/// 用户信息 + 设置入口 + AI 推荐 + 使用数据
struct UserScreen: View {
    @Environment(HMPTheme.self) private var theme

    var body: some View {
        TabScreen(title: "我的") {
            List {
                // 用户资料
                Section("用户资料") {
                    NavigationLink("个人资料") {
                        Text("个人资料 (待实现)")
                    }
                    NavigationLink("使用数据") {
                        Text("使用数据 (待实现)")
                    }
                }

                // 音乐库
                Section("音乐库") {
                    NavigationLink("音乐库设置") {
                        LibrarySettingsScreen()
                    }
                    NavigationLink("音效设置") {
                        AudioEffectsScreen()
                    }
                }

                // AI 推荐
                Section("AI 推荐") {
                    NavigationLink("AI 设置") {
                        Text("AI 设置 (待实现)")
                    }
                }

                // 数据管理
                Section("数据管理") {
                    NavigationLink("备份与恢复") {
                        Text("备份与恢复 (待实现)")
                    }
                }

                // 主题自定义
                Section("个性化") {
                    NavigationLink("主题与背景") {
                        CustomScreen()
                    }
                }

                // 关于
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
