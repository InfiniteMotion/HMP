import SwiftUI
import shared

/// 主标签页视图 - 对应 Android TabsHost.kt
/// 使用 TabView(.page) 保留 Android 端 HorizontalPager 滑动交互
struct MainTabView: View {
    @Environment(HMPTheme.self) private var theme
    @State private var selectedTab: TabItem = .library

    var body: some View {
        ZStack(alignment: .bottom) {
            // 页面内容 - 使用 TabView(.page) 实现滑动切换
            TabView(selection: $selectedTab) {
                LibraryView()
                    .tag(TabItem.library)
                    .tabItem { Label(TabItem.library.title, systemImage: TabItem.library.rawValue) }

                PlayerView()
                    .tag(TabItem.player)
                    .tabItem { Label(TabItem.player.title, systemImage: TabItem.player.rawValue) }

                PlaylistView()
                    .tag(TabItem.playlist)
                    .tabItem { Label(TabItem.playlist.title, systemImage: TabItem.playlist.rawValue) }

                SettingsView()
                    .tag(TabItem.settings)
                    .tabItem { Label(TabItem.settings.title, systemImage: TabItem.settings.rawValue) }
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
            .ignoresSafeArea(edges: .top)

            // MiniPlayerBar overlay - 在播放时显示
            MiniPlayerBar()
        }
    }
}

// MARK: - 占位页面 (后续替换为实际页面)

/// 音乐库页 - 对应 Android HomeScreen / GalleryScreen / ListScreen
struct LibraryView: View {
    @Environment(HMPTheme.self) private var theme
    @State private var viewModel = LibraryViewModel()

    var body: some View {
        NavigationStack {
            TabScreen(
                title: "音乐库",
                hasSearchButton: true
            ) {
                VStack(spacing: 16) {
                    // Scan button
                    Button(action: {
                        Task { await viewModel.fullRescan() }
                    }) {
                        HStack {
                            Image(systemName: "magnifyingglass")
                            Text(viewModel.isScanning ? "扫描中..." : "扫描音乐")
                        }
                        .padding(.horizontal, 20)
                        .padding(.vertical, 10)
                        .background(theme.primary)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                    }
                    .disabled(viewModel.isScanning)

                    // Error message
                    if let error = viewModel.errorMessage {
                        Text(error)
                            .font(TypographyTokens.bodySmall)
                            .foregroundColor(.red)
                            .padding(.horizontal)
                    }

                    // Music count
                    if viewModel.musicCount > 0 {
                        Text("共 \(viewModel.musicCount) 首歌曲")
                            .font(TypographyTokens.bodyMedium)
                            .foregroundColor(theme.text.opacity(0.6))
                    }

                    // Music list
                    if viewModel.musicList.isEmpty {
                        Spacer()
                        Text("暂无音乐")
                            .font(TypographyTokens.bodyLarge)
                            .foregroundColor(theme.text.opacity(0.4))
                        Spacer()
                    } else {
                        List {
                            ForEach(viewModel.musicList, id: \.music.id) { info in
                                MusicInfoRow(info: info, theme: theme)
                            }
                        }
                        .listStyle(.plain)
                    }
                }
                .padding()
            }
        }
    }
}

private struct MusicInfoRow: View {
    let info: MusicInfo_
    let theme: HMPTheme

    var body: some View {
        HStack(spacing: 12) {
            // Album art placeholder
            RoundedRectangle(cornerRadius: 6)
                .fill(theme.primary.opacity(0.15))
                .frame(width: 48, height: 48)
                .overlay {
                    Image(systemName: "music.note")
                        .foregroundColor(theme.primary)
                }

            VStack(alignment: .leading, spacing: 2) {
                Text(info.music.title)
                    .font(TypographyTokens.bodyMedium)
                    .foregroundColor(theme.text)
                    .lineLimit(1)

                Text("\(info.music.artist) · \(info.music.album)")
                    .font(TypographyTokens.bodySmall)
                    .foregroundColor(theme.text.opacity(0.6))
                    .lineLimit(1)
            }

            Spacer()

            Text(formatDuration(info.music.duration))
                .font(TypographyTokens.bodySmall)
                .foregroundColor(theme.text.opacity(0.4))
        }
        .padding(.vertical, 4)
    }

    private func formatDuration(_ ms: Int64) -> String {
        let seconds = ms / 1000
        let min = seconds / 60
        let sec = seconds % 60
        return String(format: "%d:%02d", min, sec)
    }
}

/// 播放页 - 对应 Android PlayerScreen
struct PlayerView: View {
    @Environment(HMPTheme.self) private var theme

    var body: some View {
        NavigationStack {
            Text("播放器 (待实现)")
                .font(TypographyTokens.bodyLarge)
                .foregroundColor(theme.text)
        }
    }
}

/// 播放列表页 - 对应 Android PlaylistScreen
struct PlaylistView: View {
    @Environment(HMPTheme.self) private var theme

    var body: some View {
        NavigationStack {
            TabScreen(
                title: "播放列表",
                hasSearchButton: false
            ) {
                Text("播放列表 (待实现)")
                    .font(TypographyTokens.bodyLarge)
                    .foregroundColor(theme.text)
            }
        }
    }
}

/// 设置页 - 对应 Android SettingScreen
struct SettingsView: View {
    @Environment(HMPTheme.self) private var theme

    var body: some View {
        NavigationStack {
            Text("设置 (待实现)")
                .font(TypographyTokens.bodyLarge)
                .foregroundColor(theme.text)
        }
    }
}
