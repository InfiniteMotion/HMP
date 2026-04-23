import SwiftUI

struct ContentView: View {
    @State private var theme = HMPTheme()

    var body: some View {
        MainTabView()
            .environment(theme)
    }
}
