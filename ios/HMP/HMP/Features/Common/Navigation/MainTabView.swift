import SwiftUI
import shared

/// 主屏幕 - 对应 Android MainScreen.kt
/// ZStack: DynamicBackground + NavigationStack(TabView) + MiniPlayerBar overlay
struct MainScreen: View {
    @Environment(HMPTheme.self) private var theme
    @State private var selectedTab: TabItem = .home
    @State private var paletteColors = PaletteColors()
    @State private var backgroundStyle: BackgroundStyle = .fluid
    
    private var controller: MusicPlayerController { MusicPlayerController.shared }
    
    private var isPlaying: Bool { controller.isPlaying }
    private var currentMusic: MusicInfo_? { controller.currentPlayingMusic }
    private var albumArtUri: String? { currentMusic?.music.albumArtUri }
    private var musicPath: String? { currentMusic?.music.path }

    var body: some View {
        NavigationStack {
            ZStack(alignment: .bottom) {
                // 背景层 - 覆盖安全区
                if isPlaying {
                    DynamicBackground(
                        albumArtUri: albumArtUri,
                        musicPath: musicPath,
                        paletteColors: paletteColors,
                        isDark: theme.isDark,
                        style: backgroundStyle
                    )
                    .ignoresSafeArea()
                } else {
                    theme.background
                    .ignoresSafeArea()
                }
                
                // 内容层 - 只保留上方安全区padding，下方忽略
                VStack(spacing: 0) {
                    // Tab 页面指示器
                    TabPageIndicator(
                        currentPage: selectedTab.rawValue,
                        totalPages: TabItem.allCases.count,
                        activeColor: theme.primary,
                        inactiveColor: theme.text.opacity(0.3)
                    )

                    // TabView Pager
                    TabView(selection: $selectedTab) {
                        HomeScreen()
                            .tag(TabItem.home)
                            .ignoresSafeArea(edges: .bottom)

                        GalleryScreen()
                            .tag(TabItem.gallery)
                            .ignoresSafeArea(edges: .bottom)

                        ListScreen()
                            .tag(TabItem.list)
                            .ignoresSafeArea(edges: .bottom)

                        UserScreen()
                            .tag(TabItem.user)
                            .ignoresSafeArea(edges: .bottom)
                    }
                    .tabViewStyle(.page(indexDisplayMode: .never))
                }
                .ignoresSafeArea(edges: .bottom)

                // MiniPlayerBar overlay - fullScreenCover 打开播放器
                MiniPlayerBar()
            }
            .navigationDestination(for: HMPRoute.self) { route in
                routeDestination(for: route)
            }
        }
        .task(id: albumArtUri) {
            extractPalette()
        }
    }
    
    private func extractPalette() {
        if let uri = albumArtUri, !uri.isEmpty, let image = CoverCache.shared.get(path: uri) {
            paletteColors = PaletteExtractor.shared.extract(from: image) ?? PaletteColors()
        } else if let path = musicPath, !path.isEmpty {
            let extractor = ArtworkExtractor()
            if let coverPath = extractor.extractAndSave(filePath: path), 
               let image = UIImage(contentsOfFile: coverPath) {
                paletteColors = PaletteExtractor.shared.extract(from: image) ?? PaletteColors()
            }
        }
    }

    @ViewBuilder
    private func routeDestination(for route: HMPRoute) -> some View {
        switch route {
        case .player:
            PlayerScreen()
        case .lyrics:
            LyricsScreen()
        case .audioEffects:
            AudioEffectsScreen()
        case .search:
            SearchScreen()
        case .songDetail(let musicId):
            SongDetailScreen(musicId: musicId)
        case .artist(let name):
            MusicListByCategoryScreen(category: .artist(name))
        case .album(let name):
            MusicListByCategoryScreen(category: .album(name))
        case .playlist(let name):
            PlaylistScreen(playlistName: name)
        case .customPlaylist(let playlistId):
            PlaylistScreen(playlistId: playlistId)
        case .userPlaylistManage:
            PlaylistManageScreen()
        case .setting:
            SettingScreen()
        case .profileSettings:
            ProfileSettingsScreen()
        case .backupSettings:
            BackupSettingsScreen()
        case .librarySettings:
            LibrarySettingsScreen()
        case .ai:
            AIScreen()
        case .custom:
            CustomScreen()
        case .userUsageData:
            UserUsageDataScreen()
        default:
            Text("Unknown route")
        }
    }
}
