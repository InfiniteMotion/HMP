import Foundation
import shared

@Observable
class SongDetailViewModel {
    var state: UiState<SongDetailData_> = .idle
    private var currentMusicId: Int64?

    private let recommendationUseCase: GetDailyMusicRecommendationUseCase
    private let playbackHistoryUseCase: PlaybackHistoryUseCase

    init() {
        self.recommendationUseCase = KoinHelperKt.getGetDailyMusicRecommendationUseCase()
        self.playbackHistoryUseCase = KoinHelperKt.getPlaybackHistoryUseCase()
    }

    func load(musicId: Int64) {
        currentMusicId = musicId
        state = .loading
        Task {
            do {
                let rec = try await recommendationUseCase.getMusicWithExtraById(musicId: musicId)
                if let musicInfo = rec?.musicInfo {
                    let history = try await KoinHelperKt.getPlaybackHistory(
                        musicId: musicId, limit: 5
                    )
                    await MainActor.run {
                        self.state = .success(SongDetailData_(
                            musicInfo: musicInfo,
                            dailyMusicInfo: rec?.dailyMusicInfo,
                            playbackHistory: history
                        ))
                    }
                } else {
                    await MainActor.run { self.state = .error("Music not found") }
                }
            } catch {
                await MainActor.run { self.state = .error(error.localizedDescription) }
            }
        }
    }

    func retry() {
        if let id = currentMusicId { load(musicId: id) }
    }
}

struct SongDetailData_ {
    let musicInfo: MusicInfo_
    let dailyMusicInfo: DailyMusicInfo_?
    let playbackHistory: [PlaybackHistory_]
}
