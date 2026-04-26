import SwiftUI
import shared

/// 迷你播放栏 - 显示当前播放歌曲，点击跳转播放器
struct MiniPlayerBar: View {
    @Environment(HMPTheme.self) private var theme

    private var controller: MusicPlayerController { MusicPlayerController.shared }

    var body: some View {
        if controller.isMiniPlayerVisible, let music = controller.currentPlayingMusic {
            Button {
                HapticManager.shared.click()
                // Navigate to PlayerScreen — handled by parent navigation
            } label: {
                HStack(spacing: 12) {
                    // 专辑封面占位
                    RoundedRectangle(cornerRadius: 8)
                        .fill(theme.primary.opacity(0.15))
                        .frame(width: 40, height: 40)
                        .overlay {
                            Image(systemName: "music.note")
                                .foregroundColor(theme.primary)
                        }

                    // 歌曲信息
                    VStack(alignment: .leading, spacing: 2) {
                        Text(music.music.title)
                            .font(TypographyTokens.titleSmall)
                            .foregroundColor(theme.text)
                            .lineLimit(1)

                        Text(music.music.artist)
                            .font(TypographyTokens.bodySmall)
                            .foregroundColor(theme.text.opacity(0.6))
                            .lineLimit(1)
                    }

                    Spacer()

                    // 播放/暂停按钮
                    Button {
                        HapticManager.shared.click()
                        if controller.isPlaying {
                            controller.pauseMusic()
                        } else {
                            controller.playOrResume()
                        }
                    } label: {
                        Image(systemName: controller.isPlaying ? "pause.fill" : "play.fill")
                            .font(.system(size: 18))
                            .foregroundColor(theme.primary)
                            .frame(width: 36, height: 36)
                            .background(
                                Circle()
                                    .fill(theme.primary.opacity(0.12))
                            )
                    }
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
}
