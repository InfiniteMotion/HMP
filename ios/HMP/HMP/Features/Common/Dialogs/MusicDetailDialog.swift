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
    let musicPath: String?
    let menuConfig: MusicDetailMenuConfig

    let onAddToPlaylist: () -> Void
    let onPlayNext: () -> Void
    let onGoToArtist: () -> Void
    let onGoToAlbum: () -> Void
    let onGoToDetail: (() -> Void)?
    let onShare: (() -> Void)?
    let onToggleFavorite: (() -> Void)?
    let onRemoveFromPlaylist: (() -> Void)?
    let onDelete: (() -> Void)?

    init(
        musicId: Int64,
        title: String,
        artist: String,
        album: String,
        durationMs: Int64 = 0,
        albumArtUri: String? = nil,
        musicPath: String? = nil,
        menuConfig: MusicDetailMenuConfig = MusicDetailMenuConfig(),
        onAddToPlaylist: @escaping () -> Void,
        onPlayNext: @escaping () -> Void,
        onGoToArtist: @escaping () -> Void,
        onGoToAlbum: @escaping () -> Void,
        onGoToDetail: (() -> Void)? = nil,
        onShare: (() -> Void)? = nil,
        onToggleFavorite: (() -> Void)? = nil,
        onRemoveFromPlaylist: (() -> Void)? = nil,
        onDelete: (() -> Void)? = nil
    ) {
        self.musicId = musicId
        self.title = title
        self.artist = artist
        self.album = album
        self.durationMs = durationMs
        self.albumArtUri = albumArtUri
        self.musicPath = musicPath
        self.menuConfig = menuConfig
        self.onAddToPlaylist = onAddToPlaylist
        self.onPlayNext = onPlayNext
        self.onGoToArtist = onGoToArtist
        self.onGoToAlbum = onGoToAlbum
        self.onGoToDetail = onGoToDetail
        self.onShare = onShare
        self.onToggleFavorite = onToggleFavorite
        self.onRemoveFromPlaylist = onRemoveFromPlaylist
        self.onDelete = onDelete
    }

    var body: some View {
        NavigationStack {
            Form {
                // 歌曲信息区
                Section {
                    HStack(spacing: 12) {
                        AlbumCover(uri: albumArtUri, musicPath: musicPath, size: 56)
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
                    if menuConfig.addToPlaylist {
                        Button {
                            HapticManager.shared.click()
                            onAddToPlaylist()
                        } label: {
                            Label("添加到播放列表", systemImage: "plus")
                        }
                    }

                    if menuConfig.playNext {
                        Button {
                            HapticManager.shared.click()
                            onPlayNext()
                        } label: {
                            Label("下一首播放", systemImage: "play.fill")
                        }
                    }

                    if menuConfig.favorite {
                        Button {
                            HapticManager.shared.click()
                            onToggleFavorite?()
                        } label: {
                            Label("收藏", systemImage: "heart")
                        }
                    }

                    if menuConfig.share {
                        Button {
                            HapticManager.shared.click()
                            onShare?()
                        } label: {
                            Label("分享", systemImage: "square.and.arrow.up")
                        }
                    }

                    if menuConfig.viewDetail {
                        Button {
                            HapticManager.shared.click()
                            onGoToDetail?()
                            dismiss()
                        } label: {
                            Label("歌曲详情", systemImage: "info.circle")
                        }
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

                if menuConfig.removeFromCurrentPlaylist, let onRemoveFromPlaylist {
                    Section {
                        Button(role: .destructive) {
                            HapticManager.shared.reject()
                            onRemoveFromPlaylist()
                            dismiss()
                        } label: {
                            Label("从当前播放列表移除", systemImage: "minus.circle")
                        }
                    }
                }

                if menuConfig.delete, let onDelete {
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
