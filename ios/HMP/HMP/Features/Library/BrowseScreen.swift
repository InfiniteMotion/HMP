import SwiftUI
import shared

struct BrowseScreen: View {
    @Environment(HMPTheme.self) private var theme
    @StateObject private var libraryVM = LibraryViewModel()
    @StateObject private var playlistVM = PlaylistViewModel()
    
    enum BrowseTab: Int, CaseIterable, Identifiable {
        case songs = 0
        case artists = 1
        case albums = 2
        case folders = 3
        case labels = 4
        
        var id: Int { rawValue }
        
        var title: String {
            switch self {
            case .songs: return "歌曲"
            case .artists: return "歌手"
            case .albums: return "专辑"
            case .folders: return "文件夹"
            case .labels: return "标签"
            }
        }
        
        var icon: String {
            switch self {
            case .songs: return "music.note.list"
            case .artists: return "person.3"
            case .albums: return "rectangle.stack"
            case .folders: return "folder"
            case .labels: return "tag"
            }
        }
    }
    
    @State private var selectedTab: BrowseTab = .songs
    
    var body: some View {
        TabScreen(title: "音乐库", hasSearchButton: true) {
            VStack(spacing: 0) {
                tabSelector
                Divider()
                tabContent
            }
        }
        .onAppear {
            playlistVM.loadLabels()
            playlistVM.loadBrowseCategories()
        }
    }
    
