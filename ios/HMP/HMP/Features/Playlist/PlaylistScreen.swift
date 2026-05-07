import SwiftUI
import shared

private let MAX_HEADER_COLLAPSE: CGFloat = 160
private let HEADER_EXPAND_DAMPING: CGFloat = 0.8

/// 播放列表页 - 对应 Android PlaylistScreen.kt
struct PlaylistScreen: View {
    @Environment(HMPTheme.self) private var theme
    @State private var playlistVM = PlaylistViewModel()
    @State private var selectedMusicId: Int64? = nil
    @State private var showEditDialog = false
    @State private var showAddSongDialog = false
    @State private var headerCollapseOffset: CGFloat = 0

    var playlistName: String? = nil
    var playlistId: Int64? = nil

    private var controller: MusicPlayerController { MusicPlayerController.shared }

    private var isCustomPlaylist: Bool { playlistId != nil }
    private var shouldCollapseHeader: Bool { isCustomPlaylist && headerCollapseOffset > 1 }

    private var musicList: [MusicInfo_] {
        switch playlistVM.selectedPlaylistState {
        case .success(let list): return list
        default: return []
        }
    }

    var body: some View {
        SubScreen(
            title: shouldCollapseHeader ? (playlistName ?? playlistVM.playlistMeta?.name ?? "播放列表") : "",
            largeTitle: !shouldCollapseHeader
        ) {
            switch playlistVM.selectedPlaylistState {
            case .idle, .loading:
                ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
            case .empty:
                VStack(spacing: 12) {
                    Image(systemName: "music.note").font(.system(size: 40)).foregroundColor(theme.text.opacity(0.4))
                    Text("播放列表为空").font(TypographyTokens.bodyMedium).foregroundColor(theme.text.opacity(0.4))
                }
                .frame(maxWidth: .infinity).padding(.top, 60)
            case .success:
                playlistContent
            case .error(let message):
                VStack(spacing: 12) {
                    Text("加载失败").font(TypographyTokens.bodyMedium).foregroundColor(theme.text.opacity(0.4))
                    Text(message).font(TypographyTokens.bodySmall).foregroundColor(theme.text.opacity(0.4))
                }
                .frame(maxWidth: .infinity).padding(.top, 60)
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
            headerCollapseOffset = 0
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
                    onCreate: { newName, newDesc in
                        playlistVM.renamePlaylist(id: id, newName: newName)
                        playlistVM.updatePlaylistDescription(id: id, description: newDesc.isEmpty ? nil : newDesc)
                    },
                    isEditing: true,
                    initialName: meta.name,
                    initialDescription: meta.description
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
            CreatePlaylistDialog { name, _ in
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

// MARK: - PlaylistScreen Extensions
private extension PlaylistScreen {
    var playlistContent: some View {
        let config = buildPlaylistConfig()
        return ScrollView {
            VStack(spacing: 0) {
                // Header with collapse animation
                PlaylistHeader(playlist: playlistVM.playlistMeta, musicList: musicList)
                    .opacity(shouldCollapseHeader ? 0 : 1)
                    .scaleEffect(shouldCollapseHeader ? 0.9 : 1)
                    .offset(y: -headerCollapseOffset * 0.3)
                    .animation(.easeInOut(duration: 0.2), value: shouldCollapseHeader)

                // Music list items in a single LazyVStack
                LazyVStack(spacing: 0) {
                    // Play/Shuffle controls header
                    switch config.header {
                    case .simple(let onOrderPlay, let onShufflePlay, let trailing):
                        SimpleHeader(count: musicList.count, onOrderPlay: onOrderPlay, onShufflePlay: onShufflePlay, trailing: trailing)
                    default:
                        EmptyView()
                    }

                    if !musicList.isEmpty {
                        Divider().padding(.vertical, 8)
                    }

                    // Song items
                    ForEach(Array(musicList.enumerated()), id: \.offset) { index, info in
                        MusicListItem(
                            musicInfo: info,
                            index: index,
                            config: config,
                            isCurrentPlaying: config.currentPlayingIndex == index,
                            isEditMode: false,
                            isSelected: false,
                            callbacks: config.callbacks
                        )
                        .id(index)
                    }
                }
                .padding(.horizontal, 8)
                .offset(y: -min(headerCollapseOffset, MAX_HEADER_COLLAPSE))
            }
        }
        .background(theme.background)
        .onScrollChange { offset in
            let delta = offset.y - headerCollapseOffset
            if delta < 0 {
                headerCollapseOffset = min(headerCollapseOffset - delta, MAX_HEADER_COLLAPSE)
            } else if delta > 0 {
                let damped = delta * HEADER_EXPAND_DAMPING
                headerCollapseOffset = max(headerCollapseOffset - damped, 0)
            }
        }
    }

    func buildPlaylistConfig() -> MusicListConfig {
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
        cb.onMoveUp = { index in
            guard let id = playlistId else { return }
            if index <= 0 || index >= musicList.count { return }
            var ids = musicList.map { $0.music.id }
            ids.swapAt(index, index - 1)
            playlistVM.reorderPlaylistItems(playlistId: id, orderedMusicIds: ids)
        }
        cb.onMoveDown = { index in
            guard let id = playlistId else { return }
            if index < 0 || index >= musicList.count - 1 { return }
            var ids = musicList.map { $0.music.id }
            ids.swapAt(index, index + 1)
            playlistVM.reorderPlaylistItems(playlistId: id, orderedMusicIds: ids)
        }
        cb.onRemove = { info in
            guard let id = playlistId else { return }
            playlistVM.removeItemFromPlaylist(musicId: info.music.id, playlistId: id)
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
    }
}

extension View {
    func onScrollChange(_ action: @escaping (CGPoint) -> Void) -> some View {
        self.modifier(ScrollChangeModifier(action: action))
    }
}

private struct ScrollChangeModifier: ViewModifier {
    let action: (CGPoint) -> Void
    
    func body(content: Content) -> some View {
        content
            .background(
                GeometryReader { proxy in
                    Color.clear
                        .preference(key: ScrollOffsetKey.self, value: proxy.frame(in: .global).origin)
                }
            )
            .onPreferenceChange(ScrollOffsetKey.self) { offset in
                action(offset)
            }
    }
}

private struct ScrollOffsetKey: PreferenceKey {
    static var defaultValue: CGPoint = .zero
    static func reduce(value: inout CGPoint, nextValue: () -> CGPoint) {}
}
