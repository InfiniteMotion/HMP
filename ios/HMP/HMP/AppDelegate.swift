import UIKit
import shared
import MediaPlayer

/// iOS AppDelegate — 在应用启动时初始化 Koin (KMP DI)
class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        requestMusicLibraryPermission()
        ensureDocumentsFolderVisible()
        KoinInitializer().doInit()
        print("[AppDelegate] Koin initialized")
        MetadataParserBridge().register(parser: MusicMetadataParser())
        return true
    }

    private func requestMusicLibraryPermission() {
        let status = MPMediaLibrary.authorizationStatus()
        if status == .notDetermined {
            MPMediaLibrary.requestAuthorization { _ in }
        }
    }

    /// 在 Documents 目录创建一个占位文件，使 HMP 文件夹在文件 App 中可见
    private func ensureDocumentsFolderVisible() {
        guard let documentsURL = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first else {
            print("[AppDelegate] cannot get Documents directory")
            return
        }

        let placeholderFile = documentsURL.appendingPathComponent(".hmp-folder-visible")
        if !FileManager.default.fileExists(atPath: placeholderFile.path) {
            do {
                try "HMP Music Player Documents".write(to: placeholderFile, atomically: true, encoding: .utf8)
                print("[AppDelegate] created placeholder file to make Documents folder visible: \(placeholderFile.path)")
            } catch {
                print("[AppDelegate] failed to create placeholder file: \(error)")
            }
        }
    }
}
