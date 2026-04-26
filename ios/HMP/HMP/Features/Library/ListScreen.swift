import SwiftUI
import shared

/// 列表页 - 对应 Android ListScreen.kt
struct ListScreen: View {
    @Environment(HMPTheme.self) private var theme
    @State private var playlistVM = PlaylistViewModel()

    var body: some View {
        TabScreen(title: "列表") {
            ScrollView {
                VStack(spacing: 24) {
                    // 用户歌单 - 横向滚动卡片
                    VStack(alignment: .leading, spacing: 12) {
                        HStack {
                            TitleWidget(title: "我的歌单")
                            Spacer()
                            NavigationLink {
                                PlaylistManageScreen()
                            } label: {
                                Text("管理").font(TypographyTokens.bodySmall).foregroundColor(theme.primary)
                            }
                        }

                        if playlistVM.userCustomPlaylists.isEmpty {
                            Text("暂无歌单")
                                .font(TypographyTokens.bodyMedium)
                                .foregroundColor(theme.text.opacity(0.4))
                                .frame(maxWidth: .infinity)
                                .padding(.top, 20)
                        } else {
                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 12) {
                                    ForEach(Array(playlistVM.userCustomPlaylists.enumerated()), id: \.element.id) { _, playlist in
                                        NavigationLink {
                                            PlaylistScreen(playlistId: playlist.id)
                                        } label: {
                                            UserPlaylistCard(playlist: playlist)
                                        }
                                        .buttonStyle(.plain)
                                    }
                                }
                            }
                        }
                    }

                    Divider()

                    // 常用列表 - 3 个横幅卡片
                    VStack(alignment: .leading, spacing: 12) {
                        TitleWidget(title: "常用")
                        HStack(spacing: 12) {
                            ListBannerCard(title: "默认", icon: "music.note.list", accent: theme.primary) {}
                            ListBannerCard(title: "红心", icon: "heart.fill", accent: .red) {}
                            ListBannerCard(title: "最近", icon: "clock.fill", accent: .orange) {}
                        }
                    }

                    Divider()

                    // 场景 - 横向滚动大卡片
                    CategorySection(title: "场景", items: playlistVM.scenarioPlaylistName, icon: "sparkles", cardStyle: .large)
                    
                    // 流派 - 横向滚动中等卡片
                    CategorySection(title: "流派", items: playlistVM.genrePlaylistName, icon: "globe", cardStyle: .medium)
                    
                    // 心情 - 2x2 网格
                    MoodGridSection(title: "心情", items: playlistVM.moodPlaylistName)
                    
                    // 语言
                    CategorySection(title: "语言", items: playlistVM.languagePlaylistName, icon: "character.bubble", cardStyle: .compact)
                    
                    // 年代
                    CategorySection(title: "年代", items: playlistVM.eraPlaylistName, icon: "calendar", cardStyle: .compact)
                }
                .padding(.horizontal, 16)
            }
        }
        .onAppear {
            playlistVM.loadLabels()
            playlistVM.loadUserCustomPlaylists()
        }
    }
}

// MARK: - User Playlist Card
struct UserPlaylistCard: View {
    @Environment(HMPTheme.self) private var theme
    let playlist: Playlist_

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // 封面占位
            Rectangle()
                .fill(theme.primary.opacity(0.12))
                .frame(width: 120, height: 120)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .overlay {
                    Image(systemName: "music.note.list")
                        .font(.system(size: 32))
                        .foregroundColor(theme.primary)
                }

            Text(playlist.name)
                .font(TypographyTokens.bodyMedium)
                .foregroundColor(theme.text)
                .lineLimit(1)

            Text("\(playlist.songCount) 首")
                .font(TypographyTokens.bodySmall)
                .foregroundColor(theme.text.opacity(0.6))
        }
        .frame(width: 120)
    }
}

// MARK: - List Banner Card
struct ListBannerCard: View {
    @Environment(HMPTheme.self) private var theme
    let title: String
    let icon: String
    let accent: Color
    let onTap: () -> Void

