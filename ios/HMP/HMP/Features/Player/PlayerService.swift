import AVFoundation

/// 播放器服务 - 封装 AVPlayer，提供播放控制
class PlayerService {
    static let shared = PlayerService()
    
    private var player: AVPlayer?
    private var isPlaying: Bool = false
    
    func play(url: URL) {
        let playerItem = AVPlayerItem(url: url)
        player = AVPlayer(playerItem: playerItem)
        player?.play()
        isPlaying = true
    }
    
    func pause() {
        player?.pause()
        isPlaying = false
    }
    
    func resume() {
        player?.play()
        isPlaying = true
    }
    
    func seek(to time: CMTime) {
        player?.seek(to: time)
    }
    
    func getCurrentTime() -> CMTime? {
        return player?.currentTime()
    }
    
    func getDuration() -> CMTime? {
        return player?.currentItem?.duration
    }
    
    func isCurrentlyPlaying() -> Bool {
        return isPlaying
    }
}
