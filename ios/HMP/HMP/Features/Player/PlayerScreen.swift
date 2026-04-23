import SwiftUI

/// 播放器页面 - 对应 Android PlayerScreen.kt
/// 对应 Android 的下滑关闭，iOS 使用 .presentationDetents + .interactiveDismiss
struct PlayerScreen: View {
    @Environment(\.dismiss) private var dismiss

    // TODO: 连接 PlaybackViewModel + PlayControlViewModel
    // @ObservedObject var playbackVM: PlaybackViewModel
    // @ObservedObject var controlVM: PlayControlViewModel

    @State private var isPlaying: Bool = false
    @State private var currentPosition: Int64 = 0
    @State private var duration: Int64 = 0
    @State private var currentMusic: MusicItem? = nil
    @State private var showLyrics = false
    @State private var showAudioEffects = false

    var body: some View {
        ZStack {
            // 动态背景
            if let uri = currentMusic?.albumArtUri {
                FluidBackgroundView(albumArtUri: uri)
            } else {
                ColorTokens.darkBackground.ignoresSafeArea()
            }

            // 遮罩
            Color.black.opacity(0.3).ignoresSafeArea()

            VStack(spacing: 0) {
                // 顶部栏
                HStack {
                    Button {
                        dismiss()
                    } label: {
                        Image(systemName: "chevron.down")
                            .font(.system(size: 20))
                            .foregroundColor(.white)
                    }

                    Spacer()

                    Button {
                        showLyrics = true
                    } label: {
                        Image(systemName: "quote.bubble")
                            .font(.system(size: 20))
                            .foregroundColor(.white)
                    }

                    Button {
                        showAudioEffects = true
                    } label: {
                        Image(systemName: "slider.horizontal.3")
                            .font(.system(size: 20))
                            .foregroundColor(.white)
                    }
                }
                .padding(.horizontal, 24)
                .padding(.top, 16)
                .padding(.bottom, 8)

                // 专辑封面
                AlbumCover(
                    uri: currentMusic?.albumArtUri,
                    size: UIScreen.main.bounds.width * 0.65,
                    cornerRadius: 20
                )
                .padding(.top, 32)

                Spacer(minLength: 32)

                // 歌曲信息
                if let music = currentMusic {
                    VStack(spacing: 8) {
                        Text(music.title)
                            .font(TypographyTokens.headlineLarge)
                            .foregroundColor(.white)
                            .lineLimit(2)
                            .multilineTextAlignment(.center)

                        Text(music.artist)
                            .font(TypographyTokens.bodyLarge)
                            .foregroundColor(.white.opacity(0.7))
                            .lineLimit(1)
                    }
                    .padding(.horizontal, 40)

                    // 进度条
                    PlayerProgressBar(
                        currentPosition: currentPosition,
                        duration: duration
                    )
                    .padding(.top, 24)
                    .padding(.horizontal, 40)

                    // 控制按钮
                    PlayerControls(
                        isPlaying: $isPlaying
                    )
                    .padding(.top, 16)
                }
            }
        }
        .ignoresSafeArea()
        .sheet(isPresented: $showLyrics) {
            LyricsScreen()
                .presentationDetents([.medium, .large])
        }
        .sheet(isPresented: $showAudioEffects) {
            AudioEffectsScreen()
                .presentationDetents([.large])
        }
    }
}

// MARK: - 进度条
struct PlayerProgressBar: View {
    @Environment(HMPTheme.self) private var theme
    let currentPosition: Int64
    let duration: Int64

    private var progress: Double {
        guard duration > 0 else { return 0 }
        return Double(currentPosition) / Double(duration)
    }

    var body: some View {
        VStack(spacing: 4) {
            ProgressView(value: progress)
                .tint(theme.primary)
                .scaleEffect(y: 1.8)

            HStack {
                Text(formatTime(currentPosition))
                    .font(TypographyTokens.labelSmall)
                    .foregroundColor(.white.opacity(0.7))
                Spacer()
                Text(formatTime(duration))
                    .font(TypographyTokens.labelSmall)
                    .foregroundColor(.white.opacity(0.7))
            }
        }
    }

    private func formatTime(_ ms: Int64) -> String {
        let seconds = ms / 1000
        let min = seconds / 60
        let sec = seconds % 60
        return String(format: "%d:%02d", min, sec)
    }
}

// MARK: - 控制按钮
struct PlayerControls: View {
    @Binding var isPlaying: Bool

    var body: some View {
        HStack(spacing: 32) {
            Button {
                HapticManager.shared.lightClick()
                // skipPrevious()
            } label: {
                Image(systemName: "backward.fill")
                    .font(.system(size: 24))
                    .foregroundColor(.white)
            }

            Button {
                HapticManager.shared.click()
                isPlaying.toggle()
            } label: {
                Image(systemName: isPlaying ? "pause.fill" : "play.fill")
                    .font(.system(size: 36))
                    .foregroundColor(.white)
                    .frame(width: 64, height: 64)
                    .background(Circle().fill(ColorTokens.hdRed))
            }

            Button {
                HapticManager.shared.lightClick()
                // skipNext()
            } label: {
                Image(systemName: "forward.fill")
                    .font(.system(size: 24))
                    .foregroundColor(.white)
            }
        }
    }
}

// MARK: - FluidBackgroundView (iOS 简化版 DynamicBackground)
struct FluidBackgroundView: View {
    let albumArtUri: String

    var body: some View {
        // TODO: 使用专辑封面生成流体光效背景
        // iOS 使用 CoreImage CIFilter 实现
        Color.black
            .ignoresSafeArea()
    }
}
