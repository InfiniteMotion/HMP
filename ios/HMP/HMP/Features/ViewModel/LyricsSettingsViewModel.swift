import Foundation
import shared

/// 歌词设置 ViewModel - 包装 LyricsSettingsUseCase
class LyricsSettingsViewModel: ObservableObject {
    @Published var originalTextSize: Int = 14
    @Published var translatedTextSize: Int = 14
    @Published var currentTimeTextSize: Int = 16
    @Published var lineSpacing: Int = 6
    @Published var displayMode: DisplayMode = .dual
    @Published var alignment: LyricsAlignment = .center

    private let lyricsSettingsUseCase: LyricsSettingsUseCase

    init() {
        self.lyricsSettingsUseCase = KoinHelperKt.getLyricsSettingsUseCase()
    }

    func loadSettings() {
        Task {
            do {
                let config = try await lyricsSettingsUseCase.getLyricsConfig()
                await MainActor.run {
                    self.originalTextSize = Int(config.originalTextSize)
                    self.translatedTextSize = Int(config.translatedTextSize)
                    self.currentTimeTextSize = Int(config.currentTimeTextSize)
                    self.lineSpacing = Int(config.lineSpacing)
                    self.displayMode = config.displayMode
                    self.alignment = config.alignment
                }
            } catch {
                print("[LyricsSettingsVM] loadSettings failed: \(error)")
            }
        }
    }

    func saveOriginalTextSize(_ size: Int) {
        originalTextSize = size
        Task { try? await lyricsSettingsUseCase.saveOriginalTextSize(size: Int32(size)) }
    }

    func saveTranslatedTextSize(_ size: Int) {
        translatedTextSize = size
        Task { try? await lyricsSettingsUseCase.saveTranslatedTextSize(size: Int32(size)) }
    }

    func saveCurrentTimeTextSize(_ size: Int) {
        currentTimeTextSize = size
        Task { try? await lyricsSettingsUseCase.saveCurrentTimeTextSize(size: Int32(size)) }
    }

    func saveLineSpacing(_ spacing: Int) {
        lineSpacing = spacing
        Task { try? await lyricsSettingsUseCase.saveLineSpacing(spacing: Int32(spacing)) }
    }

    func saveDisplayMode(_ mode: DisplayMode) {
        displayMode = mode
        Task { try? await lyricsSettingsUseCase.saveDisplayMode(mode: mode) }
    }

    func saveAlignment(_ alignment: LyricsAlignment) {
        self.alignment = alignment
        Task { try? await lyricsSettingsUseCase.saveAlignment(alignment: alignment) }
    }

    func resetToDefault() {
        Task {
            try? await lyricsSettingsUseCase.resetToDefault()
            await loadSettings()
        }
    }
}
