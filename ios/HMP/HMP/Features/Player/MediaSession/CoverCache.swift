import UIKit

/// 封面图片内存缓存（A9：从已删除的 SwiftUI AlbumCover.swift 中提取保留，
/// 供原生层 AfterNowPlaying / LiveActivity 封面加载使用）
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
        // 处理 file:// 格式的 URI
        let filePath: String
        if path.hasPrefix("file://") {
            filePath = String(path.dropFirst(7))
        } else {
            filePath = path
        }
        if let image = UIImage(contentsOfFile: filePath) {
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