import Foundation
import shared

@Observable
class AudioEffectViewModel {
    var equalizerPreset: Int = 0
    var bassBoostLevel: Int = 0
    var surroundSoundEnabled: Bool = false
    var reverbPreset: Int = 0

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
                await MainActor.run {
                    self.equalizerPreset = Int(eq)
                    self.bassBoostLevel = Int(bass)
                    self.surroundSoundEnabled = surround.boolValue
                    self.reverbPreset = Int(reverb)
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
}
