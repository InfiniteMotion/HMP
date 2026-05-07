import UIKit
import shared

class ArtworkLoader {
    private var currentLoadTask: Task<Void, Never>?

    func loadArtwork(musicInfo: MusicInfo_, completion: @escaping (UIImage?) -> Void) {
        currentLoadTask?.cancel()
        currentLoadTask = Task {
            let image = await loadArtworkAsync(musicInfo: musicInfo)
            guard !Task.isCancelled else { return }
            await MainActor.run { completion(image) }
        }
    }

    func loadArtworkAsync(musicInfo: MusicInfo_) async -> UIImage? {
        let albumArtUri = musicInfo.music.albumArtUri
        if !albumArtUri.isEmpty {
            if let cached = CoverCache.shared.get(path: albumArtUri) {
                return cached
            }
        }

        let musicPath = musicInfo.music.path
        if !musicPath.isEmpty {
            if let cached = CoverCache.shared.getOrExtractSync(musicPath: musicPath) {
                return cached
            }

            let path = musicPath
            let extracted = await Task.detached(priority: .utility) {
                ArtworkExtractor.extractAsync(filePath: path)
            }.value

            if let coverPath = extracted, let image = UIImage(contentsOfFile: coverPath) {
                CoverCache.shared.put(path: coverPath, image: image)
                return image
            }
        }

        return nil
    }

    func cancel() {
        currentLoadTask?.cancel()
        currentLoadTask = nil
    }
}
