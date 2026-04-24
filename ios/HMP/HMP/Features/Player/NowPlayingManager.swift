import AVFoundation
import MediaPlayer

/// 现在播放管理器 - 处理锁屏控制和媒体信息中心
class NowPlayingManager {
    static let shared = NowPlayingManager()
    
    private init() {
        setupRemoteCommandCenter()
    }
    
    /// 更新锁屏信息
    func updateNowPlayingInfo(title: String, artist: String, album: String, artwork: UIImage? = nil, duration: TimeInterval, elapsedTime: TimeInterval) {
        var nowPlayingInfo = [String: Any]()
        
        nowPlayingInfo[MPMediaItemPropertyTitle] = title
        nowPlayingInfo[MPMediaItemPropertyArtist] = artist
        nowPlayingInfo[MPMediaItemPropertyAlbumTitle] = album
        
        if let artwork = artwork {
            nowPlayingInfo[MPMediaItemPropertyArtwork] = MPMediaItemArtwork(boundsSize: artwork.size) { _ in
                return artwork
            }
        }
        
        nowPlayingInfo[MPMediaItemPropertyPlaybackDuration] = duration
        nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = elapsedTime
        nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] = PlayerService.shared.isCurrentlyPlaying() ? 1.0 : 0.0
        
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
    }
    
    /// 设置远程控制中心
    private func setupRemoteCommandCenter() {
        let commandCenter = MPRemoteCommandCenter.shared()
        
        // 播放/暂停
        commandCenter.playCommand.addTarget { [weak self] event in
            PlayerService.shared.resume()
            return .success
        }
        
        commandCenter.pauseCommand.addTarget { [weak self] event in
            PlayerService.shared.pause()
            return .success
        }
        
        // 下一首
        commandCenter.nextTrackCommand.addTarget { [weak self] event in
            // 实现下一首逻辑
            return .success
        }
        
        // 上一首
        commandCenter.previousTrackCommand.addTarget { [weak self] event in
            // 实现上一首逻辑
            return .success
        }
        
        // 快进/快退
        commandCenter.seekForwardCommand.addTarget { [weak self] event in
            // 实现快进逻辑
            return .success
        }
        
        commandCenter.seekBackwardCommand.addTarget { [weak self] event in
            // 实现快退逻辑
            return .success
        }
    }
    
    /// 清除现在播放信息
    func clearNowPlayingInfo() {
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nil
    }
}
