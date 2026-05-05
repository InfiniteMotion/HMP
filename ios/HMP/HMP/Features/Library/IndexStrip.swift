import SwiftUI
import shared

/// 音乐列表快速索引条 - 对应 Android MusicListIndexStrip.kt
/// 支持字母模式 (A-Z) 和锚点模式 (duration/playCount/date)
struct IndexStrip: View {
    @Environment(HMPTheme.self) private var theme

    let musicList: [MusicInfo_]
    let isLetterMode: Bool
    let isReversed: Bool
    let orderBy: String

    @State private var currentLabel: String? = nil
    @State private var isDragging: Bool = false

    private let haptic = UIImpactFeedbackGenerator(style: .light)

    // MARK: - Letter Mode Data

    private var letters: [String] {
        var base: [String] = []
        for c in UnicodeScalar("A").value...UnicodeScalar("Z").value {
            if let scalar = UnicodeScalar(c) {
                base.append(String(Character(scalar)))
            }
        }
        base.append("#")
        return isReversed ? base.reversed() : base
    }

    private var letterToIndex: [String: Int] {
        var map: [String: Int] = [:]
        for (index, info) in musicList.enumerated() {
            let letter = getLetterForMusic(info)
            if map[letter] == nil {
                map[letter] = index
            }
        }
        return map
    }

    // MARK: - Anchor Mode Data

    private var anchors: [(label: String, index: Int)] {
        generateSmartAnchors()
    }

    private var currentAnchors: [(label: String, index: Int)] {
        isReversed ? anchors.reversed() : anchors
    }

    // MARK: - Body

    var body: some View {
        VStack(spacing: 0) {
            if isLetterMode {
                letterStrip
            } else {
                anchorStrip
            }
        }
        .frame(width: 20)
        .padding(.trailing, 10)
        .padding(.vertical, 8)
        .opacity(isDragging ? 1.0 : 0.6)
    }

    // MARK: - Letter Strip

    private var letterStrip: some View {
        VStack(spacing: 0) {
            ForEach(letters, id: \.self) { letter in
                let isActive = currentLabel == letter
                Text(letter)
                    .font(.system(size: isActive ? 14 : 11, weight: isActive ? .bold : .regular))
                    .foregroundColor(isActive ? theme.primary : theme.text.opacity(0.5))
                    .frame(maxWidth: .infinity)
                    .frame(height: 18)
                    .contentShape(Rectangle())
                    .onTapGesture {
                        jumpToLetter(letter)
                    }
            }
        }
        .gesture(
            DragGesture(minimumDistance: 0)
                .onChanged { value in
                    handleLetterDrag(value: value)
                }
                .onEnded { _ in
                    isDragging = false
                    currentLabel = nil
                }
        )
    }

    // MARK: - Anchor Strip

    private var anchorStrip: some View {
        VStack(spacing: 0) {
            ForEach(currentAnchors.indices, id: \.self) { i in
                let anchor = currentAnchors[i]
                let isActive = currentLabel == anchor.label
                Text(anchor.label)
                    .font(.system(size: isActive ? 12 : 10, weight: isActive ? .bold : .regular))
                    .foregroundColor(isActive ? theme.primary : theme.text.opacity(0.5))
                    .lineLimit(1)
                    .frame(maxWidth: .infinity)
                    .frame(height: 22)
                    .contentShape(Rectangle())
                    .onTapGesture {
                        jumpToIndex(anchor.index)
                    }
            }
        }
        .gesture(
            DragGesture(minimumDistance: 0)
                .onChanged { value in
                    handleAnchorDrag(value: value)
                }
                .onEnded { _ in
                    isDragging = false
                    currentLabel = nil
                }
        )
    }

    // MARK: - Letter Drag Handling

    private func handleLetterDrag(value: DragGesture.Value) {
        isDragging = true
        let totalHeight: CGFloat = CGFloat(letters.count) * 18
        let y = value.location.y
        let padding: CGFloat = 8
        let adjustedY = y - padding
        let index = Int((adjustedY / totalHeight) * CGFloat(letters.count))
        let clampedIndex = max(0, min(letters.count - 1, index))
        let letter = letters[clampedIndex]

        if currentLabel != letter {
            currentLabel = letter
            haptic.impactOccurred()
            jumpToLetter(letter)
        }
    }

