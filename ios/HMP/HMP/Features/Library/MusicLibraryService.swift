import Foundation
import MediaPlayer
import AVFoundation

class MusicLibraryService {
    static let shared = MusicLibraryService()

    enum AuthorizationStatus {
        case notDetermined
        case authorized
        case denied
        case restricted
    }

    func checkAuthorizationStatus() -> AuthorizationStatus {
        let status = MPMediaLibrary.authorizationStatus()
        print("[MusicLibrary] authorizationStatus: \(status)")
        switch status {
        case .notDetermined:
            return .notDetermined
        case .authorized:
            return .authorized
        case .denied:
            return .denied
        case .restricted:
            return .restricted
        @unknown default:
            return .notDetermined
        }
    }

    func requestAuthorization(completion: @escaping (AuthorizationStatus) -> Void) {
        print("[MusicLibrary] requesting authorization...")
        MPMediaLibrary.requestAuthorization { status in
            print("[MusicLibrary] authorization result: \(status.rawValue)")
            switch status {
            case .authorized:
                completion(.authorized)
            case .denied:
                completion(.denied)
            case .restricted:
                completion(.restricted)
            default:
                completion(.notDetermined)
            }
        }
    }

    func fetchAllSongs() -> [MPMediaItem] {
        print("[MusicLibrary] fetchAllSongs called")
        guard checkAuthorizationStatus() == .authorized else {
            print("[MusicLibrary] not authorized, returning empty")
            return []
        }

        let query = MPMediaQuery.songs()
        let items = query.items ?? []
        print("[MusicLibrary] found \(items.count) songs")
        return items
    }

    func fetchSongMetadata(item: MPMediaItem) -> (title: String, artist: String, album: String, duration: TimeInterval)? {
        let title = item.title ?? "Unknown"
        let artist = item.artist ?? "Unknown Artist"
        let album = item.albumTitle ?? "Unknown Album"
        let duration = item.playbackDuration
        print("[MusicLibrary] metadata - title: \(title), artist: \(artist), album: \(album), duration: \(duration)")
        return (title: title, artist: artist, album: album, duration: duration)
    }
}
