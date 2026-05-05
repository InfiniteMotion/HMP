import SwiftUI
import shared

/// 音效页面 - 对应 Android AudioEffectsScreen.kt
struct AudioEffectsScreen: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(HMPTheme.self) private var theme
    @State private var viewModel = AudioEffectViewModel()

    private let eqPresets = ["正常", "流行", "摇滚", "爵士", "古典", "电子", "自定义"]
    private let reverbPresets = ["关闭", "小房间", "大房间", "音乐厅", "洞穴"]
    private let bandLabels = ["60", "230", "910", "3.6k", "14k"]

    var body: some View {
        NavigationStack {
            formContent
                .navigationTitle("音效设置")
                .toolbar {
                    ToolbarItem(placement: .topBarLeading) {
                        Button("关闭") { dismiss() }
                    }
                }
        }
    }

    @ViewBuilder
    private var formContent: some View {
        Form(content: {
            Section("均衡器预设") {
                Picker("", selection: Binding(
                    get: { viewModel.equalizerPreset },
                    set: { viewModel.setEqualizerPreset($0) }
                )) {
                    ForEach(0..<eqPresets.count, id: \.self) { Text(eqPresets[$0]).tag($0) }
                }
                .pickerStyle(.segmented)
            }

            if viewModel.equalizerPreset == 6 {
                Section("自定义均衡器") {
                    CustomEqualizerView(
                        bandLevels: viewModel.currentEqualizerBandLevels,
                        bandLabels: bandLabels,
                        minLevel: Float(viewModel.equalizerBandLevelRange.0),
                        maxLevel: Float(viewModel.equalizerBandLevelRange.1),
                        onBandChange: { band, level in
                            viewModel.setEqualizerBandLevel(band: Int32(band), level: level)
                        },
                        onReset: {
                            for i in 0..<viewModel.currentEqualizerBandLevels.count {
                                viewModel.setEqualizerBandLevel(band: Int32(i), level: 0)
                            }
                        }
                    )
                }
            }

            Section("低音增强") {
                HStack {
                    Text("强度")
                    Slider(value: Binding(
                        get: { Double(viewModel.bassBoostLevel) },
                        set: { viewModel.setBassBoost(Int($0)) }
                    ), in: 0...100)
                    .tint(theme.primary)
                    Text("\(viewModel.bassBoostLevel)%")
                        .font(TypographyTokens.labelMedium)
                        .frame(width: 50, alignment: .trailing)
                }
            }

            Section("环绕声") {
                Toggle("启用环绕声", isOn: Binding(
                    get: { viewModel.surroundSoundEnabled },
                    set: { viewModel.setSurroundSound($0) }
                ))
            }

            Section("混响") {
                Picker("", selection: Binding(
                    get: { viewModel.reverbPreset },
                    set: { viewModel.setReverb($0) }
                )) {
                    ForEach(0..<reverbPresets.count, id: \.self) { Text(reverbPresets[$0]).tag($0) }
                }
                .pickerStyle(.segmented)
            }
        })
    }
}

// MARK: - Custom Equalizer View

struct CustomEqualizerView: View {
    @Environment(HMPTheme.self) private var theme

    let bandLevels: [Float]
    let bandLabels: [String]
    let minLevel: Float
    let maxLevel: Float
    let onBandChange: (Int, Float) -> Void
    let onReset: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            HStack(alignment: .bottom, spacing: 0) {
                ForEach(0..<bandLevels.count, id: \.self) { band in
                    EqualizerBandView(
                        level: bandLevels[band],
                        minLevel: minLevel,
                        maxLevel: maxLevel,
                        label: bandLabels[band],
                        onChange: { level in onBandChange(band, level) }
                    )
                    .frame(maxWidth: .infinity)
                }
            }
            .frame(height: 220)

            Button("重置全部") {
                HapticManager.shared.click()
                onReset()
            }
            .font(TypographyTokens.bodySmall)
            .foregroundColor(theme.primary)
        }
        .padding(.vertical, 8)
    }
}

struct EqualizerBandView: View {
    @Environment(HMPTheme.self) private var theme

    let level: Float
    let minLevel: Float
    let maxLevel: Float
    let label: String
    let onChange: (Float) -> Void

    private let trackHeight: CGFloat = 180
    private let trackWidth: CGFloat = 8

    private var normalizedValue: CGFloat {
        CGFloat((level - minLevel) / (maxLevel - minLevel))
    }

    private var thumbOffset: CGFloat {
        trackHeight / 2 - normalizedValue * trackHeight
    }

    var body: some View {
        VStack(spacing: 4) {
            // Value label
            Text(formatLevel(level))
                .font(.system(size: 10))
                .foregroundColor(level > 0 ? .blue : level < 0 ? .red : .gray)
                .frame(height: 16)

            ZStack(alignment: .center) {
                // Background track
                RoundedRectangle(cornerRadius: 4)
                    .fill(theme.surface)
                    .frame(width: trackWidth, height: trackHeight)

                // Active track (from center to thumb)
                let center = trackHeight / 2
                let thumbY = (1 - normalizedValue) * trackHeight
                if thumbY < center {
                    // Positive: blue, above center
                    RoundedRectangle(cornerRadius: 4)
                        .fill(Color.blue)
                        .frame(width: trackWidth, height: center - thumbY)
                        .offset(y: -(center - thumbY) / 2)
                } else if thumbY > center {
                    // Negative: red, below center
                    RoundedRectangle(cornerRadius: 4)
                        .fill(Color.red)
                        .frame(width: trackWidth, height: thumbY - center)
                        .offset(y: (thumbY - center) / 2)
                }

                // Center line (0 dB)
                Rectangle()
                    .fill(theme.text.opacity(0.3))
                    .frame(width: 16, height: 2)

                // Thumb
                RoundedRectangle(cornerRadius: 4)
                    .fill(theme.primary)
                    .frame(width: 24, height: 28)
                    .offset(y: thumbOffset)
            }
            .frame(width: 40, height: trackHeight)
            .gesture(
                DragGesture(minimumDistance: 0)
                    .onChanged { value in
                        let normalizedY = 1 - (value.location.y / trackHeight)
                        let clamped = max(0, min(1, normalizedY))
                        let newLevel = minLevel + Float(clamped) * (maxLevel - minLevel)
                        onChange(newLevel)
                    }
            )

            // Frequency label
            Text(label)
                .font(.system(size: 10))
                .foregroundColor(theme.text.opacity(0.6))
        }
    }

    private func formatLevel(_ level: Float) -> String {
        if level > 0 { return "+\(Int(level))" }
        if level < 0 { return "\(Int(level))" }
        return "0"
    }
}
