import SwiftUI
import shared

/// 播放器页面 - 对应 Android PlayerScreen.kt
/// 核心对齐版：封面居中 + 歌曲信息左对齐 + 5个次控制按钮 + 安全区适配
struct PlayerScreen: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(HMPTheme.self) private var theme

    private var controller: MusicPlayerController { MusicPlayerController.shared }

    @State private var showLyrics = false
    @State private var showAudioEffects = false
    @State private var showQueue = false
    @State private var showTimer = false
    @State private var isSeeking = false
    @State private var seekingPosition: Double = 0
    @State private var dragOffset: CGFloat = 0

    private let dismissThreshold: CGFloat = 220

    // MARK: - Computed Properties for Default Values
    private var musicTitle: String {
        controller.currentPlayingMusic?.music.title ?? "Music Title"
    }

    private var musicArtist: String {
        controller.currentPlayingMusic?.music.artist ?? "Artist"
    }

    private var musicAlbum: String {
        controller.currentPlayingMusic?.music.album ?? "Album"
    }

    private var albumArtUri: String? {
        controller.currentPlayingMusic?.music.albumArtUri
    }

    private var musicPath: String? {
        controller.currentPlayingMusic?.music.path
    }

    var body: some View {
        ZStack {
            // 背景
            ColorTokens.darkBackground.ignoresSafeArea()

            // 遮罩
            Color.black.opacity(0.3).ignoresSafeArea()

            // 安全区适配
            SafeAreaView {
                VStack(spacing: 0) {
                    // 顶部栏 - 安全区内
                    playerHeader

                    VStack(spacing: 0) {
                        Spacer()

                        // 歌曲信息（左对齐）
                        HStack {
                            musicInfoSection
                            Spacer()
                        }
                        .padding(.horizontal, 32)

                        Spacer()

                        // 专辑封面（居中）
                        AlbumCover(
                            uri: albumArtUri,
                            musicPath: musicPath,
                            size: UIScreen.main.bounds.width * 0.65,
                            cornerRadius: 20
                        )

                        Spacer()

                        // 进度条
                        PlayerSeekBar(
                            currentPosition: controller.currentPosition,
                            duration: controller.duration,
                            isSeeking: $isSeeking,
                            seekingPosition: $seekingPosition,
                            onSeek: { pos in controller.seekTo(position: pos) }
                        )
                        .padding(.horizontal, 32)

                        Spacer()

                        // 控制按钮
                        MainControlRow()

                        Spacer()

                        SecondaryControlRow(
                            showTimerAction: { showTimer = true },
                            showQueue: $showQueue
                        )

                        Spacer()
                    }
                }
                .offset(y: dragOffset)
                .opacity(max(0, 1 - abs(dragOffset) / (2 * dismissThreshold)))
            }
        }
        .ignoresSafeArea()
        .simultaneousGesture(
            DragGesture()
                .onChanged { value in
                    if value.translation.height > 0 {
                        dragOffset = value.translation.height
                    }
                }
                .onEnded { value in
                    if value.translation.height > dismissThreshold {
                        dismiss()
                    } else {
                        withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
                            dragOffset = 0
                        }
                    }
                }
        )
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
        .sheet(isPresented: $showTimer) {
            TimerSheet()
                .presentationDetents([.height(300)])
        }
    }

    // MARK: - 顶部栏
    private var playerHeader: some View {
        HStack(spacing: 16) {
            Spacer()
            Button {
                HapticManager.shared.click()
                dismiss()
            } label: {
                Image(systemName: "chevron.down")
                    .font(.system(size: 24, weight: .semibold))
                    .foregroundColor(.white)
                    .frame(width: 32, height: 32)
            }
            Spacer()
        }
        .padding(.top, 64)
        .padding(.bottom, 16)
    }

    // MARK: - 歌曲信息区
    private var musicInfoSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(musicTitle)
                .font(TypographyTokens.displayMedium)
                .foregroundColor(.white)
                .lineLimit(1)

            Text(musicArtist)
                .font(TypographyTokens.titleMedium)
                .foregroundColor(.white.opacity(0.7))
                .lineLimit(1)

            Text(musicAlbum)
                .font(TypographyTokens.titleMedium)
                .foregroundColor(.white.opacity(0.7))
                .lineLimit(1)
        }
    }
}

// MARK: - 安全区适配容器
struct SafeAreaView<Content: View>: View {
    let content: () -> Content

    init(@ViewBuilder content: @escaping () -> Content) {
        self.content = content
    }

    var body: some View {
        GeometryReader { geo in
            content()
                .padding(.top, geo.safeAreaInsets.top)
                .padding(.bottom, geo.safeAreaInsets.bottom)
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
        return String(format: "%02d:%02d", min, sec)
    }
}

// MARK: - 主控制行（播放/暂停）
struct MainControlRow: View {
    private var controller: MusicPlayerController { MusicPlayerController.shared }
    
