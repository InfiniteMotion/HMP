import Foundation
import shared

/// 集中 Dialog 状态管理 - 对应 Android DialogViewModel.kt
@Observable
class DialogViewModel {
    enum DialogType: Equatable {
        case musicDetail(MusicDetailDialogState)
        case createPlaylist(CreatePlaylistDialogState)
        case musicPicker(MusicPickerDialogState)
        case playlistPicker(PlaylistPickerDialogState)

        static func == (lhs: DialogType, rhs: DialogType) -> Bool {
            switch (lhs, rhs) {
            case (.musicDetail(let a), .musicDetail(let b)): return a == b
            case (.createPlaylist(let a), .createPlaylist(let b)): return a == b
            case (.musicPicker(let a), .musicPicker(let b)): return a == b
            case (.playlistPicker(let a), .playlistPicker(let b)): return a == b
            default: return false
            }
        }
    }

    var activeDialog: DialogType? = nil

    // 回调存储（不在 state 中，避免 Equatable 问题）
    var onMusicPickerConfirm: ((Set<Int64>) -> Void)? = nil
    var onPlaylistPickerConfirm: ((Playlist_) -> Void)? = nil
    var onPlaylistCreated: ((Int64) -> Void)? = nil

    // 嵌套 dialog 暂存（MusicPicker → CreatePlaylist 场景）
    private var pendingCreatePlaylistState: CreatePlaylistDialogState? = nil

    private let managePlaylistUseCase: ManagePlaylistUseCase

    init() {
        self.managePlaylistUseCase = KoinHelperKt.getManagePlaylistUseCase()
    }

    // MARK: - Show Dialogs

    func showMusicDetailDialog(
        musicId: Int64,
        title: String,
        artist: String,
        album: String,
        albumArtUri: String?,
        musicPath: String?,
        durationMs: Int64 = 0,
        menuConfig: MusicDetailMenuConfig = MusicDetailMenuConfig()
    ) {
        activeDialog = .musicDetail(MusicDetailDialogState(
            musicId: musicId,
            title: title,
            artist: artist,
            album: album,
            albumArtUri: albumArtUri,
            musicPath: musicPath,
            durationMs: durationMs,
            menuConfig: menuConfig
        ))
    }

    func showCreatePlaylistDialog(editingId: Int64? = nil, initialName: String = "") {
        activeDialog = .createPlaylist(CreatePlaylistDialogState(
            name: initialName,
            isEditing: editingId != nil,
            editingId: editingId,
            nameError: nil,
            isSubmitting: false
        ))
    }

    func showEditPlaylistDialog(id: Int64, currentName: String) {
        showCreatePlaylistDialog(editingId: id, initialName: currentName)
    }

    func showMusicPickerDialog(
        allMusic: [MusicInfo_],
        selectedIds: Set<Int64>,
        title: String,
        onConfirm: @escaping (Set<Int64>) -> Void
    ) {
        onMusicPickerConfirm = onConfirm
        activeDialog = .musicPicker(MusicPickerDialogState(
            allMusic: allMusic,
            selectedIds: selectedIds,
            title: title
        ))
    }

    func showPlaylistPickerDialog(
        playlists: [Playlist_],
        title: String = "选择播放列表",
        onSelect: @escaping (Playlist_) -> Void
    ) {
        onPlaylistPickerConfirm = onSelect
        activeDialog = .playlistPicker(PlaylistPickerDialogState(
            playlists: playlists,
            title: title
        ))
    }

    // MARK: - Actions

    func submitCreatePlaylist(name: String) async {
        guard case .createPlaylist(var state) = activeDialog else { return }

        let trimmed = name.trimmingCharacters(in: .whitespaces)
        guard !trimmed.isEmpty else {
            state.nameError = "名称不能为空"
            activeDialog = .createPlaylist(state)
            return
        }
        guard trimmed.count <= 30 else {
            state.nameError = "名称不能超过30个字符"
            activeDialog = .createPlaylist(state)
            return
        }

        state.isSubmitting = true
        state.nameError = nil
        activeDialog = .createPlaylist(state)

        do {
            if let editingId = state.editingId {
                try await managePlaylistUseCase.renamePlaylist(id: editingId, newName: trimmed)
                await MainActor.run {
                    self.dismiss()
                    self.onPlaylistCreated?(editingId)
                }
            } else {
                let id = try await managePlaylistUseCase.createPlaylist(name: trimmed)
                await MainActor.run {
                    self.dismiss()
                    self.onPlaylistCreated?(id.int64Value)
                }
            }
        } catch {
            await MainActor.run {
                state.isSubmitting = false
                state.nameError = "创建失败: \(error.localizedDescription)"
                self.activeDialog = .createPlaylist(state)
            }
        }
    }

    func confirmMusicPicker(selectedIds: Set<Int64>) {
        onMusicPickerConfirm?(selectedIds)
        onMusicPickerConfirm = nil
        dismiss()
    }

    func dismissMusicPicker() {
        onMusicPickerConfirm = nil
        dismiss()
    }

    func selectPlaylist(_ playlist: Playlist_) {
        onPlaylistPickerConfirm?(playlist)
        onPlaylistPickerConfirm = nil
        dismiss()
    }

    func dismiss() {
        activeDialog = nil
    }

    // MARK: - Pending state for dialog stacking

    func savePendingCreatePlaylist() {
        if case .createPlaylist(let state) = activeDialog {
            pendingCreatePlaylistState = state
        }
    }

    func restorePendingCreatePlaylist() {
        if let pending = pendingCreatePlaylistState {
            activeDialog = .createPlaylist(pending)
            pendingCreatePlaylistState = nil
        }
    }
}

// MARK: - Dialog States

struct MusicDetailDialogState: Equatable {
    let musicId: Int64
    let title: String
    let artist: String
    let album: String
    let albumArtUri: String?
    let musicPath: String?
    let durationMs: Int64
    let menuConfig: MusicDetailMenuConfig
}

struct MusicDetailMenuConfig: Equatable {
    var addToPlaylist: Bool = true
    var addToSpecificPlaylist: Bool = true
    var share: Bool = true
    var viewDetail: Bool = true
    var playNext: Bool = true
    var removeFromCurrentPlaylist: Bool = false
    var delete: Bool = false
    var favorite: Bool = true
}

struct CreatePlaylistDialogState: Equatable {
    var name: String = ""
    var isEditing: Bool = false
    var editingId: Int64? = nil
    var nameError: String? = nil
    var isSubmitting: Bool = false
}

struct MusicPickerDialogState: Equatable {
    let allMusic: [MusicInfo_]
    let selectedIds: Set<Int64>
    let title: String
}

struct PlaylistPickerDialogState: Equatable {
    let playlists: [Playlist_]
    let title: String

    static func == (lhs: PlaylistPickerDialogState, rhs: PlaylistPickerDialogState) -> Bool {
        lhs.title == rhs.title && lhs.playlists.count == rhs.playlists.count
    }
}
