import SwiftUI
import sharedIos

/// iOS 根视图（A6：「全面替换」入口切换）。
///
/// 从 SwiftUI MainScreen 切换到共享层 Compose 应用壳（AppRoot = MainShell 4 Tab +
/// 全部二级页），SwiftUI 页面文件保留待 A9/A10 清理。
struct ContentView: View {
    var body: some View {
        ComposeAppRootView()
            .ignoresSafeArea()
    }
}

struct ComposeAppRootView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        IosAppRootKt.createAppRootViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}