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

    // AI Provider Config
    var providerApiKey: String = ""
    var providerModel: String = ""
    var isProviderConfigured: Bool = false
    var isTestingApi: Bool = false
    var apiTestResult: ApiTestResult?

    // Daily Refresh
    var dailyRefreshMode: String = "time"
    var dailyRefreshHours: Int = 24
    var dailyRefreshStartupCount: Int = 3

    // Batch Processing
    var pendingMusicCount: Int = 0
    var isProcessing: Bool = false
    var processingProgress: Float = 0.0

    // Lyrics Settings
    var lyricsOriginalTextSize: Int = 14
    var lyricsTranslatedTextSize: Int = 14
    var lyricsCurrentTimeTextSize: Int = 16
    var lyricsLineSpacing: Int = 6
    var lyricsDisplayMode: DisplayMode = .dual
    var lyricsAlignment: LyricsAlignment = .center

    private let userSettingsUseCase: UserSettingsUseCase
    private let recommendationUseCase: GetDailyMusicRecommendationUseCase

    init() {
        self.userSettingsUseCase = KoinHelperKt.getUserSettingsUseCase()
        self.recommendationUseCase = KoinHelperKt.getGetDailyMusicRecommendationUseCase()
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

                // AI Provider Config
                let config = try await userSettingsUseCase.getCurrentProviderConfig()
                let refreshStrategy = try await userSettingsUseCase.getDailyRefreshConfig()

                // Pending count
                let pending = try await KoinHelperKt.getMusicWithMissingExtraCount()

                await MainActor.run {
                    self.userName = name
                    self.customMode = mode
                    self.backgroundStyle = bgStyle
                    self.avatarUri = avatar ?? ""
                    self.currentAiProvider = provider
                    self.autoBatchProcess = autoBatch.boolValue

                    self.providerApiKey = config.apiKey
                    self.providerModel = config.model
                    self.isProviderConfigured = config.isConfigured

                    self.dailyRefreshMode = refreshStrategy.mode
                    self.dailyRefreshHours = Int(refreshStrategy.refreshHours)
                    self.dailyRefreshStartupCount = Int(refreshStrategy.startupCount)

                    self.pendingMusicCount = pending.intValue
                }
            } catch {
                print("[SettingsVM] loadSettings failed: \(error)")
            }
        }
    }

    // MARK: - Basic Settings

    func saveUserName(_ name: String) {
        userName = name
        Task { try? await userSettingsUseCase.saveUserName(name: name) }
    }

    func saveCustomMode(_ mode: String) {
        customMode = mode
        UserDefaults.standard.set(mode == "light" ? "light" : mode == "dark" ? "dark" : "system", forKey: "theme_mode")
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

    // MARK: - AI Provider

    func switchAiProvider(_ provider: AiProviderType) {
        currentAiProvider = provider
        Task {
            try? await userSettingsUseCase.setCurrentProvider(provider: provider)
            await loadProviderConfig(for: provider)
        }
    }

    func loadProviderConfig(for provider: AiProviderType? = nil) async {
        do {
            let target = provider ?? currentAiProvider
            let config = try await userSettingsUseCase.getProviderConfig(provider: target)
            await MainActor.run {
                self.providerApiKey = config.apiKey
                self.providerModel = config.model
                self.isProviderConfigured = config.isConfigured
            }
        } catch {
            print("[SettingsVM] loadProviderConfig failed: \(error)")
        }
    }

    func saveProviderConfig() {
        let config = AiProviderConfig(
            type: currentAiProvider,
            apiKey: providerApiKey,
            model: providerModel,
            isConfigured: !providerApiKey.isEmpty
        )
        Task {
            do {
                try await userSettingsUseCase.saveProviderConfig(config: config)
                await MainActor.run {
                    self.isProviderConfigured = !self.providerApiKey.isEmpty
                }
            } catch {
                print("[SettingsVM] saveProviderConfig failed: \(error)")
            }
        }
    }

    func testConnection() {
        let config = AiProviderConfig(
            type: currentAiProvider,
            apiKey: providerApiKey,
            model: providerModel,
            isConfigured: !providerApiKey.isEmpty
        )
        isTestingApi = true
        apiTestResult = nil
        Task {
            do {
                let success = try await recommendationUseCase.validateProviderApiKey(providerConfig: config)
                await MainActor.run {
                    self.isTestingApi = false
                    self.apiTestResult = success.boolValue ? .success : .error(message: "连接失败，请检查 API Key 和网络")
                }
            } catch {
                await MainActor.run {
                    self.isTestingApi = false
                    self.apiTestResult = .error(message: error.localizedDescription)
                }
            }
        }
    }

    // MARK: - Daily Refresh

    func saveDailyRefreshMode(_ mode: String) {
        dailyRefreshMode = mode
        Task { try? await userSettingsUseCase.saveDailyRefreshMode(mode: mode) }
    }

    func saveDailyRefreshHours(_ hours: Int) {
        dailyRefreshHours = hours
        Task { try? await userSettingsUseCase.saveDailyRefreshHours(hours: Int32(hours)) }
    }

    func saveDailyRefreshStartupCount(_ count: Int) {
        dailyRefreshStartupCount = count
        Task { try? await userSettingsUseCase.saveDailyRefreshStartupCount(count: Int32(count)) }
    }

    // MARK: - Batch Processing

    func refreshPendingCount() {
        Task {
            do {
                let count = try await KoinHelperKt.getMusicWithMissingExtraCount()
                await MainActor.run { self.pendingMusicCount = count.intValue }
            } catch {
                print("[SettingsVM] refreshPendingCount failed: \(error)")
            }
        }
    }

    func startBatchProcess() {
        isProcessing = true
        processingProgress = 0.0
        KoinHelperKt.resetAutoProcessState()
        Task {
            do {
                try await KoinHelperKt.autoProcessMissingExtra()
                await MainActor.run {
                    self.isProcessing = false
                    self.processingProgress = 1.0
                    self.refreshPendingCount()
                }
            } catch {
                await MainActor.run {
                    self.isProcessing = false
                }
                print("[SettingsVM] startBatchProcess failed: \(error)")
            }
        }
    }

    func pauseBatchProcess() {
        KoinHelperKt.pauseAutoProcess()
    }

    func resumeBatchProcess() {
        KoinHelperKt.resumeAutoProcess()
    }

    func cancelBatchProcess() {
        KoinHelperKt.cancelAutoProcess()
        isProcessing = false
        processingProgress = 0.0
    }
}

