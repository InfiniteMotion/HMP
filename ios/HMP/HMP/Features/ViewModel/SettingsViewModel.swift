import Foundation
import shared

@Observable
class SettingsViewModel {
    var userName: String = ""
    var customMode: String = "default"
    var backgroundStyle: String = "FLUID"
    var avatarUri: String = ""
    var currentAiProvider: AiProviderType = .deepseek
    var autoBatchProcess: Bool = false

    private let userSettingsUseCase: UserSettingsUseCase

    init() {
        self.userSettingsUseCase = KoinHelperKt.getUserSettingsUseCase()
        loadSettings()
    }

    func loadSettings() {
        Task {
            do {
                let name = try await KoinHelperKt.getSettingsUserName()
                let mode = try await KoinHelperKt.getSettingsCustomMode()
                let bgStyle = try await KoinHelperKt.getSettingsBackgroundStyle()
                let avatar = try await userSettingsUseCase.getAvatarUri()
                let provider = try await userSettingsUseCase.getCurrentProvider()
                let autoBatch = try await KoinHelperKt.getSettingsAutoBatchProcess()

                await MainActor.run {
                    self.userName = name
                    self.customMode = mode
                    self.backgroundStyle = bgStyle
                    self.avatarUri = avatar ?? ""
                    self.currentAiProvider = provider
                    self.autoBatchProcess = autoBatch.boolValue
                }
            } catch {
                print("[SettingsVM] loadSettings failed: \(error)")
            }
        }
    }

    func saveUserName(_ name: String) {
        userName = name
        Task { try? await userSettingsUseCase.saveUserName(name: name) }
    }

    func saveCustomMode(_ mode: String) {
        customMode = mode
        Task { try? await userSettingsUseCase.saveThemeMode(mode: mode) }
    }

    func saveBackgroundStyle(_ style: String) {
        backgroundStyle = style
        Task { try? await userSettingsUseCase.saveBackgroundStyle(style: style) }
    }

    func saveAvatarUri(_ uri: String) {
        avatarUri = uri
        Task { try? await userSettingsUseCase.saveAvatarUri(uri: uri) }
    }

    func saveAutoBatchProcess(_ enabled: Bool) {
        autoBatchProcess = enabled
        Task { try? await userSettingsUseCase.saveAutoBatchProcess(enabled: enabled) }
    }

    func switchAiProvider(_ provider: AiProviderType) {
        currentAiProvider = provider
        Task { try? await userSettingsUseCase.setCurrentProvider(provider: provider) }
    }
}
