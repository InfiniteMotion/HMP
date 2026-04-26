import SwiftUI
import shared

/// 主屏幕 - 对应 Android MainScreen.kt
/// ZStack: DynamicBackground + NavigationStack(TabView) + MiniPlayerBar overlay
struct MainScreen: View {
    @Environment(HMPTheme.self) private var theme
    @State private var selectedTab: TabItem = .home

    var body: some View {
        NavigationStack {
            ZStack(alignment: .bottom) {
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

                        GalleryScreen()
                            .tag(TabItem.gallery)

                        ListScreen()
                            .tag(TabItem.list)

                        UserScreen()
                            .tag(TabItem.user)
                    }
                    .tabViewStyle(.page(indexDisplayMode: .never))
                }

                // MiniPlayerBar overlay - fullScreenCover 打开播放器
                MiniPlayerBar()
            }
            .navigationDestination(for: HMPRoute.self) { route in
                routeDestination(for: route)
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
            Text("SongDetail \(musicId)")
        case .artist(let name):
            Text("Artist \(name)")
        case .album(let name):
            Text("Album \(name)")
        case .playlist(let name):
            PlaylistScreen(playlistName: name)
        case .customPlaylist(let playlistId):
            PlaylistScreen(playlistId: playlistId)
        case .userPlaylistManage:
            PlaylistManageScreen()
        case .setting:
            Text("Setting")
        case .profileSettings:
            Text("ProfileSettings")
        case .backupSettings:
            Text("BackupSettings")
        case .librarySettings:
            Text("LibrarySettings")
        case .ai:
            Text("AI")
        case .custom:
            CustomScreen()
        case .userUsageData:
            Text("UserUsageData")
        default:
            Text("Unknown route")
        }
    }
}
