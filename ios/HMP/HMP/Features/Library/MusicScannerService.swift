import Foundation
import MediaPlayer
import AVFoundation

/// 音乐扫描服务 - 递归扫描应用沙盒 Documents 目录
class MusicScannerService {
    static let shared = MusicScannerService()
    
    /// 支持的音乐文件扩展名
    private let supportedExtensions = ["mp3", "m4a", "aac", "wav", "flac", "alac", "ogg"]
    
    /// 扫描音乐文件（递归扫描 Documents 目录）
    func scanMusicFiles() -> [URL] {
        print("[MusicScanner] scanMusicFiles called")
        var musicFiles: [URL] = []
        
        // 递归扫描 Documents 目录及其所有子目录
        let documentsFiles = scanDocumentsDirectory()
        print("[MusicScanner] found \(documentsFiles.count) files in Documents")
        musicFiles.append(contentsOf: documentsFiles)
        
        print("[MusicScanner] total files: \(musicFiles.count)")
        return musicFiles
    }
    
    /// 递归扫描应用沙盒 Documents 目录
    private func scanDocumentsDirectory() -> [URL] {
        var musicFiles: [URL] = []
        
        guard let documentsURL = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first else {
            print("[MusicScanner] cannot get Documents directory")
            return musicFiles
        }
        
        print("[MusicScanner] scanning Documents directory: \(documentsURL.path)")
        
        do {
            // 递归扫描 Documents 及其所有子目录
            let enumerator = FileManager.default.enumerator(at: documentsURL, includingPropertiesForKeys: nil)
            var itemCount = 0
            
            while let fileURL = enumerator?.nextObject() as? URL {
                itemCount += 1
                let ext = fileURL.pathExtension.lowercased()
                if supportedExtensions.contains(ext) {
                    print("[MusicScanner] found music file: \(fileURL.lastPathComponent)")
                    musicFiles.append(fileURL)
                }
            }
            print("[MusicScanner] scanned \(itemCount) total items in Documents")
        } catch {
            print("[MusicScanner] error reading Documents: \(error)")
        }
        
        return musicFiles
    }
    
    /// 获取音乐文件元数据信息
    func getMusicFileInfo(url: URL) -> (title: String, artist: String, album: String)? {
        let asset = AVURLAsset(url: url)
        let metadata = asset.commonMetadata
        var title: String?
        var artist: String?
        var album: String?
        
        for item in metadata {
            if let key = item.commonKey?.rawValue {
                switch key {
                case "title": title = item.stringValue
                case "artist": artist = item.stringValue
                case "albumName": album = item.stringValue
                default: break
                }
            }
        }
        
        return (title: title ?? url.lastPathComponent, artist: artist ?? "Unknown", album: album ?? "Unknown")
    }
}
