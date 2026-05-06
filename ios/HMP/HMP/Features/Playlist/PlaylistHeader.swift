import SwiftUI
import shared

private let HEADER_COVER_SIZE: CGFloat = 280
private let HEADER_CORNER_RADIUS: CGFloat = 25

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
                    coverImage(with: image)
                } else {
                    coverPlaceholder
                }
            } else {
                coverPlaceholder
            }

            // Stats row
            if let playlist {
                StatsRow(playlist: playlist)
            }

            // Description
            if let playlist, !playlist.description.isEmpty {
                descriptionText(for: playlist)
            }
        }
        .padding(.vertical, 20)
    }

    private func coverImage(with image: UIImage) -> some View {
        ZStack(alignment: .bottomLeading) {
            Image(uiImage: image)
                .resizable()
                .aspectRatio(contentMode: .fill)
                .frame(width: HEADER_COVER_SIZE, height: HEADER_COVER_SIZE)
                .clipShape(RoundedRectangle(cornerRadius: HEADER_CORNER_RADIUS))
            
            // Gradient overlay
            LinearGradient(
                stops: [
                    .init(color: .clear, location: 0),
                    .init(color: .clear, location: 0.5),
                    .init(color: .black.opacity(0.8), location: 1)
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .frame(width: HEADER_COVER_SIZE, height: HEADER_COVER_SIZE)
            .clipShape(RoundedRectangle(cornerRadius: HEADER_CORNER_RADIUS))
            
            // Playlist name over gradient
            if let playlist {
                Text(playlist.name)
                    .font(TypographyTokens.titleLarge.bold())
                    .foregroundColor(.white)
                    .padding(.bottom, 16)
                    .padding(.leading, 16)
            }
        }
        .shadow(color: .black.opacity(0.2), radius: 15)
    }

    private var coverPlaceholder: some View {
        ZStack(alignment: .bottomLeading) {
            RoundedRectangle(cornerRadius: HEADER_CORNER_RADIUS)
                .fill(LinearGradient(
                    colors: [theme.primary.opacity(0.2), theme.primary.opacity(0.1)],
                    startPoint: .top,
                    endPoint: .bottom
                ))
                .frame(width: HEADER_COVER_SIZE, height: HEADER_COVER_SIZE)
                .overlay {
                    Image(systemName: "music.note.list")
                        .font(.system(size: 72))
                        .foregroundColor(theme.primary.opacity(0.6))
                }
            
            // Gradient overlay for placeholder
            LinearGradient(
                stops: [
                    .init(color: .clear, location: 0),
                    .init(color: .clear, location: 0.5),
                    .init(color: .black.opacity(0.6), location: 1)
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .frame(width: HEADER_COVER_SIZE, height: HEADER_COVER_SIZE)
            .clipShape(RoundedRectangle(cornerRadius: HEADER_CORNER_RADIUS))
            
            if let playlist {
                Text(playlist.name)
                    .font(TypographyTokens.titleLarge.bold())
                    .foregroundColor(.white)
                    .padding(.bottom, 16)
                    .padding(.leading, 16)
            }
        }
        .shadow(color: .black.opacity(0.15), radius: 15)
    }

    private func descriptionText(for playlist: Playlist_) -> some View {
        Text(playlist.description)
            .font(TypographyTokens.bodyMedium)
            .foregroundColor(theme.text.opacity(0.8))
            .lineLimit(2)
            .multilineTextAlignment(.center)
            .padding(.horizontal, 8)
    }
}

private struct StatsRow: View {
    @Environment(HMPTheme.self) private var theme
    let playlist: Playlist_

    var body: some View {
        HStack(spacing: 0) {
            let items = statItems()
            ForEach(items.indices, id: \.self) { index in
                StatItem(label: items[index].label, value: items[index].value)
                if index < items.count - 1 {
                    Spacer()
                }
            }
        }
        .padding(.horizontal, 16)
    }

    private func statItems() -> [(label: String, value: String)] {
        var items: [(label: String, value: String)] = []
        if playlist.songCount > 0 {
            items.append((label: "歌曲", value: "\(playlist.songCount)"))
        }
        if playlist.totalDurationMs > 0 {
            let minutes = playlist.totalDurationMs / 60000
            if minutes > 0 {
                items.append((label: "时长", value: "\(minutes)分钟"))
            }
        }
        if playlist.playCount > 0 {
            items.append((label: "播放", value: "\(playlist.playCount)"))
        }
        return items
    }
}

private struct StatItem: View {
    @Environment(HMPTheme.self) private var theme
    let label: String
    let value: String

    var body: some View {
        VStack(spacing: 4) {
            Text(value)
                .font(TypographyTokens.titleMedium.bold())
                .foregroundColor(theme.text)
            Text(label)
                .font(TypographyTokens.labelSmall)
                .foregroundColor(theme.text.opacity(0.7))
        }
        .frame(maxWidth: .infinity)
        .multilineTextAlignment(.center)
    }
}
