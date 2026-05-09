import SwiftUI
import shared

/// 我的页 - 对应 Android UserScreen.kt
struct UserScreen: View {
    @Environment(HMPTheme.self) private var theme
    @State private var settingsVM = SettingsViewModel()

    var body: some View {
        TabScreen(title: "我的") {
            ScrollView {
                VStack(spacing: 24) {
                    // 头像卡片
                    NavigationLink(value: HMPRoute.profileSettings) {
                        avatarCardContent
                    }
                    .buttonStyle(.plain)

                    // 听歌统计卡片
                    NavigationLink(value: HMPRoute.userUsageData) {
                        listeningStatsCardContent
                    }
                    .buttonStyle(.plain)

                    // 功能卡片行 1
                    HStack(spacing: 24) {
                        NavigationLink(value: HMPRoute.custom) {
                            squareCardContent(title: "主题定制", icon: "slider.vertical.3", accent: theme.primary)
                        }
                        .buttonStyle(.plain)

                        NavigationLink(value: HMPRoute.audioEffects) {
                            squareCardContent(title: "音效设置", icon: "waveform", accent: .purple)
                        }
                        .buttonStyle(.plain)
                    }

                    // 功能卡片行 2
                    HStack(spacing: 24) {
                        NavigationLink(value: HMPRoute.ai) {
                            squareCardContent(title: "AI 服务", icon: "sparkles", accent: .blue)
                        }
                        .buttonStyle(.plain)

                        NavigationLink(value: HMPRoute.setting) {
                            squareCardContent(title: "设置", icon: "gear", accent: .gray)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal, 24)
                .padding(.bottom, 48)
            }
        }
        .onAppear {
            settingsVM.loadSettings()
        }
    }
    
    // MARK: - Avatar Card Content
    private var avatarCardContent: some View {
        HStack(alignment: .center, spacing: 16) {
            // Avatar
            if !settingsVM.avatarUri.isEmpty, let image = CoverCache.shared.get(path: settingsVM.avatarUri) {
                Image(uiImage: image)
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .frame(width: 100, height: 100)
                    .clipShape(Circle())
            } else {
                Circle()
                    .fill(theme.primary.opacity(0.2))
                    .frame(width: 100, height: 100)
                    .overlay {
                        Image(systemName: "person.fill")
                            .font(.system(size: 40))
                            .foregroundColor(theme.primary)
                    }
            }

            // User Name
            Spacer()
            Text(settingsVM.userName.isEmpty ? "点击设置昵称" : settingsVM.userName)
                .font(TypographyTokens.titleLarge)
                .foregroundColor(theme.text)
            Spacer()
        }
        .padding(16)
        .background(theme.cardBackground)
        .cornerRadius(20)
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(theme.text.opacity(0.1), lineWidth: 1)
        )
    }

    // MARK: - Listening Stats Card Content
    private var listeningStatsCardContent: some View {
        VStack(spacing: 8) {
            HStack {
                Text("听歌数据")
                    .font(TypographyTokens.titleMedium)
                    .foregroundColor(theme.text)
                Spacer()
                Image(systemName: "square.grid.2x2")
                    .font(.system(size: 20))
                    .foregroundColor(theme.text.opacity(0.5))
            }

            // Simple chart placeholder
            HStack(alignment: .bottom, spacing: 3) {
                ForEach(0..<7) { i in
                    let height = CGFloat.random(in: 10...60)
                    RoundedRectangle(cornerRadius: 3)
                        .fill(theme.primary.opacity(0.6))
                        .frame(height: height)
                        .frame(maxWidth: .infinity)
                }
            }
            .frame(height: 60)
        }
        .padding(16)
        .background(theme.cardBackground)
        .cornerRadius(20)
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(theme.text.opacity(0.1), lineWidth: 1)
        )
    }

    // MARK: - Square Card Content
    private func squareCardContent(title: String, icon: String, accent: Color) -> some View {
        VStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 28))
                .foregroundColor(accent)
            Text(title)
                .font(TypographyTokens.bodyMedium)
                .foregroundColor(theme.text)
        }
        .frame(maxWidth: .infinity)
        .padding(16)
        .background(theme.cardBackground)
        .cornerRadius(16)
    }
}