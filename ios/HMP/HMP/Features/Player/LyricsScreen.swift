import SwiftUI

/// 歌词页面 - 对应 Android LyricsScreen.kt
/// 逐行歌词显示，滚动高亮当前行
struct LyricsScreen: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(HMPTheme.self) private var theme

    @State private var currentLineIndex: Int = 0
    @State private var lyricsLines: [LyricLine] = []  // 占位
    @State private var hasTranslation: Bool = false
    @State private var showSettings: Bool = false

    var body: some View {
        NavigationStack {
            ZStack {
                if let music = getCurrentMusic() {
                    FluidBackgroundView(albumArtUri: music.albumArtUri ?? "")
                }
                Color.black.opacity(0.5)

                VStack {
                    if lyricsLines.isEmpty {
                        Text("暂无歌词")
                            .font(TypographyTokens.headlineMedium)
                            .foregroundColor(.white.opacity(0.6))
                            .padding(.top, 100)
                    } else {
                        ScrollViewReader { proxy in
                            ScrollView {
                                VStack(spacing: 12) {
                                    ForEach(Array(lyricsLines.enumerated()), id: \.element.time) { index, line in
                                        Text(line.text)
                                            .font(.system(
                                                size: index == currentLineIndex ? 22 : 17,
                                                weight: index == currentLineIndex ? .bold : .regular
                                            ))
                                            .foregroundColor(
                                                index == currentLineIndex
                                                    ? theme.primary
                                                    : .white.opacity(0.5)
                                            )
                                            .multilineTextAlignment(.center)
                                            .padding(.horizontal, 32)
                                            .padding(.vertical, 4)
                                            .id(index)
                                    }
                                }
                                .padding(.top, 300)
                                .padding(.bottom, 300)
                            }
                            .onChange(of: currentLineIndex) { _, newIndex in
                                withAnimation(.easeInOut(duration: 0.3)) {
                                    proxy.scrollTo(newIndex, anchor: .center)
                                }
                            }
                        }
                    }
                }
            }
            .ignoresSafeArea()
            .navigationTitle("歌词")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("关闭") { dismiss() }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        showSettings = true
                    } label: {
                        Image(systemName: "gear")
                            .foregroundColor(.white)
                    }
                }
            }
        }
    }

    private func getCurrentMusic() -> MusicItem? { nil }
}

// MARK: - 歌词模型
struct LyricLine: Identifiable {
    let id = UUID()
    let time: Double      // 秒
    let text: String
    let translation: String?
}

// MARK: - 高级歌词页面 (对应 Android AdvancedLyrics.kt)
struct AdvancedLyricsView: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(HMPTheme.self) private var theme
    @State private var fontSize: CGFloat = 18
    @State private var lineHeight: CGFloat = 36

    var body: some View {
        NavigationStack {
            LyricsScreen()
                .navigationTitle("高级歌词")
        }
        .presentationDetents([.large])
    }
}
