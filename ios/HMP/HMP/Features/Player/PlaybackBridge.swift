import Foundation
import Observation
import sharedIos

/// Kotlin ↔ Swift 播放双桥（方向 A Phase 1 / A4）
///
/// 职责（对齐 shared-ui iosMain 的 IosPlaybackController / IosPlaybackCommands /
/// IosPlaybackStateSink）：
/// 1. 命令注册：Kotlin PlaybackController 方法 → MusicPlayerController 调用
/// 2. 状态推送：MusicPlayerController（@Observable）变更 → Kotlin StateFlow
///    （Swift Observation 的 withObservationTracking 订阅，引擎进度回调同源触发）
///
/// 跨框架类型：sharedUi framework 将 shared 的 domain 类型以 SharedUi 前缀重导出，
/// 与 shared framework 的 Swift 名字成双份（MusicInfo vs SharedMusicInfo 等）；
/// 桥的两侧统一用 Any / 基础类型跨越（底层为同一 ObjC 类，as! 运行时安全）。
///
/// 保留 Swift 原生层能力：NowPlayingInfo / RemoteCommand / LiveActivity 由
/// MusicPlayerController + HMPMediaSession 内部驱动，桥接不介入。
///
/// 安装时机：AppDelegate didFinishLaunching（Koin init 之后、播放器初始化之前）。
@MainActor
enum PlaybackBridge {
    static func install() {
        let ctrl = MusicPlayerController.shared
        let sink = IosPlaybackController.shared.stateSink
        let commands = IosPlaybackCommands.shared

        // ── 命令注册（Kotlin 界面 → Swift 引擎） ──

        commands.playWith = { music in ctrl.playWith(music as! MusicInfo) }
        commands.playOrResume = { ctrl.playOrResume() }
        commands.pauseMusic = { ctrl.pauseMusic() }
        commands.playNext = { ctrl.playNext() }
        commands.playPrevious = { ctrl.playPrevious() }
        commands.seekTo = { ctrl.seekTo(position: $0.int64Value) }
        commands.togglePlaybackModeByOrder = { ctrl.togglePlaybackModeByOrder() }
        commands.setMiniPlayerVisible = { ctrl.isMiniPlayerVisible = $0.boolValue }
        commands.playAt = { music in ctrl.playAt(music as! MusicInfo) }
        commands.addToPlaylist = { ctrl.addToPlaylist($0 as! MusicInfo) }
        commands.addToNextPlay = { ctrl.addToNextPlay($0 as! MusicInfo) }
        commands.removeFromPlaylist = { ctrl.removeFromPlaylist($0 as! MusicInfo) }
        commands.moveToTop = { ctrl.moveToTop($0 as! MusicInfo) }
        commands.clearPlaylist = { ctrl.clearPlaylist() }
        commands.addAllToPlaylistInOrder = { list in
            ctrl.addAllToPlaylistInOrder(list as! [MusicInfo])
        }
        commands.addAllToPlaylistByShuffle = { list in
            ctrl.addAllToPlaylistByShuffle(list as! [MusicInfo])
        }
        commands.playHeartMode = { ctrl.playHeartMode() }
        commands.updateMusicLikedStatus = { _, liked in ctrl.updateLikedStatus(liked.boolValue) }
        commands.startTimer = { ctrl.startTimer(minutes: Int($0)) }
        commands.cancelTimer = { ctrl.cancelTimer() }

        // ── 状态观察（Swift 引擎 → Kotlin StateFlow） ──

        startStateObservation(ctrl: ctrl, sink: sink)
    }

    private static func startStateObservation(ctrl: MusicPlayerController, sink: IosPlaybackStateSink) {
        func push() {
            // PlaybackMode 走 name（"SEQUENTIAL"/"REPEAT_ONE"/"SHUFFLE"）；
            // Kotlin Long? 导出为 KotlinLong?，Swift Int64? 需显式装箱
            sink.updateCurrentMusic(music: ctrl.currentPlayingMusic)
            sink.updateIsPlaying(playing: ctrl.isPlaying)
            sink.updatePosition(positionMs: ctrl.currentPosition, durationMs: ctrl.duration)
            sink.updatePlaybackMode(mode: ctrl.playbackMode.name)
            sink.updatePlaylist(list: ctrl.currentPlaylist)
            sink.updateCurrentIndex(index: Int32(ctrl.currentIndex))
            sink.updateLikedStatus(liked: ctrl.likeStatus)
            sink.updateLyrics(lyrics: ctrl.currentMusicLyrics)
            if let timer = ctrl.timerRemaining {
                sink.updateTimerRemaining(ms: KotlinLong(longLong: timer))
            } else {
                sink.updateTimerRemaining(ms: nil)
            }
            sink.updateMiniPlayerVisible(visible: ctrl.isMiniPlayerVisible)
        }

        /// 订阅全部状态属性；任一变更时重推 + 重新订阅（Swift Observation 标准循环模式）
        func track() {
            withObservationTracking {
                _ = ctrl.currentPlayingMusic
                _ = ctrl.isPlaying
                _ = ctrl.currentPosition
                _ = ctrl.duration
                _ = ctrl.playbackMode
                _ = ctrl.currentPlaylist
                _ = ctrl.currentIndex
                _ = ctrl.likeStatus
                _ = ctrl.currentMusicLyrics
                _ = ctrl.timerRemaining
                _ = ctrl.isMiniPlayerVisible
            } onChange: {
                Task { @MainActor in
                    push()
                    track()
                }
            }
        }

        push()
        track()
    }
}