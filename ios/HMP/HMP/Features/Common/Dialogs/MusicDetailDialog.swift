import SwiftUI

/// 歌曲详情对话框 - 对应 Android MusicDetailDialog.kt
/// 展示歌曲信息 + 操作菜单 (添加播放列表/喜欢/查看歌手等)
struct MusicDetailDialog: View {
    @Environment(\.dismiss) private var dismiss

    let musicId: Int64
    let title: String
    let artist: String
    let album: String
    let durationMs: Int64
    let albumArtUri: String?

    let onAddToPlaylist: () -> Void
    let onPlayNext: () -> Void
    let onGoToArtist: () -> Void
    let onGoToAlbum: () -> Void
    let onRemoveFromPlaylist: (() -> Void)?
    let onDelete: (() -> Void)?

    var body: some View {
        NavigationStack {
            Form {
                // 歌曲信息区
                Section {
                    HStack(spacing: 12) {
                        AlbumCover(uri: albumArtUri, size: 56)
                        VStack(alignment: .leading, spacing: 4) {
                            Text(title)
                                .font(TypographyTokens.titleMedium)
                            Text(artist)
                                .font(TypographyTokens.bodyMedium)
                            Text(album)
                                .font(TypographyTokens.bodySmall)
                        }
                    }
                    .padding(.vertical, 8)
                }

                // 操作区
                Section {
                    Button {
                        HapticManager.shared.click()
                        onAddToPlaylist()
                    } label: {
                        Label("添加到播放列表", systemImage: "plus")
                    }

                    Button {
                        HapticManager.shared.click()
                        onPlayNext()
                    } label: {
                        Label("下一首播放", systemImage: "play.fill")
                    }

                    Button {
                        HapticManager.shared.click()
                        onGoToArtist()
                    } label: {
                        Label("查看歌手", systemImage: "mic")
                    }

                    Button {
                        HapticManager.shared.click()
                        onGoToAlbum()
                    } label: {
                        Label("查看专辑", systemImage: "opticaldisc")
                    }
                }

                if let onDelete {
                    Section {
                        Button(role: .destructive) {
                            HapticManager.shared.reject()
                            onDelete()
                            dismiss()
                        } label: {
                            Label("删除", systemImage: "trash")
                        }
                    }
                }
            }
            .navigationTitle("歌曲操作")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("关闭") { dismiss() }
                }
            }
        }
        .presentationDetents([.medium, .large])
    }
}
