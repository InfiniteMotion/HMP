import Foundation
import shared

@Observable
class RecommendationViewModel {
    var dailyMusic: MusicInfo_? = nil
    var heartbeatList: [MusicInfo_] = []
    var pendingMusicCount: Int = 0
    var isProcessing: Bool = false

    private let getDailyMusicRecommendationUseCase: GetDailyMusicRecommendationUseCase
    private let getAllMusicUseCase: GetAllMusicUseCase
    private let userSettingsUseCase: UserSettingsUseCase
    private let currentPlaybackUseCase: CurrentPlaybackUseCase

    init() {
        self.getDailyMusicRecommendationUseCase = KoinHelperKt.getGetDailyMusicRecommendationUseCase()
        self.getAllMusicUseCase = KoinHelperKt.getGetAllMusicUseCase()
        self.userSettingsUseCase = KoinHelperKt.getUserSettingsUseCase()
        self.currentPlaybackUseCase = KoinHelperKt.getCurrentPlaybackUseCase()
    }

    func getDailyMusicInfo() {
        Task {
            do {
                let dailyId = try await userSettingsUseCase.getCurrentDailyMusicId()
                if let id = dailyId {
                    let recommendation = try await getDailyMusicRecommendationUseCase.getMusicWithExtraById(musicId: id.int64Value)
                    if let rec = recommendation {
                        await MainActor.run { self.dailyMusic = rec.musicInfo }
                    }
                }
                if dailyMusic == nil {
                    let recommendation = try await getDailyMusicRecommendationUseCase.getRandomMusicWithExtra()
                    await MainActor.run { self.dailyMusic = recommendation.musicInfo }
                }
            } catch {
                print("[RecommendationVM] getDailyMusicInfo failed: \(error)")
            }
        }
    }

    func refreshDailyMusicInfo() {
        dailyMusic = nil
        getDailyMusicInfo()
    }

    func loadHeartbeatList() {
        Task {
            do {
                let currentMusicId = MusicPlayerController.shared.currentPlayingMusic?.music.id
                if let musicId = currentMusicId {
                    let similar = try await currentPlaybackUseCase.getSimilarSongsByWeightedLabels(musicId: musicId, limit: 10)
                    await MainActor.run { self.heartbeatList = similar }
                } else {
                    let allMusic = try await getAllMusicUseCase.invoke(orderBy: "title", orderType: "ASC")
                    await MainActor.run { self.heartbeatList = allMusic }
                }
            } catch {
                print("[RecommendationVM] loadHeartbeatList failed: \(error)")
            }
        }
    }

    func loadPendingCount() {
        Task {
            do {
                let count = try await KoinHelperKt.getMusicWithMissingExtraCount()
                await MainActor.run { self.pendingMusicCount = Int(count) }
            } catch {
                print("[RecommendationVM] loadPendingCount failed: \(error)")
            }
        }
    }

    func startAutoProcessWithCurrentProvider() {
        isProcessing = true
        Task {
            do {
                try await KoinHelperKt.autoProcessMissingExtra()
                await MainActor.run {
                    self.isProcessing = false
                    self.loadPendingCount()
                }
            } catch {
                await MainActor.run { self.isProcessing = false }
                print("[RecommendationVM] startAutoProcess failed: \(error)")
            }
        }
    }

    func pauseProcessing() {
        getDailyMusicRecommendationUseCase.pauseProcessing()
    }

    func resumeProcessing() {
        getDailyMusicRecommendationUseCase.resumeProcessing()
    }

    func cancelProcessing() {
        getDailyMusicRecommendationUseCase.cancelProcessing()
        isProcessing = false
    }
}
