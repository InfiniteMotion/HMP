import SwiftUI
import shared

/// 歌词页面 - 对应 Android LyricsScreen.kt + AdvancedLyrics.kt
/// 支持双语歌词、播放同步、点击 seek、设置面板
struct LyricsScreen: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(HMPTheme.self) private var theme

    private var controller: MusicPlayerController { MusicPlayerController.shared }

    @State private var currentLineIndex: Int = 0
    @State private var lyricsLines: [LyricLineData] = []
    @State private var showSettings: Bool = false
    @State private var settingsVM = LyricsSettingsViewModel()
    @State private var positionTimer: Timer?

    var body: some View {
        NavigationStack {
            ZStack {
                Color.black.ignoresSafeArea()

                VStack {
                    if lyricsLines.isEmpty {
                        emptyState
                    } else {
                        lyricsContent
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
                        HapticManager.shared.click()
                        showSettings.toggle()
                    } label: {
                        Image(systemName: "gear")
                            .foregroundColor(.white)
                    }
                }
            }
            .sheet(isPresented: $showSettings) {
                NavigationStack {
                    ScrollView {
                        LyricsSettingsPanel(viewModel: settingsVM)
                    }
                    .background(theme.background)
                    .navigationTitle("歌词设置")
                    .navigationBarTitleDisplayMode(.inline)
                    .toolbar {
                        ToolbarItem(placement: .confirmationAction) {
                            Button("完成") { showSettings = false }
                        }
                    }
                }
                .presentationDetents([.medium])
            }
            .onAppear {
                settingsVM.loadSettings()
                parseLyrics()
                startPositionTimer()
            }
            .onDisappear {
                stopPositionTimer()
            }
        }
    }

    // MARK: - Empty State

    private var emptyState: some View {
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
    }

    // MARK: - Lyrics Content

    private var lyricsContent: some View {
        ScrollViewReader { proxy in
            ScrollView {
                LazyVStack(spacing: CGFloat(settingsVM.lineSpacing)) {
                    ForEach(Array(lyricsLines.enumerated()), id: \.element.id) { index, line in
                        lyricLineView(line: line, index: index)
                            .id(index)
                            .onTapGesture {
                                HapticManager.shared.click()
                                controller.seekTo(position: Int64(line.timestamp))
                                currentLineIndex = index
                            }
                    }
                }
                .padding(.top, 400)
                .padding(.bottom, 400)
            }
            .onChange(of: currentLineIndex) { _, newIndex in
                withAnimation(.easeInOut(duration: 0.3)) {
                    proxy.scrollTo(newIndex, anchor: .center)
                }
            }
        }
    }

    // MARK: - Lyric Line View

    @ViewBuilder
    private func lyricLineView(line: LyricLineData, index: Int) -> some View {
        let isCurrent = index == currentLineIndex
        let alpha: Double = isCurrent ? 1.0 : 0.5
        let scale: CGFloat = isCurrent ? 1.05 : 1.0

        VStack(spacing: 4) {
            // Original text
            if settingsVM.displayMode != .lang2 {
                Text(line.originalText)
                    .font(.system(
                        size: CGFloat(isCurrent ? settingsVM.currentTimeTextSize : settingsVM.originalTextSize),
                        weight: isCurrent ? .bold : .regular
                    ))
                    .foregroundColor(isCurrent ? theme.primary : .white.opacity(alpha))
                    .multilineTextAlignment(textAlignment)
                    .lineLimit(nil)
            }

            // Translated text
            if settingsVM.displayMode != .lang1, let translation = line.translatedText, !translation.isEmpty {
                Text(translation)
                    .font(.system(
                        size: CGFloat(isCurrent ? settingsVM.currentTimeTextSize - 2 : settingsVM.translatedTextSize),
                        weight: isCurrent ? .medium : .regular
                    ))
                    .foregroundColor(isCurrent ? theme.primary.opacity(0.8) : .white.opacity(alpha * 0.7))
                    .multilineTextAlignment(textAlignment)
                    .lineLimit(nil)
            }
        }
        .padding(.horizontal, 32)
        .padding(.vertical, 4)
        .scaleEffect(scale)
        .animation(.easeInOut(duration: 0.2), value: isCurrent)
    }

    private var textAlignment: TextAlignment {
        switch settingsVM.alignment {
        case .left: return .leading
        case .center: return .center
        case .right: return .trailing
        default: return .center
        }
    }

    // MARK: - Position Timer

    private func startPositionTimer() {
        positionTimer = Timer.scheduledTimer(withTimeInterval: 0.3, repeats: true) { _ in
            updateCurrentLine()
        }
    }

    private func stopPositionTimer() {
        positionTimer?.invalidate()
        positionTimer = nil
    }

    private func updateCurrentLine() {
        let positionMs = controller.currentPosition
        let positionSec = Double(positionMs) / 1000.0

        guard !lyricsLines.isEmpty else { return }

        // Binary search for current line
        var low = 0
        var high = lyricsLines.count - 1
        var result = 0

        while low <= high {
            let mid = (low + high) / 2
            if lyricsLines[mid].timestamp <= positionMs {
                result = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }

        if currentLineIndex != result {
            currentLineIndex = result
        }
    }

    // MARK: - Enhanced LRC Parser

    private func parseLyrics() {
        guard let raw = controller.currentMusicLyrics, !raw.isEmpty else { return }

        let parser = EnhancedLyricsParser()
        lyricsLines = parser.parse(raw)
    }
}