    var body: some View {
        HStack(spacing: 48) {
            // 上一首
            Button {
                HapticManager.shared.lightClick()
                controller.playPrevious()
            } label: {
                Image(systemName: "backward.end.fill")
                    .font(.system(size: 28))
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
                    .font(.system(size: 40))
                    .foregroundColor(.white)
                    .frame(width: 72, height: 72)
                    .background(Circle().fill(ColorTokens.hdRed))
            }

            // 下一首
            Button {
                HapticManager.shared.lightClick()
                controller.playNext()
            } label: {
                Image(systemName: "forward.end.fill")
                    .font(.system(size: 28))
                    .foregroundColor(.white)
            }
        }
    }
}

// MARK: - 次要控制行（播放模式/收藏等）
struct SecondaryControlRow: View {
    private var controller: MusicPlayerController { MusicPlayerController.shared }
    let showTimerAction: () -> Void
    let showQueue: Binding<Bool>
    
    var body: some View {
        HStack(spacing: 0) {
            // 播放模式
            Button {
                controller.togglePlaybackModeByOrder()
            } label: {
                Image(systemName: playbackModeIcon)
                    .font(.system(size: 20))
                    .foregroundColor(.white.opacity(0.7))
            }

            Spacer()

            // 收藏
            Button {
                controller.updateLikedStatus(!controller.likeStatus)
            } label: {
                Image(systemName: controller.likeStatus ? "heart.fill" : "heart")
                    .font(.system(size: 20))
                    .foregroundColor(controller.likeStatus ? ColorTokens.hdRed : .white.opacity(0.7))
            }

            Spacer()

            // 识别歌曲/心随律动
            Button {
                HapticManager.shared.click()
            } label: {
                Image(systemName: "waveform")
                    .font(.system(size: 20))
                    .foregroundColor(.white.opacity(0.7))
            }

            Spacer()

            // 定时器
            if let remaining = controller.timerRemaining {
                Button {
                    showTimerAction()
                } label: {
                    Text(formatRemaining(remaining))
                        .font(TypographyTokens.bodySmall)
                        .foregroundColor(.white.opacity(0.7))
                }
            } else {
                Button {
                    showTimerAction()
                } label: {
                    Image(systemName: "timer")
                        .font(.system(size: 20))
                        .foregroundColor(.white.opacity(0.7))
                }
            }

            Spacer()

            // 播放列表
            Button {
                HapticManager.shared.click()
                showQueue.wrappedValue = true
            } label: {
                Image(systemName: "music.note.list")
                    .font(.system(size: 20))
                    .foregroundColor(.white.opacity(0.7))
            }
        }
        .padding(.horizontal, 24)
    }
    
    private var playbackModeIcon: String {
        switch controller.playbackMode {
        case .repeatOne: return "repeat.1"
        case .shuffle: return "shuffle"
        default: return "repeat"
        }
    }
    
    private func formatRemaining(_ ms: Int64) -> String {
        let seconds = ms / 1000
        let min = seconds / 60
        let sec = seconds % 60
        return String(format: "%d:%02d", min, sec)
    }
}

// MARK: - 定时器弹窗
struct TimerSheet: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(HMPTheme.self) private var theme
    private var controller: MusicPlayerController { MusicPlayerController.shared }

    let timerOptions: [(label: String, minutes: Int)] = [
        ("1 分钟", 1),
        ("5 分钟", 5),
        ("10 分钟", 10),
        ("15 分钟", 15),
        ("30 分钟", 30),
        ("60 分钟", 60),
        ("播完当前", 0)
    ]

    var body: some View {
        NavigationStack {
            List {
                ForEach(timerOptions, id: \.minutes) { option in
                    Button {
                        HapticManager.shared.click()
                        controller.startTimer(minutes: option.minutes)
                        dismiss()
                    } label: {
                        HStack {
                            Text(option.label)
                                .foregroundColor(theme.text)
                            Spacer()
                            if controller.timerRemaining != nil && option.minutes > 0 {
                                Image(systemName: "checkmark")
                                    .foregroundColor(theme.primary)
                            }
                        }
                    }
                }

                if controller.timerRemaining != nil {
                    Button {
                        HapticManager.shared.click()
                        controller.cancelTimer()
                        dismiss()
                    } label: {
                        Text("取消定时器")
                            .foregroundColor(.red)
                    }
                }
            }
            .listStyle(.insetGrouped)
            .navigationTitle("定时关闭")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("取消") { dismiss() }
                }
            }
        }
    }
}

// MARK: - 播放队列
struct PlayQueueSheet: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(HMPTheme.self) private var theme
    private var controller: MusicPlayerController { MusicPlayerController.shared }

    var body: some View {
        NavigationStack {
            Group {
                if controller.currentPlaylist.isEmpty {
                    VStack(spacing: 16) {
                        Spacer()
                        Image(systemName: "list.bullet")
                            .font(.system(size: 48))
                            .foregroundColor(theme.text.opacity(0.3))
                        Text("队列为空")
                            .font(TypographyTokens.bodyLarge)
                            .foregroundColor(theme.text.opacity(0.4))
                        Spacer()
                    }
                } else {
                    List {
                        ForEach(Array(controller.currentPlaylist.enumerated()), id: \.element.music.id) { index, info in
                            MusicListRow(
                                music: info.music,
                                index: index,
                                isPlaying: index == controller.currentIndex
                            ) {
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
            .navigationTitle("播放队列")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("清除") {
                        controller.clearPlaylist()
                        dismiss()
                    }
                    .disabled(controller.currentPlaylist.isEmpty)
                }
            }
        }
    }
}
