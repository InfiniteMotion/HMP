import Foundation
import shared

/// 播放编排器单例 — 等价于 Android MusicController
/// 管理：播放状态、队列、播放模式、进度、历史记录、定时器
@Observable
class MusicPlayerController {
    static let shared = MusicPlayerController()

    // MARK: - Published State

    var isPlaying: Bool = false
    var currentPlaylist: [MusicInfo_] = []
    var currentIndex: Int = -1
    var currentPlayingMusic: MusicInfo_? = nil
    var currentPosition: Int64 = 0
    var duration: Int64 = 0
    var playbackMode: PlaybackMode = .sequential
    var likeStatus: Bool = false
    var currentMusicLyrics: String? = nil
    var isMiniPlayerVisible: Bool = false
    var timerRemaining: Int64? = nil

    // MARK: - Private

    private let engine: PlayerEngine
    private let currentPlaybackUseCase: CurrentPlaybackUseCase
    private let playbackHistoryUseCase: PlaybackHistoryUseCase
    private let timerUseCase: TimerUseCase
    private let managePlaylistUseCase: ManagePlaylistUseCase
    private let settingsRepository: SettingsRepository

    private var currentPlaybackHistoryId: Int64? = nil
    private var playbackStartTime: Date? = nil
    private var listeningDurationAccumulator: Int64 = 0
    private var listeningDurationTimer: Timer? = nil
    private var sleepTimer: Timer? = nil

    private init() {
        self.engine = PlayerEngine()
        self.currentPlaybackUseCase = KoinHelperKt.getCurrentPlaybackUseCase()
        self.playbackHistoryUseCase = KoinHelperKt.getPlaybackHistoryUseCase()
        self.timerUseCase = KoinHelperKt.getTimerUseCase()
        self.managePlaylistUseCase = KoinHelperKt.getManagePlaylistUseCase()
        self.settingsRepository = KoinHelperKt.getSettingsRepository()

        setupEngineCallbacks()
        restoreSavedState()
    }

    // MARK: - Engine Callbacks

    private func setupEngineCallbacks() {
        engine.onPlaybackEnded = { [weak self] in
            self?.handlePlaybackEnded()
        }
        engine.onPositionUpdated = { [weak self] pos, dur in
            self?.currentPosition = pos
            self?.duration = dur
        }
        engine.onPlayStateChanged = { [weak self] playing in
            self?.isPlaying = playing
        }
        engine.onError = { [weak self] msg in
            print("[MusicPlayerController] error: \(msg)")
        }
    }

    // MARK: - Playback Controls

    func playWith(_ musicInfo: MusicInfo_) {
        currentPlaylist = [musicInfo]
        currentIndex = 0
        startPlaying(musicInfo)
    }

    func addAllToPlaylistInOrder(_ list: [MusicInfo_]) {
        currentPlaylist = list
        if !list.isEmpty {
            currentIndex = 0
            startPlaying(list[0])
        }
    }

    func addAllToPlaylistByShuffle(_ list: [MusicInfo_]) {
        currentPlaylist = list.shuffled()
        if !currentPlaylist.isEmpty {
            currentIndex = 0
            startPlaying(currentPlaylist[0])
        }
    }

    func playOrResume() {
        if engine.isPlaying {
            return
        }
        if currentPlayingMusic != nil {
            engine.resume()
        }
    }

    func pauseMusic() {
        engine.pause()
        pauseListeningDurationTracking()
    }

    func seekTo(position: Int64) {
        engine.seekToMs(position)
    }

    func playNext() {
        guard !currentPlaylist.isEmpty else { return }

        let nextIndex: Int
        switch playbackMode {
        case .repeatOne:
            nextIndex = currentIndex
        case .shuffle:
            nextIndex = Int.random(in: 0..<currentPlaylist.count)
        default: // SEQUENTIAL
            nextIndex = currentIndex + 1
        }

        guard nextIndex < currentPlaylist.count else { return }
        currentIndex = nextIndex
        startPlaying(currentPlaylist[nextIndex])
    }

