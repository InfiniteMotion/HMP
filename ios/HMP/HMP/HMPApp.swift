import SwiftUI

@main
struct HMPApp: App {
    // Koin DI initialization via AppDelegate
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