// MARK: - LyricLineData

struct LyricLineData: Identifiable {
    let id = UUID()
    let timestamp: Int64 // milliseconds
    let originalText: String
    let translatedText: String?
}

// MARK: - Enhanced Lyrics Parser

/// 增强歌词解析器 - 对应 Android EnhancedLyricsParser
/// 支持时间戳分组、双语检测、多行处理
class EnhancedLyricsParser {
    private let timestampPattern = try! NSRegularExpression(pattern: "^\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\]")

    func parse(_ raw: String) -> [LyricLineData] {
        var timestampMap: [Int64: [String]] = [:]

        for line in raw.components(separatedBy: "\n") {
            let trimmed = line.trimmingCharacters(in: .whitespaces)
            guard !trimmed.isEmpty else { continue }

            guard let (timestamp, text) = extractTimestamp(trimmed),
                  !text.isEmpty else { continue }

            if timestampMap[timestamp] == nil {
                timestampMap[timestamp] = []
            }
            timestampMap[timestamp]?.append(text)
        }

        var results: [LyricLineData] = []
        for (timestamp, texts) in timestampMap.sorted(by: { $0.key < $1.key }) {
            let lineData = processGroup(timestamp: timestamp, texts: texts)
            results.append(lineData)
        }

        return results
    }

    private func extractTimestamp(_ line: String) -> (Int64, String)? {
        let nsLine = line as NSString
        guard let match = timestampPattern.firstMatch(in: line, range: NSRange(location: 0, length: nsLine.length)) else {
            return nil
        }

        let minStr = nsLine.substring(with: match.range(at: 1))
        let secStr = nsLine.substring(with: match.range(at: 2))
        let msStr = nsLine.substring(with: match.range(at: 3))

        guard let mins = Int(minStr), let secs = Int(secStr) else { return nil }
        let ms: Int
        if msStr.count == 2 {
            ms = (Int(msStr) ?? 0) * 10
        } else {
            ms = Int(msStr) ?? 0
        }

        let timestamp = Int64(mins * 60_000 + secs * 1000 + ms)
        let text = nsLine.substring(from: match.range.location + match.range.length)
        return (timestamp, text)
    }

    private func processGroup(timestamp: Int64, texts: [String]) -> LyricLineData {
        switch texts.count {
        case 1:
            return analyzeSingleLine(timestamp: timestamp, text: texts[0])
        case 2:
            return analyzeDualLines(timestamp: timestamp, line1: texts[0], line2: texts[1])
        default:
            return extractBestPair(timestamp: timestamp, texts: texts)
        }
    }

    private func analyzeSingleLine(timestamp: Int64, text: String) -> LyricLineData {
        if mayContainDualLanguages(text) {
            let (primary, secondary) = splitByBoundaries(text)
            if !primary.isEmpty && !secondary.isEmpty {
                return LyricLineData(timestamp: timestamp, originalText: primary, translatedText: secondary)
            }
        }
        return LyricLineData(timestamp: timestamp, originalText: text, translatedText: nil)
    }