    func playPrevious() {
        guard !currentPlaylist.isEmpty else { return }

        // If more than 3 seconds in, restart current track
        if currentPosition > 3000 {
            seekTo(position: 0)
            return
        }

        let prevIndex = max(0, currentIndex - 1)
        currentIndex = prevIndex
        startPlaying(currentPlaylist[prevIndex])
    }

    func togglePlaybackModeByOrder() {
        switch playbackMode {
        case .sequential:
            playbackMode = .repeatOne
        case .repeatOne:
            playbackMode = .shuffle
        case .shuffle:
            playbackMode = .sequential
        default:
            playbackMode = .sequential
        }
    }

    func addToNextPlay(_ musicInfo: MusicInfo_) {
        let insertIndex = currentIndex + 1
        if insertIndex >= currentPlaylist.count {
            currentPlaylist.append(musicInfo)
        } else {
            currentPlaylist.insert(musicInfo, at: insertIndex)
        }
    }

    func playAt(_ index: Int) {
        guard index >= 0 && index < currentPlaylist.count else { return }
        currentIndex = index
        startPlaying(currentPlaylist[index])
    }

    func clearPlaylist() {
        currentPlaylist = []
        currentIndex = -1
        currentPlayingMusic = nil
        isMiniPlayerVisible = false
        engine.stop()
    }

    // MARK: - Private Helpers

    private func startPlaying(_ musicInfo: MusicInfo_) {
        // End previous session
        if currentPlaybackHistoryId != nil {
            endCurrentPlaybackSession(isCompleted: false)
        }

        currentPlayingMusic = musicInfo
        isMiniPlayerVisible = true

        let path = musicInfo.music.path
        let fileExists = FileManager.default.fileExists(atPath: path)
        if !fileExists {
            // Path may be stale (app reinstalled), try Documents directory
            let filename = (path as NSString).lastPathComponent
            let docsDir = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true).first ?? ""
            let newPath = (docsDir as NSString).appendingPathComponent(filename)
            if FileManager.default.fileExists(atPath: newPath) {
                let url = URL(fileURLWithPath: newPath)
                engine.play(url: url)
                return
            }
        }
        let url = URL(fileURLWithPath: path)
        engine.play(url: url)

        // Load metadata
        loadMetadata(for: musicInfo)

        // Start new playback session
        startNewPlaybackSession(musicInfo: musicInfo)

        // Persist state
        persistPlaybackState()

