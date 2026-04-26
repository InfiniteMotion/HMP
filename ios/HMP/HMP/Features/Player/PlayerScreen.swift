import SwiftUI
import shared

/// 播放器页面 - 对应 Android PlayerScreen.kt
struct PlayerScreen: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(HMPTheme.self) private var theme

    private var controller: MusicPlayerController { MusicPlayerController.shared }

    @State private var showLyrics = false
    @State private var showAudioEffects = false
    @State private var showQueue = false
    @State private var isSeeking = false
    @State private var seekingPosition: Double = 0

    var body: some View {
        ZStack {
            // 背景
            ColorTokens.darkBackground.ignoresSafeArea()

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
                        showQueue = true
                    } label: {
                        Image(systemName: "list.bullet")
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
                RoundedRectangle(cornerRadius: 20)
                    .fill(theme.primary.opacity(0.15))
                    .frame(width: UIScreen.main.bounds.width * 0.65,
                           height: UIScreen.main.bounds.width * 0.65)
                    .overlay {
                        Image(systemName: "music.note")
                            .font(.system(size: 60))
                            .foregroundColor(theme.primary)
                    }
                    .padding(.top, 32)

                Spacer(minLength: 32)

                // 歌曲信息
                if let music = controller.currentPlayingMusic {
                    VStack(spacing: 8) {
                        Text(music.music.title)
                            .font(TypographyTokens.headlineLarge)
                            .foregroundColor(.white)
                            .lineLimit(2)
                            .multilineTextAlignment(.center)

                        Text(music.music.artist)
                            .font(TypographyTokens.bodyLarge)
                            .foregroundColor(.white.opacity(0.7))
                            .lineLimit(1)
                    }
                    .padding(.horizontal, 40)

                    // 进度条
                    PlayerSeekBar(
                        currentPosition: controller.currentPosition,
                        duration: controller.duration,
                        isSeeking: $isSeeking,
                        seekingPosition: $seekingPosition,
                        onSeek: { pos in controller.seekTo(position: pos) }
                    )
                    .padding(.top, 24)
                    .padding(.horizontal, 40)

                    // 控制按钮
                    PlayerControls()
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
        .sheet(isPresented: $showQueue) {
            PlayQueueSheet()
                .presentationDetents([.medium, .large])
        }
    }
}

// MARK: - 可拖动进度条
struct PlayerSeekBar: View {
    let currentPosition: Int64
    let duration: Int64
    @Binding var isSeeking: Bool
    @Binding var seekingPosition: Double
    let onSeek: (Int64) -> Void

    private var displayPosition: Double {
        guard duration > 0 else { return 0 }
        return isSeeking ? seekingPosition : Double(currentPosition) / Double(duration)
    }

    var body: some View {
        VStack(spacing: 4) {
            Slider(
                value: Binding(
                    get: { displayPosition },
                    set: { newValue in
                        isSeeking = true
                        seekingPosition = newValue
                    }
                ),
                onEditingChanged: { editing in
                    if !editing {
                        isSeeking = false
                        let targetMs = Int64(seekingPosition * Double(duration))
                        onSeek(targetMs)
                    }
                }
            )
            .tint(.white)

            HStack {
                Text(formatTime(isSeeking ? Int64(seekingPosition * Double(duration)) : currentPosition))
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

// MARK: - 播放控制按钮
struct PlayerControls: View {
    private var controller: MusicPlayerController { MusicPlayerController.shared }

    var body: some View {
        VStack(spacing: 16) {
            // 主控制行
            HStack(spacing: 40) {
                // 上一首
                Button {
                    HapticManager.shared.lightClick()
                    controller.playPrevious()
                } label: {
                    Image(systemName: "backward.fill")
                        .font(.system(size: 24))
                        .foregroundColor(.white)
                }

                // 播放/暂停
                Button {
                    HapticManager.shared.click()
                    if controller.isPlaying {
                        controller.pauseMusic()
                    } else {
                        controller.playOrResume()
                    }
                } label: {
                    Image(systemName: controller.isPlaying ? "pause.fill" : "play.fill")
                        .font(.system(size: 36))
                        .foregroundColor(.white)
                        .frame(width: 64, height: 64)
                        .background(Circle().fill(ColorTokens.hdRed))
                }

                // 下一首
                Button {
                    HapticManager.shared.lightClick()
                    controller.playNext()
                } label: {
                    Image(systemName: "forward.fill")
                        .font(.system(size: 24))
                        .foregroundColor(.white)
                }
            }

            // 次要控制行
            HStack(spacing: 24) {
                // 播放模式
                Button {
                    controller.togglePlaybackModeByOrder()
                } label: {
                    Image(systemName: playbackModeIcon)
                        .font(.system(size: 18))
                        .foregroundColor(.white.opacity(0.7))
                }

                Spacer()

                // 收藏
                Button {
                    controller.updateLikedStatus(!controller.likeStatus)
                } label: {
                    Image(systemName: controller.likeStatus ? "heart.fill" : "heart")
                        .font(.system(size: 18))
                        .foregroundColor(controller.likeStatus ? ColorTokens.hdRed : .white.opacity(0.7))
                }
            }
            .padding(.horizontal, 40)
        }
    }

    private var playbackModeIcon: String {
        let mode = controller.playbackMode
        if mode == PlaybackMode.repeatOne {
            return "repeat.1"
        } else if mode == PlaybackMode.shuffle {
            return "shuffle"
        } else {
            return "repeat"
        }
    }
}

// MARK: - FluidBackgroundView (placeholder)
struct FluidBackgroundView: View {
    let albumArtUri: String

    var body: some View {
        Color.black
            .ignoresSafeArea()
    }
}

// MARK: - 播放队列
struct PlayQueueSheet: View {
    @Environment(HMPTheme.self) private var theme
    private var controller: MusicPlayerController { MusicPlayerController.shared }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                if controller.currentPlaylist.isEmpty {
                    Spacer()
                    Text("队列为空")
                        .font(TypographyTokens.bodyLarge)
                        .foregroundColor(theme.text.opacity(0.4))
                    Spacer()
                } else {
                    List {
                        ForEach(Array(controller.currentPlaylist.enumerated()), id: \.element.music.id) { index, info in
                            HStack(spacing: 12) {
                                // 序号或播放指示
                                if index == controller.currentIndex {
                                    Image(systemName: "speaker.wave.2.fill")
                                        .font(.system(size: 14))
                                        .foregroundColor(theme.primary)
                                        .frame(width: 24)
                                } else {
                                    Text("\(index + 1)")
                                        .font(TypographyTokens.bodySmall)
                                        .foregroundColor(theme.text.opacity(0.4))
                                        .frame(width: 24)
                                }

                                VStack(alignment: .leading, spacing: 2) {
                                    Text(info.music.title)
                                        .font(TypographyTokens.bodyMedium)
                                        .foregroundColor(index == controller.currentIndex ? theme.primary : theme.text)
                                        .lineLimit(1)

                                    Text(info.music.artist)
                                        .font(TypographyTokens.bodySmall)
                                        .foregroundColor(theme.text.opacity(0.6))
                                        .lineLimit(1)
                                }

                                Spacer()

                                Text(formatDuration(info.music.duration))
                                    .font(TypographyTokens.bodySmall)
                                    .foregroundColor(theme.text.opacity(0.4))
                            }
                            .padding(.vertical, 4)
                            .contentShape(Rectangle())
                            .onTapGesture {
                                controller.playAt(index)
                            }
                        }
                        .onDelete { offsets in
                            for index in offsets.sorted().reversed() {
                                if index < controller.currentPlaylist.count {
                                    let info = controller.currentPlaylist[index]
                                    controller.currentPlaylist.remove(at: index)
                                    if index < controller.currentIndex {
                                        controller.currentIndex -= 1
                                    } else if index == controller.currentIndex {
                                        // Current track removed — play next or stop
                                        if controller.currentPlaylist.isEmpty {
                                            controller.clearPlaylist()
                                        } else {
                                            let nextIndex = min(controller.currentIndex, controller.currentPlaylist.count - 1)
                                            controller.playAt(nextIndex)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("播放队列 (\(controller.currentPlaylist.count))")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("清除") {
                        controller.clearPlaylist()
                    }
                    .disabled(controller.currentPlaylist.isEmpty)
                }
            }
        }
    }

    private func formatDuration(_ ms: Int64) -> String {
        let seconds = ms / 1000
        let min = seconds / 60
        let sec = seconds % 60
        return String(format: "%d:%02d", min, sec)
    }
}
