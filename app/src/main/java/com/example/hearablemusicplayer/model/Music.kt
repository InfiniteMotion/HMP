package com.example.hearablemusicplayer.model

import android.graphics.Bitmap
import android.net.Uri

data class Music(
    val id: Long?,
    val title: String,
    val artist: String,
    val album: String,
    val albumCover: Bitmap?,
    val year: String?,
    val duration: Long,
    val size: Long,
    val path: String,
    val musicUri: Uri?,
    val displayName: String,
    val albumId: String?,
    val artistId: String?,
    val isMusic: Boolean,
    val composer: String?,
    val genre: String?,
    val track: String?
)
