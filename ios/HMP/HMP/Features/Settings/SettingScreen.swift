import SwiftUI
import shared

/// 设置中心 — 对应 Android SettingScreen.kt
struct SettingScreen: View {
    @Environment(HMPTheme.self) private var theme

    var body: some View {
        SubScreen(title: "设置") {
            ScrollView {
                VStack(spacing: 16) {
                    NavigationLink(value: HMPRoute.profileSettings) {
                        SettingItem(
                            title: "个人资料",
                            description: "管理您的个人信息和头像",
                            icon: "person.circle"
                        )
                    }
                    .buttonStyle(.plain)

                    NavigationLink(value: HMPRoute.backupSettings) {
                        SettingItem(
                            title: "备份与恢复",
                            description: "备份音乐库数据和播放列表",
                            icon: "arrow.triangle.2.circlepath"
                        )
                    }
                    .buttonStyle(.plain)

                    NavigationLink(value: HMPRoute.librarySettings) {
                        SettingItem(
                            title: "音乐库设置",
                            description: "管理音乐扫描和导入选项",
                            icon: "opticaldisc"
                        )
                    }
                    .buttonStyle(.plain)
                }
                .padding(24)
            }
        }
    }
}

/// 设置项卡片组件 - 对应 Android SettingItem.kt
struct SettingItem: View {
    @Environment(HMPTheme.self) private var theme

    let title: String
    let description: String
    let icon: String

    var body: some View {
        HStack(alignment: .center, spacing: 16) {
            // Icon
            Image(systemName: icon)
                .font(.system(size: 32))
                .foregroundColor(theme.primary)

            // Text content
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(TypographyTokens.titleMedium)
                    .foregroundColor(theme.text)
                Text(description)
                    .font(TypographyTokens.bodySmall)
                    .foregroundColor(theme.text.opacity(0.6))
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            // Chevron
            Image(systemName: "chevron.right")
                .font(.system(size: 16))
                .foregroundColor(theme.text.opacity(0.5))
        }
        .padding(16)
        .background(theme.cardBackground)
        .cornerRadius(12)
    }
}