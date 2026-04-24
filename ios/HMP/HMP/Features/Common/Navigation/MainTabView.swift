import SwiftUI

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
    @State private var scannedFiles: [URL] = []
    @State private var isScanning: Bool = false

    var body: some View {
        NavigationStack {
            TabScreen(
                title: "音乐库",
                hasSearchButton: true
            ) {
                VStack(spacing: 20) {
                    Text("音乐库内容")
                        .font(TypographyTokens.bodyLarge)
                        .foregroundColor(theme.text)

                    Button(action: { scanMusic() }) {
                        Text(isScanning ? "扫描中..." : "扫描音乐")
                            .padding()
                            .background(theme.primary)
                            .foregroundColor(.white)
                            .cornerRadius(8)
                    }

                    if !scannedFiles.isEmpty {
                        List(scannedFiles, id: \.self) {
                            Text($0.lastPathComponent)
                        }
                        .frame(height: 200)
                    }
                }
                .padding()
            }
        }
    }

    private func scanMusic() {
        isScanning = true
        DispatchQueue.global().async {
            let files = MusicScannerService.shared.scanMusicFiles()
            DispatchQueue.main.async {
                scannedFiles = files
                isScanning = false
            }
        }
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