    private func analyzeDualLines(timestamp: Int64, line1: String, line2: String) -> LyricLineData {
        let lang1 = detectDominantLanguage(line1)
        let lang2 = detectDominantLanguage(line2)

        // If different languages, treat first as primary
        if lang1 != lang2 && lang1 != .unknown && lang2 != .unknown {
            return LyricLineData(timestamp: timestamp, originalText: line1, translatedText: line2)
        }

        // Same language — merge
        return LyricLineData(timestamp: timestamp, originalText: line1 + " " + line2, translatedText: nil)
    }

    private func extractBestPair(timestamp: Int64, texts: [String]) -> LyricLineData {
        // Try first + last as dual-language pair
        if texts.count >= 2 {
            let first = texts[0]
            let last = texts[texts.count - 1]
            let lang1 = detectDominantLanguage(first)
            let lang2 = detectDominantLanguage(last)
            if lang1 != lang2 && lang1 != .unknown && lang2 != .unknown {
                return LyricLineData(timestamp: timestamp, originalText: first, translatedText: last)
            }
        }

        // Fallback: join all as single text
        return LyricLineData(timestamp: timestamp, originalText: texts.joined(separator: " "), translatedText: nil)
    }

    // MARK: - Language Detection

    private enum Language {
        case english, chinese, japanese, korean, russian, mixed, unknown
    }

    private func mayContainDualLanguages(_ text: String) -> Bool {
        var scriptCounts: [String: Int] = ["cjk": 0, "latin": 0, "japanese": 0, "korean": 0]

        for scalar in text.unicodeScalars {
            let value = scalar.value
            if value >= 0x4E00 && value <= 0x9FFF {
                scriptCounts["cjk", default: 0] += 1
            } else if (value >= 0x41 && value <= 0x5A) || (value >= 0x61 && value <= 0x7A) {
                scriptCounts["latin", default: 0] += 1
            } else if value >= 0x3040 && value <= 0x30FF {
                scriptCounts["japanese", default: 0] += 1
            } else if value >= 0xAC00 && value <= 0xD7AF {
                scriptCounts["korean", default: 0] += 1
            }
        }

        let nonZero = scriptCounts.values.filter { $0 > 0 }.count
        return nonZero >= 2
    }

    private func splitByBoundaries(_ text: String) -> (String, String) {
        var cjkChars: [Character] = []
        var englishWords: [String] = []
        var currentWord = ""

        for char in text {
            if char.unicodeScalars.first.map({ $0.value >= 0x4E00 && $0.value <= 0x9FFF }) == true {
                if !currentWord.isEmpty {
                    englishWords.append(currentWord)
                    currentWord = ""
                }
                cjkChars.append(char)
            } else if char.isLetter {
                currentWord.append(char)
            } else {
                if !currentWord.isEmpty {
                    englishWords.append(currentWord)
                    currentWord = ""
                }
            }
        }
        if !currentWord.isEmpty {
            englishWords.append(currentWord)
        }

        let english = englishWords.joined(separator: " ")
        let chinese = String(cjkChars)
        return (english, chinese)
    }

    private func detectDominantLanguage(_ text: String) -> Language {
        var cjk = 0, latin = 0, japanese = 0, korean = 0, cyrillic = 0

        for scalar in text.unicodeScalars {
            let v = scalar.value
            if v >= 0x4E00 && v <= 0x9FFF { cjk += 1 }
            else if (v >= 0x41 && v <= 0x5A) || (v >= 0x61 && v <= 0x7A) { latin += 1 }
            else if v >= 0x3040 && v <= 0x30FF { japanese += 1 }
            else if v >= 0xAC00 && v <= 0xD7AF { korean += 1 }
            else if v >= 0x0400 && v <= 0x04FF { cyrillic += 1 }
        }

        let total = Double(cjk + latin + japanese + korean + cyrillic)
        guard total > 0 else { return .unknown }

        if Double(cjk) / total > 0.6 { return .chinese }
        if Double(latin) / total > 0.6 { return .english }
        if Double(japanese) / total > 0.6 { return .japanese }
        if Double(korean) / total > 0.6 { return .korean }
        if Double(cyrillic) / total > 0.6 { return .russian }

        return .mixed
    }
}
