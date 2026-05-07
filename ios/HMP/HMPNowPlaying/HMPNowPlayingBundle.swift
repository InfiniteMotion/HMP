import WidgetKit
import SwiftUI

@main
struct HMPNowPlayingBundle: WidgetBundle {
    var body: some Widget {
        HMPNowPlaying()
        HMPNowPlayingLiveActivity()
    }
}
