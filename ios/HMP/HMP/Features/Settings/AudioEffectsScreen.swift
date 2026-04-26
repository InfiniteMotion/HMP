import SwiftUI
import shared

/// 音效页面 - 对应 Android AudioEffectsScreen.kt
struct AudioEffectsScreen: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(HMPTheme.self) private var theme
    @State private var viewModel = AudioEffectViewModel()

    private let eqPresets = ["正常", "流行", "摇滚", "爵士", "古典", "电子", "自定义"]
    private let reverbPresets = ["关闭", "小房间", "大房间", "音乐厅", "洞穴"]

    var body: some View {
        NavigationStack {
            Form {
                Section("均衡器预设") {
                    Picker("", selection: Binding(
                        get: { viewModel.equalizerPreset },
                        set: { viewModel.setEqualizerPreset($0) }
                    )) {
                        ForEach(0..<eqPresets.count, id: \.self) { Text(eqPresets[$0]).tag($0) }
                    }
                    .pickerStyle(.segmented)
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
