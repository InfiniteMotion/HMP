import Foundation
import shared

@Observable
class AudioEffectViewModel {
    var equalizerPreset: Int = 0
    var bassBoostLevel: Int = 0
    var surroundSoundEnabled: Bool = false
    var reverbPreset: Int = 0
    var currentEqualizerBandLevels: [Float] = [0, 0, 0, 0, 0]

    let equalizerBandLevelRange: (Int, Int) = (-15, 15)

    private let settingsRepository: SettingsRepository

    init() {
        self.settingsRepository = KoinHelperKt.getSettingsRepository()
        loadSettings()
    }

    func loadSettings() {
        Task {
            do {
                let eq = try await KoinHelperKt.getSettingsEqualizerPreset()
                let bass = try await KoinHelperKt.getSettingsBassBoostLevel()
                let surround = try await KoinHelperKt.getSettingsIsSurroundSoundEnabled()
                let reverb = try await KoinHelperKt.getSettingsReverbPreset()
                let levels = try await KoinHelperKt.getSettingsCustomEqualizerLevels()
                await MainActor.run {
                    self.equalizerPreset = Int(eq)
                    self.bassBoostLevel = Int(bass)
                    self.surroundSoundEnabled = surround.boolValue
                    self.reverbPreset = Int(reverb)
                    if levels.size > 0 {
                        var swiftLevels: [Float] = []
                        for i in 0..<Int(levels.size) {
                            swiftLevels.append(levels.get(index: Int32(i)))
                        }
                        self.currentEqualizerBandLevels = swiftLevels
                    }
                }
            } catch {
                print("[AudioEffectVM] loadSettings failed: \(error)")
            }
        }
    }

    func setEqualizerPreset(_ preset: Int) {
        equalizerPreset = preset
        Task {
            try? await settingsRepository.saveEqualizerPreset(preset: Int32(preset))
        }
    }

    func setBassBoost(_ level: Int) {
        bassBoostLevel = level
        Task {
            try? await settingsRepository.saveBassBoostLevel(level: Int32(level))
        }
    }

    func setSurroundSound(_ enabled: Bool) {
        surroundSoundEnabled = enabled
        Task {
            try? await settingsRepository.saveSurroundSoundEnabled(enabled: enabled)
        }
    }

    func setReverb(_ preset: Int) {
        reverbPreset = preset
        Task {
            try? await settingsRepository.saveReverbPreset(preset: Int32(preset))
        }
    }

    func setEqualizerBandLevel(band: Int32, level: Float) {
        guard band >= 0 && band < currentEqualizerBandLevels.count else { return }
        currentEqualizerBandLevels[Int(band)] = level
        Task {
            try? await KoinHelperKt.saveSettingsCustomEqualizerLevels(levels: currentEqualizerBandLevels.toKotlinArray())
        }
    }
}

private extension Array where Element == Float {
    func toKotlinArray() -> KotlinFloatArray {
        let array = KotlinFloatArray(size: Int32(count))
        for (i, val) in enumerated() {
            array.set(index: Int32(i), value: val)
        }
        return array
    }
}
