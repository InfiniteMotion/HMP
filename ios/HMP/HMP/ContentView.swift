import SwiftUI
import shared

struct ContentView: View {
    @Environment(\.colorScheme) private var systemColorScheme
    @State private var theme = HMPTheme()

    var body: some View {
        MainScreen()
            .environment(theme)
            .onAppear {
                theme.systemColorScheme = systemColorScheme
            }
            .onChange(of: systemColorScheme) { _, newScheme in
                theme.systemColorScheme = newScheme
            }
            .onChange(of: theme.mode) { _, _ in
                // Sync theme mode to DataStore whenever HMPTheme changes
                let modeStr: String
                switch theme.mode {
                case .light:  modeStr = "light"
                case .dark:   modeStr = "dark"
                case .system: modeStr = "default"
                }
                Task {
                    try? await KoinHelperKt.getUserSettingsUseCase().saveThemeMode(mode: modeStr)
                }
            }
            .preferredColorScheme(theme.mode == .system ? nil :
                theme.mode == .dark ? .dark : .light)
    }
}
