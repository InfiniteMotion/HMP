import UIKit

/// iOS AppDelegate — 在应用启动时初始化 Koin (KMP DI)
class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // 初始化 KMP Koin DI
        // 注意：doInitKoin() 需要在 shared framework 集成后可用
        // 当前暂时注释掉，等 P6 编译修复后启用
        // KoinModulesKt.doInitKoin()
        return true
    }
}