enum ApiTestResult {
    case success
    case error(message: String)
}

// MARK: - Lyrics Settings Extension

extension SettingsViewModel {
    func loadLyricsSettings() {
        Task {
            do {
                let config = try await KoinHelperKt.getLyricsSettingsUseCase().getLyricsConfig()
                await MainActor.run {
                    self.lyricsOriginalTextSize = Int(config.originalTextSize)
                    self.lyricsTranslatedTextSize = Int(config.translatedTextSize)
                    self.lyricsCurrentTimeTextSize = Int(config.currentTimeTextSize)
                    self.lyricsLineSpacing = Int(config.lineSpacing)
                    self.lyricsDisplayMode = config.displayMode
                    self.lyricsAlignment = config.alignment
                }
            } catch {
                print("[SettingsVM] loadLyricsSettings failed: \(error)")
            }
        }
    }

    func saveLyricsSettings() {
        let useCase = KoinHelperKt.getLyricsSettingsUseCase()
        Task {
            try? await useCase.saveOriginalTextSize(size: Int32(lyricsOriginalTextSize))
            try? await useCase.saveTranslatedTextSize(size: Int32(lyricsTranslatedTextSize))
            try? await useCase.saveCurrentTimeTextSize(size: Int32(lyricsCurrentTimeTextSize))
            try? await useCase.saveLineSpacing(spacing: Int32(lyricsLineSpacing))
            try? await useCase.saveDisplayMode(mode: lyricsDisplayMode)
            try? await useCase.saveAlignment(alignment: lyricsAlignment)
        }
    }

    func resetLyricsSettings() {
        Task {
            try? await KoinHelperKt.getLyricsSettingsUseCase().resetToDefault()
            await loadLyricsSettings()
        }
    }
}
