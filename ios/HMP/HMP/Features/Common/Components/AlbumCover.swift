import SwiftUI

/// 专辑封面组件 - 对应 Android AlbumCover.kt
/// 优先从 albumArtUri 加载，若为空则从音频文件路径异步提取封面
struct AlbumCover: View {
    @Environment(HMPTheme.self) private var theme

    let uri: String?
    let musicPath: String?
    let size: CGFloat
    let cornerRadius: CGFloat

    @State private var resolvedImage: UIImage?

    init(
        uri: String?,
        musicPath: String? = nil,
        size: CGFloat = 200,
        cornerRadius: CGFloat = 12
    ) {
        self.uri = uri
        self.musicPath = musicPath
        self.size = size
        self.cornerRadius = cornerRadius
    }

    var body: some View {
        ZStack {
            if let resolvedImage {
                Image(uiImage: resolvedImage)
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } else {
                placeholderView
            }
        }
        .frame(width: size, height: size)
        .clipped()
        .clipShape(RoundedRectangle(cornerRadius: cornerRadius))
        .task(id: uri) { resolveCover() }
    }

    private func resolveCover() {
        // 1. Try albumArtUri (from KMP scan)
        if let uri, !uri.isEmpty, let image = CoverCache.shared.get(path: uri) {
            resolvedImage = image
            return
        }
        // 2. Async extract from music file path (fallback)
        if let musicPath, !musicPath.isEmpty {
            // Check if already cached on disk
            if let image = CoverCache.shared.getOrExtractSync(musicPath: musicPath) {
                resolvedImage = image
                return
            }
            // Extract on background thread
            let path = musicPath
            Task.detached(priority: .utility) {
                let coverPath = ArtworkExtractor.extractAsync(filePath: path)
                if let coverPath, let image = UIImage(contentsOfFile: coverPath) {
                    CoverCache.shared.put(path: coverPath, image: image)
                    await MainActor.run { resolvedImage = image }
                }
            }
        }
    }

    private var placeholderView: some View {
        Rectangle()
            .fill(theme.primaryContainer)
            .overlay(
                Image(systemName: "music.note")
                    .font(.system(size: size * 0.35))
                    .foregroundColor(theme.onPrimary)
            )
    }
}

/// 封面图片内存缓存
final class CoverCache {
    static let shared = CoverCache()
    private let cache = NSCache<NSString, UIImage>()

    private init() {
        cache.countLimit = 200
    }

    func get(path: String) -> UIImage? {
        let key = path as NSString
        if let cached = cache.object(forKey: key) {
            return cached
        }
        if let image = UIImage(contentsOfFile: path) {
            cache.setObject(image, forKey: key)
            return image
        }
        return nil
    }

    /// Synchronous check — only returns if already in memory cache or on disk
    func getOrExtractSync(musicPath: String) -> UIImage? {
        let key = ("extract:" + musicPath) as NSString
        if let cached = cache.object(forKey: key) {
            return cached
        }
        let savedPath = ArtworkExtractor.savedCoverPath(for: musicPath)
        if let image = UIImage(contentsOfFile: savedPath) {
            cache.setObject(image, forKey: key)
            cache.setObject(image, forKey: savedPath as NSString)
            return image
        }
        return nil
    }

    /// 从音频文件提取封面并缓存。提取后保存到磁盘，后续可用 path 直接加载
    @discardableResult
    func getOrExtract(musicPath: String) -> UIImage? {
        let key = ("extract:" + musicPath) as NSString
        if let cached = cache.object(forKey: key) {
            return cached
        }

        let savedPath = ArtworkExtractor.savedCoverPath(for: musicPath)
        if let image = UIImage(contentsOfFile: savedPath) {
            cache.setObject(image, forKey: key)
            cache.setObject(image, forKey: savedPath as NSString)
            return image
        }

        let extractor = ArtworkExtractor()
        guard let coverPath = extractor.extractAndSave(filePath: musicPath) else { return nil }

        if let image = UIImage(contentsOfFile: coverPath) {
            cache.setObject(image, forKey: key)
            cache.setObject(image, forKey: coverPath as NSString)
            return image
        }
        return nil
    }

    func put(path: String, image: UIImage) {
        cache.setObject(image, forKey: path as NSString)
    }
}
