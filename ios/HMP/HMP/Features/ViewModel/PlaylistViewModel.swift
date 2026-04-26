import Foundation
import shared

@Observable
class PlaylistViewModel {
    // Label lists (name strings)
    var genrePlaylistName: [String] = []
    var moodPlaylistName: [String] = []
    var scenarioPlaylistName: [String] = []
    var languagePlaylistName: [String] = []
    var eraPlaylistName: [String] = []

    // User playlists
    var userCustomPlaylists: [Playlist_] = []
    var userPlaylistsState: UiState<[Playlist_]> = .idle

    // Selected playlist
    var selectedPlaylistMusic: [MusicInfo_] = []
    var selectedPlaylistState: UiState<[MusicInfo_]> = .idle

    // Artist / Album
    var selectedArtistMusic: [MusicInfo_] = []
    var selectedArtistMusicState: UiState<[MusicInfo_]> = .idle
    var selectedAlbumMusic: [MusicInfo_] = []
    var selectedAlbumMusicState: UiState<[MusicInfo_]> = .idle

    private let managePlaylistUseCase: ManagePlaylistUseCase
    private let musicLabelUseCase: MusicLabelUseCase
    private let getAllMusicUseCase: GetAllMusicUseCase

    init() {
        self.managePlaylistUseCase = KoinHelperKt.getManagePlaylistUseCase()
        self.musicLabelUseCase = KoinHelperKt.getMusicLabelUseCase()
        self.getAllMusicUseCase = KoinHelperKt.getGetAllMusicUseCase()
    }

    // MARK: - Labels

    func loadLabels() {
        loadLabelType(.genre, into: \.genrePlaylistName)
        loadLabelType(.mood, into: \.moodPlaylistName)
        loadLabelType(.scenario, into: \.scenarioPlaylistName)
        loadLabelType(.language, into: \.languagePlaylistName)
        loadLabelType(.era, into: \.eraPlaylistName)
    }

    private func loadLabelType(_ category: LabelCategory_, into keyPath: ReferenceWritableKeyPath<PlaylistViewModel, [String]>) {
        Task {
            do {
                let labels = try await KoinHelperKt.getLabelNamesByTypeFirst(category: category)
                await MainActor.run { self[keyPath: keyPath] = labels.map { $0.name } }
            } catch {
                print("[PlaylistVM] loadLabelType failed: \(error)")
            }
        }
    }

    // MARK: - User Playlists

    func loadUserCustomPlaylists() {
        userPlaylistsState = .loading
        Task {
            do {
                let playlists = try await managePlaylistUseCase.getAllPlaylists()
                await MainActor.run {
                    self.userCustomPlaylists = playlists
                    self.userPlaylistsState = .success(playlists)
                }
            } catch {
                await MainActor.run { self.userPlaylistsState = .error(error.localizedDescription) }
            }
        }
    }

    func createPlaylist(name: String, onCreated: ((Int64) -> Void)? = nil) {
        Task {
            do {
                let id = try await managePlaylistUseCase.createPlaylist(name: name)
                await MainActor.run { onCreated?(id.int64Value) }
                loadUserCustomPlaylists()
            } catch {
                print("[PlaylistVM] createPlaylist failed: \(error)")
            }
        }
    }

    func deletePlaylist(id: Int64) {
        Task {
            do {
                try await managePlaylistUseCase.removePlaylistById(id: id)
                loadUserCustomPlaylists()
            } catch {
                print("[PlaylistVM] deletePlaylist failed: \(error)")
            }
        }
    }

    func renamePlaylist(id: Int64, newName: String) {
        Task {
            do {
                try await managePlaylistUseCase.renamePlaylist(id: id, newName: newName)
                loadUserCustomPlaylists()
            } catch {
                print("[PlaylistVM] renamePlaylist failed: \(error)")
            }
        }
    }

    func setPlaylistPinned(id: Int64, isPinned: Bool) {
        Task {
            try? await managePlaylistUseCase.setPlaylistPinned(id: id, isPinned: isPinned)
            loadUserCustomPlaylists()
        }
    }

    // MARK: - Selected Playlist

    func loadPlaylistById(_ playlistId: Int64) {
        selectedPlaylistState = .loading
        Task {
            do {
                let music = try await managePlaylistUseCase.getPlaylistById(playlistId: playlistId)
                await MainActor.run {
                    self.selectedPlaylistMusic = music
                    self.selectedPlaylistState = music.isEmpty ? .empty : .success(music)
                }
            } catch {
                await MainActor.run { self.selectedPlaylistState = .error(error.localizedDescription) }
            }
        }
    }

    func loadPlaylistByLabel(_ labelName: LabelName_) {
        selectedPlaylistState = .loading
        Task {
            do {
                let music = try await musicLabelUseCase.getMusicListByLabel(labelName: labelName)
                await MainActor.run {
                    self.selectedPlaylistMusic = music
                    self.selectedPlaylistState = music.isEmpty ? .empty : .success(music)
                }
            } catch {
                await MainActor.run { self.selectedPlaylistState = .error(error.localizedDescription) }
            }
        }
    }

    func addItemToPlaylist(playlistId: Int64, musicId: Int64, musicPath: String) {
        Task {
            try? await managePlaylistUseCase.addToPlaylist(playlistId: playlistId, musicId: musicId, musicPath: musicPath)
        }
    }

    func removeItemFromPlaylist(musicId: Int64, playlistId: Int64) {
        Task {
            try? await managePlaylistUseCase.removeItemFromPlaylist(musicId: musicId, playlistId: playlistId)
            loadPlaylistById(playlistId)
        }
    }

    // MARK: - Artist / Album

    func loadArtistMusicList(artistName: String) {
        selectedArtistMusicState = .loading
        Task {
            do {
                let music = try await getAllMusicUseCase.getMusicListByArtist(artistName: artistName)
                await MainActor.run {
                    self.selectedArtistMusic = music
                    self.selectedArtistMusicState = music.isEmpty ? .empty : .success(music)
                }
            } catch {
                await MainActor.run { self.selectedArtistMusicState = .error(error.localizedDescription) }
            }
        }
    }

    func loadAlbumMusicList(albumName: String) {
        selectedAlbumMusicState = .loading
        Task {
            do {
                let music = try await getAllMusicUseCase.getMusicListByAlbum(albumName: albumName)
                await MainActor.run {
                    self.selectedAlbumMusic = music
                    self.selectedAlbumMusicState = music.isEmpty ? .empty : .success(music)
                }
            } catch {
                await MainActor.run { self.selectedAlbumMusicState = .error(error.localizedDescription) }
            }
        }
    }
}
