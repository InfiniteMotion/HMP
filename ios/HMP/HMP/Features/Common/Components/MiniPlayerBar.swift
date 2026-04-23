import SwiftUI

/// 迷你播放栏 - 对应 Android MiniPlayerBar.kt
/// 全局底部悬浮显示当前播放歌曲，点击跳转播放器
struct MiniPlayerBar: View {
    @Environment(HMPTheme.self) private var theme

    /// TODO: 连接 PlaybackViewModel (P6 完成后)
    // @ObservedObject var playbackVM: PlaybackViewModel

    let isPlaying: Bool = false          // 占位
    let currentTitle: String = "未播放"   // 占位
    let currentArtist: String = ""        // 占位
    let coverUri: String? = nil           // 占位

    var body: some View {
        Button {
            HapticManager.shared.click()
            // Navigate to Player screen
        } label: {
            HStack(spacing: 12) {
                // 专辑封面占位
                Rectangle()
                    .fill(theme.primaryContainer)
                    .frame(width: 40, height: 40)
                    .cornerRadius(8)

                // 歌曲信息
                VStack(alignment: .leading, spacing: 2) {
                    Text(currentTitle)
                        .font(TypographyTokens.titleSmall)
                        .foregroundColor(theme.text)
                        .lineLimit(1)

                    if !currentArtist.isEmpty {
                        Text(currentArtist)
                            .font(TypographyTokens.bodySmall)
                            .foregroundColor(theme.secondaryText)
                            .lineLimit(1)
                    }
                }

                Spacer()

                // 播放/暂停按钮
                Image(systemName: isPlaying ? "pause.fill" : "play.fill")
                    .font(.system(size: 18))
                    .foregroundColor(theme.primary)
                    .frame(width: 36, height: 36)
                    .background(
                        Circle()
                            .fill(theme.primary.opacity(0.12))
                    )
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(
                .ultraThinMaterial,
                in: RoundedRectangle(cornerRadius: 16)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(theme.divider, lineWidth: 0.5)
            )
        }
        .buttonStyle(PlainButtonStyle())
        .padding(.horizontal, 16)
        .padding(.bottom, 8)
    }
}
