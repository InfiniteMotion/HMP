import MediaPlayer

class RemoteCommandManager {
    var onPlay: (() -> Void)?
    var onPause: (() -> Void)?
    var onNext: (() -> Void)?
    var onPrevious: (() -> Void)?
    var onSeek: ((TimeInterval) -> Void)?
    var onSeekForward: (() -> Void)?
    var onSeekBackward: (() -> Void)?

    private let commandCenter = MPRemoteCommandCenter.shared()

    func setupCommands() {
        commandCenter.playCommand.addTarget { [weak self] _ in
            self?.onPlay?()
            return .success
        }

        commandCenter.pauseCommand.addTarget { [weak self] _ in
            self?.onPause?()
            return .success
        }

        commandCenter.nextTrackCommand.addTarget { [weak self] _ in
            self?.onNext?()
            return .success
        }

        commandCenter.previousTrackCommand.addTarget { [weak self] _ in
            self?.onPrevious?()
            return .success
        }

        commandCenter.changePlaybackPositionCommand.addTarget { [weak self] event in
            guard let positionEvent = event as? MPChangePlaybackPositionCommandEvent else {
                return .commandFailed
            }
            self?.onSeek?(positionEvent.positionTime)
            return .success
        }

        commandCenter.seekForwardCommand.addTarget { [weak self] event in
            guard let _ = event as? MPSeekCommandEvent else { return .commandFailed }
            self?.onSeekForward?()
            return .success
        }

        commandCenter.seekBackwardCommand.addTarget { [weak self] event in
            guard let _ = event as? MPSeekCommandEvent else { return .commandFailed }
            self?.onSeekBackward?()
            return .success
        }

        commandCenter.changePlaybackPositionCommand.isEnabled = true
        commandCenter.playCommand.isEnabled = true
        commandCenter.pauseCommand.isEnabled = true
        commandCenter.nextTrackCommand.isEnabled = true
        commandCenter.previousTrackCommand.isEnabled = true
    }

    func updateAvailableCommands(hasNext: Bool, hasPrevious: Bool) {
        commandCenter.nextTrackCommand.isEnabled = hasNext
        commandCenter.previousTrackCommand.isEnabled = hasPrevious
    }

    func enableSeekCommands(enabled: Bool) {
        commandCenter.seekForwardCommand.isEnabled = enabled
        commandCenter.seekBackwardCommand.isEnabled = enabled
        commandCenter.changePlaybackPositionCommand.isEnabled = enabled
    }
}
