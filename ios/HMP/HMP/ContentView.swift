import SwiftUI

struct ContentView: View {
    @State private var theme = HMPTheme()

    var body: some View {
        MainScreen()
            .environment(theme)
    }
}
