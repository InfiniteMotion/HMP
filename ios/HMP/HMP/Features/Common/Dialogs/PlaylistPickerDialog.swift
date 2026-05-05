import SwiftUI
import shared

/// 单选歌单 Picker Dialog - 对应 Android PlaylistPickerDialog.kt
struct PlaylistPickerDialog: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(HMPTheme.self) private var theme

    let playlists: [Playlist_]
    let title: String
    let onSelect: (Playlist_) -> Void

    var body: some View {
        NavigationStack {
            List {
                ForEach(playlists, id: \.id) { playlist in
                    Button {
                        HapticManager.shared.click()
                        onSelect(playlist)
                        dismiss()
                    } label: {
                        HStack(spacing: 12) {
                            // Cover image
                            RoundedRectangle(cornerRadius: 8)
                                .fill(theme.primary.opacity(0.12))
                                .frame(width: 44, height: 44)
                                .overlay {
                                    Image(systemName: "music.note.list")
                                        .foregroundColor(theme.primary)
                                        .font(.system(size: 16))
                                }

                            VStack(alignment: .leading, spacing: 2) {
                                Text(playlist.name)
                                    .font(TypographyTokens.bodyMedium)
                                    .foregroundColor(theme.text)
                                    .lineLimit(1)

                                // Stats row
                                Text(statsText(for: playlist))
                                    .font(TypographyTokens.bodySmall)
                                    .foregroundColor(theme.text.opacity(0.6))
                            }

                            Spacer()

                            Image(systemName: "chevron.right")
                                .font(.system(size: 14))
                                .foregroundColor(theme.text.opacity(0.3))
                        }
                        .padding(.vertical, 4)
                    }
                    .buttonStyle(.plain)
                }
            }
            .listStyle(.plain)
            .navigationTitle(title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("取消") {
                        HapticManager.shared.click()
                        dismiss()
                    }
                }
            }
        }
        .presentationDetents([.medium])
    }

    private func statsText(for playlist: Playlist_) -> String {
        var parts: [String] = []
        if playlist.songCount > 0 {
            parts.append("\(playlist.songCount) 首")
        }
        if playlist.totalDurationMs > 0 {
            let minutes = playlist.totalDurationMs / 60000
            if minutes > 0 {
                parts.append("\(minutes) 分钟")
            }
        }
        if playlist.playCount > 0 {
            parts.append("\(playlist.playCount) 次播放")
        }
        return parts.joined(separator: " · ")
    }
}