    private func jumpToLetter(_ letter: String) {
        guard let index = letterToIndex[letter] else { return }
        NotificationCenter.default.post(
            name: .indexStripJump,
            object: nil,
            userInfo: ["index": index]
        )
    }

    // MARK: - Anchor Drag Handling

    private func handleAnchorDrag(value: DragGesture.Value) {
        isDragging = true
        let totalHeight: CGFloat = CGFloat(currentAnchors.count) * 22
        let y = value.location.y
        let padding: CGFloat = 8
        let adjustedY = y - padding
        let index = Int((adjustedY / totalHeight) * CGFloat(currentAnchors.count))
        let clampedIndex = max(0, min(currentAnchors.count - 1, index))
        let anchor = currentAnchors[clampedIndex]

        if currentLabel != anchor.label {
            currentLabel = anchor.label
            haptic.impactOccurred()
            jumpToIndex(anchor.index)
        }
    }

    private func jumpToIndex(_ index: Int) {
        NotificationCenter.default.post(
            name: .indexStripJump,
            object: nil,
            userInfo: ["index": index]
        )
    }

    // MARK: - Letter Extraction

    private func getLetterForMusic(_ info: MusicInfo_) -> String {
        return KoinHelperKt.getPinyinInitial(title: info.music.title)
    }

    // MARK: - Smart Anchor Generation

    private func generateSmartAnchors() -> [(label: String, index: Int)] {
        guard !musicList.isEmpty else { return [] }

        let count = musicList.count
        let anchorCount: Int
        if count < 20 { anchorCount = min(count, 6) }
        else if count < 100 { anchorCount = 8 }
        else { anchorCount = 12 }

        switch orderBy {
        case "duration":
            return generateDurationAnchors(anchorCount: anchorCount)
        case "playCount":
            return generatePlayCountAnchors(anchorCount: anchorCount)
        case "id":
            return generateDateAnchors(anchorCount: anchorCount)
        default:
            return generateGenericAnchors(anchorCount: anchorCount)
        }
    }

    private func generateDurationAnchors(anchorCount: Int) -> [(label: String, index: Int)] {
        var anchors: [(label: String, index: Int)] = []
        let step = max(1, musicList.count / anchorCount)
        for i in stride(from: 0, to: musicList.count, by: step) {
            let durationMs = musicList[i].music.duration
            let minutes = durationMs / 60000
            let seconds = (durationMs % 60000) / 1000
            let label = String(format: "%d:%02d", minutes, seconds)
            anchors.append((label: label, index: i))
        }
        return anchors
    }

    private func generatePlayCountAnchors(anchorCount: Int) -> [(label: String, index: Int)] {
        var anchors: [(label: String, index: Int)] = []
        let step = max(1, musicList.count / anchorCount)
        for i in stride(from: 0, to: musicList.count, by: step) {
            // playCount is not directly available on MusicInfo_, use a placeholder
            anchors.append((label: "\(i + 1)", index: i))
        }
        return anchors
    }

    private func generateDateAnchors(anchorCount: Int) -> [(label: String, index: Int)] {
        var anchors: [(label: String, index: Int)] = []
        let step = max(1, musicList.count / anchorCount)
        for i in stride(from: 0, to: musicList.count, by: step) {
            anchors.append((label: "#\(i + 1)", index: i))
        }
        return anchors
    }

    private func generateGenericAnchors(anchorCount: Int) -> [(label: String, index: Int)] {
        var anchors: [(label: String, index: Int)] = []
        let step = max(1, musicList.count / anchorCount)
        for i in stride(from: 0, to: musicList.count, by: step) {
            anchors.append((label: "\(i + 1)", index: i))
        }
        return anchors
    }
}

// MARK: - Notification Extension

extension Notification.Name {
    static let indexStripJump = Notification.Name("IndexStripJump")
}