    var body: some View {
        Button {
            HapticManager.shared.click()
            onTap()
        } label: {
            VStack(spacing: 8) {
                Image(systemName: icon)
                    .font(.system(size: 24))
                    .foregroundColor(accent)
                    .frame(width: 48, height: 48)
                    .background(accent.opacity(0.12))
                    .clipShape(Circle())
                Text(title)
                    .font(TypographyTokens.bodySmall)
                    .foregroundColor(theme.text)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 16)
            .background(theme.cardBackground)
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }
}

// MARK: - Category Section
enum CardStyle {
    case large   // 场景：大卡片
    case medium  // 流派：中等卡片
    case compact // 语言/年代：紧凑卡片
}

struct CategorySection: View {
    @Environment(HMPTheme.self) private var theme
    let title: String
    let items: [String]
    let icon: String
    let cardStyle: CardStyle

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            TitleWidget(title: title)
            if items.isEmpty {
                Text("暂无数据")
                    .font(TypographyTokens.bodyMedium)
                    .foregroundColor(theme.text.opacity(0.4))
            } else {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 12) {
                        ForEach(items, id: \.self) { label in
                            NavigationLink {
                                PlaylistScreen(playlistName: label)
                            } label: {
                                switch cardStyle {
                                case .large:
                                    CategoryCardLarge(title: label, icon: icon)
                                case .medium:
                                    CategoryCardMedium(title: label, icon: icon)
                                case .compact:
                                    CategoryCardCompact(title: label)
                                }
                            }
                            .buttonStyle(.plain)
                        }
                    }
                }
            }
        }
    }
}

struct CategoryCardLarge: View {
    @Environment(HMPTheme.self) private var theme
    let title: String
    let icon: String

    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 40))
                .foregroundColor(theme.primary)
            Text(title)
                .font(TypographyTokens.bodyMedium)
                .foregroundColor(theme.text)
                .lineLimit(2)
                .multilineTextAlignment(.center)
        }
        .frame(width: 140, height: 120)
        .background(theme.cardBackground)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

struct CategoryCardMedium: View {
    @Environment(HMPTheme.self) private var theme
    let title: String
    let icon: String

    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: icon)
                .font(.system(size: 28))
                .foregroundColor(theme.primary)
            Text(title)
                .font(TypographyTokens.bodySmall)
                .foregroundColor(theme.text)
                .lineLimit(1)
        }
        .frame(width: 110, height: 90)
        .background(theme.cardBackground)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

struct CategoryCardCompact: View {
    @Environment(HMPTheme.self) private var theme
    let title: String

    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: "tag")
                .font(.system(size: 14))
                .foregroundColor(theme.primary)
            Text(title)
                .font(TypographyTokens.bodySmall)
                .foregroundColor(theme.text)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
        .background(theme.cardBackground)
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}

// MARK: - Mood Grid Section (2x2)
struct MoodGridSection: View {
    @Environment(HMPTheme.self) private var theme
    let title: String
    let items: [String]

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            TitleWidget(title: title)
            if items.isEmpty {
                Text("暂无数据")
                    .font(TypographyTokens.bodyMedium)
                    .foregroundColor(theme.text.opacity(0.4))
            } else {
                let displayItems = Array(items.prefix(4))
                let columns = [
                    GridItem(.flexible(), spacing: 12),
                    GridItem(.flexible(), spacing: 12)
                ]
                LazyVGrid(columns: columns, spacing: 12) {
                    ForEach(displayItems, id: \.self) { label in
                        NavigationLink {
                            PlaylistScreen(playlistName: label)
                        } label: {
                            MoodGridItem(title: label)
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
        }
    }
}

struct MoodGridItem: View {
    @Environment(HMPTheme.self) private var theme
    let title: String

    var body: some View {
        Text(title)
            .font(TypographyTokens.bodyMedium)
            .foregroundColor(theme.text)
            .frame(maxWidth: .infinity)
            .frame(height: 50)
            .background(theme.cardBackground)
            .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}