        // Update Now Playing
        NowPlayingManager.shared.updateNowPlayingInfo(
            title: musicInfo.music.title,
            artist: musicInfo.music.artist,
            album: musicInfo.music.album,
            duration: Double(musicInfo.music.duration) / 1000.0,
            elapsedTime: 0
        )
    }

    private func loadMetadata(for musicInfo: MusicInfo_) {
        let musicId = musicInfo.music.id

        // Like status
        Task {
            do {
                let liked = try await currentPlaybackUseCase.getLikedStatus(musicId: musicId)
                await MainActor.run { self.likeStatus = liked.boolValue }
            } catch {
                print("[MusicPlayerController] getLikedStatus failed: \(error)")
            }
        }

        // Lyrics
        Task {
            do {
                let lyrics = try await currentPlaybackUseCase.getMusicLyrics(musicId: musicId)
                await MainActor.run { self.currentMusicLyrics = lyrics }
            } catch {
                print("[MusicPlayerController] getMusicLyrics failed: \(error)")
            }
        }
    }

    private func handlePlaybackEnded() {
        endCurrentPlaybackSession(isCompleted: true)

        switch playbackMode {
        case .repeatOne:
            seekTo(position: 0)
            engine.resume()
            startNewPlaybackSession(musicInfo: currentPlayingMusic!)
        case .shuffle:
            playNext()
        default: // SEQUENTIAL
            if currentIndex + 1 < currentPlaylist.count {
                playNext()
            }
            // else: end of playlist, don't auto-loop
        }
    }

    // MARK: - Playback Session Tracking

    private func startNewPlaybackSession(musicInfo: MusicInfo_) {
        let musicId = musicInfo.music.id
        playbackStartTime = Date()
        listeningDurationAccumulator = 0

        Task {
            do {
                let historyId = try await playbackHistoryUseCase.startPlaybackSession(musicId: musicId, source: nil)
                await MainActor.run { self.currentPlaybackHistoryId = historyId.int64Value }
            } catch {
                print("[MusicPlayerController] startPlaybackSession failed: \(error)")
            }
        }

        startListeningDurationTracking()
    }

    private func endCurrentPlaybackSession(isCompleted: Bool) {
        guard let historyId = currentPlaybackHistoryId,
              let musicInfo = currentPlayingMusic else { return }

        let duration = listeningDurationAccumulator
        stopListeningDurationTracking()

        Task {
            do {
                if isCompleted {
                    try await playbackHistoryUseCase.completePlaybackSession(
                        historyId: historyId,
                        musicId: musicInfo.music.id,
                        duration: duration
                    )
                } else {
                    try await playbackHistoryUseCase.skipPlaybackSession(
                        historyId: historyId,
                        musicId: musicInfo.music.id,
                        duration: duration,
                        isSkip: true
                    )
                }
            } catch {
                print("[MusicPlayerController] endPlaybackSession failed: \(error)")
            }
        }

        currentPlaybackHistoryId = nil
    }

    private func startListeningDurationTracking() {
        stopListeningDurationTracking()
        listeningDurationTimer = Timer.scheduledTimer(withTimeInterval: 30.0, repeats: true) { [weak self] _ in
            self?.recordListeningDurationTick()
        }
    }

    private func stopListeningDurationTracking() {
        listeningDurationTimer?.invalidate()
        listeningDurationTimer = nil
    }

    private func pauseListeningDurationTracking() {
        // Accumulate time played so far
        if let start = playbackStartTime {
            listeningDurationAccumulator += Int64(Date().timeIntervalSince(start) * 1000)
            playbackStartTime = nil
        }
    }

    private func recordListeningDurationTick() {
        // Add time since last checkpoint
        if let start = playbackStartTime {
            listeningDurationAccumulator += Int64(Date().timeIntervalSince(start) * 1000)
            playbackStartTime = Date()
        }

        let duration = listeningDurationAccumulator
        listeningDurationAccumulator = 0

        Task {
            do {
                try await playbackHistoryUseCase.recordListeningDuration(duration: duration)
            } catch {
                print("[MusicPlayerController] recordListeningDuration failed: \(error)")
            }
        }
    }

    // MARK: - Sleep Timer

    func startTimer(minutes: Int) {
        cancelTimer()
        let ms = Int64(minutes) * 60 * 1000
        timerUseCase.setTimerRemaining(milliseconds: KotlinLong(longLong: ms))

        sleepTimer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [weak self] _ in
            self?.tickSleepTimer()
        }
    }

    func cancelTimer() {
        sleepTimer?.invalidate()
        sleepTimer = nil
        timerUseCase.cancelTimer()
        timerRemaining = nil
    }

    private func tickSleepTimer() {
        timerUseCase.decrementTimer(decrement: 1000)
        // decrementTimer sets remaining to null when it hits 0
        if !timerUseCase.isTimerActive() {
            pauseMusic()
            cancelTimer()
        }
        timerRemaining = (timerUseCase.timerRemaining.value as? KotlinLong)?.int64Value
    }

    // MARK: - Like

    func updateLikedStatus(_ liked: Bool) {
        guard let musicInfo = currentPlayingMusic else { return }
        likeStatus = liked

        Task {
            do {
                try await currentPlaybackUseCase.updateLikedStatus(musicId: musicInfo.music.id, liked: liked)
            } catch {
                print("[MusicPlayerController] updateLikedStatus failed: \(error)")
            }
        }
    }

    // MARK: - Persist / Restore State

    private func persistPlaybackState() {
        guard let musicInfo = currentPlayingMusic else { return }
        Task {
            do {
                try await settingsRepository.saveCurrentMusicId(id: musicInfo.music.id)
                try await settingsRepository.saveCurrentPosition(position: 0)
            } catch {
                print("[MusicPlayerController] persistPlaybackState failed: \(error)")
            }
        }
    }

    private func restoreSavedState() {
        // Playback state restoration deferred — will be handled when user interacts
    }
}
