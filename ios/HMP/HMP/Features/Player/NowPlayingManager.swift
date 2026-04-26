import MediaPlayer

/// 锁屏/控制中心播放信息与远程控制
class NowPlayingManager {
    static let shared = NowPlayingManager()

    private init() {
        setupRemoteCommandCenter()
    }

    func updateNowPlayingInfo(title: String, artist: String, album: String,
                              artwork: UIImage? = nil, duration: TimeInterval, elapsedTime: TimeInterval) {
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
        nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] = MusicPlayerController.shared.isPlaying ? 1.0 : 0.0

        MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
    }

    func updateElapsedTime(_ elapsed: TimeInterval) {
        guard var info = MPNowPlayingInfoCenter.default().nowPlayingInfo else { return }
        info[MPNowPlayingInfoPropertyElapsedPlaybackTime] = elapsed
        info[MPNowPlayingInfoPropertyPlaybackRate] = MusicPlayerController.shared.isPlaying ? 1.0 : 0.0
        MPNowPlayingInfoCenter.default().nowPlayingInfo = info
    }

    private func setupRemoteCommandCenter() {
        let commandCenter = MPRemoteCommandCenter.shared()

        commandCenter.playCommand.addTarget { _ in
            MusicPlayerController.shared.playOrResume()
            return .success
        }

        commandCenter.pauseCommand.addTarget { _ in
            MusicPlayerController.shared.pauseMusic()
            return .success
        }

        commandCenter.nextTrackCommand.addTarget { _ in
            MusicPlayerController.shared.playNext()
            return .success
        }

        commandCenter.previousTrackCommand.addTarget { _ in
            MusicPlayerController.shared.playPrevious()
            return .success
        }

        commandCenter.seekForwardCommand.addTarget { event in
            guard let evt = event as? MPSeekCommandEvent else { return .commandFailed }
            let current = MusicPlayerController.shared.currentPosition
            MusicPlayerController.shared.seekTo(position: current + 10_000)
            return .success
        }

        commandCenter.seekBackwardCommand.addTarget { event in
            guard let evt = event as? MPSeekCommandEvent else { return .commandFailed }
            let current = MusicPlayerController.shared.currentPosition
            MusicPlayerController.shared.seekTo(position: max(0, current - 10_000))
            return .success
        }
    }

    func clearNowPlayingInfo() {
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nil
    }
}
