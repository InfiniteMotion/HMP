import Foundation
import shared

@Observable
class LibraryViewModel {
    var musicList: [MusicInfo_] = []
    var isScanning: Bool = false
    var musicCount: Int32 = 0
    var errorMessage: String? = nil

    private let getAllMusicUseCase: GetAllMusicUseCase
    private let loadMusicFromDeviceUseCase: LoadMusicFromDeviceUseCase
    private let syncMusicFromDeviceIncrementalUseCase: SyncMusicFromDeviceIncrementalUseCase

    init() {
        self.getAllMusicUseCase = KoinHelperKt.getGetAllMusicUseCase()
        self.loadMusicFromDeviceUseCase = KoinHelperKt.getLoadMusicFromDeviceUseCase()
        self.syncMusicFromDeviceIncrementalUseCase = KoinHelperKt.getSyncMusicFromDeviceIncrementalUseCase()

        // Load existing music on init
        Task { await loadMusic() }
    }

    @MainActor
    func loadMusic() async {
        do {
            let list = try await getAllMusicUseCase.invoke(orderBy: "title", orderType: "ASC")
            self.musicList = list
            self.musicCount = Int32(list.count)
        } catch {
            print("[LibraryVM] loadMusic failed: \(error)")
        }
    }

    @MainActor
    func fullRescan() async {
        isScanning = true
        errorMessage = nil
        do {
            try await loadMusicFromDeviceUseCase.invoke()
            let list = try await getAllMusicUseCase.invoke(orderBy: "title", orderType: "ASC")
            self.musicList = list
            self.musicCount = Int32(list.count)
        } catch {
            self.errorMessage = error.localizedDescription
        }
        isScanning = false
    }

    @MainActor
    func incrementalSync() async {
        isScanning = true
        errorMessage = nil
        do {
            try await syncMusicFromDeviceIncrementalUseCase.invoke()
            let list = try await getAllMusicUseCase.invoke(orderBy: "title", orderType: "ASC")
            self.musicList = list
            self.musicCount = Int32(list.count)
        } catch {
            self.errorMessage = error.localizedDescription
        }
        isScanning = false
    }
}
