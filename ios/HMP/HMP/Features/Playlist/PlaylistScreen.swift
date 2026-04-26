import SwiftUI
import shared

/// 播放列表页 - 对应 Android PlaylistScreen.kt
struct PlaylistScreen: View {
    @Environment(HMPTheme.self) private var theme
    @State private var playlistVM = PlaylistViewModel()

    var playlistName: String? = nil
    var playlistId: Int64? = nil

    private var controller: MusicPlayerController { MusicPlayerController.shared }

    var body: some View {
        SubScreen(
            title: playlistName ?? "播放列表"
        ) {
            switch playlistVM.selectedPlaylistState {
            case .loading:
                ProgressView().frame(maxWidth: .infinity).padding(.top, 40)
            case .empty:
                VStack(spacing: 12) {
                    Image(systemName: "music.note").font(.system(size: 40)).foregroundColor(theme.text.opacity(0.4))
                    Text("播放列表为空").font(TypographyTokens.bodyMedium).foregroundColor(theme.text.opacity(0.4))
                }
                .frame(maxWidth: .infinity).padding(.top, 60)
            case .success(let musicList):
                ScrollView {
                    LazyVStack(spacing: 0) {
                        ForEach(musicList, id: \.music.id) { info in
                            HStack(spacing: 12) {
                                AlbumCover(uri: info.music.albumArtUri, musicPath: info.music.path, size: 44, cornerRadius: 8)
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(info.music.title).font(TypographyTokens.bodyMedium).foregroundColor(theme.text).lineLimit(1)
                                    Text(info.music.artist).font(TypographyTokens.bodySmall).foregroundColor(theme.text.opacity(0.6)).lineLimit(1)
                                }
                                Spacer()
                            }
                            .contentShape(Rectangle())
                            .onTapGesture { controller.playWith(info) }
                            .padding(.vertical, 8)
                            .padding(.horizontal, 16)
                        }
                    }
                }
            case .error(let message):
                VStack(spacing: 12) {
                    Text("加载失败").font(TypographyTokens.bodyMedium).foregroundColor(theme.text.opacity(0.4))
                    Text(message).font(TypographyTokens.bodySmall).foregroundColor(theme.text.opacity(0.4))
                }
                .frame(maxWidth: .infinity).padding(.top, 60)
            case .idle:
                EmptyView()
            }
        }
        .onAppear {
            if let id = playlistId {
                playlistVM.loadPlaylistById(id)
            }
        }
    }
}

/// 歌单管理页 - 对应 Android PlaylistManageScreen.kt
struct PlaylistManageScreen: View {
    @Environment(HMPTheme.self) private var theme
    @State private var playlistVM = PlaylistViewModel()
    @State private var showCreateDialog = false

    var body: some View {
        SubScreen(title: "歌单管理") {
            if playlistVM.userCustomPlaylists.isEmpty {
                VStack(spacing: 16) {
                    Image(systemName: "music.note.list").font(.system(size: 48)).foregroundColor(theme.text.opacity(0.4))
                    Text("暂无歌单").font(TypographyTokens.titleLarge).foregroundColor(theme.text.opacity(0.4))
                    Button("创建歌单") {
                        HapticManager.shared.click()
                        showCreateDialog = true
                    }
                    .buttonStyle(.borderedProminent)
                    .padding(.top, 8)
                }
                .frame(maxWidth: .infinity).padding(.top, 60)
            } else {
                List {
                    ForEach(Array(playlistVM.userCustomPlaylists.enumerated()), id: \.element.id) { index, playlist in
                        HStack(spacing: 12) {
                            RoundedRectangle(cornerRadius: 8).fill(theme.primary.opacity(0.12)).frame(width: 44, height: 44)
                                .overlay { Image(systemName: "music.note.list").foregroundColor(theme.primary).font(.system(size: 16)) }
                            VStack(alignment: .leading, spacing: 2) {
                                Text(playlist.name).font(TypographyTokens.bodyMedium).foregroundColor(theme.text)
                                Text("\(playlist.songCount) 首").font(TypographyTokens.bodySmall).foregroundColor(theme.text.opacity(0.6))
                            }
                            Spacer()
                        }
                        .padding(.vertical, 4)
                        .swipeActions(edge: .trailing, allowsFullSwipe: true) {
                            Button(role: .destructive) {
                                playlistVM.deletePlaylist(id: playlist.id)
                            } label: {
                                Label("删除", systemImage: "trash")
                            }
                        }
                    }
                }
                .listStyle(.insetGrouped)
            }
        }
        .sheet(isPresented: $showCreateDialog) {
            CreatePlaylistDialog { name in
                playlistVM.createPlaylist(name: name)
            }
        }
        .onAppear {
            playlistVM.loadUserCustomPlaylists()
        }
    }
}
