import SwiftUI
import shared

/// 添加歌曲到歌单 Dialog - 对应 Android AddSongToPlaylistDialog.kt
/// 点击即添加，过滤已在歌单中的歌曲
struct AddSongToPlaylistDialog: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(HMPTheme.self) private var theme

    let allMusic: [MusicInfo_]
    let currentInPlaylistIds: Set<Int64>
    let onAdd: (Int64, String) -> Void

    private var availableMusic: [MusicInfo_] {
        allMusic.filter { !currentInPlaylistIds.contains($0.music.id) }
    }

    var body: some View {
        NavigationStack {
            Group {
                if availableMusic.isEmpty {
                    VStack(spacing: 12) {
                        Image(systemName: "music.note")
                            .font(.system(size: 40))
                            .foregroundColor(theme.text.opacity(0.4))
                        Text("所有歌曲已在歌单中")
                            .font(TypographyTokens.bodyMedium)
                            .foregroundColor(theme.text.opacity(0.4))
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    List {
                        ForEach(availableMusic, id: \.music.id) { info in
                            Button {
                                HapticManager.shared.click()
                                onAdd(info.music.id, info.music.path)
                            } label: {
                                HStack(spacing: 12) {
                                    AlbumCover(uri: info.music.albumArtUri, musicPath: info.music.path, size: 40)

                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(info.music.title)
                                            .font(TypographyTokens.bodyMedium)
                                            .foregroundColor(theme.text)
                                            .lineLimit(1)
                                        Text(info.music.artist)
                                            .font(TypographyTokens.bodySmall)
                                            .foregroundColor(theme.text.opacity(0.6))
                                            .lineLimit(1)
                                    }

                                    Spacer()

                                    Image(systemName: "plus.circle")
                                        .foregroundColor(theme.primary)
                                        .font(.system(size: 20))
                                }
                                .padding(.vertical, 4)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("添加歌曲")
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
        .presentationDetents([.medium, .large])
    }
}
