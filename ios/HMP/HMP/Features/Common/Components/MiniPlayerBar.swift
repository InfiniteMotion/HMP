import SwiftUI
import shared

/// 迷你播放栏 - 与 Android MiniPlayerBar 样式一致
/// 整个区域可点击打开播放器，包含旋转封面和毛玻璃背景
struct MiniPlayerBar: View {
    @Environment(HMPTheme.self) private var theme
    @State private var showPlayer = false
    @State private var coverRotation: Double = 0

    private var controller: MusicPlayerController { MusicPlayerController.shared }

    private var musicTitle: String {
        controller.currentPlayingMusic?.music.title ?? "Music Title"
    }

    private var artistName: String {
        controller.currentPlayingMusic?.music.artist ?? "Artist Name"
    }

    private var albumArtUri: String? {
        controller.currentPlayingMusic?.music.albumArtUri
    }

    private var musicPath: String? {
        controller.currentPlayingMusic?.music.path
    }

    var body: some View {
        HStack(spacing: 16) {
            // 旋转的专辑封面
            AlbumCover(uri: albumArtUri, musicPath: musicPath, size: 56, cornerRadius: 28)
                .rotationEffect(.degrees(coverRotation))

            // 音乐信息
            VStack(alignment: .leading, spacing: 4) {
                Text(musicTitle)
                    .font(TypographyTokens.titleSmall)
                    .fontWeight(.bold)
                    .foregroundColor(theme.text)
                    .lineLimit(1)

                Text(artistName)
                    .font(TypographyTokens.bodySmall)
                    .foregroundColor(theme.text.opacity(0.6))
                    .lineLimit(1)
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            // 控制按钮组
            HStack(spacing: 0) {
                // 上一首按钮
                Button {
                    HapticManager.shared.lightClick()
                    controller.playPrevious()
                } label: {
                    Image(systemName: "backward.end.fill")
                        .font(.system(size: 22))
                        .foregroundColor(theme.text)
                        .frame(width: 40, height: 40)
                }
                .buttonStyle(PlainButtonStyle())

                // 播放/暂停按钮
                Button {
                    HapticManager.shared.lightClick()
                    if controller.isPlaying {
                        controller.pauseMusic()
                    } else {
                        controller.playOrResume()
                    }
                } label: {
                    Image(systemName: controller.isPlaying ? "pause.fill" : "play.fill")
                        .font(.system(size: 26))
                        .foregroundColor(theme.text)
                        .frame(width: 44, height: 44)
                }
                .buttonStyle(PlainButtonStyle())

                // 下一首按钮
                Button {
                    HapticManager.shared.lightClick()
                    controller.playNext()
                } label: {
                    Image(systemName: "forward.end.fill")
                        .font(.system(size: 22))
                        .foregroundColor(theme.text)
                        .frame(width: 40, height: 40)
                }
                .buttonStyle(PlainButtonStyle())
            }
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 8)
        .background(
            .ultraThinMaterial,
            in: RoundedRectangle(cornerRadius: 36)
        )
        .overlay(
            RoundedRectangle(cornerRadius: 36)
                .stroke(theme.divider, lineWidth: 0.5)
        )
        .padding(.horizontal, 24)
        .padding(.top, 8)
        .padding(.bottom, 16)
        .onTapGesture {
            HapticManager.shared.lightClick()
            showPlayer = true
        }
        .fullScreenCover(isPresented: $showPlayer) {
            PlayerScreen()
        }
        .onAppear {
            startRotationAnimation()
        }
        .onChange(of: controller.isPlaying) { _ in
            startRotationAnimation()
        }
    }

    private func startRotationAnimation() {
        guard controller.isPlaying else { return }

        // 8秒一圈的旋转动画
        withAnimation(.linear(duration: 8).repeatForever(autoreverses: false)) {
            coverRotation = 360
        }
    }
}
