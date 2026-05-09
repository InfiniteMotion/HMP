import Foundation
import shared

@Observable
class RecommendationViewModel {
    var dailyMusic: MusicInfo_? = nil
    var dailyMusicInfo: DailyMusicInfo_? = nil
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

    // MARK: - Daily Music

    func getDailyMusicInfo() {
        Task {
            do {
                // Increment launch count + check should refresh
                try await userSettingsUseCase.incrementAppLaunchCount()
                let shouldRefresh = try await userSettingsUseCase.shouldRefreshDailyRecommendation()

                if shouldRefresh.boolValue {
                    print("[RecommendationVM] Should refresh daily recommendation")
                    await refreshDailyMusicInfoInternal()
                } else {
                    let savedMusicId = try await userSettingsUseCase.getCurrentDailyMusicId()
                    if let id = savedMusicId, id.int64Value > 0 {
                        let recommendation = try await getDailyMusicRecommendationUseCase.getMusicWithExtraById(musicId: id.int64Value)
                        if let rec = recommendation {
                            await MainActor.run {
                                self.dailyMusic = rec.musicInfo
                                self.dailyMusicInfo = rec.dailyMusicInfo
                            }
                            // When daily music loads, auto-refresh heartbeat
                            if let music = rec.musicInfo {
                                await refreshHeartbeatList(for: music)
                            }
                        }
                    }
                    // If saved is invalid or failed, refresh
                    if dailyMusic == nil {
                        print("[RecommendationVM] No valid saved daily music, refreshing")
                        await refreshDailyMusicInfoInternal()
                    }
                }
            } catch {
                print("[RecommendationVM] getDailyMusicInfo failed: \(error)")
                // Fallback: random music
                do {
                    let recommendation = try await getDailyMusicRecommendationUseCase.getRandomMusicWithExtra()
                    await MainActor.run {
                        self.dailyMusic = recommendation.musicInfo
                        self.dailyMusicInfo = recommendation.dailyMusicInfo
                        // labels available via recommendation.labels if needed
                    }
                    if let music = recommendation.musicInfo {
                        await refreshHeartbeatList(for: music)
                    }
                } catch {
                    print("[RecommendationVM] fallback also failed: \(error)")
                }
            }
        }
    }

    func refreshDailyMusicInfo() {
        dailyMusic = nil
        dailyMusicInfo = nil
        Task {
            await refreshDailyMusicInfoInternal()
        }
    }

    private func refreshDailyMusicInfoInternal() async {
        do {
            let recommendation = try await getDailyMusicRecommendationUseCase.getRandomMusicWithExtra()
            await MainActor.run {
                self.dailyMusic = recommendation.musicInfo
                self.dailyMusicInfo = recommendation.dailyMusicInfo
            }
            // Persist the new daily music ID and update refresh timestamp
            if let music = recommendation.musicInfo {
                try? await userSettingsUseCase.saveCurrentDailyMusicId(musicId: music.music.id)
                try? await userSettingsUseCase.updateLastDailyRefreshTimestamp()
                await refreshHeartbeatList(for: music)
            }
        } catch {
            print("[RecommendationVM] refreshDailyMusicInfo failed: \(error)")
        }
    }

    // MARK: - Heartbeat List

    func loadHeartbeatList() {
        Task {
            if let music = dailyMusic {
                await refreshHeartbeatList(for: music)
            }
        }
    }

    private func refreshHeartbeatList(for baseMusic: MusicInfo_) async {
        do {
            let similar = try await currentPlaybackUseCase.getSimilarSongsByWeightedLabels(
                musicId: baseMusic.music.id, limit: 10
            )
            await MainActor.run { self.heartbeatList = similar }
        } catch {
            print("[RecommendationVM] refreshHeartbeatList failed: \(error)")
            // Fallback to all music
            do {
                let allMusic = try await getAllMusicUseCase.invoke(orderBy: "title", orderType: "ASC")
                await MainActor.run { self.heartbeatList = allMusic }
            } catch {
                print("[RecommendationVM] fallback heartbeat also failed: \(error)")
            }
        }
    }

    // MARK: - Batch Processing

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