    private var tabSelector: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(BrowseTab.allCases) { tab in
                    Button {
                        HapticManager.shared.click()
                        selectedTab = tab
                    } label: {
                        HStack(spacing: 6) {
                            Image(systemName: tab.icon)
                                .font(.system(size: 14, weight: .medium))
                            Text(tab.title)
                                .font(TypographyTokens.bodySmall)
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                        .background(selectedTab == tab ? theme.primary : theme.surface)
                        .foregroundColor(selectedTab == tab ? .white : theme.text)
                        .clipShape(Capsule())
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
        }
    }
    
    @ViewBuilder
    private var tabContent: some View {
        switch selectedTab {
        case .songs:
            songsTab
        case .artists:
            artistsTab
        case .albums:
            albumsTab
        case .folders:
            foldersTab
        case .labels:
            labelsTab
        }
    }
    
    private var songsTab: some View {
        GalleryScreenContent(viewModel: libraryVM)
    }
    
    @ViewBuilder
    private var artistsTab: some View {
        switch playlistVM.browseState {
        case .idle, .loading:
            ProgressView()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        case .empty:
            emptyState("暂无艺术家")
        case .success:
            if playlistVM.artistList.isEmpty {
                emptyState("暂无艺术家")
            } else {
                List(playlistVM.artistList, id: \.self) { artist in
                    NavigationLink {
                        MusicListByCategoryScreen(category: .artist(artist))
                    } label: {
                        HStack {
                            Image(systemName: "person.circle")
                                .font(.system(size: 40))
                                .foregroundColor(theme.secondary)
                            VStack(alignment: .leading, spacing: 4) {
                                Text(artist)
                                    .font(TypographyTokens.titleMedium)
                                    .foregroundColor(theme.text)
                            }
                            Spacer()
                            Image(systemName: "chevron.right")
                                .foregroundColor(theme.text.opacity(0.4))
                        }
                        .padding(.vertical, 4)
                    }
                }
                .listStyle(.plain)
            }
        case .error(let msg):
            emptyState(msg)
        }
    }
    
    @ViewBuilder
    private var albumsTab: some View {
        switch playlistVM.browseState {
        case .idle, .loading:
            ProgressView()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        case .empty:
            emptyState("暂无专辑")
        case .success:
            if playlistVM.albumList.isEmpty {
                emptyState("暂无专辑")
            } else {
                List(playlistVM.albumList, id: \.self) { album in
                    NavigationLink {
                        MusicListByCategoryScreen(category: .album(album))
                    } label: {
                        HStack {
                            Image(systemName: "square.stack")
                                .font(.system(size: 40))
                                .foregroundColor(theme.secondary)
                            VStack(alignment: .leading, spacing: 4) {
                                Text(album)
                                    .font(TypographyTokens.titleMedium)
                                    .foregroundColor(theme.text)
                            }
                            Spacer()
                            Image(systemName: "chevron.right")
                                .foregroundColor(theme.text.opacity(0.4))
                        }
                        .padding(.vertical, 4)
                    }
                }
                .listStyle(.plain)
            }
        case .error(let msg):
            emptyState(msg)
        }
    }
    
    @ViewBuilder
    private var foldersTab: some View {
        switch playlistVM.browseState {
        case .idle, .loading:
            ProgressView()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        case .empty:
            emptyState("暂无文件夹")
        case .success:
            if playlistVM.folderList.isEmpty {
                emptyState("暂无文件夹")
            } else {
                List(playlistVM.folderList, id: \.self) { folder in
                    NavigationLink {
                        FolderMusicScreen(folderPath: folder)
                    } label: {
                        HStack {
                            Image(systemName: "folder.fill")
                                .font(.system(size: 40))
                                .foregroundColor(theme.secondary)
                            VStack(alignment: .leading, spacing: 4) {
                                Text((folder as NSString).lastPathComponent)
                                    .font(TypographyTokens.titleMedium)
                                    .foregroundColor(theme.text)
                                Text(folder)
                                    .font(TypographyTokens.bodySmall)
                                    .foregroundColor(theme.text.opacity(0.6))
                                    .lineLimit(1)
                            }
                            Spacer()
                            Image(systemName: "chevron.right")
                                .foregroundColor(theme.text.opacity(0.4))
                        }
                        .padding(.vertical, 4)
                    }
                }
                .listStyle(.plain)
            }
        case .error(let msg):
            emptyState(msg)
        }
    }
    
    @ViewBuilder
    private var labelsTab: some View {
        if playlistVM.labelList.isEmpty {
            emptyState("暂无标签")
        } else {
            ScrollView {
                LazyVGrid(columns: [
                    GridItem(.flexible(), spacing: 12),
                    GridItem(.flexible(), spacing: 12)
                ], spacing: 12) {
                    ForEach(playlistVM.labelList, id: \.self) { label in
                        NavigationLink {
                            LabelPlaylistScreen(labelName: label)
                        } label: {
                            LabelCard(labelName: label)
                        }
                    }
                }
                .padding(16)
            }
        }
    }
    
    private func emptyState(_ message: String) -> some View {
        VStack(spacing: 20) {
            Image(systemName: "music.note.list")
                .font(.system(size: 60))
                .foregroundColor(theme.text.opacity(0.3))
            Text(message)
                .font(TypographyTokens.titleLarge)
                .foregroundColor(theme.text.opacity(0.4))
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

struct GalleryScreenContent: View {
    @Environment(HMPTheme.self) private var theme
    @ObservedObject var viewModel: LibraryViewModel
    
    private var controller: MusicPlayerController { MusicPlayerController.shared }
    
    private var sortBy: SortOption {
        SortOption.allCases.first { $0.orderByKey == viewModel.orderBy } ?? .title
    }
    
    private var currentPlayingIndex: Int? {
        guard let currentId = controller.currentPlayingMusic?.music.id else { return nil }
        return viewModel.musicList.firstIndex { $0.music.id == currentId }
    }
    
    var body: some View {
        Group {
            if viewModel.musicList.isEmpty {
                emptyState
            } else {
                VStack(spacing: 0) {
                    sortHeader
                    ZStack(alignment: .trailing) {
                        MusicList(
                            musicInfoList: viewModel.musicList,
                            config: {
                                let cb = MusicListCallbacks()
                                cb.onItemClick = { info, _ in
                                    HapticManager.shared.click()
                                    controller.playWith(info)
                                }
                                return MusicListConfig(
                                    header: .none,
                                    item: .gallery(showMenu: true, extraMenuItems: [("加入播放列表", { info in
                                    controller.addToNextPlay(info) })]),
                                    currentPlayingIndex: currentPlayingIndex,
                                    callbacks: cb
                                )
                            }()
                        )
                        IndexStrip(
                            musicList: viewModel.musicList,
                            isLetterMode: sortBy.isLetterMode,
                            isReversed: viewModel.orderType == "DESC",
                            orderBy: sortBy.orderByKey
                        )
                    }
                }
            }
        }
    }
    
    private var sortHeader: some View {
        HStack(spacing: 8) {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 6) {
                    ForEach(SortOption.allCases, id: \.self) { option in
                    Button {
                        HapticManager.shared.click()
                        viewModel.selectSortOption(option.orderByKey)
                    } label: {
                        Text(option.rawValue)
                            .font(TypographyTokens.bodySmall)
                            .foregroundColor(sortBy == option ? .white : theme.text)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(Capsule().fill(sortBy == option ? theme.primary : theme.surface))
                    }
                    .buttonStyle(.plain)
                }
                }
            }
            Button {
                HapticManager.shared.click()
                viewModel.toggleSortOrder()
            } label: {
                Image(systemName: viewModel.orderType == "ASC" ? "arrow.up" : "arrow.down")
                    .font(.system(size: 14))
                    .foregroundColor(theme.text)
                    .frame(width: 32, height: 32)
                    .background(theme.surface, in: Circle())
            }
            .buttonStyle(.plain)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
    }
    
    private var emptyState: some View {
        VStack(spacing: 20) {
            Image(systemName: "music.note.list")
                .font(.system(size: 60))
                .foregroundColor(theme.text.opacity(0.3))
            Text("暂无音乐")
                .font(TypographyTokens.titleLarge)
                .foregroundColor(theme.text.opacity(0.4))
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
    
    enum SortOption: String, CaseIterable {
        case title = "标题"
        case artist = "艺术家"
        case album = "专辑"
        case duration = "时长"
        case dateAdded = "添加时间"
        case playCount = "播放次数"
        
        var orderByKey: String {
            switch self {
            case .title: return "title"
            case .artist: return "artist"
            case .album: return "album"
            case .duration: return "duration"
            case .dateAdded: return "id"
            case .playCount: return "playCount"
            }
        }
        
        var isLetterMode: Bool {
            switch self {
            case .title, .artist, .album: return true
            default: return false
            }
        }
    }
}

struct LabelCard: View {
    @Environment(HMPTheme.self) private var theme
    let labelName: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Spacer()
            HStack {
                Spacer()
                Image(systemName: "tag")
                    .font(.system(size: 40))
                    .foregroundColor(.white.opacity(0.8))
                Spacer()
            }
            Spacer()
            Text(labelName)
                .font(TypographyTokens.titleMedium)
                .foregroundColor(.white)
                .lineLimit(2)
        }
        .padding(12)
        .frame(maxWidth: .infinity, minHeight: 120)
        .background(
            LinearGradient(
                gradient: Gradient(colors: [
                    theme.primary,
                    theme.primary.opacity(0.7)
                ]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

struct FolderMusicScreen: View {
    @Environment(HMPTheme.self) private var theme
    @Environment(\.dismiss) private var dismiss
    let folderPath: String
    @State private var musicList: [MusicInfo_] = []
    @State private var isLoading: Bool = true
    @StateObject private var vm = PlaylistViewModel()
    
    private var controller: MusicPlayerController { MusicPlayerController.shared }
    
    private var currentPlayingIndex: Int? {
        guard let currentId = controller.currentPlayingMusic?.music.id else { return nil }
        return musicList.firstIndex { $0.music.id == currentId }
    }
    
    var body: some View {
        Group {
            if isLoading {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if musicList.isEmpty {
                emptyState
            } else {
                VStack(spacing: 0) {
                    playButtons
                    MusicList(
                        musicInfoList: musicList,
                        config: {
                            let cb = MusicListCallbacks()
                            cb.onItemClick = { info, _ in
                                HapticManager.shared.click()
                                controller.addAllToPlaylistInOrder(musicList)
                                controller.playWith(info)
                            }
                            return MusicListConfig(
                                header: .none,
                                item: .gallery(showMenu: true, extraMenuItems: [("加入播放列表", { info in
                            controller.addToNextPlay(info) })]),
                                currentPlayingIndex: currentPlayingIndex,
                                callbacks: cb
                            )
                        }()
                    )
                }
            }
        }
        .navigationTitle((folderPath as NSString).lastPathComponent)
        .navigationBarTitleDisplayMode(.inline)
        .task {
            await loadMusic()
        }
    }
    
    private func loadMusic() async {
        isLoading = true
        do {
            let getAllMusic = KoinHelperKt.getGetAllMusicUseCase()
            let allMusic = try await getAllMusic.invoke(orderBy: "title", orderType: "ASC")
            musicList = allMusic.filter { ($0.music.path as NSString).deletingLastPathComponent == folderPath }
        } catch {
            print("[FolderMusic] Error loading: \(error)")
        }
        isLoading = false
    }
    
    private var playButtons: some View {
        HStack(spacing: 12) {
            Button {
                HapticManager.shared.click()
                controller.addAllToPlaylistByShuffle(musicList)
            } label: {
                HStack(spacing: 6) {
                    Image(systemName: "shuffle")
                        .font(.system(size: 14))
                    Text("随机播放")
                        .font(TypographyTokens.bodySmall)
                }
                .foregroundColor(theme.primary)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 10)
                .background(theme.primary.opacity(0.1), in: RoundedRectangle(cornerRadius: 10))
            }
            Button {
                HapticManager.shared.click()
                controller.addAllToPlaylistInOrder(musicList)
            } label: {
                HStack(spacing: 6) {
                    Image(systemName: "play.fill")
                        .font(.system(size: 14))
                    Text("顺序播放")
                        .font(TypographyTokens.bodySmall)
                }
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 10)
                .background(theme.primary, in: RoundedRectangle(cornerRadius: 10))
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
    }
    
    private var emptyState: some View {
        VStack(spacing: 20) {
            Image(systemName: "folder")
                .font(.system(size: 60))
                .foregroundColor(theme.text.opacity(0.3))
            Text("此文件夹暂无音乐")
                .font(TypographyTokens.titleLarge)
                .foregroundColor(theme.text.opacity(0.4))
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

struct LabelPlaylistScreen: View {
    @Environment(HMPTheme.self) private var theme
    @Environment(\.dismiss) private var dismiss
    let labelName: String
    @StateObject private var vm = PlaylistViewModel()
    
    private var controller: MusicPlayerController { MusicPlayerController.shared }
    
    private var currentPlayingIndex: Int? {
        guard let currentId = controller.currentPlayingMusic?.music.id else { return nil }
        return vm.selectedPlaylistMusic.firstIndex { $0.music.id == currentId }
    }
    
    var body: some View {
        Group {
            switch vm.selectedPlaylistState {
            case .idle, .loading:
                ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
            case .empty:
                Text("暂无音乐").foregroundColor(theme.text.opacity(0.4))
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            case .success:
                VStack(spacing: 0) {
                    playButtons
                    musicListView
                }
            case .error(let msg):
                Text(msg).foregroundColor(theme.text.opacity(0.4))
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .navigationTitle(labelName)
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            if let labelNameObj = vm.getLabelName(labelName) {
                vm.loadPlaylistByLabel(labelNameObj)
            }
        }
    }
    
    private var playButtons: some View {
        HStack(spacing: 12) {
            Button {
                HapticManager.shared.click()
                controller.addAllToPlaylistByShuffle(vm.selectedPlaylistMusic)
            } label: {
                HStack(spacing: 6) {
                    Image(systemName: "shuffle").font(.system(size: 14))
                    Text("随机播放").font(TypographyTokens.bodySmall)
                }
                .foregroundColor(theme.primary)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 10)
                .background(theme.primary.opacity(0.1), in: RoundedRectangle(cornerRadius: 10))
            }
            Button {
                HapticManager.shared.click()
                controller.addAllToPlaylistInOrder(vm.selectedPlaylistMusic)
            } label: {
                HStack(spacing: 6) {
                    Image(systemName: "play.fill").font(.system(size: 14))
                    Text("顺序播放").font(TypographyTokens.bodySmall)
                }
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 10)
                .background(theme.primary, in: RoundedRectangle(cornerRadius: 10))
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
    }
    
    private var musicListView: some View {
        let cb = MusicListCallbacks()
        cb.onItemClick = { info, _ in
            HapticManager.shared.click()
            controller.addAllToPlaylistInOrder(vm.selectedPlaylistMusic)
            controller.playWith(info)
        }
        cb.onMenuClick = { _ in HapticManager.shared.click() }
        return FixedMusicList(
            musicInfoList: vm.selectedPlaylistMusic,
            config: MusicListConfig(
                header: .none,
                item: ItemConfig.full(showRemove: false, showMenu: true),
                currentPlayingIndex: currentPlayingIndex,
                callbacks: cb
            )
        )
    }
}
