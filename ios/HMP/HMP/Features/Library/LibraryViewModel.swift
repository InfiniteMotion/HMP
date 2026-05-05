import Foundation
import shared

class LibraryViewModel: ObservableObject {
    @Published var musicList: [MusicInfo_] = []
    @Published var musicCount: Int32 = 0
    @Published var musicWithExtraCount: Int32 = 0
    @Published var isScanning: Bool = false
    @Published var errorMessage: String? = nil
    @Published var orderBy: String = "title"
    @Published var orderType: String = "ASC"

    private let getAllMusicUseCase = KoinHelperKt.getGetAllMusicUseCase()

    init() {
        loadMusic()
    }

    func loadMusic() {
        let ob = orderBy
        let ot = orderType
        getAllMusicUseCase.invoke(orderBy: ob, orderType: ot, completionHandler: { [weak self] result, error in
            DispatchQueue.main.async {
                guard let self = self else { return }
                self.objectWillChange.send()
                if let list = result {
                    self.musicList = list
                    self.musicCount = Int32(list.count)
                }
            }
        })
    }

    func selectSortOption(_ newOrderBy: String) {
        guard newOrderBy != orderBy else { return }
        orderBy = newOrderBy
        loadMusic()
    }

    func toggleSortOrder() {
        orderType = orderType == "ASC" ? "DESC" : "ASC"
        loadMusic()
    }

    func fullRescan() async {
        isScanning = true
        errorMessage = nil
        do {
            try await KoinHelperKt.getLoadMusicFromDeviceUseCase().invoke()
            loadMusic()
        } catch {
            self.errorMessage = error.localizedDescription
        }
        isScanning = false
    }

    func incrementalSync() async {
        isScanning = true
        errorMessage = nil
        do {
            try await KoinHelperKt.getSyncMusicFromDeviceIncrementalUseCase().invoke()
            loadMusic()
        } catch {
            self.errorMessage = error.localizedDescription
        }
        isScanning = false
    }
}
