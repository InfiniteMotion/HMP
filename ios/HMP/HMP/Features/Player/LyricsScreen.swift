import SwiftUI
import shared

/// 歌词页面 - 对应 Android LyricsScreen.kt
struct LyricsScreen: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(HMPTheme.self) private var theme

    private var controller: MusicPlayerController { MusicPlayerController.shared }

    @State private var currentLineIndex: Int = 0
    @State private var lyricsLines: [LyricLine] = []
    @State private var showSettings: Bool = false

    var body: some View {
        NavigationStack {
            ZStack {
                Color.black.ignoresSafeArea()
                Color.black.opacity(0.5).ignoresSafeArea()

                VStack {
                    if lyricsLines.isEmpty {
                        VStack(spacing: 12) {
                            Image(systemName: "music.note")
                                .font(.system(size: 40))
                                .foregroundColor(.white.opacity(0.4))
                            Text(controller.currentMusicLyrics ?? "暂无歌词")
                                .font(TypographyTokens.bodyLarge)
                                .foregroundColor(.white.opacity(0.6))
                                .multilineTextAlignment(.center)
                                .padding(.horizontal, 40)
                        }
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
            }
            .onAppear {
                parseLyrics()
            }
        }
    }

    private func parseLyrics() {
        guard let raw = controller.currentMusicLyrics, !raw.isEmpty else { return }
        // Simple LRC parser: [mm:ss.xx]text
        let pattern = "\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)"
        var lines: [LyricLine] = []
        for line in raw.components(separatedBy: "\n") {
            if let regex = try? NSRegularExpression(pattern: pattern),
               let match = regex.firstMatch(in: line, range: NSRange(line.startIndex..., in: line)) {
                let minStr = String(line[Range(match.range(at: 1), in: line)!])
                let secStr = String(line[Range(match.range(at: 2), in: line)!])
                let msStr = String(line[Range(match.range(at: 3), in: line)!])
                let text = String(line[Range(match.range(at: 4), in: line)!])
                let ms = msStr.count == 2 ? Int(msStr)! * 10 : Int(msStr)!
                let time = Double(minStr)! * 60 + Double(secStr)! + Double(ms) / 1000.0
                lines.append(LyricLine(time: time, text: text, translation: nil))
            }
        }
        lyricsLines = lines.sorted { $0.time < $1.time }
    }
}

struct LyricLine: Identifiable {
    let id = UUID()
    let time: Double
    let text: String
    let translation: String?
}
