import SwiftUI
import shared

/// 歌单详情头 - 对应 Android PlaylistScreen.kt 中的 PlaylistHeader
/// 封面图 + 统计行 + 描述文本
struct PlaylistHeader: View {
    @Environment(HMPTheme.self) private var theme

    let playlist: Playlist_?

    var body: some View {
        VStack(spacing: 16) {
            // Cover image
            if let playlist, let coverUri = playlist.coverUri, !coverUri.isEmpty {
                if let image = CoverCache.shared.get(path: coverUri) {
                    Image(uiImage: image)
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(width: 200, height: 200)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                        .shadow(color: .black.opacity(0.2), radius: 8)
                } else {
                    coverPlaceholder
                }
            } else {
                coverPlaceholder
            }

            // Stats row
            if let playlist {
                let parts = statsParts(for: playlist)
                if !parts.isEmpty {
                    Text(parts.joined(separator: " · "))
                        .font(TypographyTokens.bodyMedium)
                        .foregroundColor(theme.text.opacity(0.6))
                }
            }

            // Description
            if let playlist, !playlist.description.isEmpty {
                Text(playlist.description)
                    .font(TypographyTokens.bodySmall)
                    .foregroundColor(theme.text.opacity(0.6))
                    .lineLimit(2)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
            }
        }
        .padding(.vertical, 16)
    }

    private var coverPlaceholder: some View {
        RoundedRectangle(cornerRadius: 16)
            .fill(theme.primary.opacity(0.12))
            .frame(width: 200, height: 200)
            .overlay {
                Image(systemName: "music.note.list")
                    .font(.system(size: 60))
                    .foregroundColor(theme.primary)
            }
    }

    private func statsParts(for playlist: Playlist_) -> [String] {
        var parts: [String] = []
        if playlist.songCount > 0 {
            parts.append("\(playlist.songCount) 首歌曲")
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
        return parts
    }
}
