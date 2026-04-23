import SwiftUI

/// 设置主页 - 对应 Android SettingScreen.kt + UserScreen.kt
/// iOS 使用原生 Form / List(.insetGrouped) 大幅简化
struct UserScreen: View {
    @Environment(HMPTheme.self) private var theme

    var body: some View {
        TabScreen(title: "设置") {
            List {
                // MARK: - 用户资料
                Section("用户资料") {
                    NavigationLink("个人资料") {
                        ProfileSettingsView()
                    }
                    NavigationLink("使用数据") {
                        UserUsageDataView()
                    }
                }

                // MARK: - 音乐库
                Section("音乐库") {
                    NavigationLink("音乐库设置") {
                        LibrarySettingsView()
                    }
                    NavigationLink("音效设置") {
                        AudioEffectsView()
                    }
                }

                // MARK: - AI 推荐
                Section("AI 推荐") {
                    NavigationLink("AI 设置") {
                        AISettingsView()
                    }
                }

                // MARK: - 数据管理
                Section("数据管理") {
                    NavigationLink("备份与恢复") {
                        BackupSettingsView()
                    }
                }

                // MARK: - 关于
                Section("关于") {
                    HStack {
                        Text("版本")
                        Spacer()
                        Text("v5.10")
                            .foregroundColor(theme.secondaryText)
                    }
                }
            }
            .listStyle(.insetGrouped)
        }
    }
}

// MARK: - 占位子页面

struct ProfileSettingsView: View {
    var body: some View {
        Text("个人资料 (待实现)")
    }
}

struct UserUsageDataView: View {
    var body: some View {
        Text("使用数据 (待实现)")
    }
}

struct LibrarySettingsView: View {
    var body: some View {
        Text("音乐库设置 (待实现)")
    }
}

struct AudioEffectsView: View {
    var body: some View {
        Text("音效设置 (待实现)")
    }
}

struct AISettingsView: View {
    var body: some View {
        Text("AI 设置 (待实现)")
    }
}

struct BackupSettingsView: View {
    var body: some View {
        Text("备份与恢复 (待实现)")
    }
}
