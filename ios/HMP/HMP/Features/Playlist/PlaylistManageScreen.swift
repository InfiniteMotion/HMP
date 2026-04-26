import SwiftUI
import shared

/// 播放列表管理页 - 对应 Android PlaylistManageScreen.kt
struct PlaylistManageScreenOld: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(HMPTheme.self) private var theme
    @State private var playlistVM = PlaylistViewModel()

    var body: some View {
        NavigationStack {
            List {
                Section("我的歌单") {
                    ForEach(Array(playlistVM.userCustomPlaylists.enumerated()), id: \.element.id) { _, playlist in
                        playlistRow(playlist)
                    }
                }
            }
            .listStyle(.insetGrouped)
            .navigationTitle("歌单管理")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("关闭") { dismiss() }
                }
            }
        }
        .onAppear {
            playlistVM.loadUserCustomPlaylists()
        }
    }

    private func playlistRow(_ playlist: Playlist_) -> some View {
        HStack(spacing: 12) {
            RoundedRectangle(cornerRadius: 8)
                .fill(theme.primary.opacity(0.12))
                .frame(width: 40, height: 40)
                .overlay {
                    Image(systemName: "music.note.list")
                        .foregroundColor(theme.primary)
                        .font(.system(size: 14))
                }
            VStack(alignment: .leading, spacing: 2) {
                Text(playlist.name)
                    .font(TypographyTokens.bodyMedium)
                    .foregroundColor(theme.text)
                Text("\(playlist.songCount) 首")
                    .font(TypographyTokens.bodySmall)
                    .foregroundColor(theme.text.opacity(0.6))
            }
            Spacer()
            if playlist.isPinned {
                Image(systemName: "pin.fill")
                    .font(.system(size: 12))
                    .foregroundColor(theme.primary)
            }
        }
        .swipeActions(edge: .trailing, allowsFullSwipe: true) {
            Button(role: .destructive) {
                playlistVM.deletePlaylist(id: playlist.id)
            } label: {
                Label("删除", systemImage: "trash")
            }
        }
    }
}
