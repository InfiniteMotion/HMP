import Foundation

/// 音乐扫描服务 - 使用 FileManager 扫描本地音乐文件
class MusicScannerService {
    static let shared = MusicScannerService()
    
    /// 扫描音乐文件
    func scanMusicFiles() -> [URL] {
        var musicFiles: [URL] = []
        
        // 获取 Documents 目录
        guard let documentsDirectory = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first else {
            return musicFiles
        }
        
        // 扫描 Documents 目录
        do {
            let files = try FileManager.default.contentsOfDirectory(at: documentsDirectory, includingPropertiesForKeys: nil)
            
            // 筛选音乐文件（支持的格式）
            let supportedExtensions = ["mp3", "m4a", "wav", "aiff", "flac"]
            for file in files {
                let fileExtension = file.pathExtension.lowercased()
                if supportedExtensions.contains(fileExtension) {
                    musicFiles.append(file)
                }
            }
        } catch {
            print("Error scanning music files: \(error)")
        }
        
        return musicFiles
    }
    
    /// 获取音乐文件信息
    func getMusicFileInfo(url: URL) -> (title: String, artist: String, album: String)? {
        // 这里可以使用 shared 模块的 MusicTagParser 来解析音乐标签
        // 暂时返回文件名作为标题
        let fileName = url.lastPathComponent.replacingOccurrences(of: ".\(url.pathExtension)", with: "")
        return (title: fileName, artist: "Unknown", album: "Unknown")
    }
}
