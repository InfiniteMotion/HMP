import ActivityKit
import Foundation

struct HMPNowPlayingAttributes: ActivityAttributes {
    public struct ContentState: Codable, Hashable {
        var title: String
        var artist: String
        var album: String
        var isPlaying: Bool
        var position: Double
        var duration: Double
        var artworkData: Data?
        var currentLyricLine: String?
        var showAnimation: Bool
        var showLyrics: Bool
    }

    var appName: String = "HMP"
}

extension Notification.Name {
    static let hmpPlay = Notification.Name("com.hmp.play")
    static let hmpPause = Notification.Name("com.hmp.pause")
    static let hmpNextTrack = Notification.Name("com.hmp.nextTrack")
    static let hmpPreviousTrack = Notification.Name("com.hmp.previousTrack")
}
