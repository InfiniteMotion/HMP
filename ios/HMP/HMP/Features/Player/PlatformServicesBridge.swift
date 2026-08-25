import Foundation
import UIKit
import PhotosUI
import UniformTypeIdentifiers
import sharedIos

/// 平台服务桥（A6：shared-ui Compose UI 的 Share / FilePicker / Cover 选择 → Swift 系统能力）
///
/// 对应 shared-ui iosMain 的 IosPlatformServicesBridge（public object），
/// 在 AppDelegate 启动时注册闭包：
/// - 分享：UIActivityViewController（文件 / 文本）
/// - 图库选图（封面/头像）：PHPickerViewController
/// - 备份文件选择：UIDocumentPickerViewController
///
/// 全部结果回主线程回调（Compose 状态更新线程模型要求）。
@MainActor
enum PlatformServicesBridge {
    static func install() {
        let bridge = IosPlatformServicesBridge.shared

        bridge.shareMusic = { request in
            var items: [Any] = []
            if FileManager.default.fileExists(atPath: request.filePath) {
                items.append(URL(fileURLWithPath: request.filePath))
            } else {
                items.append("\(request.title) - \(request.artist) (\(request.album))")
            }
            presentActivity(items: items)
        }

        bridge.shareText = { subject, text in
            presentActivity(items: ["\(subject)\n\(text)"])
        }

        bridge.shareFile = { filePath, _, _ in
            guard FileManager.default.fileExists(atPath: filePath) else { return }
            presentActivity(items: [URL(fileURLWithPath: filePath)])
        }

        bridge.pickImage = { onResult in
            pickImage { bytes in
                // 选图结果以临时文件落盘，返回文件路径（UIKit 拾取器输出为 bytes）
                guard let bytes else { onResult(nil); return }
                let tmp = FileManager.default.temporaryDirectory
                    .appendingPathComponent("hmp-picked-\(UUID().uuidString).jpg")
                do {
                    try bytes.write(to: tmp)
                    onResult(tmp.path)
                } catch {
                    onResult(nil)
                }
            }
        }

        bridge.pickCoverImage = { onResult in
            pickImage { bytes in
                onResult(bytes.map(kotlinBytes))
            }
        }

        bridge.openBackupFile = { onResult in
            presentDocumentPicker { url in
                onResult(url?.path)
            }
        }
    }

    /// Data → KotlinByteArray（sharedIos 导出 Kotlin ByteArray 为 KotlinByteArray）
    private static func kotlinBytes(_ data: Data) -> KotlinByteArray {
        let arr = KotlinByteArray(size: Int32(data.count))
        for (i, b) in data.enumerated() {
            arr.set(index: Int32(i), value: Int8(bitPattern: b))
        }
        return arr
    }

    // MARK: - UIActivityViewController

    private static func presentActivity(items: [Any]) {
        let vc = UIActivityViewController(activityItems: items, applicationActivities: nil)
        guard let root = topViewController() else { return }
        vc.popoverPresentationController?.sourceView = root.view
        vc.popoverPresentationController?.sourceRect = CGRect(x: root.view.bounds.midX, y: root.view.bounds.midY, width: 0, height: 0)
        root.present(vc, animated: true)
    }

    // MARK: - PHPicker

    private static func pickImage(_ completion: @escaping (Data?) -> Void) {
        var config = PHPickerConfiguration()
        config.filter = .images
        config.selectionLimit = 1
        let picker = PHPickerViewController(configuration: config)
        let delegate = PickerDelegate(completion: completion)
        picker.delegate = delegate
        retain(delegate, until: picker)
        guard let root = topViewController() else { completion(nil); return }
        root.present(picker, animated: true)
    }

    private final class PickerDelegate: NSObject, PHPickerViewControllerDelegate {
        private let completion: (Data?) -> Void
        init(completion: @escaping (Data?) -> Void) { self.completion = completion }

        func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
            picker.dismiss(animated: true)
            guard let provider = results.first?.itemProvider,
                  provider.hasItemConformingToTypeIdentifier(UTType.image.identifier) else {
                completion(nil)
                return
            }
            provider.loadDataRepresentation(forTypeIdentifier: UTType.image.identifier) { data, _ in
                DispatchQueue.main.async { self.completion(data) }
            }
        }
    }

    // MARK: - UIDocumentPicker

    private static func presentDocumentPicker(_ completion: @escaping (URL?) -> Void) {
        let picker = UIDocumentPickerViewController(forOpeningContentTypes: [.json, .zip, .item], asCopy: true)
        let delegate = DocumentPickerDelegate { url in
            completion(url)
        }
        picker.delegate = delegate
        retain(delegate, until: picker)
        guard let root = topViewController() else { completion(nil); return }
        root.present(picker, animated: true)
    }

    private final class DocumentPickerDelegate: NSObject, UIDocumentPickerDelegate {
        private let handler: (URL?) -> Void
        init(handler: @escaping (URL?) -> Void) { self.handler = handler }

        func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
            handler(urls.first)
        }

        func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
            handler(nil)
        }
    }

    // MARK: - Helpers

    /// 让 delegate 存活到 picker 展示周期结束（UIKit delegate 弱引用）
    private static func retain<D: AnyObject>(_ object: D, until controller: UIViewController) {
        objc_setAssociatedObject(controller, Unmanaged.passUnretained(object).toOpaque(), object, .OBJC_ASSOCIATION_RETAIN_NONATOMIC)
    }

    private static func topViewController() -> UIViewController? {
        guard let scene = UIApplication.shared.connectedScenes
            .compactMap({ $0 as? UIWindowScene })
            .first(where: { $0.activationState == .foregroundActive }) ??
            UIApplication.shared.connectedScenes.compactMap({ $0 as? UIWindowScene }).first,
            let root = scene.windows.first(where: { $0.isKeyWindow })?.rootViewController ??
                scene.windows.first?.rootViewController
        else { return nil }

        var top = root
        while let presented = top.presentedViewController {
            top = presented
        }
        return top
    }
}