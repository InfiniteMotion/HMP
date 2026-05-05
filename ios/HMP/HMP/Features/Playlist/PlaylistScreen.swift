import SwiftUI
import shared

/// 播放列表页 - 对应 Android PlaylistScreen.kt
struct PlaylistScreen: View {
    @Environment(HMPTheme.self) private var theme
    @State private var playlistVM = PlaylistViewModel()
    @State private var selectedMusicId: Int64? = nil
    @State private var showEditDialog = false
    @State private var showAddSongDialog = false

    var playlistName: String? = nil
    var playlistId: Int64? = nil

    private var controller: MusicPlayerController { MusicPlayerController.shared }

    private var musicList: [MusicInfo_] {
        switch playlistVM.selectedPlaylistState {
        case .success(let list): return list
        default: return []
        }
    }

    var body: some View {
        SubScreen(
            title: playlistName ?? playlistVM.playlistMeta?.name ?? "播放列表"
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
            case .success:
                MusicList(
                    musicInfoList: musicList,
                    config: {
                        let cb = MusicListCallbacks()
                        cb.onItemClick = { info, _ in
                            controller.addAllToPlaylistInOrder(musicList)
                            if let idx = musicList.firstIndex(where: { $0.music.id == info.music.id }) {
                                controller.playAt(idx)
                            }
                            if let id = playlistId {
                                playlistVM.recordPlaylistPlay(playlistId: id)
                            }
                        }
                        cb.onPinToTop = { info in
                            guard let id = playlistId else { return }
                            var ids = musicList.map { $0.music.id }
                            ids.removeAll { $0 == info.music.id }
                            ids.insert(info.music.id, at: 0)
                            playlistVM.reorderPlaylistItems(playlistId: id, orderedMusicIds: ids)
                        }
                        cb.onRemove = { info in
                            if let id = playlistId {
                                playlistVM.removeItemFromPlaylist(musicId: info.music.id, playlistId: id)
                            }
                        }
                        cb.onMenuClick = { info in
                            selectedMusicId = info.music.id
                        }
                        return MusicListConfig.playlistPreset(
                            onOrderPlay: {
                                controller.addAllToPlaylistInOrder(musicList)
                                if let id = playlistId { playlistVM.recordPlaylistPlay(playlistId: id) }
                            },
                            onShufflePlay: {
                                controller.addAllToPlaylistByShuffle(musicList)
                                if let id = playlistId { playlistVM.recordPlaylistPlay(playlistId: id) }
                            },
                            callbacks: cb
                        )
                    }()
                )
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
        .toolbar {
            ToolbarItemGroup(placement: .navigationBarTrailing) {
                if playlistId != nil {
                    Button {
                        HapticManager.shared.click()
                        showEditDialog = true
                    } label: {
                        Image(systemName: "pencil")
                            .font(.system(size: 16))
                    }

                    Button {
                        HapticManager.shared.click()
                        showAddSongDialog = true
                    } label: {
                        Image(systemName: "plus")
                            .font(.system(size: 16))
                    }
                }
            }
        }
        .onAppear {
            if let id = playlistId {
                playlistVM.loadPlaylistById(id)
                playlistVM.loadPlaylistMeta(id: id)
            } else if let name = playlistName, let labelName = playlistVM.getLabelName(name) {
                playlistVM.loadPlaylistByLabel(labelName)
            }
        }
        .background {
            if let musicId = selectedMusicId {
                NavigationLink(value: HMPRoute.songDetail(musicId: musicId)) {
                    EmptyView()
                }
                .hidden()
            }
        }
        .sheet(isPresented: $showEditDialog) {
            if let id = playlistId, let meta = playlistVM.playlistMeta {
                CreatePlaylistDialog(
                    onCreate: { newName in
                        playlistVM.renamePlaylist(id: id, newName: newName)
                        playlistVM.loadPlaylistMeta(id: id)
                    },
                    isEditing: true,
                    initialName: meta.name
                )
            }
        }
        .sheet(isPresented: $showAddSongDialog) {
            if let id = playlistId {
                AddSongToPlaylistSheet(playlistId: id, playlistVM: playlistVM)
            }
        }
    }
}

/// 添加歌曲到歌单的 Sheet 包装
private struct AddSongToPlaylistSheet: View {
    let playlistId: Int64
    let playlistVM: PlaylistViewModel
    @State private var allMusic: [MusicInfo_] = []
    @State private var isLoading = true

