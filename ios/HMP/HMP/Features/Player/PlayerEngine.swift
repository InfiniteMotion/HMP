import AVFoundation

/// 底层播放引擎 — 封装 AVPlayer，提供播放控制和进度回调
/// 等价于 Android 的 PlayControl 接口
class PlayerEngine {
    private var player: AVPlayer?
    var avPlayer: AVPlayer? { player }
    private var timeObserver: Any?
    private var statusObserver: NSKeyValueObservation?
    private var rateObserver: NSKeyValueObservation?
    private var itemStatusObserver: NSKeyValueObservation?
    
    /// 是否准备就绪
    var isReady: Bool = false

    var onPlaybackEnded: (() -> Void)?
    var onPositionUpdated: ((Int64, Int64) -> Void)? // (currentMs, durationMs)
    var onPlayStateChanged: ((Bool) -> Void)?
    var onError: ((String) -> Void)?
    /// 播放器准备就绪回调
    var onReady: (() -> Void)?

    var isPlaying: Bool {
        return player?.rate ?? 0 > 0
    }

    deinit {
        cleanup()
    }

    func play(url: URL) {
        cleanup()
        isReady = false

        let item = AVPlayerItem(url: url)
        player = AVPlayer(playerItem: item)

        observeItemStatus(item)
        observePlayerRate()
        addPeriodicTimeObserver()

        player?.play()
    }

    func pause() {
        player?.pause()
    }

    func resume() {
        player?.play()
    }

    func seek(to time: CMTime) {
        player?.seek(to: time, toleranceBefore: .zero, toleranceAfter: .zero)
    }

    func seekToMs(_ ms: Int64) {
        let time = CMTime(value: ms, timescale: 1000)
        seek(to: time)
    }

    func getCurrentTime() -> CMTime? {
        return player?.currentTime()
    }

    func getDurationMs() -> Int64 {
        guard let item = player?.currentItem else { return 0 }
        let seconds = CMTimeGetSeconds(item.duration)
        if seconds.isNaN || seconds.isInfinite || seconds < 0 { return 0 }
        return Int64(seconds * 1000)
    }

    func getCurrentPositionMs() -> Int64 {
        guard let time = player?.currentTime() else { return 0 }
        let seconds = CMTimeGetSeconds(time)
        if seconds.isNaN || seconds.isInfinite { return 0 }
        return Int64(seconds * 1000)
    }

    func stop() {
        cleanup()
    }

    // MARK: - Private

    private func addPeriodicTimeObserver() {
        let interval = CMTime(value: 500, timescale: 1000) // 0.5s
        timeObserver = player?.addPeriodicTimeObserver(
            forInterval: interval,
            queue: .main
        ) { [weak self] _ in
            guard let self else { return }
            let pos = self.getCurrentPositionMs()
            let dur = self.getDurationMs()
            self.onPositionUpdated?(pos, dur)
        }
    }

    private func observeItemStatus(_ item: AVPlayerItem) {
        itemStatusObserver = item.observe(\.status, options: [.new]) { [weak self] item, _ in
            guard let self else { return }
            
            if item.status == .readyToPlay {
                // 播放器准备就绪
                self.isReady = true
                self.onReady?()
            } else if item.status == .failed {
                self.onError?(item.error?.localizedDescription ?? "Playback failed")
            }
        }

        NotificationCenter.default.addObserver(
            forName: .AVPlayerItemDidPlayToEndTime,
            object: item,
            queue: .main
        ) { [weak self] _ in
            self?.onPlaybackEnded?()
        }
    }

    private func observePlayerRate() {
        rateObserver = player?.observe(\.rate, options: [.new]) { [weak self] player, _ in
            self?.onPlayStateChanged?(player.rate > 0)
        }
    }

    private func cleanup() {
        isReady = false
        
        if let observer = timeObserver {
            player?.removeTimeObserver(observer)
            timeObserver = nil
        }
        NotificationCenter.default.removeObserver(self)
        statusObserver?.invalidate()
        statusObserver = nil
        rateObserver?.invalidate()
        rateObserver = nil
        itemStatusObserver?.invalidate()
        itemStatusObserver = nil
        player?.replaceCurrentItem(with: nil)
        player = nil
    }
}