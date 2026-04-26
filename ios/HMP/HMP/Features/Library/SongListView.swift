import SwiftUI
import MediaPlayer

struct SongListView: View {
    @Environment(HMPTheme.self) private var theme
    @State private var songs: [SongItem] = []
    @State private var isLoading = false
    @State private var authStatus: MusicLibraryService.AuthorizationStatus = .notDetermined
    @State private var showAuthAlert = false

    var body: some View {
        Group {
            if isLoading {
                ProgressView("正在扫描...")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if songs.isEmpty {
                VStack(spacing: 16) {
                    Image(systemName: "music.note.list")
                        .font(.system(size: 48))
                        .foregroundColor(theme.secondaryText)
                    Text("暂无音乐")
                        .font(TypographyTokens.bodyLarge)
                        .foregroundColor(theme.secondaryText)
                    Text("请在设置中授予音乐库访问权限")
                        .font(TypographyTokens.bodySmall)
                        .foregroundColor(theme.secondaryText)
                    if authStatus == .denied || authStatus == .notDetermined {
                        Button("授权访问音乐库") {
                            requestAuthorization()
                        }
                        .buttonStyle(.borderedProminent)
                    }
                }
                .frame(maxWidth: .infinity)
                .padding(.top, 40)
            } else {
                List {
                    ForEach(songs) { song in
                        SongRowView(song: song)
                    }
                }
                .listStyle(.plain)
            }
        }
        .onAppear {
            checkAuthorization()
        }
        .alert("需要音乐库权限", isPresented: $showAuthAlert) {
            Button("打开设置") {
                if let url = URL(string: UIApplication.openSettingsURLString) {
                    UIApplication.shared.open(url)
                }
            }
            Button("取消", role: .cancel) {}
        } message: {
            Text("HMP 需要访问您的音乐库来显示和管理音乐。请在设置中授权。")
        }
    }

    private func checkAuthorization() {
        authStatus = MusicLibraryService.shared.checkAuthorizationStatus()
        if authStatus == .authorized {
            loadSongs()
        } else if authStatus == .notDetermined {
            requestAuthorization()
        }
    }

    private func requestAuthorization() {
        MusicLibraryService.shared.requestAuthorization { status in
            DispatchQueue.main.async {
                authStatus = status
                if status == .authorized {
                    loadSongs()
                } else if status == .denied {
                    showAuthAlert = true
                }
            }
        }
    }

    private func loadSongs() {
        isLoading = true
        DispatchQueue.global(qos: .userInitiated).async {
            let items = MusicLibraryService.shared.fetchAllSongs()
            let songItems = items.compactMap { item -> SongItem? in
                guard let metadata = MusicLibraryService.shared.fetchSongMetadata(item: item) else {
                    return nil
                }
                return SongItem(
                    id: item.hash,
                    title: metadata.title,
                    artist: metadata.artist,
                    album: metadata.album,
                    duration: metadata.duration,
                    persistentID: item.persistentID
                )
            }
            DispatchQueue.main.async {
                songs = songItems
                isLoading = false
            }
        }
    }
}

struct SongItem: Identifiable {
    let id: Int
    let title: String
    let artist: String
    let album: String
    let duration: TimeInterval
    let persistentID: MPMediaEntityPersistentID
}

struct SongRowView: View {
    @Environment(HMPTheme.self) private var theme
    let song: SongItem

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "music.note")
                .font(.system(size: 24))
                .frame(width: 40, height: 40)
                .cornerRadius(8)

            VStack(alignment: .leading, spacing: 2) {
                Text(song.title)
                    .font(TypographyTokens.bodyMedium)
                    .lineLimit(1)
                Text(song.artist)
                    .font(TypographyTokens.bodySmall)
                    .foregroundColor(theme.secondaryText)
                    .lineLimit(1)
            }

            Spacer()

            Text(formatDuration(song.duration))
                .font(TypographyTokens.bodySmall)
                .foregroundColor(theme.secondaryText)
        }
        .padding(.vertical, 4)
    }

    private func formatDuration(_ duration: TimeInterval) -> String {
        let minutes = Int(duration) / 60
        let seconds = Int(duration) % 60
        return String(format: "%d:%02d", minutes, seconds)
    }
}
