import UIKit

/// iOS AppDelegate — 在应用启动时初始化 Koin (KMP DI)
class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // 初始化 KMP Koin DI
        // 暂时注释掉，等 shared 模块编译修复后启用
        // let initializer = KoinInitializer()
        // initializer.init()
        return true
    }
}