    private var currentIds: Set<Int64> {
        switch playlistVM.selectedPlaylistState {
        case .success(let list): return Set(list.map { $0.music.id })
        default: return []
        }
    }

    var body: some View {
        Group {
            if isLoading {
                ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                AddSongToPlaylistDialog(
                    allMusic: allMusic,
                    currentInPlaylistIds: currentIds,
                    onAdd: { musicId, musicPath in
                        playlistVM.addItemToPlaylist(playlistId: playlistId, musicId: musicId, musicPath: musicPath)
                    }
                )
            }
        }
        .task {
            allMusic = await playlistVM.loadAllMusicForAddPicker()
            isLoading = false
        }
    }
}

/// 歌单管理页 - 对应 Android PlaylistManageScreen.kt
struct PlaylistManageScreen: View {
    @Environment(HMPTheme.self) private var theme
    @State private var playlistVM = PlaylistViewModel()
    @State private var showCreateDialog = false
    @State private var renamingPlaylist: Playlist_? = nil
    @State private var renameText: String = ""

    private var sortedPlaylists: [Playlist_] {
        playlistVM.userCustomPlaylists.sorted { a, b in
            if a.isPinned != b.isPinned { return a.isPinned }
            return (a.lastPlayedAt?.int64Value ?? 0) > (b.lastPlayedAt?.int64Value ?? 0)
        }
    }

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
                    ForEach(sortedPlaylists, id: \.id) { playlist in
                        HStack(spacing: 12) {
                            // Cover
                            if let coverUri = playlist.coverUri, !coverUri.isEmpty, let image = CoverCache.shared.get(path: coverUri) {
                                Image(uiImage: image)
                                    .resizable()
                                    .aspectRatio(contentMode: .fill)
                                    .frame(width: 44, height: 44)
                                    .clipShape(RoundedRectangle(cornerRadius: 8))
                            } else {
                                RoundedRectangle(cornerRadius: 8).fill(theme.primary.opacity(0.12)).frame(width: 44, height: 44)
                                    .overlay { Image(systemName: "music.note.list").foregroundColor(theme.primary).font(.system(size: 16)) }
                            }

                            VStack(alignment: .leading, spacing: 2) {
                                HStack(spacing: 4) {
                                    if playlist.isPinned {
                                        Image(systemName: "pin.fill")
                                            .font(.system(size: 10))
                                            .foregroundColor(theme.primary)
                                    }
                                    Text(playlist.name).font(TypographyTokens.bodyMedium).foregroundColor(theme.text)
                                }
                                statsText(for: playlist)
                                    .font(TypographyTokens.bodySmall)
                                    .foregroundColor(theme.text.opacity(0.6))
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
                            Button {
                                playlistVM.setPlaylistPinned(id: playlist.id, isPinned: !playlist.isPinned)
                            } label: {
                                Label(playlist.isPinned ? "取消置顶" : "置顶", systemImage: playlist.isPinned ? "pin.slash" : "pin")
                            }
                            .tint(.orange)
                        }
                        .swipeActions(edge: .leading, allowsFullSwipe: false) {
                            Button {
                                renamingPlaylist = playlist
                                renameText = playlist.name
                            } label: {
                                Label("重命名", systemImage: "pencil")
                            }
                            .tint(.blue)
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
        .alert("重命名歌单", isPresented: Binding(
            get: { renamingPlaylist != nil },
            set: { if !$0 { renamingPlaylist = nil } }
        )) {
            TextField("歌单名称", text: $renameText)
            Button("取消", role: .cancel) { renamingPlaylist = nil }
            Button("确定") {
                if let playlist = renamingPlaylist, !renameText.trimmingCharacters(in: .whitespaces).isEmpty {
                    playlistVM.renamePlaylist(id: playlist.id, newName: renameText.trimmingCharacters(in: .whitespaces))
                }
                renamingPlaylist = nil
            }
        }
        .onAppear {
            playlistVM.loadUserCustomPlaylists()
        }
    }

    private func statsText(for playlist: Playlist_) -> Text {
        var parts: [String] = []
        parts.append("\(playlist.songCount) 首")
        if playlist.totalDurationMs > 0 {
            let minutes = playlist.totalDurationMs / 60000
            if minutes > 0 { parts.append("\(minutes) 分钟") }
        }
        return Text(parts.joined(separator: " · "))
    }
}
