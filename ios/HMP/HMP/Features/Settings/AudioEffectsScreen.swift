import SwiftUI

/// 音效页面 - 对应 Android AudioEffectsScreen.kt
/// iOS 使用 SegmentedControl 大幅简化预设选择器
struct AudioEffectsScreen: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(HMPTheme.self) private var theme

    // 音效状态
    @State private var equalizerPreset: Int = 0
    @State private var bassBoostLevel: Int = 50
    @State private var isSurroundEnabled: Bool = false
    @State private var reverbPreset: Int = 0
    @State private var customLevels: [Float] = Array(repeating: 0.0, count: 5)

    private let eqPresets = ["正常", "流行", "摇滚", "爵士", "古典", "电子", "自定义"]
    private let reverbPresets = ["关闭", "小房间", "大房间", "音乐厅", "洞穴"]

    var body: some View {
        NavigationStack {
            Form {
                // MARK: - 均衡器预设
                Section("均衡器预设") {
                    Picker("", selection: $equalizerPreset) {
                        ForEach(0..<eqPresets.count, id: \.self) { i in
                            Text(eqPresets[i]).tag(i)
                        }
                    }
                    .pickerStyle(.segmented)
                }

                // MARK: - 自定义均衡器
                if equalizerPreset == eqPresets.count - 1 {
                    Section("自定义均衡器") {
                        ForEach(0..<customLevels.count, id: \.self) { band in
                            HStack {
                                Text("\(band * 100)Hz")
                                    .font(TypographyTokens.labelMedium)
                                    .frame(width: 60, alignment: .leading)

                                Slider(
                                    value: Binding(
                                        get: { Double(customLevels[band]) },
                                        set: { customLevels[band] = Float($0) }
                                    ),
                                    in: -12...12
                                )
                                .tint(theme.primary)

                                Text("\(Int(customLevels[band]))dB")
                                    .font(TypographyTokens.labelMedium)
                                    .frame(width: 50, alignment: .trailing)
                            }
                        }
                    }
                }

                // MARK: - 低音增强
                Section("低音增强") {
                    HStack {
                        Text("强度")
                        Slider(value: Binding(
                            get: { Double(bassBoostLevel) },
                            set: { bassBoostLevel = Int($0) }
                        ), in: 0...100)
                        .tint(theme.primary)

                        Text("\(bassBoostLevel)%")
                            .font(TypographyTokens.labelMedium)
                            .frame(width: 50, alignment: .trailing)
                    }
                }

                // MARK: - 环绕声
                Section("环绕声") {
                    Toggle("启用环绕声", isOn: $isSurroundEnabled)
                }

                // MARK: - 混响预设
                Section("混响") {
                    Picker("", selection: $reverbPreset) {
                        ForEach(0..<reverbPresets.count, id: \.self) { i in
                            Text(reverbPresets[i]).tag(i)
                        }
                    }
                    .pickerStyle(.segmented)
                }
            }
            .navigationTitle("音效设置")
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("关闭") { dismiss() }
                }
            }
        }
    }
}
