import Foundation
import shared

@Observable
class SearchViewModel {
    var searchState: UiState<[MusicInfo_]> = .idle

    private let searchMusicUseCase: SearchMusicUseCase

    init() {
        self.searchMusicUseCase = KoinHelperKt.getSearchMusicUseCase()
    }

    func searchMusic(query: String) {
        guard !query.trimmingCharacters(in: .whitespaces).isEmpty else {
            searchState = .idle
            return
        }
        searchState = .loading
        Task {
            do {
                let results = try await searchMusicUseCase.invoke(query: query)
                await MainActor.run {
                    searchState = results.isEmpty ? .empty : .success(results)
                }
            } catch {
                await MainActor.run {
                    searchState = .error(error.localizedDescription)
                }
            }
        }
    }
}
