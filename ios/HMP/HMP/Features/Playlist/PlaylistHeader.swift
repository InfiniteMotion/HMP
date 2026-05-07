import SwiftUI
import shared

private let HEADER_COVER_SIZE: CGFloat = 280
private let HEADER_CORNER_RADIUS: CGFloat = 25

/// 过滤非用户输入的描述（KMP data class 默认 toString 输出）
private extension String {
    var nonEmptyDescription: String? {
        let trimmed = trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return nil }
        // Only filter patterns that look like KMP data class toString: ClassName(property=value, ...)
        let kotlinClassPatterns = ["Playlist(", "MusicInfo(", "MusicInfo_(", "SongInfo(", "LabelName("]
        if kotlinClassPatterns.contains(where: { trimmed.starts(with: $0) }) {
            return nil
        }
        return trimmed
    }
}

/// 歌单详情头 - 对应 Android PlaylistScreen.kt 中的 PlaylistHeader
/// 封面图 + 统计行 + 描述文本
struct PlaylistHeader: View {
    @Environment(HMPTheme.self) private var theme

    let playlist: Playlist_?
    let musicList: [MusicInfo_]?

    @State private var resolvedImage: UIImage? = nil

    var body: some View {
        VStack(spacing: 16) {
            // Cover image (no gradient, centered)
            if let image = resolvedImage {
                coverImage(with: image)
            } else if let playlist, let coverUri = playlist.coverUri, !coverUri.isEmpty, let image = CoverCache.shared.get(path: coverUri) {
                coverImage(with: image)
            } else {
                coverPlaceholder
            }

            // Playlist name
            if let playlist {
                Text(playlist.name)
                    .font(TypographyTokens.titleLarge.bold())
                    .foregroundColor(theme.text)
            }

            // Stats row
            if let playlist {
                StatsRow(playlist: playlist)
            }

            // Description - only show if it looks like real text (not debug output)
            if let playlist, let desc = playlist.description.nonEmptyDescription {
                descriptionText(desc)
            }
        }
        .padding(.vertical, 20)
        .task(id: playlist?.coverUri ?? "") { resolveCover() }
    }

    private func resolveCover() {
        // 1. Try coverUri from playlist meta
        if let playlist, let coverUri = playlist.coverUri, !coverUri.isEmpty {
            if let image = CoverCache.shared.get(path: coverUri) {
                resolvedImage = image
                return
            }
        }
        // 2. Fallback: try to extract from first song's music file
        if let firstSong = musicList?.first, !firstSong.music.path.isEmpty {
            if let image = CoverCache.shared.getOrExtractSync(musicPath: firstSong.music.path) {
                resolvedImage = image
                return
            }
            let path = firstSong.music.path
            Task.detached(priority: .utility) {
                let coverPath = ArtworkExtractor.extractAsync(filePath: path)
                if let coverPath, let image = UIImage(contentsOfFile: coverPath) {
                    CoverCache.shared.put(path: coverPath, image: image)
                    await MainActor.run { resolvedImage = image }
                }
            }
        }
    }

    private func coverImage(with image: UIImage) -> some View {
        Image(uiImage: image)
            .resizable()
            .aspectRatio(contentMode: .fill)
            .frame(width: HEADER_COVER_SIZE, height: HEADER_COVER_SIZE)
            .clipShape(RoundedRectangle(cornerRadius: HEADER_CORNER_RADIUS))
            .shadow(color: .black.opacity(0.2), radius: 15)
    }

    private var coverPlaceholder: some View {
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
            .shadow(color: .black.opacity(0.15), radius: 15)
    }

    private func descriptionText(_ text: String) -> some View {
        Text(text)
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
                PlaylistStatItem(label: items[index].label, value: items[index].value)
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

private struct PlaylistStatItem: View {
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
