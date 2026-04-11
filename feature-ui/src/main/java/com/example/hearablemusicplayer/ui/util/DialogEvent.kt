
package com.example.hearablemusicplayer.ui.util

import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.domain.playlist.Playlist

sealed class DialogEvent {
    data class Message(
        val message: String,
        val duration: MessageDuration = MessageDuration.Short
    ) : DialogEvent()

    data class MusicDetail(
        val musicInfo: MusicInfo,
        val onPlay: () -> Unit,
        val onAddToPlaylist: () -> Unit,
        val onFavorite: () -> Unit,
        val onShare: () -> Unit,
        val onDetail: () -> Unit,
        val onRemove: () -> Unit
    ) : DialogEvent()

    data class MusicPicker(
        val allMusic: List<MusicInfo>,
        val selectedIds: Set<Long> = emptySet(),
        val title: String,
        val onConfirm: (Set<Long>) -> Unit
    ) : DialogEvent()

    data class PlaylistPicker(
        val playlists: List<Playlist>,
        val title: String,
        val onConfirm: (Playlist) -> Unit
    ) : DialogEvent()

    data class Confirm(
        val title: String,
        val message: String,
        val onConfirm: () -> Unit,
        val onDismiss: () -> Unit = {}
    ) : DialogEvent()

    data class Input(
        val title: String,
        val hint: String = "",
        val initialValue: String = "",
        val isMultiline: Boolean = false,
        val onConfirm: (String) -> Unit,
        val onDismiss: () -> Unit = {}
    ) : DialogEvent()
}

enum class MessageDuration {
    Short,
    Long
}
