import SwiftUI
import shared

/// 完整的音乐信息列表行 - 对应 Android MusicList 中的行
/// 包含：封面 + 序号 + 标题 + 艺术家 + 时长 + 当前播放指示
struct MusicListRow: View {
    @Environment(HMPTheme.self) private var theme

    let music: Music_
    let index: Int?
    let showIndex: Bool
    let isPlaying: Bool
    let onTap: (() -> Void)?

    init(
        music: Music_,
        index: Int? = nil,
        showIndex: Bool = true,
        isPlaying: Bool = false,
        onTap: (() -> Void)? = nil
    ) {
        self.music = music
        self.index = index
        self.showIndex = showIndex
        self.isPlaying = isPlaying
        self.onTap = onTap
    }

    var body: some View {
        Button {
            HapticManager.shared.click()
            onTap?()
        } label: {
            HStack(spacing: 12) {
                // 序号或当前播放指示
                if let idx = index, showIndex {
                    if isPlaying {
                        playingIndicator
                    } else {
                        Text("\(idx + 1)")
                            .font(TypographyTokens.bodyMedium)
                            .foregroundColor(theme.text.opacity(0.4))
                            .frame(width: 24)
                    }
                }

                // 封面
                AlbumCover(
                    uri: music.albumArtUri,
                    musicPath: music.path,
                    size: 48,
                    cornerRadius: 8
                )

                // 标题 + 艺术家
                VStack(alignment: .leading, spacing: 2) {
                    Text(music.title)
                        .font(TypographyTokens.bodyMedium)
                        .foregroundColor(isPlaying ? theme.primary : theme.text)
                        .lineLimit(1)
                    Text(music.artist)
                        .font(TypographyTokens.bodySmall)
                        .foregroundColor(theme.text.opacity(0.6))
                        .lineLimit(1)
                }

                Spacer()

                // 时长
                Text(formatDuration(music.duration))
                    .font(TypographyTokens.bodySmall)
                    .foregroundColor(theme.text.opacity(0.5))
            }
            .contentShape(Rectangle())
            .padding(.vertical, 8)
            .padding(.horizontal, 16)
        }
        .buttonStyle(.plain)
    }

    private var playingIndicator: some View {
        HStack(spacing: 2) {
            Image(systemName: "play.fill")
                .font(.system(size: 10))
                .foregroundColor(theme.primary)
        }
        .frame(width: 24)
    }

    private func formatDuration(_ ms: Int64) -> String {
        let seconds = ms / 1000
        let min = seconds / 60
        let sec = seconds % 60
        return String(format: "%d:%02d", min, sec)
    }
}
