import ActivityKit
import WidgetKit
import SwiftUI

@available(iOS 16.1, *)
struct HMPNowPlayingLiveActivity: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: HMPNowPlayingAttributes.self) { context in
            LockScreenLiveActivityView(context: context)
        } dynamicIsland: { context in
            DynamicIsland {
                DynamicIslandExpandedRegion(.leading) {
                    if let artwork = artworkFromData(context.state.artworkData) {
                        Image(uiImage: artwork)
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                            .frame(width: 64, height: 64)
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                    } else {
                        RoundedRectangle(cornerRadius: 8)
                            .fill(Color.gray.opacity(0.3))
                            .frame(width: 64, height: 64)
                            .overlay(
                                Image(systemName: "music.note")
                                    .foregroundColor(.white.opacity(0.6))
                            )
                    }
                }
                DynamicIslandExpandedRegion(.trailing) {
                    VStack(alignment: .trailing, spacing: 2) {
                        Text(context.state.title)
                            .font(.caption2)
                            .fontWeight(.semibold)
                            .lineLimit(1)
                        Text(context.state.artist)
                            .font(.caption2)
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }
                }
                DynamicIslandExpandedRegion(.center) {
                    HStack(spacing: 16) {
                        Button(action: {
                            NotificationCenter.default.post(name: .hmpPreviousTrack, object: nil)
                        }) {
                            Image(systemName: "backward.fill")
                                .font(.title3)
                        }

                        Button(action: {
                            NotificationCenter.default.post(
                                name: context.state.isPlaying ? .hmpPause : .hmpPlay,
                                object: nil
                            )
                        }) {
                            Image(systemName: context.state.isPlaying ? "pause.fill" : "play.fill")
                                .font(.title)
                        }

                        Button(action: {
                            NotificationCenter.default.post(name: .hmpNextTrack, object: nil)
                        }) {
                            Image(systemName: "forward.fill")
                                .font(.title3)
                        }
                    }
                    .foregroundColor(.white)
                }
                DynamicIslandExpandedRegion(.bottom) {
                    VStack(spacing: 4) {
                        ProgressView(
                            value: context.state.duration > 0
                                ? context.state.position / context.state.duration
                                : 0
                        )
                        .tint(.white.opacity(0.8))

                        if context.state.showLyrics,
                           let lyric = context.state.currentLyricLine, !lyric.isEmpty {
                            Text(lyric)
                                .font(.caption2)
                                .foregroundColor(.white.opacity(0.7))
                                .lineLimit(1)
                        }
                    }
                }
            } compactLeading: {
                if let artwork = artworkFromData(context.state.artworkData) {
                    Image(uiImage: artwork)
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(width: 20, height: 20)
                        .clipShape(RoundedRectangle(cornerRadius: 4))
                } else {
                    Image(systemName: "music.note")
                        .font(.system(size: 12))
                }
            } compactTrailing: {
                if context.state.showAnimation {
                    NowPlayingWaveformView(isPlaying: context.state.isPlaying)
                        .frame(width: 20, height: 16)
                } else {
                    Image(systemName: context.state.isPlaying ? "pause.fill" : "play.fill")
                        .font(.system(size: 12))
                }
            } minimal: {
                if let artwork = artworkFromData(context.state.artworkData) {
                    Image(uiImage: artwork)
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(width: 16, height: 16)
                        .clipShape(RoundedRectangle(cornerRadius: 3))
                } else {
                    Image(systemName: "music.note")
                        .font(.system(size: 10))
                }
            }
        }
    }
}

@available(iOS 16.1, *)
struct LockScreenLiveActivityView: View {
    let context: ActivityViewContext<HMPNowPlayingAttributes>

    var body: some View {
        HStack(spacing: 12) {
            if let artwork = artworkFromData(context.state.artworkData) {
                Image(uiImage: artwork)
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .frame(width: 56, height: 56)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
            } else {
                RoundedRectangle(cornerRadius: 8)
                    .fill(Color.gray.opacity(0.3))
                    .frame(width: 56, height: 56)
                    .overlay(
                        Image(systemName: "music.note")
                            .foregroundColor(.white.opacity(0.6))
                    )
            }

            VStack(alignment: .leading, spacing: 4) {
                Text(context.state.title)
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .lineLimit(1)
                Text(context.state.artist)
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .lineLimit(1)

                HStack(spacing: 20) {
                    Button(action: {
                        NotificationCenter.default.post(name: .hmpPreviousTrack, object: nil)
                    }) {
                        Image(systemName: "backward.fill")
                            .font(.body)
                    }
                    Button(action: {
                        NotificationCenter.default.post(
                            name: context.state.isPlaying ? .hmpPause : .hmpPlay,
                            object: nil
                        )
                    }) {
                        Image(systemName: context.state.isPlaying ? "pause.fill" : "play.fill")
                            .font(.title3)
                    }
                    Button(action: {
                        NotificationCenter.default.post(name: .hmpNextTrack, object: nil)
                    }) {
                        Image(systemName: "forward.fill")
                            .font(.body)
                    }
                }
                .foregroundColor(.white)
            }

            Spacer()
        }
        .padding()
    }
}

@available(iOS 16.1, *)
struct NowPlayingWaveformView: View {
    let isPlaying: Bool
    @State private var isAnimating = false

    var body: some View {
        HStack(spacing: 2) {
            ForEach(0..<3, id: \.self) { index in
                RoundedRectangle(cornerRadius: 1)
                    .fill(Color.white)
                    .frame(width: 3, height: barHeight(for: index))
            }
        }
        .animation(isPlaying ? .easeInOut(duration: 0.5).repeatForever() : .default, value: isAnimating)
        .onAppear {
            if isPlaying {
                isAnimating = true
            }
        }
        .onChange(of: isPlaying) { playing in
            isAnimating = playing
        }
    }

    private func barHeight(for index: Int) -> CGFloat {
        guard isAnimating else { return 4 }
        let heights: [CGFloat] = [8, 14, 6]
        return heights[index % heights.count]
    }
}

func artworkFromData(_ data: Data?) -> UIImage? {
    guard let data else { return nil }
    return UIImage(data: data)
}
