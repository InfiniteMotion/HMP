import SwiftUI

/// 播放列表页 - 对应 Android PlaylistScreen.kt
/// 展示所有用户自定义播放列表
struct PlaylistScreen: View {
    @Environment(HMPTheme.self) private var theme

    @State private var showCreateDialog = false
    @State private var playlists: [PlaylistItem] = []  // 占位

    // TODO: 连接 PlaylistViewModel (P6 完成后)
    // @StateObject private var playlistVM: PlaylistViewModel

    var body: some View {
        TabScreen(
            title: "播放列表",
            trailing: {
                AnyView(
                    Button {
                        HapticManager.shared.click()
                        showCreateDialog = true
                    } label: {
                        Image(systemName: "plus")
                            .font(.system(size: 18))
                            .foregroundColor(theme.text)
                    }
                )
            }
        ) {
            if playlists.isEmpty {
                emptyStateView
            } else {
                ScrollView {
                    LazyVStack(spacing: 8) {
                        ForEach(playlists) { playlist in
                            NavigationLink {
                                CustomPlaylistDetailView(playlistId: playlist.id, name: playlist.name)
                            } label: {
                                PlaylistRow(playlist: playlist)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.horizontal, 16)
                }
            }
        }
        .sheet(isPresented: $showCreateDialog) {
            CreatePlaylistDialog { name in
                // TODO: playlistVM.createPlaylist(name)
            }
        }
    }

    private var emptyStateView: some View {
        VStack(spacing: 16) {
            Image(systemName: "music.note.list")
                .font(.system(size: 48))
                .foregroundColor(theme.secondaryText)
            Text("暂无播放列表")
                .font(TypographyTokens.titleLarge)
                .foregroundColor(theme.secondaryText)
            Button("创建播放列表") {
                HapticManager.shared.click()
                showCreateDialog = true
            }
            .buttonStyle(.borderedProminent)
            .padding(.top, 8)
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 60)
    }
}

// MARK: - PlaylistRow
struct PlaylistRow: View {
    @Environment(HMPTheme.self) private var theme
    let playlist: PlaylistItem

    var body: some View {
        HStack(spacing: 12) {
            // 封面
            Rectangle()
                .fill(theme.primaryContainer)
                .frame(width: 48, height: 48)
                .cornerRadius(8)
                .overlay(
                    Image(systemName: "music.note.list")
                        .foregroundColor(theme.onPrimary)
                        .font(.system(size: 18))
                )

            VStack(alignment: .leading, spacing: 2) {
                Text(playlist.name)
                    .font(TypographyTokens.titleMedium)
                    .foregroundColor(theme.text)
                    .lineLimit(1)
                Text("\(playlist.songCount) 首")
                    .font(TypographyTokens.bodySmall)
                    .foregroundColor(theme.secondaryText)
            }

            Spacer()

            Image(systemName: "chevron.right")
                .font(.system(size: 12, weight: .medium))
                .foregroundColor(theme.secondaryText)
        }
        .padding(.vertical, 8)
    }
}

/// 播放列表模型 (Swift 端)
struct PlaylistItem: Identifiable, Hashable {
    let id: Int64
    let name: String
    let coverUri: String?
    let songCount: Int
    let description: String?
    let isPinned: Bool

    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
    }

    static func == (lhs: PlaylistItem, rhs: PlaylistItem) -> Bool {
        lhs.id == rhs.id
    }
}

/// 播放列表详情页
struct CustomPlaylistDetailView: View {
    @Environment(HMPTheme.self) private var theme
    let playlistId: Int64
    let name: String

    // 占位歌曲列表
    @State private var musicList: [MusicItem] = []

    var body: some View {
        SubScreen(title: name) {
            if musicList.isEmpty {
                VStack(spacing: 12) {
                    Image(systemName: "music.note")
                        .font(.system(size: 40))
                        .foregroundColor(theme.secondaryText)
                    Text("播放列表为空")
                        .font(TypographyTokens.bodyLarge)
                        .foregroundColor(theme.secondaryText)
                }
                .frame(maxWidth: .infinity)
                .padding(.top, 60)
            } else {
                MusicList(musicList: musicList)
            }
        }
    }
}
